package co.trikita.skeptcl;

class Token {
	public final String s;
	public final TokenType type;

	public Token(String s, TokenType t) {
		this.s = s;
		this.type = t;
	}

	public String toString() {
		return "'"+this.s+"' type: "+this.type;
	}

	public boolean equals(Object o){
		if (o == null) return false;
		if (!(o instanceof Token)) return false;

		Token other = (Token) o;
		return this.s.equals(other.s) && this.type == other.type;
	}

	public int hashCode(){
		return this.type.hashCode() * this.s.hashCode();
	}
}


