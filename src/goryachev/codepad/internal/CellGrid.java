// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.codepad.utils.CodePadUtils;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


/**
 * Cell Grid.
 * 
 * Renders text in a rectangular grid.
 * Contains and manages the canvas and the both scroll bars.
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
	private boolean handleScrollEvents = true;
	// number of rows that result in no vsb
	private int viewRows;
	// number of columns that result in no hsb
	private int viewCols;
	private int wrapLimit;


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
	
	
	private void setOrigin(int index, int cellIndex, double xoffset, double yoffset)
	{
		log.debug("index=%d, cellIndex=%d, xoffset=%f, yoffset=%f", index, cellIndex, xoffset, yoffset);
		origin = new Origin(index, cellIndex, xoffset, yoffset);
		arrangement = null;
	}
	
	
	public void handleModelChange()
	{
		cache.clear();
		arrangement = null;
		requestLayout();
	}
	
	
	public void handleLineSpacingChange()
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
			setOrigin(0, 0, contentPaddingLeft, contentPaddingTop);
		}

		// TODO update origin
		cache.clear();
		arrangement = null;
		requestLayout();
	}


	// TODO maybe invalidateXXX instead
	public void setWrapText(boolean on)
	{
		setOrigin(origin.index(), 0, contentPaddingLeft, contentPaddingTop);
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
		FX.consumeAllEvents(ScrollEvent.ANY, b);
		return b;
	}
	
	
	void handleWidthChange()
	{
		// TODO scroll horizontally
		arrangement = null;
		requestLayout();
	}
	
	
	void handleHeightChange()
	{
		arrangement = null;
		requestLayout();
	}
	
	
	void handleScaleChange()
	{
		arrangement = null;
		requestLayout();
	}


	public void handleHorizontalScroll()
	{
		// TODO
	}


	public void handleVerticalScroll()
	{
		if(handleScrollEvents)
		{
			if(editor.getParagraphCount() == 0)
			{
				return;
			}

			double val = vscroll.getValue();
			double max = vscroll.getMax();
			double min = vscroll.getMin();
			double pos = (val - min) / max;

			Arrangement ar = arrangement();

			// TODO move to arrangement?
			int size = ar.getModelSize();
			double av = ar.averageRowsPerParagraph();
			double topEst = ar.getTopIndex() * av;
			double botEst = (size - ar.getBottomIndex()) * av;
			double estTotalRows = topEst + botEst + ar.getSlidingWindowRowCount();

			// TODO perhaps the whole thing can be moved to arr.
			double targetRow = Math.round(estTotalRows * pos);

			int ix;
			int cix;
			if(targetRow < topEst)
			{
				// before the sliding window
				ix = (int)Math.round(targetRow / av);
				cix = 0;
			}
			else if(targetRow < (size - botEst))
			{
				// inside the sliding window
				int[] rv = ar.findRow((int)(targetRow - topEst));
				ix = rv[0];
				cix = rv[1];
			}
			else
			{
				// after the sliding window
				ix = (int)Math.round(size - (size - targetRow) / av);
				cix = 0;
			}

			double yoff = ix == 0 ? contentPaddingTop : 0.0;
			setOrigin(ix, cix, origin.xoffset(), yoff);

			requestLayout();
		}
	}


	protected void updateVerticalScrollBar()
	{
		double vis;
		double val;
		if(editor.getParagraphCount() == 0)
		{
			vis = 1.0;
			val = 0.0;
		}
		else
		{
			// unless the arrangement encompasses the whole model, we need to approximate,
			// using the average row count per paragraph obtained from the sliding window. 
			Arrangement ar = arrangement();
			
			// TODO move to arrangement?
			double av = ar.averageRowsPerParagraph();
			double topEst = ar.getTopIndex() * av;
			double botEst = (ar.getModelSize() - ar.getBottomIndex()) * av;
			double estTotalRows = topEst + botEst + ar.getSlidingWindowRowCount();
			
			val = CodePadUtils.toScrollBarValue(topEst + ar.getTopRowCount(), viewRows, estTotalRows);
			vis = viewRows / estTotalRows;
		}

		handleScrollEvents = false;

		vscroll.setValue(val);
		vscroll.setVisibleAmount(vis);

		handleScrollEvents = true;
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
		Color c = editor.getBackgroundColor();
		
		if(lineColor !=  null)
		{
			c = CodePadUtils.mixColor(c, lineColor, Defaults.LINE_COLOR_OPACITY);
		}
		
		if(caretLine)
		{
			c = CodePadUtils.mixColor(c, editor.getCaretLineColor(), Defaults.CARET_LINE_OPACITY);
		}
		
		if(selected)
		{
			c = CodePadUtils.mixColor(c, editor.getSelectionBackgroundColor(), Defaults.SELECTION_BACKGROUND_OPACITY);
		}
		
		if(cellBG != null)
		{
			c = CodePadUtils.mixColor(c, cellBG, Defaults.CELL_BACKGROUND_OPACITY);
		}
		
		return c;
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
	
	
	private Arrangement arrangement()
	{
		if(arrangement == null)
		{
			int size = editor.getParagraphCount();
			int ix = origin.index();
			Arrangement a = new Arrangement(cache, size, viewCols, wrapLimit);
			a.layoutViewPort(ix, origin.cellIndex(), viewRows);

			// lay out bottom half of the sliding window
			int last = a.getLastViewIndex();
			int ct = a.layoutSlidingWindow(last, Defaults.SLIDING_WINDOW_HALF, false); 
			if(ct < Defaults.SLIDING_WINDOW_HALF)
			{
				ct = (Defaults.SLIDING_WINDOW_HALF - ct) + Defaults.SLIDING_WINDOW_HALF;
			}
			else
			{
				ct = Defaults.SLIDING_WINDOW_HALF;
			}
			
			// layout upper half of the sliding window
			int top = Math.max(0, ix - ct);
			ct = ix - top;
			if(ct > 0)
			{
				a.layoutSlidingWindow(top, ct, true);
			}
			arrangement = a;
		}
		return arrangement;
	}
	
	
	@Override
	protected void layoutChildren()
	{
		// 1. determines whether the vertical scroll bar is visible, and whether the origin needs to be changed
		//    because there is too much of empty space at the bottom of the viewport.
		// 2. generates the cell arrangement which includes the viewport and a sliding window above and below the viewport.
		// 2. paints the text onto the canvas.

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
		
		int size = editor.getParagraphCount();
		boolean wrap = editor.isWrapText();
		int tabSize = tabSize();
		double lineSpacing = lineSpacing();
		TextCellMetrics tm = textCellMetrics();

		viewRows = (int)((canvasHeight - contentPaddingTop - contentPaddingBottom) / (tm.cellHeight + lineSpacing));
		viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
		wrapLimit = wrap ? viewCols : -1;
		
		double vsbWidth = 0.0;
		double hsbHeight = 0.0;
		
		cache.setParameters(model, tabSize);
		
		boolean vsb;
		if(wrap)
		{
			// make an easy check if vsb is needed
			if(size > viewRows)
			{
				vsb = true;
				vsbWidth = snapSizeX(vscroll.prefWidth(-1));
				canvasWidth -= vsbWidth;
				viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
			}
			else
			{
				// make another check for vsb visibility, this time by actually wrapping the text rows
				// start with assumption that vsb is not needed
				vsb = false;
				int nrows;
				int firstRow;
				boolean run = false;
				boolean reachedEnd = false;
				
				do
				{
					nrows = 0;
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
							run = false;
							break;
						}
						
						wi = cache.getWrapInfo(ix, wrapLimit);
						
						if(cix == 0)
						{
							nrows += wi.getRowCount();
						}
						else
						{
							firstRow = wi.getRowAtCellIndex(cix);
							nrows += (wi.getRowCount() - firstRow);
							cix = 0;
						}
						
						if(nrows > viewRows)
						{
							if(vsb)
							{
								run = false;
							}
							else
							{
								vsb = true;
								vsbWidth = snapSizeX(vscroll.prefWidth(-1));
								canvasWidth -= vsbWidth;
								viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
								wrapLimit = viewCols;
								// reflow again
								run = true;
							}
							break;
						}
						
						ix++;						
					}
				} while(run);
				
				if(reachedEnd)
				{
					// move the origin to fill in the viewport
					int ct = viewRows - nrows;
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
								setOrigin(ix, cix, origin.xoffset(), yoffset);
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
							setOrigin(ix, cix, origin.xoffset(), yoffset);
							break;
						}
					}
				}
			}
		}
		else
		{
			// non-wrapped mode is much easier
			
			// is vsb visible?
			vsb = (size > viewRows);
			if(vsb)
			{
				vsbWidth = snapSizeX(vscroll.prefWidth(-1));
				canvasWidth -= vsbWidth;
				viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
			}
			
			// change origin if needed
			if(origin.index() > 0)
			{
				if(size - origin.index() < viewRows)
				{
					// move the origin to fill in the bottom
					int ix = Math.max(0, size - viewRows);
					double pad = (ix == 0) ? contentPaddingTop : 0.0;
					setOrigin(ix, origin.cellIndex(), origin.xoffset(), pad);
				}
			}
		}
		
		// origin, vsb are set correctly now, ready for layout
		Arrangement arr = arrangement();

		boolean hsb = arr.isHsbNeeded();
		if(hsb)
		{
			hsbHeight = snapSizeY(hscroll.prefHeight(-1));
			canvasHeight -= hsbHeight;
		}
		
		ensureCanvas(canvasWidth, canvasHeight);

		vscroll.setVisible(vsb);
		hscroll.setVisible(hsb);

		// paint all
		paintAll();
		
		// layout
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
		gx.setFill(editor.getBackgroundColor());
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
		Color textColor = editor.getTextColor();
		
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
			
			// background
			Color bg = backgroundColor(caretLine, selected, wi.getBackgroundColor(), style.getBackgroundColor());
			if(bg != null)
			{
				gx.setFill(bg);
				gx.fillRect(x, y, tm.cellWidth, tm.cellHeight);
			}
			
			// TODO caret
//			if(paintCaret.get())
//			{
				if(caret)
				{
					// TODO insert mode
					gx.setFill(editor.getCaretColor());
					gx.fillRect(x, y, 2, tm.cellHeight);
				}
//			}
			
			if(style.isUnderline())
			{
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
					gx.setFill(textColor);
					gx.fillRect(x, y + tm.cellHeight / 2.0, tm.cellWidth, 0.5);
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
