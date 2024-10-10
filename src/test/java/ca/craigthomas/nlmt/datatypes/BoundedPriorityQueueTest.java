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
package ca.craigthomas.nlmt.datatypes;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Tests for the BoundedPriorityQueue class.
 */
public class BoundedPriorityQueueTest {

    private BoundedPriorityQueue<Integer> boundedPriorityQueue;

    @Test
    public void testQueueEmptyOnInit() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        assertThat(boundedPriorityQueue.getElements().isEmpty(), is(true));
        assertThat(boundedPriorityQueue.getPriorities().isEmpty(), is(true));
    }

    @Test
    public void testQueueNotEqualToNull() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        assertThat(boundedPriorityQueue.equals(null), is(false));
    }

    @Test
    public void testEmptyQueuesSameSizeAreEqual() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        BoundedPriorityQueue<Integer> boundedPriorityQueue1 = new BoundedPriorityQueue<>(3);
        assertThat(boundedPriorityQueue.equals(boundedPriorityQueue1), is(true));
        assertThat(boundedPriorityQueue.hashCode() == boundedPriorityQueue1.hashCode(), is(true));
    }

    @Test
    public void testEmptyQueuesDifferentSizeAreNotEqual() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        BoundedPriorityQueue<Integer> boundedPriorityQueue1 = new BoundedPriorityQueue<>(2);
        assertThat(boundedPriorityQueue.equals(boundedPriorityQueue1), is(false));
        assertThat(boundedPriorityQueue.hashCode() == boundedPriorityQueue1.hashCode(), is(false));
    }

    @Test
    public void testQueuesSameElementsDifferentPrioritiesAreNotEqual() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        BoundedPriorityQueue<Integer> boundedPriorityQueue1 = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(1, 0);
        boundedPriorityQueue1.add(2, 0);
        assertThat(boundedPriorityQueue.equals(boundedPriorityQueue1), is(false));
        assertThat(boundedPriorityQueue.hashCode() == boundedPriorityQueue1.hashCode(), is(false));
    }

    @Test
    public void testQueuesDifferentElementsSamePrioritiesAreNotEqual() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        BoundedPriorityQueue<Integer> boundedPriorityQueue1 = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(1, 0);
        boundedPriorityQueue1.add(1, 1);
        assertThat(boundedPriorityQueue.equals(boundedPriorityQueue1), is(false));
        assertThat(boundedPriorityQueue.hashCode() == boundedPriorityQueue1.hashCode(), is(false));
    }

    @Test
    public void testQueuesSameElementsSamePrioritiesAreEqual() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        BoundedPriorityQueue<Integer> boundedPriorityQueue1 = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(1, 0);
        boundedPriorityQueue1.add(1, 0);
        assertThat(boundedPriorityQueue.equals(boundedPriorityQueue1), is(true));
        assertThat(boundedPriorityQueue.hashCode() == boundedPriorityQueue1.hashCode(), is(true));
    }

    @Test
    public void testAddToEmptyQueueWorksCorrectly() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(1, 1000);

        List<Integer> expectedIds = new ArrayList<>();
        expectedIds.add(1000);

        List<Integer> expectedPriorities = new ArrayList<>();
        expectedPriorities.add(1);

        assertThat(boundedPriorityQueue.getElements(), is(equalTo(expectedIds)));
        assertThat(boundedPriorityQueue.getPriorities(), is(equalTo(expectedPriorities)));
    }

    @Test
    public void testAddToQueueAddsCorrectOrder() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(4);
        boundedPriorityQueue.add(10, 1000);
        boundedPriorityQueue.add(30, 3000);
        boundedPriorityQueue.add(20, 2000);

        List<Integer> expectedIds = new ArrayList<>();
        expectedIds.add(3000);
        expectedIds.add(2000);
        expectedIds.add(1000);

        List<Integer> expectedPriorities = new ArrayList<>();
        expectedPriorities.add(30);
        expectedPriorities.add(20);
        expectedPriorities.add(10);

        assertThat(boundedPriorityQueue.getElements(), is(equalTo(expectedIds)));
        assertThat(boundedPriorityQueue.getPriorities(), is(equalTo(expectedPriorities)));
    }

    @Test
    public void testAddToQueueMoreThanSizeDropsOutLowest() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(8, 8000);
        boundedPriorityQueue.add(10, 1000);
        boundedPriorityQueue.add(7, 7000);
        boundedPriorityQueue.add(30, 3000);
        boundedPriorityQueue.add(6, 6000);
        boundedPriorityQueue.add(20, 2000);

        List<Integer> expectedIds = new ArrayList<>();
        expectedIds.add(3000);
        expectedIds.add(2000);
        expectedIds.add(1000);

        List<Integer> expectedPriorities = new ArrayList<>();
        expectedPriorities.add(30);
        expectedPriorities.add(20);
        expectedPriorities.add(10);

        assertThat(boundedPriorityQueue.getElements(), is(equalTo(expectedIds)));
        assertThat(boundedPriorityQueue.getPriorities(), is(equalTo(expectedPriorities)));
    }

    @Test
    public void testAddToQueueEqualPrioritiesAddsToFront() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(10, 8000);
        boundedPriorityQueue.add(10, 1000);
        boundedPriorityQueue.add(10, 7000);
        boundedPriorityQueue.add(10, 3000);

        List<Integer> expectedIds = new ArrayList<>();
        expectedIds.add(3000);
        expectedIds.add(7000);
        expectedIds.add(1000);

        List<Integer> expectedPriorities = new ArrayList<>();
        expectedPriorities.add(10);
        expectedPriorities.add(10);
        expectedPriorities.add(10);

        assertThat(boundedPriorityQueue.getElements(), is(equalTo(expectedIds)));
        assertThat(boundedPriorityQueue.getPriorities(), is(equalTo(expectedPriorities)));
    }

    @Test
    public void testAddSmallerPrioritiesAddsToEndOfList() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(10, 10000);
        boundedPriorityQueue.add(9, 9000);
        boundedPriorityQueue.add(8, 8000);

        List<Integer> expectedIds = new ArrayList<>();
        expectedIds.add(10000);
        expectedIds.add(9000);
        expectedIds.add(8000);

        List<Integer> expectedPriorities = new ArrayList<>();
        expectedPriorities.add(10);
        expectedPriorities.add(9);
        expectedPriorities.add(8);

        assertThat(boundedPriorityQueue.getElements(), is(equalTo(expectedIds)));
        assertThat(boundedPriorityQueue.getPriorities(), is(equalTo(expectedPriorities)));
    }

    @Test
    public void testSerializationRoundTrip() {
        boundedPriorityQueue = new BoundedPriorityQueue<>(3);
        boundedPriorityQueue.add(10, 10000);
        boundedPriorityQueue.add(9, 9000);
        boundedPriorityQueue.add(8, 8000);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(boundedPriorityQueue);
            byte[] serializedObjectArray = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            assertThat(serializedObjectArray.length, is(not(CoreMatchers.equalTo(0))));

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObjectArray);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            BoundedPriorityQueue<Integer> deserializedBoundedPriorityQueue = (BoundedPriorityQueue<Integer>) objectInputStream.readObject();
            assertThat(boundedPriorityQueue.equals(deserializedBoundedPriorityQueue), is(true));
        } catch (IOException e) {
            assertFalse("IOException occurred: " + e.getMessage(), true);
        } catch (ClassNotFoundException e) {
            assertFalse("ClassNotFoundException occurred: " + e.getMessage(), true);
        }
    }

}
