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

package nlmt.topicmodels;

import nlmt.datatypes.IdentifierObjectMapper;
import nlmt.probfunctions.PMFSampler;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for the HierarchicalLDANode class.
 */
public class HierarchicalLDANodeTest
{
    private IdentifierObjectMapper<HierarchicalLDANode> nodeMapper;
    private HierarchicalLDANode hierarchicalLDANode;

    @Before
    public void setUp() {
        nodeMapper = new IdentifierObjectMapper<>();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidGammaThrowsException() {
        new HierarchicalLDANode(-0.1, 3, nodeMapper);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidNumberOfDocumentsThrowsException() {
        new HierarchicalLDANode(1.0, -2, nodeMapper);
    }

    @Test
    public void testNewNodeAddedToNodeMapper() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        assertThat(nodeMapper.contains(hierarchicalLDANode), is(true));
        assertThat(nodeMapper.getIndexFromObject(hierarchicalLDANode), is(equalTo(hierarchicalLDANode.getId())));
        assertThat(nodeMapper.getObjectFromIndex(hierarchicalLDANode.getId()), is(equalTo(hierarchicalLDANode)));
    }

    @Test
    public void testAlternateConstructorResultsInRootNode() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(nullValue()));
        assertThat(hierarchicalLDANode.isRoot(), is(true));
    }

    @Test
    public void testCreationWithParentIsNotRootNode() {
        HierarchicalLDANode root = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode = new HierarchicalLDANode(root, 1.0, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getParent(), is(root));
        assertThat(hierarchicalLDANode.isRoot(), is(false));
    }

    @Test
    public void testVisitedEmptyOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        assertThat(hierarchicalLDANode.documentsVisitingNode.isEmpty(), is(true));
    }

    @Test
    public void testSetVisitedWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(4);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
    }

    @Test
    public void testSetVisitedDoubleSetSameDocumentWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(0);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
    }

    @Test
    public void testRemoveVisitedRemoveSingleWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(4);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
    }

    @Test
    public void testRemoveVisitedRemoveItemNotInSetWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(9);
        Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(0);
        expectedSet.add(4);
        expectedSet.add(2);
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
    }

    @Test
    public void testRemoveVisitedRemoveAllWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(1.0, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(4);
        hierarchicalLDANode.setVisited(2);
        hierarchicalLDANode.removeVisited(2);
        hierarchicalLDANode.removeVisited(4);
        hierarchicalLDANode.removeVisited(0);
        Set<Integer> expectedSet = new HashSet<>();
        assertThat(hierarchicalLDANode.documentsVisitingNode, is(equalTo(expectedSet)));
    }

    @Test
    public void testGetEmptyPopularityWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getEmptyPopularity(), is(equalTo(0.2)));
    }

    @Test
    public void testGetPopularityZeroDocumentsVisitedWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        assertThat(hierarchicalLDANode.getPopularity(), is(equalTo(0.0)));
    }

    @Test
    public void testGetPopularityTwoDocumentsVisitedWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        hierarchicalLDANode.setVisited(0);
        hierarchicalLDANode.setVisited(2);
        assertThat(hierarchicalLDANode.getPopularity(), is(equalTo(0.8)));
    }

    @Test
    public void testNodeHasNoChildrenOnInit() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        List<HierarchicalLDANode> expected = new ArrayList<>();
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(expected)));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(0)));
    }

    @Test
    public void testSpawnCorrectlyAddsChildToChildren() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        HierarchicalLDANode child = hierarchicalLDANode.spawnChild();
        assertThat(hierarchicalLDANode.getChildren().contains(child), is(true));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(1)));
    }

    @Test
    public void testSpawnMultipleChildrenWorksCorrectly() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode [] expected = {child1, child2, child3};
        assertThat(hierarchicalLDANode.getChildren(), is(equalTo(Arrays.asList(expected))));
        assertThat(hierarchicalLDANode.getNumChildren(), is(equalTo(3)));
    }

    @Test
    public void testGetPopularitiesNoChildrenReturnsEmptyPopularity() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        PMFSampler sampler = hierarchicalLDANode.getChildPopularities();
        double [] expected = {1.0};
        assertThat(sampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testGetPopularitiesMultipleChildrenNoneVisited() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild();
        PMFSampler sampler = hierarchicalLDANode.getChildPopularities();
        double [] expected = {0.0, 0.0, 0.0, 1.0};
        assertThat(sampler.getProbabilities(), is(equalTo(expected)));
    }

    @Test
    public void testGetPopularitiesWithChildrenVisited() {
        hierarchicalLDANode = new HierarchicalLDANode(0.5, 3, nodeMapper);
        HierarchicalLDANode child1 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child2 = hierarchicalLDANode.spawnChild();
        HierarchicalLDANode child3 = hierarchicalLDANode.spawnChild();

        child1.setVisited(0);
        child1.setVisited(1);
        child1.setVisited(2);

        child2.setVisited(0);

        child3.setVisited(0);
        child3.setVisited(1);
        PMFSampler sampler = hierarchicalLDANode.getChildPopularities();
        double [] expected = {0.46153846153846145, 0.15384615384615385, 0.3076923076923077, 0.07692307692307697};
        double [] actual = sampler.getProbabilities();
        assertThat(actual, is(expected));
    }
}
