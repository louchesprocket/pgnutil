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

package com.dotfx.pgnutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 *
 * @author Mark Chen <chen@dotfx.com>
 */
public class EcoTree
{
    public enum Type
    {
        ECODB, // default
        STD, // raw default
        SCID, // raw Scid
        SCIDDB, // processed Scid
        NIK;
    }
    
    public class Node implements Comparable<Node>
    {
        private final int ply;
        private final String moveSt;
        private String code;
        private String desc;
        private final Node parent;
        private final Map<String,Node> branchMap; // indexed by move text

        /**
         * 
         * @param parent null for top tier nodes
         * @param ply
         * @param moveSt
         * @param code
         * @param desc 
         */
        private Node(Node parent, int ply, String moveSt, String code,
            String desc)
        {
            this.parent = parent;
            this.ply = ply;
            this.moveSt = moveSt;
            this.code = code;
            this.desc = desc;
            branchMap = new HashMap<>();
        }

        public int getPly() { return ply; }
        public String getMove() { return moveSt; }
        public String getSpecCode() { return code; }
        public String getSpecDesc() { return desc; }
        private void setCode(String code) { this.code = code; }
        private void setDesc(String desc) { this.desc = desc; }
        private Node branchTo(String moveTxt) { return branchMap.get(moveTxt); }
        public Node getParent() { return parent; }
        
        public String getMoveText()
        {
            return PgnGame.Move.getMoveText(moveSt);
        }
        
        public Node branchTo(Node node)
        {
            return branchTo(node.getMove());
        }
        
        public Node branchTo(PgnGame.Move move)
        {
            return branchTo(move.getMoveText());
        }
        
        public List<Node> getChildren()
        {
            List<Node> ret = new ArrayList<>();
            for (String s : branchMap.keySet()) ret.add(branchMap.get(s));
            return ret;
        }
        
        public Node getCodeNode()
        {
            Node node = this;
            while (node != null && node.code == null) node = node.parent;
            return node;
        }
        
        public String getCode()
        {
            Node node = getCodeNode();
            return node == null ? null : node.code;
        }
        
        public String getStdCode()
        {
            String ret = getCode();
            if (ret.length() >= 3) return ret.substring(0, 3);
            return ret;
        }
        
        public String getDesc()
        {
            Node node = this;
            
            while (node != null && node.desc == null) node = node.parent;
            return node == null ? null : node.desc;
        }
        
        public boolean isBottom()
        {
            return branchMap.isEmpty();
        }
        
        public Node getBottomNode(PgnGame game)
        {
            Node ret = this;
            Node next = ret;
            List<PgnGame.Move> moveList = game.getMoves();
            
            for (int i = 0; i < moveList.size(); i++)
            {
                next = next.branchTo(moveList.get(i).getMoveText());
                if (next == null) break;
                ret = next;
            }

            return ret;
        }
        
        public Node getBottomNode(PgnGame game, int maxDepth)
        {
            Node ret = this;
            Node next = ret;
            List<PgnGame.Move> moveList = game.getMoves();
            
            for (int i = 0; i < moveList.size() && i < maxDepth; i++)
            {
                next = next.branchTo(moveList.get(i).getMoveText());
                if (next == null) break;
                ret = next;
            }

            return ret;
        }
        
        public Node getDefinedNode()
        {
            Node node = this;
            
            while (node.getSpecCode() == null && node.getSpecDesc() == null)
                node = node.getParent();
            
            return node;
        }
        
        public Node getBottomDefinedNode(PgnGame game)
        {
            Node ret = this;
            Node next = ret;
            List<PgnGame.Move> moveList = game.getMoves();
            
            for (int i = 0; i < moveList.size(); i++)
            {
                next = next.branchTo(moveList.get(i).getMoveText());
                if (next == null) break;
                ret = next;
            }
            
            while (ret.getSpecCode() == null && ret.getSpecDesc() == null)
                ret = ret.getParent();

            return ret;
        }
        
