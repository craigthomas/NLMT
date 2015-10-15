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
package nlmt.datatypes;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Document class.
 */
public class DocumentTest {

    private IdentifierObjectMapper<String> vocabulary;
    private Document document;
    private List<String> simpleDocument;

    @Before
    public void setUp() {
        vocabulary = new IdentifierObjectMapper<>();
        document = new Document(vocabulary);
        simpleDocument = new ArrayList<>();
        simpleDocument.add("this");
        simpleDocument.add("is");
        simpleDocument.add("a");
        simpleDocument.add("test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDocumentInitWithNullVocabularyThrowsException() {
        new Document(null);
    }

    @Test
    public void testDocumentArraysEmptyOnInit() {
        assertThat(document.getTopicArray().length, is(equalTo(0)));
        assertThat(document.getWordArray().length, is(equalTo(0)));
    }

    @Test
    public void testDocumentEqualityNullDocument() {
        document = new Document(vocabulary);
        assertThat(document.equals(null), is(false));
    }

    @Test
    public void testDocumentEqualityEmptyDocumentsReturnsTrue() {
        document = new Document(vocabulary);
        Document document1 = new Document(vocabulary);
        assertThat(document.equals(document1), is(true));
    }

    @Test
    public void testDocumentEqualityDifferentVocabulariesReturnsFalse() {
        document = new Document(vocabulary);
        document.readDocument(simpleDocument);

        IdentifierObjectMapper<String> vocabulary1 = new IdentifierObjectMapper<>();
        vocabulary1.addObject("something");
        Document document1 = new Document(vocabulary1);
        document1.readDocument(simpleDocument);

        assertThat(document.equals(document1), is(false));
    }

    @Test
    public void testDocumentEqualitySameVocabulariesReturnsTrue() {
        document = new Document(vocabulary);
        document.readDocument(simpleDocument);

        Document document1 = new Document(vocabulary);
        document1.readDocument(simpleDocument);

        assertThat(document.equals(document1), is(true));
    }

    @Test
    public void testDocumentEqualitySameVocabulariesDifferentWordsReturnsTrue() {
        document = new Document(vocabulary);
        document.readDocument(simpleDocument);

        Document document1 = new Document(vocabulary);
        List<String> differentDocument = new ArrayList<>();
        differentDocument.add("different");
        document1.readDocument(differentDocument);

        assertThat(document.equals(document1), is(false));
    }

    @Test
    public void testReadDocumentAddsWordsToVocabulary() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        assertThat(vocabulary.getIndexFromObject("the"), is(not(equalTo(-1))));
        assertThat(vocabulary.getIndexFromObject("cat"), is(not(equalTo(-1))));
        assertThat(vocabulary.getIndexFromObject("sat"), is(not(equalTo(-1))));
        int theIndex = vocabulary.getIndexFromObject("the");
        int catIndex = vocabulary.getIndexFromObject("cat");
        int satIndex = vocabulary.getIndexFromObject("sat");
        assertThat(vocabulary.getObjectFromIndex(theIndex), is(equalTo("the")));
        assertThat(vocabulary.getObjectFromIndex(catIndex), is(equalTo("cat")));
        assertThat(vocabulary.getObjectFromIndex(satIndex), is(equalTo("sat")));
    }

    @Test
    public void testReadDocumentAddsWordsToWordArray() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        int theIndex = vocabulary.getIndexFromObject("the");
        int catIndex = vocabulary.getIndexFromObject("cat");
        int satIndex = vocabulary.getIndexFromObject("sat");
        int [] expectedWordArray = {theIndex, catIndex, satIndex};
        assertThat(document.getWordArray(), is(equalTo(expectedWordArray)));
    }

    @Test
    public void testGetRawWordsWorksCorrectly() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        assertThat(document.getRawWords(), is(equalTo(words)));
    }

    @Test
    public void testTopicArrayAllNegativeOnesWhenDocumentRead() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        int [] expectedTopicArrayArray = {-1, -1, -1};
        assertThat(document.getTopicArray(), is(equalTo(expectedTopicArrayArray)));
    }

    @Test
    public void testTopicArraySetSingleCorrectly() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        document.setTopicForWord(0, 1);
        int [] expectedTopicArrayArray = {1, -1, -1};
        assertThat(document.getTopicArray(), is(equalTo(expectedTopicArrayArray)));
    }

    @Test
    public void testTopicArraySetAllCorrectly() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        document.setTopicForWord(0, 1);
        document.setTopicForWord(1, 2);
        document.setTopicForWord(2, 3);
        int [] expectedTopicArrayArray = {1, 2, 3};
        assertThat(document.getTopicArray(), is(equalTo(expectedTopicArrayArray)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetTopicOnNegativeWordIndexThrowsException() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        document.setTopicForWord(-1, 1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetTopicOnWordIndexTooLargeThrowsException() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        document.setTopicForWord(3, 1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetTopicOnNoDocumentThrowsException() {
        document.setTopicForWord(0, 1);
    }

    @Test
    public void testGetWordsOnDocumentAllSameWord() {
        String [] words = {"the", "the", "the"};
        int [] vocabList = {0, 0, 0};
        document.readDocument(Arrays.asList(words));
        assertThat(document.getWordArray().length, is(equalTo(3)));
        assertThat(document.getWordArray(), is(equalTo(vocabList)));
        assertThat(document.getRawWords(), is(equalTo(words)));
    }

    @Test
    public void testClearTopicsWorksCorrectly() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        document.setTopicForWord(0, 1);
        document.setTopicForWord(1, 2);
        document.setTopicForWord(2, 3);
        document.clearTopics();
        int [] expectedTopicArrayArray = {-1, -1, -1};
        assertThat(document.getTopicArray(), is(equalTo(expectedTopicArrayArray)));
    }

    @Test
    public void testGetWordSetWorksCorrectly() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        Set<Integer> expected = new HashSet<>();
        expected.add(0);
        expected.add(1);
        expected.add(2);
        assertThat(document.getWordSet(), is(equalTo(expected)));
    }
}
