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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the HierarchicalLDAPath class.
 */
public class HierarchicalLDAPathTest
{
    private HierarchicalLDANode mockRootNode;
    private HierarchicalLDANode mockChildNode1;
    private HierarchicalLDANode mockChildNode2;
    private HierarchicalLDAPath hierarchicalLDAPath;

    @Before
    public void setUp() {
        mockRootNode = mock(HierarchicalLDANode.class);
        mockChildNode1 = mock(HierarchicalLDANode.class);
        mockChildNode2 = mock(HierarchicalLDANode.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMaxDepthLessThanOneThrowsException() {
        new HierarchicalLDAPath(mockRootNode, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullRootNodeThrowsException() {
        new HierarchicalLDAPath(null, 2);
    }

    @Test
    public void testGetCurrentNodeReturnsRootNodeOnInit() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 2);
        assertThat(hierarchicalLDAPath.getCurrentNode(), is(equalTo(mockRootNode)));
    }

    @Test
    public void testGetCurrentNodeAfterNodeAddedWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 2);
        hierarchicalLDAPath.addNode(mockChildNode1);
        assertThat(hierarchicalLDAPath.getCurrentNode(), is(equalTo(mockChildNode1)));
    }

    @Test
    public void testAddNodeAddsToListOfNodesCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 2);
        hierarchicalLDAPath.addNode(mockChildNode1);
        HierarchicalLDANode [] expected = {mockRootNode, mockChildNode1};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }

    @Test
    public void testAddNodeBeyondMaxDepthDoesNothing() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 2);
        hierarchicalLDAPath.addNode(mockChildNode1);
        hierarchicalLDAPath.addNode(mockChildNode2);
        HierarchicalLDANode [] expected = {mockRootNode, mockChildNode1};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }

    @Test
    public void testAtMaxDepthSingleNodeWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 1);
        assertThat(hierarchicalLDAPath.atMaxDepth(), is(true));
    }

    @Test
    public void testAtMaxDepthMultipleNodes() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        assertThat(hierarchicalLDAPath.atMaxDepth(), is(false));
        hierarchicalLDAPath.addNode(mockChildNode1);
        assertThat(hierarchicalLDAPath.atMaxDepth(), is(false));
        hierarchicalLDAPath.addNode(mockChildNode2);
        assertThat(hierarchicalLDAPath.atMaxDepth(), is(true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNodeNegativeLevelThrowsException() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 1);
        hierarchicalLDAPath.getNode(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetNodeGreaterThanMaxDepthThrowsException() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.getNode(4);
    }

    @Test
    public void testGetNodeNotYetEnteredReturnsNull() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        assertThat(hierarchicalLDAPath.getNode(2), is(nullValue()));
    }

    @Test
    public void testGetNodeWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        HierarchicalLDANode [] expected = {mockRootNode, null, null};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
        hierarchicalLDAPath.addNode(mockChildNode1);
        expected = new HierarchicalLDANode[]{mockRootNode, mockChildNode1, null};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
        hierarchicalLDAPath.addNode(mockChildNode2);
        expected = new HierarchicalLDANode[]{mockRootNode, mockChildNode1, mockChildNode2};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }

    @Test
    public void testClearRemovesAllButRootNode() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addNode(mockChildNode1);
        hierarchicalLDAPath.addNode(mockChildNode2);
        hierarchicalLDAPath.clear();
        HierarchicalLDANode [] expected = {mockRootNode, null, null};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }
}
