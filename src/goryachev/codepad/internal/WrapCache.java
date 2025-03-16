// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;
import goryachev.common.util.CMap;


/**
 * Caches WrapInfo objects for the given model and tab size.
 * The cache can accomodate objects corresponding to different wrap width
 * for the purposes of layout, since the layout code may need to try
 * without and with the vertical scroll bar which affects the wrapping width,
 * but we want to keep the cache content as much as possible.
 */
public class WrapCache
{
	private record Key(int index, int wrapLimit) { }
	
	private CodeModel model;
	private int tabSize;
	private CMap<Key,WrapInfo> data;
	
	
	public WrapCache()
	{
	}
	
	
	public void clear()
	{
		data = null;
	}
	
	
	public void setParameters(CodeModel model, int tabSize)
	{
		if((model != this.model) || (tabSize != this.tabSize))
		{
			this.model = model;
			this.tabSize = tabSize;
			data = null;
		}
	}
	
	
	/**
	 * Returns a non-null WrapInfo.  This method either returns a cached instance,
	 * or creates and places one in the cache. 
	 */
	public WrapInfo getWrapInfo(int modelIndex, int wrapLimit)
	{
		if(data == null)
		{
			data = new CMap<>(64);
		}

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
