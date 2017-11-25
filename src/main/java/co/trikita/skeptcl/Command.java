package co.trikita.skeptcl;

public interface Command {
	String run(Tcl interp, String[] args) throws TclException;
}

