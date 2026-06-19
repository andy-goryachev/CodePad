// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;


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
	
	
	/// Replaces the content between `start` and `end` positions with the new text.
	/// Returns `null` if the content is not writable.
	/// 
	/// @return the result, or null
	public InsertResult replace(TextPos start, TextPos end, String text, boolean undoEnabled);
}