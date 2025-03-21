// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;


/**
 * Default Values of Properties and Other Parameters.
 */
public class Defaults
{
	public static final double ASPECT_RATIO = 0.4;
	public static final double ASPECT_RATIO_MIN = 0.05;
	public static final double ASPECT_RATIO_MAX = 1.0;
	
	public static final Duration AUTO_SCROLL_PERIOD = Duration.millis(100); // arbitrary number
	public static final double AUTO_SCROLL_STEP_FAST = 200; // arbitrary number
	public static final double AUTO_SCROLL_STEP_SLOW = 20; // arbitrary number
	public static final double AUTO_SCROLL_THRESHOLD = 100; // arbitrary number
	
	public static final Color BACKGROUND_COLOR = Color.WHITE;
	
	public static final Color CARET_COLOR = Color.BLACK;
	public static final Color CARET_LINE_COLOR = Color.rgb(204, 224, 255, 0.3);
	public static final double CARET_WIDTH = 2.0;
	
	public static final Insets CONTENT_PADDING = null;
	
	public static final boolean DISPLAY_CARET = true;
	
	public static final boolean EDITABLE = true;
	
	public static final Font FONT = Font.font("Monospaced", -1);
	
	/** extra empty cell adds space to paint the trailing caret */
	public static final int HORIZONTAL_CARET_GUARD = 1;
	
	public static final double LINE_SPACING = 0.0;
	
	public static final double MIN_HEIGHT = 20;
	
	public static final double MIN_WIDTH = 20;
	
	public static final double PREF_HEIGHT = 150;
	
	public static final double PREF_WIDTH = 100;
	
	public static final double SCROLL_WHEEL_STEP_AMOUNT = 0.25;
	
	public static final Color SELECTION_BACKGROUND_COLOR = Color.rgb(255, 255, 128, 0.7);
	
	/**
	 * Number of paragraphs to lay out before and after the view port
	 * to form a sliding window, for the purpose of smoother scrolling.
	 */
	public static final int SLIDING_WINDOW_HALF = 100;
	
	public static final int TAB_SIZE = 8;
	
	public static final int TAB_SIZE_MAX = 32;
	
	public static final Color TEXT_COLOR = Color.BLACK;
	
	public static final int TRACK_PAD_STEP_AMOUNT = 3;
	
	public static final int VIEWPORT_ROW_COUNT_ESTIMATE = 128;
	
	public static final boolean WRAP_TEXT = false;
}
