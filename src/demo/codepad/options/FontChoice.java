// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.CSorter;
import goryachev.fx.FX;
import goryachev.fx.FxComboBox;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;


/**
 * Font Choice.
 */
public class FontChoice extends HBox
{
	private final FxComboBox<String> fontField;
	private final FxComboBox<String> styleField;
	private final FxComboBox<Double> sizeField;
	private final SimpleObjectProperty<Font> prop = new SimpleObjectProperty<>();
	
	
	public FontChoice(String name, ObjectProperty<Font> p)
	{
		super(2);
		FX.setName(this, name);
		
		fontField = new FxComboBox<>();
		fontField.setStyle("-fx-max-width:15em;");
		FX.setName(fontField, name + "_FAMILY");
		fontField.getItems().setAll(listFontFamilies());
		FX.addChangeListener(fontField.selectedItemProperty(), this::handleFamilyChange);
		
		styleField = new FxComboBox<>();
		styleField.setStyle("-fx-max-width:8em;");
		FX.setName(styleField, name + "_STYLE");
		FX.addInvalidationListener(styleField.selectedItemProperty(), this::update);
		
		sizeField = new FxComboBox<>();
		sizeField.setStyle("-fx-max-width:6em;");
		sizeField.getItems().setAll(listSizes());
		FX.setName(sizeField, name + "SIZE");
		FX.addInvalidationListener(sizeField.selectedItemProperty(), this::update);
		
		getChildren().setAll
		(
			fontField,
			styleField,
			sizeField
		);
		setHgrow(fontField, Priority.ALWAYS);
		
		if(p != null)
		{
			prop.bindBidirectional(p);
		}
		setFont(prop.get());
	}
	
	
	private List<String> listFontFamilies()
	{
		return new CList<>(Font.getFamilies());
	}
	
	
	private List<Double> listSizes()
	{
		return List.of
		(
			2.0,
			6.0,
			7.0,
			8.0,
			9.0,
			10.0,
			11.0,
			12.0,
			13.0,
			14.0,
			15.0,
			16.0,
			18.0,
			20.0,
			22.0,
			24.0,
			28.0,
			32.0,
			36.0,
			48.0,
			72.0,
			144.0
		);
	}
	
	
	private void handleFamilyChange(String family)
	{
		updateStyles(family);
		update();
	}


	private void updateStyles(String family)
	{
		String old = styleField.getSelectedItem();
		
		List<String> ss = Font.getFontNames(family);
		for(int i=0; i<ss.size(); i++)
		{
			String s = ss.get(i);
			if(s.startsWith(family))
			{
				s = s.substring(family.length()).trim();
				ss.set(i, s);
			}
		}
		CSorter.collate(ss);
		
		if(old != null)
		{
			styleField.getItems().setAll(ss);
			int ix = ss.indexOf(old);
			if(ix >= 0)
			{
				styleField.select(ix);
			}
		}
	}
	
	
	private void update()
	{
		Font f = getFont();
		prop.set(f);
	}


	private void setFont(Font f)
	{
		String name;
		String style;
		double size;
		if(f == null)
		{
			name = null;
			style = null;
			size = 12.0;
		}
		else
		{
			name = f.getFamily();
			style = f.getStyle();
			size = f.getSize();
		}
		fontField.select(name);
		styleField.select(style);
		sizeField.select(size);
	}


	public Font getFont()
	{
		String s = fontField.getSelectedItem();
		if(s == null)
		{
			return null;
		}
		
		String st = styleField.getSelectedItem();
		if(CKit.isNotBlank(st))
		{
			s = s + " " + st;
		}
		
		Double sz = sizeField.getSelectedItem();
		if(sz == null)
		{
			sz = 12.0;
		}
		return new Font(s, sz);
	}
}
