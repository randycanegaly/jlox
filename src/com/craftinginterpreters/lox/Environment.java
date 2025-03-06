package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	private final Map<String, Object> values = new HashMap<>();
	
	/**
	 * retrieves a value bound to a name string
	 * @param name, a Token, the actual retrieval from the HashMap is done using Token.lexeme, a string
	 * @return Object stored in the HashMap
	 */
	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}
		
		throw new RuntimeError(name, "Undefined variable'" + name.lexeme + ".");
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