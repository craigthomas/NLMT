/**
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
public class LDAModelTest {

    private String [] document1 = {"this", "is", "a", "test", "document"};
    private String [] document2 = {"the", "cat", "sat", "on", "the", "mat"};
    private String [] document3 = {"the", "dog", "chased", "the", "cat"};
    private String [] longDocument = {"once", "upon", "a", "time", "there", "lived",
                                        "a", "dragon"};

    private LDAModel ldaModel;
    private List<List<String>> documents;

    @Before
    public void setUp() {
        documents = new ArrayList<>();
        documents.add(Arrays.asList(document1));
        documents.add(Arrays.asList(document2));
        documents.add(Arrays.asList(document3));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNumTopicsLessThan1ThrowsException() {
        ldaModel = new LDAModel(0);
    }

    @Test
    public void testAlphaSetToZeroAutoSetsAlpha() {
        ldaModel = new LDAModel(2, 0, 0);
        assertThat(ldaModel.alpha, is(equalTo(0.5)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAlphaSetToNegativeNumberThrowsException() {
        ldaModel = new LDAModel(2, -1, 0);
    }

    @Test
    public void testBetaSetToZeroAutoSetsBeta() {
        ldaModel = new LDAModel(2, 0, 0);
        assertThat(ldaModel.beta, is(equalTo(0.1)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBetaSetToNegativeNumberThrowsException() {
        ldaModel = new LDAModel(2, 0, -1);
    }

    @Test
    public void testReadDocumentsWorksCorrectly() {
        ldaModel = new LDAModel(5);
        ldaModel.readDocuments(documents);
        assertThat(ldaModel.documents.length, is(equalTo(3)));
        assertThat(ldaModel.vocabulary.size(), is(equalTo(12)));
        int [] expectedDocument1 = {0, 1, 2, 3, 4};
        int [] expectedDocument2 = {5, 6, 7, 8, 5, 9};
        int [] expectedDocument3 = {5, 10, 11, 5, 6};
        assertThat(ldaModel.documents[0].getWordArray(), is(equalTo(expectedDocument1)));
        assertThat(ldaModel.documents[1].getWordArray(), is(equalTo(expectedDocument2)));
        assertThat(ldaModel.documents[2].getWordArray(), is(equalTo(expectedDocument3)));
        int [] expectedTopics1 = {-1, -1, -1, -1, -1};
        int [] expectedTopics2 = {-1, -1, -1, -1, -1, -1};
        int [] expectedTopics3 = {-1, -1, -1, -1, -1};
        assertThat(ldaModel.documents[0].getTopicArray(), is(equalTo(expectedTopics1)));
        assertThat(ldaModel.documents[1].getTopicArray(), is(equalTo(expectedTopics2)));
        assertThat(ldaModel.documents[2].getTopicArray(), is(equalTo(expectedTopics3)));
    }

    @Test
    public void testInitializeSetsRandomTopics() {
        ldaModel = new LDAModel(5);
        ldaModel.readDocuments(documents);
        ldaModel.initialize();
        int [] expectedTopics1 = {-1, -1, -1, -1, -1};
        int [] expectedTopics2 = {-1, -1, -1, -1, -1, -1};
        int [] expectedTopics3 = {-1, -1, -1, -1, -1};
        assertThat(ldaModel.documents[0].getTopicArray(), is(not(equalTo(expectedTopics1))));
        assertThat(ldaModel.documents[1].getTopicArray(), is(not(equalTo(expectedTopics2))));
        assertThat(ldaModel.documents[2].getTopicArray(), is(not(equalTo(expectedTopics3))));
    }

    @Test
    public void testAddTopicToWordWorksCorrectly() {
        ldaModel = new LDAModel(5);
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        int currentTopicDocumentCount = ldaModel.topicDocumentCount[0][0];
        int currentWordTopicCount = ldaModel.wordTopicCount[0][0];
        int currentTopicTotals = ldaModel.topicTotals[0];

        ldaModel.addTopicToWord(0, 0, 0, 0);

        assertThat(ldaModel.documents[0].getTopicArray()[0], is(equalTo(0)));
        assertThat(ldaModel.topicDocumentCount[0][0], is(equalTo(currentTopicDocumentCount + 1)));
        assertThat(ldaModel.wordTopicCount[0][0], is(equalTo(currentWordTopicCount + 1)));
        assertThat(ldaModel.topicTotals[0], is(equalTo(currentTopicTotals + 1)));
    }

    @Test
    public void testRemoveTopicFromWordWorksCorrectly() {
        ldaModel = new LDAModel(5);
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        int currentTopicDocumentCount = ldaModel.topicDocumentCount[0][0];
        int currentWordTopicCount = ldaModel.wordTopicCount[0][0];
        int currentTopicTotals = ldaModel.topicTotals[0];

        ldaModel.removeTopicFromWord(0, 0, 0, 0);

        assertThat(ldaModel.documents[0].getTopicArray()[0], is(equalTo(-1)));
        assertThat(ldaModel.topicDocumentCount[0][0], is(equalTo(currentTopicDocumentCount - 1)));
        assertThat(ldaModel.wordTopicCount[0][0], is(equalTo(currentWordTopicCount - 1)));
        assertThat(ldaModel.topicTotals[0], is(equalTo(currentTopicTotals - 1)));
    }

    @Test
    public void testGetTopicProbabilityWorksCorrectlySimpleCase() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(document1));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.topicDocumentCount[0][0] = 0;
        ldaModel.topicDocumentCount[1][0] = 0;

        ldaModel.wordTopicCount[0][0] = 0;
        ldaModel.wordTopicCount[0][1] = 0;
        ldaModel.wordTopicCount[1][0] = 0;
        ldaModel.wordTopicCount[1][1] = 0;
        ldaModel.wordTopicCount[2][0] = 0;
        ldaModel.wordTopicCount[2][1] = 0;
        ldaModel.wordTopicCount[3][0] = 0;
        ldaModel.wordTopicCount[3][1] = 0;
        ldaModel.wordTopicCount[4][0] = 0;
        ldaModel.wordTopicCount[4][1] = 0;

        ldaModel.topicTotals[0] = 0;
        ldaModel.topicTotals[1] = 0;

        ldaModel.addTopicToWord(0, 0, 0, 0);
        ldaModel.addTopicToWord(0, 1, 1, 0);
        ldaModel.addTopicToWord(0, 2, 2, 0);
        ldaModel.addTopicToWord(0, 3, 3, 1);
        ldaModel.addTopicToWord(0, 4, 4, 1);

        assertThat(ldaModel.getTopicWeight(0, 0, 0), is(equalTo(1.1666666666666667)));
    }

    @Test
    public void testGetBestTopicWorksCorrectlySimpleCase() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(document1));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.topicDocumentCount[0][0] = 0;
        ldaModel.topicDocumentCount[1][0] = 0;

        ldaModel.wordTopicCount[0][0] = 0;
        ldaModel.wordTopicCount[0][1] = 0;
        ldaModel.wordTopicCount[1][0] = 0;
        ldaModel.wordTopicCount[1][1] = 0;
        ldaModel.wordTopicCount[2][0] = 0;
        ldaModel.wordTopicCount[2][1] = 0;
        ldaModel.wordTopicCount[3][0] = 0;
        ldaModel.wordTopicCount[3][1] = 0;
        ldaModel.wordTopicCount[4][0] = 0;
        ldaModel.wordTopicCount[4][1] = 0;

        ldaModel.topicTotals[0] = 0;
        ldaModel.topicTotals[1] = 0;

        ldaModel.addTopicToWord(0, 0, 0, 0);
        ldaModel.addTopicToWord(0, 1, 1, 0);
        ldaModel.addTopicToWord(0, 2, 2, 0);
        ldaModel.addTopicToWord(0, 3, 3, 0);
        ldaModel.addTopicToWord(0, 4, 4, 0);

        System.out.println(ldaModel.getTopicWeight(0, 0, 0));
        assertThat(ldaModel.getNewTopic(0, 0), is(equalTo(0)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopWordsForTopicThrowsExceptionOnInvalidTopic() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;

        ldaModel.getTopWordsForTopic(10, 5);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopWordsForTopicThrowsExceptionOnNegativeNumWords() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;

        ldaModel.getTopWordsForTopic(0, -5);
    }

    @Test
    public void testGetTopWordsForTopicReturnsBestWords() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;

        List<String> expected = new ArrayList<>();
        expected.add("time");
        expected.add("once");
        expected.add("there");
        assertThat(ldaModel.getTopWordsForTopic(0, 3), is(equalTo(expected)));
    }

    @Test
    public void testGetTopWordsForTopicMoreWordsThanVocabReturnsAll() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        // once
        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        // upon
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        // a
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        // time
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        // there
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        // lived
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;
        // dragon
        ldaModel.wordTopicCount[6][0] = 4;
        ldaModel.wordTopicCount[6][1] = 7;

        List<String> expected = new ArrayList<>();
        expected.add("time");
        expected.add("once");
        expected.add("there");
        expected.add("upon");
        expected.add("dragon");
        expected.add("lived");
        expected.add("a");

        assertThat(ldaModel.getTopWordsForTopic(0, 20), is(equalTo(expected)));
    }

    @Test
    public void testGetTopicsWorksCorrectly() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;

        List<String> expectedTopic0 = new ArrayList<>();
        expectedTopic0.add("time");
        expectedTopic0.add("once");
        expectedTopic0.add("there");

        List<String> expectedTopic1 = new ArrayList<>();
        expectedTopic1.add("upon");
        expectedTopic1.add("time");
        expectedTopic1.add("lived");

        List<List<String>> expected = new ArrayList<>();
        expected.add(expectedTopic0);
        expected.add(expectedTopic1);

        assertThat(ldaModel.getTopics(3), is(equalTo(expected)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetTopicsThrowsExceptionOnInvalidNumWords() {
        ldaModel = new LDAModel(2);
        documents.clear();
        documents.add(Arrays.asList(longDocument));
        ldaModel.readDocuments(documents);
        ldaModel.initialize();

        ldaModel.wordTopicCount[0][0] = 12;
        ldaModel.wordTopicCount[0][1] = 4;
        ldaModel.wordTopicCount[1][0] = 8;
        ldaModel.wordTopicCount[1][1] = 10;
        ldaModel.wordTopicCount[2][0] = 1;
        ldaModel.wordTopicCount[2][1] = 1;
        ldaModel.wordTopicCount[3][0] = 13;
        ldaModel.wordTopicCount[3][1] = 8;
        ldaModel.wordTopicCount[4][0] = 9;
        ldaModel.wordTopicCount[4][1] = 5;
        ldaModel.wordTopicCount[5][0] = 3;
        ldaModel.wordTopicCount[5][1] = 7;

        ldaModel.getTopics(0);
    }

    @Test
    public void testDoGibbsSampling() {
        ldaModel = new LDAModel(10, 1.0, 0.1);
        ldaModel.readDocuments(generateTestDocuments());
        ldaModel.doGibbsSampling(200);

        String [] expectedTopic0 = {"aa", "ab", "ac", "ad", "ae"};
        String [] expectedTopic1 = {"ba", "bb", "bc", "bd", "be"};
        String [] expectedTopic2 = {"ca", "cb", "cc", "cd", "ce"};
        String [] expectedTopic3 = {"da", "db", "dc", "dd", "de"};
        String [] expectedTopic4 = {"ea", "eb", "ec", "ed", "ee"};
        String [] expectedTopic5 = {"aa", "ba", "ca", "da", "ea"};
        String [] expectedTopic6 = {"ab", "bb", "cb", "db", "eb"};
        String [] expectedTopic7 = {"ac", "bc", "cc", "dc", "ec"};
        String [] expectedTopic8 = {"ad", "bd", "cd", "dd", "ed"};
        String [] expectedTopic9 = {"ae", "be", "ce", "de", "ee"};

        Set<String> expectedSet0 = new HashSet<>(Arrays.asList(expectedTopic0));
        Set<String> expectedSet1 = new HashSet<>(Arrays.asList(expectedTopic1));
        Set<String> expectedSet2 = new HashSet<>(Arrays.asList(expectedTopic2));
        Set<String> expectedSet3 = new HashSet<>(Arrays.asList(expectedTopic3));
        Set<String> expectedSet4 = new HashSet<>(Arrays.asList(expectedTopic4));
        Set<String> expectedSet5 = new HashSet<>(Arrays.asList(expectedTopic5));
        Set<String> expectedSet6 = new HashSet<>(Arrays.asList(expectedTopic6));
        Set<String> expectedSet7 = new HashSet<>(Arrays.asList(expectedTopic7));
        Set<String> expectedSet8 = new HashSet<>(Arrays.asList(expectedTopic8));
        Set<String> expectedSet9 = new HashSet<>(Arrays.asList(expectedTopic9));

        boolean [] seen = new boolean[10];

        List<List<String>> result = ldaModel.getTopics(5);
        assertThat(result.size(), is(equalTo(10)));

        for (List<String> resultList : result) {
            Set<String> resultSet = new HashSet<>(resultList);
            seen[0] = (resultSet.contains(expectedSet0)) || seen[0];
            seen[1] = (resultSet.contains(expectedSet1)) || seen[1];
            seen[2] = (resultSet.contains(expectedSet2)) || seen[2];
            seen[3] = (resultSet.contains(expectedSet3)) || seen[3];
            seen[4] = (resultSet.contains(expectedSet4)) || seen[4];
            seen[5] = (resultSet.contains(expectedSet5)) || seen[5];
            seen[6] = (resultSet.contains(expectedSet6)) || seen[6];
            seen[7] = (resultSet.contains(expectedSet7)) || seen[7];
            seen[8] = (resultSet.contains(expectedSet8)) || seen[8];
            seen[9] = (resultSet.contains(expectedSet9)) || seen[9];
        }

        boolean [] expected = {true, true, true, true, true, true, true, true, true};
        assertThat(expected, is(equalTo(expected)));
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
        for (int document_num = 0; document_num < 2000; document_num++) {
            List<String> document = new ArrayList<>();
            for (int counter = 0; counter < 20; counter++) {
                for (String word : topics[random.nextInt(10)]) {
                    document.add(word);
                }
            }
            documents.add(document);
        }
        return documents;
    }
}
