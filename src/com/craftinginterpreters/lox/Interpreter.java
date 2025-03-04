package com.craftinginterpreters.lox;
import java.util.List;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Print;

public class Interpreter implements Expr.Visitor<Object>,
									Stmt.Visitor<Void> {
	
	void interpret (List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);//call each statement's accept() method, passing this (which is also a Stmt.Visitor type)
				//statement has to accept a bolus of Stmt.Visitor methods
				//statement then calls "its" Stmt.Visitor method, inflicting an operation on itself, like printing out a Stmt.Print's value
			}
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}
	 
	@Override
	public Object visitBinaryExpr(Binary expr) {//A Binary subclass instance of Expr will call this method to trigger the
		//operation defined here for its type. Binary instance will get an reference to 'this' when its accept() method is called.
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
			case MINUS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left - (double)right;
			case SLASH:
				checkNumberOperands(expr.operator, left, right);
				return (double)left / (double)right;
			case STAR:
				checkNumberOperands(expr.operator, left, right);
				return (double)left * (double)right;
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					return (double)left +  (double)right;
				}
				
				if (left instanceof String && right instanceof String) {
					return (String)left + (String)right;
				}
				
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
			case GREATER:
				checkNumberOperands(expr.operator, left, right);
				return (double)left > (double)right;  
			case GREATER_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left >= (double)right;  
			case LESS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left < (double)right;  
			case LESS_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left <= (double)right;  
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
		}
		return null;
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
		//(expression) - need to evaluate the expression inside the parentheses - get a value
		//so expression will be type of one of grouping, literal or unary
		//need to call whatever that expression's accept() method so that 'this' is handed to it
		//so that it can call back and use the appropriate visit*() method here.
		//the flow could be (-2), so that the expression inside the parentheses would be an Expr.Unary
		//so then, call Expr.Unary's accept() method, handing it 'this'
		//That Expr.Unary instance then calls visitUnaryExpr on the 'this that was passed to it.
		//Expr.Unary winds up asking to have the operation defined here inflicted upon it.
		//this is recursiony, could be expressions all the way down.
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
		case BANG:
			return !isTruthy(right);
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double) right;//we hit bottom, took Ast, tore it apart and calculated the actual value from all the pieces of it
		
		}
	
		return null;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}
	
	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;//operand is a good type
		throw new RuntimeError(operator, "Operand must be a number.");//type is incorrect
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;//operand is a good type
		throw new RuntimeError(operator, "Operands must be a number.");//type is incorrect
	}
	
	private boolean isTruthy(Object object) {
		if (object == null) return false;
		if (object instanceof Boolean) return (boolean)object;
		return true;
	
	}
	
	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		
		return a.equals(b);
	}
	
	private String stringify(Object object) {
		if (object == null) return "nil";
		
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		
		return object.toString();
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	
	private void execute(Stmt stmt) {
		stmt.accept(this);
	}
}
