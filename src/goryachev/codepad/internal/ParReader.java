// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import java.io.Closeable;


/// Paragraph Reader
public abstract class ParReader
	implements Closeable
{
	public static final Object NEWLINE = new Object();
	
	
	public abstract Object nextToken();


	public static ParReader of(String text)
	{
		return new StringParagraphReader(text);
	}
}
