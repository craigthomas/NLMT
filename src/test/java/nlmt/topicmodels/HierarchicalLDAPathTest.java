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

import nlmt.datatypes.IdentifierObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test for the HierarchicalLDAPath class.
 */
public class HierarchicalLDAPathTest
{
    private HierarchicalLDANode mockRootNode;
    private HierarchicalLDANode mockChildNode1;
    private HierarchicalLDANode mockChildNode2;
    private HierarchicalLDAPath hierarchicalLDAPath;
    private IdentifierObjectMapper nodeMapper;

    @Before
    public void setUp() {
        mockRootNode = mock(HierarchicalLDANode.class);
        mockChildNode1 = mock(HierarchicalLDANode.class);
        mockChildNode2 = mock(HierarchicalLDANode.class);
        nodeMapper = new IdentifierObjectMapper<HierarchicalLDANode>();
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
    public void testClearRemovesAllNodes() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addNode(mockChildNode1);
        hierarchicalLDAPath.addNode(mockChildNode2);
        hierarchicalLDAPath.clear();
        HierarchicalLDANode [] expected = {null, null, null};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddWordAndDocumentThrowsExceptionOnNegativeLevel() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addWord(0, 0, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddWordAndDocumentThrowsExceptionOnLevelGreaterThanCurrentDepth() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addWord(0, 0, 1);
    }

    @Test
    public void testAddWordAndDocumentOnGoodPathWorksCorrectly() {
        HierarchicalLDANode rootNode = new HierarchicalLDANode(3, 1, new IdentifierObjectMapper<>());
        hierarchicalLDAPath = new HierarchicalLDAPath(rootNode, 3);
        hierarchicalLDAPath.addWord(0, 0, 0);
        Set<Integer> expected = new HashSet<>();
        expected.add(0);
        assertThat(rootNode.getTotalWordCount(), is(equalTo(1)));
        assertThat(rootNode.getWordCountForDocument(0, 0), is(equalTo(1)));
    }

    @Test
    public void testAddWordAndDocumentWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addNode(mockChildNode1);
        hierarchicalLDAPath.addNode(mockChildNode2);
        hierarchicalLDAPath.clear();
        HierarchicalLDANode [] expected = {null, null, null};
        assertThat(hierarchicalLDAPath.getNodes(), is(equalTo(expected)));
    }

