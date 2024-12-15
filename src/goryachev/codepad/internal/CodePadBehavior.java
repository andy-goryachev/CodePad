// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.CodePad.Fun;
import goryachev.codepad.TextPos;
import goryachev.fx.input.BehaviorBase;
import goryachev.fx.input.KB;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


/**
 * CodePad Behavior.
 */
public class CodePadBehavior
	extends BehaviorBase<CodePad>
{
	private CellGrid grid;
	
	
	public CodePadBehavior(CodePad c, CellGrid g)
	{
		super(c);
		this.grid = g;
	}


	@Override
	protected void populateSkinInputMap()
	{
		// TODO
		
		func(Fun.MOVE_DOWN, this::moveDown);
		func(Fun.MOVE_LEFT, this::moveLeft);
		func(Fun.MOVE_RIGHT, this::moveRight);
		func(Fun.MOVE_UP, this::moveUp);
		func(Fun.SELECT_ALL, this::selectAll);
		
		key(KB.of(KeyCode.DOWN), Fun.MOVE_DOWN);
		key(KB.of(KeyCode.LEFT), Fun.MOVE_LEFT);
		key(KB.of(KeyCode.RIGHT), Fun.MOVE_RIGHT);
		key(KB.of(KeyCode.UP), Fun.MOVE_UP);
		key(KB.ctrl(KeyCode.A), Fun.SELECT_ALL);
		
		grid.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
		grid.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		grid.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		grid.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
	}
	
	
	private void handleMouseClicked(MouseEvent ev)
	{
		// TODO primary button, 2 clicks: select word, 3 clicks: select paragraph 
	}

	
	private void handleMouseDragged(MouseEvent ev)
	{
		// TODO
	}

	
	private void handleMousePressed(MouseEvent ev)
	{
		control().requestFocus();
		
		if(ev.isPopupTrigger() || (ev.getButton() != MouseButton.PRIMARY))
		{
			return;
		}
		
		TextPos p = getTextPositionFor(ev);
		if(p == null)
		{
			return;
		}
		
		grid.suppressBlinking(true);
		if(ev.isShiftDown())
		{
			control().extendSelection(p);
		}
		else
		{
			control().select(p, p);
		}
	}

	
	private void handleMouseReleased(MouseEvent ev)
	{		
		// TODO
		grid.suppressBlinking(false);
	}
	
	
	protected TextPos getTextPositionFor(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return control().getTextPositionFor(x, y);
	}
	
	
	public void moveDown()
	{
		verticalMove(1, false);
	}
	
	
	public void moveLeft()
	{
		horizontalMove(-1, false);
	}
	
	
	public void moveRight()
	{
		horizontalMove(1, false);
	}
	
	
	public void moveUp()
	{
		verticalMove(-1, false);
	}

	
	public void selectAll()
	{
		CodePad c = control();
        TextPos end = c.getDocumentEnd();
        c.select(TextPos.ZERO, end);
        // TODO clear phantom x
	}
	
	
	private void verticalMove(int dy, boolean select)
	{
		TextPos caret = control().getCaretPosition();
		if(caret != null)
		{
			scrollToVisible();
			// arrangement getwrapinfo
			// grid.verticalMove
			// scroll to visible again
		}
		// TODO
	}
	
	
	// TODO move to  grid?
	private void horizontalMove(int dx, boolean select)
	{
		scrollToVisible();
		// TODO
	}
	
	
	// TODO move to  grid?
	public void scrollToVisible()
	{
		TextPos caret = control().getCaretPosition();
		if(caret == null)
		{
			return;
		}
		
		// TODO
		Arrangement a = grid.arrangement();
		if(a.isVisible(caret))
		{
			return;
		}
		
		// TODO if below - show on the last row
		// if above - show on the first row
	}
}
