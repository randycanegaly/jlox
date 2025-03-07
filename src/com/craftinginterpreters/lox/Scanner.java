package com.craftinginterpreters.lox;

import java.util. ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;
	
	private static final Map<String, TokenType> keywords;
	
	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}
	
	Scanner(String source) {
		this.source = source;
	}
	
	List<Token> scanTokens() {
		while(!isAtEnd()) {
			//We are at the beginning of the next lexeme
			start = current;//both start at 0, after each call to scanToken(), current points to the start of the next lexeme
			scanToken();//scan token will call advance(), maybe more than once, depending on the token seen
			//it will leave current at the start of the next lexeme
		}
		
		tokens.add(new Token(EOF, "", null, line));//add an end of token list indicator to the list
		return tokens;
	}
	
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	//big switch, figure out what token is seen, added to the tokens List
	private void scanToken() {
		char c = advance();
		switch (c) {
		case '(': addToken(LEFT_PAREN); break;
		case ')': addToken(RIGHT_PAREN); break;
		case '{': addToken(LEFT_BRACE); break;
		case '}': addToken(RIGHT_BRACE); break;
		case ',': addToken(COMMA); break;
		case '.': addToken(DOT); break;
		case '-': addToken(MINUS); break;
		case '+': addToken(PLUS); break;
		case ';': addToken(SEMICOLON); break;
		case '*': addToken(STAR); break;
		
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);//! can be a token or the start of a two character token,
			//advance would have grabbed the character at current and then incremented current by 1
			//so by the time we are here, c = '!' and current points to the next character. 
			//see if that next character matches what is needed for the two character token
			break;
		case '=':
			addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		case '/'://is this divide or the start of a comment??
			if (match('/')) {//do the match() thing to see if we have '//'
				//A comment goes until the end of the line
				//here, c = '/', current is at the character after c
				//match is going to increment current again so that current is at the character after '//'
				//peek just looks at what current points to, doesn't increment current
				while (peek() != '\n' && !isAtEnd()) advance();//we know we are in a comment. look, newline? if no, advance() and peek again
			} else {
				addToken(SLASH);//the character after '/' is not '/' so this is just divide
			}
			break;
			
		case ' ':
		case '\r':
		case '\t':
			//Ignore whitespace
			break;
		
		case '\n':
			line++;
			break;
		
		case '"': string(); break;//see a quote, it's a string, process it
			
		default:
			if (isDigit(c)) {
				number();
			} else if (isAlpha(c)) {//if we got to here then we saw nothing of the above and no digit, is it an alpha character?
				identifier();
			} else {
				Lox.error(line, "Unexpected character.");
			}
			break;
		}
	}
	
	private void identifier() {//c is an alpha character
		while (isAlphaNumeric(peek())) advance();//advance as long as characters seen are alphas
		
		String text = source.substring(start, current);//build up a string of characters seen
		TokenType type = keywords.get(text);//is the string seen in the map of keywords? If so, then we have a type for that keyword
		if (type == null) type = IDENTIFIER;//string wasn't in the map, it's an identifier
		addToken(type);
	}
	
	private void number() {//c is a digit, current is at the character after the digit
		while ( isDigit(peek())) advance();//look ahead 1 character, advance as long as that is a digit
		
		//look for fractional part
		if (peek() == '.' && isDigit(peekNext())) {//peek above saw not a digit. if it's a '.' and the one after that is a digit
			//Consume the '.'
			advance();
			
			while (isDigit(peek())) advance();//do the move ahead while seeing digits again thing
		}
		
		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}
	
	
	private void string() {
		while (peek() != '"' && !isAtEnd()) {//we're in a string, keep looking ahead
			//and advancing as long as no newline or ending quote
			if (peek() == '\n') line++;
			advance();
		}
		
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}
		
		advance(); //The closing ". if we got to here, have dropped out of the while loop
		//with nothing odd happening so after the advance, current will be pointing to end quote of the string
		
		//Trim the surrounding quotes.
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}
	
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;
		
		current++;
		return true;
	}
	
	private char peek() {
		if (isAtEnd()) return '\0';//???
		return source.charAt(current);
	}
	
	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';//???
		return source.charAt(current + 1);
	}
	
	private boolean isAlpha(char c) {
		return ( c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				c == '_';
	}
	
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
		
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private char advance() {
		return source.charAt(current++);
	}
	
	private void addToken(TokenType type) {
		addToken(type, null);
	}
	
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}	
}
