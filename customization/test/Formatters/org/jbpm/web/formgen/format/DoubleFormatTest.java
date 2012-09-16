package ru.runa.bpm.web.formgen.format;

import java.util.Locale;

import ru.runa.commons.format.ParseException;

import junit.framework.TestCase;

public class DoubleFormatTest extends TestCase {

	public void testParse() throws ParseException {
		DoubleFormat format = new DoubleFormat(Locale.US);
		assertNull(format.parse(null));
		assertEquals(new Double(1), format.parse(new String[] {"1.0"}));
		assertEquals(new Double(-1), format.parse(new String[] {"-1.0"}));
		assertEquals(new Double(0.1), format.parse(new String[] {"0.1"}));
		assertEquals(new Double(-0.1), format.parse(new String[] {"-0.1"}));
		assertEquals(new Double(0.1), format.parse(new String[] {".1"}));
		
		try {
			format.parse(new String[] {"one"});
			fail("ParseException expected");
		}
		catch(ParseException e)
		{}
	}
	
	public void testFormat() {
		DoubleFormat format = new DoubleFormat(Locale.US);
		
		assertEquals("1", format.format(1));
		assertEquals("-1", format.format(-1.0));
		assertEquals("0.1", format.format(0.1));
		assertEquals("-0.1", format.format(-0.1));
		assertEquals("0.1", format.format(.1));
		
		try {
			format.format(null);
			fail("IllegalArgumentException expected");
		}
		catch(IllegalArgumentException e)
		{}
		
		try {
			format.format("hello");
			fail("IllegalArgumentException expected");
		}
		catch(IllegalArgumentException e)
		{}
	}

}
