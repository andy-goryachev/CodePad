// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;
import goryachev.common.util.CMap;


/// Caches WrapInfo objects.
///
public class CellCache
{
	private CodeModel model;
	private int tabSize;
	private int wrapLimit;
	// TODO implement circular buffer instead.  but for now, just clear the cache when it gets too big
	private static final int MAX_SIZE = 1024;
	private final CMap<Integer,WrapInfo> data;
	
	
	public CellCache(int capacity)
	{
		data = new CMap<>(capacity);
	}
	
	
	// TODO clear or invalidate?
	public void clear()
	{
		data.clear();
	}
	
	
	public void setParameters(CodeModel model, int tabSize, int wrapLimit)
	{
		if((model != this.model) || (tabSize != this.tabSize) || (wrapLimit != this.wrapLimit))
		{
			data.clear();
			this.model = model;
			this.tabSize = tabSize;
			this.wrapLimit = wrapLimit;
		}
	}
	
	
	/**
	 * Returns a non-null WrapInfo.  This method either returns a cached instance,
	 * or creates and places one in the cache. 
	 */
	public WrapInfo getWrapInfo(int modelIndex)
	{
		Integer k = Integer.valueOf(modelIndex);
		WrapInfo wi = data.get(k);
		if(wi == null)
		{
			// TODO replace with a circular buffer
			if(data.size() > MAX_SIZE)
			{
				data.clear();
			}
			
			CodeParagraph par = model.getParagraph(modelIndex);
			wi = WrapInfo.create(par, tabSize, wrapLimit);
			data.put(k, wi);
		}
		return wi;
	}
}
