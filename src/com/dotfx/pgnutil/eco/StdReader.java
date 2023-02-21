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

final class StdReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new StdReader(); }
    }

    public static final String LINE_DELIM="|";
    public static final String TUPLE_DELIM = ";";
    public static final String FIELD_DELIM = ",";

    /**
     * Reads processed files with stored FEN strings.
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
        String tupleDelim = Pattern.quote(TUPLE_DELIM);
        String fieldDelim = Pattern.quote(FIELD_DELIM);

        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
        {
            while ((line = dbReader.readLine()) != null)
            {
                TreeNode node = topNode;
                String parts[] = line.split(lineDelim);
                String tuples[] = parts[2].split(tupleDelim);

                for (int i = 0; i < tuples.length; i++) // for each move+position hash
                {
                    // field[0] is the move; field[1] is the position hash
                    String fields[] = tuples[i].split(fieldDelim);

                    if (i == tuples.length - 1) node = node.addNode(fields[0], parts[0], parts[1], this);
                    else node = node.addNode(fields[0], "", "", this);

                    if (fields.length > 1) // has position hash
                    {
                        Set<TreeNode> nodeSet = positionMap.get(fields[1]);

                        if (nodeSet == null)
                        {
                            nodeSet = new HashSet<>();
                            positionMap.put(fields[1], nodeSet);
                        }

                        nodeSet.add(node);
                    }
                }
            }
        }
    }

    @Override
    void handleNewNode(TreeNode node)
    {
        if (node.getPly() > deepestPly) deepestPly = node.getPly();
    }
}
