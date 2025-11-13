/*
 * The MIT License
 *
 * Copyright (c) 2025 Mark Chen <chen@dotfx.com>.
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

package com.dotfx.pgnutil;

import com.dotfx.pgnutil.eco.EcoTree;
import com.dotfx.pgnutil.eco.TreeNode;
import com.dotfx.pgnutil.eco.TreeNodeSet;

public class OpeningStats extends AggregateScore
{
    private final String openingSt;
    private final MoveListId oid;
    private final TreeNode eco;
    private final TreeNode scidEco;
    private final TreeNodeSet xEcoSet;
    private final TreeNodeSet xScidEcoSet;

    OpeningStats(String openingSt, MoveListId oid, TreeNode eco, TreeNode scidEco, TreeNodeSet xEcoSet,
                 TreeNodeSet xScidEcoSet)
    {
        super();
        this.openingSt = openingSt;
        this.oid = oid;
        this.eco = eco;
        this.scidEco = scidEco;
        this.xEcoSet = xEcoSet;
        this.xScidEcoSet = xScidEcoSet;
    }

    public String getOpeningSt()
    {
        return openingSt;
    }

    public MoveListId getId()
    {
        return oid;
    }

    public TreeNode getEco(EcoTree.FileType type)
    {
        if (type == EcoTree.FileType.STD) return eco;
        if (type == EcoTree.FileType.SCIDDB) return scidEco;
        return null;
    }

    public TreeNodeSet getXEcoSet(EcoTree.FileType type)
    {
        if (type == EcoTree.FileType.STD) return xEcoSet;
        if (type == EcoTree.FileType.SCIDDB) return xScidEcoSet;
        return null;
    }
}
