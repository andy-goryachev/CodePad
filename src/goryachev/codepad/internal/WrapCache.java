// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;
import goryachev.common.util.CMap;


/**
 * Caches WrapInfo objects for the given wrapping width.
 */
public class WrapCache
{
	private final CodeModel model;
	private final int tabSize;
	private final int wrapLimit;
	private final CMap<Integer,WrapInfo> wraps;
	
	
	public WrapCache(CodeModel m, int tabSize, int wrapLimit)
	{
		this.model = m;
		this.tabSize = tabSize;
		this.wrapLimit = wrapLimit;
		this.wraps = new CMap<>();
	}
	
	
	public int modelSize()
	{
		return model.size();
	}
	
	
	public int getWrapLimit()
	{
		return wrapLimit;
	}
	
	
	public boolean isNotValidFor(CodeModel m, int tabSize, int wrapLimit)
	{
		return
			(this.model != m) ||
			(this.tabSize != tabSize) ||
			(this.wrapLimit != wrapLimit);
	}
	
	
	public WrapInfo getWrapInfo(int modelIndex)
	{
		Integer k = Integer.valueOf(modelIndex);
		WrapInfo wi = wraps.get(k);
		if(wi == null)
		{
			CodeParagraph par = model.getParagraph(modelIndex);
			wi = WrapInfo.create(par, tabSize, wrapLimit);
			wraps.put(k, wi);
		}
		return wi;
	}
}
