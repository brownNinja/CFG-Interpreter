import java.io.InputStreamReader;
import java.util.HashMap;

public class Parser{

	private static InputStreamReader in;
	private static int PT[][] = new int[2000][6];
	//sets up the ID tables
	//used to determine the ID string(mostly for printing)
	public static HashMap<Integer,String>IdLookup = new HashMap<Integer,String>(50);
	//used to determine the lookup value for each ID(mostly for parsing)
	public static HashMap<String, Integer>ValueLookup = new HashMap<String, Integer>(50);
	//contains the map between the value of the ID and the lookup value for the ID(mostly for execution)
	public static HashMap <Integer,int[]>IdValues = new HashMap<Integer, int[]>(50);
	public static int Cursor = 1;
	private static int currIdNum = 0;
	private static Tokenizer tokens;
	
public Parser(InputStreamReader f){
		in = f;
		tokens = new Tokenizer(in);
		PT[Cursor][0]= 1;
		ParseProg();
		Cursor = 1;
	}

	
private static void ParseProg(){
	String tok = tokens.GetNext();
	if(!tok.equals("program")){
		System.err.println("ERROR: Program must start with word 'program'");
		System.exit(0);
	}
	Cursor = 1;
	//sets the alternate value to 0 because there is only one option
	PT[Cursor][1]= 0;
	//This sets the value associated with the parent node to 0 because there is not parent node here
	PT[Cursor][5] = -1;
	CreateLB();
	CreateMB();
	GoDownLB();
	ParseDeclSeq();
	GoUp();
	tok = tokens.GetNext();
	if(!tok.equals("begin")){
		System.err.println("ERROR: Statement block must start with word 'begin'");
		System.exit(0);
	}
	GoDownMB();
	ParseStmntSeq();
	GoUp();
}

//begin declaration parsing
//parses all of the declarations
private static void ParseDeclSeq(){
	PT[Cursor][0] = 2;
	//parses all of the declarations
	ParseDecl();
	String tok = tokens.GetNext();
	if(tok.equals("int")){
		tok = tokens.GoBack();
		//sets the alternate number to the second option
		PT[Cursor][1] = 1;
		//parses another DeclSeq
		ParseDeclSeq();
	}else if(tok.equals("begin")){
		//sets the alt number to the first option
		PT[Cursor][1] = 0;
		tokens.GoBack();
	}
}

//parses the declarations
private static void ParseDecl(){
	String tok = tokens.GetNext();
	if(!tok.equals("int")){
		System.err.println("ERROR: Declarations must start with word 'int'");
		System.exit(0);
	}
	tok = tokens.GetNext();
	//looks at all of the tokens until a ";"
	while(!tok.equals(";")){
		if(tok.equals(",")){
			tok = tokens.GetNext();
		}
		if(tok.equals("int") || tok.equals("begin")){
			System.err.println("ERROR: Declaration statements must end with a semicolon");
			System.exit(0);
		}
		int[] val = {0,0};
		//adds all of the declarations into the Id tables
		if(IdLookup.containsValue(tok)){
			System.err.println("ERROR: " + tok + " has already been declared");
			System.exit(0);
		}else{
			IdLookup.put(currIdNum, tok);
			ValueLookup.put(tok,currIdNum);
			IdValues.put(currIdNum, val);
			currIdNum++;
		}
		tok = tokens.GetNext();
	}
}
//end declaration parsing

//begin statement parsing

//parse the statement sequence
private static void ParseStmntSeq(){
	PT[Cursor][0] = 3;
	CreateLB();
	CreateMB();
	GoDownLB();
	ParseStmnt();
	GoUp();
	String tok = tokens.GetNext();
	if(!tok.equals("end") && !tok.equals("else")){
		//is of the form <stmt><stmntseq>
		PT[Cursor][1] = 1;
		tokens.GoBack();
		GoDownMB();
		ParseStmntSeq();
		GoUp();
	}else{
		//is of the form <stmnt>
		tokens.GoBack();
		PT[Cursor][1] = 0;
	}
}

private static void ParseStmnt(){
	PT[Cursor][0]=6;
	String tok = tokens.GetNext();
	if(ValueLookup.containsKey(tok)){
		PT[Cursor][1] = 0;
		CreateLB();
		GoDownLB();
		tokens.GoBack();
		ParseAssign();
	}else if(tok.equals("if")){
		PT[Cursor][1] = 1;
		CreateLB();
		GoDownLB();
		ParseIf();
	}else if(tok.equals("while")){
		PT[Cursor][1] = 2;
		CreateLB();
		GoDownLB();
		ParseLoop();
	}else if(tok.equals("read")){
		PT[Cursor][1] = 3;
		CreateLB();
		GoDownLB();
		ParseIn();
	}else if(tok.equals("write")){
		PT[Cursor][1] = 4;
		CreateLB();
		GoDownLB();
		ParseOut();
	}else{
		System.err.println("ERROR: Unknown Statement type");
		System.exit(0);
	}
	GoUp();
}

private static void ParseAssign(){
	//set the nodes to the Assign and 1st alternative
	String tok = "";
	PT[Cursor][0] = 7;
	PT[Cursor][1] = 0;
	//create the left and right subtrees
	CreateLB();
	CreateMB();
	GoDownLB();
	//parse id
	ParseId();
	GoUp();
	//ensure this is an Assign
	tok = tokens.GetNext();
	if(!tok.equals("=")){
		System.err.println("ERROR:Assignment requires an '=' sign");
		System.exit(0);
	}
	//go down right subtree and parse the expression
	GoDownMB();
	ParseExp();
	GoUp();
	tokens.SkipToken();
}

private static void ParseIf(){
	String tok = "";
	PT[Cursor][0]= 8;
	CreateLB();
	CreateMB();
	GoDownLB();
	ParseCond();
	GoUp();
	tok = tokens.GetNext();
	if(tok.equals("then")){
		GoDownMB();
		ParseStmntSeq();
		GoUp();
	}else{
		System.err.println("ERROR: Expecting a then statment for the if clause");
		System.exit(0);
	}
	tok = tokens.GetNext();
	//handle the else clause
	if(tok.equals("else")){
		//set the alt number to the second option
		PT[Cursor][1]=1;
		CreateRB();
		GoDownRB();
		//parse the stmnt seq
		ParseStmntSeq();
		GoUp();
		//check for the end statement
		tok = tokens.GetNext();
		if(!tok.equals("end")){
			System.err.println("ERROR: Expecting an 'end' for the if clause");
			System.exit(0);
		}else{
			tokens.SkipToken();
		}
	}else if(tok.equals("end")){
		//set the alt to option 1
		PT[Cursor][1] = 0;
		tokens.SkipToken();
	}else{
		System.err.println("ERROR: 'if' statment must end in an 'else' or 'end'");
		System.exit(0);
	}
}

private static void ParseLoop(){
	String tok = "";
	//set the node number and the alt number
	PT[Cursor][0]= 9;
	PT[Cursor][1] = 0;
	CreateLB();
	CreateMB();
	GoDownLB();
	//parse the conditional
	ParseCond();
	GoUp();
	//check to make sure that a loop token is given
	tok = tokens.GetNext();
	if(tok.equals("loop")){
		GoDownMB();
		ParseStmntSeq();
		GoUp();
	}else{
		System.err.println("ERROR: Expecting a 'loop' for the while clause");
		System.exit(0);
	}
	
	tok = tokens.GetNext();
	if(!tok.equals("end")){
		System.err.println("ERROR: Expecting an 'end' for the while clause");
		System.exit(0);
	}else{
		//skip the semicolon after the 'end'
		tokens.SkipToken();
	}
}

//parses the Input
private static void ParseIn(){
	PT[Cursor][0] = 10;
	PT[Cursor][1] = 0;
	CreateLB();
	GoDownLB();
	ParseIdList();
	GoUp();
 }

//Parses the Ouput
private static void ParseOut(){
	PT[Cursor][0] = 11;
	PT[Cursor][1] = 0;
	CreateLB();
	GoDownLB();
	ParseIdList();
	GoUp();
 }
	
private static void ParseIdList(){
	String tok = "";
	PT[Cursor][0] = 5;
	CreateLB();
	GoDownLB();
	ParseId();
	GoUp();
	tok = tokens.GetNext();
	if(tok.equals(",")){
		tokens.SkipToken();
	}
	if(!tok.equals(";")){
		PT[Cursor][1] = 1;
		tokens.GoBack();
		CreateRB();
		GoDownRB();
		ParseIdList();
		GoUp();
	}else{
		PT[Cursor][1] = 0;
	}
}

private static void ParseId(){
	String tok = tokens.GetNext();
	if(!ValueLookup.containsKey(tok)){
		System.err.println("ERROR: " + tok + " Undeclared");
		System.exit(0);
	}
	//puts in the node value to signify that it is an ID
	PT[Cursor][0] = 18;
	PT[Cursor][1] = 0;
	//puts in the lookup number so that the value can be determined later
	PT[Cursor][2] = ValueLookup.get(tok);
}

//parses the conditionals
private static void ParseCond(){
	String tok = tokens.GetNext();
	PT[Cursor][0] = 12;
	CreateLB();
	GoDownLB();
	if(tok.equals("(")){
		//parse the comparison conditionals
		PT[Cursor][1] = 0;
		ParseComp();
		GoUp();
	}else if (tok.equals("!")){
		//parse the not equals conditional
		PT[Cursor][1] = 1;
		ParseCond();
		GoUp();
	}else if (tok.equals("[")){
		ParseCond();
		GoUp();
		tok = tokens.GetNext();
		if(tok.equals("&&")){
			PT[Cursor][1] = 2;
		}else if (tok.equals("||")){
			PT[Cursor][1] = 3;
		}
		//parse the second part of the conditional
		CreateMB();
		GoDownMB();
		ParseCond();
		GoUp();
		//skip the last ]
		tokens.SkipToken();
	}
}
//parse the comparison operators
private static void ParseComp(){
	PT[Cursor][0] = 13;
	PT[Cursor][1] = 0;
	CreateLB();
	CreateMB();
	CreateRB();
	GoDownLB();
	//parse the first operation
	ParseOp();
	GoUp();
	GoDownMB();
	//parses the comparison operator
	ParseCompOp();
	GoUp();
	GoDownRB();
	//parse the second operation
	ParseOp();
	GoUp();
	//skips the finishing parenthesis
	tokens.SkipToken();
}
//parses all of the comparison operators
private static void ParseCompOp(){
	PT[Cursor][0] = 17;
	String tok = tokens.GetNext();
	if(tok.equals("!=")){
		PT[Cursor][1] = 0;
	}else if(tok.equals("==")){
		PT[Cursor][1] = 1;
	}else if(tok.equals("<")){
		PT[Cursor][1] = 2;
	}else if(tok.equals(">")){
		PT[Cursor][1] = 3;
	}else if(tok.equals("<=")){
		PT[Cursor][1] = 4;
	}else if(tok.equals(">=")){
		PT[Cursor][1] = 5;
	}else{
		System.err.println("ERROR: " + tok + " is an unkown operator");
		System.exit(0);
	}
}

private static void ParseOp(){
	PT[Cursor][0] = 16;
	String tok = tokens.GetNext();
	CreateLB();
	if(isInteger(tok)){//checks if token is an integer
		PT[Cursor][1] = 0;
		GoDownLB();
		PT[Cursor][0] = 20;
		PT[Cursor][1] = Integer.parseInt(tok);
		GoUp();
	}else if(ValueLookup.containsKey(tok)){//checks if token is an ID
		PT[Cursor][1] = 1;
		tokens.GoBack();
		GoDownLB();
		ParseId();
		GoUp();
	}else if(tok.equals("(")){//checks to see if it is an exp
		PT[Cursor][1] = 2;
		GoDownLB();
		ParseExp();
		GoUp();
		//skip the ending parenthesis
		tokens.SkipToken();
	}else{ // if none of those then error
		System.err.println("ERROR: " + tok + " Unknown");
		System.exit(0);
	}
}

private static void ParseExp(){
	PT[Cursor][0] = 14;
	String tok = "";
	CreateLB();
	GoDownLB();
	ParseFac();
	GoUp();
	tok = tokens.GetNext();
	if(tok.equals("+")){
		PT[Cursor][1] = 1;
		CreateMB();
		GoDownMB();
		ParseExp();
		GoUp();
	}else if(tok.equals("-")){
		PT[Cursor][1] = 2;
		CreateMB();
		GoDownMB();
		ParseExp();
		GoUp();
	}else{
		PT[Cursor][1] = 0;
		tokens.GoBack();
	}
}

//parses the factors
private static void ParseFac(){
	PT[Cursor][0] = 15;
	String tok = "";
	CreateLB();
	GoDownLB();
	ParseOp();
	GoUp();
	tok = tokens.GetNext();
	if(tok.equals("*")){//if it is <op>*<Fac> then add in new node
		PT[Cursor][1] = 1;
		CreateMB();
		GoDownMB();
		ParseFac();
		GoUp();
	}else{//otherwise this factor is finished
		PT[Cursor][1] = 0;
		tokens.GoBack();
	}
}

	
	
//the functions that are able to be used from this parser class.
public static int CurrNTNode(){
	return PT[Cursor][0];
}
public static int CurrAlt(){
	return PT[Cursor][1];
}
public static void GoDownLB(){
	Cursor = PT[Cursor][2];
}
public static void GoDownMB(){
	Cursor = PT[Cursor][3];
}
public static void GoDownRB(){
	Cursor = PT[Cursor][4];
}
private static void CreateLB(){
	//sets the LB value to the next available node
	int lbnode = NextAvaiNode();
	PT[Cursor][2] = lbnode;
	//sets the parent node value to where the cursor is now
	PT[lbnode][5] = Cursor;
}
private static void CreateMB(){
	//sets the MB value to the next available node
	int mbnode = NextAvaiNode();
	PT[Cursor][3] = mbnode;
	//sets the parent node value to where the cursor is now
	PT[mbnode][5] = Cursor;
}
private static void CreateRB(){
	//sets the RB value to the next available node
	int rbnode = NextAvaiNode();
	PT[Cursor][4] = rbnode;
	//sets the parent node value to where the cursor is now
	PT[rbnode][5] = Cursor;
}
public static void GoUp(){
	//gets the value associated with the parent node and puts the cursor there
	Cursor = PT[Cursor][5];
}
public static int CurrIdVal(){
	//checks to ensure that curr node is an id
	if(PT[Cursor][0]!= 18){
		System.err.println("This node is not an ID");
		System.exit(0);
	}
	//returns the value of the id
	if(IdValues.get(PT[Cursor][2])[1] != 1){
		System.err.println("This ID is not initialized");
		System.exit(0);
	}
	return IdValues.get(PT[Cursor][2])[0];
}
public static void SetIdVal(int x){
	//checks to ensure that curr node is an id
	if(PT[Cursor][0]!= 18){
		System.err.println("This node is not an ID");
		System.exit(0);
	}
	//sets the value of the ID
	int valArr[] = {x,1};
	IdValues.put(PT[Cursor][2], valArr);
}

public static String IdName(){
	//checks to ensure that curr node is an id
	if(PT[Cursor][0]!= 18){
		System.err.println("This node is not an ID");
	}
	//returns the String value of the ID
	return IdLookup.get(PT[Cursor][2]);
}
//resets the cursor so that it can be run through multiple times
public void ResetCursor(){
	Cursor = 1;
}
//gets the next available node so that it can be created
private static int NextAvaiNode(){
	int check = Cursor + 1;
	//determines if the node is free to use by checking to see if a parent node has been declared for it
	while(PT[check][5] != 0){
		check ++;
	}
	return check;
		
	}

//helper operation to determine if a string is an integer
private static boolean isInteger(String input) {
	try {
		Integer.parseInt(input);
		return true;
	} catch (NumberFormatException nFE) {
		return false;
	}
}
}