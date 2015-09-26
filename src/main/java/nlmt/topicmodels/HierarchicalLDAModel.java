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

    // The gamma hyper-parameter
    private double gamma;

    // The eta word-smoothing parameter
    private double [] eta;

    // Stick breaking parameter to control proportion of specific / general words
    private double m;

    // Stick breaking parameter to control how strongly m is upheld
    private double pi = 100;

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

    // Records the number of times the word appears in the document at the specified level
    // documentWordLevel[documentIndex][wordIndex][level]
    protected int [][][] documentWordLevel;

    // Records the number of times a word in the document appears at the specified level
    // documentLevelTotals[documentIndex][level]
    protected int [][] documentLevelTotals;

    // The root node of the topic tree
    private HierarchicalLDANode rootNode;

    // Used to sample from various distributions
    PMFSampler pmfSampler;

    // The default depth of the tree
    public final static int DEFAULT_MAX_DEPTH = 3;

    // The default value for the gamma hyper-parameter
    public final static double DEFAULT_GAMMA = 1.0;

    // Default values for the eta hyper-parameter - favors more terms towards the root
    public final static double [] DEFAULT_ETA = {2.0, 1.0, 0.5};

    // The default value of pi - favors more specific words
    public final static double DEFAULT_PI = 100;

    // The default value of m - favors more specific words
    public final static double DEFAULT_M = 0.5;

    public HierarchicalLDAModel() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_GAMMA, DEFAULT_ETA, DEFAULT_M, DEFAULT_PI);
    }

    public HierarchicalLDAModel(int maxDepth, double gamma, double [] eta, double m, double pi) {
        if (maxDepth <= 2) {
            throw new IllegalArgumentException("maxDepth must be > 2");
        }
        if (gamma < 0.0) {
            throw new IllegalArgumentException("gamma must be >= 0");
        }
        if (eta.length < maxDepth) {
            throw new IllegalArgumentException("eta must have at least " + maxDepth + " values");
        }
        if (m <= 0.0) {
            throw new IllegalArgumentException("m must be > 0");
        }
        if (pi <= 0.0) {
            throw new IllegalArgumentException("pi must be > 0");
        }
        this.gamma = gamma;
        this.maxDepth = maxDepth;
        this.eta = eta;
        this.m = m;
        this.pi = pi;
        vocabulary = new IdentifierObjectMapper<>();
        documents = new Document[0];
        random = new Random();
        nodeMapper = new IdentifierObjectMapper<>();
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

        documentPaths = new HierarchicalLDAPath[documents.size()];
        rootNode = new HierarchicalLDANode(vocabulary.size(), nodeMapper);
        documentWordLevel = new int[documents.size()][vocabulary.size()][maxDepth];
        documentLevelTotals = new int[documents.size()][maxDepth];
    }

    /**
     * Initialize all of the counters. For each document, generate a path
     * in the tree up to a maximum length of <code>maxDepth</code>. When the
     * path for the document has been created, randomly assign each word in the
     * document to one of the nodes in the path.
     */
    public void initialize() {

        for (int documentIndex = 0; documentIndex < documents.length; documentIndex++) {
            rootNode.setVisited(documentIndex);
        }

        for (int documentIndex = 0; documentIndex < documents.length; documentIndex++) {
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
            path.addPath(newPath, nodeMapper);
            path.addDocument(documentIndex);

            // For every word in the document, assign it to a random node in the path
            int [] words = documents[documentIndex].getWordArray();
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                int level = random.nextInt(maxDepth);
                path.getNode(level).addWord(words[wordIndex]);
                documentWordLevel[documentIndex][words[wordIndex]][level]++;
                documentLevelTotals[documentIndex][level]++;
                documents[documentIndex].setTopicForWord(wordIndex, level);
            }
        }
    }

    /**
     * Given the specified document, calculates the complete log likelihood for
     * that document given the words in this path component, and the number of
     * documents that have visited the node.
     *
     * @param thisDocument the index of the document
     * @param node the node with the words
     * @return the log likelihood of choosing this node
     */
    protected double getPathWordsLikelihood(int thisDocument, int level, HierarchicalLDANode node) {
        double factor1 = 0.0;
        double factor2 = 0.0;
        double factor3 = 0.0;
        for (int vocabIndex : vocabulary.getIndexKeys()) {
            int wordCountAllDocuments = node.getWordCount(vocabIndex);
            if (node.getDocumentsVisitingNode().contains(thisDocument)) {
                factor1 += wordCountAllDocuments - documentWordLevel[thisDocument][vocabIndex][level];
                factor2 += log(eta[level] + wordCountAllDocuments - documentWordLevel[thisDocument][vocabIndex][level]);
            } else {
                factor1 += wordCountAllDocuments;
                factor2 += log(eta[level] + wordCountAllDocuments);
            }
            factor3 += log(wordCountAllDocuments + eta[level]);
        }

        factor1 = log((eta[level] * vocabulary.size()) + factor1);
        double factor4 = log((eta[level] * vocabulary.size()) + node.getTotalWordCount());

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
        for (int level = 1; level < maxDepth; level++) {
            int nodeId = path.get(level);
            if (nodeId != -1) {
                HierarchicalLDANode node = nodeMapper.getObjectFromIndex(nodeId);
                result += getPathWordsLikelihood(documentIndex, level, node) + log(node.getNumDocumentsVisitingNode() / (documents.length - 1 + gamma));
            } else {
                HierarchicalLDANode emptyNode = new HierarchicalLDANode(vocabulary.size(), new IdentifierObjectMapper<>());
                result += getPathWordsLikelihood(documentIndex, level, emptyNode) + log(gamma / (documents.length - 1 + gamma));
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

        double biggest = Double.NEGATIVE_INFINITY;
        for (double loglikelihood : loglikelihoods) {
            if (loglikelihood > biggest) {
                biggest = loglikelihood;
            }
        }

        PMFSampler sampler = new PMFSampler(loglikelihoods.length);
        double sum = 0.0;
        for (int index = 0; index < loglikelihoods.length; index++) {
            loglikelihoods[index] = exp(loglikelihoods[index] - biggest);
            sum += loglikelihoods[index];
        }

        for (double loglikelihood : loglikelihoods) {
            sampler.add(loglikelihood / sum);
        }

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
    protected int chooseNewLevel(int documentIndex, int wordIndexInVocab, HierarchicalLDAPath path) {
        pmfSampler.clear();
        for (int topicIndex = 0; topicIndex < maxDepth; topicIndex++) {
            double weight = getLevelProbability(documentIndex, wordIndexInVocab, path, topicIndex);
            pmfSampler.add(weight);
        }
        return pmfSampler.sample();
    }

    /**
     * Returns the probability of the document with the specified word existing at the specified
     * level along the specified path.
     *
     * @param documentIndex the index of the document to check
     * @param wordIndexInVocab the index of the word in the vocabulary to check
     * @param path the path to check
     * @param level the level to check
     * @return the probability of the word in the document existing at the specified level in the path
     */
    public double getLevelProbability(int documentIndex, int wordIndexInVocab, HierarchicalLDAPath path, int level) {
        double temp = pi;
        for (int i = level; i < maxDepth; i++) {
            temp += documentLevelTotals[documentIndex][i];
        }
        double factor1 = ((m * pi) + documentLevelTotals[documentIndex][level]) / temp;

        for (int i = 1; i < level - 1; i++) {
            double numerator = (1 - m) * pi;
            double denominator = pi;
            for (int j = i + 1; j < maxDepth; j++) {
                numerator += documentLevelTotals[documentIndex][j];
            }

            for (int j = i; j < maxDepth; j++) {
                denominator += documentLevelTotals[documentIndex][j];
            }
            factor1 *= (numerator / denominator);
        }
        HierarchicalLDANode node = path.getNode(level);
        if (node.getDocumentsVisitingNode().contains(documentIndex)) {
            return factor1 * ((node.getWordCount(wordIndexInVocab) - documentWordLevel[documentIndex][wordIndexInVocab][level]) + eta[level]);
        }
        return factor1 * ((node.getWordCount(wordIndexInVocab)) + eta[level]);
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
            System.out.println("Iteration " + iteration + ", number of nodes " + nodeMapper.size() + ", number of paths " + HierarchicalLDAPath.enumeratePaths(rootNode, maxDepth).size());
            for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {

                // Sample a new path for the document
                List<List<Integer>> paths = HierarchicalLDAPath.enumeratePaths(rootNode, maxDepth);
                int bestPath = chooseBestPath(documentIndex, paths);
                List<Integer> newPath = paths.get(bestPath);

                // Deallocate the document and all its words from the current path
                HierarchicalLDAPath path = documentPaths[documentIndex];
                path.removeDocument(documentIndex);
                int[] words = documents[documentIndex].getWordArray();
                int[] oldLevels = documents[documentIndex].getTopicArray();
                for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                    documentWordLevel[documentIndex][words[wordIndex]][oldLevels[wordIndex]]--;
                    documentLevelTotals[documentIndex][oldLevels[wordIndex]]--;
                    path.getNode(oldLevels[wordIndex]).removeWord(words[wordIndex]);
                }

                // Assign the new path
                path.addPath(newPath, nodeMapper);
                path.addDocument(documentIndex);

                // For every word in the document, assign a new level in the path
                for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                    int newLevel = chooseNewLevel(documentIndex, words[wordIndex], path);
                    documentWordLevel[documentIndex][words[wordIndex]][newLevel]++;
                    documentLevelTotals[documentIndex][newLevel]++;
                    documents[documentIndex].setTopicForWord(wordIndex, newLevel);
                    path.getNode(newLevel).addWord(words[wordIndex]);
                }

                HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
            }

            System.out.println("topics " + getTopics(5, 2));
            System.out.println("tree " + rootNode.toString());
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
            priorityQueue.add(node.getWordCount(wordIndex), wordIndex);
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
