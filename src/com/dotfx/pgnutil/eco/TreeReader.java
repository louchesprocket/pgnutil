/*
 * The MIT License
 *
 * Copyright (c) 2023 Mark Chen <chen@dotfx.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED
 * "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.Board;
import com.dotfx.pgnutil.IllegalMoveException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class TreeReader
{
    final Map<String,Set<TreeNode>> positionMap;
    int deepestPly;

    TreeReader() { positionMap = new HashMap<>(5000); }

    Map<String,Set<TreeNode>> getPositionMap() { return positionMap; }
    int getDeepestPly() { return deepestPly; }

    abstract void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException;

    /**
     * Callback from TreeNode.addNode(). Adds position hash to maps. This is not needed with the StdReader because the
     * input file contains hashes.
     *
     * @param node
     * @throws IllegalMoveException
     */
    void handleNewNode(TreeNode node) throws IllegalMoveException
    {
        if (node.getSpecCode().length() == 0 && node.getSpecDesc().length() == 0) return;

        Board board = new Board(true);
        board.move(node.getPath());
        String posSt = board.positionId().toString();
        Set<TreeNode> nodeSet = positionMap.get(posSt);

        if (nodeSet == null)
        {
            nodeSet = new HashSet<>();
            positionMap.put(posSt, nodeSet);
        }

        nodeSet.add(node);
        node.setPositionId(posSt);
        if (node.getPly() > deepestPly) deepestPly = node.getPly();
    }
}