    @Test
    public void testRemoveWordAndDocumentWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        HierarchicalLDANode node = new HierarchicalLDANode(4, 1, new IdentifierObjectMapper<>());
        hierarchicalLDAPath.addNode(node);
        node.addWord(0, 0);
        Set<Integer> wordsToRemove = new HashSet<>();
        wordsToRemove.add(0);
        hierarchicalLDAPath.removeDocument(0);
        assertThat(node.getDocumentsVisitingNode(), is(equalTo(new HashSet<>())));
        assertThat(node.getTotalWordCount(), is(0));
    }

    @Test
    public void testAddPathNoNodesClearsPath() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        List<Integer> newPath = new ArrayList<>();
        hierarchicalLDAPath.addPath(newPath, nodeMapper);
        HierarchicalLDANode [] nodes = hierarchicalLDAPath.getNodes();
        assertThat(nodes[0], is(nullValue()));
        assertThat(nodes[1], is(nullValue()));
        assertThat(nodes[2], is(nullValue()));
    }

    @Test
    public void testAddPathMaxDepthAddedWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        int root = nodeMapper.addObject(mockRootNode);
        int child1 = nodeMapper.addObject(mockChildNode1);
        int child2 = nodeMapper.addObject(mockChildNode2);
        List<Integer> newPath = new ArrayList<>();
        newPath.add(root);
        newPath.add(child1);
        newPath.add(child2);
        hierarchicalLDAPath.addPath(newPath, nodeMapper);
        HierarchicalLDANode [] nodes = hierarchicalLDAPath.getNodes();
        assertThat(nodes[0], is(mockRootNode));
        assertThat(nodes[1], is(mockChildNode1));
        assertThat(nodes[2], is(mockChildNode2));
    }

    @Test
    public void testAddPathPartialPathWorksCorrectly() {
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        int root = nodeMapper.addObject(mockRootNode);
        int child1 = nodeMapper.addObject(mockChildNode1);
        List<Integer> newPath = new ArrayList<>();
        newPath.add(root);
        newPath.add(child1);
        hierarchicalLDAPath.addPath(newPath, nodeMapper);
        HierarchicalLDANode [] nodes = hierarchicalLDAPath.getNodes();
        assertThat(nodes[0], is(mockRootNode));
        assertThat(nodes[1], is(mockChildNode1));
        assertThat(nodes[2], is(nullValue()));
    }

    @Test
    public void testAddPathWithSpawnNodeWorksCorrectly() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        mockChildNode1 = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        int root = nodeMapper.addObject(mockRootNode);
        int child1 = nodeMapper.addObject(mockChildNode1);
        List<Integer> newPath = new ArrayList<>();
        newPath.add(root);
        newPath.add(child1);
        newPath.add(-1);
        hierarchicalLDAPath.addPath(newPath, nodeMapper);
        HierarchicalLDANode [] nodes = hierarchicalLDAPath.getNodes();
        assertThat(nodes[0], is(mockRootNode));
        assertThat(nodes[1], is(mockChildNode1));
        assertThat(mockChildNode1.getNumChildren(), is(equalTo(1)));
        HierarchicalLDANode newNode = mockChildNode1.getChildren().get(0);
        assertThat(nodes[2], is(newNode));
    }

    @Test
    public void testAddDocumentWorksCorrectly() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        mockChildNode1 = new HierarchicalLDANode(3, 3, nodeMapper);
        hierarchicalLDAPath = new HierarchicalLDAPath(mockRootNode, 3);
        hierarchicalLDAPath.addNode(mockChildNode1);
        hierarchicalLDAPath.addDocument(0);
        assertThat(mockRootNode.getNumDocumentsVisitingNode(), is(equalTo(1)));
        assertThat(mockChildNode1.getNumDocumentsVisitingNode(), is(equalTo(1)));
        assertThat(mockRootNode.getDocumentsVisitingNode().contains(0), is(true));
        assertThat(mockChildNode1.getDocumentsVisitingNode().contains(0), is(true));
    }

    @Test
    public void testEnumerateNodesSingleRootNode() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        List<List<Integer>> expected = new ArrayList<>();
        List<Integer> onlyPath = new ArrayList<>();
        onlyPath.add(nodeMapper.getIndexFromObject(mockRootNode));
        onlyPath.add(-1);
        onlyPath.add(-1);
        expected.add(onlyPath);
        assertThat(HierarchicalLDAPath.enumeratePaths(mockRootNode, 3), is(equalTo(expected)));
    }

    @Test
    public void testEnumerateNodesTreeWithOneChildAtEachLevelWorksCorrectly() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        mockChildNode1 = mockRootNode.spawnChild();
        mockChildNode2 = mockChildNode1.spawnChild();
        List<List<Integer>> expected = new ArrayList<>();
        List<Integer> path1 = new ArrayList<>();
        path1.add(mockRootNode.getId());
        path1.add(mockChildNode1.getId());
        path1.add(mockChildNode2.getId());
        expected.add(path1);

        List<Integer> path2 = new ArrayList<>();
        path2.add(mockRootNode.getId());
        path2.add(mockChildNode1.getId());
        path2.add(-1);
        expected.add(path2);

        List<Integer> path3 = new ArrayList<>();
        path3.add(mockRootNode.getId());
        path3.add(-1);
        path3.add(-1);
        expected.add(path3);

        assertThat(HierarchicalLDAPath.enumeratePaths(mockRootNode, 3), is(equalTo(expected)));
    }

    @Test
    public void testEnumerateNodesTreeStopsAtFirstLevelWorksCorrectly() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        mockChildNode1 = mockRootNode.spawnChild();
        mockChildNode2 = mockRootNode.spawnChild();
        List<List<Integer>> expected = new ArrayList<>();
        List<Integer> path1 = new ArrayList<>();
        path1.add(mockRootNode.getId());
        path1.add(mockChildNode1.getId());
        path1.add(-1);
        expected.add(path1);

        List<Integer> path2 = new ArrayList<>();
        path2.add(mockRootNode.getId());
        path2.add(mockChildNode2.getId());
        path2.add(-1);
        expected.add(path2);

        List<Integer> path3 = new ArrayList<>();
        path3.add(mockRootNode.getId());
        path3.add(-1);
        path3.add(-1);
        expected.add(path3);

        assertThat(HierarchicalLDAPath.enumeratePaths(mockRootNode, 3), is(equalTo(expected)));
    }

    @Test
    public void testEnumerateNodesFullTreeCorrectly() {
        mockRootNode = new HierarchicalLDANode(3, 3, nodeMapper);
        mockChildNode1 = mockRootNode.spawnChild();
        HierarchicalLDANode child1Child1 = mockChildNode1.spawnChild();
        HierarchicalLDANode child1Child2 = mockChildNode1.spawnChild();
        mockChildNode2 = mockRootNode.spawnChild();
        HierarchicalLDANode child2Child1 = mockChildNode2.spawnChild();
        HierarchicalLDANode child2Child2 = mockChildNode2.spawnChild();
        List<List<Integer>> expected = new ArrayList<>();
        List<Integer> path1 = new ArrayList<>();
        path1.add(mockRootNode.getId());
        path1.add(mockChildNode1.getId());
        path1.add(child1Child1.getId());
        expected.add(path1);

        List<Integer> path2 = new ArrayList<>();
        path2.add(mockRootNode.getId());
        path2.add(mockChildNode1.getId());
        path2.add(child1Child2.getId());
        expected.add(path2);

        List<Integer> path3 = new ArrayList<>();
        path3.add(mockRootNode.getId());
        path3.add(mockChildNode1.getId());
        path3.add(-1);
        expected.add(path3);

        List<Integer> path4 = new ArrayList<>();
        path4.add(mockRootNode.getId());
        path4.add(mockChildNode2.getId());
        path4.add(child2Child1.getId());
        expected.add(path4);

        List<Integer> path5 = new ArrayList<>();
        path5.add(mockRootNode.getId());
        path5.add(mockChildNode2.getId());
        path5.add(child2Child2.getId());
        expected.add(path5);

        List<Integer> path6 = new ArrayList<>();
        path6.add(mockRootNode.getId());
        path6.add(mockChildNode2.getId());
        path6.add(-1);
        expected.add(path6);

        List<Integer> path7 = new ArrayList<>();
        path7.add(mockRootNode.getId());
        path7.add(-1);
        path7.add(-1);
        expected.add(path7);

        assertThat(HierarchicalLDAPath.enumeratePaths(mockRootNode, 3), is(equalTo(expected)));
    }
}