        /**
         * 
         * @param moveSt example: "1. Nf3 c5 2. g3 Nc6"
         * @return 
         */
        public Node getBottomNode(String moveSt)
        {
            List<String> moveList = PgnGame.parseMoveString(moveSt);
            Node ret = this;
            Node next = ret;
            
            for (int i = 0; i < moveList.size(); i++)
            {
                next = next.branchTo(moveList.get(i));
                if (next == null) break;
                ret = next;
            }

            return ret;
        }
        
        public Node getBottomNode(List<Node> path)
        {
            Node ret = this;
            Node next = ret;
            
            for (int i = 0; i < path.size(); i++)
            {
                next = next.branchTo(path.get(i).getMoveText());
                if (next == null) break;
                ret = next;
            }

            return ret;
        }
        
        public List<Node> getPath()
        {
            List<Node> ret = new ArrayList<>();
            Node next = this;
            
            while (next.getParent() != null)
            {
                ret.add(0, next);
                next = next.getParent();
            }
            
            return ret;
        }
    
        /**
         * Adds a path below this node.
         * 
         * @param moveList
         * @param code
         * @param desc
         * @return 
         */
        private Node addNodes(List<String> moveList, String code, String desc)
            throws IllegalMoveException
        {
            Node leaf = this;
            int size = moveList.size();

            for (int i = 0; i < size; i++)
            {
                if (i == size - 1)
                    leaf = leaf.addNode(moveList.get(i), code, desc);
                
                else leaf = leaf.addNode(moveList.get(i), null, null);
            }
            
            return leaf;
        }

        /**
         * Adds a node if it does not already exist.
         * 
         * @param moveSt
         * @param code
         * @param desc
         * @return 
         */
        private Node addNode(String moveSt, String code, String desc)
            throws IllegalMoveException
        {
            String moveOnly = PgnGame.Move.getMoveText(moveSt);
            Node branch = branchTo(moveOnly);
            
            if (branch == null)
            {
                branch = new Node(this, ply + 1, moveSt, code, desc);
                branchMap.put(moveOnly, branch);
                
                Board board = new Board(true);
                board.move(branch.getPath());
                String fen = board.toShortFen().trim();

                Set<Node> nodeSet = fenMap.get(fen);

                if (nodeSet == null)
                {
                    nodeSet = new HashSet<>();
                    fenMap.put(fen, nodeSet);
                }
                
                nodeSet.add(branch);
                if (branch.getPly() > deepestPly) deepestPly = branch.getPly();
            }
            
            if (code != null) branch.code = code;
            if (desc != null) branch.desc = desc;
            
            return branch;
        }
        
        public int count()
        {
            int ret = 1;
            for (String s : branchMap.keySet()) ret += branchMap.get(s).count();
            return ret;
        }
        
        @Override
        public int compareTo(Node that)
        {
            if (that == null) return 1;
            
            if (that.getCode().equals(getCode()))
            {
                if (that.getDesc().equals(getDesc()))
                {
                    if (that.ply == ply)
                        return moveSt.compareTo(that.moveSt);
                    
                    return ply - that.ply;
                }
                
                return getDesc().compareTo(that.getDesc());
            }
            
            return getCode().compareTo(that.getCode());
        }
        
        @Override
        public String toString()
        {
            return ply + ": " + moveSt + " (" + code + ": " + desc + ")";
        }
    }
    
    public static class NodeSet implements Comparable<NodeSet>
    {
        private final Set<Node> nodeSet;
        
        public NodeSet(Set<Node> nodeSet) { this.nodeSet = nodeSet; }
        public NodeSet(Node node) { nodeSet = Collections.singleton(node); }
        public Set<Node> getNodeSet() { return nodeSet; }
        
        public String getDesc()
        {
            StringBuilder sb = new StringBuilder();
            Iterator<Node> iter = nodeSet.iterator();
            
            while (iter.hasNext())
            {
                sb.append(iter.next().getDesc());
                if (iter.hasNext()) sb.append(CLOptions.valueDelim);
            }
            
            return sb.toString();
        }
        
