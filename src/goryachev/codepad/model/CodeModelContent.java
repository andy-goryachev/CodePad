// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;


/// CodeModel Content.
public interface CodeModelContent
{
	public boolean isWritable();
	
	
	public boolean isAppendable();
	
	
	public int size();

	
	/// Returns the [CodeParagraph] at the specified `index`.
	///
	/// This index should never go beyond the number of paragraphs as determined by [#size()].
	/// Doing so might result in an undetermined behavior (most likely an exception).
	public CodeParagraph getParagraph(int index);
	
	
	public String getPlainText(int index);
	
	
	public static CodeModelContent ofStrings(String ... paragraphs)
	{
		return new StringArrayCodeModelContent(paragraphs, null);
	}
	
	
	public static CodeModelContent ofDecoratedStrings(ParagraphDecorator d, String ... paragraphs)
	{
		return new StringArrayCodeModelContent(paragraphs, d);
	}
}