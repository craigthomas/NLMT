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
package nlmt.models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        assertThat(ldaModel.alpha, is(equalTo(25.0)));
    }

    @Test
    public void testBetaSetToZeroAutoSetsBeta() {
        ldaModel = new LDAModel(2, 0, 0);
        assertThat(ldaModel.beta, is(equalTo(0.1)));
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
}
