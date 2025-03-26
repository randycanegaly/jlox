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
	private final Environment closure;//put any data known at the time this function is declared. "close" around that data
	private final boolean isInitializer;
	
	/**
	 * Constructor
	 * @param declaration - the Stmt.Function to wrap
	 */
	LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
		this.isInitializer = isInitializer;
		this.closure = closure;
		this.declaration = declaration;
	}
	
	LoxFunction bind(LoxInstance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new LoxFunction(declaration, environment, isInitializer);
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment environment = new Environment(closure);
		//create an environment for the function call, passing the closure environment to be the parent environment
		//this way, the function object has access to any data declared at the time it is defined
		for (int i = 0; i < declaration.params.size(); i++) {
			environment.define(declaration.params.get(i).lexeme, arguments.get(i));//bind arguments to parameter names
		}
		
		try {
			interpreter.executeBlock(declaration.body, environment);//passing the environment for this function allows execution of the block within that scope
		} catch (ReturnException returnValue) {//not really an Exception, just a way to wind back to here, the caller upon function return
			//if we got to here and the function we called has returned and that function is an initializer, we want to make sure it returns 'this'
			if (isInitializer) return closure.getAt(0, "this");
			
			return returnValue.value;//if the block was a function body and had a return statement we catch the Expception's value and return it
		}
		
		if (isInitializer) return closure.getAt(0, "this");//if the function being called is an initializer, then we want 'this' to be returned no matter where it is called from
		
		return null;//if we got to here then the function never did a return statement so we just return null (nil)
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
