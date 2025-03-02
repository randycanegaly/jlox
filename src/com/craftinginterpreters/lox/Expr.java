package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expr {

	//Declare an abstract method that takes Visitor type. All subclasses of baseName implement this method. 
	abstract <R> R accept(Visitor<R> visitor);

	/* 	The Visitor interface defines a set of methods, one for each type
   		such that some action can be enabled across all types, minimizing in-type edits to implement the methods. */
	interface Visitor<R> {//visit method prototypes for each type. Using generics.
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
 	}

	//Binary
	static class Binary extends Expr {
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		final Expr left;
		final  Token operator;
		final  Expr right;
	}

	//Grouping
	static class Grouping extends Expr {
		Grouping(Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		final Expr expression;
	}

	//Literal
	static class Literal extends Expr {
		Literal(Object value) {
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		final Object value;
	}

	//Unary
	static class Unary extends Expr {
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		final Token operator;
		final  Expr right;
	}
}