        public NodeSet getDefinedNodeSet()
        {
            Set<Node> ret = new HashSet<>();
            for (Node node : nodeSet) ret.add(node.getDefinedNode());
            return new NodeSet(ret);
        }
        
        public String getMoveString()
        {
            StringBuilder sb = new StringBuilder();
            Iterator<Node> iter = nodeSet.iterator();
            
            while (iter.hasNext())
            {
                Node endNode = iter.next();
                
                for (EcoTree.Node pathNode : endNode.getPath())
                {
                    int ply = pathNode.getPly();
                    if (ply != 1) sb.append(" ");

                    if (ply % 2 == 1)
                        sb.append(((ply + 1)/2)).append(".");

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
            Iterator<Node> iter = nodeSet.iterator();
            
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
                NodeSet that = (NodeSet)other;
                if (that.nodeSet.size() != nodeSet.size()) return false;
                
                java.util.Iterator<Node> thatIter =
                    that.nodeSet.iterator();
                
                for (Node node : nodeSet)
                    if (!node.equals(thatIter.next())) return false;
                
                return true;
            }
            
            catch (ClassCastException | NullPointerException e) { return false; }
        }
        
        @Override
        public int hashCode()
        {
            int ret = NodeSet.class.hashCode();
            for (Node node : nodeSet) ret ^= node.hashCode();
            return ret;
        }
        
        @Override
        public int compareTo(NodeSet that)
        {
            if (that == null) return 1;
            Iterator<Node> iter = that.nodeSet.iterator();
            
            for (Node node : nodeSet)
            {
                if (!iter.hasNext()) return 1;
                int comp = node.compareTo(iter.next());
                if (comp != 0) return comp;
            }
            
            if (iter.hasNext()) return -1;
            return 0;
        }
    }
    
    private static EcoTree instance;
    private static EcoTree scidInstance;
    private static EcoTree nikInstance;
    private static EcoTree stdInstance;
    
    private Node topNode;
    private int deepestPly;
    private final Map<String,Set<Node>> fenMap;
    
    private EcoTree(Type type) throws IOException, IllegalMoveException
    {
        topNode = new Node(null, 0, null, "", null);
        fenMap = new HashMap<>();
        
        switch (type)
        {
            case SCID:
                try (BufferedReader dbReader =
                    new BufferedReader(new InputStreamReader(
                        new FileInputStream("/Users/chen/Documents/chess/openings/scid.eco"))))
                {
                    String line;

                    while ((line = dbReader.readLine()) != null)
                    {
                        StringBuilder sb = new StringBuilder();
                        String s;

                        if (line.startsWith("#") || line.trim().length() == 0)
                            continue;

                        sb.append(line);

                        while (!line.contains("*"))
                        {
                            line = dbReader.readLine();
                            sb.append(line);
                        }

                        s = sb.toString();
                        List<String> moveList = new ArrayList<>();
                        String parts[] = s.split("\"");
                        String rawMoves[] = parts[2].trim().split("\\s+");

                        for (String move : rawMoves)
                        {
                            if (move.equals("*")) break;
                            int moveStart = 0;
                            int moveEnd = move.length() - 1;

                            while (!Character.isLetterOrDigit(move.charAt(moveEnd)))
                                moveEnd--;
                            
                            while (moveStart < moveEnd &&
                                !Character.isLetter(move.charAt(moveStart)))
                                moveStart++;
                            
                            if (moveStart == moveEnd) continue; // space after move #
                            
                            moveList.add(move.substring(moveStart,
                                moveEnd + 1));
                        }

                        insertNodes(moveList, parts[0].trim(), parts[1].trim());
                    }
                }
                
                break;

            case NIK:
                try (BufferedReader dbReader =
                    new BufferedReader(new InputStreamReader(
                        new FileInputStream("/Users/chen/Documents/chess/niklasf_eco/eco.json"))))
                {
                    String line;

                    while ((line = dbReader.readLine()) != null)
                    {
                        if (!line.trim().startsWith("{")) continue;

                        Board board = new Board(true);
                        List<String> moveList = new ArrayList<>();
                        String parts[] = line.split("\"");
                        String rawMoves[] = parts[17].trim().split("\\s+");

                        for (String move : rawMoves)
                            moveList.add(board.coordToSan(move));

                        insertNodes(moveList, parts[3], parts[7]);
                    }
                }
                
                break;
                
            case STD:
                try (BufferedReader dbReader =
                    new BufferedReader(new InputStreamReader(
                        EcoTree.class.getResource("/com/dotfx/pgnutil/std_eco").
                            openStream())))
                {
                    String line;

                    while ((line = dbReader.readLine()) != null)
                    {
                        if (line.startsWith("#")) continue;
                        line = line.trim();
                        if (line.length() == 0) continue;
                        
                        String code = line.substring(0, 3);
                        String desc = line.substring(3).trim();
                        
                        line = dbReader.readLine().trim();

                        Board board = new Board(true);
                        List<String> moveList = new ArrayList<>();
                        String rawMoves[] = line.split("\\s+");

                        for (String move : rawMoves)
                        {
                            if (Character.isDigit(move.charAt(0))) continue;
                            moveList.add(board.normalize(move));
                        }

                        insertNodes(moveList, code, desc);
                    }
                }
                
                break;
                
            case ECODB: readEcoDb("/com/dotfx/pgnutil/eco_db"); break;
            case SCIDDB: readEcoDb("/com/dotfx/pgnutil/scideco_db");
        }
    }
    
