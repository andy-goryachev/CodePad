// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.CodePad.Fun;
import goryachev.codepad.TextPos;
import goryachev.fx.input.BehaviorBase;
import goryachev.fx.input.KB;
import javafx.scene.input.KeyCode;


/**
 * CodePad Behavior.
 */
public class CodePadBehavior
	extends BehaviorBase<CodePad>
{
	public CodePadBehavior(CodePad c)
	{
		super(c);
	}


	@Override
	protected void populateSkinInputMap()
	{
		// TODO
		func(Fun.SELECT_ALL, this::selectAll);
		
		key(KB.ctrl(KeyCode.A), Fun.SELECT_ALL);
	}
	
	
	public void selectAll()
	{
		CodePad c = control();
        TextPos end = c.getDocumentEnd();
        c.select(TextPos.ZERO, end);
        // TODO clear phantom x
	}
}
