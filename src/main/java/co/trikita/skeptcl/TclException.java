package co.trikita.skeptcl;

public class TclException extends Exception {

	public TclException(String msg) {
		super(msg);
	}

	public TclException(Throwable e) {
		super(e);
	}
}

