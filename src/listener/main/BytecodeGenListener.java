package listener.main;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.ProgramContext;
import generated.MiniCParser.StmtContext;
import generated.MiniCParser.Var_declContext;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();

	int tab = 0;
	int label = 0;

	String arrow_variable = "";
	// program	: decl+

	@Override
	public void enterFun_decl(Fun_declContext ctx) {
		symbolTable.initFunDecl();
		symbolTable.getFunSpecStr("main");
		String fname = getFunName(ctx);
		ParamsContext params;

		if (fname.equals("main")) {
			symbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			symbolTable.putFunSpecStr(ctx);
			params = (ParamsContext) ctx.getChild(3);
			symbolTable.putParams(params);
		}
	}


	// var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(Var_declContext ctx) {
		String varName = ctx.IDENT().getText();

		if (isArrayDecl(ctx)) {
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
		}
		else  { // simple decl
			symbolTable.putGlobalVar(varName, Type.INT);
		}
	}


	@Override
	public void enterLocal_decl(Local_declContext ctx) {
		if (isArrayDecl(ctx)) {
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INTARRAY, Integer.parseInt(ctx.LITERAL().toString()));
		}
		else if (isDeclWithInit(ctx)) {
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
		}
		else  { // simple decl
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}
	}


	@Override
	public void exitProgram(ProgramContext ctx) {
		String classProlog = getFunProlog();
		String fun_decl = "", var_decl = "";
		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}
		newTexts.put(ctx, classProlog + var_decl + fun_decl);
		System.out.println(newTexts.get(ctx));
		/*
		try {
			BufferedWriter bw = new BufferedWriter(new BufferedWriter(new FileWriter("Test.j")));
			bw.write(newTexts.get(ctx));
			bw.close();
		} catch(Exception e){}
		*/
	}


	// decl	: var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if(ctx.getChildCount() == 1)
		{
			if(ctx.var_decl() != null)				//var_decl
				decl += newTexts.get(ctx.var_decl());
			else							//fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}

	// stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(StmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() > 0) {
			if(ctx.expr_stmt() != null)				// expr_stmt
				stmt += newTexts.get(ctx.expr_stmt());
			else if(ctx.arrow_stmt() != null)        // arrow_stmt
				stmt += newTexts.get(ctx.arrow_stmt());
			else if(ctx.compound_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			else if(ctx.if_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.if_stmt());
			else if(ctx.while_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.while_stmt());
			else if(ctx.return_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.return_stmt());
		}
		newTexts.put(ctx, stmt);
	}

	// expr_stmt	: expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() == 2)
		{
			stmt += newTexts.get(ctx.expr());	// expr
		}
		newTexts.put(ctx, stmt);
	}


	// while_stmt	: WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		StringBuilder sb = new StringBuilder();
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());

		String l1 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		sb.append(l1).append(":\n");
		sb.append(condExpr).append("\n");
		sb.append("ifeq " + lend + "\n");
		sb.append(newTexts.get(ctx.stmt()));
		sb.append("goto "+l1+"\n");
		sb.append(lend+":\n");

		newTexts.put(ctx, sb.toString());
	}

	private void inputTabs(StringBuilder sb){
		for(int i=0;i<tab;i++)
			sb.append('\t');
	}
	private String inputTabs(String s){
		for(int i=0;i<tab;i++)
			s += '\t';
		return s;
	}
	@Override
	public void exitFun_decl(Fun_declContext ctx) {
			// <(2) Fill here!>
		StringBuilder sb = new StringBuilder();
		String fname = getFunName(ctx);
		String header = funcHeader(ctx, fname);
		//String params = getParamTypesText(ctx.params());
		String compound = newTexts.get(ctx.compound_stmt());

		sb.append(header);
		//sb.append(params);
		sb.append(compound);
		if(ctx.compound_stmt().stmt() != null){
			if(ctx.compound_stmt().stmt(ctx.compound_stmt().stmt().size()-1).return_stmt() == null) {
				sb.append("return\n");
				sb.append(".end method\n");
			}

		}
		newTexts.put(ctx, sb.toString());

	}


	private String funcHeader(Fun_declContext ctx, String fname) {
		return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"
				+ ".limit stack " 	+ getStackSize(ctx) + "\n"
				+ ".limit locals " 	+ getLocalVarSize(ctx) + "\n";
	}



	@Override
	public void exitVar_decl(Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			varDecl += "putfield " + varName + "\n";
			// v. initialization => Later! skip now..:
		}
		newTexts.put(ctx, varDecl);
	}

	@Override
	public void exitLocal_decl(Local_declContext ctx) {
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			//symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
			String vId = symbolTable.getVarId(ctx);
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
					+ "istore " + vId + "\n";
		} else if (isArrayDecl(ctx)) {
			String vId = symbolTable.getVarId(ctx);
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
					+ "newarray int" + "\n"
					+ "astore " + vId + "\n";
		}

		newTexts.put(ctx, varDecl);
	}


	@Override
	public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		tab += 1;
	}

	// compound_stmt	: '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		//System.out.println("tab: "+tab);
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<ctx.local_decl().size();i++)
			sb.append(newTexts.get(ctx.local_decl(i)));
		for(int i=0;i<ctx.stmt().size();i++)
			sb.append(newTexts.get(ctx.stmt(i)));
		tab -= 1;
		newTexts.put(ctx, sb.toString());
	}

	// if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));

		String lend = symbolTable.newLabel();

		//System.out.println(newTexts.get(ctx.stmt(1)));
		//System.out.println(ctx.getChildCount());
		if(noElse(ctx)) {
			stmt += condExpr + "\n";

			stmt += "ifeq ";

			stmt += lend + "\n"
				+ thenStmt + "\n"
				+ lend + ":"  + "\n";
		}
		else {

			String lelse = symbolTable.newLabel();
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr + "\n";
			stmt += "ifeq ";
			stmt += lelse + "\n"
					+ thenStmt + "\n"
					+ "goto " + lend + "\n"
					+ lelse + ": " + elseStmt + "\n"
					+ lend + ":"  + "\n";
		}

		newTexts.put(ctx, stmt);
	}


	// return_stmt	: RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		// <(4) Fill here>
		String stmt = "";
		if(ctx.expr() != null) {
			stmt += newTexts.get(ctx.expr())+"\n";
			stmt += "ireturn\n";
			stmt += ".end method\n";
		}
		else{
			stmt += "return\n";
			stmt += ".end method\n";
		}
		newTexts.put(ctx, stmt);
	}


	@Override
	public void exitExpr(ExprContext ctx) {
		String expr = "";
		if(ctx.getChildCount() <= 0) {
			newTexts.put(ctx, "");
			return;
		}
		//expr = inputTabs(expr);

		if(ctx.getChildCount() == 1) { // IDENT | LITERAL
			if(ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if(symbolTable.getVarType(idName) == Type.INT) {
					expr += "iload " + symbolTable.getVarId(idName) + " \n";
				}
				//else	// Type int array => Later! skip now..
				//	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
				} else if (ctx.LITERAL() != null) {
					String literalStr = ctx.LITERAL().getText();
					expr += "ldc " + literalStr + " \n";
				}

			} else if(ctx.getChildCount() == 2) { // UnaryOperation
			//System.out.println(newTexts.get(ctx));
			//expr = handleUnaryExpr(ctx, newTexts.get(ctx) + expr);
			expr = handleUnaryExpr(ctx, expr);
		}
		else if(ctx.getChildCount() == 3) {
			if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
				expr = newTexts.get(ctx.expr(0));

			} else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
				expr = newTexts.get(ctx.expr(0))
						+ "istore " + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";

			} else { 											// binary operation
				expr = handleBinExpr(ctx, expr);

			}
		}
		// IDENT '(' args ')' |  IDENT '[' expr ']'
		else if(ctx.getChildCount() == 4) {
			if(ctx.args() != null){		// function calls
				expr = handleFunCall(ctx, expr);
			} else { // expr
				// Arrays: TODO
				// aload (id)
				// expr
				// iaload
				expr = "aload " + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
				     + newTexts.get(ctx.expr(0))
					 + "iaload" + "\n";
			}
		}
		// IDENT '[' expr ']' '=' expr a[0] = ++i;
		else if (ctx.getChildCount() == 6){ // Arrays: TODO			*/
			String expr0 = newTexts.get(ctx.expr(0));
			String expr1 = newTexts.get(ctx.expr(1));
			expr = "aload " + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
				 + expr0;
			expr += needLoad(ctx.expr(0).getText(), expr0) + "\n";
			expr += expr1;
			expr += needLoad(ctx.expr(1).getText(), expr1) + "\n";
			expr += "iastore" + "\n";
		}
		newTexts.put(ctx, expr);
	}


	private String handleUnaryExpr(ExprContext ctx, String expr) {

		expr += newTexts.get(ctx.expr(0));
		String vid;
		switch(ctx.getChild(0).getText()) {
		case "-":
			expr += "ineg \n";
			/*
			if(ctx.expr(0).IDENT() != null){
				vid = symbolTable.getVarId(ctx.expr(0).IDENT().getText());
				expr += "istore "+vid + "\n";
			}*/
			break;
		case "--":
			vid = symbolTable.getVarId(ctx.expr(0).IDENT().getText());
			expr += "ldc 1" + "\n"
					+ "isub" + "\n";
			expr += "istore "+vid  + "\n";
			break;
		case "++":
			vid = symbolTable.getVarId(ctx.expr(0).IDENT().getText());
			expr += "ldc 1" + "\n"
					+ "iadd" + "\n";
			expr += "istore "+vid + "\n";
			break;
		case "!":
			String l1 = symbolTable.newLabel();
			String l2 = symbolTable.newLabel();
			String lend = symbolTable.newLabel();
			expr += "ifeq " + l2 + "\n"
					+ l1 + ": " + "\n"
					+ "ldc 0" + "\n"
					+ "goto " + lend + "\n"
					+ l2 + ": " +"\n"
					+ "ldc 1" + "\n"
					+ lend + ": " + "\n";
			//if(ctx.IDENT() != null) {
			//	vid = symbolTable.getVarId(ctx.expr(0).IDENT().getText());
			//	expr += "istore " + vid+"\n";
			//}
			break;

		}
		return expr;
	}


	private String handleBinExpr(ExprContext ctx, String expr) {
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		switch(ctx.getChild(1).getText()){
			case "and":
				expr += newTexts.get(ctx.expr(0));
				expr +=  "ifne " + l2 + "\n";
				expr +=  "ldc 0\n";
				expr +=  "goto "+lend+"\n";
				expr += l2 + ":\n";
				expr += newTexts.get(ctx.expr(1));
				expr += lend + ":\n";
				return expr;
			case "or":
				expr += newTexts.get(ctx.expr(0));
				expr +=  "ifeq " + l2 + "\n";
				expr +=  "ldc 1\n";
				expr +=  "goto "+lend+"\n";
				expr += l2 + ":\n";
				expr += newTexts.get(ctx.expr(1));
				expr += lend + ":\n";
				return expr;
		}

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));
		//String id = symbolTable.getVarId(ctx.expr(0).IDENT().getText());


		switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "imul \n"; break;
			case "/":
				expr += "idiv \n"; break;
			case "%":
				expr += "irem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "iadd \n"; break;
			case "-":
				expr += "isub \n"; break;

			case "==":
				expr += "isub " + "\n"
						+ "ifeq "+ l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+"ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "isub " + "\n"
						+ "ifne "+ l2+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+"ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<=":
				expr += "isub " + "\n"
						+ "ifle "+ l2+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<":
				expr += "isub " + "\n"
						+ "iflt "+ l2+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " +"\n"
						+"ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">=":
				expr += "isub " + "\n"
						+ "ifge "+ l2+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 1" + "\n"
						+ lend + ": " + "\n";

				break;

			case ">":
				expr += "isub " + "\n"
						+ "ifgt "+ l2+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": "+"\n"
						+ "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case "and":
				//if a and b
				//a is 0 short cut using pop
				//a is 1 goto lend
				expr +=  "ifne " + lend + "\n"
					+ "pop" + "\n" + "ldc 0" + "\n"
					+ lend + ": " + "\n"; break;
			case "or":
				//if a or b
				//a is 1 short cut using pop
				//a is 0 goto lend
				expr +=  "ifeq " + lend + "\n"
					+ "pop" + "\n" + "ldc 1" + "\n"
					+ lend + ": " + "\n";
				break;

		}
		return expr;
	}
	private String handleFunCall(ExprContext ctx, String expr) {
		// IDENT ( args )
		String fname = getFunName(ctx);

		if (fname.equals("_print")) {		// System.out.println
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
			  		+ newTexts.get(ctx.args())
			  		+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {
			expr = newTexts.get(ctx.args())
					+ "invokestatic " + getCurrentClassName()+ "/" + symbolTable.getFunSpecStr(fname) + "\n";
		}

		return expr;

	}

	// args	: expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "\n";

		for (int i=0; i < ctx.expr().size() ; i++) {
			argsStr += newTexts.get(ctx.expr(i)) ;
		}
		newTexts.put(ctx, argsStr);
	}

	@Override
	public void enterArrow_stmt(MiniCParser.Arrow_stmtContext ctx) {
		arrow_variable = ctx.IDENT().getText();
	}

	// arrow_stmt  : IDENT arrow ';'         ;
	@Override
	public void exitArrow_stmt(MiniCParser.Arrow_stmtContext ctx) {
		String result = "";
		result = newTexts.get(ctx.arrow());
		arrow_variable = "";
//		System.out.println("arrow_stmt : \n"+result);  // DEBUG
		newTexts.put(ctx, result);
	}

	// arrow   : ARROW single_expr (ARROW single_expr)* ;
	@Override
	public void exitArrow(MiniCParser.ArrowContext ctx) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < ctx.single_expr().size(); i++) {
			result.append(newTexts.get(ctx.single_expr().get(i)));
		}
