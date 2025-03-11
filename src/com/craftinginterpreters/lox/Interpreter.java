package com.craftinginterpreters.lox;
import java.util.ArrayList;
import java.util.List;

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
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.While;

@SuppressWarnings("unused")
public class Interpreter implements Expr.Visitor<Object>,
									Stmt.Visitor<Void> {

	final Environment globals = new Environment();//the global environment
	private Environment environment = globals;//the local environment, initially set to match the global environment
	
	Interpreter() {
		globals.define("clock", new LoxCallable() {
			@Override
			public int arity() { return 0; }
		
			@Override public Object call(Interpreter interpreter,
											List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000.0;
			}
			
			@Override
			public String toString() { return "<native fn>"; }
		});
	}
	
	
	
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
	public Object visitBinaryExpr(Expr.Binary expr) {//A Binary subclass instance of Expr will call this method to trigger the
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
		case AND:
			break;
		case BANG:
			break;
		case CLASS:
			break;
		case COMMA:
			break;
		case DOT:
			break;
		case ELSE:
			break;
		case EOF:
			break;
		case EQUAL:
			break;
		case FALSE:
			break;
		case FOR:
			break;
		case FUN:
			break;
		case IDENTIFIER:
			break;
		case IF:
			break;
		case LEFT_BRACE:
			break;
		case LEFT_PAREN:
			break;
		case NIL:
			break;
		case NUMBER:
			break;
		case OR:
			break;
		case PRINT:
			break;
		case RETURN:
			break;
		case RIGHT_BRACE:
			break;
		case RIGHT_PAREN:
			break;
		case SEMICOLON:
			break;
		case STRING:
			break;
		case SUPER:
			break;
		case THIS:
			break;
		case TRUE:
			break;
		case VAR:
			break;
		case WHILE:
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
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
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
		case BANG:
			return !isTruthy(right);
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double) right;//we hit bottom, took Ast, tore it apart and calculated the actual value from all the pieces of it
		case AND:
			break;
		case BANG_EQUAL:
			break;
		case CLASS:
			break;
		case COMMA:
			break;
		case DOT:
			break;
		case ELSE:
			break;
		case EOF:
			break;
		case EQUAL:
			break;
		case EQUAL_EQUAL:
			break;
		case FALSE:
			break;
		case FOR:
			break;
		case FUN:
			break;
		case GREATER:
			break;
		case GREATER_EQUAL:
			break;
		case IDENTIFIER:
			break;
		case IF:
			break;
		case LEFT_BRACE:
			break;
		case LEFT_PAREN:
			break;
		case LESS:
			break;
		case LESS_EQUAL:
			break;
		case NIL:
			break;
		case NUMBER:
			break;
		case OR:
			break;
		case PLUS:
			break;
		case PRINT:
			break;
		case RETURN:
			break;
		case RIGHT_BRACE:
			break;
		case RIGHT_PAREN:
			break;
		case SEMICOLON:
			break;
		case SLASH:
			break;
		case STAR:
			break;
		case STRING:
			break;
		case SUPER:
			break;
		case THIS:
			break;
		case TRUE:
			break;
		case VAR:
			break;
		case WHILE:
			break;
		default:
			break;
		
		}
	
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
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
		/*
		call the Ast object's accept() method, 
		passing it a reference to this interpreter. 
		Ast will call the appropriate visit* method here, passing a reference to itself 
		so that the visit* method can inflict that method on the Ast object
		*/
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		return environment.get(expr.name);
		
	
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		environment.assign(expr.name, value);
		return value;
	
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}
	
	/*
	 * evaluate the statements in the block  
	 * @param statements
	 * @param environment
	 */
	void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;//set the environment for this scope (inner) to the Environment passed in
			
			for (Stmt statement : statements) {//execute every statement in the block
				execute(statement);
			}
		} finally {
			this.environment = previous;//done executing the block contents, block exits, its scope disappears, 
			//restore "current" environment to the one enclosing the block
		}
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		if(isTruthy(evaluate(stmt.condition))) {//condition is true
			execute(stmt.thenBranch);//do then branch
		} else if (stmt.elseBranch != null) {//condition is false and there is an else block
			execute(stmt.elseBranch);//do else block
		}

		return null;
	}

	@Override
	public Object visitLogicalExpr(Expr.Logical expr) {
		Object left = evaluate(expr.left);
		
		if (expr.operator.type == TokenType.OR) {//it's an OR
			//figure out if we can short cut
			//if left is true, just return it
			//if left is false, return whatever right is
			if (isTruthy(left)) return left;//if not truthy, just fall to the bottom and return right
		} else if (expr.operator.type == TokenType.AND) {//it's an AND
			if (!isTruthy(left)) return left;//AND, and left is false, we know the AND is false. If left is true, fall to the bottom and return whatever right is
		}
		
		return evaluate(expr.right);
	
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		while(isTruthy(evaluate(stmt.condition))) {//I get it. These methods are to actually evaluate/execute/create the value represented by the Ast
											//here, and elsewhere in these, we just substitute the Java for doing that ... a Java while loop
			execute(stmt.body);
		}
		
		return null;
	}

	@Override
	public Object visitCallExpr(Call expr) {
		Object callee = evaluate(expr.callee);//function name or another expression that evaluates to a function object
		
		List<Object> arguments = new ArrayList<>();//where to stuff the arguments we find
		for (Expr argument : expr.arguments) {
			arguments.add(evaluate(argument));//evaluate the argument and add it to the arguments list
		}
	
		if (!(callee instanceof LoxCallable)) {
			throw new RuntimeError(expr.paren, "Can only call functions and classes.");
		}
		
		LoxCallable function = (LoxCallable)callee;//cast the callee. LoxCallable has methods to assist with using functions in Lox. callee must implement LoxCallable.
		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
		}
		return function.call(this, arguments);//The Java representation of any Lox object that can be called like a function will implement the LoxCallable interface.
	}
}
