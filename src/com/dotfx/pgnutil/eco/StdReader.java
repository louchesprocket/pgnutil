/*
 * The MIT License
 *
 * Copyright 2019 Mark Chen <chen@dotfx.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.IllegalMoveException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads from standard database without computing position hashes (because hashes are stored).
 *
 */
final class StdReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new StdReader(); }
    }

    public static final String LINE_DELIM="|";
    public static final String MOVE_DELIM = " ";

    /**
     * Reads processed files with stored position hashes.
     *
     * @param in
     * @param topNode
     * @throws IOException
     * @throws IllegalMoveException
     */
    @Override
    void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException
    {
        String line;
        String lineDelim = Pattern.quote(LINE_DELIM);
        String moveDelim = Pattern.quote(MOVE_DELIM);

        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
        {
            while ((line = dbReader.readLine()) != null)
            {
                TreeNode node = topNode;
                String parts[] = line.split(lineDelim);
                String moves[] = parts[2].split(moveDelim);

                for (int i = 0; i < moves.length; i++) // for each move + position hash
                {
                    if (i == moves.length - 1) node = node.addNode(moves[i], parts[0], parts[1], this);
                    else node = node.addNode(moves[i], "", "", this);
                }

                Set<TreeNode> nodeSet = positionMap.get(parts[3]);

                if (nodeSet == null)
                {
                    nodeSet = new HashSet<>();
                    positionMap.put(parts[3], nodeSet);
                }

                nodeSet.add(node);
                node.setPositionId(parts[3]);
            }
        }
    }

    @Override
    void handleNewNode(TreeNode node)
    {
        if (node.getPly() > deepestPly) deepestPly = node.getPly();
    }
}
