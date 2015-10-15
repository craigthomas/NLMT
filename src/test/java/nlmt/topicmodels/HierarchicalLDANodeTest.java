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
import nlmt.datatypes.Word;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
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
    public void testNullNodeMapperThrowsException() {
        new HierarchicalLDANode(3, null);
    }

    @Test
    public void testNodeNotEqualToNull() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.equals(null), is(false));
    }

    @Test
    public void testNodeEqualToItself() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.equals(hierarchicalLDANode), is(true));
    }

    @Test
    public void testNodeNotEqualToDifferentNode() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);

        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper1 = new IdentifierObjectMapper<>();
        HierarchicalLDANode hierarchicalLDANode1 = new HierarchicalLDANode(3, nodeMapper1);

        assertThat(hierarchicalLDANode.equals(hierarchicalLDANode1), is(false));
    }

    @Test
    public void testNewNodeAddedToNodeMapper() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(nodeMapper.contains(hierarchicalLDANode), is(true));
        assertThat(nodeMapper.getIndexFromObject(hierarchicalLDANode), is(equalTo(hierarchicalLDANode.getId())));
        assertThat(nodeMapper.getObjectFromIndex(hierarchicalLDANode.getId()), is(equalTo(hierarchicalLDANode)));
    }

    @Test
    public void testAlternateConstructorResultsInRootNode() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.isRoot(), is(true));
    }

    @Test
    public void testCreationWithParentIsNotRootNode() {
        HierarchicalLDANode root = new HierarchicalLDANode(3, nodeMapper);
        hierarchicalLDANode = new HierarchicalLDANode(root, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(root));
        assertThat(hierarchicalLDANode.isRoot(), is(false));
    }

    @Test
    public void testVisitedEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.documentsVisitingNode.isEmpty(), is(true));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(0)));
    }

    @Test
    public void testSetVisitedWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
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
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
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
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
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
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
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
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
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
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        List<HierarchicalLDANode> expected = new ArrayList<>();
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(expected)));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(0)));
    }

    @Test
    public void testNodeIsLevel0OnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.getLevel(), is(equalTo(0)));
    }

    @Test
    public void testNodeSetLevelWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        hierarchicalLDANode.setLevel(1);
        assertThat(hierarchicalLDANode.getLevel(), is(equalTo(1)));
    }

    @Test
    public void testSpawnCorrectlyAddsChildToChildren() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        HierarchicalLDANode child = hierarchicalLDANode.spawnChild(1);
        assertThat(hierarchicalLDANode.getChildren().contains(child), is(true));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getChildren().get(0).getLevel(), is(equalTo(1)));
    }

    @Test
    public void testSpawnMultipleChildrenWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild(1);
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild(1);
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild(1);
        HierarchicalLDANode [] expected = {child1, child2, child3};
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(Arrays.asList(expected))));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(3)));
    }

    @Test
    public void testGetDocumentsVisitingNodeIsEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.getDocumentsVisitingNode().isEmpty(), is(true));
        assertThat(hierarchicalLDANode.getNumDocumentsVisitingNode(), is(equalTo(0)));
    }

    @Test
    public void testGetTotalWordCountIsEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
    }

    @Test
    public void testAddWordWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        word.setTotalCount(3);
        hierarchicalLDANode.addWord(word);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(3)));
        assertThat(hierarchicalLDANode.getWordCount(0), is(equalTo(3)));
        assertThat(hierarchicalLDANode.getWordCount(1), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(2), is(equalTo(0)));
    }

    @Test
    public void testDoubleAddWordToDocumentsWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        word.setTotalCount(3);
        hierarchicalLDANode.addWord(word);
        hierarchicalLDANode.addWord(word);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(6)));
        assertThat(hierarchicalLDANode.getWordCount(0), is(equalTo(6)));
        assertThat(hierarchicalLDANode.getWordCount(1), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(2), is(equalTo(0)));
    }

    @Test
    public void testRemoveWordWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        word.setTotalCount(3);
        hierarchicalLDANode.addWord(word);
        hierarchicalLDANode.removeWord(word);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(0), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(1), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(2), is(equalTo(0)));
    }

    @Test
    public void testDoubleAddWordRemoveOneWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        word.setTotalCount(3);
        hierarchicalLDANode.addWord(word);
        word.setTotalCount(2);
        hierarchicalLDANode.addWord(word);
        hierarchicalLDANode.removeWord(word);
        assertThat(hierarchicalLDANode.getTotalWordCount(), is(equalTo(3)));
        assertThat(hierarchicalLDANode.getWordCount(0), is(equalTo(3)));
        assertThat(hierarchicalLDANode.getWordCount(1), is(equalTo(0)));
        assertThat(hierarchicalLDANode.getWordCount(2), is(equalTo(0)));
    }

    @Test
    public void testRemoveFromParentWorksCorrectlySingleChild() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        HierarchicalLDANode child = hierarchicalLDANode.spawnChild(1);
        assertThat(hierarchicalLDANode.getChildren().contains(child), is(true));
        child.removeFromParent();
        assertThat(child.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testRemoveFromParentWorksCorrectlyMultipleChildren() {
        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild(1);
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild(1);
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild(1);
        assertThat(hierarchicalLDANode.getChildren().contains(child1), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child2), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child3), is(true));
        child2.removeFromParent();
        assertThat(child2.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.getChildren().contains(child1), is(true));
        assertThat(hierarchicalLDANode.getChildren().contains(child2), is(false));
        assertThat(hierarchicalLDANode.getChildren().contains(child3), is(true));
    }

    @Test
    public void testDeleteEmptyNodesWorksCorrectlyNoNodes() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.size(), is(equalTo(0)));
    }

    @Test
    public void testDeleteEmptyNodesSingleNodeNoDocumentsNoWords() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node = new HierarchicalLDANode(1, nodeMapper);
        assertThat(nodeMapper.contains(node), is(true));
        assertThat(nodeMapper.size(), is(equalTo(1)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node), is(false));
        assertThat(nodeMapper.size(), is(equalTo(0)));
    }

    @Test
    public void testDeleteEmptyNodesSingleNodeOneDocumentsNoWords() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node = new HierarchicalLDANode(1, nodeMapper);
        node.setVisited(0);
        assertThat(nodeMapper.contains(node), is(true));
        assertThat(nodeMapper.size(), is(equalTo(1)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node), is(false));
        assertThat(nodeMapper.size(), is(equalTo(0)));
    }

    @Test
    public void testDeleteEmptyNodesSingleNodeWithDocuments() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node = new HierarchicalLDANode(1, nodeMapper);
        Word word = new Word("test", vocabulary.addObject("test"));
        node.setVisited(1);
        node.addWord(word);
        assertThat(nodeMapper.contains(node), is(true));
        assertThat(nodeMapper.size(), is(equalTo(1)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node), is(true));
        assertThat(nodeMapper.size(), is(equalTo(1)));
    }

    @Test
    public void testDeleteEmptyNodesTwoNodesBothEmpty() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(1, nodeMapper);
        HierarchicalLDANode node2 = new HierarchicalLDANode(1, nodeMapper);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(true));
        assertThat(nodeMapper.size(), is(equalTo(2)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node1), is(false));
        assertThat(nodeMapper.contains(node2), is(false));
        assertThat(nodeMapper.size(), is(equalTo(0)));
    }

    @Test
    public void testDeleteEmptyNodesTwoNodesOneEmpty() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(1, nodeMapper);
        node1.setVisited(1);
        node1.addWord(word);
        HierarchicalLDANode node2 = new HierarchicalLDANode(1, nodeMapper);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(true));
        assertThat(nodeMapper.size(), is(equalTo(2)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(false));
        assertThat(nodeMapper.size(), is(equalTo(1)));
    }

    @Test
    public void testDeleteEmptyNodesTwoNodesNeitherEmpty() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(1, nodeMapper);
        node1.setVisited(1);
        node1.addWord(word);
        HierarchicalLDANode node2 = new HierarchicalLDANode(1, nodeMapper);
        node2.setVisited(1);
        node2.addWord(word);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(true));
        assertThat(nodeMapper.size(), is(equalTo(2)));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(true));
        assertThat(nodeMapper.size(), is(equalTo(2)));
    }

    @Test
    public void testDeleteEmptyNodesRemovesEmptyNodeFromParent() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word = new Word("test", vocabulary.addObject("test"));
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(1, nodeMapper);
        node1.setVisited(1);
        node1.addWord(word);
        HierarchicalLDANode node2 = node1.spawnChild(1);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(true));
        assertThat(nodeMapper.size(), is(equalTo(2)));
        assertThat(node1.getChildren().contains(node2), is(true));
        HierarchicalLDANode.deleteEmptyNodes(nodeMapper);
        assertThat(nodeMapper.contains(node1), is(true));
        assertThat(nodeMapper.contains(node2), is(false));
        assertThat(nodeMapper.size(), is(equalTo(1)));
        assertThat(node1.getChildren().contains(node2), is(false));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopWordsThrowsExceptionOnBadNumWords() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(1, nodeMapper);
        node1.getTopWords(0, vocabulary);
    }

    @Test
    public void testGetTopWordsWorksCorrectly() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(10, nodeMapper);
        Word word1 = new Word("word1", vocabulary.addObject("word1"));
        word1.setTotalCount(3);
        Word word2 = new Word("word2", vocabulary.addObject("word2"));
        word2.setTotalCount(1);
        Word word3 = new Word("word3", vocabulary.addObject("word3"));
        word3.setTotalCount(4);
        Word word4 = new Word("word4", vocabulary.addObject("word4"));
        word4.setTotalCount(5);
        Word word5 = new Word("word5", vocabulary.addObject("word5"));
        word5.setTotalCount(2);
        Word word6 = new Word("word6", vocabulary.addObject("word6"));
        word6.setTotalCount(9);
        Word word7 = new Word("word7", vocabulary.addObject("word7"));
        word7.setTotalCount(8);
        Word word8 = new Word("word8", vocabulary.addObject("word8"));
        word8.setTotalCount(7);
        Word word9 = new Word("word9", vocabulary.addObject("word9"));
        word9.setTotalCount(6);
        Word word10 = new Word("word10", vocabulary.addObject("word10"));
        word10.setTotalCount(10);
        node1.addWord(word1);
        node1.addWord(word2);
        node1.addWord(word3);
        node1.addWord(word4);
        node1.addWord(word5);
        node1.addWord(word6);
        node1.addWord(word7);
        node1.addWord(word8);
        node1.addWord(word9);
        node1.addWord(word10);
        List<String> result = node1.getTopWords(5, vocabulary);
        List<String> expected = Arrays.asList("word10", "word6", "word7", "word8", "word9");
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testGetTopWordsReturnsFullSortedListWhenMoreWordsRequestedThanActualWords() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        HierarchicalLDANode node1 = new HierarchicalLDANode(10, nodeMapper);
        Word word1 = new Word("word1", vocabulary.addObject("word1"));
        word1.setTotalCount(3);
        Word word2 = new Word("word2", vocabulary.addObject("word2"));
        word2.setTotalCount(1);
        Word word3 = new Word("word3", vocabulary.addObject("word3"));
        word3.setTotalCount(4);
        Word word4 = new Word("word4", vocabulary.addObject("word4"));
        word4.setTotalCount(5);
        Word word5 = new Word("word5", vocabulary.addObject("word5"));
        word5.setTotalCount(2);
        Word word6 = new Word("word6", vocabulary.addObject("word6"));
        word6.setTotalCount(9);
        Word word7 = new Word("word7", vocabulary.addObject("word7"));
        word7.setTotalCount(8);
        Word word8 = new Word("word8", vocabulary.addObject("word8"));
        word8.setTotalCount(7);
        Word word9 = new Word("word9", vocabulary.addObject("word9"));
        word9.setTotalCount(6);
        Word word10 = new Word("word10", vocabulary.addObject("word10"));
        word10.setTotalCount(10);
        node1.addWord(word1);
        node1.addWord(word2);
        node1.addWord(word3);
        node1.addWord(word4);
        node1.addWord(word5);
        node1.addWord(word6);
        node1.addWord(word7);
        node1.addWord(word8);
        node1.addWord(word9);
        node1.addWord(word10);
        List<String> result = node1.getTopWords(20, vocabulary);
        List<String> expected = Arrays.asList("word10", "word6", "word7", "word8", "word9", "word4", "word3", "word1", "word5", "word2");
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void testWordsInNodeEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(1, nodeMapper);
        assertThat(hierarchicalLDANode.getWordsInNode(), is(equalTo(new HashSet<>())));
    }

    @Test
    public void testWordsInNodeCorrectWhenWordsAdded() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word1 = new Word("wordOne", vocabulary.addObject("wordOne"));
        Word word2 = new Word("wordTwo", vocabulary.addObject("wordTwo"));
        hierarchicalLDANode = new HierarchicalLDANode(2, nodeMapper);
        hierarchicalLDANode.addWord(word1);
        hierarchicalLDANode.addWord(word2);
        assertThat(hierarchicalLDANode.getWordsInNode().size(), is(equalTo(2)));
        assertThat(hierarchicalLDANode.getWordsInNode().contains(word1.getVocabularyId()), is(true));
        assertThat(hierarchicalLDANode.getWordsInNode().contains(word2.getVocabularyId()), is(true));
    }

    @Test
    public void testWordsInNodeCorrectWhenWordsAddedAndRemoved() {
        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
        Word word1 = new Word("wordOne", vocabulary.addObject("wordOne"));
        Word word2 = new Word("wordTwo", vocabulary.addObject("wordTwo"));
        hierarchicalLDANode = new HierarchicalLDANode(2, nodeMapper);
        hierarchicalLDANode.addWord(word1);
        hierarchicalLDANode.addWord(word2);
        hierarchicalLDANode.removeWord(word2);
        assertThat(hierarchicalLDANode.getWordsInNode().size(), is(equalTo(1)));
        assertThat(hierarchicalLDANode.getWordsInNode().contains(word1.getVocabularyId()), is(true));
        assertThat(hierarchicalLDANode.getWordsInNode().contains(word2.getVocabularyId()), is(false));
    }

    @Test
    public void testGenerateMapReturnsEmptyWhenRootNodeIsSingleNode() {
        hierarchicalLDANode = new HierarchicalLDANode(1, nodeMapper);
        Map<Integer, List<Integer>> expected = new HashMap<>();
        expected.put(0, new ArrayList<>());
        assertThat(HierarchicalLDANode.generateMap(nodeMapper), is(equalTo(expected)));
    }

    @Test
    public void testGenerateMapComplexExampleWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1, nodeMapper);  // node 0
        hierarchicalLDANode.spawnChild(1);                             // node 1
        HierarchicalLDANode node2 = hierarchicalLDANode.spawnChild(1); // node 2
        HierarchicalLDANode node3 = node2.spawnChild(2);               // node 3
        node3.spawnChild(3);                                           // node 4
        Map<Integer, List<Integer>> expected = new HashMap<>();
        expected.put(0, Arrays.asList(1, 2));
        expected.put(1, new ArrayList<>());
        expected.put(2, Collections.singletonList(3));
        expected.put(3, Collections.singletonList(4));
        expected.put(4, new ArrayList<>());
        assertThat(HierarchicalLDANode.generateMap(nodeMapper), is(equalTo(expected)));
    }

//    @Test
//    public void testSerializationRoundTrip() {
//        hierarchicalLDANode = new HierarchicalLDANode(3, nodeMapper);
//        hierarchicalLDANode.setVisited(2);
//        IdentifierObjectMapper<String> vocabulary = new IdentifierObjectMapper<>();
//        Word word = new Word("test", vocabulary.addObject("test"));
//        hierarchicalLDANode.addWord(word);
//        try {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//            objectOutputStream.writeObject(hierarchicalLDANode);
//            byte[] serializedObjectArray = byteArrayOutputStream.toByteArray();
//            objectOutputStream.close();
//            byteArrayOutputStream.close();
//
//            assertThat(serializedObjectArray.length, is(not(CoreMatchers.equalTo(0))));
//
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObjectArray);
//            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//            HierarchicalLDANode deserializedNode = (HierarchicalLDANode) objectInputStream.readObject();
//            assertThat(hierarchicalLDANode.equals(deserializedNode), is(true));
//        } catch (IOException e) {
//            assertFalse("IOException occurred: " + e.getMessage(), true);
//        } catch (ClassNotFoundException e) {
//            assertFalse("ClassNotFoundException occurred: " + e.getMessage(), true);
//        }
//    }
}
