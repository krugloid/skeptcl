package co.trikita.skeptcl;

class Parser {
	private final String str;
	private int pos;
	private Token token;
	private boolean quoted;

	public Parser(String s) {
		this.str = s;
		this.token = new Token("", TokenType.EOL);
	}

	public Token token() {
		return this.token;
	}

	public Token sep() throws TclException {
		StringBuilder s = new StringBuilder("");
		while (this.pos < this.str.length() &&
				(this.str.charAt(this.pos) == ' ' || this.str.charAt(this.pos) == '\t' ||
				this.str.charAt(this.pos) == '\r')) {
			s.append(this.str.charAt(this.pos));
			this.pos++;
		}
		return new Token(s.toString(), TokenType.SEP);
	}

	public Token eol() throws TclException {
		StringBuilder s = new StringBuilder("");
		while (this.pos < str.length() && 
				(str.charAt(this.pos) == ' ' || str.charAt(this.pos) == '\t' ||
				str.charAt(this.pos) == ';' || str.charAt(this.pos) == '\r' ||
				str.charAt(this.pos) == '\n')) {
			s.append(str.charAt(this.pos));
			this.pos++;
		}
		return new Token(s.toString(), TokenType.EOL);
	}

	public Token cmd() throws TclException {
		int level = 1;
		int blevel = 0;
		this.pos++;
		StringBuilder s = new StringBuilder("");
		while (true) {
			if (this.pos == this.str.length()) {
				break;
			} else if (this.str.charAt(this.pos) == '[' && blevel == 0) {
				level++;
			} else if (this.str.charAt(this.pos) == ']' && blevel == 0) {
				if (--level == 0) {
					break;
				}
			} else if (this.str.charAt(this.pos) == '\\') {
				this.pos++;
			} else if (this.str.charAt(this.pos) == '{') {
				blevel++;
			} else if (this.str.charAt(this.pos) == '}') {
				if (blevel != 0) {
					blevel--;
				}
			}
			s.append(this.str.charAt(this.pos));
			this.pos++;
		}
		if (this.pos < this.str.length() && this.str.charAt(this.pos) == ']') {
			this.pos++;
		}
		if (level > 0) {
			throw new TclException("Mismatching ']'");
		}
		if (blevel > 0) {
			throw new TclException("Mismatching '}'");
		}
		return new Token(s.toString(), TokenType.CMD);
	}

	public Token var() throws TclException {
		this.pos++;
		StringBuilder s = new StringBuilder("");
		while (true) {
			if (this.pos == this.str.length()) {
				break;
			}
			if ((this.str.charAt(this.pos) >= 'a' && this.str.charAt(this.pos) <= 'z') ||
				(this.str.charAt(this.pos) >= 'A' && this.str.charAt(this.pos) <= 'Z') ||
				(this.str.charAt(this.pos) >= '0' && this.str.charAt(this.pos) <= '9') ||
				this.str.charAt(this.pos) == '_') {

				s.append(this.str.charAt(this.pos));
				this.pos++;
				continue;
			}
			break;
		}
		if (s.length() == 0) {
			return new Token("$", TokenType.STR);
		}
		return new Token(s.toString(), TokenType.VAR);
	}

	public Token brace() throws TclException {
		int level = 1;
		this.pos++;
		StringBuilder s = new StringBuilder("");
		while (true) {
			if (this.pos == this.str.length() && level > 0) {
				throw new TclException("Mismatching '}'");
			}
			if (this.str.charAt(this.pos) == '\\' && this.str.length()-this.pos >= 2) {
				this.pos++;
			} else if (this.pos == this.str.length() || this.str.charAt(this.pos) == '}') {
				level--;
				if (level == 0 || this.pos == this.str.length()) {
					if (this.pos < this.str.length()) {
						this.pos++;
					}
					return new Token(s.toString(), TokenType.STR);
				}
			} else if (this.str.charAt(this.pos) == '{') {
				level++;
			}
			s.append(this.str.charAt(this.pos));
			this.pos++;
		}
	}
	
	public Token string() throws TclException {
		boolean newWord = (this.token.type == TokenType.SEP ||
				this.token.type == TokenType.EOL ||
				this.token.type == TokenType.STR);
		if (newWord && this.str.charAt(this.pos) == '{') {
			return brace();
		} else if (newWord && this.str.charAt(this.pos) == '"') {
			this.quoted = true;
			this.pos++;
		}
		StringBuilder s = new StringBuilder("");
		while (true) {
			if (this.pos == this.str.length()) {
				if (this.quoted) {
					return new Token("\""+s.toString(), TokenType.STR);
				}
				return new Token(s.toString(), TokenType.STR);
			}
			//System.out.println("char: '"+this.str.charAt(this.pos)+"' pos="+this.pos);
			switch (this.str.charAt(this.pos)) {
				case '\\':
					if (this.str.length()-this.pos >= 2) { this.pos++; }
					break;
				case '$':
				case '[':
					return new Token(s.toString(), TokenType.STR);
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case ';':
					if (!this.quoted) {
						return new Token(s.toString(), TokenType.STR);
					}
					break;
				case '"':
					if (this.quoted) {
						this.pos++;
						this.quoted = false;
						return new Token(s.toString(), TokenType.STR);
					}
					break;
			}
			s.append(this.str.charAt(this.pos));
			this.pos++;
		}
	}

	public void comment() throws TclException {
		while (this.pos < this.str.length() && this.str.charAt(this.pos) != '\n') {
			this.pos++;
		}
	}

	public Token nextToken() throws TclException {
		//System.out.println("getToken(): pos="+this.pos+" token="+this.token);
		while (true) {
			if (this.pos >= this.str.length()) {
				if (this.token.type != TokenType.EOL && this.token.type != TokenType.EOF) {
					this.token = new Token("", TokenType.EOL);
				} else {
					this.token = new Token("", TokenType.EOF);
				}
				return this.token;
			}
			switch (this.str.charAt(this.pos)) {
				case ' ':
				case '\t':
				case '\r':
					if (this.quoted) {
						this.token = string();
					} else {
						this.token = sep();
					}
					break;
				case '\n':
				case ';':
					if (this.quoted) {
						this.token = string();
					} else {
						this.token = eol();
					}
					break;
				case '[':
					this.token = cmd();
					break;
				case '$':
					this.token = var();
					break;
				case '#':
					if (this.token.type == TokenType.EOL) {
						comment();
						continue;
					} else {
						this.token = string();
					}
					break;
				default:
					this.token = string();
			}
			return this.token;
		}
	}
}

