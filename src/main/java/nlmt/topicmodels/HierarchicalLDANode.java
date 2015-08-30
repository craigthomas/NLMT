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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements a node on the tree formed by the Nested Chinese Restaurant Problem.
 */
public class HierarchicalLDANode {

    // Stores the parent to this node
    private HierarchicalLDANode parent;

    // Stores the list of children spawned from this node
    private List<HierarchicalLDANode> children;

    // The total number of documents in the collection used to build the HLDA model
    private int totalDocuments;

    // The gamma tuning parameter
    private double gamma;

    // The set of documents that have visited this node in a path
    protected Set<Integer> documentsVisitingNode;

    /**
     * Alternate constructor used to create a node with no parent. Nodes
     * without parents are considered to be root nodes.
     *
     * @param gamma the gamma parameter
     * @param totalDocuments the total number of documents that will be processed
     */
    public HierarchicalLDANode(double gamma, int totalDocuments) {
        this(null, gamma, totalDocuments);
    }

    public HierarchicalLDANode(HierarchicalLDANode parent, double gamma, int totalDocuments) {
        if (totalDocuments < 0) {
            throw new IllegalArgumentException("totalDocuments must be > 0");
        }
        if (gamma <= 0.0) {
            throw new IllegalArgumentException("gamma must be > 0");
        }
        this.gamma = gamma;
        this.totalDocuments = totalDocuments;
        this.parent = parent;
        children = new ArrayList<>();
        documentsVisitingNode = new HashSet<>();
    }

    /**
     * Spawns a new child on for this node, and returns the spawned child.
     *
     * @return the newly spawned node
     */
    public HierarchicalLDANode spawnChild() {
        HierarchicalLDANode child = new HierarchicalLDANode(this, gamma, totalDocuments);
        children.add(child);
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
}
