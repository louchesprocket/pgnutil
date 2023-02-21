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

    void handleNewNode(TreeNode node) throws IllegalMoveException
    {
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
        if (node.getPly() > deepestPly) deepestPly = node.getPly();
    }
}
