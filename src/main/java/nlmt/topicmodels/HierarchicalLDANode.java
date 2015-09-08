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

import java.util.*;

/**
 * Implements a node on the tree formed by the Nested Chinese Restaurant Problem.
 */
public class HierarchicalLDANode
{
    // The identifier for the node - becomes the topic number
    private int id;

    // Stores the parent to this node
    private HierarchicalLDANode parent;

    // Stores the list of children spawned from this node
    private List<HierarchicalLDANode> children;

    // The number of children spawned by this node (saves calling children.size())
    private int numChildren;

    // The total number of documents in the collection used to build the HLDA model
    private int totalDocuments;

    // The set of documents that have visited this node in a path
    protected Set<Integer> documentsVisitingNode;

    // The mapper responsible for mapping nodes to indices
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    // The total number of words used on this node
    private int totalWords;

    // A count of a document and the number of times the vocab word
    // appears in that document, referenced as
    // documentWordCountArray[documentIndex][vocabIndex]
    private int [][] documentWordCountArray;

    // A count of the vocabulary words present in the node, referenced as
    // wordCounts[vocabIndex]
    private int [] wordCounts;

    // The total size of the vocabulary
    private int vocabularySize;

    // The total number of documents that have visited the node
    private int numDocumentsVisitingNode;

    /**
     * Alternate constructor used to create a node with no parent. Nodes
     * without parents are considered to be root nodes.
     *
     * @param totalDocuments the total number of documents that will be processed
     */
    public HierarchicalLDANode(int totalDocuments, int vocabularySize, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        this(null, totalDocuments, vocabularySize, nodeMapper);
    }

    public HierarchicalLDANode(HierarchicalLDANode parent, int totalDocuments, int vocabularySize, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        if (totalDocuments < 0) {
            throw new IllegalArgumentException("totalDocuments must be > 0");
        }
        this.totalDocuments = totalDocuments;
        this.vocabularySize = vocabularySize;
        this.parent = parent;
        this.nodeMapper = nodeMapper;
        children = new ArrayList<>();
        documentsVisitingNode = new HashSet<>();
        numChildren = 0;
        id = nodeMapper.addObject(this);
        totalWords = 0;
        numDocumentsVisitingNode = 0;
        documentWordCountArray  = new int[totalDocuments][vocabularySize];
        wordCounts = new int [vocabularySize];
    }

    /**
     * Spawns a new child on for this node, and returns the spawned child.
     *
     * @return the newly spawned node
     */
    public HierarchicalLDANode spawnChild() {
        HierarchicalLDANode child = new HierarchicalLDANode(this, totalDocuments, vocabularySize, nodeMapper);
        children.add(child);
        numChildren++;
        return child;
    }

    /**
     * Returns the list of children of this node.
     *
     * @return the children of this node
     */
    public List<HierarchicalLDANode> getChildren() {
        return children;
    }

    /**
     * Returns the number of children spawned by this node.
     *
     * @return the number of children spawned by this node
     */
    public int getNumChildren() {
        return numChildren;
    }

    /**
     * Returns the parent of this node. A null parent indicates that this
     * node is the root node.
     *
     * @return the parent node of this node
     */
    public HierarchicalLDANode getParent() {
        return parent;
    }

    /**
     * Returns true if this node is the root node, false otherwise.
     *
     * @return true if this is the root node
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Marks the fact that the specified document has visited the node.
     *
     * @param documentIndex the index of the document visiting the node
     */
    public void setVisited(int documentIndex) {
        if (!documentsVisitingNode.contains(documentIndex)) {
            numDocumentsVisitingNode++;
            documentsVisitingNode.add(documentIndex);
        }
    }

    /**
     * Removes the document from having visited the node.
     *
     * @param documentIndex the index of the document to remove
     */
    public void removeVisited(int documentIndex) {
        if (documentsVisitingNode.contains(documentIndex)) {
            numDocumentsVisitingNode--;
            documentsVisitingNode.remove(documentIndex);
        }
    }

    /**
     * Returns the Id of this node (which is basically the topic number).
     *
     * @return the topic number of the node
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the set of documents that have visited this node.
     *
     * @return the set of document indices that have visited the node
     */
    public Set<Integer> getDocumentsVisitingNode() {
        return documentsVisitingNode;
    }

    /**
     * Returns the total number of documents that have visited the node.
     *
     * @return the total number of documents visiting the node
     */
    public int getNumDocumentsVisitingNode() {
        return numDocumentsVisitingNode;
    }

