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
		
		defineAst(outputDir, "Expr", Arrays.asList(
				"Binary		: Expr left, Token operator, Expr right",
				"Grouping	: Expr expression",
				"Literal	: Object value",
				"Unary		: Token operator, Expr right"
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
		
		defineVisitor(writer, baseName, types);
		
		//the AST classes
		for (String type : types) {
			String className = type.split(":")[0].trim();//for each specific subclass descriptor string split on ':' and grab the leftmost thing = class name
			String fields = type.split(":")[1].trim();//same, but get the righthand string = fields descriptor string
			defineType(writer, baseName, className, fields);
		}
		
		//The base accept() method.
		writer.println();
		writer.println("	abstract <R> R accept(Visitor<R> visitor);");
		
		writer.println("}");
		writer.close();
	}
	
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("	interface Visitor<R> {//visit method prototypes for each type. Using generics.");
		
		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("		R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}
		
		writer.println(" 	}");
	}
	
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList ) {
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
		writer.println();
		writer.println("		//This class's implementation of the accept() abstract method.");
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
