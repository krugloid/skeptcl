package co.trikita.skeptcl;

import java.util.*;

public class Tcl {

	private final List<Env> env = new ArrayList<Env>() {{
		add(new Env());
	}};

	private final Map<String, Command> cmds = new HashMap<>();

	public Tcl() {
		String[] coreCmds = {
			"+","-","*","/",">",">=","<","<=","==","!=",
			"set", "puts", "if", "while", "continue", "break",
			"return", "proc", "global"
		};
		Command coreCmd = new CoreCommand();
		for (String cmd : coreCmds) {
			registerCommand(cmd, coreCmd);
		}
	}

	public String getVar(String name) throws TclException {
		for (int i = this.env.size()-1; i >= 0; i--) {
			for (int j = 0; j < this.env.get(i).vars.size(); j++) {
				if (this.env.get(i).vars.get(j).name.equals(name)) {
					return this.env.get(i).vars.get(j).value;
				}
			}
		}
		throw new TclException("Variable '"+name+"' not found");
	}

	public Variable getGlobalVar(String name) {
		for (int i = 0; i < this.env.get(0).vars.size(); i++) {
			if (this.env.get(0).vars.get(i).name.equals(name)) {
				return this.env.get(0).vars.get(i);
			}
		}
		return null;
	}

	public void setVar(Variable v) throws TclException {
		for (int i = 0; i < this.env.get(this.env.size()-1).vars.size(); i++) {
			if (v.name.equals(this.env.get(this.env.size()-1).vars.get(i).name)) {
				this.env.get(this.env.size()-1).vars.get(i).set(v.value);
				return;
			}
		}
		this.env.get(this.env.size()-1).vars.add(v);
	}

	public void registerCommand(String name, Command cmd) {
		if (name != null && cmd != null) {
			this.cmds.put(name, cmd);
		}
	}

	public String eval(String s) throws TclException {
		String res = "";
		List<String> args = new ArrayList<>();
		Parser parser = new Parser(s);

		Token prevToken = new Token("", TokenType.EOL);
		while (true) {
			Token t = parser.nextToken();
			//System.out.println("New token: "+t+"; argc="+args.size());
			//for (String a : args) {
				//System.out.println("'" + a + "'");
			//}

			String arg = t.s;
			switch (t.type) {
				case EOF:
					return res;
				case VAR:
					arg = getVar(arg);
					break;
				case CMD:
					arg = eval(arg);
					break;
				case SEP:
					prevToken = t;
					continue;
				case EOL:
					prevToken = t;
					if (args.size() > 0) {
						Command cmd = this.cmds.get(args.get(0));
						String[] argsArray = new String[args.size()-1];
						if (cmd != null) {
							res = cmd.run(this, args.toArray(argsArray));
						} else {
							throw new TclException("Command '"+args.get(0)+"' not found");
						}
					}
					args.clear();
					continue;
			}

			if (prevToken.type == TokenType.SEP || prevToken.type == TokenType.EOL) {
				args.add(arg);
			} else {
				args.set(args.size()-1, args.get(args.size()-1)+arg);
			}
		}
	}

	private class CoreCommand implements Command {
		@Override
		public String run(Tcl interp, String[] args) throws TclException {
			String cmd = args[0];
			List<String> mathCmds = new ArrayList<String>(
					Arrays.asList("+","-","*","/",">",">=","<","<=","==","!="));
			if (mathCmds.contains(cmd)) return mathCommands(args);
			else if ("set".equals(cmd)) return setCommand(args);
			else if ("puts".equals(cmd)) return putsCommand(args);
			else if ("if".equals(cmd)) return ifCommand(args);
			else if ("while".equals(cmd)) return whileCommand(args);
			else if ("continue".equals(cmd)) return continueCommand(args);
			else if ("break".equals(cmd)) return breakCommand(args);
			else if ("return".equals(cmd)) return returnCommand(args);
			else if ("proc".equals(cmd)) return procCommand(args);
			else if ("global".equals(cmd)) return globalCommand(args);
			else throw new TclException("Unknown command: " + args[0]);
		}

		private String mathCommands(String[] args) throws TclException {
			if (args.length != 3) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}
			int a, b;
			try {
				a = Integer.parseInt(args[1]);
				b = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				throw new TclException(e);
			}

