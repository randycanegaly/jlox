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
		LoxFunction initializer = findMethod("init");//create a runtime function object for the init() method
		if (initializer != null) {
			//bind creates a new environment that binds "this" to the LoxInstance
			//it then passes that environment as closure to a new LoxFunction and returns it
			//so that LoxFunction's environment chain as one environment where "this" is bound
			//we then call that function (init) passing the arguments from the call to the Class() method
			//thus arguments to Bagel() get passed to Bagel's init to do the the init things
			initializer.bind(instance).call(interpreter, arguments);
		}
		
		return instance;
	}

	@Override
	public int arity() {
		LoxFunction initializer = findMethod("init");
		if (initializer == null) return 0;//no initializer, no arguments
		return initializer.arity();//return init's number of arguments
	}
}
