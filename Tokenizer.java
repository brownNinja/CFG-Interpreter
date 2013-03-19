import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;

public class Tokenizer{
	
	private static InputStreamReader in;
	private static HashMap<String,Integer> LookupTbl = new HashMap<String,Integer>(35);
	private static String[] TokenList;
	private static int outputPos;
	private static int inputpos;
	private static String specialToken = "";
	//Constructor
	public Tokenizer(InputStreamReader f){
		in = f;
		TokenList = new String[800];
		outputPos = 0;
		inputpos = 0;
		UpdateTbl();
		BuildTokenArray();
	}
	
	/*Populates the HashMap LookupTbl with all of the reserved words and special
	 * symbols and their corresponding token numbers 
	 */
	private static void UpdateTbl(){
		int pos = 1;
		LookupTbl.put("program", pos);
		pos++;
		LookupTbl.put("begin", pos);
		pos++;
		LookupTbl.put("end", pos);
		pos++;
		LookupTbl.put("int", pos);
		pos++;
		LookupTbl.put("if", pos);
		pos++;
		LookupTbl.put("then", pos);
		pos++;
		LookupTbl.put("else", pos);
		pos++;
		LookupTbl.put("while", pos);
		pos++;
		LookupTbl.put("loop", pos);
		pos++;
		LookupTbl.put("read", pos);
		pos++;
		LookupTbl.put("write", pos);
		pos++;
		LookupTbl.put(";", pos);
		pos++;
		LookupTbl.put(",", pos);
		pos++;
		LookupTbl.put("=", pos);
		pos++;
		LookupTbl.put("!", pos);
		pos++;
		LookupTbl.put("[", pos);
		pos++;
		LookupTbl.put("]", pos);
		pos++;
		LookupTbl.put("&&", pos);
		pos++;
		LookupTbl.put("||", pos);
		pos++;
		LookupTbl.put("(", pos);
		pos++;
		LookupTbl.put(")", pos);
		pos++;
		LookupTbl.put("+", pos);
		pos++;
		LookupTbl.put("-", pos);
		pos++;
		LookupTbl.put("*", pos);
		pos++;
		LookupTbl.put("!=", pos);
		pos++;
		LookupTbl.put("==", pos);
		pos++;
		LookupTbl.put("<", pos);
		pos++;
		LookupTbl.put(">", pos);
		pos++;
		LookupTbl.put("<=", pos);
		pos++;
		LookupTbl.put(">=", pos);
		pos++;
		LookupTbl.put("[(", pos);
		pos++;
		LookupTbl.put(")]", pos);
		pos++;
		
	}
	//gets the next token, returns the token if available otherwise returns empty string;
	public String GetNext(){
		String nextToken;
		if(outputPos < inputpos){
		nextToken = TokenList[outputPos];
		outputPos++;
		}else{
			nextToken = "$$END";
		}
		return nextToken;
	}
	//returns 0 if successful, otherwise returns -1 if no more tokens left
	public int SkipToken(){
		if(outputPos < inputpos){
			outputPos++;
			return 0;
		}else
		{
			return -1;
		}
	}
	public String GoBack(){
		String prevToken = "";
		if(outputPos > 0){
			outputPos--;
			prevToken = TokenList[outputPos];
		}
		return prevToken;
	}
	
	private static void BuildTokenArray(){
		
		int data = 0;
		char c;
		String token = "";
		try{
	    data = in.read();
		while(data != -1){
			c = (char)data;
			//checks to see if the token being created is an ID
			//TODO possible that uppercase will return true on whitespace?
			if(Character.isUpperCase(c)){
				if(specialToken != ""){
					TokenList[inputpos]= specialToken;
					inputpos++;
					specialToken = "";
				}
				//errors if a token that is not an id contains uppercase letters
				if (!token.equals("") ){
					System.err.println("Invalid Token. Non IDs cannot have Upper Case letters");
					System.exit(0);
				}
				//Reads until the character read is a special token or a newline or a whitespace
				while( (IsToken(Character.toString(c)) != 1) && (c!= ' ') && (c!= '\n') && (c!= '\r') && (data != -1)){
				//errors if the id contains lower case letters
				if(Character.isLowerCase(c)){
					System.err.println("Invalid Token. IDs Cannot have Lower Case letters");
					System.exit(0);
				}
				token = token.concat(Character.toString(c));
				data = in.read();
				c = (char)data;
				}
				//outputs the correct token number
				//adds the token to the list of tokens
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
			}
			//checks to see if the token is a number
			if(Character.isDigit(c)){
				if(specialToken != ""){
					TokenList[inputpos]= specialToken;
					inputpos++;
					specialToken = "";
				}
				//errors if a non id contains a number
				if (!token.equals("")){
					System.err.println("Invalid Token. Only IDs and numbers may have integers");
					System.exit(0);
				}
				//builds the token until a special character, newline, or whitespace is reached
				while( (IsToken(Character.toString(c)) != 1) && (c!= ' ') && (c!= '\n') && (c!= '\r') && (data != -1)){
					
					//errors if a number contains non numbers
					if(!Character.isDigit(c)){
						System.err.println("Invalid Token. Numbers cannot have non-integers");
						System.exit(0);
					}
					token = token.concat(Character.toString(c));
					data = in.read();
					c = (char)data;
				}
					//prints the correct token number
					//adds the token to the list of tokens
					TokenList[inputpos]= token;
					inputpos++;
					token = "";
				
			}
			//adds character read to the token after being checked for ints and uppercase
			token = token.concat(Character.toString(c));
			//checks to see if the token is a reserved word
			if(IsToken(token) == 0){
				if(specialToken != ""){
					TokenList[inputpos]= specialToken;
					inputpos++;
					specialToken = "";
				}
				TokenList[inputpos]= token;
				inputpos++;
				token = "";
			}
			//checks to see if the token is a special token
			else if(IsToken(token)==1){
				
				/*If the token is a special token, see if the previous token was
				 * also a special token then add it in
				 */
				if(specialToken != ""){
					specialToken = specialToken.concat(Character.toString(c));
					if(IsToken(specialToken)==1){
						if(specialToken.equals("[(") || specialToken.equals(")]")){
							token = "";
							token = specialToken.substring(0,1);
							TokenList[inputpos]= token;
							inputpos++;
							token = "";
							token = specialToken.substring(1);
							TokenList[inputpos]= token;
							inputpos++;
							token = "";
							specialToken = "";
						}else{
						TokenList[inputpos]= specialToken;
						inputpos++;
						token = "";
						specialToken = "";
						}
					}
				}else{
					specialToken = token;
					token = "";
				}
			}
			//checks for to see if token is a whitespace or newline
			else if(IsToken(token)==2 || c == '\n' || c == '\r'){
					if(!specialToken.equals("")){
						TokenList[inputpos]= specialToken;
						inputpos++;
						specialToken = "";
					}
				if(token.length() > 1){
					System.err.println("Invalid Token." + "\n");
					System.exit(0);
				}
				token = "";
			}
			data = in.read();
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		}
		
	/*checks to see if the given String is a Token is a token
	 * returns 0 if it is reserved word
	 * returns 1 if it is a special token
	 * returns 2 if it is a whitespace
	 * returns -1 if it is nothing
	 */
	private static int IsToken(String tok){
		//checks to see if the token is a reserved word or special token
		if(LookupTbl.containsKey(tok)){
			if(LookupTbl.get(tok)<= 11){
				return 0;
			}else{
				return 1;
			}
		}//checks to see if the token is a white space
		else if(tok.equals(" ")){
			return 2;
		}else{
			return -1;
		}
	}
}
