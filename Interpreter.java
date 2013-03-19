import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("static-access")
public class Interpreter{
		
	private static Parser PT;
	//used to print out the declarations
	private static Tokenizer tokens;
	//tokenize the input file for ease of use
	private static Tokenizer inputF;
	
	//main procedure
    public static void main(final String[] args) throws IOException {
	//create the input stream reader
	InputStreamReader prog,input,toks;
	//checks to make sure there are the correct number of arguments
	if(args.length < 2){
	    System.err.println("Must have 2 Arguments");
	    System.exit(0);
	}
	try{
	    //take in the input file
	    FileInputStream instream = new FileInputStream(args[0]);
	    FileInputStream tokStream = new FileInputStream(args[0]);
	    //the input files
	    FileInputStream inputstream = new FileInputStream(args[1]);
	    prog = new InputStreamReader(instream);
	    toks = new InputStreamReader(tokStream);
	    input = new InputStreamReader(inputstream);
	    PT = new Parser(prog);
	    PT.ResetCursor();
	    //tokenize the input file for ease of use
	    tokens = new Tokenizer(toks);
	    //print program
	    PrintProg();
	    PT.ResetCursor();
	    inputF = new Tokenizer(input);
	    //execute the program
	    ExecProg();
	    //close the files
	    prog.close();
	    input.close();
	    toks.close();
	}catch (FileNotFoundException e) {
	    System.err.println("Error: file not found");
	    System.exit(0);
	}
    }
    
    //begin Printing section
	private static void PrintProg(){
    	String tok = "";
    	System.out.println("program");
    	//skips the first token
    	tokens.SkipToken();
    	tok = tokens.GetNext();
    	System.out.print("\t");
    	//prints the declaration section. This method was used because
    	//it is more efficient than making it part of the parse tree
    	while(!tok.equals("begin")){
    		if(tok.equals(";")){
    			System.out.println(tok);
    			if(!tokens.GetNext().equals("begin")){
    				System.out.print("\t");
    			}
    			tokens.GoBack();
    		}else{
    		System.out.print(tok + " ");
    		}
    		tok = tokens.GetNext();
    	}
    	System.out.println("begin");
    	System.out.print("\t");
    	//begin printing the StmntSeq
    	PT.GoDownMB();
    	PrintSS();
    	PT.GoUp();
    	System.out.println("end");
    }

