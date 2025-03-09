package com.craftinginterpreters.lox;
import java.util.List;
import java.util.ArrayList;

import static com.craftinginterpreters.lox.TokenType.*;//"static" here allows future usage without having to say TokenType.____

public class Parser {
	private static class ParseError extends RuntimeException {}//empty inner class to just provide a specific exception
	
	private final List<Token> tokens;
	private int current = 0;//marker for where we are in tokens list
	
	//constructor
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	//Scanner emits tokens
	//Parser consumes a collection of tokens and emits syntax trees
	//Grammar .. program -> declaration* EOF ;
	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();//new instance of ArrayList<>, which implements List<>. Make the variable be of the 
		//interface type for flexibility
		while (!isAtEnd()) {//checks if current has run off the end of the list of tokens
			statements.add(declaration());//build syntax trees (bunch of statements)
		}
		
		return statements;
	}
	
	//below, a series of methods, each corresponding to a rule in the grammar ...
	
	private Expr expression() {
		return assignment();
	}
	
	private Stmt declaration() {
		try {
			if (match(VAR)) return varDeclaration();
			return statement();
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}
	
	//Grammar .. statement -> exprStmt
	//						| printStmt;
	private Stmt statement() {
		if (match(IF)) return ifStatement();
		if (match(PRINT)) return printStatement();
		if (match(LEFT_BRACE)) return new Stmt.Block(block());
		
		return expressionStatement();
	}
	
	private Stmt ifStatement() {
		consume (LEFT_PAREN, "Expect '(' after 'if'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after if condition.");
		
		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if (match(ELSE)) {
			elseBranch = statement();
		}
		
		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	//Grammar .. printStmt -> "print" expression ";"
	private Stmt printStatement() {
		//already matched PRINT, so know we're in a print statement
		//Just need to emit an abstract syntax tree for expression
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");//check for ; and advance current
		return new Stmt.Print(value);//the side-effect that makes printStatement a statement is to print out its value
	}
	
	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER, "Expect variable name.");
		
		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}
		
		consume(SEMICOLON, "Expect ';' after value.");//check for ; and advance current
		return new Stmt.Var(name, initializer);
	}
	
	//Grammar .. exprStmt -> expression ";"
	private Stmt expressionStatement() {
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}
	
	/**
	 * Create list of Asts for what's inside of a block 
	 * @return
	 */
	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();
		
		while(!check(RIGHT_BRACE) && !isAtEnd()) {
			statements.add(declaration());
		}
		
		consume(RIGHT_BRACE, "Expect '}' after block.");
		return statements;
	}
	
	/* CE page 125, "little trick" to compensate for only being able to look one token ahead ....
	 * 
	 * The trick is that right before we create the assignment expression node,
	 * we look at the left-hand side expression and figure out what kind of assignment target it is.
	 * 	"look at" means that calling expression() below will call all successively higher precedence
	 * 	Ast generating methods (we are following the grammar) until we "match" something.
	 * 	In this case we match at IDENTIFIER in primary() because the left-and thing is an IDENTIFIER
	 * 	that intends to indicate/say "go find the storage location of this IDENTIFIER because we 
	 * 	want to assign this new value to that identifier and store it there
	 * 		primary(), for an identifier returns ... return new Expr.Variable(previous()), a Variable
	 * 		WE FOUND AN IDENTIFER, KNOW WE ARE SUPPOSED TO BE ASSIGNING, AND NOW HAVE AHOLD OF A VARIABLE TYPE
	 * We convert the r-value expression node into an l-value representation.
	 * 	We just treated expr like an r-value thingy, we evaluated it. We did that to figure out what it
	 * 	was and to confirm it was a thing we wanted to assign to and expr refers to a Variable Ast type
	 * We have to cast expr to the Expr.Variable subclass and then create a Token with Variable name in expr 
	 * That makes it now an l-value, we can assign to it --> create new Expr.Assign Ast
	
	/*
	expression 	-> assignment;
	assignment 	-> IDENTIFIER "=" assignment
				| logic_or ; //Grammar goes top to bottom, lowest precedence at top. If don't see IDENTIFIER then it must be next highest precedence .. OR
	logic_or	-> logic_and ( "or" logic_and )* ; //<something> or <something>. The <something> first could be a logic_and, but could also be any of the higher precedence grammar constructions. So anything or anything
	logic_and	-> equality ( "and" equality )* ;//Same logic as above ... could be any higher precedence Ast	
	 */

	private Expr assignment() {
		Expr expr = or();
		/*the case where we didn't see '=' and so it is some other expression of higher precedence than assignment
		* likely will be the Variable case of primary() where IDENTIFIER was seen
		* so expr probably points to an Expr.Variable - see above
		* 	match() calls check(), which peek()s and checks for a matching type
		* 	peek() gets the current token
		* 	So ... get the current token, see if its type matches EQUAL
		*/
		if (match(EQUAL)) { //see the EQUAL identifier, meaning we are in an assignment expression (assignment returns a value, so is an expression) 
			Token equals = previous();//match() does an advance(), so we have to backup one
			Expr value = assignment();//the thing we want assigned, our r-value
			
			if (expr instanceof Expr.Variable) {//is it an Expr.Variable? this check lets us know we want to assign to it --> see above comment block
				Token name = ((Expr.Variable)expr).name;//have to cast it because expr from above is an Expr base class type. 
				//line above converts to/creates an l-value that can be assigned to with a name
				return new Expr.Assign(name, value);//make the assign of the r-value thing to the l-value thing
			}
			
			error(equals, "Invalid assignment target.");
		}
		
		return expr;
	}
	
	private Expr or() {
		Expr expr = and();
		
		while (match(OR)) {//can have a bunch or ORs in series, process them left to right
			Token operator = previous();//match would have walked to next one
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}
		
		return expr;
	}

	private Expr and() {
		Expr expr = equality();//start out assuming its just one, no following ANDed, create the 'left' Ast
		
		while (match(AND)) {//but I do see at least one AND, loop to see more if there's a series of them
			Token operator = previous();
			Expr right = equality();//create the 'right' Ast
			expr = new Expr.Logical(expr, operator, expr);//'reset' the Expr to return to be one made up of the original 'left' above, operator and then the 'right'
		}
		
		return expr;
	
	}

	private Expr equality() {
		Expr expr = comparison();
		while (match(BANG_EQUAL, EQUAL_EQUAL)) {//import static from above at work here
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}

	private Expr comparison() {
		Expr expr = term();
		
		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous(); //match would have advanced current by one past the operator seen
			Expr right = term();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}
	
	private Expr term() {
		Expr expr = factor();
		
		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}

	private Expr factor() {
		Expr expr = unary();
		
		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}
		
		return expr;
	}
		
	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}
		
		return primary();
	}
	
	private Expr primary() {
		if (match(FALSE)) return new Expr.Literal(false);
		if (match(TRUE)) return new Expr.Literal(true);
		if (match(NIL)) return new Expr.Literal(null);
		
		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().literal);
		}
	
		if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
			
		}
		
		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}
		
		throw error(peek(), " Expect expression.");//descended all the way through the grammar and even here at highest precedence, 
		//have no match for the current token 
	}
		
	private boolean match(TokenType... types) {//variable args, can take a comma separated list of token types
		for (TokenType type : types) {
			if (check(type)) {//is the current token the same as type?
				advance();//then move current to the next token
				return true;//did match
			}
		}
		return false;//didn't match
	}
	
	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();//pass in ')', does it match current token? If so, OK.
		
		throw error(peek(), message);//didn't find ')', we have a problem
	}
	
	//does the type of the current token match the type passed in?
	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}
	
	//move to the next token and return the prior value at current
	private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}
	
	private boolean isAtEnd() {
		return peek().type == EOF;
	}
	
	//get the value of the current token
	private Token peek() {
		return tokens.get(current);
	}
	
	//look back one token and get that value from the list of tokens
	private Token previous() {
		return tokens.get(current-1);
	}
	
	//report an error
	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}
	
	//this will be called where ParserError is caught, when we are working with statements, at a statement boundary, not coded yet
	private void synchronize() {
		advance();
		while (!isAtEnd()) {
			if (previous().type == SEMICOLON) return;//just saw a ';', we know we are at a statement boundary
			
			switch(peek().type) {
			case CLASS: case FOR: case FUN: case IF: case PRINT: case RETURN: case VAR: case WHILE:
				return;
			}
			
			advance();
		}
	}
}
