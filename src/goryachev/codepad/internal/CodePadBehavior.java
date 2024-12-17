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
	private final CellGrid grid;
	
	
	public CodePadBehavior(CodePad c, CellGrid g)
	{
		super(c);
		this.grid = g;
	}


	@Override
	protected void populateSkinInputMap()
	{
		func(Fun.MOVE_DOWN, this::moveDown);
		func(Fun.MOVE_LEFT, this::moveLeft);
		func(Fun.MOVE_RIGHT, this::moveRight);
		func(Fun.MOVE_UP, this::moveUp);
		func(Fun.PAGE_DOWN, this::pageDown);
		func(Fun.PAGE_UP, this::pageUp);
		func(Fun.SELECT_DOWN, this::selectDown);
		func(Fun.SELECT_LEFT, this::selectLeft);
		func(Fun.SELECT_PAGE_DOWN, this::selectPageDown);
		func(Fun.SELECT_PAGE_UP, this::selectPageUp);
		func(Fun.SELECT_RIGHT, this::selectRight);
		func(Fun.SELECT_UP, this::selectUp);
		func(Fun.SELECT_ALL, this::selectAll);
		
		key(KB.of(KeyCode.DOWN), Fun.MOVE_DOWN);
		key(KB.shift(KeyCode.DOWN), Fun.SELECT_DOWN);
		key(KB.of(KeyCode.LEFT), Fun.MOVE_LEFT);
		key(KB.shift(KeyCode.LEFT), Fun.SELECT_LEFT);
		key(KB.of(KeyCode.PAGE_DOWN), Fun.PAGE_DOWN);
		key(KB.shift(KeyCode.PAGE_DOWN), Fun.SELECT_PAGE_DOWN);
		key(KB.of(KeyCode.PAGE_UP), Fun.PAGE_UP);
		key(KB.shift(KeyCode.PAGE_UP), Fun.SELECT_PAGE_UP);
		key(KB.of(KeyCode.RIGHT), Fun.MOVE_RIGHT);
		key(KB.shift(KeyCode.RIGHT), Fun.SELECT_RIGHT);
		key(KB.of(KeyCode.UP), Fun.MOVE_UP);
		key(KB.shift(KeyCode.UP), Fun.SELECT_UP);

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
		grid.clearPhantomX();
	}
	
	
	protected TextPos getTextPositionFor(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return control().getTextPositionFor(x, y);
	}
	
	
	public void moveDown()
	{
		move(true, 1, false);
	}
	
	
	public void moveLeft()
	{
		move(false, -1, false);
	}
	
	
	public void moveRight()
	{
		move(false, 1, false);
	}
	
	
	public void moveUp()
	{
		move(true, -1, false);
	}
	
	
	public void pageDown()
	{
		move(true, grid.getPageSize(), false);
	}
	
	
	public void pageUp()
	{
		move(true, -grid.getPageSize(), false);
	}
	
	
	public void selectDown()
	{
		move(true, 1, true);
	}
	
	
	public void selectLeft()
	{
		move(false, -1, true);
	}
	
	
	public void selectPageDown()
	{
		move(true, grid.getPageSize(), true);
	}
	
	
	public void selectPageUp()
	{
		move(true, -grid.getPageSize(), true);
	}
	
	
	public void selectRight()
	{
		move(false, 1, true);
	}
	
	
	public void selectUp()
	{
		move(true, -1, true);
	}

	
	private void move(boolean vertical, int delta, boolean select)
	{
		TextPos caret = control().getCaretPosition();
		if(caret != null)
		{
			TextPos p;
			if(vertical)
			{
				p = grid.verticalMove(caret, delta);
			}
			else
			{
				p = grid.horizontalMove(caret, delta);
			}
			moveCaret(p, select);
		}
	}
	
	
	// combine with previous method?
	private void moveCaret(TextPos p, boolean extendSelection)
	{
		if(extendSelection)
		{
			control().extendSelection(p);
		}
		else
		{
			control().select(p);
		}
	}
	
	
	public void selectAll()
	{
        TextPos end = control().getDocumentEnd();
        control().select(TextPos.ZERO, end);
	}
}
