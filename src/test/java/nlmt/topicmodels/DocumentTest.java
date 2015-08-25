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

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Document class.
 */
public class DocumentTest {

    private Vocabulary vocabulary;
    private Document document;

    @Before
    public void setUp() {
        vocabulary = new Vocabulary();
        document = new Document(vocabulary);
    }

    @Test
    public void testDocumentArraysEmptyOnInit() {
        assertThat(document.getTopicArray().length, is(equalTo(0)));
        assertThat(document.getWordArray().length, is(equalTo(0)));
    }

    @Test
    public void testReadDocumentAddsWordsToVocabulary() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        assertThat(vocabulary.getIndexFromWord("the"), is(not(equalTo(-1))));
        assertThat(vocabulary.getIndexFromWord("cat"), is(not(equalTo(-1))));
        assertThat(vocabulary.getIndexFromWord("sat"), is(not(equalTo(-1))));
        int theIndex = vocabulary.getIndexFromWord("the");
        int catIndex = vocabulary.getIndexFromWord("cat");
        int satIndex = vocabulary.getIndexFromWord("sat");
        assertThat(vocabulary.getWordFromIndex(theIndex), is(equalTo("the")));
        assertThat(vocabulary.getWordFromIndex(catIndex), is(equalTo("cat")));
        assertThat(vocabulary.getWordFromIndex(satIndex), is(equalTo("sat")));
    }

    @Test
    public void testReadDocumentAddsWordsToWordArray() {
        String [] words = {"the", "cat", "sat"};
        document.readDocument(Arrays.asList(words));
        int theIndex = vocabulary.getIndexFromWord("the");
        int catIndex = vocabulary.getIndexFromWord("cat");
        int satIndex = vocabulary.getIndexFromWord("sat");
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
}
