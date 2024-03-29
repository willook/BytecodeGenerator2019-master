package listener.main;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable.Type;
import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}
	
	static public class VarInfo {
		Type type; 
		int id;
		int initVal;
		
		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}
	
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function 
	
		
	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;
		_lsymtable = new HashMap<>();
	}
	
	void putLocalVar(String varname, Type type){
		_lsymtable.put(varname, new VarInfo(type, _localVarID++));
	}
	
	void putGlobalVar(String varname, Type type) {
		_gsymtable.put(varname, new VarInfo(type, _globalVarID++));
	}
	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		_lsymtable.put(varname, new VarInfo(type, _localVarID++, initVar));
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		_gsymtable.put(varname, new VarInfo(type, _globalVarID++, initVar));
	}
	
	void putParams(ParamsContext params) {
		if(params.VOID() != null) return;
		for(int i = 0; i < params.param().size(); i++) {
			if (isArrayParam(params.param(i))) {
				putLocalVar(getParamName(params.param(i)), Type.INTARRAY);
			} else {
				putLocalVar(getParamName(params.param(i)), Type.INT);
			}
		}
	}
	
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}
	
	public String getFunSpecStr(String fname) {
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
        return getFunSpecStr(ctx);
	}
	
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";	
		String rtype = "";
		String res = "";
		if(fname.equals("main")) return getFunSpecStr("main");

		argtype = getParamTypesText(ctx.params());
		rtype = getTypeText(ctx.type_spec());

		res =  fname + "(" + argtype + ")" + rtype;
		
		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);
		
		return res;
	}
	
	String getVarId(String name){
		VarInfo var = _lsymtable.get(name);
		if(var == null)
			var = _gsymtable.get(name);
		if(var == null) {
			//System.out.println("There is no variant "+name);
			return null;
		}
        return "" + var.id;
	}
	
	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}
		
		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}
		
		return Type.ERROR;	
	}

	int getVarInitValue(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.initVal;
		}

		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return lvar.initVal;
		}

		return 0;
	}

	String newLabel() {
		return "label" + _labelID++;
	}
	
	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		String id = "";
		return id + _globalVarID++;
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	public int getTempValue() {
		return _localVarID++;
	}
}
