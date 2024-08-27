package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.CLOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TreeNodeSet implements Comparable<TreeNodeSet>
{
    private final Set<TreeNode> nodeSet;

    public TreeNodeSet(Set<TreeNode> nodeSet)
    {
        if (nodeSet == null) this.nodeSet = new HashSet<>();
        else this.nodeSet = nodeSet;
    }

    public TreeNodeSet(TreeNode node) {
        nodeSet = Collections.singleton(node);
    }

    public Set<TreeNode> getNodeSet() {
        return nodeSet;
    }

    public String getCode()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<TreeNode> iter = nodeSet.iterator();

        while (iter.hasNext())
        {
            sb.append(iter.next().getCode());
            if (iter.hasNext()) sb.append(CLOptions.valueDelim);
        }

        return sb.toString();
    }

    public String getDesc()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<TreeNode> iter = nodeSet.iterator();

        while (iter.hasNext())
        {
            sb.append(iter.next().getDesc());
            if (iter.hasNext()) sb.append(CLOptions.valueDelim);
        }

        return sb.toString();
    }

    public TreeNodeSet getDefinedNodeSet()
    {
        Set<TreeNode> ret = new HashSet<>();
        for (TreeNode node : nodeSet) ret.add(node.getDefinedNode());
        return new TreeNodeSet(ret);
    }

    public String getMoveString()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<TreeNode> iter = nodeSet.iterator();

        while (iter.hasNext())
        {
            TreeNode endNode = iter.next();

            for (TreeNode pathNode : endNode.getPath())
            {
                int ply = pathNode.getPly();
                if (ply != 1) sb.append(" ");
                if ((ply & 1) == 1) sb.append(((ply + 1) / 2)).append(".");
                sb.append(pathNode.getMove());
            }

            if (iter.hasNext()) sb.append(CLOptions.valueDelim);
        }

        return sb.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<TreeNode> iter = nodeSet.iterator();

        while (iter.hasNext())
        {
            sb.append(iter.next().getCode());
            if (iter.hasNext()) sb.append(CLOptions.valueDelim);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;

        try
        {
            TreeNodeSet that = (TreeNodeSet)other;
            if (that.nodeSet.size() != nodeSet.size()) return false;
            Iterator<TreeNode> thatIter = that.nodeSet.iterator();

            for (TreeNode node : nodeSet) if (!node.equals(thatIter.next())) return false;

            return true;
        }

        catch (ClassCastException | NullPointerException e) { return false; }
    }

    @Override
    public int hashCode()
    {
        int ret = TreeNodeSet.class.hashCode();
        for (TreeNode node : nodeSet) ret ^= node.hashCode();
        return ret;
    }

    @Override
    public int compareTo(TreeNodeSet that)
    {
        if (that == null) return 1;
        Iterator<TreeNode> iter = that.nodeSet.iterator();

        for (TreeNode node : nodeSet)
        {
            if (!iter.hasNext()) return 1;
            int comp = node.compareTo(iter.next());
            if (comp != 0) return comp;
        }

        if (iter.hasNext()) return -1;
        return 0;
    }
}
