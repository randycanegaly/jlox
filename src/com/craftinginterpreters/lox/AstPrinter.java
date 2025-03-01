package com.craftinginterpreters.lox;

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
	 * (* (- 123) (group 45.67))
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
}
