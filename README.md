# pgnutil

Pgnutil is a command-line tool for querying and filtering Portable Game Notation (PGN) files.  I wrote the tool because I like to run computer-chess tournaments for my own amusement, and found that calculating results was a nightmare due to a lack of very basic capabilities for handling PGN files.  Common problems such as finding duplicate games, identifying time forfeitures, and even just figuring out if I had already played a particular pair of engines against a particular set of test positions were nearly insoluble.  After failing to find any applicable tool on the
Internet, I wrote pgnutil.

Pgnutil works as a Unix-style command-line filter that performs [matching/selecting](#matching) and [replacing](#replacing) functions on PGN files in conjunction with various [output options](#output-selectors). By default, pgnutil simply outputs selected games after performing any stipulated replacement operations, but it can instead output user-selected lists of fields.  In addition, pgnutil has various ["special" output options](#special-output-options): duplicate finding, event listing, [opening statistics](#openings), and player statistics.  Pgnutil is particularly designed to be used in conjunction with tools such as bayeselo or elostat.


## Requirements

Pgnutil requires Java 1.8 or higher.  It theoretically runs on any platform with a command line.


## Building

To build, open the project in IntelliJ Idea and select
"Build->Build Artifacts->pgnutil:jar->Rebuild" from the menu bar. Then run script/mkdist.sh. This will create the pgnutil integrated executable in the dist directory.


## Usage

For a complete list of options:

``pgnutil -h``

### Matching
Pgnutil's most basic matching option is "-m", which takes a regular expression as its parameter and matches against the entire text of each game in the file.  For example, the following search prints every game containing the word "forfeit" from the file mygames.pgn:

``pgnutil -m 'forfeit' -i mygames.pgn``

By default, pgnutil outputs the entire text of each matched game. So in the previous example, we would have to sort through the output to discover which players forfeited.  Much better to save the trouble by combining the "-s" option (see [Output Selectors](#output-selectors), below), and perhaps piping the output to another Unix command to produce a list of unique names of forfeiters:

``pgnutil -m 'forfeit' -s loser -i mygames.pgn | sort -u``

If we only want to get a list of players that have forfeited in a game involving some version of Stockfish, we can combine the "-mp" option, which matches any player name (either black or white):

``pgnutil -mp 'Stockfish' -m 'forfeit' -s loser -i mygames.pgn | sort -u``

If we don't want to see Stockfish itself in this list (which would happen if Stockfish forfeited any games), we can use the "-mw" (match winner) option to omit cases where Stockfish lost.  We can also list the event and round number for every such game:

``pgnutil -m 'forfeit' -mw 'Stockfish' -s loser,Event,Round -i mygames.pgn``

To output every game that Stockfish drew, we can use the -mt ("match
tag") option:

``pgnutil -mp 'Stockfish' -mt 'Result/1\/2-1\/2' -i mygames.pgn``

If we want to perform any of these queries on a list of player names (instead of a single player name) we can use the player-file options "-pf" (to match both players), "-apf" (to match either player), or "-npf" (to match neither player).  For example, the command:

``pgnutil -pf myplayers.txt -mt 'Result/1\/2-1\/2' -i mygames.pgn``

will output every drawn game from the file mygames.pgn wherein both players are listed in the file myplayers.txt.  The latter is a simple text file listing player names, one per line.

#### Position Search

Position searches may be performed with the "-mpos" (match position) option, where the parameter is in Standard Algebraic Notation. For example:

``pgnutil -mpos '1.d4 Nf6 2.c4 c5' -i mygames.pgn``

Alternatively,

``pgnutil -posf sanfile.txt -i mygames.pgn``

uses the "-posf" (position-file) option to search for a list of positions contained in sanfile.txt.

The "-mfen" (match FEN) option may be used to search for positions from a FEN string:

``pgnutil -mfen 'rnb1kb1r/ppq2ppp/3p4/2pPp3/8/2B2Q2/PPP2PPP/2KR1BNR w kq e6 0 9' -i mygames.pgn``

And its corresponding multi-position search option is "-ff" (FEN-file):

``pgnutil -ff fenfile.txt -i mygames.pgn``

#### ECO Search

Searches for ECO codes may, of course, be performed with the <nobr>"-mt"</nobr> option if the games in question have that tag. A more general way to perform ECO searches is with the <nobr>"-me"</nobr> (match ECO) option. For example:

``pgnutil -me 'A4[23]' -i mygames.pgn``

will return every game in which the ECO openings A42 or A43 were played, ignoring any "ECO" tags. Note that ECO codes for this option are matched for the entire game, not just the "opening" as defined under [Openings](#openings), below. Other ECO-related matching options are:

* -med: match ECO description, searching by move sequence
* -mxe: match ECO code, searching positions transpositionally
* -mxed: match ECO description, searching positions transpositionally
* -mse: match Scid ECO code, searching by move sequence
* -msed: match Scid ECO description, searching by move sequence
* -mxse: match Scid ECO code, searching positions transpositionally
* -mxsed: match Scid ECO description, searching positions transpositionally

For all ECO-related operations, the standard (non-Scid) ECO database may be specified on the command line with the "-ef" (ECO file) option. This file should use the same format as Lichess (see https://github.com/lichess-org/chess-openings). If this option is not set, pgnutil will use its own internal database.

### Replacing

The "-r" option performs replacements on the game text.  For example, to strip comments (or, to be precise, all text between curly braces) from mygames.pgn:

``pgnutil -r '.*/\{.*?\}/' -i mygames.pgn``

The first "/"-separated component in the parameter to the "-r" option selects games upon which to perform the replacement.  So the previous example means, "For every game containing the regular expression '.&#42;' (i.e., any game at all), replace every occurrence of the regular expression '\\{.&#42;?\\}' with an empty string."

To replace every instance of 'Quazar' from the file mygames.pgn with 'Quazar 0.4 x64':

``pgnutil -r '.*/Quazar/Quazar 0.4 x64' -i mygames.pgn``

Similarly, the command:

``pgnutil -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -i mygames.pgn``

means, "For every game containing 'Nunn 1' or 'Noomen 2012,' replace every occurrence of 'Quazar' with 'Quazar 0.4 x64.'"

The selectivity of the replacement can be further refined with other options.  For example, the command:

``pgnutil -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -rl 'Glaurung 2.0.1' -i mygames.pgn``

uses the "-rl" (replace loser) option to mean, "For every game containing 'Nunn 1' or 'Noomen 2012' that was lost by 'Glaurung 2.0.1,' replace every occurrence of 'Quazar' with 'Quazar 0.4 x64.'" And, of course, any of these may be combined with any of the various [matching](#matching) and [output-selection](#output-selectors) options:

``pgnutil -m '[Bb]litz' -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -rl 'Glaurung 2.0.1' -s Event -i mygames.pgn``

means, "Output the value of the 'Event' tag for every game containing 'Blitz' or 'blitz,' but of these games, for every game containing 'Nunn 1' or 'Noomen 2012' that was lost by 'Glaurung 2.0.1,' replace every occurrence of 'Quazar' with 'Quazar 0.4 x64.'"

### Output Selectors

By default, pgnutil will output the full text of the game in response to any search operation.  The "-s" option may be used to restrict the output to a list of selected fields. Selected fields may include any PGN tag and should be separated on the command line by commas.

By default, fields selected by the "-s" option appear on the output separated by the pipe ("|") character.  If a different output delimiter is desired, this may be set with the "-od" (output-delimiter) option.

Similarly, values within a field are, by default, separated by commas.  If a different value delimiter is desired, it may be set with the "-vd" (value-delimiter) option.

#### Special Output Selectors

There are several "special" selectors recognized by the "-s" option.  For example:

*	moves: causes pgnutil to output the game's move list
* 	tags: causes pgnutil to output an alphabetized list of the game's header tags
* 	decoratedmoves: causes pgnutil to output the game's move list and move comments
* 	openingmoves: causes pgnutil to output the opening move list (see [Openings](#openings), below)
* 	oid: causes pgnutil to output the game's opening identifier (see [Openings](#openings), below)
* 	winner: causes pgnutil to output the name of the winner
* 	loser: causes pgnutil to output the name of the loser
* 	opponent: when the "-mp" (match player) option is used, output the name of the other player
* 	playerelo: when the "-mp" (match player) option is used, output the value of the "Elo" header for the specified player
* 	opponentelo: when the "-mp" (match player) option is used, output the value of the "Elo" header for the opposing player
* 	gameno: causes pgnutil to output the game's ordinal position within the PGN file
* 	plies: causes pgnutil to output the number of half-moves present in the game
* 	cbplayers: for Aquarium games only, and in conjunction with the "-cb" option (see [Time (Aquarium Only)](#time-aquarium-only), below), causes pgnutil to output the names of players whose clocks fell below the indicated time threshold
* 	lowclockwhite: for Aquarium games only, causes pgnutil to output the lowest clock value for the white player (see [Time (Aquarium Only)](#time-aquarium-only), below)
* 	lowclockblack: for Aquarium games only, causes pgnutil to output the lowest clock value for the black player (see [Time (Aquarium Only)](#time-aquarium-only), below)
* 	textsize: causes pgnutil to output the size (in characters) of the original game text

Thus, the command:

``pgnutil -mt 'Event/Nunn 1' -mp 'Glaurung 2.0.1' -s opponent,winner -i mygames.pgn``

means, "For every game in which the 'Event' tag contains the text 'Nunn 1' and in which 'Glaurung 2.0.1' was a player, output the name of the opponent and the name of the winner."

#### ECO-Related Output Selectors

There are also several output selectors relating to ECO codes:

* 	stdeco: output the standard ECO code for the game, matching move sequences
* 	xstdeco: output the standard ECO code for the game, matching positions transpositionally
* 	scideco: output the Scid ECO code for the game, matching move sequences
* 	xscideco: output the Scid ECO code for the game, matching positions transpositionally
* 	stdecodesc: output the standard description of the opening, matching move sequences
* 	xstdecodesc: output the standard description of the opening, matching positions transpositionally
* 	scidecodesc: output the Scid description of the opening, matching move sequences
* 	xscidecodesc: output the Scid description of the opening, matching positions transpositionally
* 	stdecomoves: output the game moves that match the line defining the standard ECO code, matching move sequences
* 	xstdecomoves: output the game moves that match the line defining the standard ECO code, matching positions transpositionally
* 	scidecomoves: output the game moves that match the line defining the Scid ECO code, matching move sequences
* 	xscidecomoves: output the game moves that match the line defining the Scid ECO code, matching positions transpositionally

Note that any of the transpositional selectors ("xstdeco," "xscideco," "xstdecodesc," "xscidecodesc," "xstdecomoves," and "xscidecomoves") may return more than one result per game.

### Special Output Options

The "special" output options include "-d" (duplicates), "-dm" (duplicate moves), "-do" (duplicate [openings](#openings)), "-e" (events), "-csr" (check sequential rounds), "-o" ([opening](#openings) statistics), and "-p" (player statistics). Any of these may be combined with any [matching](#matching) and [replacing](#replacing) options (see above).

To find duplicate games (defined as games with the same players and same move list) in the file mygames.pgn:

``pgnutil -d -i mygames.pgn``

The previous command will print a comma-separated list of game numbers (indexed from the first game of the PGN file), with each set of duplicates on a separate line.  For example, the output:

> 1616,1617<br>
> 1622,1623,1710

means that game 1617 is a duplicate of 1616, and games 1623 and 1710 are duplicates of 1622.  To output games 1616 and 1617 from the PGN file:

``pgnutil -gn 1616,1617 -i mygames.pgn``

To output games 1616-1623 and game 1710:

``pgnutil -gn 1616-1623,1710 -i mygames.pgn``


To list each event from the file mygames.pgn, along with the games that belong to it:

``pgnutil -e -i mygames.pgn``

To list each player from the file mygames.pgn, along with win/loss/draw statistics:

``pgnutil -p -i mygames.pgn``

The "-do" option is for engine tournaments. It works analogously to the "-d" option, and outputs games that contain the same [opening](#openings) and same players.

The "-csr" option checks for non-sequential round numbers within events. Note that assignment of round numbers is dependent on the tournament format, so this option is only useful with respect to tournaments where each game is assigned a unique round number (e.g., Aquarium matches, but not Banksia matches).

### Openings

Pgnutil's definition of an "opening" pertains specifically to chess engines; it is the list of a game's book moves that occur prior to the first engine-generated move.

To list opening statistics for all games in mygames.pgn:

``pgnutil -o -i mygames.pgn``

This query may also be paramaterized and run on a [selection of games](#matching). For example:

``pgnutil -mt 'Event/Nunn 1' -o -cmin 100 -lwd -.1 -hwd .1 -hdraw .5 -i mygames.pgn``

means, "Print opening statistics for every opening represented by at least 100 games where the 'Event' tag contains the text 'Nunn 1,' the difference in win percentage between black and white is no greater than 10%, and the draw percentage is no greater than 50%."

The opening-statistics function has its own set of output selectors:

* 	oid: the opening identifier; this may be used with the "match opening" ("-mo") and "replace opening" ("-ro") options
* 	count: the count of games represented by this opening
* 	wwins: the number of white wins for this opening
* 	wwinpct: white wins, expressed as a percentage of all games having a result
* 	bwins: the number of black wins for this opening
* 	bwinpct: black wins, expressed as a percentage of all games having a result
* 	diff: the ference in wins between white and black
* 	diffpct: the different in wins between white and black, expressed as a percentage of all games having a result
* 	draws: the number of draws for this opening
* 	drawpct: the number of draws for this opening expressed as a percentage of all games having a result

In addition, any of the [ECO-related output selectors](#eco-related_output_selectors) may be used in this context, but will apply only to the opening moves of the game.

We can use the previous example to generate a list of opening identifers (note addition of the "-s" option):

``pgnutil -mt 'Event/Nunn 1' -o -cmin 100 -lwd -.1 -hwd .1 -hdraw .5 -s oid -i mygames.pgn``

which may produce output such as:

> 4748b62c5f943db4<br>
> 58042cacbd9498f9<br>
> 3ef5ae89557400b9<br>
> 5e8c715de2d30397<br>

These can then be fed to a matching command ("-mo"):

``pgnutil -mo 4748b62c5f943db4,58042cacbd9498f9,3ef5ae89557400b9,5e8c715de2d30397 -i mygames.pgn``

to output those games wherein the selected openings were played. If you wish to input a long list of opening identifiers, you may instead use the "-of" (opening-file) option, which takes as its parameter the name of a file containing opening identifiers:

``pgnutil -of myopeningsfile -i mygames.pgn``

To output opening statistics by ECO code instead of opening identifier (based on "opening" moves only, as defined in this section), use the <nobr>"-eco"</nobr> option in conjuction with <nobr>"-o"</nobr>:

``pgnutil -o -eco -i mygames.pgn``

Similarly, the options <nobr>"-xeco"</nobr>, <nobr>"-seco"</nobr>, and <nobr>"-xseco"</nobr> collate the output by ECO code matched transpositionally, Scid ECO code, and Scid ECO code matched transpositionally, respectively.

Pgnutil identifies the demarcation point between "opening" moves and engine-generated moves by searching for a specific regular expression. By default, this regular expression is:

``(out\s+of\s+book)|(^End\s+of\s+opening)``

which matches the "out-of-book" marker for both Aquarium and Banksia. This regular expression may be set to any value with the book-marker ("-bm") option. For example,

``pgnutil -of myopeningsfile -bm 'my book marker' -i mygames.pgn``

will search for games matching any of the openings from myopeningsfile, using "my book marker" as the demarcation between "book" moves and engine moves.

If pgnutil fails to find the out-of-book marker for a game, it assumes that the first commented move is the first non-book move. This corresponds to Arena's behavior. Therefore, pgnutil's default behavior (without the "-bm" option) will correctly identify the out-of-book condition for Aquarium, Banksia, and Arena.

### Time (Aquarium Only)

Several options and [output selectors](#output-selectors) make use of Aquarium's time annotations to allow filtering of games based on minimum clock conditions. These features are chiefly intended to assist with engines that have faulty time management, and are particularly useful in conjunction with Aquarium's "No flag loss" option in the engine-tournament interface. In games where this option was set, pgnutil can be used to search and report on conditions where a player's clock went negative.

The clock-below ("-cb") option takes a parameter (formatted as either integer seconds or as "h:mm:ss") to select games where at least one player's clock fell below the indicated value. For example,

``pgnutil -cb -0:01:10 -i mygames.pgn``

will return every game from the file mygames.pgn wherein at least one player's clock fell below negative one minute and ten seconds.

Conversely, we can use the clock-not-below ("-cnb") option to return every game wherein neither player's clock fell below the same threshold:

``pgnutil -cnb -70 -i mygames.pgn``

In conjunction with the "-cb" option, the "cbplayers" [output selector](#output-selectors) allows us to discover the offending players:

``pgnutil -cb -70 -s cbplayers -i mygames.pgn``

If we further wish to know the lowest clock value for each player in these games, we can add the "lowclockwhite" and "lowclockblack" [output selectors](#output-selectors):

``pgnutil -cb -70 -s cbplayers,lowclockwhite,lowclockblack -i mygames.pgn``


## Known Issues

Pgnutil is victim to the PGN specification's greatest infirmity: lack of standardized move notation.  PGN files are allowed to list moves in any way at all.  This means, for example, that "Bb2," "c1-b2," and "B-QN2" are all valid notation.  Pgnutil makes no effort to normalize moves (as this would entail a large performance penalty), so all move comparisons, such as the "-mo" and "-ro" options, as well as duplicate finding and opening statistics, assume that notation throughout the input file is consistent.  The various ECO-code and position-finding options require that the input file be in Standard Algebraic Notation.

PGN files often violate the PGN specification. Where it can, pgnutil refrains from complaining about such errors (for example, it does not demand a correct Seven Tag Roster).  Certain problems, however, will cause an error to be reported.  Kingbase, for instance, uses the backslash ("\\") character in some event names.  Since the PGN specification defines this as an escape character, it may cause certain tag-value pairs to be unterminated, resulting in an exception.

Also, the PGN spec permits "%"-style comments.  I have never seen these used, so they are unsupported.
