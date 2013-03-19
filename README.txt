README:
This program was created by Guhan Venguswamy.

Files contained:
1.Interpreter.java --> This program contains the printer and executor. It is what should be run forthe program to be executed

2.Tokenizer.java --> Contains the implementaion of the Tokenizer.

3.Parser.java --> Contains the implementation of the Parser

4.Readme.txt--> This file


How to run and compile this program:

This Interpreter can be run in one of 2 ways.

1.Because it is a java program and was created in eclipse, it can be run on the eclipse platform. Just take the java files from the submitted folder and add them to a java directory in eclipse. Then alter the run configurations to include the program file and the input file. Then run the Interpreter.java. This takes 2 arguments:
	1. Program file
	2. Input file

2.This program is also ported to work in stdsun using the java compiler.
   1.Run the java compiler on Parser.java, Tokenizer.java, and Interpreter.java with the following command line code:
		javac Parser.java
                javac Tokenizer.java
		javac Interpreter.java
   2.After the compilation just run the Interpreter with 2 arguments(the program file and input).
                java Interpreter "programfile" "inputfile"
   the output of this will be on the main screen and will print the program, then print the results of the program.