package com.craftinginterpreters.lox;
import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;//"static" here allows future usage without having to say TokenType.____

public class Parser {
	private static class ParseError extends RuntimeException {}//empty inner class to just provide a specific exception
	
	private final List<Token> tokens;
	private int current = 0;//marker for where we are in tokens list
	
	//constructor
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	Expr parse() {
		try {
			return expression();
		} catch (ParseError error) {
			return null;
		}
	}
	
	//below, a series of methods, each corresponding to a rule in the grammar ...
	private Expr expression() {
		return equality();
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
