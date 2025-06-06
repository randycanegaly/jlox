package com.craftinginterpreters.lox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Get;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Set;
import com.craftinginterpreters.lox.Expr.Super;
import com.craftinginterpreters.lox.Expr.This;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Block;
import com.craftinginterpreters.lox.Stmt.Class;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Return;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>,
									Stmt.Visitor<Void> {

	final Environment globals = new Environment();//the global environment
	private Environment environment = globals;//the local environment, initially set to match the global environment
	private final Map<Expr, Integer> locals = new HashMap<>();//a map of expressions and integer key/pairs. The integer is the depth 
	//in the environment chain where the definition of the variable can be found
	
	Interpreter() {
		/* bind a name to a LoxCallable object
		 * implements the LoxCallable interface methods
		 * call allows it to be called
		 * clock.call(this, Collections.emptyList());
		 * to get back the current epoch time in seconds
		 */
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
		/*
		call the Ast object's accept() method, 
		passing it a reference to this interpreter. 
		Ast will call the appropriate visit* method here, passing a reference to itself 
		so that the visit* method can inflict that method on the Ast object
		*/
	}
	
	/**
	 * is passed an Expr subclass object and depth
	 * puts the syntax tree node in the locals map along with the distance between where it is used and where it is defined
	 * depth is the distance along the environment chain 
	 * @param expr
	 * @param depth
	 */
	void resolve(Expr expr, int depth) {
		locals.put(expr, depth);
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}

	/**
	 * Method to get the value for a variable
	 * When a variable expression is seen, go get its value
	 */
	@Override
	public Object visitVariableExpr(Variable expr) {
		return lookUpVariable(expr.name, expr);
	}

	private Object lookUpVariable(Token name, Expr expr) {
		Integer distance = locals.get(expr);
		if (distance != null) {
			return environment.getAt(distance, name.lexeme);//go to the environment at distance, where we determined the variable is defined
			//get the value of the variable there
		} else {
			return globals.get(name);//not in the map, so the variable is a global
		}
	}

	@Override
	public Object visitAssignExpr(Assign expr) {
		Object value = evaluate(expr.value);
		
		Integer distance = locals.get(expr);
		if (distance != null) {
			environment.assignAt(distance, expr.name, value);
		} else {
			globals.assign(expr.name, value);//otherwise put the name/value pair in the global environment
		}
		
		return value;
	}

	@Override
	public Void visitBlockStmt(Block stmt) {
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
	public Void visitIfStmt(If stmt) {
		if(isTruthy(evaluate(stmt.condition))) {//condition is true
			execute(stmt.thenBranch);//do then branch
		} else if (stmt.elseBranch != null) {//condition is false and there is an else block
			execute(stmt.elseBranch);//do else block
		}

		return null;
	}

	@Override
	public Object visitLogicalExpr(Logical expr) {
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
	public Void visitWhileStmt(While stmt) {
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

	@Override
	public Void visitFunctionStmt(Function stmt) {
		LoxFunction function = new LoxFunction(stmt, environment, false);//false = not in an initializer
		//give the function the current environment - the one that is active when the function is declared - the closure
		//we got a syntax tree node function instance, but that doesn't have the mechanics for calling it
		//wrap it in a LoxFunction which has call(), etc.
		environment.define(stmt.name.lexeme, function);//bind the function name to the function object in the local scope environment
		return null;
	}

	@Override
	public Void visitReturnStmt(Return stmt) {
		Object value = null;
		if (stmt.value != null) value = evaluate(stmt.value);
		
		throw new ReturnException(value);//return means leave the current scope and hand execution back to the caller
		//so use the Java Exception mechanism to bubble up to where the function was called
	}

	/**
	 *interpreting means converting a syntax tree node (an AST) into its runtime representation
	 *the runtime representation of a Lox language class is a Java LoxClass class. So make one.
	 *two-stage process, declare the name, bound to nothing then go back and bind the LoxClass instance to the name
	 *this allows for referencing the class inside its own methods
	 *the runtime representation of each Stmt.Class's method, Stmt.Function, is the Java LoxFunction class
	 */
	@Override
	public Void visitClassStmt(Class stmt) {
		Object superclass = null;
		if (stmt.superclass != null) {
			superclass = evaluate(stmt.superclass);
			if (!(superclass instanceof LoxClass)) {
				throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
			}
		}
		
		environment.define(stmt.name.lexeme, null);//in the environment, bind the class name to null
		
		if (stmt.superclass != null) {
			environment = new Environment(environment);//make a new child environment with the old current environment as its parent, 
			//this is now the environment we work with
			environment.define("super", superclass);//bind "super" to the subclass in that environment
		}
		
		Map<String, LoxFunction> methods = new HashMap<>();
		for (Stmt.Function method : stmt.methods ) {
			LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));//class methods get the environment from above with
			//"super" bound to the superclass, as their closure
			methods.put(method.name.lexeme, function);
		}
		
		LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass)superclass, methods);//put the runtime representations of the Stmt.Class's methods into the LoxClass instance
		
		if (superclass != null) {
			environment = environment.enclosing;//set the environment back to the one that was in use before we created a child environment
			//and bound "super" to the superclass as that new child environment with "super" bound was used as closures for the LoxFunctions of the class methods
			//we don't need it anymore.
		}
		
		environment.assign(stmt.name, klass);//"re-bind" the name to the new LoxClass instance
		
		return null;
	}

	/**
	 *interpreting means converting a syntax tree node (an AST) into its runtime representation
	 */
	@Override
	public Object visitGetExpr(Get expr) {
		Object object = evaluate(expr.object);
		if (object instanceof LoxInstance) {
			return ((LoxInstance) object).get(expr.name);//got back Object type from evaluate, cast it to LoxInstance since it is one
		}
		
		throw new RuntimeError(expr.name, "Only instances have properties.");
	}

	@Override
	public Object visitSetExpr(Set expr) {
		Object object = evaluate(expr.object);//the thing being set
		
		if (!(object instanceof LoxInstance)) {
			throw new RuntimeError(expr.name, "Only instances have fields.");
		}
		
		Object value = evaluate(expr.value);
		((LoxInstance)object).set(expr.name, value);
		return value;
	}



	@Override
	public Object visitThisExpr(This expr) {
		return lookUpVariable(expr.keyword, expr);
	}

	/**
	 * @param Expr.Super that has the "super" keyword and a method name, the super.method we want
	 * @return LoxFunction of the method on super that we want. It has a closure that has a bind for 'this'
	 *We see a super expression. It has the keyword "super" and the name of the method.
	 *We go to the locals HashMap and giving that expression, get back the depth from the current environment to where "super" is bound,
	 *"super" was bound in the environment where the class definition that used the superclass was seen
	 *We get the object bound to "super" in that distant environment and cast it to a LoxClass
	 *We need to create a LoxInstance representing 'this'
	 *'this' will be found just inside the environment that binds "super", so we do the distance-1 calculation to get that object and cast it to a LoxInstance
	 *We find the method, a LoxFunction, by name, that we want on the LoxClass superclass
	 *bind() on that LoxFunction creates a new environment, a closure for the LoxFunction and in that closure 
	 */
	@Override
	public Object visitSuperExpr(Super expr) {
		int distance = locals.get(expr);//expr is the key in the locals Hashmap, get returns the value, which is the depth.
		//depth is the environments distance between where the variable is used and where it is defined
		LoxClass superclass = (LoxClass)environment.getAt(distance, "super");//gets the environment distance jumps up the chain and gets the object for the "super" key
		
		//We know distance is the number of environment hops from the super expression to where the superclass is bound to "super" in some environment
		//The environment where 'this' is bound is the one inside of where the superclass is bound to "super", so distance-1
		LoxInstance object = (LoxInstance)environment.getAt(distance - 1, "this");//kind of a hack
	
		LoxFunction method = superclass.findMethod(expr.method.lexeme);
		
		if (method == null) {
			throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
		}
		
		return method.bind(object);
	}
}
