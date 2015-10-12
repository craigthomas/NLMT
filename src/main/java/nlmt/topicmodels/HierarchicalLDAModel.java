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
import nlmt.datatypes.SparseDocument;
import nlmt.datatypes.Word;
import nlmt.probfunctions.PMFSampler;

import java.util.*;

import static org.apache.commons.math3.special.Gamma.logGamma;

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

    // The gamma hyper-parameter - controls how likely documents will choose new paths
    private double gamma;

    // The eta word-smoothing parameter - smaller values results in more topics
    private double [] eta;

    // Stick breaking parameter to control proportion of specific / general words
    private double m;

    // Stick breaking parameter to control how strongly m is upheld
    private double pi;

    // Keeps track of what topics have been assigned to the word in a document.
    // The documentIndex will range from 0 to the total number of documents
    protected SparseDocument[] documents;

    // Associates a word with a specific number
    protected IdentifierObjectMapper<String> vocabulary;

    // Used to produce random numbers
    private Random random;

    // Used to maps nodes to their indices (topic numbers)
    protected IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    // The paths that each document takes through the topic tree
    protected HierarchicalLDAPath [] documentPaths;

    // The root node of the topic tree
    protected HierarchicalLDANode rootNode;

    // Used to sample from various distributions
    PMFSampler pmfSampler;

    // The default depth of the tree
    public final static int DEFAULT_MAX_DEPTH = 3;

    // The default value for the gamma hyper-parameter
    public final static double DEFAULT_GAMMA = 1.0;

    // Default values for the eta hyper-parameter - favors more terms towards the root
    public final static double [] DEFAULT_ETA = {0.1, 0.1, 0.1};

    // The default value of pi - favors more specific words
    public final static double DEFAULT_PI = 2;

    // The default value of m - favors more specific words
    public final static double DEFAULT_M = 0.5;

    public HierarchicalLDAModel() {
        this(DEFAULT_MAX_DEPTH, DEFAULT_GAMMA, DEFAULT_ETA, DEFAULT_M, DEFAULT_PI);
    }

    public HierarchicalLDAModel(int maxDepth, double gamma, double [] eta, double m, double pi) {
        if (maxDepth < 2) {
            throw new IllegalArgumentException("maxDepth must be >= 2");
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
        documents = new SparseDocument[0];
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
        this.documents = new SparseDocument[documents.size()];

        for (int i = 0; i < documents.size(); i++) {
            this.documents[i] = new SparseDocument(vocabulary);
            this.documents[i].readDocument(documents.get(i));
        }

        documentPaths = new HierarchicalLDAPath[documents.size()];
        rootNode = new HierarchicalLDANode(vocabulary.size(), nodeMapper);
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
                        double weight = child.getNumDocumentsVisitingNode() / (documents.length - 1 + gamma);
                        sampler.add(weight);
                    }
                    sampler.add(gamma / (documents.length - 1 + gamma));
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
            Set<Word> words = documents[documentIndex].getWordSet();
            for (Word word : words) {
                int level = random.nextInt(maxDepth);
                path.getNode(level).addWord(word);
                word.setTopic(level);
            }
        }
    }

    /**
     * Given the specified document, calculates the complete log likelihood for
     * that document given the words in this path component, and the number of
     * documents that have visited the node.
     *
     * @param wordCountsAtLevel the total word counts for all vocabulary words in the document at this level
     * @param node the node with the words
     * @return the log likelihood of choosing this node
     */
    protected double getPathWordsLikelihood(Map<Integer, Integer> wordCountsAtLevel, Set<Word> documentWords, double eta, HierarchicalLDANode node) {
        double etaTotal = eta * vocabulary.size();
        int nodeTotalWordCount = node.getTotalWordCount();
        double result = logGamma(etaTotal + nodeTotalWordCount - wordCountsAtLevel.values().stream().mapToInt(v -> v).sum());
        result -= logGamma(etaTotal + nodeTotalWordCount);

        for (Word word : documentWords) {
            int vocabIndex = word.getVocabularyId();
            int wordCountAllDocuments = node.getWordCount(vocabIndex);
            result -= logGamma(eta + wordCountAllDocuments - wordCountsAtLevel.getOrDefault(vocabIndex, 0));
            result += logGamma(eta + wordCountAllDocuments);
        }
        return result;
    }

    /**
     * Calculates the probability for the specified path, given the document
     * in question. The path is passed as a list of node ids (e.g. [0, 3, 12]).
     * The path may have an id element of -1, which indicates that a new node
     * should be created (e.g. [0, 3, -1]). Assumes that the path weights have
     * already been propagated from the root node to all of the leaves.
     *
     * @param pathToConsider the List of nodes in a path to check
     * @return the probability of selecting the specified path
     */
    protected double calculatePathLikelihood(List<Integer> pathToConsider) {
        int nodeId = -1;
        int lastNode = maxDepth - 1;
        while (nodeId == -1) {
            nodeId = pathToConsider.get(lastNode);
            lastNode--;
        }
        return nodeMapper.getObjectFromIndex(nodeId).getPathWeight();
    }

    /**
     * Calculates the log-likelihood of the path given the words that occur in the
     * document. The path is passed as a list of node ids (e.g. [0, 3, 12]).
     * The path may have an id element of -1, which indicates that a new node
     * should be created (e.g. [0, 3, -1]).
     *
     * @param documentIndex the index of the document to consider
     * @param pathToConsider the path nodes to consider
     * @return the log-likelihood of the path
     */
    protected double calculateWordLikelihood(int documentIndex, List<Integer> pathToConsider) {
        double wordProbability = 0.0;
        Set<Word> documentWords = documents[documentIndex].getWordSet();
        for (int level = 0; level < maxDepth; level++) {
            int nodeId = pathToConsider.get(level);
            if (nodeId != -1) {
                HierarchicalLDANode node = nodeMapper.getObjectFromIndex(nodeId);
                // Don't include the document words if the document isn't mapped to the node!
                Map<Integer, Integer> wordCountsByLevel =
                        (documentPaths[documentIndex].getNode(level).getId() == nodeId) ? documents[documentIndex].getWordCountsByTopic(level) : new HashMap<>();
                wordProbability += getPathWordsLikelihood(wordCountsByLevel, documentWords, eta[level], node);
            }
        }
        return wordProbability;
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
        rootNode.propagatePathWeight(0.0, gamma, maxDepth);
        double [] logLikelihoods = new double [paths.size()];
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
            logLikelihoods[pathIndex] = (calculateWordLikelihood(documentIndex, paths.get(pathIndex)) + calculatePathLikelihood(paths.get(pathIndex)));
        }
        return PMFSampler.normalizeLogLikelihoods(logLikelihoods).sample();
    }

    /**
     * Determines what topic the word should belong to given the current topic counts
     * in the specified document. It looks at the nodes (topics or topic clusters) in
     * the path, and chooses one based upon the word distributions. This works identically
     * as it did in the LDAModel, except that the number of topics is limited to the
     * depth of the path.
     *
     * @param document the document to consider
     * @param word the word to check
     * @param path the HierarchicalLDAPath to consider
     * @return the level of the node in the path that was chosen
     */
    protected int chooseNewLevelForWord(SparseDocument document, Word word, HierarchicalLDAPath path) {
        pmfSampler.clear();
        Map<Integer, Integer> documentTopicCounts = document.getTopicCounts();
        double [] levelProbabilities = getLevelProbabilities(documentTopicCounts);
        double [] wordProbabilities = getWordProbabilities(word, path);

        for (int level = 0; level < maxDepth; level++) {
            pmfSampler.add(wordProbabilities[level] * levelProbabilities[level]);
        }
        return pmfSampler.sample();
    }

    /**
     * Calculates the smoothed probability of the word for each of the levels on the path. The
     * size of the returned array will have maxDepth items in it, one for each level.
     *
     * @param word the word to check
     * @param path the path to check
     * @return the Array of probabilities for the word at each level
     */
    public double [] getWordProbabilities(Word word, HierarchicalLDAPath path) {
        double [] probabilities = new double [maxDepth];
        for (int level = 0; level < maxDepth; level++) {
            HierarchicalLDANode node = path.getNode(level);
            probabilities[level] = (node.getWordCount(word.getVocabularyId()) + eta[level]) / (node.getTotalWordCount() + (vocabulary.size() * eta[level]));
        }
        return probabilities;
    }

    /**
     * Using the stick breaking convention and the count of number of words in the document
     * at the various levels, return an array containing the probabilities of each level.
     * In other words calculate p(level).
     *
     * @param documentTopicCounts the counts of the number of words in each level
     * @return an array of probabilities for each level
     */
    public double [] getLevelProbabilities(Map<Integer, Integer> documentTopicCounts) {
        double [] probabilities = new double [maxDepth];
        int totalWordCount = documentTopicCounts.values().stream().mapToInt(v -> v).sum();
        double remainingStickLength = 1.0;

        for (int i = 0; i < maxDepth - 1; i++) {
            int wordCountAtLevel = documentTopicCounts.getOrDefault(i, 0);
            double stickPiece = (((1 - m) * pi) + wordCountAtLevel) / (pi + totalWordCount);
            totalWordCount -= wordCountAtLevel;
            probabilities[i] = stickPiece * remainingStickLength;
            remainingStickLength *= 1.0 - stickPiece;
        }

        probabilities[maxDepth - 1] = 1.0 - Arrays.stream(probabilities).sum();
        return probabilities;
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
        initialize();

        for (int iteration = 0; iteration < numIterations; iteration++) {
            for (int documentIndex = 0; documentIndex < totalDocs; documentIndex++) {

                // Sample a new path for the document
                List<List<Integer>> paths = HierarchicalLDAPath.enumeratePaths(rootNode, maxDepth);
                int bestPath = chooseBestPath(documentIndex, paths);
                List<Integer> newPath = paths.get(bestPath);

                // Deallocate the document and all its words from the current path
                HierarchicalLDAPath path = documentPaths[documentIndex];
                path.removeDocument(documentIndex);
                Set<Word> words = documents[documentIndex].getWordSet();
                for (Word word : words) {
                    path.getNode(word.getTopic()).removeWord(word);
                    //word.setTopic(-1);
                }

                // Assign the new path
                path.addPath(newPath, nodeMapper);
                path.addDocument(documentIndex);

                // For every word in the document, assign a new level in the path
                for (Word word : words) {
                    int newLevel = chooseNewLevelForWord(documents[documentIndex], word, path);
                    word.setTopic(newLevel);
                    path.getNode(newLevel).addWord(word);
                }

                HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
            }
        }
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
                topics.put(key, node.getTopWords(numWords, vocabulary));
            }
        }
        return topics;
    }

    /**
     * Returns a string representation of the tree starting at the root.
     *
     * @param numWords the number of words to print per line
     */
    public String prettyPrintTree(int numWords) {
        return prettyPrintNode(rootNode, 0, numWords);
    }

    /**
     * Returns a string representation of the tree, formatted nicely, starting at the
     * specified node.
     *
     * @param node the node to be considered as the root node
     * @param indentationLevel the current indentation level
     * @param numWords the number of words to print per line
     */
    public String prettyPrintNode(HierarchicalLDANode node, int indentationLevel, int numWords) {
        String indents = "";
        String result = "";
        for (int i = 0; i <= indentationLevel; i++) {
            indents += "-";
        }
        result += indents + " Node " + node.getId() + ": ";
        result += node.getNumDocumentsVisitingNode() + " docs, words: ";
        result += node.getTopWords(numWords, vocabulary) + "\n";
        for (HierarchicalLDANode child : node.getChildren()) {
            result += prettyPrintNode(child, indentationLevel + 2, numWords);
        }
        return result;
    }
}
