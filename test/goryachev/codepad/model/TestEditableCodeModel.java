// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.ParReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/// Tests EditableCodeModel
public class TestEditableCodeModel
{
	@Test
	public void replaceSinlgeLine() throws Exception
	{
		test("123", 0, 0, 0, 99, "", "");
		test("123", 0, 1, 0, 2, "", "13");
		test("12345", 0, 1, 0, 4, "", "15");
		test("12345", 0, 0, 0, 2, "", "345");
		test("12345", 0, 3, 0, 10, "", "123");
	}

	
	@Test
	public void replaceMultiLine() throws Exception
	{
		// FIX
		//test("11\n22", 0, 0, 1, 99, "", "");
		test("11\n22\n33", 0, 1, 2, 1, "", "1", "3");
	}
	
	
	private static void test(String initialText, int ix1, int off1, int ix2, int off2, String replace, String ... expected) throws Exception
	{
		TextPos p1 = new TextPos(ix1, off1);
		TextPos p2 = new TextPos(ix2, off2);
		EditableCodeModel m = new EditableCodeModel();
		m.replace(TextPos.ZERO, TextPos.ZERO, initialText);
		// TODO check initial state by exporting it
		m.replace(p1, p2, replace);
		String[] result = toArray(m);
		Assertions.assertArrayEquals(expected, result);
	}
	
	
	private static String[] toArray(CodeModel m)
	{
		int sz = m.size();
		String[] rv = new String[sz];
		for(int i=0; i<sz; i++)
		{
			rv[i] = m.getPlainText(i);
		}
		return rv;
	}
}
