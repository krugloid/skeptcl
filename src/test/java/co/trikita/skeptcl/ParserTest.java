package co.trikita.skeptcl;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

	@Test
	public void testEmptyString() throws TclException {
		Parser parser = new Parser("");
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testStrings() throws TclException {
		Parser parser = new Parser("Hell_o :0123 world$ !@#%^&*() $ $$ # #not-a-comment");
		assertEquals(new Token("Hell_o", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token(":0123", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("world", TokenType.STR), parser.nextToken());
		assertEquals(new Token("$", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("!@#%^&*()", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("$", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("$", TokenType.STR), parser.nextToken());
		assertEquals(new Token("$", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("#", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("#not-a-comment", TokenType.STR), parser.nextToken());
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testSeparatorsAndEols() throws TclException {
		Parser parser = new Parser("  \t\n;\n\t\t  ");
		assertEquals(new Token("  \t", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("\n;\n\t\t  ", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testVars() throws TclException {
		Parser parser = new Parser("$var1 $var2 $_\n foo$bar");
		assertEquals(new Token("var1", TokenType.VAR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("var2", TokenType.VAR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("_", TokenType.VAR), parser.nextToken());
		assertEquals(new Token("\n ", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("foo", TokenType.STR), parser.nextToken());
		assertEquals(new Token("bar", TokenType.VAR), parser.nextToken());
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testCommands() throws TclException {
		Parser parser = new Parser("[] [wow ] [{\n}\n][[]");
		assertEquals(new Token("", TokenType.CMD), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("wow ", TokenType.CMD), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("{\n}\n", TokenType.CMD), parser.nextToken());
		try {
			parser.nextToken();
			fail("Mismatched ']' not detected");
		} catch (TclException e) { }
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
		parser = new Parser("$[$var\n[foo]]\n][{]");
		assertEquals(new Token("$", TokenType.STR), parser.nextToken());
		assertEquals(new Token("$var\n[foo]", TokenType.CMD), parser.nextToken());
		assertEquals(new Token("\n", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("]", TokenType.STR), parser.nextToken());
		try {
			parser.nextToken();
			fail("Mismatched '{' not detected");
		} catch (TclException e) { }
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testBraces() throws TclException {
		Parser parser = new Parser("{} foo{} {bar}baz\n{{hel\nlo}} {{world}");
		assertEquals(new Token("", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("foo{}", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("bar", TokenType.STR), parser.nextToken());
		assertEquals(new Token("baz", TokenType.STR), parser.nextToken());
		assertEquals(new Token("\n", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("{hel\nlo}", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		try {
			parser.nextToken();
			fail("Mismatched '}' not detected");
		} catch (TclException e) { }
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
		parser = new Parser("{foo}{bar[baz]} {}}hello");
		assertEquals(new Token("foo", TokenType.STR), parser.nextToken());
		assertEquals(new Token("bar[baz]", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("", TokenType.STR), parser.nextToken());
		assertEquals(new Token("}hello", TokenType.STR), parser.nextToken());
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testQuotes() throws TclException {
		Parser parser = new Parser("\"\" foo\"bar\nbaz \"hel\nlo\"\"wor\nld\" \"foo");
		assertEquals(new Token("", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("foo\"bar", TokenType.STR), parser.nextToken());
		assertEquals(new Token("\n", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("baz", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("hel\nlo", TokenType.STR), parser.nextToken());
		assertEquals(new Token("wor\nld", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("\"foo", TokenType.STR), parser.nextToken());
		assertEquals(new Token("", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}

	@Test
	public void testComments() throws TclException {
		Parser parser = new Parser("foo #bar \n # comment\n# comment #2");
		assertEquals(new Token("foo", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("#bar", TokenType.STR), parser.nextToken());
		assertEquals(new Token(" ", TokenType.SEP), parser.nextToken());
		assertEquals(new Token("\n ", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("\n", TokenType.EOL), parser.nextToken());
		assertEquals(new Token("", TokenType.EOF), parser.nextToken());
	}
}
