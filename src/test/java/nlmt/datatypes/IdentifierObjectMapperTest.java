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
}