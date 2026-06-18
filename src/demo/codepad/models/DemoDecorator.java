// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package demo.codepad.models;
import goryachev.codepad.model.CodeModelContent;
import goryachev.codepad.model.CodeParagraph;
import goryachev.codepad.model.ParagraphDecorator;


/// Demo Decorator
public class DemoDecorator
	implements ParagraphDecorator
{
	public DemoDecorator()
	{
	}


	@Override
	public CodeParagraph decorate(CodeModelContent content, int index, String text)
	{
		return new DemoParagraph(index, text);
	}
}
