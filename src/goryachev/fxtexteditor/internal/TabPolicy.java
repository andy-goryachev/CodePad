// Copyright © 2019-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor.internal;
import goryachev.fxtexteditor.ITabPolicy;


/**
 * Tab Policy.
 */
public class TabPolicy
	implements ITabPolicy
{
	private final int tabWidth;
	
	
	public TabPolicy(int tabWidth)
	{
		if(tabWidth <= 0)
		{
			throw new IllegalArgumentException("tabWidth must be >0: " + tabWidth);
		}
		this.tabWidth = tabWidth;
	}
	
	
	public static TabPolicy create(int tabWidth)
	{
		return new TabPolicy(tabWidth);
	}


	public int distanceToNextTabStop(int position)
	{
		int rv = tabWidth - (position % tabWidth);
		return rv == 0 ? tabWidth : rv;
	}


	@Override
	public int nextTabStop(int position)
	{
		int d = distanceToNextTabStop(position);
		return position + d;
	}


	@Override
	public boolean isSimple()
	{
		return tabWidth == 1;
	}
}
