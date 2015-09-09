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
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the HierarchicalLDANode class.
 */
public class HierarchicalLDANodeTest
{
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;
    private HierarchicalLDANode hierarchicalLDANode;

    @Before
    public void setUp() {
        nodeMapper = new IdentifierObjectMapper<>();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidNumberOfDocumentsThrowsException() {
        new HierarchicalLDANode(-2, 3, nodeMapper);
    }

    @Test
    public void testNewNodeAddedToNodeMapper() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        assertThat(nodeMapper.contains(hierarchicalLDANode), is(true));
        assertThat(nodeMapper.getIndexFromObject(hierarchicalLDANode), is(equalTo(hierarchicalLDANode.getId())));
        assertThat(nodeMapper.getObjectFromIndex(hierarchicalLDANode.getId()), is(equalTo(hierarchicalLDANode)));
    }

    @Test
    public void testAlternateConstructorResultsInRootNode() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.isRoot(), is(true));
    }

    @Test
    public void testCreationWithParentIsNotRootNode() {
        HierarchicalLDANode root = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode = new HierarchicalLDANode(root, 3, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(root));
        assertThat(hierarchicalLDANode.isRoot(), is(false));
    }

    @Test
    public void testVisitedEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        assertThat(hierarchicalLDANode.documentsVisitingNode.isEmpty(), is(true));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(0)));
    }

    @Test
    public void testSetVisitedWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(4);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.getDocumentsVisitingNode(), is(equalTo(expectedSet)));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(3)));
    }

    @Test
    public void testSetVisitedDoubleSetSameDocumentWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(0);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(1)));
    }

    @Test
    public void testRemoveVisitedRemoveSingleWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(4);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(2)));
    }

    @Test
    public void testRemoveVisitedRemoveItemNotInSetWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(9);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(4);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(3)));
    }

    @Test
    public void testRemoveVisitedRemoveAllWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(2);
        hierarchicalLDANode.removeVisited(4);
        hierarchicalLDANode.removeVisited(0);
        Set<Integer> expectedSet = new HashSet<>();
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(0)));
    }

    @Test
    public void testNodeHasNoChildrenOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        List<HierarchicalLDANode> expected = new ArrayList<>();
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(expected)));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(0)));
    }

    @Test
    public void testSpawnCorrectlyAddsChildToChildren() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        HierarchicalLDANode child = hierarchicalLDANode.spawnChild();
        assertThat(hierarchicalLDANode.getChildren().contains(child), is(true));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(1)));
    }

    @Test
    public void testSpawnMultipleChildrenWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode [] expected = {child1, child2, child3};
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(Arrays.asList(expected))));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(3)));
    }

    @Test
    public void testGetDocumentsVisitingNodeIsEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getDocumentsVisitingNode().isEmpty(), is(true));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(0)));
    }

    @Test
    public void testGetTotalWordCountIsEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
    }

    @Test
    public void testAddWordNoDocumentsWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(1)));
    }

    @Test
    public void testDoubleAddWordToDocumentsWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.addWord(0, 0);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(2)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(2)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(2)));
    }

    @Test
    public void testAddWordsMultipleDocumentsWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(2, 4, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.addWord(0, 1);
        hierarchicalLDANode.addWord(1, 2);
        hierarchicalLDANode.addWord(1, 3);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(4)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(1), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(2), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(3), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 1), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 2), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 3), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(1, 0), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(1, 1), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(1, 2), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(1, 3), is(equalTo(1)));
    }

    @Test
    public void testRemoveWordNoDocumentsWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.removeWord(0, 0);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(0)));
    }

    @Test
    public void testDoubleAddWordRemoveOneWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.removeWord(0, 0);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(1)));
    }

    @Test
    public void testDoubleAddWordRemoveBothWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.removeWord(0, 0);
        hierarchicalLDANode.removeWord(0, 0);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(0), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 0), is(equalTo(0)));
    }

    @Test
    public void testGetWordCountForDocumentIsZeroOnInvalidDocument() {
        hierarchicalLDANode = new HierarchicalLDANode(1, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        assertThat(hierarchicalLDANode.getWordCountForDocument(1, 0), is(equalTo(0)));
    }

    @Test
    public void testGetWordCountForDocumentIsZeroOnInvalidWord() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 1, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        assertThat(hierarchicalLDANode.getWordCountForDocument(0, 1), is(equalTo(0)));
    }

    @Test
    public void testGetWordCountForAllDocumentsIsZeroOnInvalidWord() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 1, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        assertThat(hierarchicalLDANode.getWordCountAllDocuments(1), is(equalTo(0)));
    }

    @Test
    public void testGetTopicWeightEmptyNodeReturnsZero() {
        HierarchicalLDANode node = new HierarchicalLDANode(3, 3, new IdentifierObjectMapper<>());
        assertThat(node.getWeight(0, 0, 1.0, 1.5), is(equalTo(0.0)));
    }

    @Test
    public void testGetTopicWeightWorksCorrectlySimpleCase() {
        HierarchicalLDANode node = new HierarchicalLDANode(3, 5, new IdentifierObjectMapper<>());
        node.addWord(0, 0);
        node.addWord(0, 1);
        node.addWord(0, 2);
        node.addWord(0, 3);
        node.addWord(0, 4);

        assertThat(node.getWeight(0, 0, 1.0, 1.0), is(equalTo(0.4)));
    }

    @Test
    public void testAddWordOnBadDocumentIndexBadVocabIndexDoesNothing() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(-1, 0);
        hierarchicalLDANode.addWord(4, 0);
        hierarchicalLDANode.addWord(0, -1);
        hierarchicalLDANode.addWord(0, 4);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
    }

    @Test
    public void testRemoveWordOnBadDocumentIndexBadVocabIndexDoesNothing() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDANode.addWord(0, 0);
        hierarchicalLDANode.removeWord(-1, 0);
        hierarchicalLDANode.removeWord(4, 0);
        hierarchicalLDANode.removeWord(0, -1);
        hierarchicalLDANode.removeWord(0, 4);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(1)));
    }

    @Test
    public void testRemoveFromParentWorksCorrectlySingleChild() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        HierarchicalLDANode child = hierarchicalLDANode.spawnChild();
        assertThat(hierarchicalLDANode.getChildren().contains(child), is(true));
        child.removeFromParent();
        assertThat(child.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testRemoveFromParentWorksCorrectlyMultipleChildren() {
        hierarchicalLDANode = new HierarchicalLDANode(3, 3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild();
        assertThat(hierarchicalLDANode.getChildren().contains(child1), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child2), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child3), is(true));
        child2.removeFromParent();
        assertThat(child2.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.getChildren().contains(child1), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child2), is(false));
        assertThat(hierarchicalLDANode.getChildren().contains(child3), is(true));
    }
}
