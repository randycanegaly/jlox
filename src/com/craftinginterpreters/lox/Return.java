package com.craftinginterpreters.lox;

class ReturnException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	final Object value;
	
	ReturnException (Object value) {
		super(null, null, false, false);
		this.value = value;
	}
}
