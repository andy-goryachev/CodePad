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
		func(Fun.SELECT_ALL, this::selectAll);
		
		key(KB.ctrl(KeyCode.A), Fun.SELECT_ALL);
		
		grid.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
		grid.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		grid.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		grid.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
	}
	
	
	private void handleMouseClicked(MouseEvent ev)
	{
		// TODO
	}

	
	private void handleMouseDragged(MouseEvent ev)
	{
		// TODO
	}

	
	private void handleMousePressed(MouseEvent ev)
	{
		// TODO
	}

	
	private void handleMouseReleased(MouseEvent ev)
	{
		if(ev.isPopupTrigger() || (ev.getButton() != MouseButton.PRIMARY))
		{
			return;
		}
		
		TextPos p = getTextPositionFor(ev);
		if(p == null)
		{
			return;
		}
		
		// TODO selection, suppress blink
		
		control().requestFocus();
	}
	
	
	protected TextPos getTextPositionFor(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return control().getTextPositionFor(x, y);
	}

	
	public void selectAll()
	{
		CodePad c = control();
        TextPos end = c.getDocumentEnd();
        c.select(TextPos.ZERO, end);
        // TODO clear phantom x
	}
}
