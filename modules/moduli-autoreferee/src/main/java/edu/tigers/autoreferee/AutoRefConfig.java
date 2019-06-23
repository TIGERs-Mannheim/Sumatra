/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Static class which contains configuration parameters that can be changed through the Configurable interface
 * 
 * @author "Lukas Magel"
 */
public class AutoRefConfig
{
	@Configurable(comment = "Enable ball placement calls for the blue teams")
	private static boolean ballPlacementBlueEnabled = false;
	
	@Configurable(comment = "Enable ball placement calls for the blue teams")
	private static boolean ballPlacementYellowEnabled = false;
	
	@Configurable(comment = "Enable ball placement calls for the blue teams")
	private static ETeamColor ballPlacementPreference = ETeamColor.NEUTRAL;
	
	@Configurable(comment = "[mm] The accuracy with which the ball needs to be placed")
	private static double ballPlacementAccuracy = 200;
	
	@Configurable(comment = "[mm] The accuracy with which the ball needs to be placed by the robots")
	private static double robotBallPlacementAccuracy = 100;
	
	@Configurable(comment = "[m/s] The maximum allowed ball velocity ingame")
	private static double maxBallVelocity = 8.5d;
	
	@Configurable(comment = "[m/s] The maximum bot velocity during game stoppage")
	private static double maxBotStopSpeed = 1.7d; // in m/s
	
	@Configurable(comment = "[m/s] The velocity below which a bot is considered to be stationary")
	private static double botStationarySpeedThreshold = 0.3;
	
	@Configurable(comment = "[m/s] The velocity below which the ball is considered to be stationary")
	private static double ballStationarySpeedThreshold = 0.08;
	
	@Configurable(comment = "[ms] The time each team has to place the ball")
	private static int ballPlacementWindow = 15_000;
	
	@Configurable(comment = "The hostname/ip address of the refbox")
	private static String refboxHostname = "localhost";
	
	@Configurable(comment = "The port which will be used to connect to the refbox")
	private static int refboxPort = 10007;
	
	static
	{
		ConfigRegistration.registerClass("autoreferee", AutoRefConfig.class);
	}
	
	
	private AutoRefConfig()
	{
		// hide public constructor
	}
	
	
	/**
	 * Init class
	 */
	public static void touch()
	{
		// only for static initialization
	}
	
	
	/**
	 * @return The teams which are capable of performing a ball placement
	 */
	public static List<ETeamColor> getBallPlacementTeams()
	{
		List<ETeamColor> teams = new ArrayList<>();
		if (ballPlacementYellowEnabled)
		{
			teams.add(ETeamColor.YELLOW);
		}
		if (ballPlacementBlueEnabled)
		{
			teams.add(ETeamColor.BLUE);
		}
		
		return teams;
	}
	
	
	/**
	 * @return
	 */
	public static ETeamColor getBallPlacementPreference()
	{
		return ballPlacementPreference;
	}
	
	
	/**
	 * @return
	 */
	public static double getBallPlacementAccuracy()
	{
		return ballPlacementAccuracy;
	}
	
	
	/**
	 * @return
	 */
	public static double getRobotBallPlacementAccuracy()
	{
		return robotBallPlacementAccuracy;
	}
	
	
	/**
	 * @return The maximum allowed ball velocity in m/s
	 */
	public static double getMaxBallVelocity()
	{
		return maxBallVelocity;
	}
	
	
	/**
	 * @return The maximum allowed bot speed during game stoppage in m/s
	 */
	public static double getMaxBotStopSpeed()
	{
		return maxBotStopSpeed;
	}
	
	
	/**
	 * @return The velocity below which a bot is considered to be stationary in m/s
	 */
	public static double getBotStationarySpeedThreshold()
	{
		return botStationarySpeedThreshold;
	}
	
	
	/**
	 * @return The velocity below which the ball is considered to be stationary in m/s
	 */
	public static double getBallStationarySpeedThreshold()
	{
		return ballStationarySpeedThreshold;
	}
	
	
	/**
	 * @return
	 */
	public static long getBallPlacementWindow()
	{
		return ballPlacementWindow;
	}
	
	
	/**
	 * The hostname of the refbox
	 * 
	 * @return a string that represents the hostname/ ip address of the refbox
	 */
	public static String getRefboxHostname()
	{
		return refboxHostname;
	}
	
	
	/**
	 * The port on which the refbox accepts remote control connections
	 * 
	 * @return port number
	 */
	public static int getRefboxPort()
	{
		return refboxPort;
	}
}
