package com.craftinginterpreters.lox;

import java.util.List;

public interface LoxCallable {
	/**
	 * Interface method declaration
	 * @param interpreter
	 * @param arguments
	 * @return
	 */
	Object call(Interpreter interpreter, List<Object> arguments);
	
	/**
	 * arity = the number of arguments expected
	 * @return arity
	 */
	int arity();
}
