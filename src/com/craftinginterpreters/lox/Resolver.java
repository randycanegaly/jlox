package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Block;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Return;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.While;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	private final Stack<Map<String, Boolean>> scopes;//A Stack. Each element is a map of String/Boolean key/value pairs
	
	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	private void resolve(List<Stmt> statements) {
		for (Stmt statement : statements) {
			resolve(statement);
		}
	}

	/**
	 *Resolve a block
	 *Create the scope for it
	 *Resolve every statement in the block
	 *Remove the scope
	 */
	@Override
	public Void visitBlockStmt(Block stmt) {
		beginScope();//Add a new scope. Push a new HashMap onto the Stack
		resolve(stmt.statements);
		endScope();//Remove the latest scope. Pop the Stack.
		return null;
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);//accept() calls here, the appropriate visit*Stmt() for that Stmt subclass
	}

	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}
	
	@Override
	public Void visitExpressionStmt(Expression stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitFunctionStmt(Function stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitBinaryExpr(Binary expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitGroupingExpr(Grouping expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitLiteralExpr(Literal expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitUnaryExpr(Unary expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void visitVariableExpr(Variable expr) {
	
	
	}
}
