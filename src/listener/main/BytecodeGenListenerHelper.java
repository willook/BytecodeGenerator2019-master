package listener.main;

import java.util.Hashtable;

import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable;
import listener.main.SymbolTable.VarInfo;

public class BytecodeGenListenerHelper {
	
	// <boolean functions>
	
	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof Fun_declContext;
	}
	
	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}
	
	// global vars
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}
	
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	
	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in>
		return false;
	}
	
	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}
	
	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
		if(typespec.getText().equals("int")) return "I";
		return "V";
	}

	// params
	static String getParamName(ParamContext param) {

		return param.IDENT().getText();
	}
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			Type_specContext typespec = (Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}
	
	static String getLocalVarName(Local_declContext local_decl) {
		// <Fill in>
		return local_decl.IDENT().getText();
	}
	
	static String getFunName(Fun_declContext ctx) {
		// <Fill in>
		return ctx.getChild(1).getText();
	}
	
	static String getFunName(ExprContext ctx) {
		// <Fill in>
		return ctx.getChild(0).getText();
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 6;
	}
	
	static String getFunProlog() {
		StringBuilder sb = new StringBuilder();
		sb.append(".class public ");
		sb.append(getCurrentClassName()).append('\n');
		sb.append(".super java/lang/Object\n");
		//sb.append(".method public static main([Ljava/lang/String;)V\n");
		//sb.append(".method public static main([Ljava/lang/String;)V\n");

		return sb.toString();
	}
	
	static String getCurrentClassName() {
		return "Test";
	}

	static boolean isArrayParam(ParamContext ctx) {
		return ctx.getChildCount() == 4;
	}

	static String needLoad(String input, String ctx) {
		String[] lines = input.split("\n");
		String last = lines[lines.length - 1];
		String[] ctxLines = ctx.split("\n");
		String ctxLast = ctxLines[ctxLines.length - 1];

		if (last.contains("--") || last.contains("++")) {
			return ctxLast.replace("store", "load");
		}
		return "";
	}
}
