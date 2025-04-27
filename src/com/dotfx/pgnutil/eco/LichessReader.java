package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.Board;
import com.dotfx.pgnutil.IllegalMoveException;
import com.dotfx.pgnutil.PgnGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LichessReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new LichessReader(); }
    }

    @Override
    void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException
    {
        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
        {
            String line;

            while ((line = dbReader.readLine()) != null)
            {
                if (line.startsWith("eco")) continue;
                List<String> moveList = new ArrayList<>();
                Board board = new Board(true);
                String parts[] = line.split("\\t");

                try
                {
                    for (String move : PgnGame.parseMoveString(parts[2].trim()))
                        moveList.add(board.normalize(move, false));

                    TreeNode newNode = topNode.addNodes(moveList, parts[0].trim(), parts[1].trim());
                    newNode.setPositionId(board.positionId().toString());
                    handleNewNode(newNode);
                }

                catch (Exception e)
                {
                    System.err.println(e.getMessage() + " at " + "'" + parts[0].trim() + "':" +
                            "'" + parts[1].trim() + "':" + "'" + parts[2].trim() + "'");
                    throw e;
                }
            }
        }
    }
}