    private void readEcoDb(String resName)
        throws IOException, IllegalMoveException
    {
        try (BufferedReader dbReader =
            new BufferedReader(new InputStreamReader(
                EcoTree.class.getResource(resName).openStream())))
        {
            String line;
            String delim = Pattern.quote("|");

            while ((line = dbReader.readLine()) != null)
            {
                if (line.startsWith("#")) continue;
                line = line.trim();
                if (line.length() == 0) continue;

                String parts[] = line.split(delim);
                List<String> moveList = new ArrayList<>();

                for (String move : parts[2].split("\\s+"))
                {
                    int start = 0;
                    while (!Character.isLetter(move.charAt(start))) start++;
                    moveList.add(move.substring(start));
                }

                insertNodes(moveList, parts[0], parts[1]);
            }
        }
    }
    
    private Node insertNodes(List<String> moveList, String code, String desc)
        throws IllegalMoveException
    {
        return topNode.addNodes(moveList, code, desc);
    }
    
    public static EcoTree getInstance()
    {
        try { if (instance == null) instance = new EcoTree(Type.ECODB); }
        
        catch (IOException | IllegalMoveException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        return instance;
    }
    
    private static EcoTree getStdInstance()
    {
        try { if (stdInstance == null) stdInstance = new EcoTree(Type.STD); }
        
        catch (IOException | IllegalMoveException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
        return stdInstance;
    }
    
    public static EcoTree getScidInstance()
    {
        try
        {
            if (scidInstance == null) scidInstance = new EcoTree(Type.SCIDDB);
        }
        
        catch (IOException | IllegalMoveException e)
        {
            System.err.println("error reading ECO file");
            System.exit(-1);
        }
        
        return scidInstance;
    }
    
    private static EcoTree getNikInstance()
    {
        try { if (nikInstance == null) nikInstance = new EcoTree(Type.NIK); }
        
        catch (IOException | IllegalMoveException e)
        {
            System.err.println("error reading ECO file");
            System.exit(-1);
        }
        
        return nikInstance;
    }
    
    public Node getDeepestNode(PgnGame game)
    {
        return topNode.getBottomNode(game);
    }
    
    public Node getDeepestDefined(PgnGame game)
    {
        return topNode.getBottomDefinedNode(game);
    }
    
    public Node get(PgnGame game, int maxDepth)
    {
        return topNode.getBottomNode(game, maxDepth);
    }
    
    /**
     * 
     * @param moveSt example: "1. Nf3 c5 2. g3 Nc6"
     * @return 
     */
    public Node get(String moveSt)
    {
        return topNode.getBottomNode(moveSt);
    }
    
    /**
     * 
     * @param fen short FEN string
     * @return 
     */
    public NodeSet getFen(String fen) { return new NodeSet(fenMap.get(fen)); }
    
    public NodeSet getDeepestTranspositionSet(PgnGame game)
        throws IllegalMoveException
    {
        Set<Node> deepest = null;
        int ply = 0;
        Board board = new Board(true);
        
        for (PgnGame.Move move : game.getMoves())
        {
            if (++ply > deepestPly) break;
            Set<Node> nodeSet;
            
            try { nodeSet = fenMap.get(board.move(move).toShortFen()); }
            
            catch (IllegalMoveException | NullPointerException |
                StringIndexOutOfBoundsException e)
            {
                throw new IllegalMoveException("Illegal move: '" + move +
                    "' in game " + game.getNumber() + ".  Game text:\n" +
                    "##################################################\n" +
                    game.getOrigText() +
                    "\n##################################################", e);
            }
            
            if (nodeSet != null) deepest = nodeSet;
        }
        
        return new NodeSet(deepest);
//        return new NodeSet(deepest).getDefinedNodeSet();
    }
    
    public NodeSet getDeepestTranspositionSet(List<String> moveList)
        throws IllegalMoveException
    {
        Set<Node> deepest = null;
        int ply = 0;
        Board board = new Board(true);
        
        for (String move : moveList)
        {
            if (++ply > deepestPly) break;
            Set<Node> nodeSet = fenMap.get(board.move(move).toShortFen().trim());
            if (nodeSet != null) deepest = nodeSet;
        }
        
        return new NodeSet(deepest).getDefinedNodeSet();
    }
    
    public Node getTop() { return topNode; }
    public int positionCount() { return topNode.count(); }
    
    /**
     * Finds different codes.  Tries to go to the bottom of that.
     * 
     * @param that
     * @param ignoreDepth
     * @return List of parameter nodes whose codes differ from mine
     */
    private List<Node> diff(EcoTree that, boolean ignoreDepth)
    {
        List<Node> ret = new ArrayList<>();
        List<Node> thatChildren = that.topNode.getChildren();
        
        while (thatChildren.size() > 0)
        {
            Node thatNode = thatChildren.get(0);
            Node myNode = topNode.getBottomNode(thatNode.getPath());
            int mySize = myNode.getPath().size();
            int thatSize = thatNode.getPath().size();

            if (ignoreDepth || thatNode.getPly() == myNode.getPly())
            {
                if (mySize != thatSize)
                {
                    StringBuilder sb = new StringBuilder();
                    
                    for (Node node : myNode.getPath())
                    {
                        int ply = node.getPly();
                        if (ply != 1) sb.append(" ");

                        if (ply % 2 == 1) sb.append((ply + 1)/2).append(".");

                        sb.append(node.getMove());
                    }
                    
                    sb.append("\n");
                    
                    for (Node node : thatNode.getPath())
                    {
                        int ply = node.getPly();
                        if (ply != 1) sb.append(" ");

                        if (ply % 2 == 1) sb.append((ply + 1)/2).append(".");

                        sb.append(node.getMove());
                    }
                    
                    System.out.println("\n===== node diff:\n" + sb.toString());
                }
                
                if (!myNode.getStdCode().equals(thatNode.getStdCode()))
                    ret.add(thatNode);
                
                thatChildren.addAll(thatNode.getChildren());
            }
            
            thatChildren.remove(thatNode);
        }
        
        return ret;
    }
    
    public Map<PositionId,List<Node>> getPositionIdMap()
        throws IllegalMoveException
    {
        Map<PositionId,List<Node>> ret = new HashMap<>();
        List<Node> children = topNode.getChildren();
        
        while (children.size() > 0)
        {
            Board board = new Board(true);
            Node oneChild = children.get(0);
            List<Node> path = oneChild.getPath();
            
            for (Node node : path) board.move(node.getMove());
            
            PositionId positionId = board.positionId();
            List<Node> nodeList = ret.get(positionId);
            
            if (nodeList == null) nodeList = new ArrayList<>();
            nodeList.add(oneChild);
            
            ret.put(board.positionId(), nodeList);
            
            children.addAll(oneChild.getChildren());
            children.remove(oneChild);
        }
        
        return ret;
    }
    
    private Map<PositionId,List<Node>> getTranspositions()
        throws IllegalMoveException
    {
        Map<PositionId,List<Node>> dupeMap = new HashMap<>();
        Map<PositionId,List<Node>> positionMap = getPositionIdMap();
        
        for (PositionId position : positionMap.keySet())
        {
            List<Node> nodeList = positionMap.get(position);
            if (nodeList.size() > 1) dupeMap.put(position, nodeList);
        }
        
        return dupeMap;
    }
    
    private void printTranspositions() throws IllegalMoveException
    {
        int transCount = 0;
        
        for (String fen : fenMap.keySet())
        {
            Set<Node> transposed = fenMap.get(fen);
            if (transposed.size() < 2) continue;
            transCount++;
            
            for (Node node : transposed)
            {
                System.out.println(node.getCode() + ": " + node.getDesc() +
                    " (depth " + node.getPly() + ")");
                
                for (Node pathNode : node.getPath())
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
    
    private void writeStdFormat(File outFile)
        throws IllegalMoveException, IOException
    {
        SortedSet<String> lineSet = new TreeSet<>();
        List<Node> children = topNode.getChildren();

        while (children.size() > 0)
        {
            Node node = children.get(0);
            children.addAll(node.getChildren());
            children.remove(node);
            
            if (node.getSpecCode() == null && node.getSpecDesc() == null/* ||
                (node.getSpecCode().equals(node.getParent().getCode()) &&
                node.getSpecDesc().equals(node.getParent().getDesc()))*/)
                continue;
            
            boolean diff = false;
            Board board = new Board(true);
            List<Node> nodePath = node.getPath();
            StringBuilder sb = new StringBuilder();

            sb.append(node.getCode()).append("|").append(node.getDesc()).
                append("|");

            for (Node pathNode : nodePath)
            {
                int ply = pathNode.getPly();
                if (ply != 1) sb.append(" ");
                if (ply % 2 == 1) sb.append((ply + 1)/2).append(".");
                String moveSt = board.normalize(pathNode.getMove());
                sb.append(moveSt);
                
                if (!moveSt.equals(pathNode.getMove()))
                {
                    diff = true;
                    
                    System.out.println("===== path: " + pathNode.getMove() +
                        "; moveSt: " + moveSt);
                }
            }

            sb.append("\n");
            lineSet.add(sb.toString());
            
            if (diff) System.out.println(sb.toString());
        }
        
        try (Writer writer = new FileWriter(outFile, false))
        {
            for (String line : lineSet) writer.write(line);
        }
    }
    
    private void printTree() throws IllegalMoveException
    {
        List<Node> children = topNode.getChildren();

        while (children.size() > 0)
        {
            Node node = children.get(0);
            List<Node> nodePath = node.getPath();

            System.out.print(node.getCode() + " ");

            for (Node pathNode : nodePath)
            {
                int ply = pathNode.getPly();
                if (ply != 1) System.out.print(" ");

                if (ply % 2 == 1)
                    System.out.print(((ply + 1)/2) + ".");

                System.out.print(pathNode.getMove());
            }

            System.out.print("\n");

            children.addAll(node.getChildren());
            children.remove(node);
        }
    }
}
