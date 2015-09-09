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

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the IdentifierObjectMapper class.
 */
public class IdentifierObjectMapperTest {

    private IdentifierObjectMapper<String> mapper;

    @Before
    public void setUp() {
        mapper = new IdentifierObjectMapper<>();
    }

    @Test
    public void testMapperEmptyOnInit() {
        assertThat(mapper.size(), is(equalTo(0)));
    }

    @Test
    public void testGetIndexFromObjectReturnsNegativeOneOnEmptyMapper() {
        assertThat(mapper.getIndexFromObject("nothing"), is(equalTo(-1)));
    }

    @Test
    public void testGetObjectFromIndexReturnsNullOnEmptyMapper() {
        assertThat(mapper.getObjectFromIndex(1), is(nullValue()));
    }

    @Test
    public void testAddObjectToMapperWorksCorrectly() {
        mapper.addObject("test");
        assertThat(mapper.size(), is(equalTo(1)));
        assertThat(mapper.getIndexFromObject("test"), is(equalTo(0)));
        assertThat(mapper.getObjectFromIndex(0), is(equalTo("test")));
    }

    @Test
    public void testGetObjectNotInPopulatedMapperWorksCorrectly() {
        mapper.addObject("some");
        mapper.addObject("words");
        assertThat(mapper.size(), is(equalTo(2)));
        assertThat(mapper.getIndexFromObject("not"), is(equalTo(-1)));
        assertThat(mapper.getObjectFromIndex(2), is(nullValue()));
    }

    @Test
    public void testContainsInEmptyMapperWorksCorrectly() {
        assertThat(mapper.contains("not"), is(equalTo(false)));
    }

    @Test
    public void testContainsInPopulatedMapperWorksCorrectly() {
        mapper.addObject("some");
        mapper.addObject("words");
        assertThat(mapper.contains("some"), is(equalTo(true)));
        assertThat(mapper.contains("words"), is(equalTo(true)));
        assertThat(mapper.contains("not"), is(equalTo(false)));
    }

    @Test
    public void testContainsIndexInEmptyMapperWorksCorrectly() {
        assertThat(mapper.containsIndex(0), is(equalTo(false)));
    }

    @Test
    public void testContainsIndexInPopulatedMapperWorksCorrectly() {
        mapper.addObject("some");
        mapper.addObject("words");
        assertThat(mapper.containsIndex(0), is(equalTo(true)));
        assertThat(mapper.containsIndex(1), is(equalTo(true)));
        assertThat(mapper.containsIndex(2), is(equalTo(false)));
    }

    @Test
    public void testGetIndexKeysEmptyMapperWorksCorrectly() {
        Set<Integer> expected = new HashSet<>();
        assertThat(mapper.getIndexKeys(), is(equalTo(expected)));
    }

    @Test
    public void testGetIndexKeysPopulatedMapperWorksCorrectly() {
        mapper.addObject("some");
        mapper.addObject("words");
        Set<Integer> expected = new HashSet<>();
        expected.add(0);
        expected.add(1);
        assertThat(mapper.getIndexKeys(), is(equalTo(expected)));
    }

    @Test
    public void testContainsCaseSensitive() {
        mapper.addObject("some");
        mapper.addObject("words");
        assertThat(mapper.contains("SOME"), is(equalTo(false)));
        assertThat(mapper.contains("WORDS"), is(equalTo(false)));
    }

    @Test
    public void testGetObjectFromIndexReturnsNullOnNegativeIndex() {
        assertThat(mapper.getObjectFromIndex(-100), is(nullValue()));
    }

    @Test
    public void testAddObjectTwiceDoesNotAddTwice() {
        mapper.addObject("word");
        mapper.addObject("word");
        assertThat(mapper.size(), is(equalTo(1)));
        assertThat(mapper.getIndexFromObject("word"), is(equalTo(0)));
        assertThat(mapper.getObjectFromIndex(0), is(equalTo("word")));
        assertThat(mapper.getObjectFromIndex(1), is(nullValue()));
    }

    @Test
    public void testAddObjectReturnsIndex() {
        int index = mapper.addObject("word");
        assertThat(mapper.getIndexFromObject("word"), is(equalTo(0)));
        assertThat(index, is(equalTo(0)));
    }

    @Test
    public void testAddObjectTwiceReturnsSameIndex() {
        int index = mapper.addObject("word");
        assertThat(mapper.getIndexFromObject("word"), is(equalTo(0)));
        assertThat(index, is(equalTo(0)));
        index = mapper.addObject("word");
        assertThat(index, is(equalTo(0)));
    }

    @Test
    public void testDeleteIndexOnInvalidIndexDoesNothing() {
        int index = mapper.addObject("word");
        mapper.deleteIndex(-1);
        mapper.deleteIndex(2);
        assertThat(mapper.contains("word"), is(true));
        assertThat(mapper.containsIndex(index), is(true));
    }

    @Test
    public void testDeleteIndexWorksCorrectly() {
        int index1 = mapper.addObject("word");
        int index2 = mapper.addObject("that");
        int index3 = mapper.addObject("another");
        mapper.deleteIndex(index2);
        assertThat(mapper.contains("word"), is(true));
        assertThat(mapper.contains("that"), is(false));
        assertThat(mapper.contains("another"), is(true));
        assertThat(mapper.containsIndex(index1), is(true));
        assertThat(mapper.containsIndex(index2), is(false));
        assertThat(mapper.containsIndex(index3), is(true));
    }
}
