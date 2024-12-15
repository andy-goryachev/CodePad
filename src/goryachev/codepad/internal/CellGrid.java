// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.SelectionRange;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.codepad.utils.CodePadUtils;
import goryachev.common.log.Log;
import goryachev.fx.FX;
import goryachev.fx.FxBooleanBinding;
import goryachev.fx.TextCellMetrics;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;


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
	private final SimpleBooleanProperty caretEnabledProperty = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty suppressBlink = new SimpleBooleanProperty(false);
	private final BooleanExpression paintCaret;
	private Timeline cursorAnimation;
	private boolean cursorOn = true;
	private boolean highlightCaretLine;


	public CellGrid(CodePadSkin skin, ScrollBar vscroll, ScrollBar hscroll)
	{
		this.skin = skin;
		this.editor = skin.getSkinnable();
		this.vscroll = configureScrollBar(vscroll);
		this.hscroll = configureScrollBar(hscroll);
		
		setBorder(Border.stroke(Color.TRANSPARENT));

		getChildren().addAll(vscroll, hscroll);
		
		paintCaret = new FxBooleanBinding(caretEnabledProperty, editor.displayCaretProperty(), editor.focusedProperty(), editor.disabledProperty(), suppressBlink)
		{
			@Override
			protected boolean computeValue()
			{
				return
					(caretEnabledProperty.get() || suppressBlink.get()) &&
					editor.isDisplayCaret() &&
					editor.isFocused() &&
					(!editor.isDisabled());
			}
		};
		paintCaret.addListener((s,p,c) -> refreshCursor());
		
		FX.addInvalidationListener(widthProperty(), this::handleWidthChange);
		FX.addInvalidationListener(heightProperty(), this::handleHeightChange);
		FX.addInvalidationListener(scaleXProperty(), this::handleScaleChange);
		FX.addInvalidationListener(scaleYProperty(), this::handleScaleChange);
		
		// FIX parent AND display caret AND model != null
		FX.parentWindowProperty(this).addListener((s,p,c) -> updateCursorAnimation(c));
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


	private boolean setOrigin(int index, int cellIndex, double xoffset, double yoffset)
	{
		log.debug("index=%d, cellIndex=%d, xoffset=%f, yoffset=%f", index, cellIndex, xoffset, yoffset);
		Origin or = new Origin(index, cellIndex, xoffset, yoffset);
		if(!origin.equals(or))
		{
			origin = or;
			arrangement = null;
			return true;
		}
		return false;
	}
	
	
	private void refreshCursor()
	{
		SelectionRange sel = editor.getSelection();
		// TODO create binding of(model, parent window, non-null selection)
		if(sel != null)
		{
			TextPos caret = sel.getCaret();
			if(isVisible(caret))
			{
				// TODO repaint only the damaged area
				paintAll();
			}
		}
	}
	
	
	private boolean isVisible(TextPos p)
	{
		return p == null ? false : arrangement().isVisible(p);
	}
	
	
	public TextPos getTextPos(Point2D local)
	{
		double x = local.getX() - origin.xoffset();
		double y = local.getY() - origin.yoffset();
		Arrangement a = arrangement();
		TextCellMetrics tm = textCellMetrics();
		int row = (int)(y / (tm.cellHeight + lineSpacing()));
		int col = (int)Math.round(x / tm.cellWidth);
		int ix = a.indexAtViewRow(row);
		if(ix < 0)
		{
			return null;
		}
		int cix = a.cellIndexAtViewRow(row) + col;
		// TODO clamp?
		return new TextPos(ix, cix);
	}

	
	private void blinkCursor()
	{
		cursorOn = !cursorOn;
		refreshCursor();
	}
	
	
	public void suppressBlinking(boolean on)
	{
		suppressBlink.set(on);
		cursorOn = true;
		// TODO repaint caret cell?
	}
	
	
	private void updateCursorAnimation(Window w)
	{
		if(w == null)
		{
			if(cursorAnimation != null)
			{
				log.trace("stopping cursor animation");
				cursorAnimation.stop();
				cursorAnimation = null;
			}
		}
		else
		{
			if(cursorAnimation == null)
			{
				log.trace("starting cursor animation");
				cursorAnimation = createCursorAnimation();
				cursorOn = true;
			}
		}
	}
	
	
	private Timeline createCursorAnimation()
	{
		Timeline t = new Timeline(new KeyFrame(Duration.millis(500), (ev) -> blinkCursor()));
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		return t;
	}


	public void handleModelChange()
	{
		setOrigin(0, 0, contentPaddingLeft, contentPaddingTop);
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

		contentPaddingTop = snapSizeY(m.getTop());
		contentPaddingBottom = snapSizeY(m.getBottom());
		contentPaddingLeft = snapSizeX(m.getLeft());
		contentPaddingRight = snapSizeX(m.getRight());

		// TODO set horizontal scroll to 0

		if(origin.index() == 0)
		{
			setOrigin(0, 0, contentPaddingLeft, contentPaddingTop);
		}

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
	
	
	private void handleWidthChange()
	{
		// TODO scroll horizontally
		arrangement = null;
		requestLayout();
	}
	
	
	private void handleHeightChange()
	{
		arrangement = null;
		requestLayout();
	}
	
	
	private void handleScaleChange()
	{
		arrangement = null;
		requestLayout();
	}
	
	
	public void handleSelectionChange(Object src, SelectionRange old, SelectionRange sel)
	{
		// TODO repaint damaged area: union of old and new selection ranges
		paintAll();
	}


	public void handleHorizontalScroll()
	{
		// TODO
	}


	public void handleVerticalScroll()
	{
		if(handleScrollEvents)
		{
			int size = editor.getParagraphCount(); 
			if(size == 0)
			{
				return;
			}
			
			// 1. place the origin roughly at [(size - viewRows) * pos]
			// 2. compute arrangement
			// 3. if arr.rowCount > slidingWindowSize, shift the origin down by [(arr.rowCount - slidingWindowSize) * pos]

			double val = vscroll.getValue();
			
			// FIX
			//val = 1;
			
			double max = vscroll.getMax();
			double min = vscroll.getMin();
			double pos = (val - min) / max;
			
			// 1. rough estimate
			int ix = Math.max(0, (int)Math.round(size * pos));
			int cix = 0;
			setOrigin(ix, cix, origin.xoffset(), 0);
			arrangement = null;
			
			// 2. compute arrangement
			Arrangement ar = arrangement();
			log.debug(ar);
			
			// 3. adjust
			int space = ar.getRowCount() - viewRows;
			if(space > 0)
			{
				int d = (int)Math.round(space * pos);
				int[] rv = ar.findRow(d);
				ix = rv[0];
				cix = rv[1];
			}

			double yoff = ix == 0 ? contentPaddingTop : 0.0;
			if(setOrigin(ix, cix, origin.xoffset(), yoff))
			{
				requestLayout();
			}
		}
	}


	protected void updateVerticalScrollBar()
	{
		double vis;
		double val;
		
		int size = editor.getParagraphCount(); 
		if(size == 0)
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
			double top = ar.getTopIndex();
			double btm = (size - ar.getBottomIndex());
			double totalRows = top + btm + ar.getSlidingWindowRowCount();
			
			val = CodePadUtils.toScrollBarValue(top + ar.getTopRowCount(), viewRows, totalRows);
			vis = viewRows / totalRows;
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
			c = CodePadUtils.mixColor(c, editor.getSelectionColor(), Defaults.SELECTION_BACKGROUND_OPACITY);
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
	
	
	public Arrangement arrangement()
	{
		if(arrangement == null)
		{
			arrangement = createArrangement();
		}
		return arrangement;
	}
	
	
	private Arrangement createArrangement()
	{
		int modelSize = editor.getParagraphCount();
		int ix = origin.index();
		int cix = origin.cellIndex();
		Arrangement a = new Arrangement(cache, modelSize, viewCols, wrapLimit, ix, cix);
		a.layoutViewPort(viewRows);
		
		// lay out bottom half of the sliding window
		int last = a.getLastViewIndex();
		int ct = a.layoutSlidingWindow(last, Defaults.SLIDING_WINDOW_HALF, true); 
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
			a.layoutSlidingWindow(top, ct, false);
		}
		return a;
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
			int nrows = 0;
			int firstRow = -1;
			WrapInfo wi;
			boolean reachedEnd = false;

			// make an easy check if vsb is needed
			if(size > viewRows)
			{
				vsb = true;
				vsbWidth = snapSizeX(vscroll.prefWidth(-1));
				canvasWidth -= vsbWidth;
				viewCols = (int)((canvasWidth - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
				wrapLimit = viewCols;
				
				// check to see if the origin needs to be moved to fill the view port
				int ix = origin.index();
				int cix = origin.cellIndex();
				
				for(;;)
				{
					if(ix >= size)
					{
						reachedEnd = true;
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
						break;
					}
					
					ix++;						
				}
			}
			else
			{
				// make another check for vsb visibility, this time by actually wrapping the text rows
				// start with assumption that vsb is not needed
				vsb = false;
				boolean run = false;
				
				do
				{
					nrows = 0;
					reachedEnd = false;
					firstRow = -1;

					int ix = origin.index();
					int cix = origin.cellIndex();
					wi = null;
					
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
			}
				
			if(reachedEnd)
			{
				// move the origin to fill in the viewport
				int ct = viewRows - nrows;
				int ix = origin.index();
				int cix = origin.cellIndex();
				
				while((ix > 0) && (ct > 0))
				{
					wi = cache.getWrapInfo(ix, wrapLimit);
					
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
		// can cache because this method will be called on change
		highlightCaretLine = (editor.getCaretColor() != null);
		
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
	private void paintCells(TextCellMetrics tm, WrapInfo wi, int cellIndex0, int cellCount, double x, double y)
	{
		double maxx = canvas.getWidth();
		Color textColor = editor.getTextColor();
		boolean drawCaret = cursorOn && paintCaret.get();
		
		for(int i=0; i<cellCount; i++)
		{
			int cix = cellIndex0 + i;
			int flags = SelectionHelper.getFlags(this, highlightCaretLine, editor.getSelection(), wi, cix);
			boolean caretLine = SelectionHelper.isCaretLine(flags);
			boolean caret = drawCaret && SelectionHelper.isCaret(flags);
			boolean selected = SelectionHelper.isSelected(flags);
			
			// style
			CellStyle style = wi.getCellStyle(cix);
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
			
			// caret
			if(caret)
			{
				// TODO insert mode
				gx.setFill(editor.getCaretColor());
				gx.fillRect(x, y, 2, tm.cellHeight);
			}
			
			if(style.isUnderline())
			{
				gx.setFill(textColor);
				gx.fillRect(x, y + tm.cellHeight - 1, tm.cellWidth, 1);
			}
			
			// text
			String text = wi.getCellText(cix);
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
