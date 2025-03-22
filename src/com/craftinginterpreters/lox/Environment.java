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
	 * binds a new value to an existing name in the environment
	 * @param name, a Token. Token is passed in, but the actual key in values is token.lexeme string
	 * @param value, the object that is being bound to the name
	 */
	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme) ) {//if it's there bind the new object to the name
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
	
	Environment ancestor(int distance) {
		Environment environment = this;//remember the environment I'm at now .. will be the 'old' one
		for (int i = 0; i < distance; i++) {
			environment = environment.enclosing;//walk up the environment chain distance jumps and return that environment
		}
		
		return environment;
	}
	
	
	Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);//get the environment at distance, get the value there based on the name
	}
	
	void assignAt(int distance, Token name, Object value) {
		ancestor(distance).values.put(name.lexeme, value);//get the environment at distance, put the value there based on the name 
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