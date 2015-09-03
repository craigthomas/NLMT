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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the LDAModel class.
 */
public class HierarchicalLDAModelTest {

    private String [] document1 = {"this", "is", "a", "test", "document"};
    private String [] document2 = {"the", "cat", "sat", "on", "the", "mat"};
    private String [] document3 = {"the", "dog", "chased", "the", "cat"};

    private HierarchicalLDAModel hierarchicalLDAModel;
    private List<List<String>> documents;
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    @Before
    public void setUp() {
        documents = new ArrayList<>();
        documents.add(Arrays.asList(document1));
        documents.add(Arrays.asList(document2));
        documents.add(Arrays.asList(document3));
        nodeMapper = new IdentifierObjectMapper<>();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMaxDepthThanTwoThrowsException() {
        hierarchicalLDAModel = new HierarchicalLDAModel(1, HierarchicalLDAModel.DEFAULT_ALPHA, HierarchicalLDAModel.DEFAULT_GAMMA);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGammaLessThanZeroThrowsException() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_ALPHA, -1.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAlphaLessThanZeroThrowsException() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, -1.0, 1.0);
    }

    @Test
    public void testReadDocumentsWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        assertThat(hierarchicalLDAModel.documents.length, is(equalTo(3)));
        assertThat(hierarchicalLDAModel.vocabulary.size(), is(equalTo(12)));
        int [] expectedDocument1 = {0, 1, 2, 3, 4};
        int [] expectedDocument2 = {5, 6, 7, 8, 5, 9};
        int [] expectedDocument3 = {5, 10, 11, 5, 6};
        assertThat(hierarchicalLDAModel.documents[0].getWordArray(), is(equalTo(expectedDocument1)));
        assertThat(hierarchicalLDAModel.documents[1].getWordArray(), is(equalTo(expectedDocument2)));
        assertThat(hierarchicalLDAModel.documents[2].getWordArray(), is(equalTo(expectedDocument3)));
        int [] expectedTopics1 = {-1, -1, -1, -1, -1};
        int [] expectedTopics2 = {-1, -1, -1, -1, -1, -1};
        int [] expectedTopics3 = {-1, -1, -1, -1, -1};
        assertThat(hierarchicalLDAModel.documents[0].getTopicArray(), is(equalTo(expectedTopics1)));
        assertThat(hierarchicalLDAModel.documents[1].getTopicArray(), is(equalTo(expectedTopics2)));
        assertThat(hierarchicalLDAModel.documents[2].getTopicArray(), is(equalTo(expectedTopics3)));
    }

    @Test
    public void testInitializeSetsRandomTopics() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();
        int [] expectedTopics1 = {-1, -1, -1, -1, -1};
        int [] expectedTopics2 = {-1, -1, -1, -1, -1, -1};
        int [] expectedTopics3 = {-1, -1, -1, -1, -1};
        assertThat(hierarchicalLDAModel.documents[0].getTopicArray(), is(not(equalTo(expectedTopics1))));
        assertThat(hierarchicalLDAModel.documents[1].getTopicArray(), is(not(equalTo(expectedTopics2))));
        assertThat(hierarchicalLDAModel.documents[2].getTopicArray(), is(not(equalTo(expectedTopics3))));
    }

    @Test
    public void testGetSumLogGammaWordsNotInDocument() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        Set<Integer> vocabInNode = new HashSet<>();
        vocabInNode.add(0);
        assertThat(hierarchicalLDAModel.getSumLogGammaWordsNotInDocument(0, testNode, vocabInNode), is(equalTo(0.04543773854448513)));
    }

    @Test
    public void testGetLogGammaEtaPlusWordsNotInDocument() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        Set<Integer> vocabInNode = new HashSet<>();
        vocabInNode.add(0);
        assertThat(hierarchicalLDAModel.getLogGammaEtaPlusWordsNotInDocument(0, testNode, vocabInNode), is(equalTo(0.8854048271549092)));
    }

    @Test
    public void testGetLogGammaEtaSumAllWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getLogGammaEtaSumAllWords(testNode), is(equalTo(2.04855563696059)));
    }

    @Test
    public void testGetSumLogGammaAllWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        Set<Integer> vocabInNode = new HashSet<>();
        vocabInNode.add(0);
        assertThat(hierarchicalLDAModel.getSumLogGammaAllWords(testNode, vocabInNode), is(equalTo(0.7873750832738625)));
    }

    @Test
    public void getTopicLikelihood() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getTopicLikelihood(0, testNode), is(equalTo(-0.42121346507630353)));
    }

    @Test
    public void getTopicLikelihoodSingleDocumentManyWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode node = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, new IdentifierObjectMapper<>());
        node.addWord(0, 0);
        node.addWord(0, 1);
        node.addWord(0, 2);
        node.addWord(0, 3);
        node.addWord(0, 4);
        assertThat(hierarchicalLDAModel.getTopicLikelihood(0, node), is(equalTo(-16.730598342810836)));
    }

    @Test
    public void testGetTopicWeightEmptyNodeReturnsZero() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.5, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode node = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, new IdentifierObjectMapper<>());
        assertThat(hierarchicalLDAModel.getTopicWeight(0, 0, node), is(equalTo(0.0)));
    }

    @Test
    public void testGetTopicWeightWorksCorrectlySimpleCase() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.5, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode node = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, new IdentifierObjectMapper<>());
        node.addWord(0, 0);
        node.addWord(0, 1);
        node.addWord(0, 2);
        node.addWord(0, 3);
        node.addWord(0, 4);

        assertThat(hierarchicalLDAModel.getTopicWeight(0, 0, node), is(equalTo(0.30000000000000004)));
    }

    @Test
    public void testGetNewTopicAllWordsInDocumentInLevelOneReturnsCorrectChild() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.5, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode rootNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, new IdentifierObjectMapper<>());
        HierarchicalLDAPath path = new HierarchicalLDAPath(rootNode, 3);
        path.addNode(rootNode.spawnChild());
        HierarchicalLDANode child0 = rootNode.getChildren().get(0);
        path.addNode(child0.spawnChild());
        HierarchicalLDANode child1 = child0.getChildren().get(0);

        child1.addWord(0, 0);
        child1.addWord(0, 1);
        child1.addWord(0, 2);
        child1.addWord(0, 3);
        child1.addWord(0, 4);

        child0.addWord(0, 1);
        child0.addWord(0, 2);
        child0.addWord(0, 3);
        child0.addWord(0, 4);

        rootNode.addWord(0, 1);
        rootNode.addWord(0, 2);
        rootNode.addWord(0, 3);
        rootNode.addWord(0, 4);

        int newTopic = hierarchicalLDAModel.getNewTopic(0, 0, path);
        assertThat(newTopic, is(equalTo(2)));
    }

    @Test
    public void testGetNewPathNodeReturnsNewChildWhenNoChildren() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.5, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode rootNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, new IdentifierObjectMapper<>());
        int selectedChild = hierarchicalLDAModel.getNewPathNode(0, rootNode);
        assertThat(selectedChild, is(equalTo(0)));
    }

    @Test
    public void testGetNewPathNodeReturnsExistingChildThatIsMorePopular() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, HierarchicalLDAModel.DEFAULT_ALPHA, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode rootNode = new HierarchicalLDANode(0.0000000000000001, 3, new IdentifierObjectMapper<>());
        HierarchicalLDAPath path = new HierarchicalLDAPath(rootNode, 3);
        path.addNode(rootNode.spawnChild());
        HierarchicalLDANode child0 = rootNode.getChildren().get(0);

        child0.setVisited(0);
        child0.addWord(0, 0);
        child0.addWord(0, 1);
        child0.addWord(0, 2);
        child0.addWord(0, 3);
        child0.addWord(0, 4);

        int selectedChild = hierarchicalLDAModel.getNewPathNode(0, rootNode);
        assertThat(selectedChild, is(equalTo(0)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopWordsForTopicThrowsExceptionOnInvalidTopic() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();
        hierarchicalLDAModel.getTopWordsForTopic(200000, 5);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopWordsForTopicThrowsExceptionOnBadNumWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();
        hierarchicalLDAModel.getTopWordsForTopic(0, 0);
    }

    @Test
    public void testGetTopicsWorksCorrectly() {
        IdentifierObjectMapper<HierarchicalLDANode> nodeMapper = new IdentifierObjectMapper<>();
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode testNode = new HierarchicalLDANode(HierarchicalLDAModel.DEFAULT_GAMMA, 3, nodeMapper);
        hierarchicalLDAModel.nodeMapper = nodeMapper;

        testNode.setVisited(0);
        testNode.addWord(0, 0);
        testNode.addWord(0, 0);
        testNode.addWord(0, 0);
        testNode.addWord(0, 0);
        testNode.addWord(0, 0);
        testNode.addWord(0, 1);
        testNode.addWord(0, 1);
        testNode.addWord(0, 1);
        testNode.addWord(0, 1);
        testNode.addWord(0, 2);
        testNode.addWord(0, 2);
        testNode.addWord(0, 2);
        testNode.addWord(0, 3);
        testNode.addWord(0, 3);

        Map<Integer, List<String>> expected = new HashMap<>();
        List<String> expectedWords = new ArrayList<>();
        expectedWords.add("this");
        expectedWords.add("is");
        expectedWords.add("a");
        expectedWords.add("test");
        expected.put(0, expectedWords);

        assertThat(hierarchicalLDAModel.getTopics(4, 1), is(equalTo(expected)));
    }
}
