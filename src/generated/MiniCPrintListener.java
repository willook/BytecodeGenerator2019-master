package generated;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class MiniCPrintListener extends MiniCBaseListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    String semicolon = ";\n";
    int nTabs=0;
    private boolean isCompound=false;
    private void inputTabs(StringBuilder sb){
        for(int i=0;i<nTabs;i++)
            sb.append("....");
    }
    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<ctx.decl().size();i++){
            String line = newTexts.get(ctx.decl(i));
            sb.append(line);
        }
        System.out.print(sb);
    }
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        String s0;
        if(ctx.var_decl() != null) {
            s0 = newTexts.get(ctx.var_decl());
            newTexts.put(ctx, s0);
        }
        else{
            s0 = newTexts.get(ctx.fun_decl());
            newTexts.put(ctx, s0);
        }
    }
    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String s0, s1, s2;
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        s0 = newTexts.get(ctx.type_spec());
        sb.append(s0).append(' ');
        s1 = ctx.IDENT().toString();
        sb.append(s1);
        if(ctx.getChildCount() == 3){
            sb.append(semicolon);
            newTexts.put(ctx, sb.toString());
        }else if(ctx.getChildCount() == 5){
            sb.append(" = ");
            sb.append(ctx.LITERAL()).append(semicolon);
        }else{
            s2 = newTexts.get(ctx.LITERAL());
            sb.append(" [");
            sb.append(ctx.LITERAL()).append(']').append(semicolon);
        }
        newTexts.put(ctx, sb.toString());
    }

    @Override public void exitType_spec(MiniCParser.Type_specContext ctx) {
        String s0;
        if(ctx.VOID()!=null)
            s0 = ctx.VOID().toString();
        else {
            s0 = ctx.INT().toString();
        }
        newTexts.put(ctx, s0);
    }

    @Override public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        String s0, s1, s2, s3;
        s0 = newTexts.get(ctx.type_spec());
        s1 = ctx.IDENT().toString();
        s2 = newTexts.get(ctx.params());
        s3 = newTexts.get(ctx.compound_stmt());
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        sb.append(s0).append(' ');
        sb.append(s1).append('(');
        sb.append(s2).append(')');
        sb.append(s3);
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitParams(MiniCParser.ParamsContext ctx) {
        StringBuilder sb = new StringBuilder();
        String s0,s1,s2;
        if(ctx.VOID() != null)
            sb.append(ctx.VOID().toString());
        else if(ctx.getChildCount()==0){
            //nothing to do
        }
        else {
            s0 = newTexts.get(ctx.param(0));
            sb.append(s0);
            for (int i = 2; i < ctx.getChildCount(); i++) {
                s1 = ctx.getChild(i - 1).getText();
                s2 = newTexts.get(ctx.param(i / 2));
                sb.append(s1).append(' ');
                sb.append(s2);
            }
        }
        newTexts.put(ctx,sb.toString());
    }
    @Override public void exitParam(MiniCParser.ParamContext ctx) {
        StringBuilder sb = new StringBuilder();
        String s0, s1;
        s0 = newTexts.get(ctx.type_spec());
        s1 = ctx.IDENT().toString();
        sb.append(s0).append(' ').append(s1);
        if(ctx.getChildCount()==4)
            sb.append("[]");
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitStmt(MiniCParser.StmtContext ctx) {
        String s0;
        isCompound = false;
        if(ctx.expr_stmt() != null)
            s0 = newTexts.get(ctx.expr_stmt());
        else if(ctx.compound_stmt() != null) {
            s0 = newTexts.get(ctx.compound_stmt());
            isCompound = true;
        }
        else if(ctx.if_stmt() != null)
            s0 = newTexts.get(ctx.if_stmt());
        else if(ctx.while_stmt() != null)
            s0 = newTexts.get(ctx.while_stmt());
        else
            s0 = newTexts.get(ctx.return_stmt());

        newTexts.put(ctx, s0);
    }
    @Override public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        String s0 = newTexts.get(ctx.expr());
        sb.append(s0).append(semicolon);
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        String s0, s1, s2;
        s0 = ctx.WHILE().toString();
        s1 = newTexts.get(ctx.expr());
        s2 = newTexts.get(ctx.stmt());
        sb.append(s0).append(" (");
        sb.append(s1).append(')').append('\n');

        if(!isCompound) {
            inputTabs(sb);
            sb.append("{\n");
            sb.append("....");
            for(int i=0;i<s2.length()-1;i++)
                if(s2.charAt(i) == '\n')
                    sb.append("\n....");
                else
                    sb.append(s2.charAt(i));
            sb.append(s2.charAt(s2.length()-1));
            inputTabs(sb);
            sb.append("}\n");
        }
        else{
            sb.append(s2);
        }
        newTexts.put(ctx, sb.toString());
    }
    @Override public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        nTabs += 1;
    }
    @Override public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        nTabs -= 1;
        String s0,s1;
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        sb.append("{\n");
        for(int i=0;i<ctx.local_decl().size();i++) {
            s0 = newTexts.get(ctx.local_decl(i));
            sb.append(s0);
        }
        for(int i=0;i<ctx.stmt().size();i++) {
            s1 = newTexts.get(ctx.stmt(i));
            sb.append(s1);
        }
        inputTabs(sb);
        sb.append("}\n");
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String s0, s1, s2;
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        s0 = newTexts.get(ctx.type_spec());
        s1 = ctx.IDENT().toString();
        sb.append(s0).append(' ').append(s1);
        if(ctx.getChildCount() == 5){
            sb.append(" = ");
            s2 = ctx.LITERAL().getText();
            sb.append(s2);
        }else if(ctx.getChildCount() == 6){
            sb.append('[');
            s2 = ctx.LITERAL().getText();
            sb.append(s2).append(']');
        }
        sb.append(semicolon);
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        //if statement stack에 저장하다가 하나씩 뺄것
        //        또는 컴파운드 찾는것 만들기
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        String s0,s1,s2,s3,s4;
        s0 = ctx.IF().getText();

        s1 = newTexts.get(ctx.expr());
        s2 = newTexts.get(ctx.stmt(0));
        sb.append(s0).append(" (");
        sb.append(s1).append(")\n");
        if(!isCompound) {
            inputTabs(sb);
            sb.append("{\n");

            sb.append("....");
            for(int i=0;i<s2.length()-1;i++)
                if(s2.charAt(i) == '\n')
                    sb.append("\n....");
                else
                    sb.append(s2.charAt(i));
            sb.append(s2.charAt(s2.length()-1));
            inputTabs(sb);
            sb.append("}\n");
        }
        else{
            sb.append(s2);
        }
        if(ctx.getChildCount() == 7){
            s3 = ctx.ELSE().getText();
            s4 = newTexts.get(ctx.stmt(1));
            inputTabs(sb);
            sb.append(s3).append('\n');
            inputTabs(sb);
            sb.append(s4).append('\n');
        }
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        inputTabs(sb);
        String s0 = ctx.RETURN().getText();
        sb.append(s0);
        if(ctx.expr()!=null){
            String s1 = newTexts.get(ctx.expr());
            sb.append(' ').append(s1);
        }
        sb.append(semicolon);
        newTexts.put(ctx, sb.toString());
    }
    private boolean isUnaryOp(MiniCParser.ExprContext ctx){
        if(ctx.getChildCount() != 2) return false;
        if(!ctx.getChild(0).getText().equals("-") &&
                !ctx.getChild(0).getText().equals("+") &&
                !ctx.getChild(0).getText().equals("--") &&
                !ctx.getChild(0).getText().equals("++") &&
                !ctx.getChild(0).getText().equals("!"))
            return false;
        if(ctx.expr(0) == null) return false;
        return true;
    }
    private boolean isParentheses(MiniCParser.ExprContext ctx){
        if(ctx.getChildCount() != 3) return false;
        if(!ctx.getChild(0).getText().equals("(") ||
            !ctx.getChild(2).getText().equals(")"))
            return false;
        if(ctx.expr(0) == null) return false;
        return true;
    }
    @Override public void exitExpr(MiniCParser.ExprContext ctx) {
        StringBuilder sb = new StringBuilder();
        String s0,s1,s2,s3,op;
        if(ctx.LITERAL() != null){
            s0 = ctx.LITERAL().getText();
            sb.append(s0);
        }else if(isUnaryOp(ctx)){
            op = ctx.getChild(0).getText();
            s0 = newTexts.get(ctx.expr(0));
            sb.append(op).append(s0);
        }else if(isParentheses(ctx)){
            s0 = ctx.getChild(0).getText();
            s1 = newTexts.get(ctx.expr(0));
            s2 = ctx.getChild(2).getText();
            sb.append(s0).append(s1).append(s2);
        }else if(ctx.IDENT() != null){
            s0 = ctx.IDENT().getText();
            sb.append(s0);
            if(ctx.getChildCount() >= 4){
                s1 = ctx.getChild(1).getText();
                s3 = ctx.getChild(3).getText();
                if(ctx.getChild(1).getText().equals("["))
                    s2 = newTexts.get(ctx.expr(0));
                else
                    s2 = newTexts.get(ctx.args());
                sb.append(s1).append(s2).append(s3);
            }
            if(ctx.getChild(ctx.getChildCount()-2) != null && ctx.getChild(ctx.getChildCount()-2).getText().equals("=")){
                String op2 = ctx.getChild(ctx.getChildCount()-2).getText();
                String s4 = newTexts.get(ctx.expr(ctx.expr().size()-1));
                sb.append(' ').append(op2).append(' ').append(s4);
            }
        }
        else{
            s0 = newTexts.get(ctx.expr(0));
            op = ctx.getChild(1).getText();
            s1 = newTexts.get(ctx.expr(1));
            sb.append(s0).append(' ').append(op).append(' ').append(s1);
        }
        newTexts.put(ctx, sb.toString());
    }
    @Override public void exitArgs(MiniCParser.ArgsContext ctx) {
        StringBuilder sb = new StringBuilder();
        String s0, s1, s2;
        s0 = newTexts.get(ctx.expr(0));
        sb.append(s0);
        for (int i = 2; i < ctx.getChildCount(); i++) {
            s1 = ctx.getChild(i - 1).getText();
            s2 = newTexts.get(ctx.expr(i / 2));
            sb.append(s1).append(' ');
            sb.append(s2);
        }
        newTexts.put(ctx, sb.toString());
    }
}