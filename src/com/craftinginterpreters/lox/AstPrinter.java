package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Variable;

/* Defines an AST printing operation that applies across all subclass types of Expr
 * Even if that operation needs to be different for every subclass, those differences are defined here.
 * No print operation specific changes are needed in each subclass
 * - As this implements Expr.Visitor, you see below that this had to implement a visitBinaryExpr() method.
 * - visitBinaryExpr() defines for that one subclass the operation that will be done to it
 * - Each Expr subclass defines an accept() method that takes an instance of this.
 * - An Expr.Binary's accept(this) method is called
 * - the implementation of accept() in the E.B instance thus has a reference to this
 * - E.B, in its accept() implementation calls this's visitBinaryExpr() method, passing itself (E.B) back to this
 * - this does whatever visitBinaryExpr() says to do to E.B.
 * 
 * * some operation on some type is defined in a doer class that implements Visitor
 * * there is a method in that doer class describing the thing to be done to that some type
 * * that some type implements an accept method that takes a Visitor type (the doer class)
 * * when that some type's accept method is called, that some type has a reference to the Visitor/doer class
 * * that some type's accept() method calls doer's corresponding some type method (Visitor methods are named after the some types), 
 * 	   passing a reference to the some type
 * * doer then has a reference to the some type instance and does the type appropriate operation on it.
 * 
 * In this case ....
 * > AstPrinter implements Expr.Visitor
 * > Expr.Visitor interface requires the visitBinaryExpr() method
 * > visitBinaryExpr() can grab Expr.Binary data members and prints them out "pretty-printed"
 * > Expr.Binary instance's accept method is called, passing in this AstPrinter (as a Visitor type)
 * > E.B takes that reference to the Visitor and calls "its" method on it - visitBinaryExpr() passing a reference to itself back
 * > AstPrinters' visitBinaryExpr method than does the printing thing with E.B's data.
 * 
 * "To E.B., here, accept this thing that will do something to you"
 * "Reply to AstPrinter, sweet, do it to me, here is me back at you"
 * "AstPrinter .. I have you, now I do the thing to you.
 * All the information about the doer and the type-specific things to be done are in the doer class.
 * No specifics about what will be done are in the E.Bs
 * E.Bs just have an accept() method that takes a reference to the Visitor interface.
 * Any number of doers, each doing different things can be defined, thus creating the ability to newly define many cross-type operations
 * without touching those types. 
 */

public class AstPrinter implements Expr.Visitor<String> {
	
	String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.value == null) return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}
	
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		builder.append(")");
		
		return builder.toString();
	}
	
	
	/*
	 * Create an instance of an Expr.Binary AST
	 * that looks like .... (* (- 123) (group 45.67))
	 * Create an instance of this AstPrinter class and call its print method for the expression.
	 */
	
	
	public static void main(String[] args) {
		Expr expression = new Expr.Binary(//binary expression: left, operator, right
			new Expr.Unary(//this is left for binary
					new Token(TokenType.MINUS, "-", null, 1),
					new Expr.Literal(123)),
			new Token(TokenType.STAR, "*", null, 1),//operator for binary
			new Expr.Grouping(//this is right for binary
					new Expr.Literal(45.67)));
		
		System.out.println(new AstPrinter().print(expression));					
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		return parenthesize(expr.name.lexeme);
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}
}
