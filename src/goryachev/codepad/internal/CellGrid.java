// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.common.log.Log;
import goryachev.fx.FX;
import goryachev.fx.TextCellMetrics;
import javafx.beans.property.SimpleObjectProperty;
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
	private final CodePadSkin skin;
	private final CodePad editor;
	private final ScrollBar vscroll;
	private final ScrollBar hscroll;
	private final SimpleObjectProperty<Origin> origin = new SimpleObjectProperty<>(Origin.ZERO);
	private Canvas canvas;
	private GraphicsContext gx;
	private TextCellMetrics metrics;
	private Font baseFont;
	private Font boldFont;
	private Font boldItalicFont;
	private Font italicFont;
	private WrapCache cache;
	private Arrangement arrangement;


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
		FX.addInvalidationListener(origin, this::requestLayout);
	}
	
	
	public void setFont(Font f)
	{
		baseFont = f;
		boldFont = null;
		boldItalicFont = null;
		italicFont = null;
		metrics = null;
		cache = null;
		arrangement = null;
		requestLayout();
	}
	
	
	// TODO maybe invalidateXXX instead
	public void setWrapText(boolean on)
	{
		cache = null;
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


	private TextCellMetrics textCellMetrics()
	{
		if(metrics == null)
		{
			Text t = new Text("8");
			t.setFont(baseFont);
			
			getChildren().add(t);
			try
			{
				double fontAspect = 0.8; // TODO property
				Bounds b = t.getBoundsInLocal();
				double w = snapSizeX(b.getWidth() * fontAspect);
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
	
	
	private WrapCache cache(CodeModel model, int tabSize, int wrapLimit)
	{
		if((cache == null) || cache.isNotValidFor(model, tabSize, wrapLimit))
		{
			cache = new WrapCache(model, tabSize, wrapLimit);
		}
		return cache;
	}
	

	@Override
	protected void layoutChildren()
	{
		// TODO two separate steps:
		// 1. compute layout (check if canvas needs to be re-created, origin, scroll bars, ...)
		//    may need to bail out and repeat if the scroll bar visibility changed and the layout needs to be recomputed
		// 2. paint the canvas

		double width = getWidth();
		if(width == 0.0)
		{
			return;
		}
		
		// TODO if model == null: disable scroll bars, paint background
		
		// we have all the information, so we can re-flow in one pass!  steps:
		// - get the canvas size w/o scroll bars, rowCount
		// - is vsb needed? (easy answers: origin > ZERO, rowCount > model.size)
		// - if vsb not needed, lay out w/o vsb.  if does not fit, must use vsb.
		// - determine if hsb is needed.  easy answers(wrap on, unwrapped width > grid.width)
		// - if vsb not needed, but hsb is needed, lay out one more time, vsb may be needed after all
		// - do the layout: view port, N lines after, M lines before (adjusting N,M when close to the model edges)

		boolean wrap = editor.isWrapText();
		int tabSize = tabSize();
		CodeModel model = editor.getModel();
		Insets pad = editor.getContentPadding();
		
		Origin or = origin.get();
		double canvasWidth = snapSizeX(getWidth() - snappedLeftInset() - snappedRightInset());
		double canvasHeight = snapSizeY(getHeight() - snappedTopInset() - snappedBottomInset());
		TextCellMetrics tm = textCellMetrics();
		
		int size = paragraphCount();
		Arrangement arr = null;
		
		// number of full and partial columns visible in viewport
		int viewCols = (int)((canvasWidth - pad.getLeft() - pad.getRight()) / tm.cellWidth);
		int wrapLimit = wrap ? viewCols : -1;
		
		// number of whole rows in the viewport
		int viewRows = (int)(canvasHeight / tm.cellHeight);
		int rowCount = (int)Math.ceil(canvasHeight / tm.cellHeight);
		
		// determine if the vertical scroll bar is needed
		// easy answers first
		boolean vsb = (size > viewRows) || (or.index() > 0);
		if(!vsb)
		{
			// attempt to lay out w/o the vertical scroll bar
			WrapCache wc = cache(model, tabSize, wrapLimit);
			arr = new Arrangement(wc, viewRows, viewCols + 1);
			arr.layoutViewPort(or.index(), or.cellIndex(), rowCount);
			// layout and see if vsb is needed
			if(arr.isVsbNeeded())
			{
				vsb = true;
				arr = null;
			}
		}
		
		double vsbWidth = 0.0;
		double hsbHeight = 0.0;
		
		if(vsb)
		{
			// view got narrower due to vsb
			vsbWidth = snapSizeX(vscroll.prefWidth(-1));
			canvasWidth -= vsbWidth;
			viewCols = (int)((canvasWidth - pad.getLeft() - pad.getRight()) / tm.cellWidth) + 1;
			if(wrap)
			{
				wrapLimit = (int)((canvasWidth - pad.getLeft() - pad.getRight()) / tm.cellWidth); 
			}
		}
		
		if(arr == null)
		{
			WrapCache wc = cache(model, tabSize, wrapLimit);
			arr = new Arrangement(wc, viewRows, viewCols);
			arr.layoutViewPort(or.index(), or.cellIndex(), rowCount);
		}

		// TODO adjust origin if too much whitespace at the end
		
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
		int top = Math.max(0, or.index() - ct);
		ct = or.index() - top;
		if(ct > 0)
		{
			arr.layoutSlidingWindow(top, ct, true);
		}

		// we now have the layout
		boolean hsb = arr.isHsbNeeded();
		if(hsb)
		{
			hsbHeight = snapSizeY(hscroll.prefHeight(-1));
			canvasHeight -= hsbHeight;
		}

		boolean recreateCanvas =
			(canvas == null) || 
			GridUtils.notClose(canvasWidth, canvas.getWidth()) ||
			GridUtils.notClose(canvasHeight, canvas.getHeight());
		if(recreateCanvas)
		{
			if(canvas != null)
			{
				getChildren().remove(canvas);
			}
			
			// create new canvas
			canvas = new Canvas(canvasWidth, canvasHeight);
			gx = canvas.getGraphicsContext2D();
			
			getChildren().add(canvas);
		}
		
		vscroll.setVisible(vsb);
		hscroll.setVisible(hsb);

		arrangement = arr;
		
		paintAll();
		
		double x0 = snappedLeftInset();
		double y0 = snappedTopInset();
		double cw = canvas.getWidth();
		double ch = canvas.getHeight();

		if(vsb)
		{
			layoutInArea(vscroll, x0 + cw, y0, vsbWidth, ch, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
		}
		
		if(hsb)
		{
			layoutInArea(hscroll, x0, y0 + ch, cw, hsbHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
		}
		
		layoutInArea(canvas, x0, y0, cw, ch, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
	}
	
	
	public void paintAll()
	{
		int maxy = arrangement.getVisibleRowCount();
		int maxx = arrangement.getVisibleColumnCount() + 1; // TODO find out why
		TextCellMetrics tm = textCellMetrics();
		
		// attempt to limit the canvas queue
		// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8092801
		// https://github.com/kasemir/org.csstudio.display.builder/issues/174
		// https://stackoverflow.com/questions/18097404/how-can-i-free-canvas-memory
		// https://bugs.openjdk.java.net/browse/JDK-8103438
		gx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// FIX background color
		gx.setFill(Color.WHITE);
		gx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// TODO caret line
		
		double x = 0.0;
		double y = 0.0;
		for(int ix=0; ix<maxy; ix++)
		{
			WrapInfo wi = arrangement.wrapInfoAtViewRow(ix);
			int cellIndex = arrangement.cellIndexAtViewRow(ix);
			int ct = Math.min(maxx, wi.getCellCount() - cellIndex);
			paintCells(tm, wi, cellIndex, ct, x, y);
			y = snapPositionY(y + tm.cellHeight); // TODO line spacing
		}
	}


	// paints a number of cells horizontally
	// TODO move to cell grid?
	private void paintCells(TextCellMetrics tm, WrapInfo wi, int cellIndex, int cellCount, double x, double y)
	{
		// TODO
		Color textColor = Color.BLACK;
		
		for(int i=0; i<cellCount; i++)
		{
			int ix = cellIndex + i;
//			double cx = snapPositionX(x * tm.cellWidth /*+ lineNumbersBarWidth*/);
//			double cy = snapPositionY(y * tm.cellHeight);
			
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
		}
	}
}
