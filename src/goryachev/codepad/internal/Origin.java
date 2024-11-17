// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;


/**
 * Viewport Origin.
 * 
 * @param index the paragraph index
 * @param cellIndex the index of the cell within the paragraph
 * @param xoffset the distance between the first cell and the top of the canvas
 * @param yoffset the distance between the first cell and the top of the canvas
 * TODO or Point2D? 
 */
public record Origin(int index, int cellIndex, double xoffset, double yoffset)
{
	public static final Origin ZERO = new Origin(0, 0, 0.0, 0.0);
}
