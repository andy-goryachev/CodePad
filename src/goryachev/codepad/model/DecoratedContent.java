// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;


/// Decorated CodeModelContent
public abstract class DecoratedContent
	implements CodeModelContent
{
	private ParagraphDecorator decorator;


	public DecoratedContent()
	{
	}
	
	
	public DecoratedContent(ParagraphDecorator d)
	{
		setDecorator(d);
	}


	public final void setDecorator(ParagraphDecorator d)
	{
		this.decorator = d;
	}
	
	
	@Override
	public final CodeParagraph getParagraph(int index)
	{
		String text = getPlainText(index);
		return decorator == null ? CodeParagraph.fast(index, text) : decorator.decorate(this, index, text);
	}
}
