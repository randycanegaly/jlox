package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {

	//Declare an abstract method that takes Visitor type. All subclasses of baseName implement this method. 
	abstract <R> R accept(Visitor<R> visitor);

	/* 	The Visitor interface defines a set of methods, one for each type
   		such that some action can be enabled across all types, minimizing in-type edits to implement the methods. */
	interface Visitor<R> {//visit method prototypes for each type. Using generics.
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
 	}

	//Expression
	static class Expression extends Stmt {
		Expression(Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		final Expr expression;
	}

	//Print
	static class Print extends Stmt {
		Print(Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		final Expr expression;
	}

	//Var
	static class Var extends Stmt {
		Var(Token name, Expr intializer) {
			this.name = name;
			this.intializer = intializer;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		final Token name;
		final  Expr intializer;
	}
}
