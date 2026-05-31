// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.SelectionRange;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeModel;
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
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Duration;


/// Cell Grid.
///
/// Renders text in a rectangular grid.
/// Contains the canvas and the scroll bars.
/// 
public class CellGrid
	extends Pane
{
	private static final Log log = Log.get("CellGrid");
	final CodePad editor;
	private final CellCache cache = new CellCache(64);
	private final ScrollBar vscroll;
	private final ScrollBar hscroll;
	private Origin origin = Origin.ZERO;
	private Canvas canvas;
	private GraphicsContext gx; // create ClippedCanvas?
	private TextCellMetrics metrics;
	private Arrangement arrangement;
	private Font baseFont;
	private Font boldFont;
	private Font boldItalicFont;
	private Font italicFont;
	private boolean wrap;
	private double aspectRatio;
	private double contentPaddingTop;
	private double contentPaddingBottom;
	private double contentPaddingLeft;
	private double contentPaddingRight;
	private boolean handleScrollEvents = true;
	private boolean updateScrollBars = true;
	// number of rows that result in no vsb
	@Deprecated private int viewRows; // TODO move to arrangement
	// number of columns that result in no hsb
	@Deprecated private int viewCols; // TODO move to arrangement
	// negative if no wrap
	private int wrapLimit;
	final SimpleBooleanProperty caretEnabledProperty = new SimpleBooleanProperty(true);
	final SimpleBooleanProperty suppressBlink = new SimpleBooleanProperty(false);
	private final BooleanExpression paintCaret;
	private Timeline cursorAnimation;
	private boolean cursorOn = true;
	private boolean highlightCaretLine;
	private int phantomx = -1;


	public CellGrid(CodePad ed, ScrollBar vscroll, ScrollBar hscroll)
	{
		this.editor = ed;
		this.vscroll = configureScrollBar(vscroll);
		this.hscroll = configureScrollBar(hscroll);
		getChildren().addAll(vscroll, hscroll);
		
		setBorder(Border.stroke(Color.TRANSPARENT));
		
		// TODO possibly move all these listeners to the skin
		
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
		requestLayout();
	}


	private void setOrigin(int index, int cellIndex, double xoffset, double yoffset)
	{
		log.debug("ix={0}, cix={1}, xoff={2}, yoff={3}", index, cellIndex, xoffset, yoffset);
		
		// FIX remove
		if(cellIndex < 0)
		{
			log.warn();
		}
		
		// TODO perhaps adjust origin to avoid
		// - going before the document start
		// - going past the (document end - viewRows) 
		
		Origin or = new Origin(index, cellIndex, xoffset, yoffset);
		if(!origin.equals(or))
		{
			origin = or;
			requestLayout();
		}
	}
	
	
	private void refreshCursor()
	{
		SelectionRange sel = editor.getSelection();
		if(sel != null)
		{
			TextPos caret = sel.getCaret();
			if(isVisible(caret))
			{
				paintCaretLine();
			}
		}
	}
	
	
	private boolean isVisible(TextPos p)
	{
		if(p == null)
		{
			return false;
		}

		RelativePosition rp = arrangement().getRelativePosition(p);
		return (rp == RelativePosition.VISIBLE);
	}
	
	
	public TextPos textPosAtPoint(Point2D local)
	{
		double x = local.getX() - origin.xoffset();
		double y = local.getY() - origin.yoffset();
		TextCellMetrics tm = textCellMetrics();
		int row = (int)(y / (tm.cellHeight + lineSpacing()));
		int col = (int)Math.round(x / tm.cellWidth);
		Arrangement a = arrangement();
		int ix = a.indexAtRow(row);
		if(ix < 0)
		{
			if(row <= 0)
			{
				return TextPos.ZERO;
			}
			// FIX incorrect in case when the partial row below the arrangement is requested
			return editor.getDocumentEnd();
		}

		// clamp to viewCols in wrapped mode
		int cix = a.cellIndexAtRow(row) + (wrap ? Math.min(viewCols, col) : col);
		WrapInfo wi = cache.getWrapInfo(ix);
		return wi.clamp(cix);
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
		
		if(on)
		{
			if(cursorAnimation != null)
			{
				cursorAnimation.stop();
			}
		}
		else
		{
			if(cursorAnimation != null)
			{
				cursorAnimation.stop();
				cursorAnimation.play();
			}
		}
		
		// TODO repaint caret line only
		paintCaretLine();
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
		Timeline t = new Timeline(new KeyFrame(Duration.millis(500), (ev) ->
		{
			blinkCursor();
		}));
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		return t;
	}


	public void handleModelChange()
	{
		setOrigin(0, 0, contentPaddingLeft, contentPaddingTop);
		cache.clear();
		requestLayout();
	}
	
	
	public void handleLineSpacingChange()
	{
		cache.clear();
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
		requestLayout();
	}


	public void setWrapText(boolean on)
	{
		wrap = on;
		int ix = origin.index();
		double yoff = (ix == 0) ? contentPaddingTop : 0.0;
		setOrigin(ix, 0, contentPaddingLeft, yoff);
		cache.clear();
		clearPhantomX();
		requestLayout();
	}
	
	
	public int getViewPortRowCount()
	{
		return viewRows;
	}
	
	
	public int getViewPortColumnCount()
	{
		return viewCols;
	}
	
	
	private static ScrollBar configureScrollBar(ScrollBar b)
	{
		b.setManaged(false);
		b.setMin(0.0);
		b.setMax(1.0);
		b.setUnitIncrement(0.01);
		b.setBlockIncrement(0.05);
		b.setVisible(false);
		b.setViewOrder(-10); // above canvas
		FX.consumeAllEvents(ScrollEvent.ANY, b);
		return b;
	}
	
	
	private void handleWidthChange()
	{
		// TODO scroll horizontally
		requestLayout();
	}
	
	
	private void handleHeightChange()
	{
		requestLayout();
	}
	
	
	private void handleScaleChange()
	{
		requestLayout();
	}
	
	
	public void handleSelectionChange(Object src, SelectionRange old, SelectionRange sel)
	{
		if(sel != null)
		{
			TextPos p = sel.getCaret();
			if(p != null)
			{
				scrollToVisible(p);
			}
		}
		
		// TODO repaint only the damaged area: union of old and new selection ranges
		repaintAll();
	}
	
	
	public void handleScrollBarMousePressed(MouseEvent ev)
	{
		updateScrollBars = false;
	}
	
	
	public void handleScrollBarMouseReleased(MouseEvent ev)
	{
		updateScrollBars = true;
	}


	public void handleHorizontalScroll()
	{
		if(handleScrollEvents)
		{
			double val = hscroll.getValue();
			double max = hscroll.getMax();
			double min = hscroll.getMin();
			double pos = (val - min) / max;
			
			TextCellMetrics tm = textCellMetrics();
			Arrangement ar = arrangement();
			int maxCells = ar.lastColumn() + Defaults.HORIZONTAL_CARET_GUARD;
			double w = contentPaddingLeft + contentPaddingRight + maxCells * tm.cellWidth;
			double cw = canvas.getWidth();
			
			int cix;
			double xoff;
			if(w < cw)
			{
				cix = 0;
				xoff = contentPaddingLeft;
			}
			else
			{
				double x = ((w - cw) * val) - contentPaddingLeft;
				if(x < 0)
				{
					cix = 0;
					xoff = -x;
				}
				else
				{
					cix = (int)(x / tm.cellWidth);
					xoff = 0.0;
				}
			}
			
			int ix = origin.index();
			double yoff = origin.yoffset();
			setOrigin(ix, cix, xoff, yoff);
		}
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
			double max = vscroll.getMax();
			double min = vscroll.getMin();
			double pos = (val - min) / max;
			
			// 1. rough estimate
			int ix = Math.max(0, (int)Math.round(size * pos));
			ix = Math.min(ix, size - 1);
			int cix = 0;
			setOrigin(ix, cix, origin.xoffset(), 0);
			
			// 2. compute arrangement
			if(arrangement != null)
			{
				log.error("arrangement must be null at this point");
				arrangement = null;				
			}
			Arrangement ar = arrangement();
			log.debug(ar);
			
			// 3. adjust
			int space = ar.getSlidingWindowRowCount() - viewRows;
			if(space > 0)
			{
				int d = (int)Math.round(space * pos);
				TextPos p = ar.textPosAtRow(d);
				ix = p.index();
				cix = p.cellIndex();
			}

			double yoff = ix == 0 ? contentPaddingTop : 0.0;
			setOrigin(ix, cix, origin.xoffset(), yoff);
		}
	}


	public void updateVerticalScrollBar()
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
			// unless the arrangement encompasses the whole model, we need to approximate
			// by using the row count within the sliding window and paragraph counts above and below
			// the sliding window. 

			TextCellMetrics tm = textCellMetrics();
			Arrangement ar = arrangement();
			
			double top = ar.getTopIndex();
			double btm = (size - ar.getBottomIndex());
			double totalRows = top + btm + ar.getSlidingWindowRowCount();
			
			double pos = contentPaddingTop + (top + ar.getTopRowCount()) * tm.cellHeight;
			double max = contentPaddingTop + contentPaddingBottom + (totalRows * tm.cellHeight);
			double visible = canvas.getHeight();
			
			val = CodePadUtils.toScrollBarValue(pos, visible, max);
			vis = visible / max;
		}

		handleScrollEvents = false;
		{
			vscroll.setValue(val);
			vscroll.setVisibleAmount(vis);
		}
		handleScrollEvents = true;
	}
	
	
	public void updateHorizontalScrollBar()
	{
		double vis;
		double val;

		Arrangement ar = arrangement();
		int w = ar.lastColumn();
		if(w == 0)
		{
			vis = 1.0;
			val = 0.0;
		}
		else
		{
			TextCellMetrics tm = textCellMetrics();
			double pos = contentPaddingLeft - origin.xoffset() + origin.cellIndex() * tm.cellWidth;
			double max = contentPaddingLeft + contentPaddingRight + (w + Defaults.HORIZONTAL_CARET_GUARD) * tm.cellWidth;
			double visible = canvas.getWidth();
			
			val = CodePadUtils.toScrollBarValue(pos, visible, max);
			vis = visible / max;
		}
		
		handleScrollEvents = false;
		{
			hscroll.setValue(val);
			hscroll.setVisibleAmount(vis);
		}
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
	
	
	private Font getFont(CellStyle st)
	{
		if(st.isBold())
		{
			if(st.isItalic())
			{
				if(boldItalicFont == null)
				{
					boldItalicFont = Font.font(baseFont.getFamily(), FontWeight.BOLD, FontPosture.ITALIC, baseFont.getSize());
				}
				return boldItalicFont;
			}
			else
			{
				if(boldFont == null)
				{
					boldFont = Font.font(baseFont.getFamily(), FontWeight.BOLD, FontPosture.REGULAR, baseFont.getSize());
				}
				return boldFont;
			}
		}
		else
		{
			if(st.isItalic())
			{
				if(italicFont == null)
				{
					italicFont = Font.font(baseFont.getFamily(), FontPosture.ITALIC, baseFont.getSize());
				}
				return italicFont;
			}
			else
			{
				return baseFont;
			}
		}
	}
	

	@Override
	public void requestLayout()
	{
		super.requestLayout();
		arrangement = null;
	}
	
	
	private Arrangement arrangement()
	{
		if(arrangement == null)
		{
			arrangement = computeArrangement(null, null);
		}
		return arrangement;
	}

	
	/// - computes Arrangement
	/// - paints the canvas
	/// - lays out the canvas, and the scrollbars
	@Override
	protected void layoutChildren()
	{
		double width = getWidth();
		log.debug(width);
		if(width == 0.0)
		{
			return;
		}
		
		double x0 = snappedLeftInset();
		double y0 = snappedTopInset();

		CodeModel model = editor.getModel();
		if(model == null)
		{
			// blank screen
			// TODO possibly use the normal path? i.e. create Arrangement.BLANK
			double w = snapSizeX(getWidth()) - snappedLeftInset() - snappedRightInset();
			double h = snapSizeY(getHeight()) - snappedTopInset() - snappedBottomInset();
			ensureCanvas(w, h);
			vscroll.setVisible(false);
			hscroll.setVisible(false);
			clearCanvas();
			layoutInArea(canvas, x0, y0, w, h, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
		}
		else
		{
			// arrange/wrap text
			// TODO extract into arrangement() method,
			Arrangement ar = computeArrangement(null, null);
			double w = ar.canvasWidth();
			double h = ar.canvasHeight();
			ensureCanvas(w, h);
			paintCanvas(ar);

			double vsbWidth = ar.getVSBWidth();
			boolean vsb = (vsbWidth > 0.0);
			vscroll.setVisible(vsb);
			if(vsb)
			{
				layoutInArea(vscroll, x0 + w, y0, vsbWidth, h, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
			}
			
			double hsbHeight = ar.getHSBHeight();
			boolean hsb = (hsbHeight > 0.0);
			hscroll.setVisible(hsb);
			if(hsb)
			{
				layoutInArea(hscroll, x0, y0 + h, w, hsbHeight, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
			}
			
			layoutInArea(canvas, x0, y0, w, h, 0.0, null, true, true, HPos.CENTER, VPos.CENTER);
			
			if(updateScrollBars)
			{
				updateHorizontalScrollBar();
				// TODO
				//updateVerticalScrollBar();
			}
		}
	}
	
	
	// starts with no scrollbars, unless it can be determined that the scrollbars are visible
	// populates the visible area
	// checks the empty space at the bottom, shifts the content down if necessary
	// if vertical scrollbar appears, reflow with the vsb enabled
	// if horizontal scrollbar appears, reflow with hsb enabled
	private Arrangement computeArrangement(Boolean initVsb, Boolean initHsb)
	{
		log.debug("vsb={0} hsb={1}", initVsb, initHsb);
		int size = editor.getParagraphCount();
		int tabSize = tabSize();
		double lineSpacing = lineSpacing();
		TextCellMetrics tm = textCellMetrics();

		double width = snapSizeX(getWidth()) - snappedLeftInset() - snappedRightInset();
		double height = snapSizeY(getHeight()) - snappedTopInset() - snappedBottomInset();
		boolean vsb = (initVsb == null) ? false : initVsb;
		boolean hsb = (initHsb == null) ? false : initHsb;
		double vsbWidth = 0.0;
		double hsbHeight = 0.0;

		viewRows = (int)Math.ceil((height - contentPaddingTop - contentPaddingBottom) / (tm.cellHeight + lineSpacing));
		viewCols = (int)((width - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
		wrapLimit = wrap ? viewCols : -1;
		
		if(size > viewRows)
		{
			vsb = true;
		}
			
		if(vsb)
		{
			vsbWidth = snapSizeX(vscroll.prefWidth(-1));
			width = snapSizeX(width - vsbWidth);
			viewCols = (int)((width - contentPaddingLeft - contentPaddingRight) / tm.cellWidth);
			wrapLimit = wrap ? viewCols : -1;
		}
		
		if(hsb)
		{
			hsbHeight = snapSizeY(hscroll.prefHeight(-1));
			height = snapSizeY(height - hsbHeight);
			viewRows = (int)Math.ceil((height - contentPaddingTop - contentPaddingBottom) / (tm.cellHeight + lineSpacing));
		}
		
		// here we assume the origin cell index is correct for the given width
		int ix = origin.index();
		int cix = origin.cellIndex();
		int rows = 0;
		boolean reachedEnd = false;
		
		for(;;)
		{
			if(ix >= size)
			{
				reachedEnd = true;
				break;
			}
			
			WrapInfo wi = getWrapInfo(ix);
			if(cix == 0)
			{
				rows += wi.getRowCount();
			}
			else
			{
				rows += (wi.getRowCount() - wi.getRowAtCellIndex(cix));
				cix = 0;
			}
			
			ix++;
			if(rows >= viewRows)
			{
				if(ix == size)
				{
					break;
				}

				if(!vsb)
				{
					// vsb appears, recompute
					log.debug("vsb needed, recomputing");
					return computeArrangement(Boolean.TRUE, initHsb);
				}
			}
		}
		
		if(reachedEnd)
		{
			// move origin back to avoid empty space at the end
			origin = recomputeOrigin(rows - viewRows);
		}
		
		// TODO: roll arrangement to get a second copy? if needed
		Arrangement ar = new Arrangement(viewCols, wrapLimit, width, height, hsbHeight, vsbWidth);
		ix = origin.index();
		cix = origin.cellIndex();
		WrapInfo wi = null;
		int lastCol = 0;
		for(int i=0; i<viewRows; i++)
		{
			if(ix >= size)
			{
				break;
			}
			
			if(wi == null)
			{
				wi = getWrapInfo(ix);
				if(!wrap)
				{
					int w = wi.getCellCount();
					if(w > lastCol)
					{
						lastCol = w;
					}
				}
			}
			
			ar.addRow(ix, cix);
			
			if(wrap)
			{
				cix = wi.nextRow(cix);
				if(cix < 0)
				{
					wi = null;
					ix++;
					cix = 0;
				}
			}
			else
			{
				wi = null;
				ix++;
			}
		}
		
		if(!wrap)
		{
			ar.setLastColumn(lastCol);
			if(!hsb)
			{
				// TODO can be done earlier
				if(lastCol > wrapLimit)
				{
					// hsb appears
					log.debug("hsb needed, recomputing");
					return computeArrangement(vsb, Boolean.TRUE);
				}
			}
		}
		
		// compute sliding window
		int topIndex;
		int bottomIndex;
		int slidingWindowRowCount;
		int topRowCount;
		
		// TODO first, going up
		// TODO then, going down
		
		// FIX for now
		topIndex = origin.index();
		bottomIndex = ix;
		slidingWindowRowCount = viewRows;
		topRowCount = 0;
		
		ar.setSlidingWindow(topIndex, bottomIndex, slidingWindowRowCount, topRowCount);
		return ar;
	}
	
	
	private Origin recomputeOrigin(int deltaRows)
	{
		// TODO
		return origin;
	}
	
	
	private void paintCanvas(Arrangement a)
	{
		// TODO check if canvas needs to be (re-)created?
		// TODO
		// can cache because this method will be called on change
		highlightCaretLine = (editor.getCaretColor() != null);
		
		int maxy = a.visibleRowCount();
		int wrapLimit = a.wrapLimit();
		TextCellMetrics tm = textCellMetrics();
		double lineSpacing = lineSpacing();
		
		clearCanvas();
		
		double x = origin.xoffset();
		double y = origin.yoffset();
		
		for(int i=0; i<maxy; i++)
		{
			int ix = a.indexAtRow(i);
			WrapInfo wi = getWrapInfo(ix);
			int cix = a.cellIndexAtRow(i);
			int ct = wrap ? Math.min(wrapLimit, wi.getCellCount() - cix) : wi.getCellCount();
			paintCells(tm, wi, cix, ct, x, y);
			y = snapPositionY(y + tm.cellHeight + lineSpacing);
		}
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
			
			if(canvas != null) // TODO is this needed?
			{
				canvas.setClip(null);
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
		
		// background color
		gx.setFill(editor.getBackgroundColor());
		gx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	
	private Rectangle2D computeSelectionRectangle(SelectionRange sel, int ix, int cellIndex, int count, double x, double y, double cellWidth, double height)
	{
		if(sel == null)
		{
			return null;
		}

		TextPos min = sel.getMin();
		if(min == null)
		{
			return null;
		}
		
		TextPos max = sel.getMax();
		if(max == null)
		{
			return null;
		}
		
		if(min.equals(max))
		{
			return null;
		}
		
		//
		
		TextPos p0 = new TextPos(ix, cellIndex);
		TextPos p1 = new TextPos(ix, cellIndex + viewCols);
		
		if(max.compareTo(p0) <= 0)
		{
			return null;
		}
		else if(min.compareTo(p1) >= 0)
		{
			return null;
		}
		
		boolean leftEdge = (min.compareTo(p0) < 0);
		double x0 = leftEdge ? 0.0 : x + (min.cellIndex() - cellIndex) * cellWidth;
		
		boolean rightEdge = (max.compareTo(p1) >= 0) && (!(min.cellIndex() == max.cellIndex()));
		double x1 = rightEdge ? canvas.getWidth() : x + (max.cellIndex() - cellIndex) * cellWidth;
		
		double w = x1 - x0;
		if(w < 0.0)
		{
			// TODO find out why
			return null;
		}
		return new Rectangle2D(x0, y, w, height);
	}
	
	
	public void paintCaretLine()
	{
		// TODO optimize
		repaintAll();
	}
	
	
	/// repaints canvas assuming the size hasn't changed
	public void repaintAll()
	{
		Arrangement ar = arrangement();
		paintCanvas(ar);
	}


	// paints a single row horizontally
	private void paintCells(TextCellMetrics tm, WrapInfo wi, int cellIndex0, int count, double x, double y)
	{
		double maxx = canvas.getWidth();
		Color textColor = editor.getTextColor();
		int ix = wi.getIndex();
		int len = wi.getCellCount();
		SelectionRange sel = editor.getSelection();
		boolean drawCaret = cursorOn && paintCaret.get() && (sel != null);
		boolean caretLine = highlightCaretLine && (sel != null) && sel.isCaretLine(ix);
		Color parBG = wi.getBackgroundColor();
		double lineH = tm.cellHeight + lineSpacing();
		
		if(caretLine)
		{
			if((wrap && (count < viewCols)) || !wrap)
			{
				count += Defaults.HORIZONTAL_CARET_GUARD;
			}
		}
		
		// current paragraph highlight extends to the edge of canvas
		if(caretLine)
		{
			Color bg = editor.getCaretLineColor();
			gx.setFill(bg);
			gx.fillRect(0, y, canvas.getWidth(), tm.cellHeight);
		}
		
		// selection highlight extends to the edge of canvas
		Rectangle2D selR = computeSelectionRectangle(sel, ix, cellIndex0, count, x, y, tm.cellWidth, lineH);
		if(selR != null)
		{
			Color bg = editor.getSelectionColor();
			gx.setFill(bg);
			gx.fillRect(selR.getMinX(), selR.getMinY(), selR.getWidth(), selR.getHeight());
		}
		
		// paragraph background extends to the edge of canvas
		if(parBG != null)
		{
			gx.setFill(parBG);
			gx.fillRect(0, y, canvas.getWidth(), tm.cellHeight);
		}
		
		for(int i=0; i<count; i++)
		{
			int cix = cellIndex0 + i;

			// style
			CellStyle style = wi.getCellStyle(cix);
			if(style == null)
			{
				style = CellStyle.EMPTY;
			}
			
			// cell background
			Color cellBG = style.getBackgroundColor();
			if(cellBG != null)
			{
				gx.setFill(cellBG);
				gx.fillRect(x, y, tm.cellWidth, tm.cellHeight);
			}
			
			// caret
			if(caretLine && drawCaret)
			{
				@SuppressWarnings("null")
				TextPos ca = sel.getCaret();
				if(ca.cellIndex() == cix)
				{
					gx.setFill(editor.getCaretColor());
					// TODO insert mode
					double caretWidth = snapSizeX(Defaults.CARET_WIDTH);
					gx.fillRect(x, y, caretWidth, tm.cellHeight);
				}
			}
			
			if(style.isUnderline())
			{
				gx.setFill(textColor);
				gx.fillRect(x, y + tm.cellHeight - 1, tm.cellWidth, 1);
			}
			
			// text
			if(cix < len)
			{
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
			}
			
			x = snapPositionX(x + tm.cellWidth);
			if(x > maxx)
			{
				break;
			}
		}
	}
	
	
	private WrapInfo getWrapInfo(int modelIndex)
	{
		cache.check(editor.getModel(), tabSize(), wrapLimit);
		return cache.getWrapInfo(modelIndex);
	}
	
	
	public void clearPhantomX()
	{
		phantomx = -1;
	}
	
	
	public TextPos goVertically(TextPos from, int delta, boolean usePhantomX)
	{
		if(wrap)
		{
			return goVerticallyWrapped(from, delta, usePhantomX);
		}
		else
		{
			return goVerticallyNonWrapped(from, delta, usePhantomX);
		}
	}
		
	
	private TextPos goVerticallyNonWrapped(TextPos from, int delta, boolean usePhantomX)
	{
		int ix = from.index();
		int cix = from.cellIndex();
		WrapInfo wi = getWrapInfo(ix);
		
		if(usePhantomX)
		{
			if(phantomx < 0)
			{
				phantomx = cix;
			}
			else
			{
				cix = phantomx;
			}
		}

		ix += delta;
		if(ix < 0)
		{
			return TextPos.ZERO;
		}
		else
		{
			int sz = editor.getParagraphCount();
			if(ix < sz)
			{
				wi = getWrapInfo(ix);
				return wi.clamp(cix);
			}
			else
			{
				return editor.getDocumentEnd();
			}
		}
	}
	
	
	private TextPos goVerticallyWrapped(TextPos from, int delta, boolean usePhantomX)
	{
		int ix = from.index();
		int cix = from.cellIndex();
		WrapInfo wi = getWrapInfo(ix);

		int col;
		if(usePhantomX)
		{
			if(phantomx < 0)
			{
				col = cix - lineStartCellIndex(from);
				phantomx = col;
			}
			else
			{
				col = phantomx;
			}
		}
		else
		{
			col = cix - lineStartCellIndex(from);
		}
		
		int lineStart = lineStartCellIndex(from);
		
		if(delta < 0)
		{
			// going up
			int ct = -delta;
			for(;;)
			{
				if(ix < 0)
				{
					return TextPos.ZERO;
				}
				
				if(wi == null)
				{
					wi = getWrapInfo(ix);
				}
				
				if(lineStart < 0)
				{
					lineStart = lineStartCellIndex(wi.clamp(wi.getCellCount()));
				}
				
				int n = lineStart / wrapLimit;
				if(ct <= n)
				{
					cix = lineStart - (ct * wrapLimit);
					if(cix < 0)
					{
						throw new Error("cix=" + cix); // safeguard, should not happen
					}

					cix += col;
					return new TextPos(ix, cix);
				}
				else
				{
					ct -= (n + 1);
					ix--;
					wi = null;
					lineStart = -1;
				}				
			}
		}
		else
		{
			// going down
			int max = editor.getParagraphCount();
			int ct = delta;
			for(;;)
			{
				if(ix >= max)
				{
					return editor.getDocumentEnd();
				}
				
				if(wi == null)
				{
					wi = getWrapInfo(ix);
				}

				int h = wi.getRowCount();
				
				int n = lineStart / wrapLimit;
				if(ct + n < h)
				{
					cix = lineStart + (ct * wrapLimit) + col;
					return wi.clamp(cix);
				}
				else
				{
					ct -= (h - n);
					ix++;
					wi = null;
					lineStart = 0;
				}
			}
		}
	}
	
	
	public TextPos goHorizontally(TextPos from, int delta)
	{
		int ix = from.index();
		int cix = from.cellIndex() + delta;
		
		if(delta < 0)
		{
			// move left
			if(cix >= 0)
			{
				return new TextPos(ix, cix);
			}
			else
			{
				if(ix == 0)
				{
					return null;
				}
				else
				{
					--ix;
					WrapInfo wi = getWrapInfo(ix);
					return wi.clamp(wi.getCellCount());
				}
			}
		}
		else
		{
			// move right
			WrapInfo wi = getWrapInfo(ix);
			int len = wi.getCellCount();
			if(cix <= len)
			{
				return wi.clamp(cix);
			}
			else
			{
				if(ix == editor.getParagraphCount())
				{
					return null;
				}
				else
				{
					ix++;
					return new TextPos(ix, 0);
				}
			}
		}
	}
	
	
	// try showing more of a short line
	private int adjustToMaximizeViewableText(int ix, int cix)
	{
		WrapInfo wi = getWrapInfo(ix);
		int mx = Math.min(viewCols / 2, wi.getCellCount());
		if(cix >= mx)
		{
			return Math.max(0, mx - viewCols);
		}
		return cix;
	}
	
	
	public void scrollToVisible(TextPos pos)
	{
		RelativePosition rel = arrangement().getRelativePosition(pos);
		log.debug(rel);

		int ix;
		int cix;
		double xoff;
		double yoff;
		switch(rel)
		{
		case ABOVE:
			if(wrap)
			{
				ix = pos.index();
				cix = (pos.cellIndex() / viewCols) * viewCols;
				yoff = (ix == 0) ? contentPaddingTop : 0.0;
				setOrigin(ix, cix, contentPaddingLeft, yoff);
			}
			else
			{
				ix = pos.index();
				yoff = (ix == 0) ? contentPaddingTop : 0.0;
				setOrigin(ix, origin.cellIndex(), origin.xoffset(), yoff);
			}
			break;
		case ABOVE_LEFT:
			ix = pos.index();
			cix = pos.cellIndex();
			cix = adjustToMaximizeViewableText(ix, cix);
			xoff = (cix == 0) ? contentPaddingLeft : 0.0;
			yoff = (ix == 0) ? contentPaddingTop : 0.0;
			setOrigin(ix, cix, xoff, yoff);
			break;
		case ABOVE_RIGHT:
			ix = pos.index();
			cix = Math.max(0, pos.cellIndex() - viewCols);
			yoff = (ix == 0) ? contentPaddingTop : 0.0;
			setOrigin(ix, cix, 0.0, yoff);
			break;
		case BELOW:
			if(wrap)
			{
				TextPos p = goVertically(pos, 1 - viewRows, false);
				cix = p.cellIndex();
				cix = (cix / wrapLimit) * wrapLimit;
				ix = p.index();
				setOrigin(ix, cix, contentPaddingLeft, 0.0); 
			}
			else
			{
				ix = pos.index();
				ix = Math.max(0, ix - viewRows + 1);
				setOrigin(ix, origin.cellIndex(), origin.xoffset(), 0.0);
			}
			break;
		case BELOW_LEFT:
			ix = pos.index();
			ix = Math.max(0, ix - viewRows);
			cix = pos.cellIndex();
			cix = adjustToMaximizeViewableText(ix, cix);
			xoff = (cix == 0) ? contentPaddingLeft : 0.0;
			setOrigin(ix, cix, xoff, 0.0);
			break;			
		case BELOW_RIGHT:
			ix = pos.index();
			ix = Math.max(0, ix - viewRows + 1);
			cix = Math.max(0, pos.cellIndex() - viewCols);
			setOrigin(ix, cix, 0.0, 0.0);
			break;
		case LEFT:
			ix = pos.index();
			cix = pos.cellIndex();
			cix = adjustToMaximizeViewableText(ix, cix);
			xoff = (cix == 0) ? contentPaddingLeft : 0.0;
			setOrigin(origin.index(), cix, xoff, origin.yoffset());
			break;
		case RIGHT:
			cix = Math.max(0, pos.cellIndex() - viewCols);
			setOrigin(origin.index(), cix, 0.0, origin.yoffset());
			break;
		}
	}
	
	
	public void blockScroll(double deltaPixels)
	{
		log.debug(deltaPixels);
		TextCellMetrics tm = textCellMetrics();
		int delta;
		if(deltaPixels < 0.0)
		{
			delta = (int)Math.floor(deltaPixels / (tm.cellHeight + lineSpacing())) - 1;
		}
		else
		{
			delta = (int)Math.ceil(deltaPixels / (tm.cellHeight + lineSpacing())) + 1;
		}
		
		shiftOrigin(delta);
	}
	
	
	public void verticalScroll(int deltaLines, boolean up)
	{
		log.trace("deltaLines={0} {1}", deltaLines, up);

		if(deltaLines < 1)
		{
			deltaLines = 1;
		}
		else if(deltaLines > viewRows)
		{
			deltaLines = viewCols;
		}
		
		shiftOrigin(up ? -deltaLines : deltaLines);
	}
	
	
	public void shiftOrigin(int deltaLines)
	{
		log.debug("deltaLines={0}", deltaLines);
		
		int cix = origin.cellIndex();
		TextPos from = new TextPos(origin.index(), cix);
		TextPos p = goVertically(from, deltaLines, false);
		int ix = p.index();
		cix = p.cellIndex();
		double yoff = (ix == 0) ? contentPaddingTop : 0.0;
		setOrigin(ix, cix, origin.xoffset(), yoff);
		requestLayout();
	}
	
	
	public TextPos goLineStart(TextPos from)
	{
		int ix = from.index();
		if(wrap)
		{
			int cix = from.cellIndex();
			WrapInfo wi = getWrapInfo(ix);
			int row = wi.getRowAtCellIndex(cix);
			cix = wi.getCellIndexAtRow(row);
			return new TextPos(ix, cix);
		}
		else
		{
			return new TextPos(ix, 0);
		}
	}


	public TextPos goLineEnd(TextPos from)
	{
		int ix = from.index();
		WrapInfo wi = getWrapInfo(ix);
		if(wrap)
		{
			int cix = from.cellIndex();
			int row = wi.getRowAtCellIndex(cix);
			cix = wi.getCellIndexAtRow(row + 1) - 1;
			return wi.clamp(cix);
		}
		else
		{
			return new TextPos(ix, wi.getCellCount());
		}
	}
	
	
	private int lineStartCellIndex(TextPos p)
	{
		int cix = p.cellIndex();
		int row = cix / wrapLimit;
		return row * wrapLimit;
	}
}
