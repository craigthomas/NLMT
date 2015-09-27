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
package nlmt.probfunctions;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the PMFSampler class.
 */
public class PMFSamplerTest {

    private PMFSampler pmfSampler;

    @Test(expected=IllegalArgumentException.class)
    public void testInitWithSizeLessThanOne() {
        pmfSampler = new PMFSampler(0);
    }

    @Test
    public void testSizeSetCorrectlyOnInit() {
        pmfSampler = new PMFSampler(2);
        assertThat(pmfSampler.getSize(), is(equalTo(2)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddingNegativeWeightThrowsException() {
        pmfSampler = new PMFSampler(2);
        pmfSampler.add(-1.0);
    }

    @Test
    public void testAddingSamplesGreaterThanMaxSizeDoesNotCauseException() {
        pmfSampler = new PMFSampler(2);
        pmfSampler.add(1.0);
        pmfSampler.add(1.0);
        pmfSampler.add(1.0);
        assertThat(pmfSampler.getSize(), is(equalTo(2)));
    }

    @Test
    public void testAllWeightsZeroExceptFirstPositionReturnsFirst() {
        pmfSampler = new PMFSampler(3);
        pmfSampler.add(1.0);
        pmfSampler.add(0.0);
        pmfSampler.add(0.0);
        assertThat(pmfSampler.sample(), is(equalTo(0)));
    }

    @Test
    public void testAllWeightsZeroExceptSecondPositionReturnsSecond() {
        pmfSampler = new PMFSampler(3);
        pmfSampler.add(0.0);
        pmfSampler.add(1.0);
        pmfSampler.add(0.0);
        assertThat(pmfSampler.sample(), is(equalTo(1)));
    }

    @Test
    public void testAllWeightsZeroExceptThirdPositionReturnsThird() {
        pmfSampler = new PMFSampler(3);
        pmfSampler.add(0.0);
        pmfSampler.add(0.0);
        pmfSampler.add(1.0);
        assertThat(pmfSampler.sample(), is(equalTo(2)));
    }

    @Test
    public void testAllWeightsZeroWillReturnRandomResult() {
        pmfSampler = new PMFSampler(3);
        pmfSampler.add(0.0);
        pmfSampler.add(0.0);
        pmfSampler.add(0.0);

        int [] results = new int[10];
        for (int i = 0; i < 10; i++) {
            results[i] = pmfSampler.sample();
        }
        int [] badResult = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertThat(results, is(not(equalTo(badResult))));
    }

    @Test
    public void testGetProbabilities() {
        pmfSampler = new PMFSampler(5);
        pmfSampler.add(1.3);
        pmfSampler.add(3.1);
        pmfSampler.add(1.1);
        pmfSampler.add(2.2);
        pmfSampler.add(0.9);

        double [] expected = {
                0.15116279069767444,
                0.36046511627906985,
                0.12790697674418602,
                0.2558139534883721,
                0.10465116279069761};
        assertThat(pmfSampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testNormalizeLogLikelihoodsWithSingleValue() {
        double [] likelihoods = {0.1};
        pmfSampler = PMFSampler.normalizeLogLikelihoods(likelihoods);
        double [] expected = {1.0};
        assertThat(pmfSampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testNormalizeLogLikelihoodsWithTwoValues() {
        double [] likelihoods = {0.1, 0.1};
        pmfSampler = PMFSampler.normalizeLogLikelihoods(likelihoods);
        double [] expected = {0.5, 0.5};
        assertThat(pmfSampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testNormalizeLogLikelihoodsNegativeValues() {
        double [] likelihoods = {-0.1, -0.1};
        pmfSampler = PMFSampler.normalizeLogLikelihoods(likelihoods);
        double [] expected = {0.5, 0.5};
        assertThat(pmfSampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testNormalizeLogLikelihoodsNegativePositiveValues() {
        double [] likelihoods = {1000.0, -0.00001};
        pmfSampler = PMFSampler.normalizeLogLikelihoods(likelihoods);
        double [] expected = {1.0, 0.0};
        assertThat(pmfSampler.getProbabilities(), is(equalTo(expected)));
    }

}
