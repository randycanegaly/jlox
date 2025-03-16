package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		
		String outputDir = args[0];
		
		//generate the expression Ast class
		defineAst(outputDir, "Expr", Arrays.asList(
				"Assign		: Token name, Expr value",//generates a subclass named Assign with fields Token name and Expr value
				"Binary		: Expr left, Token operator, Expr right",
				"Call		: Expr callee, Token paren, List<Expr> arguments",
				"Grouping	: Expr expression",
				"Literal	: Object value",
				"Logical	: Expr left, Token operator, Expr right",
				"Unary		: Token operator, Expr right",
				"Variable	: Token name" //this an expression, it produces a value, when the name is seen, produce the corresponding value
		));
		
		//generate the statement Ast class
		defineAst(outputDir, "Stmt", Arrays.asList(
				"Block		:	List<Stmt> statements",
				"Expression :	Expr expression",
				"Function	: 	Token name, List<Token> params, List<Stmt> body",
				"If			:	Expr condition, Stmt thenBranch, Stmt elseBranch",
				"While 		:	Expr condition, Stmt body",
				"Print 		: 	Expr expression",	
				"Return		: 	Token keyword, Expr value",
				"Var		: 	Token name, Expr initializer" //this is a statement, it has a side effect - bind a value to a variable
		));
	}
	
	private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		
		writer.println("package com.craftinginterpreters.lox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("abstract class " + baseName + " {");
		
		//The base accept() method.
		writer.println();
		writer.println("	//Declare an abstract method that takes Visitor type. All subclasses of baseName implement this method. ");
		writer.println("	abstract <R> R accept(Visitor<R> visitor);");
		writer.println();
		
		defineVisitor(writer, baseName, types);
		
		//the AST classes
		//boolean showComment = true;
		for (String type : types) {
			String className = type.split(":")[0].trim();//for each specific subclass descriptor string split on ':' and grab the leftmost thing = class name
			String fields = type.split(":")[1].trim();//same, but get the righthand string = fields descriptor string
			defineType(writer, baseName, className, fields);
			//showComment = false;
		}
		
		writer.println("}");
		writer.close();
	}
	
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("	/* 	The Visitor interface defines a set of methods, one for each type");
		writer.println("   		such that some action can be enabled across all types, minimizing in-type edits to implement the methods. */");
		
		writer.println("	interface Visitor<R> {//visit method prototypes for each type. Using generics.");
		
		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("		R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}
		
		writer.println(" 	}");
	}
	
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		writer.println();
		writer.println("	//" + className);
		writer.println("	static class " + className + " extends " + baseName + " {");
		
		//Constructor.
		writer.println("		" + className + "(" + fieldList + ") {");
		
		//Store parameters in fields
		String[] fields = fieldList.split(",");
		for (String field : fields) {
			String name = field.trim().split(" ")[1].trim();
			writer.println("			this." + name + " = " + name + ";");
		}
		
		writer.println("		}");
		
		//Visitor pattern
		writer.println("		@Override");
		writer.println("		<R> R accept(Visitor<R> visitor) {");
		writer.println("			return visitor.visit" + className + baseName + "(this);");
		writer.println("		}");
		
		//fields
		writer.println();
		for (String field : fields) {
			writer.println("		final " + field + ";");
		}
		
		writer.println("	}");
	}
}
