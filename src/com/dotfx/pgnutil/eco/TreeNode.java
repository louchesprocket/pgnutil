package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.IllegalMoveException;
import com.dotfx.pgnutil.PgnGame;

import java.util.*;

/**
 * The entire tree of these things must be immutable (hash codes depend on it).
 */
public final class TreeNode implements Comparable<TreeNode>
{
    private final int ply;
    private final String moveSt;
    private String code;
    private String desc;
    private final TreeNode parent;
    private final Map<String, TreeNode> branchMap; // children indexed by move text

    /**
     * @param parent null for top tier nodes
     * @param ply
     * @param moveSt
     * @param code
     * @param desc
     */
    TreeNode(TreeNode parent, int ply, String moveSt, String code, String desc)
    {
        this.parent = parent;
        this.ply = ply;
        this.moveSt = moveSt;
        this.code = code == null ? "" : code;
        this.desc = desc == null ? "" : desc;
        branchMap = new HashMap<>();
    }

    public int getPly() {
        return ply;
    }
    public String getMove() {
        return moveSt;
    }
    public String getSpecCode() {
        return code;
    }
    public void setSpecCode(String code) {
        this.code = code == null ? "" : code;
    }
    public String getSpecDesc() {
        return desc;
    }
    private void setSpecDesc(String desc) {
        this.desc = desc == null ? "" : desc;
    }
    private TreeNode branchTo(String moveTxt) {
        return branchMap.get(moveTxt);
    }
    public TreeNode getParent() {
        return parent;
    }
    public String getMoveText() {
        return PgnGame.Move.getMoveText(moveSt);
    }
    public TreeNode branchTo(TreeNode node) {
        return branchTo(node.getMove());
    }
    public TreeNode branchTo(PgnGame.Move move) {
        return branchTo(move.getMoveText());
    }

    public List<TreeNode> getChildren()
    {
        List<TreeNode> ret = new ArrayList<>();
        for (String s : branchMap.keySet()) ret.add(branchMap.get(s));
        return ret;
    }

    public String getCode()
    {
        TreeNode node = this;
        while (node != null && node.code.length() == 0) node = node.parent;
        return node == null ? "" : node.code;
    }

    public String getStdCode()
    {
        String ret = getCode();
        if (ret.length() >= 3) return ret.substring(0, 3);
        return ret;
    }

    public String getDesc()
    {
        TreeNode node = this;
        while (node != null && node.desc.length() == 0) node = node.parent;
        return node == null ? "" : node.desc;
    }

    public boolean isBottom() {
        return branchMap.isEmpty();
    }

    public TreeNode getBottomNode(PgnGame game)
    {
        TreeNode ret = this;
        TreeNode next = ret;
        List<PgnGame.Move> moveList = game.getMoves();

        for (int i = 0; i < moveList.size(); i++)
        {
            next = next.branchTo(moveList.get(i).getMoveText());
            if (next == null) break;
            ret = next;
        }

        return ret;
    }

    public TreeNode getBottomNode(PgnGame game, int maxDepth)
    {
        TreeNode ret = this;
        TreeNode next = ret;
        List<PgnGame.Move> moveList = game.getMoves();

        for (int i = 0; i < moveList.size() && i < maxDepth; i++)
        {
            next = next.branchTo(moveList.get(i).getMoveText());
            if (next == null) break;
            ret = next;
        }

        return ret;
    }

    public TreeNode getDefinedNode()
    {
        TreeNode node = this;
        while (node.code.length() == 0 && node.desc.length() == 0) node = node.getParent();
        return node;
    }

    public TreeNode getBottomDefinedNode(PgnGame game)
    {
        TreeNode ret = this;
        TreeNode next = ret;
        List<PgnGame.Move> moveList = game.getMoves();

        for (int i = 0; i < moveList.size(); i++)
        {
            next = next.branchTo(moveList.get(i).getMoveText());
            if (next == null) break;
            ret = next;
        }

        while (ret.code.length() == 0 && ret.desc.length() == 0) ret = ret.getParent();
        return ret;
    }

    /**
     * @param moveSt example: "1. Nf3 c5 2. g3 Nc6"
     * @return
     */
    public TreeNode getBottomNode(String moveSt)
    {
        List<String> moveList = PgnGame.parseMoveString(moveSt);
        TreeNode ret = this;
        TreeNode next = ret;

        for (int i = 0; i < moveList.size(); i++)
        {
            next = next.branchTo(moveList.get(i));
            if (next == null) break;
            ret = next;
        }

        return ret;
    }

    public TreeNode getBottomNode(List<TreeNode> path)
    {
        TreeNode ret = this;
        TreeNode next = ret;

        for (int i = 0; i < path.size(); i++)
        {
            next = next.branchTo(path.get(i).getMoveText());
            if (next == null) break;
            ret = next;
        }

        return ret;
    }

    public List<TreeNode> getPath()
    {
        List<TreeNode> ret = new ArrayList<>();
        TreeNode next = this;

        while (next.getParent() != null)
        {
            ret.add(0, next);
            next = next.getParent();
        }

        return ret;
    }

    /**
     * @return example: "e4 e5 Nf3"
     */
    public String getPathString()
    {
        StringBuilder sb = new StringBuilder();
        TreeNode next = this;

        while (next.getParent() != null)
        {
            sb.insert(0, " ").insert(0, next.getMoveText());
            next = next.getParent();
        }

        return sb.toString().trim();
    }

    /**
     * Adds a path below this node. Assumes no stored FEN.
     *
     * @param moveList
     * @param code
     * @param desc
     * @return
     */
    TreeNode addNodes(List<String> moveList, String code, String desc, TreeReader handler) throws IllegalMoveException
    {
        TreeNode leaf = this;
        int size = moveList.size();

        for (int i = 0; i < size; i++)
        {
            if (i == size - 1) leaf = leaf.addNode(moveList.get(i), code, desc, handler);
            else leaf = leaf.addNode(moveList.get(i), null, null, handler);
        }

        return leaf;
    }

    /**
     * Adds a node if it does not already exist. Assumes no stored FEN.
     *
     * @param moveSt a single move in SAN
     * @param code
     * @param desc
     * @return
     */
    TreeNode addNode(String moveSt, String code, String desc, TreeReader handler) throws IllegalMoveException
    {
        String moveOnly = PgnGame.Move.getMoveText(moveSt);
        TreeNode branch = branchTo(moveOnly);

        if (branch == null)
        {
            branch = new TreeNode(this, ply + 1, moveSt, code, desc);
            branchMap.put(moveOnly, branch);
            handler.handleNewNode(branch);
        }

        else
        {
            if (code != null && code.length() > 0) branch.setSpecCode(code);
            if (desc != null && desc.length() > 0) branch.setSpecDesc(desc);
        }

        return branch;
    }

    /**
     * Recursive sub-node count, including this Node.
     *
     * @return
     */
    public int deepCount()
    {
        int ret = 1;
        for (String s : branchMap.keySet()) ret += branchMap.get(s).deepCount();
        return ret;
    }

    /**
     * code and desc are not included in comparison because they may change.
     *
     * @param that
     * @return 0 if same path
     */
    @Override
    public int compareTo(TreeNode that)
    {
        if (that == null) return 1;
        return getPathString().compareTo(that.getPathString());
    }

    @Override
    public int hashCode() {
        return 0x22AB9054 ^ getPathString().hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        try {return hashCode() == ((TreeNode) other).hashCode(); }
        catch (ClassCastException | NullPointerException e) { return false; }
    }

    @Override
    public String toString() {
        return getPathString() + " (" + code + ": " + desc + ")";
    }
}
