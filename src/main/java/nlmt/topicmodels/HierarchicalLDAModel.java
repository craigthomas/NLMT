/*
 * Copyright 2015 Craig Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nlmt.topicmodels;

import nlmt.datatypes.IdentifierObjectMapper;
import nlmt.probfunctions.PMFSampler;
import org.apache.commons.math3.special.Gamma;

import java.util.*;

import static java.lang.Math.exp;
import static java.lang.Math.log;

/**
 * Simple implementation of Hierarchical Latent Dirichlet Allocation using Gibbs
 * Sampling. The <code>HierarchicalLDANode</code> class is used to build the topic trees that are
 * described as part of the Nested Chinese Restaurant Problem. Each node in the tree
 * is essentially a topic.
 *
 * The tree is initialized randomly at first. Each document generates a random path
 * in the tree, up to a <code>maxDepth</code>. When initially generating a path, it
 * looks at the children along the parent node, and chooses the next node in the
 * path based on the popularity of the children (i.e. how many documents have already
 * been allocated to that node). It may generate new nodes in the tree. When the path
 * is generated, each word in the document is randomly assigned to a node in the path.
 *
 * When <code>doGibbsSampling</code> is called, it looks at each document's path, and
 * removes the documents and all of its words from the nodes in the path. Then, it
 * generates a new path for the document from the root of the tree to the
 * <code>maxDepth</code>. When generating paths, it calculates the probability of choosing
 * the next node based upon the words in the document, the words in the node, and the
 * popularity of the node. Once the path has been generated, it assigns each word in
 * the document to a node as normal Latent Dirichlet Allocation would work (the
 * number of topics is equal to the total length of the path).
 */
public class HierarchicalLDAModel
{
    // Keeps track of how deep the tree can become
    private int maxDepth;

    // The alpha smoothing parameter
    private double alpha;

    // The gamma hyper-parameter
    private double gamma;

    // The eta word-smoothing parameter
    private double eta;

    private double etaSum;

    // Keeps track of what topics have been assigned to the word in a document.
    // The documentIndex will range from 0 to the total number of documents
    protected Document [] documents;

    // Associates a word with a specific number
    protected IdentifierObjectMapper<String> vocabulary;

    // Used to produce random numbers
    private Random random;

    // Used to maps nodes to their indices (topic numbers)
    protected IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    // The paths that each document takes through the topic tree
    protected HierarchicalLDAPath [] documentPaths;

    // The root node of the topic tree
    private HierarchicalLDANode rootNode;

    // Used to sample from various distributions
    PMFSampler pmfSampler;

    // The default depth of the tree
    public final static int DEFAULT_MAX_DEPTH = 3;

    // The default value for the gamma hyper-parameter
    public final static double DEFAULT_GAMMA = 0.5;

    // The default value for the alpha smoothing parameter
    public final static double DEFAULT_ALPHA = 1.0;