//		System.out.println("arrow : " + result.toString()); // DEBUG
		newTexts.put(ctx, result.toString());
	}

	//	single_expr : '+' expr | '-' expr | '*' expr | '/' expr;
	@Override
	public void exitSingle_expr(MiniCParser.Single_exprContext ctx) {
		String result = "";
		String op = getOperationText(ctx.getChild(0).getText());
		String expr = newTexts.get(ctx.expr());
//		System.out.println("Single : "+expr); // DEBUG
		if (symbolTable.getVarType(arrow_variable) == Type.INT){
			result = "iload " + symbolTable.getVarId(arrow_variable) + "\n"
					+ expr;

			result += op;
			result += "istore " + symbolTable.getVarId(arrow_variable) + "\n";
		}
		else if (symbolTable.getVarType(arrow_variable) == Type.INTARRAY){
			int arraySize= symbolTable.getVarInitValue(arrow_variable);
			for (int i = 0; i < arraySize; i++) {
				//Todo 배열 0 ~ size - 1(포함) 까지 값 업데이트;
				//Example int a[2] = { 1, 2 }; a -> + 3; a = { 4, 5 }
//				result += "aload " + symbolTable.getVarId(arrow_variable) + "\n"
//						+ "ldc " + i + "\n"
//						+ expr;
//				result += op;
//				result += "iastore " +
			}
		}

		newTexts.put(ctx, result);
	}

	private String getOperationText(String op) {
		switch (op) {
			case "+":
				return "iadd\n";
			case "-":
				return "isub\n";
			case "*":
				return "imul\n";
			case "/":
				return "idiv\n";
		}
		return null;
	}
}
