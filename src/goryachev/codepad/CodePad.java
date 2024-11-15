// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.internal.SelectionModel;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.skin.CodePadSkin;
import goryachev.fxtexteditor.TextPos;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
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
	// styleable properties are not created lazily
	private static final StyleablePropertyFactory<CodePad> SPF = new StyleablePropertyFactory<>(Control.getClassCssMetaData());
	private final StyleableProperty<Insets> contentPadding = SPF.createStyleableInsetsProperty(this, "contentPadding", "-ag-content-padding", (c) -> c.contentPadding, Defaults.CONTENT_PADDING);
	private final StyleableProperty<Font> font = SPF.createStyleableFontProperty(this, "font", "-ag-font", (c) -> c.font, Defaults.FONT);
	private final StyleableProperty<Number> tabSize = SPF.createStyleableNumberProperty(this, "tabSize", "-ag-tab-size", (c) -> c.tabSize, Defaults.TAB_SIZE);
	private final StyleableProperty<Boolean> wrapText = SPF.createStyleableBooleanProperty(this, "wrapText", "-ag-wrap-text", (c) -> c.wrapText, Defaults.WRAP_TEXT);


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
		return SPF.getCssMetaData();
	}


	public final ReadOnlyProperty<TextPos> anchorPositionProperty()
	{
		return anchorPosition.getReadOnlyProperty();
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
	public final ObservableValue<Insets> contentPaddingProperty()
	{
		return (ObservableValue<Insets>)contentPadding;
	}


	public final Insets getContentPadding()
	{
		return contentPadding.getValue();
	}


	public final void setContentPadding(Insets v)
	{
		contentPadding.setValue(v);
	}
	

	/**
	 * The font to be used by the editor.
	 * @defaultValue Monospaced font ot the {@link Font#defaultSystemFontSize default size}
	 */
	public final ObservableValue<Font> fontProperty()
	{
		return (ObservableValue<Font>)font;
	}


	public final Font getFont()
	{
		return font.getValue();
	}


	public final void setFont(Font f)
	{
		font.setValue(f);
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


	public final void setModel(CodeModel m)
	{
		modelProperty().set(m);
	}


	public final CodeModel getModel()
	{
		return model == null ? null : model.get();
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
	// TODO this property should be initialized lazily and be an IntegerProperty styleable property should be extracted into a separate class.
	public final ObservableValue<Number> tabSizeProperty()
	{
		return (ObservableValue<Number>)tabSize;
	}


	public final int getTabSize()
	{
		int n = tabSize.getValue().intValue();
		if(n < 1)
		{
			return 1;
		}
		else if(n > Defaults.TAB_SIZE_MAX)
		{
			return Defaults.TAB_SIZE_MAX;
		}
		return n;
	}


	public final void setTabSize(int v)
	{
		tabSize.setValue(v);
	}


    /**
     * Determines whether the text should be wrapped to fin the viewable area width.
     * <p>
     * The horizontal scrolling will be disabled when this property is set to {@code true},
     * and the horizontal scroll bar will be hidden.
     * 
     * @defaultValue {@code false}
     */
	public final ObservableValue<Boolean> wrapTextProperty()
	{
		return (ObservableValue<Boolean>)wrapText;
	}


	public final boolean isWrapText()
	{
		return wrapText.getValue();
	}


	public final void setWrapText(boolean on)
	{
		wrapText.setValue(on);
	}
}
