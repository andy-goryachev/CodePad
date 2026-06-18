// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;


/// Paragraph Decorator
public interface ParagraphDecorator
{
	public CodeParagraph decorate(CodeModelContent content, int index, String text);
}
