package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();

	/**
	 *Null constructor. Environment for the global scope.
	 */
	Environment() {
		enclosing = null;
	}
	
	/*
	 * Constructor for inner environments, with reference to its enclosing environment.
	 * @param enclosing - the next, outer environment
	 */
	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}
	/**
	 * retrieves a value bound to a name string
	 * @param name, a Token, the actual retrieval from the HashMap is done using Token.lexeme, a string
	 * @return Object stored in the HashMap
	 */
	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}
		
		//need to search the environment chain if the name was not found "here"
		if (enclosing != null) return enclosing.get(name);//this will be recursive, if not found in enclosing, then enclosing enclosing will be searched
		
		throw new RuntimeError(name, "Undefined variable'" + name.lexeme + ".");
	}
	
	/**
	 * puts a new name/object key/value pair in the values Map. It "remembers" it.
	 * @param name, a Token. Token is passed in, but the actual key in values is token.lexeme string
	 * @param value, the object that is being bound to the name
	 */
	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme) ) {
			values.put(name.lexeme, value);
			return;
		}
		
		if (enclosing != null) {
			enclosing.assign(name, value);//this will be recursive, if not found in enclosing, then enclosing enclosing will be searched
 
			return;
		}
		
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
	
	/**
	 * Creates a binding between a name and a value
	 * @param name
	 * @param value
	 */
	void define(String name, Object value) {
		values.put(name, value);
	}
}

/*
expression -> assignment;
assignment -> IDENTIFIER "=" assignment
			| equality ;
*/

/*
statement	->	exprStmt
			|	printStmt
			| 	block;
			
block 		->	"{" declaration* "}" //inside the braces, can be anything that makes up a program
			
			


*/