// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


/**
 * Defaults.
 */
public class Defaults
{
	public static final double ASPECT_RATIO = 0.4;
	public static final double ASPECT_RATIO_MIN = 0.05;
	public static final double ASPECT_RATIO_MAX = 1.0;
	
	public static final Color BACKGROUND_COLOR = Color.WHITE;
	
	public static final Color CARET_LINE_COLOR = Color.rgb(255, 200, 255);
	
	public static final double CARET_LINE_OPACITY = 0.3;
	
	public static final double CELL_BACKGROUND_OPACITY = 0.8;
	
	public static final Insets CONTENT_PADDING = null;
	
	public static final Font FONT = Font.font("Monospaced", -1);
	
	public static final double LINE_COLOR_OPACITY = 0.9;
	
	public static final double MIN_HEIGHT = 20;
	
	public static final double MIN_WIDTH = 20;
	
	public static final double PREF_HEIGHT = 150;
	
	public static final double PREF_WIDTH = 100;
	
	public static final Color SELECTION_BACKGROUND_COLOR = Color.rgb(255, 255, 128);
	
	public static final double SELECTION_BACKGROUND_OPACITY = 0.9;
	
	/**
	 * Number of paragraphs to lay out before and after the view port
	 * to form a sliding window, for the purpose of smoother scrolling.
	 */
	public static final int SLIDING_WINDOW_HALF = 100;
	
	public static final int TAB_SIZE = 8;
	
	public static final int TAB_SIZE_MAX = 32;
	
	public static final Color TEXT_COLOR = Color.BLACK;
	
	public static final int VIEWPORT_ROW_COUNT_ESTIMATE = 128;
	
	public static final boolean WRAP_TEXT = false;
}