			if (args[0].length() == 1) {
				char op = args[0].charAt(0);
				if (op == '+') return "" + (a + b);
				else if (op == '-') return "" + (a - b);
				else if (op == '*') return "" + (a * b);
				else if (op == '/') return "" + (a / b);
				else if (op == '>') return "" + (a > b);
				else if (op == '<') return "" + (a < b);
				else throw new TclException("Invalid command syntax: " + args[0]);
			} else if (args[0].length() == 2) {
				char op1 = args[0].charAt(0);
				char op2 = args[0].charAt(1);
				if (op1 == '>' && op2 == '=') return "" + (a >= b);
				else if (op1 == '<' && op2 == '=') return "" + (a <= b);
				else if (op1 == '=' && op2 == '=') return "" + (a == b);
				else if (op1 == '!' && op2 == '=') return "" + (a != b);
				else throw new TclException("Invalid command syntax: " + args[0]);
			}
			throw new TclException("Invalid command syntax: " + args[0]);
		}

		private String setCommand(String[] args) throws TclException {
			if (args.length == 3) {
				setVar(new Variable(args[1], args[2]));
				return "";
			} else if (args.length == 2) {
				return getVar(args[1]);
			} else {
				throw new TclException("Invalid command syntax: " + args[0]);
			}
		}

		private String putsCommand(String[] args) throws TclException {
			if (args.length != 2) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}
			System.out.println(""+args[1]+"\n");
			return "";
		}

		private String ifCommand(String[] args) throws TclException {
			if (args.length < 3) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}

			boolean condition = Boolean.parseBoolean(eval(args[1]));
			if (condition) {
				return eval(args[2]);
			} else if (args.length > 3) {
				for (int i = 3; i < args.length; i=i+2) {
					String res = args[i];
					if (i+1 == args.length) {
						return eval(args[i]);
					} else {
						if (Boolean.parseBoolean(res)) {
							return eval(args[i+1]);
						}
					}
				}
			}
			return "";
		}

		private String whileCommand(String[] args) throws TclException {
			if (args.length != 3) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}

			String res = "";
			while (true) {
				boolean condition = Boolean.parseBoolean(eval(args[1]));
				if (!condition) return res;
				try {
					res = eval(args[2]);
				} catch (ContinueException e) {
					continue;
				} catch (BreakException e) {
					break;
				}
			}
			return res;
		}

		private String continueCommand(String[] args) throws TclException {
			if (args.length != 1) {
				throw new TclException("Invalid command syntax: " + args[0]);
			} else {
				throw new ContinueException("continue");
			}
		}

		private String breakCommand(String[] args) throws TclException {
			if (args.length != 1) {
				throw new TclException("Invalid command syntax: " + args[0]);
			} else {
				throw new BreakException("break");
			}
		}

		private String returnCommand(String[] args) throws TclException {
			// TODO clear last Env
			if (args.length == 1) {
				return "";
			} else if (args.length == 2) {
				return args[1];
			} else {
				throw new TclException("Invalid command syntax: " + args[0]);
			}
		}

		private String procCommand(String[] args) throws TclException {
			if (args.length != 4) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}

			Scanner scanner = new Scanner(args[2]);        
			if (args[2].matches("^[a-zA-Z0-9_]+$")) {
				throw new TclException("Invalid function "+args[1]+" params");
			}

			String[] argNames;
			if (args[2].isEmpty()) {
				argNames = new String[]{};
			} else {
				argNames = args[2].split(" ");
			}
			final String body = args[3];
			registerCommand(args[1], new Command() {
				@Override
				public String run(Tcl tcl, String[] argv) throws TclException {
					if (argNames.length != argv.length-1 ) {
						throw new TclException("Invalid command syntax: "+argv[0]);
					}
					Tcl.this.env.add(new Env());
					for (int i = 0; i < argNames.length; i++) {
						setVar(new Variable(argNames[i], argv[i+1]));
					}
					String res = eval(body);
					Tcl.this.env.remove(Tcl.this.env.size()-1);
					return res;
				}
			});
			return "";
		}

		private String globalCommand(String[] args) throws TclException {
			if (args.length != 2) {
				throw new TclException("Invalid command syntax: " + args[0]);
			}

			Variable var = getGlobalVar(args[1]);
			if (var == null) {
				var = new Variable(args[1], "");
				Tcl.this.env.get(0).vars.add(var);
			}
			setVar(var);
			return "";
		}
	}

	private class BreakException extends TclException {

		public BreakException(String msg) {
			super(msg);
		}

		public BreakException(Throwable e) {
			super(e);
		}
	}

	private class ContinueException extends TclException {

		public ContinueException(String msg) {
			super(msg);
		}

		public ContinueException(Throwable e) {
			super(e);
		}
	}

	private class Env {
		public final List<Variable> vars = new ArrayList<Variable>();
	}

	private class Variable {
		public final String name;
		private String value;

		public Variable(String n, String v) {
			this.name = n;
			this.value = v;
		}

		public String get() {
			return this.value;
		}

		public void set(String v) {
			this.value = v;
		}
	}
}
