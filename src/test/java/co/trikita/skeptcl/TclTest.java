package co.trikita.skeptcl;

import org.junit.Test;
import static org.junit.Assert.*;

public class TclTest {

	@Test
	public void testUserCommands() throws TclException {
		Tcl tcl = new Tcl();
		tcl.registerCommand("test", new Command() {
			public String run(Tcl interp, String[] args) {
				return "foo";
			}
		}); 
		assertEquals("foo", tcl.eval("test"));

		tcl.registerCommand("hello", new Command() {
			public String run(Tcl tcl, String[] args) {
				return args[1].toUpperCase();
			}
		});
		assertEquals("KNOPF", tcl.eval("hello Knopf"));
	}

	@Test
	public void testIf() throws TclException {
		Tcl tcl = new Tcl();
		assertEquals("foo", tcl.eval("set i 3; if {== $i 3} {return \"foo\"}"));
		assertEquals("2", tcl.eval("set i 1;if {!= 2 1}{\nset i [+ $i 1]\nputs \"hi\"\n}\nset i"));
		assertEquals("4", tcl.eval("if {<= 1 2} {return [+ 2 2]} {return 5}"));
	}

	@Test
	public void testWhile() throws TclException {
		Tcl tcl = new Tcl();
		assertEquals("0", tcl.eval("set k 3;while {!= $k 0}{\nset k [- $k 1]\n}\nset k"));
	}

	@Test
	public void testBreak() throws TclException {
		Tcl tcl = new Tcl();
		assertEquals("3", tcl.eval("set i 9;while {== 1 1} {if {== $i 3} break\nset i [- $i 2]}\nset i"));
	}

	@Test
	public void testContinue() throws TclException {
		Tcl tcl = new Tcl();
		assertEquals("2", tcl.eval("set cnt 3; set i 0;" +
					"while {!= $cnt 0} {\n"+
					"set cnt [- $cnt 1];" +
					"if {== $cnt 2} continue;"+
					"set i [+ $i 1];"+
					"}\nset i"));
	}

	@Test
	public void testProc() throws TclException {
		Tcl tcl = new Tcl();
		assertEquals("5", tcl.eval("proc sum {a b} {return [+ $a $b]};"+
					"set x [sum 2 3]; set x"));
	}
}

