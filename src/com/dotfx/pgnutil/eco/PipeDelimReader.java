package com.dotfx.pgnutil.eco;

import com.dotfx.pgnutil.IllegalMoveException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Obsolete format. Reads pipe-delimited input file without stored FEN strings.
 */
public class PipeDelimReader extends TreeReader
{
    static class Factory implements TreeReaderFactory
    {
        @Override
        public TreeReader newInstance() { return new PipeDelimReader(); }
    }

    @Override
    void readTree(InputStream in, TreeNode topNode) throws IOException, IllegalMoveException
    {
        try (BufferedReader dbReader = new BufferedReader(new InputStreamReader(in)))
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

                topNode.addNodes(moveList, parts[0], parts[1]);
            }
        }
    }
}
