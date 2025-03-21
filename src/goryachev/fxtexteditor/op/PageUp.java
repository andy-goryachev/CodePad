// Copyright © 2020-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor.op;
import goryachev.common.log.Log;
import goryachev.fxtexteditor.FxTextEditor;
import goryachev.fxtexteditor.Marker;
import goryachev.fxtexteditor.WrapPos;
import goryachev.fxtexteditor.internal.NavigationAction;
import goryachev.fxtexteditor.internal.WrapInfo;


/**
 * Moves the cursor one page up.
 */
public class PageUp
	extends NavigationAction
{
	protected static final Log log = Log.get("PageUp");
	
	
	public PageUp(FxTextEditor ed)
	{
		super(ed);
	}
	
	
	@Override
	public void action()
	{
		super.action();
	}
	

	@Override
	protected Marker move(Marker m)
	{
		int screenHeight = vflow().getScreenRowCount();
		
		vflow().shiftViewPort(-screenHeight);
		
		int pos = m.getCharIndex();
		int line = m.getLine();
		int col = updatePhantomColumn(line, pos);
		
		WrapInfo wr = wrapInfo(line);
		int wrapRow = wr.getWrapRowForCharIndex(pos);
		
		WrapPos wp = vflow().advance(line, wrapRow, -screenHeight);
		int newLine = wp.getLine();
		int newWrapRow = wp.getRow();
		
		wr = wrapInfo(newLine);
		int newPos = wr.getCharIndexForColumn(newWrapRow, col);
		
		log.debug("col=%d line=%d pos=%d", col, newLine, newPos);
		
		return editor().newMarker(newLine, newPos);
	}
}
