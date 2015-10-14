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

package nlmt.datatypes;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the SparseDocument class.
 */
public class SparseDocumentTest
{
    private SparseDocument sparseDocument;
    private IdentifierObjectMapper<String> vocabulary;

    @Before
    public void setUp() {
        vocabulary = new IdentifierObjectMapper<>();
    }

    @Test
    public void testReadDocumentWorksCorrectly() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        Set<String> expectedWords = new HashSet<>(Arrays.asList("wordOne", "wordTwo", "wordThree"));
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        assertThat(sparseDocument.getWordSet().stream().map(Word::getRawWord).collect(Collectors.toSet()), is(equalTo(expectedWords)));
        assertThat(sparseDocument.getWordCount(vocabulary.getIndexFromObject("wordOne")), is(equalTo(1)));
        assertThat(sparseDocument.getWordCount(vocabulary.getIndexFromObject("wordTwo")), is(equalTo(1)));
        assertThat(sparseDocument.getWordCount(vocabulary.getIndexFromObject("wordThree")), is(equalTo(2)));
    }

    @Test
    public void testSetTopicForWordSingleWordWorksCorrectly() {
        List<String> document = Arrays.asList("wordOne");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 31);
        assertThat(sparseDocument.getTopicForWord(vocabulary.getIndexFromObject("wordOne")), is(equalTo(31)));
    }

    @Test
    public void testSetTopicForNonExistentWordDoesNothing() {
        List<String> document = Arrays.asList("wordOne");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 31);
        sparseDocument.setTopicForWord(1111, 2);
        assertThat(sparseDocument.getTopicForWord(vocabulary.getIndexFromObject("wordOne")), is(equalTo(31)));
    }

    @Test
    public void testGetTopicForNonExistentWordReturnsNegativeOne() {
        List<String> document = Arrays.asList("wordOne");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 31);
        assertThat(sparseDocument.getTopicForWord(1111), is(equalTo(-1)));
    }

    @Test
    public void testGetCountForNonExistentWordReturnsZero() {
        List<String> document = Arrays.asList("wordOne");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        assertThat(sparseDocument.getWordCount(1111), is(equalTo(0)));
    }

    @Test
    public void testGetTopicsWorksCorrectly() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Set<Integer> expectedTopics = new HashSet<>(Arrays.asList(1, 2, 3));
        assertThat(sparseDocument.getTopics(), is(equalTo(expectedTopics)));
    }

    @Test
    public void testGetTopicCountsEmptyDocumentReturnsEmptyMap() {
        sparseDocument = new SparseDocument(vocabulary);
        assertThat(sparseDocument.getTopicCounts(), is(equalTo(new HashMap<>())));
    }

    @Test
    public void testGetTopicCountsReturnsNegativeOneCountWhenNoTopicsAssigned() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        Map<Integer, Integer> expectedTopicCounts = new HashMap<>();
        expectedTopicCounts.put(-1, 3);
        assertThat(sparseDocument.getTopicCounts(), is(equalTo(expectedTopicCounts)));
    }

    @Test
    public void testGetTopicCountsWorksCorrectlyWhenTopicsAssigned() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Map<Integer, Integer> expectedTopicCounts = new HashMap<>();
        expectedTopicCounts.put(1, 1);
        expectedTopicCounts.put(2, 1);
        expectedTopicCounts.put(3, 2);
        assertThat(sparseDocument.getTopicCounts(), is(equalTo(expectedTopicCounts)));
    }

    @Test
    public void testGetWordTopicCountWorksCorrectly() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Word word = new Word("wordThree", vocabulary.getIndexFromObject("wordThree"));
        Map<Integer, Integer> expectedTopicCounts = new HashMap<>();
        expectedTopicCounts.put(3, 2);
        assertThat(sparseDocument.getWordTopicCount(word), is(equalTo(expectedTopicCounts)));
    }

    @Test
    public void testGetWordTopicCountEmptyOnNonExistentWord() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Word word = new Word("wordFour", vocabulary.getIndexFromObject("wordFour"));
        Map<Integer, Integer> expectedTopicCounts = new HashMap<>();
        assertThat(sparseDocument.getWordTopicCount(word), is(equalTo(expectedTopicCounts)));
    }

    @Test
    public void testGetWordTopicNegativeOneWhenNotInitialized() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Word word = new Word("wordOne", vocabulary.getIndexFromObject("wordOne"));
        Map<Integer, Integer> expectedTopicCounts = new HashMap<>();
        expectedTopicCounts.put(-1, 1);
        assertThat(sparseDocument.getWordTopicCount(word), is(equalTo(expectedTopicCounts)));
    }

    @Test
    public void testGetWordCountsByTopicCorrectlyWhenTopicsAssigned() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Map<Integer, Integer> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put(vocabulary.getIndexFromObject("wordThree"), 2);
        assertThat(sparseDocument.getWordCountsByTopic(3), is(equalTo(expectedWordCounts)));
    }

    @Test
    public void testGetWordCountsByTopicIsEmptyOnNonExistentTopic() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 1);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 3);
        Map<Integer, Integer> expectedWordCounts = new HashMap<>();
        assertThat(sparseDocument.getWordCountsByTopic(4), is(equalTo(expectedWordCounts)));
    }

    @Test
    public void testGetWordCountsByTopicMultipleWordsSameTopic() {
        List<String> document = Arrays.asList("wordOne", "wordTwo", "wordThree", "wordThree");
        sparseDocument = new SparseDocument(vocabulary);
        sparseDocument.readDocument(document);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordOne"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordTwo"), 2);
        sparseDocument.setTopicForWord(vocabulary.getIndexFromObject("wordThree"), 2);
        Map<Integer, Integer> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put(vocabulary.getIndexFromObject("wordOne"), 1);
        expectedWordCounts.put(vocabulary.getIndexFromObject("wordTwo"), 1);
        expectedWordCounts.put(vocabulary.getIndexFromObject("wordThree"), 2);
        assertThat(sparseDocument.getWordCountsByTopic(2), is(equalTo(expectedWordCounts)));
    }
}
