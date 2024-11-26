// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.internal.SelectionModel;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.fx.FX;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
 * CodePad is a high performance monospaced text editor for JavaFX.
 * 
 * Supports:
 * - large virtualized models up to ~2 billion paragraphs
 * - long paragraphs (millions on symbols)
 * - fixed-cell grid rendering
 * - limited set of text attributes
 * - limited decorations
 */
public class CodePad
	extends Control
{
	private final Config config;
    private final ReadOnlyObjectWrapper<TextPos> anchorPosition = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPos> caretPosition = new ReadOnlyObjectWrapper<>();
	private SimpleObjectProperty<CodeModel> model;
	private final SelectionModel selectionModel = new SelectionModel();
	private DoubleProperty aspectRatio;
	private ObjectProperty<Color> backgroundColor;
	private ObjectProperty<Color> caretColor;
	private ObjectProperty<Color> caretLineColor;
	private ObjectProperty<Insets> contentPadding;
	private ObjectProperty<Font> font;
	private DoubleProperty lineSpacing;
	private ObjectProperty<Color> selectionBackgroundColor;
	private IntegerProperty tabSize;
	private ObjectProperty<Color> textColor;
	private BooleanProperty wrapText;


	public CodePad(Config config, CodeModel model)
	{
		this.config = config.copy();
		setModel(model);
	}
	

	public CodePad(CodeModel model)
	{
		this(Config.getDefault(), model);
	}


	@Override
	protected CodePadSkin createDefaultSkin()
	{
		return new CodePadSkin(this);
	}


	@Override
	public List<CssMetaData<? extends Styleable,?>> getControlCssMetaData()
	{
		return StyleableProperties.STYLEABLES;
	}


	public final ReadOnlyProperty<TextPos> anchorPositionProperty()
	{
		return anchorPosition.getReadOnlyProperty();
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
		return caretPosition.getReadOnlyProperty();
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
			lineSpacing = new StyleableDoubleProperty(0)
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
		return lineSpacing == null ? 0 : lineSpacing.get();
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
		return selectionModel.getSelection();
	}
	
	
	/**
	 * Defines the selection background color.
	 *
	 * @defaultValue Color.TBD
	 */
	public final ObjectProperty<Color> selectionBackgroundColorProperty()
	{
		if(selectionBackgroundColor == null)
		{
			selectionBackgroundColor = new StyleableObjectProperty(Defaults.SELECTION_BACKGROUND_COLOR)
			{
				@Override
				public Object getBean()
				{
					return CodePad.this;
				}


				@Override
				public String getName()
				{
					return "selectionBackgroundColor";
				}


				@Override
				public CssMetaData<CodePad,Color> getCssMetaData()
				{
					return StyleableProperties.SELECTION_BACKGROUND_COLOR;
				}


				@Override
				public void invalidated()
				{
					requestLayout();
				}
			};
		}
		return selectionBackgroundColor;
	}


	public final Color getSelectionBackgroundColor()
	{
		return selectionBackgroundColor == null ? Defaults.SELECTION_BACKGROUND_COLOR : selectionBackgroundColor.get();
	}


	public final void setSelectionBackgroundColor(Color c)
	{
		selectionBackgroundColorProperty().set(c);
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
			wrapText = new StyleableBooleanProperty(false)
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
		return wrapText.getValue();
	}


	public final void setWrapText(boolean on)
	{
		wrapText.setValue(on);
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
		private static final CssMetaData<CodePad,Color> SELECTION_BACKGROUND_COLOR = new CssMetaData<>("-ag-selection-background-color", ColorConverter.getInstance(), Defaults.SELECTION_BACKGROUND_COLOR)
		{
			@Override
			public boolean isSettable(CodePad n)
			{
				return n.selectionBackgroundColor == null || !n.selectionBackgroundColor.isBound();
			}


			@Override
			public StyleableProperty<Color> getStyleableProperty(CodePad n)
			{
				return (StyleableProperty<Color>)n.selectionBackgroundColorProperty();
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
			FONT,
			LINE_SPACING,
			SELECTION_BACKGROUND_COLOR,
			TAB_SIZE,
			TEXT_COLOR,
			WRAP_TEXT
		);
	}
}
