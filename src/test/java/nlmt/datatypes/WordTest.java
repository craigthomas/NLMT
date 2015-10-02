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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the Word class.
 */
public class WordTest {

    private Word word;

    @Test
    public void testTotalCountOneOnInit() {
        word = new Word("test", 0);
        assertThat(word.getTotalCount(), is(equalTo(1)));
    }

    @Test
    public void testTopicIsNegativeOneOnInit() {
        word = new Word("test", 0);
        assertThat(word.getTopic(), is(equalTo(-1)));
    }

    @Test
    public void testRawWordSetCorrectlyOnInit() {
        word = new Word("test", 0);
        assertThat(word.getRawWord(), is(equalTo("test")));
    }

    @Test
    public void testVocabularyIdSetCorrectlyOnInit() {
        word = new Word("test", 0);
        assertThat(word.getVocabularyId(), is(equalTo(0)));
    }

    @Test
    public void testSetAndGetTotalCountWorksCorrectly() {
        word = new Word("test", 0);
        word.setTotalCount(13);
        assertThat(word.getTotalCount(), is(equalTo(13)));
    }

    @Test
    public void testSetAndGetTopicWorksCorrectly() {
        word = new Word("test", 0);
        word.setTopic(13);
        assertThat(word.getTopic(), is(equalTo(13)));
    }
}
