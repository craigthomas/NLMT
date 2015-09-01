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
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.special.Gamma;

import java.util.*;

/**
 * Simple implementation of Hierarchical Latent Dirichlet Allocation using Gibbs
 * Sampling. The HierarchicalLDANode class is used to build the topic trees that are
 * described as part of the Nested Chinese Restaurant Problem.
 */
public class HierarchicalLDAModel
{
    // Keeps track of how deep the tree can become
    private int maxDepth;

    // The gamma hyper-parameter
    private double gamma;

    // The eta word-smoothing parameter
    private double eta;

    private double etaSum;

    // Keeps track of what topics have been assigned to the word in a document,
    // associates each word with an index giving us wordIndexInDoc. The
    // documentIndex will range from 0 to the total number of documents
    protected Document [] documents;

    // Associates a word with a specific number, giving us wordIndexInVocab
    protected IdentifierObjectMapper<String> vocabulary;

    // Used to produce random numbers
    private Random random;

    // Used to maps nodes to their indices (topic numbers)
    protected IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    // The paths that each document takes through the topic tree
    protected HierarchicalLDAPath [] documentPaths;

    // The root node of the topic tree
    private HierarchicalLDANode rootNode;

    private Log log;

    public HierarchicalLDAModel(int maxDepth, double gamma) {
        if (maxDepth <= 2) {
            throw new IllegalArgumentException("maxDepth must be > 2");
        }
        if (gamma < 0) {
            throw new IllegalArgumentException("gamma must be >= 0");
        }
        this.gamma = (gamma == 0.0) ? 0.5 : gamma;
        this.maxDepth = maxDepth;
        vocabulary = new IdentifierObjectMapper<>();
        documents = new Document[0];
        random = new Random();
        nodeMapper = new IdentifierObjectMapper<>();
        log = new Log();
        eta = 0.1;
        etaSum = 0.0;
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
     * Initialize all of the counters. Simply loop through every word in
     * every document, and assign it a random topic. Update the global
     * counters to reflect the random assignments.
     */
    public void initialize() {
        int totalDocs = documents.length;
        documentPaths = new HierarchicalLDAPath[totalDocs];
        rootNode = new HierarchicalLDANode(gamma, totalDocs, nodeMapper);

        for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {
            documentPaths[documentIndex] = new HierarchicalLDAPath(rootNode, maxDepth);

            // For each document, generate a path through the tree starting at the root node,
            // to a depth of maxDepth. Starting at the root node, check the popularity rating
            // of its children, an choose the one with the best popularity as the next node
            // in the path
            while (!documentPaths[documentIndex].atMaxDepth()) {
                HierarchicalLDANode currentNode = documentPaths[documentIndex].getCurrentNode();
                PMFSampler pmfSampler = currentNode.getChildPopularities();
                int nodeToFollow = pmfSampler.sample();
                if (currentNode.getNumChildren() == 0 || nodeToFollow == currentNode.getNumChildren()) {
                    documentPaths[documentIndex].addNode(currentNode.spawnChild());
                } else {
                    documentPaths[documentIndex].addNode(currentNode.getChildren().get(nodeToFollow));
                }
            }

            // For every word in the document, assign it to a random node in the path
            int [] words = documents[documentIndex].getWordArray();
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                int topic = random.nextInt(maxDepth);
                HierarchicalLDANode node = documentPaths[documentIndex].getNode(topic);
                node.setVisited(documentIndex);
                node.addWord(documentIndex, words[wordIndex]);
                documents[documentIndex].setTopicForWord(wordIndex, node.getId());
            }
        }
    }

    public double getSumLogGammaWordsNotInDocument(int documentIndex, HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = 0.0;
        for (int vocabIndex : vocabulary) {
            result += log.value(
                    Gamma.gamma(
                            eta + node.getWordCountAllDocuments(vocabIndex) -
                            node.getWordCountForDocument(documentIndex, vocabIndex)));
        }
        return result;
    }

    public double getLogGammaEtaPlusWordsNotInDocument(int documentIndex, HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = etaSum;
        for (int vocabIndex : vocabulary) {
            result += node.getWordCountAllDocuments(vocabIndex) - node.getWordCountForDocument(documentIndex, vocabIndex);
        }
        return log.value(Gamma.gamma(result));
    }

    public double getSumLogGammaAllWords(HierarchicalLDANode node, Set<Integer> vocabulary) {
        double result = 0.0;
        for (int vocabIndex : vocabulary) {
            result += log.value(
                    Gamma.gamma(
                            node.getWordCountAllDocuments(vocabIndex) + eta
                    ));
        }
        return result;
    }

    public double getLogGammaEtaSumAllWords(HierarchicalLDANode node) {
        return log.value(Gamma.gamma(etaSum + node.getTotalWordCount()));
    }

    public double getTopicLikelihood(int currentDocumentIndex, HierarchicalLDANode node) {
        Set<Integer> vocabularyUsedInTopic = node.getVocabularyPresent();

        double factor1 = getLogGammaEtaPlusWordsNotInDocument(currentDocumentIndex, node, vocabularyUsedInTopic);
        double factor2 = getSumLogGammaWordsNotInDocument(currentDocumentIndex, node, vocabularyUsedInTopic);
        double factor3 = getSumLogGammaAllWords(node, vocabularyUsedInTopic);
        double factor4 = getLogGammaEtaSumAllWords(node);

        return factor1 - factor2 + factor3 - factor4;
    }
}
