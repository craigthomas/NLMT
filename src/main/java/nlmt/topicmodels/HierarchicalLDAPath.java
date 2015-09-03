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

import java.util.Set;

/**
 * Represents a path through a Hierarchical LDA Tree.
 */
public class HierarchicalLDAPath
{
    // The nodes in the path
    private HierarchicalLDANode [] nodes;

    // The maximum depth of the path to be generated
    private int maxDepth;

    // The current depth of the path
    private int currentDepth;

    public HierarchicalLDAPath(HierarchicalLDANode rootNode, int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be > 0");
        }
        if (rootNode == null) {
            throw new IllegalArgumentException("rootNode cannot be null");
        }
        nodes = new HierarchicalLDANode[maxDepth];
        nodes[0] = rootNode;
        this.maxDepth = maxDepth;
        currentDepth = 1;
    }

    /**
     * Adds a node to the path.
     *
     * @param node the node to add to the path
     */
    public void addNode(HierarchicalLDANode node) {
        if (currentDepth < maxDepth) {
            nodes[currentDepth] = node;
            currentDepth++;
        }
    }

    /**
     * Returns the last node added (the highest level node in the path).
     *
     * @return the last node added
     */
    public HierarchicalLDANode getCurrentNode() {
        return nodes[currentDepth - 1];
    }

    /**
     * Gets the node in the path on the specified level.
     *
     * @param level the level of the node to fetch
     * @return the node at the specified level
     */
    public HierarchicalLDANode getNode(int level) {
        if (level < 0 || level >= maxDepth) {
            throw new IllegalArgumentException("level must be >= 0 and < maxDepth");
        }
        return nodes[level];
    }

    /**
     * Returns an array containing all the nodes in the path.
     *
     * @return the array of nodes in the path
     */
    public HierarchicalLDANode [] getNodes() {
        return nodes;
    }

    /**
     * Clears out everything except the root node from the array of nodes.
     */
    public void clear() {
        for (int i = 1; i < maxDepth; i++) {
            nodes[i] = null;
        }
        currentDepth = 1;
    }

    /**
     * Returns <code>true</code> if the maximum depth of the path has been
     * reached.
     *
     * @return <code>true</code> if the path has reached its maximum depth
     */
    public boolean atMaxDepth() {
        return currentDepth == maxDepth;
    }

    /**
     * Removes both the document and the words in the document from the path,
     * excluding the root.
     *
     * @param documentIndex the index of the document to remove
     * @param wordSet the set of words appearing in the document
     */
    public void removeDocumentWordsAndClear(int documentIndex, Set<Integer> wordSet) {
        for (int pathIndex = 1; pathIndex < currentDepth; pathIndex++) {
            HierarchicalLDANode currentNode = getNode(pathIndex);
            currentNode.removeVisited(documentIndex);
            for (Integer word : wordSet) {
                currentNode.removeWord(documentIndex, word);
            }
        }
        clear();
    }

    /**
     * Adds a word to the node at the specified level in the path.
     *
     * @param documentIndex the index of the document to add
     * @param vocabularyIndex the vocabulary index of the word to add
     * @param level the level of the node in the path
     */
    public void addWord(int documentIndex, int vocabularyIndex, int level) {
        if (level < 0 || level > currentDepth - 1) {
            throw new IllegalArgumentException("level must be >= 0 and <= current path length");
        }
        nodes[level].setVisited(documentIndex);
        nodes[level].addWord(documentIndex, vocabularyIndex);
    }
}
