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
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
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
	private ObjectProperty<Insets> contentPadding;
	private ObjectProperty<Font> font;
	private DoubleProperty lineSpacing;
	private IntegerProperty tabSize;
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


	public final TextPos getAnchorPosition()
	{
		return anchorPositionProperty().getValue();
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
		
		// content -padding
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
			CONTENT_PADDING,
			FONT,
			LINE_SPACING,
			TAB_SIZE,
			WRAP_TEXT
		);
	}
}
