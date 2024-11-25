// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.common.log.Log;
import goryachev.fx.FX;
import goryachev.fx.TextCellMetrics;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


/**
 * Cell Grid.
 * 
 * Contains:
 * - canvas
 * - vertical and horizontal scroll bars
 */
public class CellGrid
	extends Pane
{
	private static final Log log = Log.get("CellGrid");
	private final WrapCache cache = new WrapCache();
	private final CodePadSkin skin;
	private final CodePad editor;
	private final ScrollBar vscroll;
	private final ScrollBar hscroll;
	private Origin origin = Origin.ZERO;
	private Canvas canvas;
	private GraphicsContext gx;
	private TextCellMetrics metrics;
	private Font baseFont;
	private Font boldFont;
	private Font boldItalicFont;
	private Font italicFont;
	private Arrangement arrangement;
	private double aspectRatio;
	private double contentPaddingTop;
	private double contentPaddingBottom;
	private double contentPaddingLeft;
	private double contentPaddingRight;


	public CellGrid(CodePadSkin skin, ScrollBar vscroll, ScrollBar hscroll)
	{
		this.skin = skin;
		this.editor = skin.getSkinnable();
		this.vscroll = configureScrollBar(vscroll);
		this.hscroll = configureScrollBar(hscroll);

		getChildren().addAll(vscroll, hscroll);
		
		FX.addInvalidationListener(widthProperty(), this::handleWidthChange);
		FX.addInvalidationListener(heightProperty(), this::handleHeightChange);
		FX.addInvalidationListener(scaleXProperty(), this::handleScaleChange);
		FX.addInvalidationListener(scaleYProperty(), this::handleScaleChange);
	}
	
	
	public void setAspectRatio(Number value)
	{
		double v = (value == null) ? Defaults.ASPECT_RATIO : value.doubleValue();
		if(v < Defaults.ASPECT_RATIO_MIN)
		{
			v = Defaults.ASPECT_RATIO_MIN;
		}
		else if(v > Defaults.ASPECT_RATIO_MAX)
		{
			v = Defaults.ASPECT_RATIO_MAX;
		}
		
		aspectRatio = v;
		metrics = null;
		cache.clear();
		arrangement = null;
		requestLayout();
	}
	
	
	public void setFont(Font f)
	{
		baseFont = f;
		boldFont = null;
		boldItalicFont = null;
		italicFont = null;
		metrics = null;
		cache.clear();
		arrangement = null;
		requestLayout();
	}
	
	
	public void handleModelChange()
	{
		cache.clear();
		arrangement = null;
		requestLayout();
	}


	public void setContentPadding(Insets m)
	{
		if(m == null)
		{
			m = Insets.EMPTY;
		}

		contentPaddingTop = snapPositionY(m.getTop());
		contentPaddingBottom = snapPositionY(m.getBottom());
		contentPaddingLeft = snapPositionX(m.getLeft());
		contentPaddingRight = snapPositionX(m.getRight());

		// TODO set horizontal scroll to 0

		if(origin.index() == 0)
		{
			origin = new Origin(0, 0, contentPaddingLeft, contentPaddingTop);
		}

		// TODO update origin
		cache.clear();
		arrangement = null;
		requestLayout();
	}


	// TODO maybe invalidateXXX instead
	public void setWrapText(boolean on)
	{
		origin = new Origin(origin.index(), 0, contentPaddingLeft, contentPaddingTop);
		cache.clear();
		arrangement = null;
		requestLayout();
	}
	
	
	private static ScrollBar configureScrollBar(ScrollBar b)
	{
		b.setManaged(false);
		b.setMin(0.0);
		b.setMax(1.0);
		b.setUnitIncrement(0.01);
		b.setBlockIncrement(0.05);
		b.setVisible(false);
		return b;
	}
	
	
	void handleWidthChange()
	{
		// TODO scroll horizontally
		requestLayout();
	}
	
	
	void handleHeightChange()
	{
		requestLayout();
	}
	
	
	void handleScaleChange()
	{
		requestLayout();
	}
	
	
	private int paragraphCount()
	{
		CodeModel m = editor.getModel();
		return (m == null) ? 0 : m.size();
	}


	private int tabSize()
	{
		int v = editor.getTabSize();
		if(v < 1)
		{
			return 1;
		}
		else if(v > Defaults.TAB_SIZE_MAX)
		{
			return Defaults.TAB_SIZE_MAX;
		}
		return v;
	}
	
	
	private double lineSpacing()
	{
		return snapSpaceY(editor.getLineSpacing());
	}


	private TextCellMetrics textCellMetrics()
	{
		if(metrics == null)
		{
			Text t = new Text("8");
			t.setFont(baseFont);
			
			getChildren().add(t);
			try
			{
				Bounds b = t.getBoundsInLocal();
				double w = snapSizeX(b.getHeight() * aspectRatio);
				double h = snapSizeY(b.getHeight());
				double baseLine = b.getMinY();
				metrics = new TextCellMetrics(baseFont, baseLine, w, h);
			}
			finally
			{
				getChildren().remove(t);
			}
		}
		return metrics;
	}
	
	
	private Color backgroundColor(boolean caretLine, boolean selected, Color lineColor, Color cellBG)
	{
//		Color c = editor.getBackgroundColor();
//		
//		if(lineColor !=  null)
//		{
//			c = mixColor(c, lineColor, LINE_COLOR_OPACITY);
//		}
//		
//		if(caretLine)
//		{
//			c = mixColor(c, editor.getCaretLineColor(), CARET_LINE_OPACITY);
//		}
//		
//		if(selected)
//		{
//			c = mixColor(c, editor.getSelectionBackgroundColor(), SELECTION_BACKGROUND_OPACITY);
//		}
//		
//		if(cellBG != null)
//		{
//			c = mixColor(c, cellBG, CELL_BACKGROUND_OPACITY);
//		}
//		
//		return c;
		return null;
	}
	
	
	private Font getFont(CellStyle st)
	{
		if(st.isBold())
		{
			if(st.isItalic())
			{
				return boldItalicFont;
			}
			else
			{
				return boldFont;
			}
		}
		else
		{
			if(st.isItalic())
			{
				return italicFont;
			}
			else
			{
				return baseFont;
			}
		}
	}
	
//	private WrapCache createCache(CodeModel model, int tabSize, int wrapLimit, WrapCache old)
//	{
//		if((old == null) || old.isNotValidFor(model, tabSize, wrapLimit))
//		{
//			return new WrapCache(model, tabSize, wrapLimit);
//		}
//		return old;
//	}
	
	@Override
	protected void layoutChildren()
	{
		// TODO two separate steps:
		// 1. compute layout (check if canvas needs to be re-created, origin, scroll bars, ...)
		//    may need to bail out and repeat if the scroll bar visibility changed and the layout needs to be recomputed
		// 2. paint the canvas
		//
		// detailed process:
		// - get the canvas size w/o scroll bars, rowCount
		// - is vsb needed? (easy answers: origin > ZERO, rowCount > model.size)
		// - if vsb not needed, lay out w/o vsb.  if does not fit, must use vsb.
		// - determine if hsb is needed.  easy answers(wrap on, unwrapped width > grid.width)
		// - if vsb not needed, but hsb is needed, lay out one more time, vsb may be needed after all
		// - do the layout: view port, N lines after, M lines before (adjusting N,M when close to the model edges)

		double width = getWidth();
		if(width == 0.0)
		{
			return;
		}
		
		double x0 = snappedLeftInset();
		double y0 = snappedTopInset();
		double canvasWidth = snapSizeX(getWidth()) - snappedLeftInset() - snappedRightInset();
		double canvasHeight = snapSizeY(getHeight()) - snappedTopInset() - snappedBottomInset();

		CodeModel model = editor.getModel();
		if(model == null)
		{
			// blank screen
			vscroll.setVisible(false);
			hscroll.setVisible(false);
			ensureCanvas(canvasWidth, canvasHeight);
			clearCanvas();
			layoutInArea(canvas, x0, y0, canvasWidth, canvasHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
			return;
		}
		
		int size = paragraphCount();
		boolean wrap = editor.isWrapText();
		int tabSize = tabSize();
		double lineSpacing = lineSpacing();
		TextCellMetrics tm = textCellMetrics();
		Arrangement arr = null;

		// number of rows that result in no vsb
		int viewRows = (int)((canvasHeight - contentPaddingTop - contentPaddingBottom) / (tm.cellHeight + lineSpacing));
		// number of columns that result in no hsb
		int viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
		int wrapLimit = wrap ? viewCols : -1;
		
		cache.setParameters(model, tabSize);
		
		// TODO using cell cache, try to determine:
		// - whether the origin needs to change
		// - whether the vsb is visible
		// - whether the hsb is visible
		// and only then create the arrangement
		// (flags: origin change, vsb changed, hsb changed)
		
		boolean vsb;
		if(wrap)
		{
			// make an easy check if vsb is needed
			if(size > viewRows)
			{
				vsb = true;
			}
			else
			{
				// make another check for vsb visibility, this time by actually wrapping the text rows
				
				// start with assumption that vsb is not needed
				
				// TODO
				// at this point, we might have a populating cache with vsb on - something we might want to retain
				// in case it will be determined that vsb is needed after all 

				vsb = false;
				boolean reachedEnd = false;
				int rcount = 0;
				boolean run = true;
				int firstRow = -1;
				
				while(run)
				{
					rcount = 0;
					reachedEnd = false;
					firstRow = -1;

					int ix = origin.index();
					int cix = origin.cellIndex();
					WrapInfo wi = null;
					
					for(;;)
					{
						if(ix >= size)
						{
							reachedEnd = true;
							break;
						}
						
						// TODO use the right cache (pass tabsize, wraplimit)
						wi = cache.getWrapInfo(ix, wrapLimit);
						
						if(cix == 0)
						{
							rcount += wi.getRowCount();
						}
						else
						{
							firstRow = wi.getRowAtCellIndex(cix);
							rcount += (wi.getRowCount() - firstRow);
							cix = 0;
						}
						
						ix++;
						
						if(rcount > viewRows)
						{
							vsb = true;
							// FIX update wrap limit, canvaswidth
							// reflow again
							break;
						}
						else
						{
							// no more reflow is needed
							run = false;
						}
					}
				}
				
				if(reachedEnd)
				{
					// move the origin to fill in the bottom
					int ct = viewRows - rcount;
					int ix = origin.index();
					int cix = origin.cellIndex();
					
					while((ix > 0) && (ct > 0))
					{
						WrapInfo wi = cache.getWrapInfo(ix, wrapLimit);
						
						if(firstRow > 0)
						{
							if(firstRow < ct)
							{
								ct -= firstRow;
								firstRow = 0;
								ix--;
							}
							else
							{
								firstRow -= ct;
								cix = wi.getCellIndexAtRow(firstRow);
								double yoffset = ix == 0 ? contentPaddingTop : 0.0;
								origin = new Origin(ix, cix, origin.xoffset(), yoffset);
								break;
							}
						}
						
						int rc = wi.getRowCount();
						if(rc < ct)
						{
							--ix;
							--ct;
						}
						else
						{
							cix = wi.getCellIndexAtRow(rc - ct);
							double yoffset = ix == 0 ? contentPaddingTop : 0.0;
							origin = new Origin(ix, cix, origin.xoffset(), yoffset);
							break;
						}
					}
				}
			}
		}
		else
		{
			// is vsb visible?
			vsb = (size > viewRows);
			// change origin if needed
			if(origin.index() > 0)
			{
				if(size - origin.index() < viewRows)
				{
					// move the origin to fill in the bottom
					int ix = Math.max(0, size - viewRows);
					double pad = (ix == 0) ? contentPaddingTop : 0.0;
					origin = new Origin(ix, origin.cellIndex(), origin.xoffset(), pad);
				}
			}
		}
		
		// origin is set correctly now
		// lay out the arrangement
		
		arr = new Arrangement(cache, viewRows, wrapLimit);
		arr.layoutViewPort(origin.index(), origin.cellIndex(), viewRows, size);

		// number of full and partial columns visible in viewport
//		int viewCols = wrap ?
//			(int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth) :
//			(int)Math.ceil(canvasWidth / tm.cellWidth);
//		int wrapLimit = wrap ? viewCols : -1;
//		
//		// number of whole rows in the viewport
//		int rowCount = (int)Math.ceil(canvasHeight / tm.cellHeight);
//		
//		// determine if the vertical scroll bar is needed
//		// easy answers first
//		boolean vsb = (size > viewRows) || (origin.index() > 0);
//		if(!vsb)
//		{
//			// attempt to lay out w/o the vertical scroll bar
//			WrapCache wc = cache(model, tabSize, wrapLimit);
//			arr = new Arrangement(wc, viewRows, viewCols);
//			arr.layoutViewPort(origin.index(), origin.cellIndex(), rowCount);
//			// layout and see if vsb is needed
//			if(arr.isVsbNeeded())
//			{
//				vsb = true;
//				arr = null;
//			}
//		}
		
		double vsbWidth = 0.0;
		double hsbHeight = 0.0;
		
//		if(vsb)
//		{
//			// view got narrower due to vsb
//			vsbWidth = snapSizeX(vscroll.prefWidth(-1));
//			canvasWidth -= vsbWidth;
//			viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
//			if(wrap)
//			{
//				wrapLimit = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth); 
//			}
//		}

		// lay out bottom half of the sliding window
		int last = arr.getLastIndex();
		int max = Math.min(size, last + Defaults.SLIDING_WINDOW_HALF);
		int ct = arr.layoutSlidingWindow(last, Defaults.SLIDING_WINDOW_HALF, false); 
		if(ct < Defaults.SLIDING_WINDOW_HALF)
		{
			ct = (Defaults.SLIDING_WINDOW_HALF - ct) + Defaults.SLIDING_WINDOW_HALF;
		}
		else
		{
			ct = Defaults.SLIDING_WINDOW_HALF;
		}
		
		// layout upper half of the sliding window
		int top = Math.max(0, origin.index() - ct);
		ct = origin.index() - top;
		if(ct > 0)
		{
			arr.layoutSlidingWindow(top, ct, true);
		}

		boolean hsb = arr.isHsbNeeded();
		if(hsb)
		{
			hsbHeight = snapSizeY(hscroll.prefHeight(-1));
			canvasHeight -= hsbHeight;
		}
		
		ensureCanvas(canvasWidth, canvasHeight);

		vscroll.setVisible(vsb);
		hscroll.setVisible(hsb);

		arrangement = arr;
		
		paintAll();
		
		if(vsb)
		{
			layoutInArea(vscroll, x0 + canvasWidth, y0, vsbWidth, canvasHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
		}
		
		if(hsb)
		{
			layoutInArea(hscroll, x0, y0 + canvasHeight, canvasWidth, hsbHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
		}
		
		layoutInArea(canvas, x0, y0, canvasWidth, canvasHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
	}
	
	
	private void ensureCanvas(double w, double h)
	{
		boolean recreateCanvas =
			(canvas == null) || 
			GridUtils.notClose(w, canvas.getWidth()) ||
			GridUtils.notClose(h, canvas.getHeight());
		if(recreateCanvas)
		{
			if(canvas != null)
			{
				getChildren().remove(canvas);
			}
			
			// create new canvas
			canvas = new Canvas(w, h);
			gx = canvas.getGraphicsContext2D();
			
			getChildren().add(canvas);
		}
	}
	
	
	private void clearCanvas()
	{
		// attempt to limit the canvas queue
		// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8092801
		// https://github.com/kasemir/org.csstudio.display.builder/issues/174
		// https://stackoverflow.com/questions/18097404/how-can-i-free-canvas-memory
		// https://bugs.openjdk.java.net/browse/JDK-8103438
		gx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// FIX background color
		gx.setFill(Color.WHITE);
		gx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	
	public void paintAll()
	{
		int maxy = arrangement.getVisibleRowCount();
		int wrapLimit = arrangement.getWrapLimit();
		TextCellMetrics tm = textCellMetrics();
		boolean wrap = editor.isWrapText();
		double lineSpacing = lineSpacing();
		
		clearCanvas();
		
		// TODO caret line
		
		double x = origin.xoffset();
		double y = origin.yoffset();
		
		for(int ix=0; ix<maxy; ix++)
		{
			WrapInfo wi = arrangement.wrapInfoAtViewRow(ix);
			int cellIndex = arrangement.cellIndexAtViewRow(ix);
			int ct = wrapLimit < 0 ? wi.getCellCount() : Math.min(wrapLimit, wi.getCellCount() - cellIndex);
			paintCells(tm, wi, cellIndex, ct, x, y);
			y = snapPositionY(y + tm.cellHeight + lineSpacing);
		}
	}


	// paints a number of cells horizontally
	// TODO cell count, or until x > canvasWidth
	private void paintCells(TextCellMetrics tm, WrapInfo wi, int cellIndex, int cellCount, double x, double y)
	{
		double maxx = canvas.getWidth();
		
		// TODO
		Color textColor = Color.BLACK;
		
		for(int i=0; i<cellCount; i++)
		{
			int ix = cellIndex + i;
			
			// TODO
//			int line = row.getLineNumber();
//			int flags = SelectionHelper.getFlags(this, editor.selector.getSelectedSegment(), line, cell, x);
			boolean caretLine = false; //SelectionHelper.isCaretLine(flags);
			boolean caret = false; //SelectionHelper.isCaret(flags);
			boolean selected = false; //SelectionHelper.isSelected(flags);
			
			// style
			CellStyle style = wi.getCellStyle(ix);
			if(style == null)
			{
				style = CellStyle.EMPTY;
			}
			
//			TextCellStyle style = row.getCellStyles(cell);
//			if(style == null)
//			{
//				style = TextCellStyle.NONE;
//			}
			
			// background
			Color bg = backgroundColor(caretLine, selected, wi.getBackgroundColor(), style.getBackgroundColor());
			if(bg != null)
			{
				gx.setFill(bg);
				gx.fillRect(x, y, tm.cellWidth, tm.cellHeight);
			}
			
			// caret
//			if(paintCaret.get())
//			{
//				if(caret)
//				{
//					// TODO insert mode
//					gx.setFill(caretColor);
//					gx.fillRect(cx, cy, 2, tm.cellHeight);
//				}
//			}
			
			if(style.isUnderline())
			{
				// TODO special property, mix with background
				gx.setFill(textColor);
				gx.fillRect(x, y + tm.cellHeight - 1, tm.cellWidth, 1);
			}
			
			// text
			String text = wi.getCellText(ix);
			if(text != null)
			{
				Color fg = style.getTextColor();
				if(fg == null)
				{
					fg = textColor;
				}
				
				Font f = getFont(style);
				gx.setFont(f);
				gx.setFill(fg);
				gx.fillText(text, x, y - tm.baseLine, tm.cellWidth);
			
				if(style.isStrikeThrough())
				{
					// TODO special property, mix with background
					gx.setFill(textColor);
					gx.fillRect(x, y + tm.cellHeight/2, tm.cellWidth, 1);
				}
			}
			
			x = snapPositionX(x + tm.cellWidth);
			if(x > maxx)
			{
				break;
			}
		}
	}
}
