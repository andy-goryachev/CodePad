// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


/**
 * Options Pane.
 */
public class OptionsPane
	extends BorderPane
{
	private final VBox vbox;
	
	
	public OptionsPane()
	{
		vbox = new VBox();
		
		ScrollPane scroll = new ScrollPane(vbox);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
		
		setCenter(scroll);
	}
	
	
	public Section section(String name)
	{
		Section s = new Section();
		
		TitledPane t = new TitledPane();
		t.setAnimated(false);
		t.setExpanded(true);
		t.setText(name);
		t.setContent(s);
		
		vbox.getChildren().add(t);
		return s;
	}
	
	
	public void option(Node n)
	{
		lastSection().add(n);
	}


	public void option(String text, Node n)
	{
		lastSection().add(text, n);
	}
	
	
	private Section lastSection()
	{
		List<Node> cs = vbox.getChildren();
		for(int i=cs.size()-1; i>=0; i++)
		{
			Node n = cs.get(i);
			if(n instanceof TitledPane t)
			{
				if(t.getContent() instanceof Section s)
				{
					return s;
				}
			}
		}
		
		return section("Options");
	}

	
	//
	
	
	public static class Section extends GridPane
	{
		private int row;
		private static final Insets PADDING = new Insets(2);
		private static final Insets CELL_PADDING = new Insets(1, 2, 1, 2);
		
		
		public Section()
		{
			setPadding(PADDING);
		}

		
		public void add(Node n)
		{
			add(n, 0, row++, 2, 1);
			setMargin(n, CELL_PADDING);
			setFillHeight(n, Boolean.TRUE);
			setFillWidth(n, Boolean.TRUE);
		}


		public void add(String text, Node n)
		{
			Label label = new Label(text);
			setMargin(label, CELL_PADDING);
			setFillHeight(label, Boolean.TRUE);
			setFillWidth(label, Boolean.TRUE);
			setHgrow(label, Priority.ALWAYS);
			add(label, 0, row);

			if(n == null)
			{
				label.setDisable(true);
			}
			else
			{
				add(n, 1, row);
				setMargin(n, CELL_PADDING);
				setFillHeight(n, Boolean.TRUE);
				setFillWidth(n, Boolean.TRUE);
			}
			
			row++;
		}
	}
}
