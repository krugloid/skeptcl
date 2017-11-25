package co.trikita.skeptcl;

class TclException extends Exception {

	public TclException(String msg) {
		super(msg);
	}

	public TclException(Throwable e) {
		super(e);
	}
}

