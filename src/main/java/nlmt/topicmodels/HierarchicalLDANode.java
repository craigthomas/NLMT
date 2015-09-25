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

    // The set of documents that have visited this node in a path
    protected Set<Integer> documentsVisitingNode;

    // The mapper responsible for mapping nodes to indices
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    // The total number of documents that have visited the node
    private int numDocumentsVisitingNode;

    private int [] wordCounts;

    private int totalWordCount;

    private int level;

    /**
     * Alternate constructor used to create a node with no parent. Nodes
     * without parents are considered to be root nodes.
     *
     * @param nodeMapper the mapping of nodes to IDs
     */
    public HierarchicalLDANode(int vocabularySize, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        this(null, vocabularySize, nodeMapper);
    }

    public HierarchicalLDANode(HierarchicalLDANode parent, int vocabularySize, IdentifierObjectMapper<HierarchicalLDANode> nodeMapper) {
        this.parent = parent;
        this.nodeMapper = nodeMapper;
        children = new ArrayList<>();
        documentsVisitingNode = new HashSet<>();
        numChildren = 0;
        id = nodeMapper.addObject(this);
        numDocumentsVisitingNode = 0;
        level = 0;
        wordCounts = new int[vocabularySize];
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Spawns a new child on for this node, and returns the spawned child.
     *
     * @return the newly spawned node
     */
    public HierarchicalLDANode spawnChild(int level) {
        HierarchicalLDANode child = new HierarchicalLDANode(this, wordCounts.length, nodeMapper);
        child.setLevel(level);
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
     * Removes this node from the parent's list of children.
     */
    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public void addWord(int wordIndexInVocab) {
        wordCounts[wordIndexInVocab]++;
        totalWordCount++;
    }

    public void removeWord(int wordIndexInVocab) {
        wordCounts[wordIndexInVocab]--;
        totalWordCount--;
    }

    public int getWordCount(int wordIndexInVocab) {
        return wordCounts[wordIndexInVocab];
    }

    public int getTotalWordCount() {
        return totalWordCount;
    }
}
