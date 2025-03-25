package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
	final String name;
	private final Map<String, LoxFunction> methods;
	
	LoxClass(String name, Map<String, LoxFunction> methods) {
		this.name = name;
		this.methods = methods;
	}

	LoxFunction findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}
		
		return null;
	}
	
	
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * To be able call on a Class
	 * @param interpreter
	 * @param arguments
	 * @return LoxClass instance
	 */
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);//instance has a reference to the class because the class holds behavior, instance holds state
		return instance;
	}

	@Override
	public int arity() {
		return 0;
	}
}
