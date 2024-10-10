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

package ca.craigthomas.nlmt.datatypes;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Word class.
 */
public class WordTest {

    private Word word;

    @Before
    public void setUp() {
        word = new Word("test", 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRawWordNullOnInitThrowsException() {
        new Word(null, 0);
    }

    @Test
    public void testWordNotEqualToNull() {
        assertThat(word.equals(null), is(false));
    }

    @Test
    public void testWordEqualityWorksWhenAllEqual() {
        Word word1 = new Word("test", 0);
        assertThat(word.equals(word1), is(true));
        assertThat(word.hashCode() == word1.hashCode(), is(true));
    }

    @Test
    public void testWordDifferentVocabularyIdsNotEqual() {
        Word word1 = new Word("test", 1);
        assertThat(word.equals(word1), is(false));
        assertThat(word.hashCode() == word1.hashCode(), is(false));
    }

    @Test
    public void testWordDifferentRawWordsNotEqual() {
        Word word1 = new Word("test1", 0);
        assertThat(word.equals(word1), is(false));
        assertThat(word.hashCode() == word1.hashCode(), is(false));
    }

    @Test
    public void testWordDifferentTopicAssignmentsNotEqual() {
        Word word1 = new Word("test", 0);
        word1.setTopic(1);
        assertThat(word.equals(word1), is(false));
        assertThat(word.hashCode() == word1.hashCode(), is(false));
    }

    @Test
    public void testWordDifferentCountsNotEqual() {
        Word word1 = new Word("test", 0);
        word1.setTotalCount(2);
        assertThat(word.equals(word1), is(false));
        assertThat(word.hashCode() == word1.hashCode(), is(false));
    }

    @Test
    public void testTotalCountOneOnInit() {
        assertThat(word.getTotalCount(), is(equalTo(1)));
    }

    @Test
    public void testTopicIsNegativeOneOnInit() {
        assertThat(word.getTopic(), is(equalTo(-1)));
    }

    @Test
    public void testRawWordSetCorrectlyOnInit() {
        assertThat(word.getRawWord(), is(equalTo("test")));
    }

    @Test
    public void testVocabularyIdSetCorrectlyOnInit() {
        assertThat(word.getVocabularyId(), is(equalTo(0)));
    }

    @Test
    public void testSetAndGetTotalCountWorksCorrectly() {
        word.setTotalCount(13);
        assertThat(word.getTotalCount(), is(equalTo(13)));
    }

    @Test
    public void testSetAndGetTopicWorksCorrectly() {
        word.setTopic(13);
        assertThat(word.getTopic(), is(equalTo(13)));
    }

    @Test
    public void testSerializationRoundTrip() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(word);
            byte[] serializedObjectArray = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            assertThat(serializedObjectArray.length, is(not(equalTo(0))));

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObjectArray);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Word deserializedWord = (Word) objectInputStream.readObject();
            assertThat(word.equals(deserializedWord), is(true));
        } catch (IOException e) {
            assertFalse("IOException occurred: " + e.getMessage(), true);
        } catch (ClassNotFoundException e) {
            assertFalse("ClassNotFoundException occurred: " + e.getMessage(), true);
        }
    }
}
