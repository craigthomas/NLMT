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
package nlmt.samplers;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the RandomSampler class.
 */
public class RandomSamplerTest {

    private RandomSampler randomSampler;

    @Test(expected=IllegalArgumentException.class)
    public void testInitWithSizeLessThanOne() {
        randomSampler = new RandomSampler(0);
    }

    @Test
    public void testSizeSetCorrectlyOnInit() {
        randomSampler = new RandomSampler(2);
        assertThat(randomSampler.getSize(), is(equalTo(2)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddingNegativeWeightThrowsException() {
        randomSampler = new RandomSampler(2);
        randomSampler.addSample(-1.0);
    }

    @Test
    public void testAddingSamplesGreaterThanMaxSizeDoesNotCauseException() {
        randomSampler = new RandomSampler(2);
        randomSampler.addSample(1.0);
        randomSampler.addSample(1.0);
        randomSampler.addSample(1.0);
        assertThat(randomSampler.getSize(), is(equalTo(2)));
    }

    @Test
    public void testAllWeightsZeroExceptFirstPositionReturnsFirst() {
        randomSampler = new RandomSampler(3);
        randomSampler.addSample(1.0);
        randomSampler.addSample(0.0);
        randomSampler.addSample(0.0);
        assertThat(randomSampler.sample(), is(equalTo(0)));
    }

    @Test
    public void testAllWeightsZeroExceptSecondPositionReturnsSecond() {
        randomSampler = new RandomSampler(3);
        randomSampler.addSample(0.0);
        randomSampler.addSample(1.0);
        randomSampler.addSample(0.0);
        assertThat(randomSampler.sample(), is(equalTo(1)));
    }

    @Test
    public void testAllWeightsZeroExceptThirdPositionReturnsThird() {
        randomSampler = new RandomSampler(3);
        randomSampler.addSample(0.0);
        randomSampler.addSample(0.0);
        randomSampler.addSample(1.0);
        assertThat(randomSampler.sample(), is(equalTo(2)));
    }

    @Test
    public void testAllWeightsZeroWillReturnRandomResult() {
        randomSampler = new RandomSampler(3);
        randomSampler.addSample(0.0);
        randomSampler.addSample(0.0);
        randomSampler.addSample(0.0);

        int [] results = new int[10];
        for (int i = 0; i < 10; i++) {
            results[i] = randomSampler.sample();
        }
        int [] badResult = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertThat(results, is(not(equalTo(badResult))));
    }
}
