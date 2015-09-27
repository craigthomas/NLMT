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
    private String [] singleWordDoc = {"the"};
    private String [] threeWordDoc = {"the", "dog", "chased"};

    private HierarchicalLDAModel hierarchicalLDAModel;
    private List<List<String>> documents;
    private List<List<String>> singleWordDocument;
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
        singleWordDocument = new ArrayList<>();
        singleWordDocument.add(Arrays.asList(singleWordDoc));
        fiveWordDocument = new ArrayList<>();
        fiveWordDocument.add(Arrays.asList(document1));
        fiveWordDocumentTwice = new ArrayList<>();
        fiveWordDocumentTwice.add(Arrays.asList(document1));
        fiveWordDocumentTwice.add(Arrays.asList(document1));
        threeWordDocument = new ArrayList<>();
        threeWordDocument.add(Arrays.asList(threeWordDoc));
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
    public void getPathWordsLikelihood() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(threeWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(3, nodeMapper);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[1][2][1]++;
        hierarchicalLDAModel.documentWordLevel[1][2][1]++;
        testNode.addWord(0);
        testNode.addWord(1);
        testNode.addWord(2);
        testNode.addWord(2);
        testNode.addWord(2);
        testNode.setVisited(0);
        testNode.setVisited(1);
        assertThat(hierarchicalLDAModel.getPathWordsLikelihood(0, 1, testNode), is(equalTo(4.350457614735491)));
    }

    @Test
    public void getPathWordsLikelihoodSingleDocumentManyWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode node = new HierarchicalLDANode(5, nodeMapper);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        node.addWord(0);
        node.addWord(1);
        node.addWord(2);
        node.addWord(3);
        node.addWord(4);
        node.setVisited(0);
        assertThat(hierarchicalLDAModel.getPathWordsLikelihood(0, 1, node), is(equalTo(9.59158109119348)));
    }

    @Test
    public void getPathWordsLikelihoodDocumentNotInNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        HierarchicalLDANode node = new HierarchicalLDANode(5, nodeMapper);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        node.addWord(0);
        node.addWord(1);
        node.addWord(2);
        node.addWord(3);
        node.addWord(4);
        node.setVisited(0);
        assertThat(hierarchicalLDAModel.getPathWordsLikelihood(1, 1, node), is(equalTo(0.0)));
    }

    @Test
    public void getCalculatePathLikelihoodSinglePathSingleNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode node = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        node.addWord(0);
        node.addWord(1);
        node.addWord(2);
        node.addWord(3);
        node.addWord(4);
        node.setVisited(0);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(0, Arrays.asList(new Integer[]{0, 1})), is(equalTo(9.59158109119348)));
    }

    @Test
    public void getCalculatePathLikelihoodSinglePathSingleNodeTwoDocumentsVisitingNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode node = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        node.addWord(0);
        node.addWord(1);
        node.addWord(2);
        node.addWord(3);
        node.addWord(4);
        node.setVisited(0);
        node.setVisited(1);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(0, Arrays.asList(new Integer[]{0, 1})), is(equalTo(10.284728271753425)));
    }

    @Test
    public void getCalculatePathLikelihoodSinglePathSingleEmptyNode() {
        hierarchicalLDAModel = new HierarchicalLDAModel(2, 0.1,
                new double [] {1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(0, Arrays.asList(new Integer[]{0, -1})), is(equalTo(-2.3978952727983707)));
    }

    @Test
    public void getCalculatePathLikelihoodSinglePathMultipleEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.1,
                new double [] {1.0, 1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocumentTwice);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(0, Arrays.asList(new Integer[]{0, -1, -1})), is(equalTo(-2.3978952727983707)));
    }

    @Test
    public void getCalculatePathLikelihoodSinglePathWithRealAndEmptyNodes() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.1,
                new double [] {1.0, 1.0, 1.0}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode node = hierarchicalLDAModel.rootNode.spawnChild(1);
        hierarchicalLDAModel.documentWordLevel[0][0][1]++;
        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        node.addWord(0);
        node.addWord(1);
        node.addWord(2);
        node.addWord(3);
        node.addWord(4);
        node.setVisited(0);
        assertThat(hierarchicalLDAModel.calculatePathLikelihood(0, Arrays.asList(new Integer[]{0, 1, -1})), is(equalTo(5.075173815233827)));
    }

    @Test
    public void testGetNewTopicAllWordsInDocumentInLevelOneReturnsCorrectChild() {
        hierarchicalLDAModel = new HierarchicalLDAModel(HierarchicalLDAModel.DEFAULT_MAX_DEPTH, HierarchicalLDAModel.DEFAULT_GAMMA,
                new double [] {0.1, 0.1, 0.1}, HierarchicalLDAModel.DEFAULT_M, HierarchicalLDAModel.DEFAULT_PI);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode rootNode = new HierarchicalLDANode(5, new IdentifierObjectMapper<>());
        HierarchicalLDAPath path = new HierarchicalLDAPath(rootNode, 3);
        path.addNode(rootNode.spawnChild(1));
        HierarchicalLDANode child0 = rootNode.getChildren().get(0);
        path.addNode(child0.spawnChild(2));
        HierarchicalLDANode child1 = child0.getChildren().get(0);

        hierarchicalLDAModel.documentWordLevel[0][0][2] += 10;
        hierarchicalLDAModel.documentWordLevel[0][1][2]++;
        hierarchicalLDAModel.documentWordLevel[0][2][2]++;
        hierarchicalLDAModel.documentWordLevel[0][3][2]++;
        hierarchicalLDAModel.documentWordLevel[0][4][2]++;
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(0);
        child1.addWord(1);
        child1.addWord(2);
        child1.addWord(3);
        child1.addWord(4);

        hierarchicalLDAModel.documentWordLevel[0][1][1]++;
        hierarchicalLDAModel.documentWordLevel[0][2][1]++;
        hierarchicalLDAModel.documentWordLevel[0][3][1]++;
        hierarchicalLDAModel.documentWordLevel[0][4][1]++;
        child0.addWord(1);
        child0.addWord(2);
        child0.addWord(3);
        child0.addWord(4);

        hierarchicalLDAModel.documentWordLevel[0][1][0]++;
        hierarchicalLDAModel.documentWordLevel[0][2][0]++;
        hierarchicalLDAModel.documentWordLevel[0][3][0]++;
        hierarchicalLDAModel.documentWordLevel[0][4][0]++;
        rootNode.addWord(1);
        rootNode.addWord(2);
        rootNode.addWord(3);
        rootNode.addWord(4);

        int newTopic = hierarchicalLDAModel.chooseNewLevel(0, 0, path);
        assertThat(newTopic, is(equalTo(2)));
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
        documents = new ArrayList<>();
        documents.add(Arrays.asList(document1));

        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);

        HierarchicalLDANode testNode = new HierarchicalLDANode(5, hierarchicalLDAModel.nodeMapper);
        testNode.setVisited(0);
        testNode.addWord(0);
        testNode.addWord(0);
        testNode.addWord(0);
        testNode.addWord(0);
        testNode.addWord(0);
        testNode.addWord(1);
        testNode.addWord(1);
        testNode.addWord(1);
        testNode.addWord(1);
        testNode.addWord(2);
        testNode.addWord(2);
        testNode.addWord(3);
        testNode.addWord(3);
        testNode.addWord(3);

        Map<Integer, List<String>> expected = new HashMap<>();
        List<String> expectedWords = new ArrayList<>();
        expectedWords.add("this");
        expectedWords.add("is");
        expectedWords.add("test");
        expectedWords.add("a");
        expected.put(1, expectedWords);

        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(0), is(equalTo("this")));
        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(1), is(equalTo("is")));
        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(2), is(equalTo("a")));
        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(3), is(equalTo("test")));
        assertThat(hierarchicalLDAModel.vocabulary.getObjectFromIndex(4), is(equalTo("document")));

        assertThat(hierarchicalLDAModel.getTopics(4, 1), is(equalTo(expected)));
    }

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
