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

    // The gamma tuning parameter
    private double gamma;

    // The set of documents that have visited this node in a path
    protected Set<Integer> documentsVisitingNode;

    // The mapper responsible for mapping nodes to indices
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    private int totalWords;

    private Map<Integer, Map<Integer, Integer>> documentWordCount;

    /**
     * Alternate constructor used to create a node with no parent. Nodes
     * without parents are considered to be root nodes.
     *
     * @param gamma the gamma parameter
     * @param totalDocuments the total number of documents that will be processed
     */
    public HierarchicalLDANode(double gamma, int totalDocuments, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        this(null, gamma, totalDocuments, nodeMapper);
    }

    public HierarchicalLDANode(HierarchicalLDANode parent, double gamma, int totalDocuments, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        if (totalDocuments < 0) {
            throw new IllegalArgumentException("totalDocuments must be > 0");
        }
        if (gamma <= 0.0) {
            throw new IllegalArgumentException("gamma must be > 0");
        }
        this.gamma = gamma;
        this.totalDocuments = totalDocuments;
        this.parent = parent;
        this.nodeMapper = nodeMapper;
        children = new ArrayList<>();
        documentsVisitingNode = new HashSet<>();
        numChildren = 0;
        id = nodeMapper.addObject(this);
        documentWordCount = new HashMap<>();
        totalWords = 0;
    }

    /**
     * Spawns a new child on for this node, and returns the spawned child.
     *
     * @return the newly spawned node
     */
    public HierarchicalLDANode spawnChild() {
        HierarchicalLDANode child = new HierarchicalLDANode(this, gamma, totalDocuments, nodeMapper);
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
     * The popularity of the node is calculated based on the number of
     * documents that have visited the node (i.e. the total number of people who
     * have visited this restaurant). This is governed by the equation:
     *
     * popularity = (# documents in node) / (total documents - 1 + gamma)
     *
     * The popularity metric is used to decide whether or not to use this
     * node in a path, or whether a new node should be spawned.
     *
     * @return the popularity of this node
     */
    public double getPopularity() {
        return (documentsVisitingNode.size() / (totalDocuments - 1 + gamma));
    }

    /**
     * Returns the popularity of a new and empty node. Given that there are
     * no children yet, the function is governed by the equation:
     *
     * popularity = gamma / (total documents - 1 + gamma)
     *
     * @return the popularity of an empty node
     */
    public double getEmptyPopularity() {
        return (gamma / (totalDocuments - 1 + gamma));
    }

    /**
     * Collects the popularity from all children into a PMF sampler. Adds
     * the empty popularity as a final sample.
     *
     * @return the collection of popularities
     */
    public PMFSampler getChildPopularities() {
        PMFSampler result = new PMFSampler(numChildren + 1);
        for (HierarchicalLDANode child : children) {
            result.add(child.getPopularity());
        }
        result.add(getEmptyPopularity());
        return result;
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
        documentsVisitingNode.add(documentIndex);
    }

    /**
     * Removes the document from having visited the node.
     *
     * @param documentIndex the index of the document to remove
     */
    public void removeVisited(int documentIndex) {
        documentsVisitingNode.remove(documentIndex);
    }

    /**
     * Returns the Id of this node - which is basically the topic number.
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

    public void addWord(int documentIndex, int wordIndexInVocab) {
        if (documentWordCount.containsKey(documentIndex)) {
            Map<Integer, Integer> wordMap = documentWordCount.get(documentIndex);
            if (wordMap.containsKey(wordIndexInVocab)) {
                int wordCount = wordMap.get(wordIndexInVocab);
                wordMap.put(wordIndexInVocab, wordCount + 1);
            } else {
                wordMap.put(wordIndexInVocab, 1);
            }
        } else {
            Map<Integer, Integer> wordMap = new HashMap<>();
            wordMap.put(wordIndexInVocab, 1);
            documentWordCount.put(documentIndex, wordMap);
        }
        totalWords++;
    }

    public void removeWord(int documentIndex, int wordIndexInVocab) {
        if (documentWordCount.containsKey(documentIndex)) {
            Map<Integer, Integer> wordMap = documentWordCount.get(documentIndex);
            if (wordMap.containsKey(wordIndexInVocab)) {
                int wordCount = wordMap.get(wordIndexInVocab);
                wordCount--;
                if (wordCount > 0) {
                    wordMap.put(wordIndexInVocab, wordCount);
                } else {
                    wordMap.remove(wordIndexInVocab);
                }
                totalWords--;
            }
            if (wordMap.isEmpty()) {
                documentWordCount.remove(documentIndex);
            }
        }
    }

    public int getWordCountForDocument(int documentIndex, int wordIndexInVocab) {
        if (documentWordCount.containsKey(documentIndex)) {
            Map<Integer, Integer> wordMap = documentWordCount.get(documentIndex);
            if (wordMap.containsKey(wordIndexInVocab)) {
                return wordMap.get(wordIndexInVocab);
            }
        }
        return 0;
    }

    public int getWordCountAllDocuments(int wordIndexInVocab) {
        int sum = 0;
        for (int documentIndex : documentWordCount.keySet()) {
            Map<Integer, Integer> wordMap = documentWordCount.get(documentIndex);
            if (wordMap.containsKey(wordIndexInVocab)) {
                sum += wordMap.get(wordIndexInVocab);
            }
        }
        return sum;
    }

    public Set<Integer> getVocabularyPresent() {
        Set<Integer> result = new HashSet<>();
        for (int documentIndex : documentWordCount.keySet()) {
            Map<Integer, Integer> wordMap = documentWordCount.get(documentIndex);
            result.addAll(wordMap.keySet());
        }
        return result;
    }

    public int getTotalWordCount() {
        return totalWords;
    }
}
