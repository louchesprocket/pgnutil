Description

Pgnutil is a command-line tool for performing various operations
on Portable Game Notation (PGN) files.  I wrote the tool because
I like to run computer-chess tournaments for my own amusement, and
found that calculating results was a nightmare due to a lack of very
basic capabilities for handling PGN files.  Common problems such
as finding duplicate games, identifying time forfeitures, and even
just figuring out if I had already played a particular pair of
players against a particular set of test positions were nearly
insoluble.  After failing to find any applicable tool on the
Internet (a fact that still astounds me), I wrote pgnutil.

Pgnutil works as a Unix-style command-line filter that performs
matching/selecting and replacing functions on PGN files in conjunction
with various output options.  By default, pgnutil simply outputs
selected games after performing any stipulated replacement operations,
but it can instead output user-selected lists of fields.  In addition,
pgnutil has three "special" output options: duplicate finding, event
listing, and opening statistics.  Pgnutil is particularly designed to
be used in conjunction with tools such as bayeselo or elostat.


Requirements

pgnutil requires Java 1.8 or higher.  It theoretically runs on any
platform with a command line.


Usage

For a complete list of options:

	java -jar pgnutil.jar -h

Pgnutil's most basic matching option is "-m", which takes a regular
expression as its parameter and matches against the entire text of
each game in the file.  For example, the following search prints
every game containing the word "forfeit" from the file mygames.pgn:

	java -jar pgnutil.jar -m 'forfeit' -i mygames.pgn

By default, pgnutil outputs the entire text of each matched game.
So in the previous example, we would have to sort through the output
to discover which players forfeited.  Much better to save ourselves
the trouble by combining the "-s" option, and perhaps piping the
output to another Unix command to produce a list of unique names of
forfeiters:

	java -jar pgnutil.jar -m 'forfeit' -s 'loser' -i mygames.pgn | sort -u

If we only want to get a list of players that have forfeited in a
game involving some version of Stockfish, we can combine the "-mp"
option, which matches any player name (either black or white):

	java -jar pgnutil.jar -mp 'Stockfish' -m 'forfeit' -s 'loser' -i mygames.pgn | sort -u

If we don't want to see Stockfish itself in this list (which would
happen if Stockfish forfeited any games), we can use the "-mw"
(match winner) option to omit cases where Stockfish lost:

	java -jar pgnutil.jar -m 'forfeit' -mw 'Stockfish' -s 'loser' -i mygames.pgn | sort -u

