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

import com.dotfx.pgnutil.Board;
import com.dotfx.pgnutil.IllegalMoveException;
import com.dotfx.pgnutil.PgnGame;
import com.dotfx.pgnutil.PositionId;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public final class EcoTree
{
    public enum FileType
    {
        // raw trees
        SCID("resources/scid.eco", false, new ScidReader.Factory()), // raw Scid
        NIK("resources/eco.json", false, new NikReader.Factory()), // old lichess
        LICHESS("../lichess_eco/dist/all.tsv", false, new LichessReader.Factory()), // new lichess

        // processed trees
        SCIDDB("/com/dotfx/pgnutil/eco/scideco_db", true, new StdReader.Factory()),
        STD("/com/dotfx/pgnutil/eco/lichesseco_db", true, new StdReader.Factory());

        private final String path;
        private final
        boolean isResource;
        private final TreeReaderFactory readerFactory;
        private EcoTree treeInstance;

        FileType(String path, boolean isResource, TreeReaderFactory readerFactory)
        {
            this.path = path;
            this.isResource = isResource;
            this.readerFactory = readerFactory;
        }

        private String getPath() { return path; }
        private boolean isResource() { return isResource; }
        private TreeReader getReader() { return readerFactory.newInstance(); }

        public EcoTree getEcoTree()
        {
            try { if (treeInstance == null) treeInstance = new EcoTree(this); }

            catch (IOException | IllegalMoveException e)
            {
                e.printStackTrace();
                System.exit(-1);
            }

            return treeInstance;
        }
    }
    
    private TreeNode topNode;
    private TreeReader reader;

    private EcoTree(FileType fileType) throws IOException, IllegalMoveException
    {
        this(fileType, null);
    }
    
    private EcoTree(FileType fileType, File pathOverride) throws IOException, IllegalMoveException
    {
        topNode = new TreeNode(null, 0, null, "", "");
        reader = fileType.getReader();

        try (InputStream in = pathOverride == null ?
                (fileType.isResource() ?
                        new BufferedInputStream(EcoTree.class.getResource(fileType.getPath()).openStream()) :
                        new BufferedInputStream(new FileInputStream(fileType.getPath()))) :
                new BufferedInputStream(new FileInputStream(pathOverride)))
        {
            reader.readTree(in, topNode);
        }

    }
    
    public TreeNode getDeepestNode(PgnGame game) { return topNode.getBottomNode(game); }
    public TreeNode getDeepestDefined(PgnGame game) { return topNode.getBottomDefinedNode(game); }
    public TreeNode getDeepestDefined(List<PgnGame.Move> moveList) { return topNode.getBottomDefinedNode(moveList); }
    public TreeNode get(PgnGame game, int maxDepth) { return topNode.getBottomNode(game, maxDepth); }
    
    /**
     * 
     * @param moveSt example: "1. Nf3 c5 2. g3 Nc6"
     * @return 
     */
    public TreeNode get(String moveSt) { return topNode.getBottomNode(moveSt); }
    
    public TreeNodeSet getDeepestTranspositionSet(PgnGame game) throws IllegalMoveException
    {
        try { return getDeepestTranspositionSet(game.getMoves()); }

        catch (IllegalMoveException | NullPointerException | StringIndexOutOfBoundsException e)
        {
            throw new IllegalMoveException("in game " + game.getNumber() +
                    ". Game text:\n##################################################\n" + game.getOrigText() +
                    "\n##################################################", e);
        }
    }

    /**
     *
     * @param moveList
     * @return
     * @throws IllegalMoveException
     */
    public TreeNodeSet getDeepestTranspositionSet(List<PgnGame.Move> moveList) throws IllegalMoveException
    {
        Map<String,Set<TreeNode>> posMap = reader.getPositionMap();
        Board board = new Board(true);
        Set<TreeNode> deepest = null;

        for (int i = board.getPly(); i < moveList.size() && i <= reader.getDeepestPly(); i++)
        {
            PgnGame.Move move = moveList.get(i);

            try
            {
                Set<TreeNode> nodeSet = posMap.get(board.move(move).positionId().toString());
                if (nodeSet != null) deepest = nodeSet;
            }

            catch (IllegalMoveException | NullPointerException | StringIndexOutOfBoundsException e)
            {
                throw new IllegalMoveException("Illegal move: '" + move + "'", e);
            }
        }

        return deepest == null ? new TreeNodeSet(new TreeSet<>()) : new TreeNodeSet(deepest);
    }
    
    public TreeNode getTop() { return topNode; }
    public int positionCount() { return topNode.deepCount(); }

    /**
     * Finds different codes.  Tries to go to the bottom of that.
     *
     * @param that
     * @param ignoreDepth
     * @param checkDesc
     * @return
     */
    private List<TreeNode> diff(EcoTree that, boolean ignoreDepth, boolean checkDesc)
    {
        List<TreeNode> ret = new ArrayList<>();
        List<TreeNode> thatChildren = that.topNode.getChildren();
        
        while (thatChildren.size() > 0)
        {
            TreeNode thatNode = thatChildren.get(0);
            TreeNode myNode = topNode.getBottomNode(thatNode.getPath());
            int mySize = myNode.getPath().size();
            int thatSize = thatNode.getPath().size();

            if (ignoreDepth || thatNode.getPly() == myNode.getPly())
            {
                if (mySize != thatSize)
                {
                    StringBuilder sb = new StringBuilder();
                    
                    for (TreeNode node : myNode.getPath())
                    {
                        int ply = node.getPly();
                        if (ply != 1) sb.append(" ");
                        if (ply % 2 == 1) sb.append((ply + 1)/2).append(".");
                        sb.append(node.getMove());
                    }
                    
                    sb.append("\n");
                    
                    for (TreeNode node : thatNode.getPath())
                    {
                        int ply = node.getPly();
                        if (ply != 1) sb.append(" ");
                        if (ply % 2 == 1) sb.append((ply + 1)/2).append(".");
                        sb.append(node.getMove());
                    }
                    
                    System.out.println("\n===== node diff:\n" + sb.toString());
                }
                
                if (!myNode.getStdCode().equals(thatNode.getStdCode())) ret.add(thatNode);
                if (checkDesc && !myNode.getDesc().equals(thatNode.getDesc())) ret.add(thatNode);
                
                thatChildren.addAll(thatNode.getChildren());
            }
            
            thatChildren.remove(thatNode);
        }
        
        return ret;
    }
    
    public Map<PositionId,List<TreeNode>> getPositionIdMap() throws IllegalMoveException
    {
        Map<PositionId,List<TreeNode>> ret = new HashMap<>();
        List<TreeNode> children = topNode.getChildren();
        
        while (children.size() > 0)
        {
            Board board = new Board(true);
            TreeNode oneChild = children.get(0);
            List<TreeNode> path = oneChild.getPath();
            
            for (TreeNode node : path) board.move(node.getMove());
            
            PositionId positionId = board.positionId();
            List<TreeNode> nodeList = ret.get(positionId);
            
            if (nodeList == null) nodeList = new ArrayList<>();
            nodeList.add(oneChild);
            
            ret.put(board.positionId(), nodeList);
            
            children.addAll(oneChild.getChildren());
            children.remove(oneChild);
        }
        
        return ret;
    }
    
    private Map<PositionId,List<TreeNode>> getTranspositions() throws IllegalMoveException
    {
        Map<PositionId,List<TreeNode>> dupeMap = new HashMap<>();
        Map<PositionId,List<TreeNode>> positionMap = getPositionIdMap();
        
        for (PositionId position : positionMap.keySet())
        {
            List<TreeNode> nodeList = positionMap.get(position);
            if (nodeList.size() > 1) dupeMap.put(position, nodeList);
        }
        
        return dupeMap;
    }
    
    private void printTranspositions()
    {
        Map<String,Set<TreeNode>> posMap = reader.getPositionMap();
        int transCount = 0;
        
        for (String posSt : posMap.keySet())
        {
            Set<TreeNode> transposed = posMap.get(posSt);
            if (transposed.size() < 2) continue;
            transCount++;
            
            for (TreeNode node : transposed)
            {
                System.out.println(node.getCode() + ": " + node.getDesc() + " (depth " + node.getPly() + ")");
                
                for (TreeNode pathNode : node.getPath())
                {
                    int moveno = (pathNode.getPly() + 1)/2;
                    
                    if (pathNode.getPly() % 2 == 1) System.out.print(moveno + ". " );
                    System.out.print(pathNode.getMove() + " ");
                }
                
                System.out.print("\n");
            }
            
            System.out.println("====================");
        }
        
        System.out.println("TRANSPOSITIONS: " + transCount);
        
//        Map<PositionId,List<Node>> transpositions = getTranspositions();
//        
//        for (PositionId positionId : transpositions.keySet())
//        {
//            List<Node> transposed = transpositions.get(positionId);
//            
//            for (Node node : transposed)
//            {
//                System.out.println(node.getCode() + ": " + node.getDesc());
//                
//                for (Node pathNode : node.getPath())
//                {
//                    int moveno = (pathNode.getPly() + 1)/2;
//                    
//                    if (pathNode.getPly() % 2 == 1) System.out.print(moveno + ". " );
//                    System.out.print(pathNode.getMove() + " ");
//                }
//                
//                System.out.print("\n");
//            }
//            
//            System.out.println("====================");
//        }
//        
//        System.out.println("TRANSPOSITIONS: " + transpositions.keySet().size());
    }

    /**
     * Writes standard format (read by StdReader).
     *
     * @param outFile
     * @throws IOException
     * @throws IllegalMoveException
     */
    private void writeTree(File outFile) throws IOException, IllegalMoveException
    {
        List<TreeNode> children = topNode.getChildren();

        try (Writer writer = new FileWriter(outFile, false))
        {
            while (children.size() > 0)
            {
                TreeNode node = children.get(0);
                children.addAll(node.getChildren());
                children.remove(node);

                if (node.getSpecCode().length() == 0 && node.getSpecDesc().length() == 0) continue;

                // At this point, we have a path ending with either a code or a description (presumably both).

                StringBuilder sb = new StringBuilder();

                sb.append(node.getCode()).append(StdReader.LINE_DELIM).append(node.getDesc()).
                        append(StdReader.LINE_DELIM);

                List<TreeNode> path = node.getPath();
                Board board = new Board(true);

                for (TreeNode subPathNode : path)
                {
                    board.move(subPathNode.getMove());
                    sb.append(subPathNode.getMove()).append(StdReader.MOVE_DELIM);
                }

                sb.setLength(sb.length() - 1);
                sb.append(StdReader.LINE_DELIM);
                sb.append(board.positionId());

                sb.append("\n");
                writer.write(sb.toString());
            }
        }
    }
    
    private void printTree() throws IllegalMoveException
    {
        List<TreeNode> children = topNode.getChildren();

        while (children.size() > 0)
        {
            TreeNode node = children.get(0);
            List<TreeNode> nodePath = node.getPath();

            System.out.print(node.getCode() + " ");

            for (TreeNode pathNode : nodePath)
            {
                int ply = pathNode.getPly();
                if (ply != 1) System.out.print(" ");
                if (ply % 2 == 1) System.out.print(((ply + 1)/2) + ".");
                System.out.print(pathNode.getMove());
            }

            System.out.print("\n");

            children.addAll(node.getChildren());
            children.remove(node);
        }
    }

    public static void printDiff(EcoTree tree1, EcoTree tree2, boolean checkDesc)
    {
        List<TreeNode> treeDiff = tree1.diff(tree2, false, checkDesc);

        // Prints tree2 code, tree1 code, moves
        for (TreeNode tree2Node : treeDiff)
        {
            List<TreeNode> tree2Path = tree2Node.getPath();
            System.out.print(tree1.getTop().getBottomNode(tree2Path).getStdCode() + "|" + tree2Node.getStdCode() + "|");

            if (checkDesc)
                System.out.print(tree1.getTop().getBottomNode(tree2Path).getDesc() + "|" + tree2Node.getDesc() + "|");

            for (TreeNode pathNode : tree2Path)
            {
                int ply = pathNode.getPly();
                if (ply != 1) System.out.print(" ");
                if (ply % 2 == 1) System.out.print(((ply + 1)/2) + ".");
                System.out.print(pathNode.getMove());
            }

            System.out.print("\n");
        }

        System.out.println("DIFF: " + treeDiff.size());
    }
    
//    public static void main(String args[]) throws Exception
//    {
//        long start = System.currentTimeMillis();
////        EcoTree tree1 = new EcoTree(Type.SCID);
////        EcoTree tree1 = getScidInstance();
////        tree1.printTranspositions();
//
//        EcoTree tree1 = new EcoTree(FileType.SCIDDB);
////        tree1.printTree();
////        tree1.writeTree(new File("test.out"));
////        EcoTree tree2 = new EcoTree(FileType.STD);
////        printDiff(tree1, tree2, false);
////        tree1.printTranspositions();
////        System.out.println("nodes: " + tree1.positionCount());
//
//        Board board = new Board(true).move("Nf3").move("e5").move("e4").move("Nc6").move("Bb5");
//
////        List<TreeNode> nodeList = tree1.get("1. d4 Nf6 2. c4 e6 3. Nf3 c5 4. d5 exd5 5. cxd5 d6 6. Nc3 g6 7. e4 Bg7 " +
////                "8. Be2 O-O 9. O-O a6 10. a4").getPath();
//////        List<TreeNode> nodeList = tree1.get("1.d4 e6 2. Nf3").getPath();
////
////        System.out.println("===== len: " + nodeList.size());
////        TreeNode bottom = nodeList.get(nodeList.size() - 1);
////        System.out.println("===== code: " + bottom.getCode());
////        System.out.println("===== desc: " + bottom.getDesc());
//
////        System.out.println("NODE COUNT: " + tree1.positionCount());
////        printDiff(tree1, tree2);
//        System.out.println("ELAPSED: " + (System.currentTimeMillis() - start) + "ms");
//    }
}