	//print the statement sequence
	private static void PrintSS(){
		//if there is only one statment then print it
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			PrintStmnt();
			PT.GoUp();
		}else{//otherwise print the first statment and the other statement sequence
			PT.GoDownLB();
			PrintStmnt();
			PT.GoUp();
			PT.GoDownMB();
			PrintSS();
			PT.GoUp();
		}
	}
	
	private static void PrintStmnt(){
		//prints the statement.
		//This is basically a switch for the if while in out and assignment
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			PrintAssign();
			System.out.print("\t");
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){
			PT.GoDownLB();
			PrintIf();
			System.out.print("\t");
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){
			PT.GoDownLB();
			PrintLoop();
			System.out.print("\t");
			PT.GoUp();
		}else if(PT.CurrAlt() == 3){
			PT.GoDownLB();
			PrintIn();
			System.out.print("\t");
			PT.GoUp();
		}else if(PT.CurrAlt() == 4){
			PT.GoDownLB();
			PrintOut();
			System.out.print("\t");
			PT.GoUp();
		}
	}
	
	private static void PrintAssign(){
		//goes down left and prints the ID
		PT.GoDownLB();
		PrintId();
		PT.GoUp();
		//prints the '=' then goes down middle and prints the expression
		System.out.print(" = ");
		PT.GoDownMB();
		PrintExp();
		PT.GoUp();
		System.out.print(";");
		System.out.println("");
	}
	
	private static void PrintIf(){
		//prints the if and then goes down left and prints the condition
		System.out.print("if ");
		PT.GoDownLB();
		PrintCond();
		//prints the then then goes down middle and prints the StmntSeq
		System.out.println("then ");
		System.out.print("\t");
		PT.GoUp();
		PT.GoDownMB();
		PrintSS();
		PT.GoUp();
		//if there is an else clause then prints it
		if(PT.CurrAlt() == 1){
			System.out.println("else");
			System.out.print("\t");
			PT.GoDownRB();
			PrintSS();
			PT.GoUp();
		}
		System.out.println("end;");
	}
	
	private static void PrintLoop(){
		//the exact same as PrintIf except with no else clause
		System.out.print("while ");
		PT.GoDownLB();
		PrintCond();
		System.out.print("loop \n ");
		System.out.print("\t");
		PT.GoUp();
		PT.GoDownMB();
		PrintSS();
		PT.GoUp();
		System.out.println("end;");
	}
	
	private static void PrintIn(){
		//prints 'read' then the idlist
		System.out.print("read ");
		PT.GoDownLB();
		PrintIdList();
		PT.GoUp();
		System.out.print("; \n");
	}
	private static void PrintOut(){
		//prints 'write' then the id list
		System.out.print("write ");
		PT.GoDownLB();
		PrintIdList();
		PT.GoUp();
		System.out.print("; \n");
	}
	
	private static void PrintIdList(){
		//goes down left and prints the id
		PT.GoDownLB();
		PrintId();
		PT.GoUp();
		//checks if there are more in the list and prints them
		if(PT.CurrAlt() == 1){
			PT.GoDownMB();
			System.out.print(", ");
			PrintIdList();
			PT.GoUp();
		}else{
		}
	}
	
	private static void PrintCond(){
		//print the comparison if that is the conditional
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			PrintComp();
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){//print the not conditional
			System.out.print("!");
			PT.GoDownLB();
			PrintCond();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){//print the && conditional
			System.out.print("[");
			PT.GoDownLB();
			PrintCond();
			PT.GoUp();
			System.out.print("&&");
			PT.GoDownMB();
			PrintCond();
			PT.GoUp();
			System.out.print("]");
		}else if(PT.CurrAlt() == 3){//print the || conditional
			System.out.print("[");
			PT.GoDownLB();
			PrintCond();
			PT.GoUp();
			System.out.print("||");
			PT.GoDownMB();
			PrintCond();
			PT.GoUp();
			System.out.print("]");
		}
			System.out.print(" ");
	}
	
	//print the ID
	private static void PrintId(){
		System.out.print(PT.IdName());
	}
	
	private static void PrintComp(){
		System.out.print("(");
		PT.GoDownLB();
		PrintOp();
		PT.GoUp();
		PT.GoDownMB();
		if(PT.CurrAlt() == 0){
			System.out.print(" != ");
		}else if(PT.CurrAlt() == 1){
			System.out.print(" == ");
		}else if(PT.CurrAlt() == 2){
			System.out.print(" < ");
		}else if(PT.CurrAlt() == 3){
			System.out.print(" > ");
		}else if(PT.CurrAlt() == 4){
			System.out.print(" <= ");
		}else if(PT.CurrAlt() == 5){
			System.out.print(" >= ");
		}
		PT.GoUp();
		PT.GoDownRB();
		PrintOp();
		PT.GoUp();
		System.out.print(")");
	}
	
	//prints the expression
	private static void PrintExp(){
		//go down left and print the factor
		PT.GoDownLB();
		PrintFac();
		PT.GoUp();
		//if there is an addition or subtraction then print them and the exp
		if(PT.CurrAlt() == 1){
			System.out.print(" + ");
			PT.GoDownMB();
			PrintExp();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){
			System.out.print(" - ");
			PT.GoDownMB();
			PrintExp();
			PT.GoUp();
		}
	}
	
	//prints the factor
	private static void PrintFac(){
		PT.GoDownLB();
		PrintOp();
		PT.GoUp();
		//if there is a multiplication print it and call PrintFac
		if(PT.CurrAlt() == 1){
			System.out.print(" * ");
			PT.GoDownMB();
			PrintFac();
			PT.GoUp();
		}
	}

	//prints the Op
	private static void PrintOp(){
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			System.out.print(PT.CurrAlt());
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){
			PT.GoDownLB();
			PrintId();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){
			System.out.print("(");
			PT.GoDownLB();
			PrintExp();
			PT.GoUp();
			System.out.print(")");
		}
	}
	
	//end the printing section
	
	//begin the execution section
	
	private static void ExecProg(){
		PT.GoDownMB();
    	ExecSS();
    	PT.GoUp();
	}
	
	//Execute the StatementSeq
	private static void ExecSS(){
		if(PT.CurrNTNode()!= 3){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//if there is only one statment then Execute it
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			ExecStmnt();
			PT.GoUp();
		}else{//otherwise Exec the first statment and the other statement sequence
			PT.GoDownLB();
			ExecStmnt();
			PT.GoUp();
			PT.GoDownMB();
			ExecSS();
			PT.GoUp();
		}
	}
	
	//Execute the Statement
	private static void ExecStmnt(){
		if(PT.CurrNTNode() != 6){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//Executes the statement.
		//This is basically a switch for the if while in out and assignment
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			ExecAssign();
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){
			PT.GoDownLB();
			ExecIf();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){
			PT.GoDownLB();
			ExecLoop();
			PT.GoUp();
		}else if(PT.CurrAlt() == 3){
			PT.GoDownLB();
			ExecIn();
			PT.GoUp();
		}else if(PT.CurrAlt() == 4){
			PT.GoDownLB();
			ExecOut();
			PT.GoUp();
		}
	}
	
	private static void ExecAssign(){
		if(PT.CurrNTNode() != 7){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//Executes the expression to assign a value to the id
		PT.GoDownMB();
		int x = ExecExp();
		PT.GoUp();
		//goes down left and sets the value of the ID
		PT.GoDownLB();
		PT.SetIdVal(x);
		PT.GoUp();
	}
	
	//executes the If statment
	
	private static void ExecIf(){
		if(PT.CurrNTNode() != 8){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		int x = PT.CurrAlt();
		PT.GoDownLB();
		boolean y = ExecCond();
		PT.GoUp();
		if(x == 0){//no else statement
			//if the conditional is true
			if(y == true){
				PT.GoDownMB();
				ExecSS();
				PT.GoUp();
			}//if not do nothing
		}else if(x==1){//else statement
			if(y == true){//if the conditional is true
				PT.GoDownMB();
				ExecSS();
				PT.GoUp();
			}else{//if false then else statment
				PT.GoDownRB();
				ExecSS();
				PT.GoUp();
			}
		}
	}
	//execute the loop
	private static void ExecLoop(){
		if(PT.CurrNTNode() != 9){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//check the condition
		PT.GoDownLB();
		boolean y = ExecCond();
		PT.GoUp();
		if(y == true){//if it is true, execute the StmntSeq and recurse
			PT.GoDownMB();
			ExecSS();
			PT.GoUp();
			ExecLoop();
		}
	}
	
	//execute the read
	private static void ExecIn(){
		if(PT.CurrNTNode() != 10){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		PT.GoDownLB();
		ExecIdList(0);		
		PT.GoUp();
	}
	
	private static void ExecOut(){
		if(PT.CurrNTNode() != 11){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		PT.GoDownLB();
		ExecIdList(1);		
		PT.GoUp();
	}
	
	private static void ExecIdList(int num){
		if(PT.CurrNTNode() != 5){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//if num is 0 then the id list is taking a Read
		if(num == 0){
			//get the input value
			String tok = inputF.GetNext();
			if(tok.equals(",")){
				tok = inputF.GetNext();
			}else if(tok.equals("-")){
				String tok2 = inputF.GetNext();
				if(tok2.equals(",")){
					tok2 = inputF.GetNext();
				}
				tok = tok.concat(tok2);
			}
			int x = 0;
			if(isInteger(tok)){
			x = Integer.parseInt(tok);
			}else{
				System.err.print("ERROR: Cannot use a non integer input");
				System.exit(0);
			}
			
			PT.GoDownLB();
			PT.SetIdVal(x);
			PT.GoUp();
			if(PT.CurrAlt() == 1){//if there is not just an ID at this node
				PT.GoDownMB();
				ExecIdList(0);
				PT.GoUp();
			}
		}else if(num == 1){//if num is 1 then it is a write
			PT.GoDownLB();
			int x = PT.CurrIdVal();
			String id = PT.IdName();
			//write the ID value to the screen
			System.out.print(id + " = " + x + " \n");
			PT.GoUp();
			if(PT.CurrAlt() == 1){//if there is not just an ID at this node
				PT.GoDownMB();
				ExecIdList(1);
				PT.GoUp();
			}
		}
	}
	//execute the conditionals
	
	private static boolean ExecCond(){
		if(PT.CurrNTNode() != 12){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//execute the comparison if that is the conditional
		boolean result = false;
		if(PT.CurrAlt() == 0){
			PT.GoDownLB();
			result = ExecComp();
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){//execute the not conditional
			PT.GoDownLB();
			//computes the not conditional
			result = !ExecCond();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){//exec the && conditional
			boolean left, right;
			PT.GoDownLB();
			left = ExecCond();
			PT.GoUp();
			PT.GoDownMB();
			right = ExecCond();
			PT.GoUp();
			//computes the and conditional
			result = left && right;
		}else if(PT.CurrAlt() == 3){//exec the || conditional
			boolean left, right;
			PT.GoDownLB();
			left = ExecCond();
			PT.GoUp();
			PT.GoDownMB();
			right = ExecCond();
			PT.GoUp();
			//computes the or conditional
			result = left || right;
		}
		return result;
	}
	
	//execute the comparison statement
	private static boolean ExecComp(){
		if(PT.CurrNTNode() != 13){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		int left, right;
		boolean result = false;
		//get the result of the left op
		PT.GoDownLB();
		left = ExecOp();
		PT.GoUp();
		//get the result of the right op
		PT.GoDownRB();
		right = ExecOp();
		PT.GoUp();
		//determine what the operation is and compare them
		PT.GoDownMB();
		if(PT.CurrAlt() == 0){
			if(left != right){
				result = true;
			}
		}else if(PT.CurrAlt() == 1){
			if(left == right){
				result = true;
			}
		}else if(PT.CurrAlt() == 2){
			if(left < right){
				result = true;
			}
		}else if(PT.CurrAlt() == 3){
			if(left > right){
				result = true;
			}
		}else if(PT.CurrAlt() == 4){
			if(left <= right){
				result = true;
			}
		}else if(PT.CurrAlt() == 5){
			if(left >= right){
				result = true;
			}
		}else{
			System.err.print("Unknown Comparison Operation ");
		}
		PT.GoUp();
		return result;
	}
	
	//Executes the expression
	private static int ExecExp(){
		if(PT.CurrNTNode() != 14){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//go down left and execute the factor
		int left, right, result;
		result = 0;
		PT.GoDownLB();
		left = ExecFac();
		PT.GoUp();
		//if there is an addition or subtraction then execute them and the exp
		if(PT.CurrAlt() == 1){
			//add the two values together
			PT.GoDownMB();
			right = ExecExp();
			PT.GoUp();
			result = left + right;
		}else if(PT.CurrAlt() == 2){
			//subtract the values
			PT.GoDownMB();
			right = ExecExp();
			PT.GoUp();
			result = left - right;
		}else{
			//otherwise it is just the factor and there is not add or subtract
			result = left;
		}
		return result;
	}
	
	//Execute the factor
	private static int ExecFac(){
		if(PT.CurrNTNode() != 15){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		//go down left and execute the factor
		int left, right, result;
		result = 0;
		PT.GoDownLB();
		left = ExecOp();
		PT.GoUp();
		//if there is a multiply then execute them and the exp
		if(PT.CurrAlt() == 1){
			//times the two values together
			PT.GoDownMB();
			right = ExecFac();
			PT.GoUp();
			result = left * right;
		}else{
			//otherwise it is just the factor and there is not a times
			result = left;
		}
		return result;
	}
	//execute Op
	private static int ExecOp(){
		if(PT.CurrNTNode() != 16){
			System.err.print("ERROR: Not executing correct statement");
			System.exit(0);
		}
		int result = 0;
		if(PT.CurrAlt() == 0){//if the op is an int
			PT.GoDownLB();
			result = PT.CurrAlt();
			PT.GoUp();
		}else if(PT.CurrAlt() == 1){//if the op is an ID
			PT.GoDownLB();
			result = PT.CurrIdVal();
			PT.GoUp();
		}else if(PT.CurrAlt() == 2){//if the op is an expression
			PT.GoDownLB();
			result = ExecExp();
			PT.GoUp();
		}
		return result;
	}
	
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException nFE) {
			return false;
		}
	}
}
