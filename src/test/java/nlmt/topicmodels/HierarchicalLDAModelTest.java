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
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
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

    @Test
    public void testEqualsNullWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        assertThat(hierarchicalLDAModel.equals(null), is(false));
    }

    @Test
    public void testEqualsOnInitIsTrue() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel();
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(true));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(true));
    }

    @Test
    public void testEqualsDifferentGammaIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, 2.0,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentMaxDepthIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentEtaIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentMIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, 0.2, HierarchicalLDAModel.DEFAULT_PI);
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentPiIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, 10.0);
        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentDocumentsIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);

        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel();
        hierarchicalLDAModel1.readDocuments(fiveWordDocument);

        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

    @Test
    public void testEqualsDifferentPathsIsFalse() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);

        HierarchicalLDAModel hierarchicalLDAModel1 = new HierarchicalLDAModel();
        hierarchicalLDAModel1.readDocuments(documents);
        hierarchicalLDAModel1.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel1.rootNode, 3);

        assertThat(hierarchicalLDAModel.equals(hierarchicalLDAModel1), is(false));
        assertThat(hierarchicalLDAModel.hashCode() == hierarchicalLDAModel1.hashCode(), is(false));
    }

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
        HierarchicalLDANode testNode = new HierarchicalLDANode(null, 5);
        nodeMapper.addObject(testNode);
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
        HierarchicalLDANode testNode = new HierarchicalLDANode(null, 5);
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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0], Arrays.asList(0, 1)), is(equalTo(-14.898374489664242)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathSingleNodeTwoDocumentsVisitingNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentPaths[0].addNode(testNode);

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0], Arrays.asList(0, 1)), is(equalTo(-9.50626035276342)));
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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0], Arrays.asList(0, -1, -1)), is(equalTo(-8.92365779985749)));
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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0], Arrays.asList(0, -1, -1)), is(equalTo(0.0)));
    }

    @Test
    public void testCalculatePathLikelihoodSinglePathWithRealAndEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0],
                Arrays.asList(0, 1, -1)), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testCalculateWordLikelihoodReturnsBiggerLikelihoodForPathWithMoreSimilarWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 1.0,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        HierarchicalLDANode testNode = hierarchicalLDAModel.rootNode.spawnChild(1);

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        testNode.setId(hierarchicalLDAModel.nodeMapper.addObject(testNode));

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
        assertThat(hierarchicalLDAModel.calculateWordLikelihood(hierarchicalLDAModel.documents[0],
                hierarchicalLDAModel.documentPaths[0],
                Arrays.asList(0, 1, -1)), is(equalTo(-9.50626035276342)));
    }

    @Test
    public void testGetTopicsReturnsEmptyMapWhenNoDocuments() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.rootNode = new HierarchicalLDANode(null, 3);         // node 0
        HierarchicalLDANode node1 = hierarchicalLDAModel.rootNode.spawnChild(1);  // node 1
        HierarchicalLDANode node2 = hierarchicalLDAModel.rootNode.spawnChild(1);  // node 2
        HierarchicalLDANode node3 = node1.spawnChild(2);                          // node 3
        hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode);
        hierarchicalLDAModel.nodeMapper.addObject(node1);
        hierarchicalLDAModel.nodeMapper.addObject(node2);
        hierarchicalLDAModel.nodeMapper.addObject(node3);
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

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        node1.setId(hierarchicalLDAModel.nodeMapper.addObject(node1));
        node1.setId(hierarchicalLDAModel.nodeMapper.addObject(node1));
        node1.setId(hierarchicalLDAModel.nodeMapper.addObject(node1));
        node1.setId(hierarchicalLDAModel.nodeMapper.addObject(node1));

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
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, 2.0);
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
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, 2.0);
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
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                HierarchicalLDAModel.DEFAULT_ETA, HierarchicalLDAModel.DEFAULT_M, 2.0);
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

    @Test
    public void testPrettyPrintTreeWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> testDocuments = new ArrayList<>();
        testDocuments.add(Arrays.asList(oneWord));
        testDocuments.add(Arrays.asList(threeWordDoc));
        hierarchicalLDAModel.readDocuments(testDocuments);
        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        path.addNode(hierarchicalLDAModel.rootNode.spawnChild(1));
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.getChildren().get(0);

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        child0.setId(hierarchicalLDAModel.nodeMapper.addObject(child0));

        hierarchicalLDAModel.rootNode.setVisited(0);
        hierarchicalLDAModel.rootNode.setVisited(1);
        child0.setVisited(1);

        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(child0::addWord);

        String expected = "- Node 0: 2 docs, words: [test]\n--- Node 1: 1 docs, words: [dog, chased, the]\n";
        assertThat(hierarchicalLDAModel.prettyPrintTree(10), is(equalTo(expected)));
    }

    @Test
    public void testGetWordProbabilitiesEmptyNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        Word word = new Word("test", hierarchicalLDAModel.vocabulary.getIndexFromObject("test"));
        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        path.addNode(child0);
        path.addNode(child1);
        double [] expected = {0.2, 0.2, 0.2};
        assertThat(hierarchicalLDAModel.getWordProbabilities(word, path), is(equalTo(expected)));
    }

    @Test
    public void testGetWordProbabilitiesWordsInRootNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        Word word = new Word("test", hierarchicalLDAModel.vocabulary.getIndexFromObject("test"));
        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        path.addNode(child0);
        path.addNode(child1);
        double [] expected = {0.2, 0.2, 0.2};
        assertThat(hierarchicalLDAModel.getWordProbabilities(word, path), is(equalTo(expected)));
    }

    @Test
    public void testGetWordProbabilitiesMultipleDocumentsSomeWordsInRootNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        Word word = new Word("test", hierarchicalLDAModel.vocabulary.getIndexFromObject("test"));
        HierarchicalLDAPath path = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(child1::addWord);
        path.addNode(child0);
        path.addNode(child1);
        double [] expected = {0.08571428571428572, 0.08333333333333333, 0.041666666666666664};
        assertThat(hierarchicalLDAModel.getWordProbabilities(word, path), is(equalTo(expected)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInferenceWithZeroIterationsThrowsException() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.inference(new ArrayList<>(), 0, true);
    }

    @Test
    public void testInferenceOnEmptyDocumentDoesNothing() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<Integer> expectedNodes = new ArrayList<>();
        List<Double> expectedDistributions = new ArrayList<>();
        assertThat(hierarchicalLDAModel.inference(new ArrayList<>(), 1, true), is(equalTo(Pair.of(expectedNodes, expectedDistributions))));
    }

    @Test
    public void testInferenceGammaRestoredWhenOverridden() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> documentList = new ArrayList<>();
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document2));
        documentList.add(Arrays.asList(document3));
        hierarchicalLDAModel.readDocuments(documentList);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[4].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[5].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[6].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[7].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[8].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[9].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[10].getWordSet().forEach(child0::addWord);
        hierarchicalLDAModel.documents[11].getWordSet().forEach(child1::addWord);
        List<String> testDocument = new ArrayList<>();
        testDocument.add("test");
        hierarchicalLDAModel.inference(testDocument, 1, true);
        assertThat(hierarchicalLDAModel.gamma, is(equalTo(HierarchicalLDAModel.DEFAULT_GAMMA)));
    }

    @Test
    public void testInferenceGammaOverridePreventsNewNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        List<List<String>> documentList = new ArrayList<>();
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document2));
        documentList.add(Arrays.asList(document3));
        hierarchicalLDAModel.readDocuments(documentList);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        child0.setId(hierarchicalLDAModel.nodeMapper.addObject(child0));
        child1.setId(hierarchicalLDAModel.nodeMapper.addObject(child1));

        new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[4].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[5].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[6].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[7].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[8].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[9].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[10].getWordSet().forEach(child0::addWord);
        hierarchicalLDAModel.documents[11].getWordSet().forEach(child1::addWord);
        List<String> testDocument = new ArrayList<>();
        testDocument.add("test");
        Pair<List<Integer>, List<Double>> result = hierarchicalLDAModel.inference(testDocument, 1, true);

        List<Integer> expectedPath = new ArrayList<>();
        expectedPath.add(0);
        expectedPath.add(1);
        expectedPath.add(2);
        assertThat(result.getLeft(), is(equalTo(expectedPath)));
    }

    @Test
    public void testSingleWordDocumentInferenceWorksCorrectlyWhenWordInRootNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {2.0, 0.1, 0.1}, 0.1, 1.0);
        List<List<String>> documentList = new ArrayList<>();
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document2));
        documentList.add(Arrays.asList(document3));
        hierarchicalLDAModel.readDocuments(documentList);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[4].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[5].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[6].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[7].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[8].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[9].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[10].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[11].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[12].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[13].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[14].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[15].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[16].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[17].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[18].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[19].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[20].getWordSet().forEach(child0::addWord);
        hierarchicalLDAModel.documents[21].getWordSet().forEach(child1::addWord);

        List<String> testDocument = new ArrayList<>();
        testDocument.add("test");

        List<Double> expectedDistributions = new ArrayList<>();
        expectedDistributions.add(1.0);
        expectedDistributions.add(0.0);
        expectedDistributions.add(0.0);

        // We only care where the word distributions ended up. We have one word "test"
        // that should end up in the root node, so just ignore the paths, since new paths may have been generated
        // by the Gibbs sampler
        Pair<List<Integer>, List<Double>> results = hierarchicalLDAModel.inference(testDocument, 3, false);
        assertThat(results.getRight(), is(equalTo(expectedDistributions)));
    }

    @Test
    public void testSingleWordDocumentInferenceWorksCorrectlyWhenWordInLeafNode() {
        // We need equal probabilities of seeing a word at any level of the tree
        // Control gamma so that no new nodes are really generated
        hierarchicalLDAModel = new HierarchicalLDAModel(3, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 2.0}, 0.99, 1.0);
        List<List<String>> documentList = new ArrayList<>();
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document2));
        documentList.add(Arrays.asList(document3));
        hierarchicalLDAModel.readDocuments(documentList);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[3].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[4].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[5].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[6].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[7].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[8].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[9].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[10].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[11].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[12].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[13].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[14].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[15].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[16].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[17].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[18].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[19].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[20].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);
        hierarchicalLDAModel.documents[21].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);

        List<String> testDocument = new ArrayList<>();
        testDocument.add("test");

        List<Double> expectedDistributions = new ArrayList<>();
        expectedDistributions.add(0.0);
        expectedDistributions.add(0.0);
        expectedDistributions.add(1.0);
        List<Integer> expectedNodes = new ArrayList<>();
        expectedNodes.add(0);
        expectedNodes.add(1);
        expectedNodes.add(2);

        // This time both the path and the distributions should match. Once again, we only have
        // a single word in our document ("test"), and that word should only exist along a single
        // path at the leaf node of that path
        Pair<List<Integer>, List<Double>> results = hierarchicalLDAModel.inference(testDocument, 10, true);
        assertThat(results, is(equalTo(Pair.of(expectedNodes, expectedDistributions))));
    }

    @Test
    public void testGetHierarchyComplexExampleWorksCorrectly() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.rootNode = new HierarchicalLDANode(null, 1);        // node 0
        HierarchicalLDANode node1 = hierarchicalLDAModel.rootNode.spawnChild(1); // node 1
        HierarchicalLDANode node2 = hierarchicalLDAModel.rootNode.spawnChild(1); // node 2
        HierarchicalLDANode node3 = node2.spawnChild(2);                         // node 3
        HierarchicalLDANode node4 = node3.spawnChild(3);                         // node 4

        hierarchicalLDAModel.rootNode.setId(hierarchicalLDAModel.nodeMapper.addObject(hierarchicalLDAModel.rootNode));
        node1.setId(hierarchicalLDAModel.nodeMapper.addObject(node1));
        node2.setId(hierarchicalLDAModel.nodeMapper.addObject(node2));
        node3.setId(hierarchicalLDAModel.nodeMapper.addObject(node3));
        node4.setId(hierarchicalLDAModel.nodeMapper.addObject(node4));

        Map<Integer, List<Integer>> expected = new HashMap<>();
        expected.put(0, Arrays.asList(1, 2));
        expected.put(1, new ArrayList<>());
        expected.put(2, Collections.singletonList(3));
        expected.put(3, Collections.singletonList(4));
        expected.put(4, new ArrayList<>());
        assertThat(hierarchicalLDAModel.getHierarchy(), is(equalTo(expected)));
    }

    @Test
    public void testSerializationRoundTrip() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 2.0,
                new double [] {0.1, 0.1, 2.0}, 0.99, 1.0);
        List<List<String>> documentList = new ArrayList<>();
        documentList.add(Arrays.asList(document1));
        documentList.add(Arrays.asList(document2));
        documentList.add(Arrays.asList(document3));
        hierarchicalLDAModel.readDocuments(documentList);
        HierarchicalLDANode child0 = hierarchicalLDAModel.rootNode.spawnChild(1);
        HierarchicalLDANode child1 = child0.spawnChild(2);
        hierarchicalLDAModel.documents[0].getWordSet().forEach(child1::addWord);
        hierarchicalLDAModel.documents[1].getWordSet().forEach(child0::addWord);
        hierarchicalLDAModel.documents[2].getWordSet().forEach(hierarchicalLDAModel.rootNode::addWord);

        hierarchicalLDAModel.rootNode.setVisited(2);
        child0.setVisited(1);
        child1.setVisited(0);

        hierarchicalLDAModel.documentPaths[0] = new HierarchicalLDAPath(hierarchicalLDAModel.rootNode, 3);
        hierarchicalLDAModel.documentPaths[0].addNode(child0);
        hierarchicalLDAModel.documentPaths[0].addNode(child1);

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(hierarchicalLDAModel);
            byte [] serializedObjectArray = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            assertThat(serializedObjectArray.length, is(not(CoreMatchers.equalTo(0))));

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObjectArray);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            HierarchicalLDAModel deserializedModel = (HierarchicalLDAModel) objectInputStream.readObject();
            assertThat(hierarchicalLDAModel.equals(deserializedModel), is(true));
        } catch (IOException e) {
            assertFalse("IOException occurred: " + e.getMessage(), true);
        } catch (ClassNotFoundException e) {
            assertFalse("ClassNotFoundException occurred: " + e.getMessage(), true);
        }

    }
}
