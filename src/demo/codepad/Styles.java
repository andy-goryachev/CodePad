// Copyright © 2016-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.fx.CommonStyles;
import goryachev.fx.FxStyleSheet;
import goryachev.fx.Theme;
import javafx.scene.paint.Color;


/**
 * this is how a style sheet is generated.
 */
public class Styles
	extends FxStyleSheet
{
	public Styles()
	{
		Theme theme = Theme.current();
		
		add
		(
			// common fx styles
			new CommonStyles(),
			
			// demo ui
			selector(StatusBar.LABEL_LEADING).defines
			(
				padding(1, 1, 1, 5)
			),
			selector(StatusBar.LABEL_TRAILING).defines
			(
				padding(1, 15, 1, 1)
			),
			
			// code pad styles
			selector(CodePad.STYLE).defines
			(
				padding(0),
				backgroundColor(commas(Color.WHITE, Color.WHITE)),
				backgroundInsets(commas(0, 1)),
				backgroundRadius(commas(3, 2)),
				
				selector(DISABLED).defines
				(
					opacity(0.4)
				),
				
				selector(FOCUSED).defines
				(
					backgroundColor(commas("-fx-focus-color", "-fx-control-inner-background", "-fx-faint-focus-color")),
					backgroundInsets(commas(-0.2, 1, -1.4, 3)),
					backgroundRadius(commas(3, 2, 4, 0))
				)
			)
		);
	}
}
