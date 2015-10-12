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
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for the LDAModel class.
 */
public class HierarchicalLDAModelTest {

    private String [] document1 = {"this", "is", "a", "test", "document"};
    private String [] document2 = {"the", "cat", "sat", "on", "the", "mat"};
    private String [] document3 = {"the", "dog", "chased", "the", "cat"};
    private String [] oneWord = {"test"};
    private String [] threeWordDoc = {"the", "the", "dog", "dog", "dog", "chased", "chased"};

    private HierarchicalLDAModel hierarchicalLDAModel;
    private List<List<String>> documents;
    private List<List<String>> multipleDocuments;
    private List<List<String>> fiveWordDocument;
    private List<List<String>> fiveWordDocumentTwice;
    private List<List<String>> threeWordDocument;
    private List<List<String>> oneWordDocument;
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;

    @Before
    public void setUp() {
        documents = new ArrayList<>();
        documents.add(Arrays.asList(document1));
        documents.add(Arrays.asList(document2));
        documents.add(Arrays.asList(document3));
        multipleDocuments = new ArrayList<>();
        multipleDocuments.add(Arrays.asList(document1));
        multipleDocuments.add(Arrays.asList(document2));
        multipleDocuments.add(Arrays.asList(document3));
        multipleDocuments.add(Arrays.asList(threeWordDoc));
        fiveWordDocument = new ArrayList<>();
        fiveWordDocument.add(Arrays.asList(document1));
        fiveWordDocumentTwice = new ArrayList<>();
        fiveWordDocumentTwice.add(Arrays.asList(document1));
        fiveWordDocumentTwice.add(Arrays.asList(document1));
        threeWordDocument = new ArrayList<>();
        threeWordDocument.add(Arrays.asList(threeWordDoc));
        oneWordDocument = new ArrayList<>();
        oneWordDocument.add(Arrays.asList(oneWord));
        nodeMapper = new IdentifierObjectMapper<>();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEtaSizeLessThanMaxDepthValuesThrowsException() {
        new HierarchicalLDAModel(3, HierarchicalLDAModel.DEFAULT_GAMMA, new double [] {0.1, 0.2},
                HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMLessThanZeroThrowsException() {
        new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, -1.0, HierarchicalLDAModel.DEFAULT_PI);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMZeroThrowsException() {
        new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, 0.0, HierarchicalLDAModel.DEFAULT_PI);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPiLessThanZeroThrowsException() {
        new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, -1.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPiZeroThrowsException() {
        new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, 0.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMaxDepthThanTwoThrowsException() {
        new HierarchicalLDAModel(1, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGammaLessThanZeroThrowsException() {
        new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH,
                -1.0, HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
    }

//    @Test
//    public void testReadDocumentsWorksCorrectly() {
//        hierarchicalLDAModel = new HierarchicalLDAModel();
//        hierarchicalLDAModel.readDocuments(documents);
//        assertThat(hierarchicalLDAModel.documents.length, is(equalTo(3)));
//        assertThat(hierarchicalLDAModel.vocabulary.size(), is(equalTo(12)));
//        int [] expectedDocument1 = {0, 1, 2, 3, 4};
//        int [] expectedDocument2 = {5, 6, 7, 8, 5, 9};
//        int [] expectedDocument3 = {5, 10, 11, 5, 6};
//        assertThat(hierarchicalLDAModel.documents[0].getWordArray(), is(equalTo(expectedDocument1)));
//        assertThat(hierarchicalLDAModel.documents[1].getWordArray(), is(equalTo(expectedDocument2)));
//        assertThat(hierarchicalLDAModel.documents[2].getWordArray(), is(equalTo(expectedDocument3)));
//        int [] expectedTopics1 = {-1, -1, -1, -1, -1};
//        int [] expectedTopics2 = {-1, -1, -1, -1, -1, -1};
//        int [] expectedTopics3 = {-1, -1, -1, -1, -1};
//        assertThat(hierarchicalLDAModel.documents[0].getTopicArray(), is(equalTo(expectedTopics1)));
//        assertThat(hierarchicalLDAModel.documents[1].getTopicArray(), is(equalTo(expectedTopics2)));
//        assertThat(hierarchicalLDAModel.documents[2].getTopicArray(), is(equalTo(expectedTopics3)));
//    }

    @Test
    public void testInitializeSetsRandomTopics() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();
        for (Word word : hierarchicalLDAModel.documents[0].getWordSet()) {
            assertThat(word.getTopic(), is(not(equalTo(-1))));
        }
        assertThat(hierarchicalLDAModel.documents[0].getTopics().contains(-1), is(false));
        for (Word word : hierarchicalLDAModel.documents[1].getWordSet()) {
            assertThat(word.getTopic(), is(not(equalTo(-1))));
        }
        assertThat(hierarchicalLDAModel.documents[1].getTopics().contains(-1), is(false));
        for (Word word : hierarchicalLDAModel.documents[2].getWordSet()) {
            assertThat(word.getTopic(), is(not(equalTo(-1))));
        }
        assertThat(hierarchicalLDAModel.documents[2].getTopics().contains(-1), is(false));
    }

    @Test
    public void testPathWordsLikelihood() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        HierarchicalLDANode testNode = new HierarchicalLDANode(5, nodeMapper);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 1);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        testNode.setVisited(0);
        testNode.setVisited(1);
        Map<Integer, Integer> wordCountsByLevel = hierarchicalLDAModel.documents[0].getWordCountsByTopic(1);
        assertThat(hierarchicalLDAModel.getPathWordsLikelihood(wordCountsByLevel, hierarchicalLDAModel.documents[0].getWordSet(), 0.1, testNode), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testPathWordsLikelihoodDocumentNotInNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, 0.1,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        HierarchicalLDANode testNode = new HierarchicalLDANode(5, nodeMapper);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 1);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        testNode.setVisited(0);
        Map<Integer, Integer> wordCountsByLevel = new HashMap<>();
        assertThat(hierarchicalLDAModel.getPathWordsLikelihood(wordCountsByLevel, hierarchicalLDAModel.documents[0].getWordSet(), 0.1, testNode), is(equalTo(0.0)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathSingleNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.rootNode.setVisited(0);
        testNode.setVisited(0);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, 2);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, 1)), is(equalTo(0.0)));
    }

    @Test
    public void testCalculateWordLikelihoodSinglePathSingleNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        testNode.setVisited(0);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, 1)), is(equalTo(-14.898374489664242)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathSingleNodeTwoDocumentsVisitingNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        testNode.setVisited(0);
        testNode.setVisited(1);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, 2);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, 1)), is(equalTo(0.0)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathSingleNodeTwoDocumentsButOnlyOneVisitingNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        testNode.setVisited(0);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, 2);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, 1)), is(equalTo(-0.6931471805599453)));
    }

    @Test
    public void testCalculateWordLikelihoodSinglePathSingleNodeTwoDocumentsVisitingNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 1);
        for (SparseDocument document : hierarchicalLDAModel.documents) {
            document.getWordSet().forEach(testNode::addWord);
        }
        testNode.setVisited(0);
        testNode.setVisited(1);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, 1)), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testCalculatePathProbabilitySinglePathSingleEmptyNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, 1.0,
                new double [] {1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, HierarchicalLDAModel.DEFAULT_MAX_DEPTH);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, -1)), is(equalTo(-0.6931471805599453)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathMultipleEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 1.0,
                new double [] {1.0, 1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, HierarchicalLDAModel.DEFAULT_MAX_DEPTH);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, -1, -1)), is(equalTo(-0.6931471805599453)));
    }

    @Test
    public void testCalculateWordLikelihoodSinglePathMultipleEmptyNodesBothDocumentsAssignedToRoot() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.1,
                new double [] {1.0, 1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths = new HierarchicalLDAPath[2];
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 0);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 0);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 0);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 0);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 0);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 0);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, -1, -1)), is(equalTo(-8.92365779985749)));
    }

    @Test
    public void testCalculateWordLikelihoodSinglePathMultipleEmptyNodesOtherDocumentAssignedToRoot() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.1,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths = new HierarchicalLDAPath[2];
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 0);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 0);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, -1, -1)), is(equalTo(0.0)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathWithRealAndEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.rootNode.setVisited(2);
        testNode.setVisited(0);
        testNode.setVisited(2);
        hierarchicalLDAModel.rootNode.propagatePathWeight(0.0, HierarchicalLDAModel.DEFAULT_GAMMA, HierarchicalLDAModel.DEFAULT_MAX_DEPTH);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(Arrays.asList(0, 1, -1)), is(equalTo(-1.0986122886681098)));
    }

    @Test
    public void testCalculateWordLikelihoodSinglePathWithRealAndEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 1.0,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(testNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(testNode::addWord);
        testNode.setVisited(0);
        testNode.setVisited(1);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, 1, -1)), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testCalculateWordLikelihoodReturnsBiggerLikelihoodForPathWithMoreSimilarWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 1.0,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);
        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(0, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(1, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(2, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(3, 1);
        hierarchicalLDAModel.documents[1].setTopicForWord(4, 1);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(testNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(testNode::addWord);
        testNode.setVisited(0);
        testNode.setVisited(1);
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(0, Arrays.asList(0, 1, -1)), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testGetTopicsReturnsEmptyMapWhenNoDocuments() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.rootNode = new HierarchicalLDANode(3, nodeMapper);   // node 0
        HierarchicalLDANode node1 = hierarchicalLDAModel.rootNode.spawnChild(1);  // node 1
        hierarchicalLDAModel.rootNode.spawnChild(1);                              // node 2
        node1.spawnChild(2);                                                      // node 3
        Map<Integer, List<String>> expected = new HashMap<>();
        assertThat(hierarchicalLDAModel.getTopics(5, 1), is(equalTo(expected)));
    }

    @Test
    public void testGetTopicsWorksCorrectlySingleNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(threeWordDocument);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        Map<Integer, List<String>> expected = new HashMap<>();
        expected.put(0, Arrays.asList("dog", "chased", "the"));
        assertThat(hierarchicalLDAModel.getTopics(3, 1), is(equalTo(expected)));
    }

    @Test
    public void testGetTopicsMoreWordsRequestedThanWordsExistingWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(threeWordDocument);
        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        Map<Integer, List<String>> expected = new HashMap<>();
        expected.put(0, Arrays.asList("dog", "chased", "the"));
        assertThat(hierarchicalLDAModel.getTopics(6, 1), is(equalTo(expected)));
    }

    @Test
    public void testGetTopicsMoreDocumentsThanVisitedWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(threeWordDocument);
        hierarchicalLDAModel.rootNode.setVisited(0);
        Map<Integer, List<String>> expected = new HashMap<>();
        assertThat(hierarchicalLDAModel.getTopics(3, 2), is(equalTo(expected)));
    }

    @Test
    public void testGetTopicsMultipleNodesMultipleTopicsWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(multipleDocuments);
        HierarchicalLDANode node1 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode node2 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode node3 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode node4 = hierarchicalLDAModel.rootNode.spawnChild(1);

        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        hierarchicalLDAModel.rootNode.setVisited(2);
        hierarchicalLDAModel.rootNode.setVisited(3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);

        node1.setVisited(0);
        node1.setVisited(1);
        node1.setVisited(3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(node1::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(node1::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(node1::addWord);

        node2.setVisited(1);
        node2.setVisited(2);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(node2::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(node2::addWord);

        node3.setVisited(0);
        node3.setVisited(2);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(node3::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(node3::addWord);

        node4.setVisited(0);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(node4::addWord);

        Map<Integer, List<String>> expected = new HashMap<>();
        expected.put(0, Arrays.asList("the", "dog", "chased", "cat"));
        expected.put(1, Arrays.asList("the", "dog", "chased", "on"));
        assertThat(hierarchicalLDAModel.getTopics(4, 3), is(equalTo(expected)));
    }

    @Test
    public void testGetLevelProbabilitiesSimpleCaseLevel0() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> testDocuments = new ArrayList<>();
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(testDocuments);

        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        path.addNode(hierarchicalLDAModel.rootNode.spawnChild(1));
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.getChildren().get(0);
        path.addNode(child0.spawnChild(2));
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(w -> w.setTopic(0));
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(w -> w.setTopic(0));
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(w -> w.setTopic(0));

        Map<Integer, Integer> documentTopicCounts = hierarchicalLDAModel.documents[2].getTopicCounts();
        double [] levelProbabilities = hierarchicalLDAModel.getLevelProbabilities(documentTopicCounts);
        double [] expected = {0.8571428571428571, 0.07142857142857145, 0.0714285714285714};
        assertArrayEquals(expected, levelProbabilities, 0.0000001);
    }

    @Test
    public void testGetLevelProbabilitiesSimpleCaseLevel1() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> testDocuments = new ArrayList<>();
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(testDocuments);

        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        path.addNode(hierarchicalLDAModel.rootNode.spawnChild(1));
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.getChildren().get(0);
        path.addNode(child0.spawnChild(2));
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(w -> w.setTopic(1));
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(w -> w.setTopic(1));
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(w -> w.setTopic(1));

        Map<Integer, Integer> documentTopicCounts = hierarchicalLDAModel.documents[2].getTopicCounts();
        double [] levelProbabilities = hierarchicalLDAModel.getLevelProbabilities(documentTopicCounts);
        double [] expected = {0.14285714285714285, 0.7346938775510204, 0.12244897959183676};
        assertArrayEquals(expected, levelProbabilities, 0.0000001);
    }

    @Test
    public void testGetLevelProbabilitiesSimpleCaseLevel2() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> testDocuments = new ArrayList<>();
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        testDocuments.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(testDocuments);

        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        path.addNode(hierarchicalLDAModel.rootNode.spawnChild(1));
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.getChildren().get(0);
        path.addNode(child0.spawnChild(2));
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(w -> w.setTopic(2));
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(w -> w.setTopic(2));
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(w -> w.setTopic(2));

        Map<Integer, Integer> documentTopicCounts = hierarchicalLDAModel.documents[2].getTopicCounts();
        double [] levelProbabilities = hierarchicalLDAModel.getLevelProbabilities(documentTopicCounts);
        double [] expected = {0.14285714285714285, 0.12244897959183673, 0.7346938775510204};
        assertArrayEquals(expected, levelProbabilities, 0.0000001);
    }

//    @Test
//    public void testGetNewTopicAllWordsInDocumentInLevelOneReturnsCorrectChild() {
//        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
//                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
//        documents.clear();
//        documents.add(Arrays.asList(document1));
//        documents.add(Arrays.asList(oneWord));
//        hierarchicalLDAModel.readDocuments(documents);
//        hierarchicalLDAModel.initialize();
//
//        HierarchicalLDANode rootNode = new HierarchicalLDANode(hierarchicalLDAModel.vocabulary.size(), new IdentifierObjectMapper<>());
//        HierarchicalLDAPath path = new HierarchicalLDAPath(rootNode, 3);
//        path.addNode(rootNode.spawnChild(1));
//        HierarchicalLDANode child0 = rootNode.getChildren().get(0);
//        path.addNode(child0.spawnChild(2));
//        HierarchicalLDANode child1 = child0.getChildren().get(0);
//
//        hierarchicalLDAModel.documents[0].setTopicForWord(0, 1);
//        hierarchicalLDAModel.documents[0].setTopicForWord(1, 1);
//        hierarchicalLDAModel.documents[0].setTopicForWord(2, 1);
//        hierarchicalLDAModel.documents[0].setTopicForWord(3, 1);
//        hierarchicalLDAModel.documents[0].setTopicForWord(4, 1);
//        hierarchicalLDAModel.documents[0].getWordSet().forEach(child1::addWord);
//
//        Word word = new Word("test", hierarchicalLDAModel.vocabulary.getIndexFromObject("test"));
//
//        int newTopic = hierarchicalLDAModel.chooseNewLevel(hierarchicalLDAModel.documents[1], word, path);
//        assertThat(newTopic, is(equalTo(1)));
//    }

//    @Test
//    public void testGetTopicsWorksCorrectly() {
//        documents = new ArrayList<>();
//        documents.add(Arrays.asList(document1));
//
//        hierarchicalLDAModel = new HierarchicalLDAModel();
//        hierarchicalLDAModel.readDocuments(documents);
//
//        HierarchicalLDANode testNode = new HierarchicalLDANode(5, hierarchicalLDAModel.nodeMapper);
//        testNode.setVisited(0);
//        testNode.addWord(0);
//        testNode.addWord(0);
//        testNode.addWord(0);
//        testNode.addWord(0);
//        testNode.addWord(0);
//        testNode.addWord(1);
//        testNode.addWord(1);
//        testNode.addWord(1);
//        testNode.addWord(1);
//        testNode.addWord(2);
//        testNode.addWord(2);
//        testNode.addWord(3);
//        testNode.addWord(3);
//        testNode.addWord(3);
//
//        Map<Integer, List<String>> expected = new HashMap<>();
//        List<String> expectedWords = new ArrayList<>();
//        expectedWords.add("this");
//        expectedWords.add("is");
//        expectedWords.add("test");
//        expectedWords.add("a");
//        expected.put(1, expectedWords);
//
//        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(0), is(equalTo("this")));
//        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(1), is(equalTo("is")));
//        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(2), is(equalTo("a")));
//        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(3), is(equalTo("test")));
//        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(4), is(equalTo("document")));
//
//        assertThat(hierarchicalLDAModel.getTopics(4, 1), is(equalTo(expected)));
//    }

    /**
     * Constructs a test document as described in:
     *
     * Griffiths, T. L. & Steyvers, M. (2004). Finding scientific topics.
     * Proceedings of the National Academy of Sciences, 101, 5228-5235.
     * http://psiexp.ss.uci.edu/research/papers/sciencetopics.pdf
     *
     * Sample randomly 20 times from among the 10 different topics. Each
     * topic has 5 words in them. The total vocabulary size is 25 words.
     * If you imagine each word records the column and row
     * (e.g. "ca" means column "c", row "a"), then you can see the topics
     * form horizontal and vertical stripes, as described in the scientific
     * paper. Any LDA model should be able to recover the topics in roughly
     * 500 iterations.
     *
     * @return a list of documents for testing
     */
    public List<List<String>> generateTestDocuments() {
        Random random = new Random();
        String [][] topics = {
                {"aa", "ab", "ac", "ad", "ae"},
                {"ba", "bb", "bc", "bd", "be"},
                {"ca", "cb", "cc", "cd", "ce"},
                {"da", "db", "dc", "dd", "de"},
                {"ea", "eb", "ec", "ed", "ee"},

                {"aa", "ba", "ca", "da", "ea"},
                {"ab", "bb", "cb", "db", "eb"},
                {"ac", "bc", "cc", "dc", "ec"},
                {"ad", "bd", "cd", "dd", "ed"},
                {"ae", "be", "ce", "de", "ee"}
        };

        List<List<String>> documents = new ArrayList<>();
        for (int document_num = 0; document_num < 100; document_num++) {
            List<String> document = new ArrayList<>();
            for (int counter = 0; counter < 20; counter++) {
                Collections.addAll(document, topics[random.nextInt(10)]);
            }
            documents.add(document);
        }
        return documents;
    }

//    @Test
//    public void testDoGibbsSampling() {
//        hierarchicalLDAModel = new HierarchicalLDAModel(3, 1.0, 0.1);
//        hierarchicalLDAModel.readDocuments(generateTestDocuments());
//        hierarchicalLDAModel.doGibbsSampling(200);
//
//        String [] expectedTopic0 = {"aa", "ab", "ac", "ad", "ae"};
//        String [] expectedTopic1 = {"ba", "bb", "bc", "bd", "be"};
//        String [] expectedTopic2 = {"ca", "cb", "cc", "cd", "ce"};
//        String [] expectedTopic3 = {"da", "db", "dc", "dd", "de"};
//        String [] expectedTopic4 = {"ea", "eb", "ec", "ed", "ee"};
//        String [] expectedTopic5 = {"aa", "ba", "ca", "da", "ea"};
//        String [] expectedTopic6 = {"ab", "bb", "cb", "db", "eb"};
//        String [] expectedTopic7 = {"ac", "bc", "cc", "dc", "ec"};
//        String [] expectedTopic8 = {"ad", "bd", "cd", "dd", "ed"};
//        String [] expectedTopic9 = {"ae", "be", "ce", "de", "ee"};
//
//        Set<String> expectedSet0 = new HashSet<>(Arrays.asList(expectedTopic0));
//        Set<String> expectedSet1 = new HashSet<>(Arrays.asList(expectedTopic1));
//        Set<String> expectedSet2 = new HashSet<>(Arrays.asList(expectedTopic2));
//        Set<String> expectedSet3 = new HashSet<>(Arrays.asList(expectedTopic3));
//        Set<String> expectedSet4 = new HashSet<>(Arrays.asList(expectedTopic4));
//        Set<String> expectedSet5 = new HashSet<>(Arrays.asList(expectedTopic5));
//        Set<String> expectedSet6 = new HashSet<>(Arrays.asList(expectedTopic6));
//        Set<String> expectedSet7 = new HashSet<>(Arrays.asList(expectedTopic7));
//        Set<String> expectedSet8 = new HashSet<>(Arrays.asList(expectedTopic8));
//        Set<String> expectedSet9 = new HashSet<>(Arrays.asList(expectedTopic9));
//
//        boolean [] seen = new boolean[10];
//
//        Map<Integer, List<String>> result = hierarchicalLDAModel.getTopics(5, 2);
//        assertThat(result.size(), is(equalTo(10)));
//
//        for (List<String> resultList : result.values()) {
//            Set<String> resultSet = new HashSet<>(resultList);
//            seen[0] = (resultSet.equals(expectedSet0)) || seen[0];
//            seen[1] = (resultSet.equals(expectedSet1)) || seen[1];
//            seen[2] = (resultSet.equals(expectedSet2)) || seen[2];
//            seen[3] = (resultSet.equals(expectedSet3)) || seen[3];
//            seen[4] = (resultSet.equals(expectedSet4)) || seen[4];
//            seen[5] = (resultSet.equals(expectedSet5)) || seen[5];
//            seen[6] = (resultSet.equals(expectedSet6)) || seen[6];
//            seen[7] = (resultSet.equals(expectedSet7)) || seen[7];
//            seen[8] = (resultSet.equals(expectedSet8)) || seen[8];
//            seen[9] = (resultSet.equals(expectedSet9)) || seen[9];
//        }
//
//        boolean [] expected = {true, true, true, true, true, true, true, true, true};
//        assertThat(expected, is(equalTo(expected)));
//    }
}
