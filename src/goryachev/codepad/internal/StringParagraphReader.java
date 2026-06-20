// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import java.io.IOException;


/// String ParagraphReader
public class StringParagraphReader
	extends ParReader
{
	private final String text;
	private int index;
	
	
	public StringParagraphReader(String text)
	{
		this.text = text;
	}
	
	
	@Override
	public void close()
	{
	}


	@Override
	public Object nextToken()
	{
		// TODO
		return null;
	}
}
