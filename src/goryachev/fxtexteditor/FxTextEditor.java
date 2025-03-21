// Copyright © 2019-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor;
import goryachev.common.log.Log;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.fx.FX;
import goryachev.fx.FxBoolean;
import goryachev.fx.FxDouble;
import goryachev.fx.FxObject;
import goryachev.fx.TextCellMetrics;
import goryachev.fx.XScrollBar;
import goryachev.fxtexteditor.internal.InputHandler;
import goryachev.fxtexteditor.internal.Markers;
import goryachev.fxtexteditor.internal.TabPolicy;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.DataFormat;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;


/**
 * Monospaced Text Editor.
 */
public class FxTextEditor
	extends BorderPane
{
	protected static final Log log = Log.get("FxTextEditor");
	public final Actions actions;
	protected final FxObject<Color> backgroundColor = new FxObject(Color.WHITE);
	protected final FxObject<Color> loadingIndicatorColor = new FxObject(FX.gray(32));
	protected final FxObject<Color> caretLineColor = new FxObject(FX.rgb(255, 200, 255));
	protected final FxObject<Color> selectionBackgroundColor = new FxObject(FX.rgb(255, 255, 128));
	protected final FxObject<Color> lineNumberColor = new FxObject(Color.GRAY);
	protected final FxObject<Font> fontProperty = new FxObject(Font.font("Monospace", -1));
	protected final FxBoolean editableProperty = new FxBoolean(false);
	protected final FxObject<FxTextEditorModel> modelProperty = new FxObject<>();
	protected final FxBoolean wrapLinesProperty = new FxBoolean(true);
	protected final FxBoolean displayCaretProperty = new FxBoolean(true);
	protected final FxBoolean showLineNumbersProperty = new FxBoolean(false);
	protected final FxBoolean highlightCaretLineProperty = new FxBoolean(true);
	protected final FxDouble scrollWheelStepSize = new FxDouble(-0.25);
	protected final FxObject<Duration> caretBlinkRateProperty = new FxObject(Duration.millis(500));
	protected final FxObject<ALineNumberFormatter> lineNumberFormatterProperty = new FxObject<>(ALineNumberFormatter.getDefault());
	protected final FxObject<ITabPolicy> tabPolicy = new FxObject();
	// TODO lineCount r/o property
	protected final FxTextEditorModelListener modelListener;
	protected final SelectionController selector;
	protected final Markers markers = new Markers(32);
	protected final VFlow vflow;
	protected final ScrollBar vscroll;
	protected final ScrollBar hscroll;
	protected final ChangeListener<LoadStatus> loadStatusListener;
	private InputHandler inputHandler;
	private boolean caretAtEofPriorToLastUpdate;

	
	// TODO perhaps pass final Capabilities object that defines basic parameters
	// such as cache size, various limits, behaviors, etc.
	public FxTextEditor()
	{
		modelListener = new FxTextEditorModelListener()
		{
			@Override
			public void eventAllLinesChanged()
			{
				handleAllLinesChanged();
			}

			@Override
			public void eventTextAltered(int line1, int charIndex1, int endLine, int endPos, int charsAdded1, int linesAdded, int charsAdded2)
			{
				handleTextAltered(line1, charIndex1, endLine, endPos, charsAdded1, linesAdded, charsAdded2);
			}
		};
		
		loadStatusListener = new ChangeListener<LoadStatus>()
		{
			@Override
			public void changed(ObservableValue<? extends LoadStatus> observable, LoadStatus prev, LoadStatus cur)
			{
				updateLoadStatus(cur);
			}
		};
		
		selector = createSelectionController();
		
		vscroll = createVScrollBar();
		vscroll.setOrientation(Orientation.VERTICAL);
		vscroll.setManaged(true);
		vscroll.setMin(0.0);
		vscroll.setMax(1.0);
		vscroll.addEventFilter(ScrollEvent.ANY, (ev) -> ev.consume());
		
		hscroll = createHScrollBar();
		hscroll.setOrientation(Orientation.HORIZONTAL);
		hscroll.setManaged(true);
		hscroll.setMin(0.0);
		hscroll.setMax(1.0);
		hscroll.addEventFilter(ScrollEvent.ANY, (ev) -> ev.consume());
		hscroll.visibleProperty().bind(wrapLinesProperty.not());
		
		vflow = new VFlow(this);
		
		getChildren().addAll(vflow, vscroll, hscroll);
				
		// TODO
//		FX.onChange(vflow::updateBlinkRate, true, blinkRateProperty());
		
		actions = new Actions(this);
		
		inputHandler = createInputHandler();
		setFocusTraversable(true);
		
		setTabPolicy(TabPolicy.create(4));
		
		vflow.layoutXProperty().addListener((s,p,c) -> D.print("vflow", c));
		layoutXProperty().addListener((s,p,c) -> D.print("editor", c));
	}
	
	
	/** override to provide your own implementation.  warning: this method is called from the constructor */
	protected ScrollBar createVScrollBar()
	{
		return new XScrollBar();
	}
	
	
	/** override to provide your own implementation.  warning: this method is called from the constructor */
	protected ScrollBar createHScrollBar()
	{
		return new XScrollBar();
	}
	
	
	/** override to provide your own implementation.  warning: this method is called from the constructor */
	protected InputHandler createInputHandler()
	{
		return new InputHandler(this, vflow, selector);
	}
	
	
	public FxObject<Font> fontProperty()
	{
		return fontProperty;
	}
	
	
	public Font getFont()
	{
		return fontProperty.get();
	}
	

	public void setFont(Font f)
	{
		fontProperty.set(f);
	}
	
	
	public void setFontSize(double size)
	{
		Font f = getFont();
		f = Font.font(f.getFamily(), size);
		setFont(f);
	}
	
	
	public ScrollBar getVerticalScrollBar()
	{
		return vscroll;
	}
	
	
	public ScrollBar getHorizontalScrollBar()
	{
		return hscroll;
	}
	
	
	public void setContentPadding(Insets m)
	{
		vflow.setPadding(m);
	}
	
	
	public Insets getContentPadding()
	{
		return vflow.getPadding();
	}
	
	
	public FxObject<ALineNumberFormatter> lineNumberFormatterProperty()
	{
		return lineNumberFormatterProperty;
	}
	
	
	public ALineNumberFormatter getLineNumberFormatter()
	{
		return lineNumberFormatterProperty.get();
	}
	
	
	public void setLineNumberFormatter(ALineNumberFormatter f)
	{
		if(f == null)
		{
			f = ALineNumberFormatter.getDefault();
		}
		lineNumberFormatterProperty.set(f);
	}
	
	
	/** override to provide your own selection model */
	protected SelectionController createSelectionController()
	{
		return new SelectionController();
	}
	
	
	public ReadOnlyProperty<SelectionSegment> selectionSegmentProperty()
	{
		return selector.selectionSegmentProperty();
	}
	
	
	public SelectionSegment getSelectedSegment()
	{
		return selector.getSelectedSegment();
	}
	
	
	public ReadOnlyObjectProperty<EditorSelection> selectionProperty()
	{
		return selector.selectionProperty();
	}
	
	
	public EditorSelection getSelection()
	{
		return selector.getSelection();
	}
	
	
	public void clearSelection()
	{
		selector.clear();
	}

	
	public void setModel(FxTextEditorModel m)
	{
		markers.clear();
		clearSelection();
		
		FxTextEditorModel old = getModel();
		if(old != null)
		{
			old.removeListener(modelListener);
			old.loadStatus().removeListener(loadStatusListener);
		}
		
		modelProperty.set(m);
		caretAtEofPriorToLastUpdate = false;
		
		if(m != null)
		{
			vflow.setBreakIterator(m.getBreakIterator());
			
			m.addListener(modelListener);
			m.loadStatus().addListener(loadStatusListener);
			updateLoadStatus(m.getLoadStatus());
		}
		
		handleAllLinesChanged();		
	}
	
	
	public FxTextEditorModel getModel()
	{
		return modelProperty.get();
	}
	
	
	public int getLineCount()
	{
		FxTextEditorModel m = getModel();
		return m == null ? 0 : m.getLineCount();
	}
	
	
	protected void updateLoadStatus(LoadStatus s)
	{
		if(vscroll instanceof XScrollBar)
		{
			Color c = getLoadingIndicatorColor();
			XScrollBar vs = (XScrollBar)vscroll;
			if(s.isValid() && (c != null))
			{
				vs.setPainter((canvas) ->
				{
					double w = canvas.getWidth();
					double h = canvas.getHeight();
					double y = s.getProgress() * h;
					GraphicsContext g = canvas.getGraphicsContext2D();
					g.setFill(c);
					g.fillRect(0, y, w, h - y);
				});
			}
			else
			{
				vs.setPainter(null);
			}
		}
	}
	
	
	public void setLoadingIndicatorColor(Color c)
	{
		loadingIndicatorColor.set(c);
	}
	
	
	public Color getLoadingIndicatorColor()
	{
		return loadingIndicatorColor.get();
	}
	
	
	public boolean isWrapLines()
	{
		return wrapLinesProperty.get();
	}
	
	
	public void setWrapLines(boolean on)
	{
		wrapLinesProperty.set(on);
	}
	
	
	public BooleanProperty wrapLinesProperty()
	{
		return wrapLinesProperty;
	}
	
	
	public ReadOnlyObjectProperty<FxTextEditorModel> modelProperty()
	{
		return modelProperty.getReadOnlyProperty();
	}
	
	
	@Override
	protected void layoutChildren()
	{
		double x0 = snappedLeftInset();
		double y0 = snappedTopInset();
		
		double vscrollWidth = 0.0;
		double hscrollHeight = 0.0;
		
		// position the scrollbar(s)
		if(vscroll.isVisible())
		{
			vscrollWidth = vscroll.prefWidth(-1);
		}
		
		if(hscroll.isVisible())
		{
			hscrollHeight = hscroll.prefHeight(-1);
		}
		
		// TODO line numbers column
		
		double w = snapSizeX(getWidth() - vscrollWidth - 1) - snappedLeftInset() - snappedRightInset();
		double h = getHeight() - snappedTopInset() - snappedBottomInset() - hscrollHeight - 1;

		// layout children
		layoutInArea(vscroll, w, y0 + 1, vscrollWidth, h, 0, null, true, true, HPos.RIGHT, VPos.TOP);
		layoutInArea(hscroll, x0 + 1, h, w, hscrollHeight, 0, null, true, true, HPos.LEFT, VPos.BOTTOM);
		layoutInArea(vflow, x0, y0, w, h, 0, null, true, true, HPos.LEFT, VPos.TOP);
	}
	
	
	/** returns a new Marker at the specified screen coordinates */
	public Marker getInsertPosition(double screenx, double screeny)
	{
		TextPos p = vflow.getInsertPosition(screenx, screeny);
		int line = p.getLine();
		int off = p.getCharIndex();
		
		if(line < 0)
		{
			line = getLineCount();
			off = 0;
		}
		else if(off < 0)
		{
			String s = getPlainText(line);
			if(s == null)
			{
				off = 0;
			}
			else
			{
				off = s.length();
			}
		}
		
		return markers.newMarker(line, off);
	}
	
	
	public Marker newMarker(int lineNumber, int position)
	{
		return markers.newMarker(lineNumber, position);
	}
	
	
	public ReadOnlyObjectProperty<Duration> blinkRateProperty()
	{
		return caretBlinkRateProperty.getReadOnlyProperty();
	}
	
	
	public Duration getBlinkRate()
	{
		return caretBlinkRateProperty.get();
	}
	
	
	public void setBlinkRate(Duration d)
	{
		caretBlinkRateProperty.set(d);
	}
	
	
	public boolean isEditable()
	{
		return editableProperty.get();
	}
	
	
	/** enables editing in the component.  this setting will be ignored if a a model is read only */
	public void setEditable(boolean on)
	{
		editableProperty.set(on);
	}
	
	
	/** 
	 * sets the scroll wheel step size: in lines (val >= 1.0),
	 * or a fraction of screen height (val in the range [-1.0 ... 0[.
	 * the actual step size will be limited to [1..screenRowCount] 
	 */
	public void setScrollWheelStepSize(double val)
	{
		scrollWheelStepSize.set(val);
	}
	
	
	public double getScrollWheelStepSize()
	{
		return scrollWheelStepSize.get();
	}

	
	protected void handleAllLinesChanged()
	{
//		clearSelection();
		vflow.reset();
	}

	
	public void setDisplayCaret(boolean on)
	{
		displayCaretProperty.set(on);
	}
	
	
	public boolean isDisplayCaret()
	{
		return displayCaretProperty.get();
	}
	
	
	public void setShowLineNumbers(boolean on)
	{
		showLineNumbersProperty.set(on);
	}
	
	
	public boolean isShowLineNumbers()
	{
		return showLineNumbersProperty.get();
	}
	
	
	public BooleanProperty showLineNumbersProperty()
	{
		return showLineNumbersProperty;
	}
	
	
	public void setHighlightCaretLine(boolean on)
	{
		highlightCaretLineProperty.set(on);
	}
	
	
	public boolean isHighlightCaretLine()
	{
		return highlightCaretLineProperty.get();
	}
	
	
	public void setOrigin(int row)
	{
		if(row >= getLineCount())
		{
			row = getLineCount() - 1;
		}
		
		if(row < 0)
		{
			row = 0;
		}
		
		vflow.setOrigin(row, 0);
	}
	
	
	public void setCaret(int row, int charIndex)
	{
		Marker m = newMarker(row, charIndex);
		select(m, m);
	}

	
	public void setDoubleClickHandler(BiConsumer<FxTextEditor,Marker> h)
	{
		inputHandler.setDoubleClickHandler(h);
	}
	
	
	public BiConsumer<FxTextEditor,Marker> getDoubleClickHandler()
	{
		return inputHandler.getDoubleClickHandler();
	}
	
	
	public void setTripleClickHandler(BiConsumer<FxTextEditor,Marker> h)
	{
		inputHandler.setTripleClickHandler(h);
	}
	
	
	public BiConsumer<FxTextEditor,Marker> getTripleClickHandler()
	{
		return inputHandler.getTripleClickHandler();
	}
	
	
	public int getTextLength(int line)
	{
		FxTextEditorModel m = getModel();
		if(m == null)
		{
			return 0;
		}
		String s = m.getPlainText(line);
		if(s == null)
		{
			return 0;
		}
		return s.length();
	}

	
	public Color getCaretLineColor()
	{
		return caretLineColor.get();
	}
	
	
	public void setCaretLineColor(Color c)
	{
		caretLineColor.set(c);
	}
	
	
	public Color getSelectionBackgroundColor()
	{
		return selectionBackgroundColor.get();
	}
	
	
	public void setSelectionBackgroundColor(Color c)
	{
		selectionBackgroundColor.set(c);
	}
	
	
	public Color getLineNumberColor()
	{
		return lineNumberColor.get();
	}
	
	
	public void setLineNumberColor(Color c)
	{
		lineNumberColor.set(c);
	}
	
	
	public void reloadVisibleArea()
	{
		vflow.repaint();
	}


	public ITabPolicy getTabPolicy()
	{
		return tabPolicy.get();
	}
	

	public void setTabPolicy(ITabPolicy p)
	{
		if(p == null)
		{
			p = TabPolicy.create(1);
		}
		tabPolicy.set(p);
	}
	
	
	public void setTabSize(int size)
	{
		setTabPolicy(TabPolicy.create(size));
	}

	
	public boolean isCaretLine(int line)
	{
		return selector.isCaretLine(line);
	}
	
	
	public boolean isSelected(int line, int pos)
	{
		return selector.isSelected(line, pos);
	}
	
	
	public void setBackgroundColor(Color c)
	{
		backgroundColor.set(c);
	}
	
	
	public Color getBackgroundColor()
	{
		return backgroundColor.get();
	}
	
	
	public FxObject<Color> backgroundColorProperty()
	{
		return backgroundColor;
	}
	
	
	public int getColumnAt(Marker m)
	{
		int line = m.getLine();
		int pos = m.getCharIndex();
		return vflow.getColumnAt(line, pos);
	}
	

	/** returns plain text on the specified line */
	public String getPlainText(int line)
	{
		FxTextEditorModel m = getModel();
		if(m == null)
		{
			return null;
		}
		return m.getPlainText(line);
	}


	/** returns selected plain text */
	public String getSelectedPlainText() throws Exception
	{
		StringWriter wr = new StringWriter();
		writeSelectedText(wr);
		return wr.toString();
	}
	
	
	/** 
	 * outputs selected plain texty.
	 * this method should be used where allocating a single (potentially large) string is undesirable,
	 * for example when saving to a file.
	 */
	public void writeSelectedText(Writer wr) throws Exception
	{
		EditorSelection sel = getSelection();
		if(sel == null)
		{
			return;
		}
		
		SelectionSegment seg = sel.getSegment();
		if(seg == null)
		{
			return;
		}
		
		int startLine = seg.getMin().getLine();
		int startPos = seg.getMin().getCharIndex();
		
		int endLine = seg.getMax().getLine();
		int endPos = seg.getMax().getCharIndex();
		
		getModel().writePlainText(startLine, startPos, endLine, endPos, wr);
	}
	
	
	/** copies all supported formats */
	public void copy()
	{
		copy(null, false, getModel().getSupportedFormats(true));
	}
	
	
	/** copies all supported formats */
	public void smartCopy()
	{
		copy(null, true, getModel().getSupportedFormats(true));
	}
	
	
	/** copies plain text selection to clipboard */
	public void copyPlainText()
	{
		copy(null, false, DataFormat.PLAIN_TEXT);
	}
	
	
	/** copies plain text selection to clipboard, or all if selection is empty */
	public void smartCopyPlainText()
	{
		copy(null, true, DataFormat.PLAIN_TEXT);
	}
	
	
	/** copies selection to clipboard in RTF format.  does nothing if RTF is not supported */
	public void copyRTF()
	{
		DataFormat[] formats = getModel().getSupportedFormats(true);
		if(CKit.contains(formats, DataFormat.RTF))
		{
			copy(null, false, DataFormat.RTF);
		}
	}
	
	
	/** copies selection to clipboard in RTF format, or all if no selection.  does nothing if RTF is not supported. */
	public void smartCopyRTF()
	{
		DataFormat[] formats = getModel().getSupportedFormats(true);
		if(CKit.contains(formats, DataFormat.RTF))
		{
			copy(null, true, DataFormat.RTF);
		}
	}
	
	
	/** copies selection to clipboard in HTML format.  does nothing if HTML is not supported. */
	public void copyHTML()
	{
		DataFormat[] formats = getModel().getSupportedFormats(true);
		if(CKit.contains(formats, DataFormat.HTML))
		{
			copy(null, false, DataFormat.HTML);
		}
	}
	
	
	/** copies selection to clipboard in HTML format, or all if no selection.  does nothing if HTML is not supported. */
	public void smartCopyHTML()
	{
		DataFormat[] formats = getModel().getSupportedFormats(true);
		if(CKit.contains(formats, DataFormat.HTML))
		{
			copy(null, true, DataFormat.HTML);
		}
	}
	
	
	/** copies specified formats to clipboard, using an error handler.  when smart=true, empty selection means all */
	public void copy(Consumer<Throwable> errorHandler, boolean smart, DataFormat ... formats)
	{
		int startLine;
		int startPos;
		int endLine;
		int endPos;
		
		SelectionSegment seg = getNonEmptySelection();
		if(seg == null)
		{
			if(smart)
			{
				startLine = 0;
				startPos = 0;
				
				endLine = getLineCount() - 1;
				endPos = getTextLength(endLine);
			}
			else
			{
				return;
			}
		}
		else
		{
			startLine = seg.getMin().getLine();
			startPos = seg.getMin().getCharIndex();
			
			endLine = seg.getMax().getLine();
			endPos = seg.getMax().getCharIndex();
		}
		
		if((startLine == endLine) && (startPos == endPos))
		{
			return;
		}
		
		copy(startLine, startPos, endLine, endPos, errorHandler, formats);
	}
	
	
	protected SelectionSegment getNonEmptySelection()
	{
		EditorSelection sel = getSelection();
		if(sel != null)
		{
			SelectionSegment seg = sel.getSegment();
			if(seg != null)
			{
				if(!seg.isEmpty())
				{
					return seg;
				}
			}
		}	
		return null;
	}


	public void copy(int startLine, int startPos, int endLine, int endPos, Consumer<Throwable> errorHandler, DataFormat ... formats)
	{
		getModel().copyToClipboard(startLine, startPos, endLine, endPos, errorHandler, formats);
	}
	
	
	/** copies complete text in all supported formats */
	public void copyAll()
	{
		FxTextEditorModel m = getModel();
		int endLine = m.getLineCount() - 1;
		int endPos = m.getTextLine(endLine).getTextLength();
		copy(0, 0, endLine, endPos, null, m.getSupportedFormats(true));
	}
	

	public void select(Marker start, Marker end)
	{
		selector.setSelection(start, end);
		selector.commitSelection();
		vflow.scrollCaretToView();
	}
	
	
	public void scrollCaretToView()
	{
		vflow.scrollCaretToView();
	}
	
	
	public void select(int startLine, int startPos, int endLine, int endPos)
	{
		Marker start = markers.newMarker(startLine, startPos);
		Marker end = markers.newMarker(endLine, endPos);
		select(start, end);
	}
	

	protected void handleTextAltered(int line1, int charIndex1, int line2, int charIndex2, int charsAdded1, int linesAdded, int charsAdded2)
	{
		log.debug("line1=%d charIndex1=%d line2=%d charIndex2=%d | charsAdded1=%d linesAdded=%d charsAdded2=%d", line1, charIndex1, line2, charIndex2, charsAdded1, linesAdded, charsAdded2);
		
		caretAtEofPriorToLastUpdate = isCaretAtEOF();
		
		markers.update(line1, charIndex1, charsAdded1, linesAdded, line2, charIndex2, charsAdded2);
		selector.refresh();

		vflow.update(line1, linesAdded, line2);
	}


	/** navigates to the line if row is between [0...lineCount-1], otherwise does nothing */
	public void goToLine(int row)
	{
		// TODO smarter algorithm near the end of file
		if((row >= 0) && (row < getLineCount()))
		{
			setOrigin(Math.max(0, row - 3));
			setCaret(row, 0);
		}
	}


	public int getCaretLine()
	{
		SelectionSegment seg = selector.getSelectedSegment();
		return seg == null ? 0 : seg.getCaretLine();
	}
	

	public boolean isCaretAtEOF()
	{
		SelectionSegment seg = getSelectedSegment();
		if(seg == null)
		{
			return true;
		}
		
		if(seg.isEmpty())
		{
			if(seg.getCaretLine() == (getLineCount() - 1))
			{
				String txt = getPlainText(seg.getCaretLine());
				int end = (txt == null ? 0 : txt.length());
				if(seg.getMaxCharIndex() == end)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean isCaretAtEofPriorToLastUpdate()
	{
		return caretAtEofPriorToLastUpdate;
	}
	
	
	public void scrollToEOF()
	{
		log.debug("scrollToEOF");
		
		actions.moveDocumentEnd().fire();
		scrollCaretToView();
	}
	
	
	/** computes preferred content diimensions, including padding, assuming wrap lines mode is off */
	public Dimension2D computePreferredContentSize()
	{
		FxTextEditorModel m = getModel();
		int w = 0;
		int max = Math.min(1000, m.getLineCount());
		for(int i=0; i<max; i++)
		{
			ITextLine t = m.getTextLine(i);
			int len = t.getTextLength();
			if(len > w)
			{
				w = len;
			}
		}
		
		Insets p = getContentPadding();
		TextCellMetrics tm = vflow.textMetrics();
		double width = (w * tm.cellWidth) + p.getLeft() + p.getRight();
		double height = max * tm.cellHeight + p.getTop() + p.getBottom();
		return new Dimension2D(width, height);
	}
}
