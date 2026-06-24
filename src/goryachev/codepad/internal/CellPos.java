// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;


/// Cell Position represents the model index and a cell index from the paragraph start.
/// 
/// @param index the model paragraph index
/// @param cellIndex the cell index in the paragraph
/// 
public record CellPos(int index, int cellIndex) implements Comparable<CellPos>
{
	@Override
	public int compareTo(CellPos x)
	{
		int d = index - x.index;
		if(d == 0)
		{
			return cellIndex - x.cellIndex;
		}
		return d;
	}
	
	// TODO rename GridPos
	// TODO add WrapInfo?
	// OR delete?
}