    /**
     * Adds a word belonging to the specified document to the node.
     *
     * @param documentIndex the index of the document containing the word
     * @param wordIndexInVocab the index in the vocabulary of the word to add
     */
    public void addWord(int documentIndex, int wordIndexInVocab) {
        if (documentIndex > totalDocuments || wordIndexInVocab > vocabularySize) {
            return;
        }
        documentWordCountArray[documentIndex][wordIndexInVocab]++;
        wordCounts[wordIndexInVocab]++;
        totalWords++;
    }

    /**
     * Removes a word belonging to the specified document from the node.
     *
     * @param documentIndex the index of the document
     * @param wordIndexInVocab the index of the word in the vocabulary
     */
    public void removeWord(int documentIndex, int wordIndexInVocab) {
        if (documentIndex < 0 || documentIndex >= totalDocuments ||
                wordIndexInVocab < 0 || wordIndexInVocab >= vocabularySize) {
            return;
        }
        documentWordCountArray[documentIndex][wordIndexInVocab]--;
        if (documentWordCountArray[documentIndex][wordIndexInVocab] < 0) {
            documentWordCountArray[documentIndex][wordIndexInVocab] = 0;
        }
        wordCounts[wordIndexInVocab]--;
        if (wordCounts[wordIndexInVocab] < 0) {
            wordCounts[wordIndexInVocab] = 0;
        }
        totalWords--;
        if (totalWords < 0) {
            totalWords = 0;
        }
    }

    /**
     * Given a document, remove all instances of that document's words from
     * the node.
     *
     * @param documentIndex the index of the document to remove
     */
    public void removeDocumentWords(int documentIndex) {
        for (int wordIndexInVocab = 0; wordIndexInVocab < vocabularySize; wordIndexInVocab++) {
            int wordCount = documentWordCountArray[documentIndex][wordIndexInVocab];
            wordCounts[wordIndexInVocab] -= wordCount;
            totalWords -= wordCount;
            documentWordCountArray[documentIndex][wordIndexInVocab] = 0;
        }
    }

    /**
     * Counts the total occurrences of the specified word belonging to the
     * document.
     *
     * @param documentIndex the index of the document to check
     * @param wordIndexInVocab the index in the vocabulary of the word
     * @return the count of the word belonging to the document
     */
    public int getWordCountForDocument(int documentIndex, int wordIndexInVocab) {
        return (documentIndex >= totalDocuments || documentIndex < 0 ||
                wordIndexInVocab < 0 || wordIndexInVocab >= vocabularySize)
                        ? 0 : documentWordCountArray[documentIndex][wordIndexInVocab];
    }

    /**
     * Returns the total number of times the specified word appears in the
     * node across all documents.
     *
     * @param wordIndexInVocab the index of the word in the vocabulary
     * @return the total number of times the word occurs in the node
     */
    public int getWordCountAllDocuments(int wordIndexInVocab) {
        return (wordIndexInVocab < 0 || wordIndexInVocab >= vocabularySize) ? 0 : wordCounts[wordIndexInVocab];
    }

    /**
     * Returns the total number of word occurrences on this node.
     *
     * @return the total number of word occurrences
     */
    public int getTotalWordCount() {
        return totalWords;
    }

    /**
     * Removes this node from the parent's list of children.
     */
    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    /**
     * Given the <code>HierarchicalLDANode</code> that contains the document counts, determine the
     * mass that this node should have when determining whether to associate the specified
     * word with the topic.
     *
     * @param documentIndex the index of the document to consider
     * @param wordIndexInVocab the vocabulary index of the word
     * @param alpha the alpha smoothing parameter
     * @param eta the eta hyper-parameter
     * @return the weight associated with the topic
     */
    protected double getWeight(int documentIndex, int wordIndexInVocab, double alpha, double eta) {
        int wordTopicCount = getWordCountAllDocuments(wordIndexInVocab);
        int topicTotal = getTotalWordCount();
        int topicDocumentCount = getWordCountForDocument(documentIndex, wordIndexInVocab);
        double denominator = topicTotal + (topicTotal * eta);
        if (denominator == 0.0) {
            return 0.0;
        }
        return ((wordTopicCount + eta) / denominator) *
                (topicDocumentCount + alpha);
    }

    public String toString() {
        String result = "{" + id;

        if (!children.isEmpty()) {
            result += ": [";
            for (HierarchicalLDANode child : children) {
                result += child.toString();
            }
            result += "]";
        }
        result += "}";
        return result;
    }
}
