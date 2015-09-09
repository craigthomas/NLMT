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

    private HierarchicalLDAModel hierarchicalLDAModel;
    private List<List<String>> documents;
    private List<List<String>> singleWordDocument;
    private List<List<String>> fiveWordDocument;
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
    public void testGetSumLogEtaPlusWordsNotInDocument() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(singleWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(2, 1, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getSumLogEtaPlusWordsNotInDocument(0, testNode), is(equalTo(0.7419373447293773)));
    }

    @Test
    public void testGetLogEtaSumPlusWordsNotInDocument() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(singleWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(2, 1, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getLogEtaSumPlusWordsNotInDocument(0, testNode), is(equalTo(0.7419373447293773)));
    }

    @Test
    public void testGetLogEtaPlusSumAllWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(singleWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(2, 1, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getLogEtaPlusSumAllWords(testNode), is(equalTo(1.1314021114911006)));
    }

    @Test
    public void testGetSumLogAllWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(singleWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(2, 1, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(1, 0);
        testNode.addWord(1, 0);
        assertThat(hierarchicalLDAModel.getSumLogAllWords(testNode), is(equalTo(1.1314021114911006)));
    }

    @Test
    public void getTopicLikelihood() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(fiveWordDocument);
        HierarchicalLDANode testNode = new HierarchicalLDANode(2, 5, nodeMapper);
        testNode.addWord(0, 0);
        testNode.addWord(0, 1);
        testNode.addWord(0, 2);
        testNode.addWord(1, 2);
        testNode.addWord(1, 2);
        assertThat(hierarchicalLDAModel.getTopicLikelihood(0, testNode), is(equalTo(4.396797951994191)));
    }

    @Test
    public void getTopicLikelihoodSingleDocumentManyWords() {
        hierarchicalLDAModel = new HierarchicalLDAModel();
        hierarchicalLDAModel.readDocuments(documents);
        HierarchicalLDANode node = new HierarchicalLDANode(3, 5, new IdentifierObjectMapper<>());
        node.addWord(0, 0);
        node.addWord(0, 1);
        node.addWord(0, 2);
        node.addWord(0, 3);
        node.addWord(0, 4);
        assertThat(hierarchicalLDAModel.getTopicLikelihood(0, node), is(equalTo(10.347248628734768)));
    }

    @Test
    public void testGetNewTopicAllWordsInDocumentInLevelOneReturnsCorrectChild() {
        hierarchicalLDAModel = new HierarchicalLDAModel(3, 0.5, HierarchicalLDAModel.DEFAULT_GAMMA);
        documents.clear();
        documents.add(Arrays.asList(document1));
        hierarchicalLDAModel.readDocuments(documents);
        hierarchicalLDAModel.initialize();

        HierarchicalLDANode rootNode = new HierarchicalLDANode(3, hierarchicalLDAModel.vocabulary.size(), new IdentifierObjectMapper<>());
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

        HierarchicalLDANode testNode = new HierarchicalLDANode(3, hierarchicalLDAModel.vocabulary.size(), nodeMapper);
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
