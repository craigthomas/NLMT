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

import nlmt.datatypes.BoundedPriorityQueue;
import nlmt.datatypes.IdentifierObjectMapper;
import nlmt.probfunctions.PMFSampler;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

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

    protected double etaTotal;

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
    public final static double DEFAULT_GAMMA = 1.0;

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
        etaTotal = 0.0;
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

        etaTotal = vocabulary.size() * eta;
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
        rootNode = new HierarchicalLDANode(totalDocs, vocabulary.size(), nodeMapper);
//        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
//            rootNode.setVisited(documentIndex);
//        }

        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
            documentPaths[documentIndex] = new HierarchicalLDAPath(rootNode, maxDepth);
            HierarchicalLDAPath path = documentPaths[documentIndex];

            // For each document, generate a path through the tree starting at the root node,
            // to a depth of maxDepth. Starting at the root node, check the popularity rating
            // of its children, and choose based on popularity the next node in the path
            List<Integer> newPath = new ArrayList<>();
            newPath.add(rootNode.getId());
            HierarchicalLDANode parent = rootNode;
            for (int level = 1; level < maxDepth; level++) {
                if (parent == null || parent.getNumChildren() == 0) {
                    newPath.add(-1);
                } else {
                    List<HierarchicalLDANode> children = parent.getChildren();
                    PMFSampler sampler = new PMFSampler(children.size() + 1);
                    for (HierarchicalLDANode child : children) {
                        double weight = child.getNumDocumentsVisitingNode() / (parent.getNumDocumentsVisitingNode() - 1 + gamma);
                        sampler.add(weight);
                    }
                    sampler.add(gamma / (parent.getNumDocumentsVisitingNode() - 1 + gamma));
                    int pathComponent = sampler.sample();
                    if (pathComponent == children.size()) {
                        newPath.add(-1);
                        parent = null;
                    } else {
                        newPath.add(children.get(pathComponent).getId());
                        parent = children.get(pathComponent);
                    }
                }
            }

            // Generate the new path through the tree
            System.out.println("Document " + documentIndex + " new path " + newPath);
            path.addPath(newPath, nodeMapper);
            path.addDocument(documentIndex);

            // Flag the nodes in the path as being visited
            for (int level = 1; level < maxDepth; level++) {
                path.getNode(level).setVisited(documentIndex);
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
     * @return the sum of the log gamma counts for each word not in the document
     */
    protected double getSumLogEtaPlusWordsNotInDocument(int documentIndex, HierarchicalLDANode node) {
        double result = 0.0;
        for (int vocabIndex : vocabulary.getIndexKeys()) {
            result += log(
                            eta + node.getWordCountAllDocuments(vocabIndex) -
                                    node.getWordCountForDocument(documentIndex, vocabIndex));
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
     * @return the log calculation
     */
    protected double getLogEtaSumPlusWordsNotInDocument(int documentIndex, HierarchicalLDANode node) {
        double result = etaTotal;
        for (int vocabIndex : vocabulary.getIndexKeys()) {
            result += node.getWordCountAllDocuments(vocabIndex) - node.getWordCountForDocument(documentIndex, vocabIndex);
        }
        result = log(result);
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
     * @param node the HierarchicalLDANode with the words
     * @return the log calculation
     */
    protected double getSumLogAllWords(HierarchicalLDANode node) {
        double result = 0.0;
        for (int vocabIndex : vocabulary.getIndexKeys()) {
            result += log(node.getWordCountAllDocuments(vocabIndex) + eta);
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
    protected double getLogEtaPlusSumAllWords(HierarchicalLDANode node) {
        return log(etaTotal + node.getTotalWordCount());
    }

    /**
     * Calculates the complete log likelihood for the given node and document index.
     *
     * @param documentIndex the index of the document
     * @param node the node with the words
     * @return the log likelihood of choosing this node
     */
    protected double getTopicLikelihood(int documentIndex, HierarchicalLDANode node) {
        double factor1 = getLogEtaSumPlusWordsNotInDocument(documentIndex, node);
        double factor2 = getSumLogEtaPlusWordsNotInDocument(documentIndex, node);
        double factor3 = getSumLogAllWords(node);
        double factor4 = getLogEtaPlusSumAllWords(node);

        return factor1 - factor2 + factor3 - factor4;
    }

    /**
     * Calculates the log-likelihood for the specified path, given the document
     * in question. The path is passed as a list of node ids (e.g. [0, 3, 12]).
     * The path may have an id element of -1, which indicates that a new node
     * should be created (e.g. [0, 3, -1]).
     *
     * @param documentIndex the index of the document to check
     * @param path the path elements to check
     * @return the log-likelihood of the specified path
     */
    protected double calculatePathLikelihood(int documentIndex, List<Integer> path) {
        double result = 0.0;
        for (int nodeId : path) {
            if (nodeId != -1) {
                HierarchicalLDANode node = nodeMapper.getObjectFromIndex(nodeId);
                result += getTopicLikelihood(documentIndex, node) + log(node.getNumDocumentsVisitingNode() / (documents.length - 1 + gamma));
            } else {
                HierarchicalLDANode emptyNode = new HierarchicalLDANode(documents.length, vocabulary.size(), new IdentifierObjectMapper<>());
                result += getTopicLikelihood(documentIndex, emptyNode) + log(gamma / (documents.length - 1 + gamma));
                break;
            }
        }
        return result;
    }

    /**
     * Given a list of paths through the tree, calculates the log-likelihood of
     * each potential path, converts back to probability space, and then selects
     * one randomly (based upon their total 'weight'). Each path in the list of
     * paths is specified by a series of node ids (e.g. [1, 4, 9]).
     *
     * @param documentIndex the index of the document to check
     * @param paths a list of paths
     * @return the index of the path chosen from the list
     */
    protected int chooseBestPath(int documentIndex, List<List<Integer>> paths) {
        double [] loglikelihoods = new double [paths.size()];
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
            loglikelihoods[pathIndex] = calculatePathLikelihood(documentIndex, paths.get(pathIndex));
        }
        //System.out.println("raw loglikelihoods " + Arrays.toString(loglikelihoods));

        double biggest = Double.NEGATIVE_INFINITY;
        for (double loglikelihood : loglikelihoods) {
            if (loglikelihood > biggest) {
                biggest = loglikelihood;
            }
        }

        PMFSampler sampler = new PMFSampler(loglikelihoods.length);
        double sum = 0.0;
        for (int index = 0; index < loglikelihoods.length; index++) {
            //sampler.add(loglikelihoods[index]);
            loglikelihoods[index] = exp(loglikelihoods[index] - biggest);
            sum += loglikelihoods[index];
        }
        //System.out.println("probability space " + Arrays.toString(loglikelihoods));

        for (double loglikelihood : loglikelihoods) {
            sampler.add(loglikelihood / sum);
        }

        //System.out.println("normalized loglikelihoods " + Arrays.toString(sampler.getProbabilities()));
        return sampler.sample();
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
            double weight = path.getNode(topicIndex).getWeight(documentIndex, wordIndexInVocab, alpha, eta, etaTotal);
            pmfSampler.add(weight);
        }
        return pmfSampler.sample();
    }

    /**
     * Scans the trees for nodes that have 0 documents, 0 words and no children,
     * and deletes them from the tree.
     */
    protected void deleteEmptyNodes() {
        List<Integer> nodesToDelete = new ArrayList<>();
        for (int nodeIndex : nodeMapper.getIndexKeys()) {
            HierarchicalLDANode node = nodeMapper.getObjectFromIndex(nodeIndex);
            if (node.getDocumentsVisitingNode().size() == 0 && node.getTotalWordCount() == 0 && node.getNumChildren() == 0) {
                node.removeFromParent();
                nodesToDelete.add(nodeIndex);
            }
        }
        nodesToDelete.forEach(nodeMapper::deleteIndex);
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
        System.out.println("Initialize");
        initialize();

        System.out.println("Gibbs Sampling");
        for (int iteration = 0; iteration < numIterations; iteration++) {
            System.out.println("Iteration " + iteration + ", number of nodes " + nodeMapper.size());
            for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {

                // Sample a new path for the document
                List<List<Integer>> paths = HierarchicalLDAPath.enumeratePaths(rootNode, maxDepth);
                int bestPath = chooseBestPath(documentIndex, paths);
                List<Integer> newPath = paths.get(bestPath);

                //System.out.println("document " + documentIndex + " Chose path " + newPathNodes);

                // Deallocate the document and all its words from the current path
                HierarchicalLDAPath path = documentPaths[documentIndex];
                path.removeDocument(documentIndex);
                path.addPath(newPath, nodeMapper);
                path.addDocument(documentIndex);

                // For every word in the document, assign it to a node (topic) in the path
                int[] words = documents[documentIndex].getWordArray();
                for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                    int newTopic = getNewTopic(documentIndex, words[wordIndex], path);
                    path.addWord(documentIndex, words[wordIndex], newTopic);
                    documents[documentIndex].setTopicForWord(wordIndex, path.getNode(newTopic).getId());
                }
            }

            deleteEmptyNodes();

            if (iteration % 10 == 0) {
                System.out.println("topics " + getTopics(5, 2));
                System.out.println("tree " + rootNode.toString());
            }
        }
    }

    /**
     * Returns the top <code>numWords</code> that best describe the topic.
     *
     * @param topicId the topic to scan
     * @param numWords the number of words to return
     * @return an List of Strings that are the words that describe the topic
     */
    public List<String> getTopWordsForTopic(int topicId, int numWords) {
        if (!nodeMapper.containsIndex(topicId)) {
            throw new IllegalArgumentException("topic does not exist");
        }
        if (numWords <= 0) {
            throw new IllegalArgumentException("numWords must be > 0");
        }
        HierarchicalLDANode node = nodeMapper.getObjectFromIndex(topicId);
        BoundedPriorityQueue<Integer> priorityQueue = new BoundedPriorityQueue<>(numWords);
        for (int wordIndex = 0; wordIndex < vocabulary.size(); wordIndex++) {
            priorityQueue.add(node.getWordCountAllDocuments(wordIndex), wordIndex);
        }
        return priorityQueue.getElements().stream().map(vocabulary::getObjectFromIndex).collect(Collectors.toList());
    }

    /**
     * Returns the list of words that describe each topic. The <code>minNumDocuments</code>
     * parameter controls what topics are displayed. Topics must have the minimum number
     * of documents specified within them to be considered a valid topic, otherwise they
     * will not appear in the list.
     *
     * @param numWords the number of words to fetch
     * @param minNumDocuments the minimum number of documents that must appear in the topic
     * @return the List of topics, each with a List of Strings that are the words for that topic
     */
    public Map<Integer, List<String>> getTopics(int numWords, int minNumDocuments) {
        Map<Integer, List<String>> topics = new HashMap<>();
        for (int key : nodeMapper.getIndexKeys()) {
            HierarchicalLDANode node = nodeMapper.getObjectFromIndex(key);
            if (node.getDocumentsVisitingNode().size() >= minNumDocuments) {
                topics.put(node.getId(), getTopWordsForTopic(node.getId(), numWords));
            }
        }
        return topics;
    }
}
