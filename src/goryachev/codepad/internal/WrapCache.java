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
	private record Key(int index, int wrapLimit) { }
	
	private CodeModel model;
	private int tabSize;
	private CMap<Key,WrapInfo> data = new CMap<>();
	
	
	public WrapCache()
	{
	}
	
	
	public void clear()
	{
		data = new CMap<>();
	}
	
	
	public void setParameters(CodeModel model, int tabSize)
	{
		if((model != this.model) && (tabSize != this.tabSize))
		{
			data = new CMap<>();
		}
		this.model = model;
		this.tabSize = tabSize;
	}
	
	
//	public boolean isNotValidFor(CodeModel m, int tabSize, int wrapLimit)
//	{
//		return
//			(this.model != m) ||
//			(this.tabSize != tabSize) ||
//			(this.wrapLimit != wrapLimit);
//	}
	
	
	public WrapInfo getWrapInfo(int modelIndex, int wrapLimit)
	{
		Key k = new Key(modelIndex, wrapLimit);
		WrapInfo wi = data.get(k);
		if(wi == null)
		{
			CodeParagraph par = model.getParagraph(modelIndex);
			wi = WrapInfo.create(par, tabSize, wrapLimit);
			data.put(k, wi);
		}
		return wi;
	}
}
