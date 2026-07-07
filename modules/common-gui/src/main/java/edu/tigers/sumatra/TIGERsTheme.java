package edu.tigers.sumatra;

import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.util.LoggingFacade;
import edu.tigers.sumatra.model.SumatraModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TIGERsTheme extends IntelliJTheme.ThemeLaf
{
	public static final String NAME = "TIGERs";


	public TIGERsTheme()
	{
		super(loadTheme());
	}


	private static IntelliJTheme loadTheme()
	{
		var path = Paths.get(SumatraModel.getInstance().getConfigPath().toAbsolutePath().toString(), "TIGERs.theme.json");
		try
		{
			return new IntelliJTheme(Files.newInputStream(path));
		} catch (IOException ex)
		{
			String msg = "FlatLaf: Failed to load IntelliJ theme '" + NAME + "'";
			LoggingFacade.INSTANCE.logSevere(msg, ex);
			throw new RuntimeException(msg, ex);
		}
	}


	public static void installLafInfo()
	{
		installLafInfo(NAME, TIGERsTheme.class);
	}
}
