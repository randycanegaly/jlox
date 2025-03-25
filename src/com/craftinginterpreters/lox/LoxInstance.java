package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;


public class LoxInstance {
	private LoxClass klass;
	private final Map<String, Object> fields = new HashMap<>();//this is the instance's state
	
	LoxInstance(LoxClass klass) {
		this.klass = klass;
	}
	
	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}
		
		LoxFunction method = klass.findMethod(name.lexeme);//couldn't find a field by that name, look for a method by that name in the instance's class
		if (method != null) return method.bind(this);
		
		throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
	}
	
	void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}
	
	@Override
	public String toString() {
		return klass.name + " instance.";
	}
}
