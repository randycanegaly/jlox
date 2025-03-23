package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expr {

	//Declare an abstract method that takes Visitor type. All subclasses of baseName implement this method. 
	abstract <R> R accept(Visitor<R> visitor);

	/* 	The Visitor interface defines a set of methods, one for each type
   		such that some action can be enabled across all types, minimizing in-type edits to implement the methods. */
	interface Visitor<R> {//visit method prototypes for each type. Using generics.
		R visitAssignExpr(Assign expr);
		R visitBinaryExpr(Binary expr);
		R visitCallExpr(Call expr);
		R visitGetExpr(Get expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitLogicalExpr(Logical expr);
		R visitSetExpr(Set expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
 	}

	//Assign
	static class Assign extends Expr {
		Assign(Token name, Expr value) {
			this.name = name;
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}

		final Token name;
		final  Expr value;
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

	//Call
	static class Call extends Expr {
		Call(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}

		final Expr callee;
		final  Token paren;
		final  List<Expr> arguments;
	}

	//Get
	static class Get extends Expr {
		Get(Expr object, Token name) {
			this.object = object;
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}

		final Expr object;
		final  Token name;
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

	//Logical
	static class Logical extends Expr {
		Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}

		final Expr left;
		final  Token operator;
		final  Expr right;
	}

	//Set
	static class Set extends Expr {
		Set(Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}

		final Expr object;
		final  Token name;
		final  Expr value;
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

	//Variable
	static class Variable extends Expr {
		Variable(Token name) {
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}

		final Token name;
	}
}
