package com.craftinginterpreters.lox;

import java.util.List;

/**
 * At runtime, we need a thing that holds a function object's parameters and body for use when the function is called
 * That looks a lot like Stmt.Function, but that's a front-end Parser used class. 
 * Don't want to muddy the boundary between parsing and interpreting.
 * So create this new runtime class and have it have/wrap a Stmt.Function
 */
public class LoxFunction implements LoxCallable{
	private final Stmt.Function declaration;
	
	
	/**
	 * Constructor
	 * @param declaration - the Stmt.Function to wrap
	 */
	LoxFunction(Stmt.Function declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(interpreter.globals);//function has its own environment - for local variable binding etc.
		for (int i = 0; i < declaration.params.size(); i++) {
			environment.define(declaration.params.get(i).lexeme, arguments.get(i));//bind arguments to parameter names
		}
		
		interpreter.executeBlock(declaration.body, environment);//passing the environment for this function allows execution of the block within that scope
		return null;
	}

	@Override
	public int arity() {
		return declaration.params.size();//when this function gets called, 
		//it will be asked for its arity so the caller can check that it is providing a matching number of arguments
	}
	
	@Override
	public String toString() {
		return "<fn " + declaration.name.lexeme + ">";
	}

}
