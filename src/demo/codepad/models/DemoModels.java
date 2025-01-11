// Copyright © 2019-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.models;
import goryachev.codepad.model.CodeModel;
import java.util.function.Supplier;


/**
 * Demo Text Models.
 */
public enum DemoModels
{
	AVERAGE("Average Size", () ->
	{
		return TestCodeModel.of
		(
			"""
			0. Line one.
			1. Line two, slightly longer.
			2. Line three, Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
			3. Line four.
			4. The End.
			"""
		);
	}),
	LARGE_1B("1,000,000,000 Lines", () ->
	{
		return new LargeModel(1_000_000_000);
	}),
	LARGE_1M("1,000,000 Lines", () ->
	{
		return new LargeModel(1_000_000);
	}),
	LARGE_1K("1,000 Lines", () ->
	{
		return new LargeModel(1_000);
	}),
	NULL("<null>", () ->
	{
		return null;
	}),
	SHORT("Short", () ->
	{
		return TestCodeModel.of
		(
			"""
			One.
			Two.
			Three.  This line is slightly longer than the other lines.
			"""
		);
	});
	
	
	private final String name;
	private final Supplier<CodeModel> gen;
	
	
	private DemoModels(String name, Supplier<CodeModel> gen)
	{
		this.name = name;
		this.gen = gen;
	}
	
	
	public static CodeModel getModel(Object x)
	{
		DemoModels v = (x instanceof DemoModels ch) ? ch : DemoModels.NULL;
		return v.gen.get();
	}
}