To list every game that Stockfish drew, we can use the -mt ("match
tag") option:

	java -jar pgnutil.jar -mp 'Stockfish' -mt 'Result/1\/2-1\/2' -i mygames.pgn

The "-r" option performs replacements on the game text.  For example,
to replace every instance of 'Quazar' from the file mygames.pgn with
'Quazar 0.4 x64':

	java -jar pgnutil.jar -r '.*/Quazar/Quazar 0.4 x64' -i mygames.pgn

The first "/"-separated component in the parameter to the "-r"
option actually selects games upon which to perform the replace-
ment.  So the previous example means, "for every game containing
the regular expression '.*' (i.e., any game at all),  replace every
occurrence of 'Quazar' with 'Quazar 0.4 x64.'"  Similarly, the command:

	java -jar pgnutil.jar -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -i mygames.pgn

means, "for every game containing 'Nunn 1' or 'Noomen 2012,' replace
every occurrence of 'Quazar' with 'Quazar 0.4 x64.'"  The selec-
tivity of the replacement can be further refined with other op-
tions.  For example, the command:

	java -jar pgnutil.jar -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -rl 'Glaurung 2.0.1' -i mygames.pgn

uses the "-rl" (replace loser) option to mean, "for every game
containing 'Nunn 1' or 'Noomen 2012' that was lost by 'Glaurung 2.0.1,'
replace every occurrence of 'Quazar' with 'Quazar 0.4 x64.'" 
And, of course, any of these may be combined with any of the
various matching ond output-selection options:

	java -jar pgnutil.jar -m '[Bb]litz' -r '(Nunn 1)|(Noomen 2012)/Quazar/Quazar 0.4 x64' -rl 'Glaurung 2.0.1' -s 'Event' -i mygames.pgn

means, "output the value of the 'Event' tag for every game
containing 'Blitz' or 'blitz,' but of these games, for every game
containing 'Nunn 1' or 'Noomen 2012' that was lost by 'Glaurung
2.0.1,' replace every occurrence of 'Quazar' with 'Quazar 0.4 x64.'"

By default, pgnutil will output the full text of the game in re-
sponse to any search operation.  The "-s" option may be used to
restrict the output to a pipe-separated list of selected fields.
Selected fields may include any PGN tag.  There are also five
"special" tags recognized by the "-s" option:

	moves: causes pgnutil to print the game's move list
	opponent: when the "-mp" (match player) option is used,
		output the name of the other player
	gameno: causes pgnutil to print the game's position within
		the PGN file
	oid: causes pgnutil to print the game's opening identifier
		(see below)
	winner: causes pgnutil to print the name of the winner
	loser: causes pgnutil to print the name of the loser

By default, fields selected by the "-s" option appear on the output
separated by the pipe ("|") character.  If a different output-
delimiter is desired, this may be set with the "-od" (output-
delimiter) option.

The "special" output options are "-d" (duplicates), "-e" (events),
and "-o" (opening statistics).  Any of these may be combined with
any matching and replacing options (see above).

To find duplicate games (defined as games with the same players and
same move list) in the file mygames.pgn:

	java -jar pgnutil.jar -d -i mygames.pgn

The previous command will print a space-sparated list of game
numbers (indexed from the first game of the PGN file), with each set
of duplicates on a separate line.  For example, the output:

	1616 1617
	1622 1623 1710

means that game 1617 is a duplicate of 1616, and games 1623 and
1710 are duplicates of 1622.  To output games 1616 and 1617 from the
PGN file:

	java -jar pgnutil.jar -gn 1616,1617 -i mygames.pgn

To output games 1616-1623 and game 1710:

	java -jar pgnutil.jar -gn 1616-1623,1710 -i mygames.pgn


To list each event from the file mygames.pgn, along with the games
that belong to it:

	java -jar pgnutil.jar -e -i mygames.pgn

To list opening statistics for all games in mygames.pgn:

	java -jar pgnutil.jar -o -i mygames.pgn

This query may also be paramaterized.  For example:

	java -jar pgnutil.jar -o -cmin 100 -ldp -.1 -hdp .1 -hdraw .5 -i mygames.pgn

means, "print opening statistics for every opening represented by
at least 100 games where the difference in win percentage between
black and white is no greater than 10% and the draw percentage is
no greater than 50%."  The opening-statistics function has its own
set of output selectors:

	eco: the ECO code of the opening
	oid: the opening identifier; this may be used with the
		"match opening" ("-mo") and "replace opening"
		("-ro") options
	count: the count of games represented by this opening
	wwins: the number of white wins for this opening
	wwinpct: white wins, expressed as a percentage of all games
		having a result
	bwins: the number of black wins for this opening
	bwinpct: black wins, expressed as a percentage of all games
                having a result
	diff: the difference in wins between white and black
	diffpct: the different in wins between white and black,
		expressed as a percentage of all games having a
		result
	draws: the number of draws for this opening
	drawpct: the number of draws for this opening expressed as
		a percentage of all games having a result

So we can use the previous example to generate a list of opening
identifers (note addition of the "-s" option):

	java -jar pgnutil.jar -o -cmin 100 -ldp -.1 -hdp .1 -hdraw .5 -i -s 'oid' mygames.pgn

which may produce output such as:

	4748b62c5f943db4
	58042cacbd9498f9
	3ef5ae89557400b9
	5e8c715de2d30397

We can then feed these to a matching command ("-mo"):

	java -jar pgnutil.jar -mo 4748b62c5f943db4,58042cacbd9498f9,3ef5ae89557400b9,5e8c715de2d30397 -i mygames.pgn

to output those games wherein the selected openings were played.
If you wish to input a long list of opening identifiers, you may
instead use the "-of" (opening-file) option, which takes as its
parameter the file name of a file containing opening identifiers:

	java -jar pgnutil.jar -of myopeningsfile -i mygames.pgn

NOTE: pgnutil correctly identifies openings (to be precise, it
correctly identifies the "out-of-book" condition) for PGN files
produced by Arena and Aquarium.  Other types of PGN files have not
been tested.


Known Issues

Apart from the opening issue (see just above), pgnutil is also
victim to the PGN specification's greatest infirmity: lack of
standardized move notation.  PGN files are allowed to list moves
in any way at all.  This means, for example, that "Bb2," "c1-b2,"
and "B-QN2" are all valid notation.  Pgnutil makes no effort to
standardize moves (as this would entail a large performance penalty),
so all move comparisons, such as the "-mo" and "-ro" options, as well
as duplicate finding and opening statistics, assume that notation
throughout the input file is consistent.

Also, the PGN spec permits "%"-style comments.  I have never seen
these used, so they are unsupported.
