// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.internal.SelectionModel;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.input.FID;
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
	public static class Fun
	{
		public static final FID MOVE_DOWN = new FID();
		public static final FID MOVE_LEFT = new FID();
		public static final FID MOVE_RIGHT = new FID();
		public static final FID MOVE_UP = new FID();
		public static final FID PAGE_DOWN = new FID();
		public static final FID PAGE_UP = new FID();
		public static final FID SELECT_ALL = new FID();
		public static final FID SELECT_DOWN = new FID();
		public static final FID SELECT_LEFT = new FID();
		public static final FID SELECT_PAGE_DOWN = new FID();
		public static final FID SELECT_PAGE_UP = new FID();
		public static final FID SELECT_RIGHT = new FID();
		public static final FID SELECT_UP = new FID();
	}
	
	public static final CssStyle STYLE = new CssStyle();
	
	
	private final Config config;
	private final InputMap inputMap;
	private SimpleObjectProperty<CodeModel> model;
	private final SelectionModel selectionModel = new SelectionModel();
	private DoubleProperty aspectRatio;
	private StyleableObjectProperty<Color> backgroundColor;
	private StyleableObjectProperty<Color> caretColor;
	private StyleableObjectProperty<Color> caretLineColor;
	private StyleableObjectProperty<Insets> contentPadding;
	private StyleableBooleanProperty displayCaretProperty;
	private StyleableObjectProperty<Font> font;
	private DoubleProperty lineSpacing;
	private StyleableObjectProperty<Color> selectionColor;
	private IntegerProperty tabSize;
	private StyleableObjectProperty<Color> textColor;
	private StyleableBooleanProperty wrapText;


	public CodePad(Config config, CodeModel model)
	{
		this.config = config.copy();
		this.inputMap = new InputMap(this);

		FX.style(this, STYLE);
		setModel(model);
		//setBorder(Border.stroke(Color.RED)); // FIX
	}


	public CodePad(CodeModel model)
	{
		this(Config.getDefault(), model);
	}
	
	
	public InputMap getInputMap()
	{
		return inputMap;
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
	
	
	public int getParagraphCount()
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
					// TODO only if changed
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
	// private final StyleableProperty<Number> tabSize = SPF.createStyleableNumberProperty(this, "tabSize", "-ag-tab-size", (c) -> c.tabSize, Defaults.TAB_SIZE);
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
					// TODO only if effective tab size changed
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
		private static final CssMetaData<CodePad,Number> ASPECT_RATIO = new CssMetaData<>("-ag-aspect-ratio", SizeConverter.getInstance(), Defaults.ASPECT_RATIO)
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
		private static final CssMetaData<CodePad,Color> BACKGROUND_COLOR = new CssMetaData<>("-ag-background-color", ColorConverter.getInstance(), Defaults.BACKGROUND_COLOR)
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
		private static final CssMetaData<CodePad,Color> CARET_COLOR = new CssMetaData<>("-ag-caret-color", ColorConverter.getInstance(), Defaults.CARET_COLOR)
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
		private static final CssMetaData<CodePad,Color> CARET_LINE_COLOR = new CssMetaData<>("-ag-caret-line-color", ColorConverter.getInstance(), Defaults.CARET_LINE_COLOR)
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
		private static final CssMetaData<CodePad,Insets> CONTENT_PADDING = new CssMetaData<>("-ag-content-padding", InsetsConverter.getInstance(), Defaults.CONTENT_PADDING)
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
		private static final CssMetaData<CodePad,Boolean> DISPLAY_CARET = new CssMetaData<>("-ag-display-caret", StyleConverter.getBooleanConverter(), Defaults.DISPLAY_CARET)
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

		// font
		private static final FontCssMetaData<CodePad> FONT = new FontCssMetaData<>("-ag-font", Defaults.FONT)
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
		private static final CssMetaData<CodePad,Number> LINE_SPACING = new CssMetaData<>("-ag-line-spacing", SizeConverter.getInstance(), 0)
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
		private static final CssMetaData<CodePad,Color> SELECTION_COLOR = new CssMetaData<>("-ag-selection-background-color", ColorConverter.getInstance(), Defaults.SELECTION_BACKGROUND_COLOR)
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
		private static final CssMetaData<CodePad,Number> TAB_SIZE = new CssMetaData<>("-ag-tab-size", SizeConverter.getInstance(), Defaults.TAB_SIZE)
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
		private static final CssMetaData<CodePad,Color> TEXT_COLOR = new CssMetaData<>("-ag-text-color", ColorConverter.getInstance(), Defaults.TEXT_COLOR)
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
		private static final CssMetaData<CodePad,Boolean> WRAP_TEXT = new CssMetaData<>("-ag-wrap-text", StyleConverter.getBooleanConverter(), Defaults.WRAP_TEXT)
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
	
	
	private void exec(FID f)
	{
		getInputMap().exec(Fun.SELECT_ALL);
	}
	
	
	public void selectAll()
	{
		exec(Fun.SELECT_ALL);
	}
}
