// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


/// Tests StringParReader
public class TestStringParReader
{
	private static final Object NL = ParReader.NEWLINE;
	
	
	@Test
	public void nextToken()
	{
		test("");
		
		test("\n\r\r\n", NL, NL, NL);
		
		test("aaaaaa\rbbb", "aaaaaa", NL, "bbb");
		
		test("\n1\n\n2\n3\n\n\n", NL, "1", NL, NL, "2", NL, "3", NL, NL, NL);
		
		test("1\n2\r3\r\n4", "1", NL, "2", NL, "3", NL, "4");
	}
	
	
	private static void test(String text, Object ... expected)
	{
		try(StringParReader rd = new StringParReader(text))
		{
			ArrayList<Object> res = new ArrayList<>();
			Object x;
			while((x = rd.nextToken()) != null)
			{
				res.add(x);
			}
		}
	}
}