    public HierarchicalLDAModel() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_ALPHA, DEFAULT_GAMMA);
    }

    public HierarchicalLDAModel(int maxDepth, double alpha, double gamma) {
        if (maxDepth <= 2) {
            throw new IllegalArgumentException("maxDepth must be > 2");
        }
        if (gamma < 0) {
            throw new IllegalArgumentException("gamma must be >= 0");
        }
        if (alpha < 0) {
            throw new IllegalArgumentException("alpha must be >= 0");
        }
        this.gamma = gamma;
        this.maxDepth = maxDepth;
        this.alpha = alpha;
        vocabulary = new IdentifierObjectMapper<>();
        documents = new Document[0];
        random = new Random();
        nodeMapper = new IdentifierObjectMapper<>();
        eta = 0.1;
        etaSum = 0.0;
        pmfSampler = new PMFSampler(maxDepth);
    }

    /**
     * Read in a list of documents. Each document is assumed to be a
     * list of Strings. Stopwords and other pre-processing should be
     * done prior to reading documents. The documents should be read before
     * running doGibbsSampling.
     *
     * @param documents the List of documents, each being a List of Strings
     */
    public void readDocuments(List<List<String>> documents) {
        this.documents = new Document[documents.size()];

        for (int i = 0; i < documents.size(); i++) {
            this.documents[i] = new Document(vocabulary);
            this.documents[i].readDocument(documents.get(i));
        }

        etaSum = vocabulary.size() * eta;
    }

    /**
     * Initialize all of the counters. For each document, generate a path
     * in the tree up to a maximum length of <code>maxDepth</code>. When the
     * path for the document has been created, randomly assign each word in the
     * document to one of the nodes in the path.
     */
    public void initialize() {
        int totalDocs = documents.length;
        documentPaths = new HierarchicalLDAPath[totalDocs];
        rootNode = new HierarchicalLDANode(gamma, totalDocs, nodeMapper);

        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
            documentPaths[documentIndex] = new HierarchicalLDAPath(rootNode, maxDepth);
            HierarchicalLDAPath path = documentPaths[documentIndex];

            // For each document, generate a path through the tree starting at the root node,
            // to a depth of maxDepth. Starting at the root node, check the popularity rating
            // of its children, an choose the one with the best popularity as the next node
            // in the path
            while (!path.atMaxDepth()) {
                HierarchicalLDANode currentNode = path.getCurrentNode();
                PMFSampler pmfSampler = currentNode.getChildPopularities();
                int nodeToFollow = pmfSampler.sample();
                if (currentNode.getNumChildren() == 0 || nodeToFollow == currentNode.getNumChildren()) {
                    path.addNode(currentNode.spawnChild());
                } else {
                    path.addNode(currentNode.getChildren().get(nodeToFollow));
                }
            }

            // For every word in the document, assign it to a random node in the path
            int [] words = documents[documentIndex].getWordArray();
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                int level = random.nextInt(maxDepth);
                path.addWord(documentIndex, words[wordIndex], level);
                documents[documentIndex].setTopicForWord(wordIndex, path.getNode(level).getId());
            }
        }
    }

    /**
     * Used to calculate a portion of the log likelihood for a topic given a document and a vocabulary
     * word. Separated out for easier testing. NB: calculating what words are in use in
     * a node is an expensive operation, so it appears as the <code>vocabulary</code>
     * parameter here even though we could call <code>node.getVocabularyPresent()</code>
     * to get it (in other words, it is better to call it once and cache the result
     * for the each component of the log likelihood calculation rather than call it
     * each time for each component).
     *
     * @param documentIndex the index of the document to consider
     * @param node the HierarchicalLDANode with the document
     * @param vocabulary the set of words occurring in the HierarchicalLDANode
     * @return the sum of the log gamma counts for each word not in the document
     */
    protected double getSumLogGammaWordsNotInDocument(int documentIndex, HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = 0.0;
        for (int vocabIndex : vocabulary) {
            result += log(
                    Gamma.gamma(
                            eta + node.getWordCountAllDocuments(vocabIndex) -
                                    node.getWordCountForDocument(documentIndex, vocabIndex)));
        }
        return result;
    }

    /**
     * Used to calculate a portion of the log likelihood for a topic given a document and a vocabulary
     * word. Separated out for easier testing. NB: calculating what words are in use in
     * a node is an expensive operation, so it appears as the <code>vocabulary</code>
     * parameter here even though we could call <code>node.getVocabularyPresent()</code>
     * to get it (in other words, it is better to call it once and cache the result
     * for the each component of the log likelihood calculation rather than call it
     * each time for each component).
     *
     * @param documentIndex the index of the document to consider
     * @param node the HierarchicalLDANode with the document
     * @param vocabulary the set of words occurring in the HierarchicalLDANode
     * @return the log calculation
     */
    protected double getLogGammaEtaPlusWordsNotInDocument(int documentIndex, HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = etaSum;
        for (int vocabIndex : vocabulary) {
            result += node.getWordCountAllDocuments(vocabIndex) - node.getWordCountForDocument(documentIndex, vocabIndex);
        }
        return log(Gamma.gamma(result));
    }

    /**
     * Used to calculate a portion of the log likelihood for a topic given a document and a vocabulary
     * word. Separated out for easier testing. NB: calculating what words are in use in
     * a node is an expensive operation, so it appears as the <code>vocabulary</code>
     * parameter here even though we could call <code>node.getVocabularyPresent()</code>
     * to get it (in other words, it is better to call it once and cache the result
     * for the each component of the log likelihood calculation rather than call it
     * each time for each component).
     *
     * @param node the HierarchicalLDANode with the words
     * @param vocabulary the set of words occurring in the HierarchicalLDANode
     * @return the log calculation
     */
    protected double getSumLogGammaAllWords(HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = 0.0;
        for (int vocabIndex : vocabulary) {
            result += log(
                    Gamma.gamma(
                            node.getWordCountAllDocuments(vocabIndex) + eta
                    ));
        }
        return result;
    }

    /**
     * Used to calculate a portion of the log likelihood for a topic given a document and a vocabulary
     * word. Separated out for easier testing.
     *
     * @param node the HierarchicalLDANode with the words
     * @return the log calculation
     */
    protected double getLogGammaEtaSumAllWords(HierarchicalLDANode node) {
        return log(Gamma.gamma(etaSum + node.getTotalWordCount()));
    }

    /**
     * Calculates the complete log likelihood for the given node and document index.
     *
     * @param documentIndex the index of the document
     * @param node the node with the words
     * @return the log likelihood of choosing this node
     */
    protected double getTopicLikelihood(int documentIndex, HierarchicalLDANode node) {
        Set<Integer> vocabularyUsedInTopic = node.getVocabularyPresent();

        double factor1 = getLogGammaEtaPlusWordsNotInDocument(documentIndex, node, vocabularyUsedInTopic);
        double factor2 = getSumLogGammaWordsNotInDocument(documentIndex, node, vocabularyUsedInTopic);
        double factor3 = getSumLogGammaAllWords(node, vocabularyUsedInTopic);
        double factor4 = getLogGammaEtaSumAllWords(node);

        return factor1 - factor2 + factor3 - factor4;
    }

    /**
     * Given a node in the Hierarchical LDA tree, choose the child that should
     * be used for the next path component. This effectively calculates the log
     * probability of the children along the path, taking into account the words
     * in the document, the words in the node, and the popularity of the node.
     * Will return an index between 0 and the number of children + 1. The last
     * index returned indicates that a new child node should be generated.
     *
     * @param documentIndex the index of the document to process
     * @param node the <code>HierarchicalLDANode</code> that is the parent
     * @return the child number to use as the next path component
     */
    public int getNewPathNode(int documentIndex, HierarchicalLDANode node) {
        PMFSampler sampler = new PMFSampler(node.getNumChildren() + 1);
        for (HierarchicalLDANode child : node.getChildren()) {
            sampler.add(exp(getTopicLikelihood(documentIndex, node)) * child.getPopularity());
        }

        // Calculate one for the empty node
        HierarchicalLDANode newNode = new HierarchicalLDANode(gamma, documents.length, new IdentifierObjectMapper<>());
        sampler.add(exp(getTopicLikelihood(documentIndex, newNode)) * node.getEmptyPopularity());
        return sampler.sample();
    }

    /**
     * Given the <code>HierarchicalLDANode</code> that contains the document counts, determine the
     * mass that this node should have when determining whether to associate the specified
     * word with the topic.
     *
     * @param documentIndex the index of the document to consider
     * @param wordIndexInVocab the vocabulary index of the word
     * @param node the <code>HierarchicalLDANode</code> with the word count information
     * @return the weight associated with the topic
     */
    protected double getTopicWeight(int documentIndex, int wordIndexInVocab, HierarchicalLDANode node) {
        int wordTopicCount = node.getWordCountAllDocuments(wordIndexInVocab);
        int topicTotal = node.getTotalWordCount();
        int topicDocumentCount = node.getWordCountForDocument(documentIndex, wordIndexInVocab);
        double denominator = topicTotal + (topicTotal * eta);
        if (denominator == 0.0) {
            return 0.0;
        }
        return ((wordTopicCount + eta) / denominator) *
                (topicDocumentCount + alpha);
    }

    /**
     * Determines what topic the word should belong to given the current topic counts
     * in the specified document. It looks at the nodes (topics or topic clusters) in
     * the path, and chooses one based upon the word distributions. This works identically
     * as it did in the LDAModel, except that the number of topics is limited to the
     * depth of the path.
     *
     * @param documentIndex the current document index being considered
     * @param wordIndexInVocab the word in the vocabulary to check
     * @param path the HierarchicalLDAPath to consider
     * @return the level of the node in the path that was chosen
     */
    protected int getNewTopic(int documentIndex, int wordIndexInVocab, HierarchicalLDAPath path) {
        pmfSampler.clear();
        for (int topicIndex = 0; topicIndex < maxDepth; topicIndex++) {
            HierarchicalLDANode node = path.getNode(topicIndex);
            if (node != null) {
                double weight = getTopicWeight(documentIndex, wordIndexInVocab, node);
                pmfSampler.add(weight);
            } else {
                pmfSampler.add(0.0);
            }
        }
        return pmfSampler.sample();
    }

    /**
     * Loops for the specified number of iterations. For each document, removes
     * the document from its current path, generates a new path through the tree,
     * removes each word of the document from its old node, and then calculates what
     * node in the new path the word should belong to. The result of Gibbs Sampling
     * will be that every document has a path. Each node in the path represents a
     * topic. Documents that share nodes are considered to be related to one another.
     * Level 0 of every path corresponds to the root node, which all documents
     * share.
     *
     * @param numIterations the number of times to perform Gibbs Sampling
     */
    public void doGibbsSampling(int numIterations) {
        int totalDocs = documents.length;

        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
            // Deallocate the document and all its words from the current path
            HierarchicalLDAPath path = documentPaths[documentIndex];
            path.removeDocumentWordsAndClear(documentIndex, documents[documentIndex].getWordSet());
            documents[documentIndex].clearTopics();

            // Sample a new path for the document, generating new nodes if necessary
            while (!path.atMaxDepth()) {
                HierarchicalLDANode currentNode = path.getCurrentNode();
                int newTopic = getNewPathNode(documentIndex, currentNode);
                if (currentNode.getNumChildren() == 0 || newTopic == currentNode.getNumChildren()) {
                    path.addNode(currentNode.spawnChild());
                } else {
                    path.addNode(currentNode.getChildren().get(newTopic));
                }
            }

            // For every word in the document, assign it to a node (topic) in the path
            int [] words = documents[documentIndex].getWordArray();
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                int newTopic = getNewTopic(documentIndex, words[wordIndex], path);
                path.addWord(documentIndex, words[wordIndex], newTopic);
                documents[documentIndex].setTopicForWord(wordIndex, path.getNode(newTopic).getId());
            }
        }
    }
}
