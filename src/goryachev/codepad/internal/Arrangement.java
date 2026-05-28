// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.common.util.CList;


/// Represents the configuration of visible cells, parameters of paragraphs
/// immediately preceing and following the visible area, and the visibility of
/// the scroll bars.
public class Arrangement
{
	record Row(int index, int cellIndex) { }
	
	private final int wrapLimit;
	private final double canvasWidth;
	private final double canvasHeight;
	private final double hsbHeight;
	private final double vsbWidth;
	private int lastColumn;
	// TODO replace with elastic int array [index, cellIndex]
	private final CList<Row> rows = new CList<>();
	
	
	public Arrangement(int wrapLimit, double canvasWidth, double canvasHeight, double hsbHeight, double vsbWidth)
	{
		this.wrapLimit = wrapLimit;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.hsbHeight = hsbHeight;
		this.vsbWidth = vsbWidth;
	}
	
	
	public void addRow(int index, int cellIndex)
	{
		rows.add(new Row(index, cellIndex));
	}


	public int visibleRowCount()
	{
		return rows.size();
	}


	public double getHSBHeight()
	{
		return hsbHeight;
	}


	public double getVSBWidth()
	{
		return vsbWidth;
	}


	public int wrapLimit()
	{
		return wrapLimit;
	}


	public int indexAtRow(int ix)
	{
		return rows.get(ix).index();
	}


	public int cellIndexAtRow(int ix)
	{
		return rows.get(ix).cellIndex();
	}


	public void setLastColumn(int v)
	{
		lastColumn = v;
	}
	
	
	public int lastColumn()
	{
		return lastColumn;
	}
	
	
	public double canvasWidth()
	{
		return canvasWidth;
	}
	
	
	public double canvasHeight()
	{
		return canvasHeight;
	}
}
