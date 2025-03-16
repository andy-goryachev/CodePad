// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.codepad.CodePad.FN;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.internal.SelectionModel;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.input.Func;
import goryachev.fx.input.InputMap;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.ColorConverter;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


/**
 * CodePad is a high performance monospaced text editor for JavaFX,
 * suitable for code editors and file/log/xml/json viewers.
 * 
 * Supported:
 * - large virtualized models up to ~2 billion paragraphs
 * - long paragraphs (millions of symbols)
 * - fixed-cell grid rendering
 * - limited set of text attributes
 * - limited decorations
 * 
 * Not supported:
 * - bidirectional text
 * - text shaping
 * - proportional fonts
 */
public class CodePad
	extends Control
{
	/** CodePad function identifiers. */
	public static class FN
	{
		public static final Func BACKSPACE = new Func();
		public static final Func COPY = new Func();
		public static final Func COPY_PLAIN_TEXT = new Func();
		public static final Func CUT = new Func();
		public static final Func DELETE = new Func();
		public static final Func DELETE_PARAGRAPH = new Func();
		public static final Func DELETE_TO_PARAGRAPH_START = new Func();
		public static final Func DELETE_WORD_NEXT = new Func();
		public static final Func DELETE_WORD_PREVIOUS = new Func();
		public static final Func ERROR_FEEDBACK = new Func();
		public static final Func FOCUS_NEXT = new Func();
		public static final Func FOCUS_PREVIOUS = new Func();
		public static final Func INSERT_LINE_BREAK = new Func();
		public static final Func INSERT_TAB = new Func();
		public static final Func MOVE_DOWN = new Func();
		public static final Func MOVE_LEFT = new Func();
		public static final Func MOVE_RIGHT = new Func();
		public static final Func MOVE_TO_DOCUMENT_END = new Func();
		public static final Func MOVE_TO_DOCUMENT_START = new Func();
		public static final Func MOVE_TO_LINE_END = new Func();
		public static final Func MOVE_TO_LINE_START = new Func();
		public static final Func MOVE_TO_PARAGRAPH_END = new Func();
		public static final Func MOVE_TO_PARAGRAPH_START = new Func();
		public static final Func MOVE_UP = new Func();
		public static final Func MOVE_WORD_LEFT = new Func();
		public static final Func MOVE_WORD_RIGHT = new Func();
		public static final Func PAGE_DOWN = new Func();
		public static final Func PAGE_UP = new Func();
		public static final Func PASTE = new Func();
		public static final Func PASTE_PLAIN_TEXT = new Func();
		public static final Func REDO = new Func();
		public static final Func SELECT_ALL = new Func();
		public static final Func SELECT_DOWN = new Func();
		public static final Func SELECT_LEFT = new Func();
		public static final Func SELECT_PAGE_DOWN = new Func();
		public static final Func SELECT_PAGE_UP = new Func();
		public static final Func SELECT_PARAGRAPH = new Func();
		public static final Func SELECT_RIGHT = new Func();
		public static final Func SELECT_TO_DOCUMENT_END = new Func();
		public static final Func SELECT_TO_DOCUMENT_START = new Func();
		public static final Func SELECT_TO_LINE_END = new Func();
		public static final Func SELECT_TO_LINE_START = new Func();
		public static final Func SELECT_TO_PARAGRAPH_END = new Func();
		public static final Func SELECT_TO_PARAGRAPH_START = new Func();
		public static final Func SELECT_UP = new Func();
		public static final Func SELECT_WORD = new Func();
		public static final Func SELECT_WORD_LEFT = new Func();
		public static final Func SELECT_WORD_RIGHT = new Func();
		public static final Func UNDO = new Func();
	}
	
	public static final CssStyle STYLE = new CssStyle();
	
	private final InputMap inputMap;
	private SimpleObjectProperty<CodeModel> model;
	private final SelectionModel selectionModel = new SelectionModel();
	private DoubleProperty aspectRatio;
	private StyleableObjectProperty<Color> backgroundColor;
	private StyleableObjectProperty<Color> caretColor;
	private StyleableObjectProperty<Color> caretLineColor;
	private StyleableObjectProperty<Insets> contentPadding;
	private StyleableBooleanProperty displayCaretProperty;
	private StyleableBooleanProperty editable;
	private StyleableObjectProperty<Font> font;
	private DoubleProperty lineSpacing;
	private StyleableObjectProperty<Color> selectionColor;
	private IntegerProperty tabSize;
	private StyleableObjectProperty<Color> textColor;
	private StyleableBooleanProperty wrapText;


	public CodePad(CodeModel model)
	{
		this.inputMap = new InputMap(this);
		STYLE.set(this);
		setModel(model);
	}

	
	public InputMap getInputMap()
	{
		return inputMap;
	}
	
	
	/**
	 * Moves the caret to the specified position.
	 * When the {@code extendSelection} flag is {@code true}, the selection is extended to the new position.
	 * When the {@code clearPhantomPosition} is {@code true}, the "phantom x position", or the column from which the vertical navigation
	 * has started, is cleared.
	 * 
	 * @param p the position to move the caret to
	 * @param extendSelection whether to extend selection or not
	 * @param clearPhantomPosition whether to clear (remove) phantom x position
	 */
	public final void moveCaret(TextPos p, boolean extendSelection, boolean clearPhantomPosition)
	{
		if(p != null)
		{
			if(extendSelection)
			{
				extendSelection(p);
			}
			else
			{
				select(p);
			}
			
			if(clearPhantomPosition)
			{
				Object v = getSkin();
				if(v instanceof CodePadSkin skin)
				{
					skin.clearPhantomX();
				}
			}
		}
	}


	public void select(TextPos p)
	{
		select(p, p);
	}


	public void select(TextPos anchor, TextPos caret)
	{
		CodeModel m = getModel();
		if(m != null)
		{
			selectionModel.setSelectionRange(m, anchor, caret);
		}
	}
	
	
	public void extendSelection(TextPos p)
	{
		CodeModel m = getModel();
		if(m != null)
		{
			selectionModel.extendSelection(m, p);
		}
	}


	@Override
	protected CodePadSkin createDefaultSkin()
	{
		return new CodePadSkin(this);
	}
	
	
	public TextPos getTextPositionFor(double screenx, double screeny)
	{
		Object v = getSkin();
		if(v instanceof CodePadSkin skin)
		{
			return skin.getTextPositionFor(screenx, screeny);
		}
		return null;
	}


	@Override
	public List<CssMetaData<? extends Styleable,?>> getControlCssMetaData()
	{
		return StyleableProperties.STYLEABLES;
	}
	
	
	/**
	 * Returns the {@link CodeParagraph} for the given {@code index},
	 * or null if the model is {@code null} or the specified {@code index} is outside of the
	 * model boundaries.
	 * 
	 * @return CodeParagraph or null
	 */
	public final CodeParagraph getParagraph(int ix)
	{
		CodeModel m = getModel();
		if(m != null)
		{
			if((ix >= 0) && (ix < m.size()))
			{
				return m.getParagraph(ix);
			}
		}
		return null;
	}
	
	
	public final int getParagraphCount()
	{
		CodeModel m = getModel();
		return m == null ? 0 : m.size();
	}


	public final ReadOnlyProperty<TextPos> anchorPositionProperty()
	{
		return selectionModel.anchorPositionProperty();
	}


	/**
	 * Defines the text cell aspect ratio, the cell width divided by the cell height.
	 * The actual value used will be clipped to the range [0.05 ... 1.0] (inclusive).
	 * <p>
	 * A value of 1.0 results in a square cell.
	 *
	 * @defaultValue 0.4
	 */
	public final DoubleProperty aspectRatioProperty()
	{
		if(aspectRatio == null)
		{
			aspectRatio = new StyleableDoubleProperty(Defaults.ASPECT_RATIO)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "aspectRatio";
				}


				@Override
				public CssMetaData<CodePad,Number> getCssMetaData()
				{
					return StyleableProperties.ASPECT_RATIO;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return aspectRatio;
	}


	public final double getAspectRatio()
	{
		return aspectRatio == null ? Defaults.ASPECT_RATIO : aspectRatio.get();
	}


	public final void setAspectRatio(double v)
	{
		aspectRatioProperty().set(v);
	}
	
	
	/**
	 * Defines the CodePad content background color.
	 *
	 * @defaultValue Color.WHITE
	 */
	public final ObjectProperty<Color> backgroundColorProperty()
	{
		if(backgroundColor == null)
		{
			backgroundColor = new StyleableObjectProperty(Defaults.BACKGROUND_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "backgroundColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.BACKGROUND_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return backgroundColor;
	}


	public final Color getBackgroundColor()
	{
		return backgroundColor == null ? Defaults.BACKGROUND_COLOR : backgroundColor.get();
	}


	public final void setBackgroundColor(Color c)
	{
		backgroundColorProperty().set(c);
	}


	public final TextPos getAnchorPosition()
	{
		return anchorPositionProperty().getValue();
	}
	
	
	/**
	 * Defines the color of the caret.
	 *
	 * @defaultValue Color.BLACK
	 */
	public final ObjectProperty<Color> caretColorProperty()
	{
		if(caretColor == null)
		{
			caretColor = new StyleableObjectProperty(Defaults.CARET_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "caretColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.CARET_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return caretColor;
	}


	public final Color getCaretColor()
	{
		return caretColor == null ? Defaults.CARET_COLOR : caretColor.get();
	}


	public final void setCaretColor(Color c)
	{
		caretColorProperty().set(c);
	}
	
	
	/**
	 * Defines the background color of the current caret paragraph.
	 *
	 * @defaultValue Color.TBD
	 */
	public final ObjectProperty<Color> caretLineColorProperty()
	{
		if(caretLineColor == null)
		{
			caretLineColor = new StyleableObjectProperty(Defaults.CARET_LINE_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "caretLineColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.CARET_LINE_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return caretLineColor;
	}


	public final Color getCaretLineColor()
	{
		return caretLineColor == null ? Defaults.CARET_LINE_COLOR : caretLineColor.get();
	}


	public final void setCaretLineColor(Color c)
	{
		caretLineColorProperty().set(c);
	}
	
	
	public final ReadOnlyProperty<TextPos> caretPositionProperty()
	{
		return selectionModel.caretPositionProperty();
	}
	
	
	public final TextPos getCaretPosition()
	{
		return caretPositionProperty().getValue();
	}
	

	/**
	 * Determines the padding between the text content and the edges of the document.
	 * 
	 * @defaultValue {@code null}
	 */
	public final ObjectProperty<Insets> contentPaddingProperty()
	{
		if(contentPadding == null)
		{
			contentPadding = new StyleableObjectProperty<Insets>(Insets.EMPTY)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "contentPadding";
				}


				@Override
				public CssMetaData<CodePad,Insets> getCssMetaData()
				{
					return StyleableProperties.CONTENT_PADDING;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return contentPadding;
	}


	public final Insets getContentPadding()
	{
		return contentPadding == null ? null : contentPadding.getValue();
	}


	public final void setContentPadding(Insets v)
	{
		contentPaddingProperty().setValue(v);
	}
	
	
    /**
     * Determines whether to show the caret.
     * 
     * @defaultValue {@code true}
     */
	public final BooleanProperty displayCaretProperty()
	{
		if(displayCaretProperty == null)
		{
			displayCaretProperty = new StyleableBooleanProperty(Defaults.DISPLAY_CARET)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "displayCaret";
				}


				@Override
				public CssMetaData getCssMetaData()
				{
					return StyleableProperties.DISPLAY_CARET;
				}
			};
		}
		return displayCaretProperty;
	}


	public final boolean isDisplayCaret()
	{
		return displayCaretProperty == null ? Defaults.DISPLAY_CARET : displayCaretProperty.getValue();
	}


	public final void setDisplayCaret(boolean on)
	{
		displayCaretProperty().setValue(on);
	}
	
	
    /**
     * Indicates whether this CodeArea can be edited by the user.
     * 
     * @defaultValue {@code true}
     */
	public final BooleanProperty editableProperty()
	{
		if(editable == null)
		{
			editable = new StyleableBooleanProperty(Defaults.EDITABLE)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "editable";
				}


				@Override
				public CssMetaData getCssMetaData()
				{
					return StyleableProperties.EDITABLE;
				}
			};
		}
		return editable;
	}


	public final boolean isEditable()
	{
		return editable == null ? Defaults.EDITABLE : editable.getValue();
	}


	public final void setEditable(boolean on)
	{
		editableProperty().setValue(on);
	}


	/**
	 * The font to be used by the editor.
	 * 
	 * @defaultValue Monospaced font ot the default size
	 */
	public final ObjectProperty<Font> fontProperty()
	{
		if(font == null)
		{
			font = new StyleableObjectProperty<Font>(Font.getDefault())
			{
				@Override
				protected void invalidated()
				{
					requestLayout();
				}


				@Override
				public CssMetaData<CodePad,Font> getCssMetaData()
				{
					return StyleableProperties.FONT;
				}


				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "font";
				}
			};
		}
		return font;
	}


	public final Font getFont()
	{
		return font == null ? Defaults.FONT : font.getValue();
	}


	public final void setFont(Font f)
	{
		fontProperty().setValue(f);
	}
	
	
    /**
     * Defines the vertical space between lines, in pixels.
     *
     * @defaultValue 0
     */
	public final DoubleProperty lineSpacingProperty()
	{
		if(lineSpacing == null)
		{
			lineSpacing = new StyleableDoubleProperty(Defaults.LINE_SPACING)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "lineSpacing";
				}


				@Override
				public CssMetaData<CodePad,Number> getCssMetaData()
				{
					return StyleableProperties.LINE_SPACING;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return lineSpacing;
	}
	

	public final double getLineSpacing()
	{
		return lineSpacing == null ? Defaults.LINE_SPACING : lineSpacing.get();
	}

	
	public final void setLineSpacing(double spacing)
	{
		lineSpacingProperty().set(spacing);
	}


	public final ObjectProperty<CodeModel> modelProperty()
	{
		if(model == null)
		{
			model = new SimpleObjectProperty<>(this, "model")
			{
				@Override
				protected void invalidated()
				{
					selectionModel.clear();
				}
			};
		}
		return model;
	}


	public final CodeModel getModel()
	{
		return model == null ? null : model.get();
	}


	public final void setModel(CodeModel m)
	{
		modelProperty().set(m);
	}


	public final ReadOnlyProperty<SelectionRange> selectionProperty()
	{
		return selectionModel.selectionProperty();
	}


	public final SelectionRange getSelection()
	{
		return selectionModel.getSelectionRange();
	}
	
	
	/**
	 * Defines the selection background color.
	 *
	 * @defaultValue Color.TBD
	 */
	public final ObjectProperty<Color> selectionColorProperty()
	{
		if(selectionColor == null)
		{
			selectionColor = new StyleableObjectProperty(Defaults.SELECTION_BACKGROUND_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "selectionColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.SELECTION_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return selectionColor;
	}


	public final Color getSelectionColor()
	{
		return selectionColor == null ? Defaults.SELECTION_BACKGROUND_COLOR : selectionColor.get();
	}


	public final void setSelectionColor(Color c)
	{
		selectionColorProperty().set(c);
	}
	
	
    /**
     * The size of a tab stop.
     * The values are converted to an {@code int}.
     * Values less than 1 are treated as 1.  Values greater than 32 are treated as 32.
     * @defaultValue 8
     */
	public final IntegerProperty tabSizeProperty()
	{
		if(tabSize == null)
		{
			tabSize = new StyleableIntegerProperty(Defaults.TAB_SIZE)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "tabSize";
				}


				@Override
				public CssMetaData getCssMetaData()
				{
					return StyleableProperties.TAB_SIZE;
				}


				@Override
				protected void invalidated()
				{
					requestLayout();
				}
			};
		}
		return tabSize;
	}


	public final int getTabSize()
	{
		return tabSize == null ? Defaults.TAB_SIZE : tabSize.get();
	}


	public final void setTabSize(int v)
	{
		tabSizeProperty().set(v);
	}
	
	
	/**
	 * Defines the CodePad text color.
	 *
	 * @defaultValue Color.BLACK
	 */
	public final ObjectProperty<Color> textColorProperty()
	{
		if(textColor == null)
		{
			textColor = new StyleableObjectProperty(Defaults.TEXT_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "textColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.TEXT_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return textColor;
	}


	public final Color getTextColor()
	{
		return textColor == null ? Defaults.TEXT_COLOR : textColor.get();
	}


	public final void setTextColor(Color c)
	{
		textColorProperty().set(c);
	}
	

    /**
     * Determines whether the text should be wrapped to fin the viewable area width.
     * <p>
     * The horizontal scrolling will be disabled when this property is set to {@code true},
     * and the horizontal scroll bar will be hidden.
     * 
     * @defaultValue {@code false}
     */
	public final BooleanProperty wrapTextProperty()
	{
		if(wrapText == null)
		{
			wrapText = new StyleableBooleanProperty(Defaults.WRAP_TEXT)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "wrapText";
				}


				@Override
				public CssMetaData getCssMetaData()
				{
					return StyleableProperties.WRAP_TEXT;
				}
			};
		}
		return wrapText;
	}


	public final boolean isWrapText()
	{
		return wrapText == null ? Defaults.WRAP_TEXT : wrapText.getValue();
	}


	public final void setWrapText(boolean on)
	{
		wrapTextProperty().setValue(on);
	}


	/** styleable properties monstrocity */
	private static final class StyleableProperties
	{
		// aspect ratio
		private static final CssMetaData<CodePad,Number> ASPECT_RATIO = new CssMetaData<>("-fx-aspect-ratio", SizeConverter.getInstance(), Defaults.ASPECT_RATIO)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.aspectRatio == null || !n.aspectRatio.isBound();
			}


			@Override
			public StyleableProperty<Number> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Number>)n.aspectRatioProperty();
			}
		};
		
		// background color
		private static final CssMetaData<CodePad,Color> BACKGROUND_COLOR = new CssMetaData<>("-fx-color-background", ColorConverter.getInstance(), Defaults.BACKGROUND_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.backgroundColor == null || !n.backgroundColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.backgroundColorProperty();
			}
		};
		
		// caret color
		private static final CssMetaData<CodePad,Color> CARET_COLOR = new CssMetaData<>("-fx-caret-color", ColorConverter.getInstance(), Defaults.CARET_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.caretColor == null || !n.caretColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.caretColorProperty();
			}
		};
		
		// caret line color
		private static final CssMetaData<CodePad,Color> CARET_LINE_COLOR = new CssMetaData<>("-fx-caret-line-color", ColorConverter.getInstance(), Defaults.CARET_LINE_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.caretLineColor == null || !n.caretLineColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.caretLineColorProperty();
			}
		};
		
		// content padding
		private static final CssMetaData<CodePad,Insets> CONTENT_PADDING = new CssMetaData<>("-fx-content-padding", InsetsConverter.getInstance(), Defaults.CONTENT_PADDING)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.contentPadding == null || !n.contentPadding.isBound();
			}


			@Override
			public StyleableProperty<Insets> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Insets>)n.contentPaddingProperty();
			}
		};
		
		// display caret
		private static final CssMetaData<CodePad,Boolean> DISPLAY_CARET = new CssMetaData<>("-fx-display-caret", StyleConverter.getBooleanConverter(), Defaults.DISPLAY_CARET)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.displayCaretProperty == null || !n.displayCaretProperty.isBound(); 
			}


			@Override
			public StyleableProperty<Boolean> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Boolean>)n.displayCaretProperty();
			}
		};
		
		// editable
		private static final CssMetaData<CodePad,Boolean> EDITABLE = new CssMetaData<>("-fx-editable", StyleConverter.getBooleanConverter(), Defaults.EDITABLE)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.editable == null || !n.editable.isBound(); 
			}


			@Override
			public StyleableProperty<Boolean> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Boolean>)n.editableProperty();
			}
		};

		// font
		private static final FontCssMetaData<CodePad> FONT = new FontCssMetaData<>("-fx-font", Defaults.FONT)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.font == null || !n.font.isBound();
			}


			@Override
			public StyleableProperty<Font> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Font>)n.fontProperty();
			}
		};

		// line spacing
		private static final CssMetaData<CodePad,Number> LINE_SPACING = new CssMetaData<>("-fx-line-spacing", SizeConverter.getInstance(), 0)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.lineSpacing == null || !n.lineSpacing.isBound();
			}


			@Override
			public StyleableProperty<Number> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Number>)n.lineSpacingProperty();
			}
		};
		
		// selection background
		private static final CssMetaData<CodePad,Color> SELECTION_COLOR = new CssMetaData<>("-fx-selection-background-color", ColorConverter.getInstance(), Defaults.SELECTION_BACKGROUND_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.selectionColor == null || !n.selectionColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.selectionColorProperty();
			}
		};

		// tab size
		private static final CssMetaData<CodePad,Number> TAB_SIZE = new CssMetaData<>("-fx-tab-size", SizeConverter.getInstance(), Defaults.TAB_SIZE)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.tabSize == null || !n.tabSize.isBound();
			}


			@Override
			public StyleableProperty<Number> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Number>)n.tabSizeProperty();
			}
		};
		
		// text color
		private static final CssMetaData<CodePad,Color> TEXT_COLOR = new CssMetaData<>("-fx-text-color", ColorConverter.getInstance(), Defaults.TEXT_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.textColor == null || !n.textColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.textColorProperty();
			}
		};

		// wrap text
		private static final CssMetaData<CodePad,Boolean> WRAP_TEXT = new CssMetaData<>("-fx-wrap-text", StyleConverter.getBooleanConverter(), Defaults.WRAP_TEXT)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.wrapText == null || !n.wrapText.isBound(); 
			}


			@Override
			public StyleableProperty<Boolean> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Boolean>)n.wrapTextProperty();
			}
		};

		private static final List<CssMetaData<? extends Styleable,?>> STYLEABLES = FX.initCssMetadata
		(
			Control.getClassCssMetaData(),
			ASPECT_RATIO,
			BACKGROUND_COLOR,
			CARET_COLOR,
			CARET_LINE_COLOR,
			CONTENT_PADDING,
			DISPLAY_CARET,
			EDITABLE,
			FONT,
			LINE_SPACING,
			SELECTION_COLOR,
			TAB_SIZE,
			TEXT_COLOR,
			WRAP_TEXT
		);
	}
	
	
	public TextPos getDocumentEnd()
	{
		CodeModel m = getModel();
		return m == null ? TextPos.ZERO : m.getDocumentEnd();
	}
	
	
	private void exec(Func f)
	{
		getInputMap().exec(f);
	}
	
	
	public void backspace()
	{
		exec(FN.BACKSPACE);
	}
	
	
	public void copy()
	{
		exec(FN.COPY);
	}

	
	public void copyPlainText()
	{
		exec(FN.COPY_PLAIN_TEXT);
	}
	
	
	public void cut()
	{
		exec(FN.CUT);
	}
	
	
	public void delete()
	{
		exec(FN.DELETE);
	}
	
	
	public void deleteParagraph()
	{
		exec(FN.DELETE_PARAGRAPH);
	}
	
	
	public void deleteToParagraphStart()
	{
		exec(FN.DELETE_TO_PARAGRAPH_START);
	}
	
	
	public void deleteWordNext()
	{
		exec(FN.DELETE_WORD_NEXT);
	}
	
	
	public void deleteWordPrevious()
	{
		exec(FN.DELETE_WORD_PREVIOUS);
	}
	
	
	public void errorFeedback()
	{
		exec(FN.DELETE_WORD_PREVIOUS);
	}
	
	
	public void focusNext()
	{
		exec(FN.FOCUS_NEXT);
	}
	
	
	public void focusPrevious()
	{
		exec(FN.FOCUS_PREVIOUS);
	}
	
	
	public void insertLineBreak()
	{
		exec(FN.INSERT_LINE_BREAK);
	}
	
	
	public void insertTab()
	{
		exec(FN.INSERT_TAB);
	}
	
	
	public void moveDown()
	{
		exec(FN.MOVE_DOWN);
	}
	
	
	public void moveLeft()
	{
		exec(FN.MOVE_LEFT);
	}
	
	
	public void moveRight()
	{
		exec(FN.MOVE_RIGHT);
	}
	
	
	public void moveToDocumentEnd()
	{
		exec(FN.MOVE_TO_DOCUMENT_END);
	}
	
	
	public void moveToDocumentStart()
	{
		exec(FN.MOVE_TO_DOCUMENT_START);
	}
	
	
	public void moveToLineEnd()
	{
		exec(FN.MOVE_TO_LINE_END);
	}
	
	
	public void moveToLineStart()
	{
		exec(FN.MOVE_TO_LINE_START);
	}
	
	
	public void moveToParagraphEnd()
	{
		exec(FN.MOVE_TO_PARAGRAPH_END);
	}
	
	
	public void moveToParagraphStart()
	{
		exec(FN.MOVE_TO_PARAGRAPH_START);
	}
	
	
	public void moveUp()
	{
		exec(FN.MOVE_UP);
	}
	
	
	public void moveWordLeft()
	{
		exec(FN.MOVE_WORD_LEFT);
	}
	
	
	public void moveWordRight()
	{
		exec(FN.MOVE_WORD_RIGHT);
	}
	
	
	public void pageDown()
	{
		exec(FN.PAGE_DOWN);
	}
	
	
	public void pageUp()
	{
		exec(FN.PAGE_UP);
	}
	
	
	public void paste()
	{
		exec(FN.PASTE);
	}
	
	
	public void pastePlainText()
	{
		exec(FN.PASTE_PLAIN_TEXT);
	}
	
	
	public void redo()
	{
		exec(FN.REDO);
	}
	
	
	public void selectAll()
	{
		exec(FN.SELECT_ALL);
	}
	
	
	public void selectDown()
	{
		exec(FN.SELECT_DOWN);
	}
	
	
	public void selectLeft()
	{
		exec(FN.SELECT_LEFT);
	}
	
	
	public void selectPageDown()
	{
		exec(FN.SELECT_PAGE_DOWN);
	}

	
	public void selectPageUp()
	{
		exec(FN.SELECT_PAGE_UP);
	}
	
	
	public void selectParagraph()
	{
		exec(FN.SELECT_PARAGRAPH);
	}
	
	
	public void selectRight()
	{
		exec(FN.SELECT_RIGHT);
	}
	
	
	public void selectToDocumentEnd()
	{
		exec(FN.SELECT_TO_DOCUMENT_END);
	}
	
	
	public void selectToDocumentStart()
	{
		exec(FN.SELECT_TO_DOCUMENT_START);
	}
	
	
	public void selectToLineEnd()
	{
		exec(FN.SELECT_TO_LINE_END);
	}
	
	
	public void selectToLineStart()
	{
		exec(FN.SELECT_TO_LINE_START);
	}
	
	
	public void selectUp()
	{
		exec(FN.SELECT_UP);
	}
	
	
	public void selectWord()
	{
		exec(FN.SELECT_WORD);
	}
	
	
	public void selectWordLeft()
	{
		exec(FN.SELECT_WORD_LEFT);
	}
	
	
	public void selectWordRight()
	{
		exec(FN.SELECT_WORD_RIGHT);
	}
	
	
	public void undo()
	{
		exec(FN.UNDO);
	}
}
