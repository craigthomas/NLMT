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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Vocabulary class.
 */
public class VocabularyTest {

    private Vocabulary vocabulary;

    @Before
    public void setUp() {
        vocabulary = new Vocabulary();
    }

    @Test
    public void testVocabularyEmptyOnInit() {
        assertThat(vocabulary.size(), is(equalTo(0)));
    }

    @Test
    public void testGetIndexFromWordReturnsNegativeOneOnEmptyVocabulary() {
        assertThat(vocabulary.getIndexFromWord("nothing"), is(equalTo(-1)));
    }

    @Test
    public void testGetWordFromIndexReturnsEmptyStringOnEmptyVocabulary() {
        assertThat(vocabulary.getWordFromIndex(1), is(equalTo("")));
    }

    @Test
    public void testAddWordToVocabularyWorksCorrectly() {
        vocabulary.addWord("test");
        assertThat(vocabulary.size(), is(equalTo(1)));
        assertThat(vocabulary.getIndexFromWord("test"), is(equalTo(0)));
        assertThat(vocabulary.getWordFromIndex(0), is(equalTo("test")));
    }

    @Test
    public void testGetWordNotInPopulatedVocabularyWorksCorrectly() {
        vocabulary.addWord("some");
        vocabulary.addWord("words");
        assertThat(vocabulary.size(), is(equalTo(2)));
        assertThat(vocabulary.getIndexFromWord("not"), is(equalTo(-1)));
        assertThat(vocabulary.getWordFromIndex(2), is(equalTo("")));
    }

    @Test
    public void testGetWordFromIndexReturnsEmptyOnNegativeIndex() {
        assertThat(vocabulary.getWordFromIndex(-100), is(equalTo("")));
    }

    @Test
    public void testAddWordTwiceDoesNotAddTwice() {
        vocabulary.addWord("word");
        vocabulary.addWord("word");
        assertThat(vocabulary.size(), is(equalTo(1)));
        assertThat(vocabulary.getIndexFromWord("word"), is(equalTo(0)));
        assertThat(vocabulary.getWordFromIndex(0), is(equalTo("word")));
        assertThat(vocabulary.getWordFromIndex(1), is(equalTo("")));
    }
}
