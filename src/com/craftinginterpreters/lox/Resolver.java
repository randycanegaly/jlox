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
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();//A Stack. Each element is a map of String/Boolean key/value pairs
	private FunctionType currentFunction = FunctionType.NONE;//default value for whether we are currently in a function or not
	
	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	private enum FunctionType {
		NONE,
		FUNCTION
	}
	
	
	
	void resolve(List<Stmt> statements) {
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

	private void resolve(Expr expr) {
		expr.accept(this);
	}
	
	private void resolveFunction(Function function, FunctionType type) {
		FunctionType enclosingFunction = currentFunction;//remember the current function type
		currentFunction = type; 
		
		beginScope();//create a new inner scope for the function
		for (Token param : function.params) {//declare and define each param in the function's inner scope
			declare(param);
			define(param);
		}
		
		resolve(function.body);
		endScope();
		currentFunction = enclosingFunction;//set the function type back to what it was before
	}
	
	private void beginScope() {
		scopes.push(new HashMap<String, Boolean>());
	}

	private void endScope() {
		scopes.pop();
	}
	
	private void declare(Token name) {
		if (scopes.isEmpty()) return;
		
		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme)) {
			Lox.error(name, "Already a variable with this name in this scope.");
		}
		
		scope.put(name.lexeme, false);
	}
	
	private void define(Token name) {
		if (scopes.isEmpty()) return;
		scopes.peek().put(name.lexeme, true);
	}
	
	/**
	 * resolves a variable usage/access
	 * walks down the stack of scopes until it finds where the variable is defined
	 * tells the interpreter of the variable resolution and passes the depth
	 * at which the variable definition can be found so that interpreter
	 * can retrieve the correct definition for the variable
	 * @param expr
	 * @param name
	 */
	private void resolveLocal(Expr expr, Token name) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(name.lexeme)) {
				interpreter.resolve(expr, scopes.size() - 1 - i);
				return;
			}
		}
	}
	
	@Override
	public Void visitExpressionStmt(Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitFunctionStmt(Function stmt) {
		declare(stmt.name);//bind the name of the function in the surrounding scope
		define(stmt.name);//set the boolean for the function name variable to true to indicate we got beyond just declaring it
	
		resolveFunction(stmt, FunctionType.FUNCTION);//pass that we are in a function
		return null;
	}

	@Override
	public Void visitIfStmt(If stmt) {
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null) resolve(stmt.elseBranch); 
		return null;
	}

	@Override
	public Void visitWhileStmt(While stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		//am I trying to use a return statement outside of a function?
		if (currentFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword, "Can't return from top-level code");
		}
		
		
		
		if (stmt.value != null) {
			resolve(stmt.value);
		}
		
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		declare(stmt.name);
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}

	@Override
	public Void visitAssignExpr(Assign expr) {
		resolve(expr.value);//resolve the value being assigned
		resolveLocal(expr, expr.name);//resolve the variable being assigned to
		return null;
	}

	@Override
	public Void visitBinaryExpr(Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitCallExpr(Call expr) {
		resolve(expr.callee);
		
		for (Expr argument : expr.arguments) {
			resolve(argument);
		}
		
		return null;
	}

	@Override
	public Void visitGroupingExpr(Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpr(Literal expr) {
		return null;
	}

	@Override
	public Void visitLogicalExpr(Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Unary expr) {
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitVariableExpr(Variable expr) {
		if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {//the variable hasn't been defined yet - false
			Lox.error(expr.name, "Can't read local variable in its own initializer.");
		}
		
		resolveLocal(expr, expr.name);
		return null;
	}
}
