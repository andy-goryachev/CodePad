// Copyright © 2016-2025 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.common.log.Log;
import goryachev.common.log.LogLevel;
import goryachev.common.util.ASettingsStore;
import goryachev.common.util.FileSettingsProvider;
import goryachev.common.util.GlobalSettings;
import goryachev.fx.FxFramework;
import goryachev.fx.settings.FxSettingsSchema;
import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * CodePad Tester Application.
 */
public class CodePadTesterApp
	extends Application
{
	public static final String COPYRIGHT = "copyright © 2025 andy goryachev";
	
	
	public static void main(String[] args)
	{
		Log.initConsole(LogLevel.INFO);
		Log.setLevel(LogLevel.DEBUG, "CodePadBehavior");
		Log.setLevel(LogLevel.DEBUG, "CellGrid");
		
		launch(args);
	}


	@Override
	public void init() throws Exception
	{
		File baseDir = new File(System.getProperty("user.home"), ".goryachev.com/CodePadTester");
		File settingsFile = new File(baseDir, "settings.conf");

		FileSettingsProvider p = new FileSettingsProvider(settingsFile);
		GlobalSettings.setProvider(p);
		p.loadQuiet();
	}


	@Override
	public void start(Stage stage) throws Exception
	{
		// generate stylesheet
		FxFramework.setStyleSheet(Styles::new);

		// support multiple windows
		ASettingsStore store = GlobalSettings.instance();
		FxFramework.openLayout(new FxSettingsSchema(store)
		{
			@Override
			public Stage createDefaultWindow()
			{
				return new TesterWindow();
			}

			@Override
			protected Stage createWindow(String name)
			{
				return new TesterWindow();
			}
		});		
	}
}
