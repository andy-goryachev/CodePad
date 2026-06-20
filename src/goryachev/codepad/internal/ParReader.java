// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import java.io.Closeable;


/// Paragraph Reader.
/// Supports reading lines of text, as well as new line separator tokens.
/// A line is considered to be terminated by any one of a LF ('\n'), a CR ('\r'), or a CRLF ("\r\n").
public abstract class ParReader
	implements Closeable
{
	public static final Object NEWLINE = new Object();
	
	
	/// Reads next token from its input.
	/// Returns:
	/// - the next line as a `String`
	/// - ParReader#NEWLINE for a line separator
	/// - `null` when an end of file is reached 
	/// @return the next token
	public abstract Object nextToken();


	public static ParReader of(String text)
	{
		return new StringParReader(text);
	}
}
