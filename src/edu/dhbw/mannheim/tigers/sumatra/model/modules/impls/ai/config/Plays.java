/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.06.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;


/**
 * Configuration object for the plays.
 * 
 * @author FlorianS
 * 
 */
public class Plays
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// parent node for all inner classes
	protected final static String					parentNodePath	= "plays.";
	
	ConfigGameOffensePrepareWithThree			gameOffensePrepareWithThree;
	ConfigGameOffensePrepareWithTwo				gameOffensePrepareWithTwo;
	ConfigPassToKeeper								passToKeeper;
	ConfigPositionImprovingNoBallWithOne		positionImprovingNoBallWithOne;
	ConfigPositionImprovingNoBallWithTwo		positionImprovingNoBallWithTwo;
	ConfigAroundTheBall								aroundTheBall;
	ConfigPositioningOnKickOffThem				positioningOnKickOffThem;
	ConfigPositioningOnStoppedPlayWithTwo		positioningOnStoppedPlayWithTwo;
	ConfigPositioningOnStoppedPlayWithThree	positioningOnStoppedPlayWithThree;
	ConfigDirectShot									directShot;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	public Plays(XMLConfiguration configFile)
	{
		gameOffensePrepareWithThree = new ConfigGameOffensePrepareWithThree(configFile);
		gameOffensePrepareWithTwo = new ConfigGameOffensePrepareWithTwo(configFile);
		passToKeeper = new ConfigPassToKeeper(configFile);
		positionImprovingNoBallWithOne = new ConfigPositionImprovingNoBallWithOne(configFile);
		positionImprovingNoBallWithTwo = new ConfigPositionImprovingNoBallWithTwo(configFile);
		aroundTheBall = new ConfigAroundTheBall(configFile);
		positioningOnKickOffThem = new ConfigPositioningOnKickOffThem(configFile);
		positioningOnStoppedPlayWithTwo = new ConfigPositioningOnStoppedPlayWithTwo(configFile);
		positioningOnStoppedPlayWithThree = new ConfigPositioningOnStoppedPlayWithThree(configFile);
		directShot = new ConfigDirectShot(configFile);
	}
	
	// --------------------------------------------------------------------------
	// --- inner classes --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public static class ConfigGameOffensePrepareWithTwo
	{
		private final String	nodePath	= parentNodePath + "GAME_OFFENSE_PREPARE_WITH_TWO.";
		
		private final float	improvement_value;
		
		
		public ConfigGameOffensePrepareWithTwo(XMLConfiguration configFile)
		{
			improvement_value = configFile.getFloat(nodePath + "IMPROVEMENT_VALUE");
		}
		
		
		/**
		 * @return improvement_value
		 */
		public float getImprovementValue()
		{
			return improvement_value;
		}
	}
	
	public static class ConfigGameOffensePrepareWithThree
	{
		private final String	nodePath	= parentNodePath + "GAME_OFFENSE_PREPARE_WITH_THREE.";
		
		private final float	improvement_value;
		
		
		public ConfigGameOffensePrepareWithThree(XMLConfiguration configFile)
		{
			improvement_value = configFile.getFloat(nodePath + "IMPROVEMENT_VALUE");
		}
		
		
		/**
		 * @return improvement_value
		 */
		public float getImprovementValue()
		{
			return improvement_value;
		}
	}
	
	public static class ConfigPassToKeeper
	{
		private final String	nodePath	= parentNodePath + "PASS_TO_KEEPER.";
		
		private final float	finished_ballspeed;
		
		
		public ConfigPassToKeeper(XMLConfiguration configFile)
		{
			finished_ballspeed = configFile.getFloat(nodePath + "FINISHED_BALLSPEED");
		}
		
		
		/**
		 * @return finished_ballspeed
		 */
		public float getFinishedBallspeed()
		{
			return finished_ballspeed;
		}
	}
	
	public static class ConfigPositionImprovingNoBallWithOne
	{
		private final String	nodePath	= parentNodePath + "POSITION_IMPROVING_NO_BALL_WITH_ONE.";
		
		private final float	improvement_value;
		
		
		public ConfigPositionImprovingNoBallWithOne(XMLConfiguration configFile)
		{
			improvement_value = configFile.getFloat(nodePath + "IMPROVEMENT_VALUE");
		}
		
		
		/**
		 * @return improvement_value
		 */
		public float getImprovementValue()
		{
			return improvement_value;
		}
	}
	
	public static class ConfigPositionImprovingNoBallWithTwo
	{
		private final String	nodePath	= parentNodePath + "POSITION_IMPROVING_NO_BALL_WITH_TWO.";
		
		private final float	improvement_value;
		
		
		public ConfigPositionImprovingNoBallWithTwo(XMLConfiguration configFile)
		{
			improvement_value = configFile.getFloat(nodePath + "IMPROVEMENT_VALUE");
		}
		
		
		/**
		 * @return improvement_value
		 */
		public float getImprovementValue()
		{
			return improvement_value;
		}
	}
	
	public static class ConfigAroundTheBall
	{
		private final String	nodePath	= parentNodePath + "AROUND_THE_BALL.";
		
		private final float	radius;
		
		
		public ConfigAroundTheBall(XMLConfiguration configFile)
		{
			radius = configFile.getFloat(nodePath + "RADIUS");
		}
		
		
		/**
		 * @return radius
		 */
		public float getRadius()
		{
			return radius;
		}
	}
	
	public static class ConfigPositioningOnKickOffThem
	{
		private final String	nodePath	= parentNodePath + "POSITIONING_ON_KICK_OFF_THEM.";
		
		private final float	maximum_length;
		
		
		public ConfigPositioningOnKickOffThem(XMLConfiguration configFile)
		{
			maximum_length = configFile.getFloat(nodePath + "MAXIMUM_LENGTH");
		}
		
		
		/**
		 * @return maximum_length
		 */
		public float getMaximumLength()
		{
			return maximum_length;
		}
	}
	
	public static class ConfigPositioningOnStoppedPlayWithTwo
	{
		private final String	nodePath	= parentNodePath + "POSITIONING_ON_STOPPED_PLAY_WITH_TWO.";
		
		private final float	space_between_bots;
		
		
		public ConfigPositioningOnStoppedPlayWithTwo(XMLConfiguration configFile)
		{
			space_between_bots = configFile.getFloat(nodePath + "SPACE_BETWEEN_BOTS");
		}
		
		
		/**
		 * @return space_between_bots
		 */
		public float getSpaceBetweenBots()
		{
			return space_between_bots;
		}
	}
	
	public static class ConfigPositioningOnStoppedPlayWithThree
	{
		private final String	nodePath	= parentNodePath + "POSITIONING_ON_STOPPED_PLAY_WITH_THREE.";
		
		private final float	space_between_bots;
		
		
		public ConfigPositioningOnStoppedPlayWithThree(XMLConfiguration configFile)
		{
			space_between_bots = configFile.getFloat(nodePath + "SPACE_BETWEEN_BOTS");
		}
		
		
		/**
		 * @return space_between_bots
		 */
		public float getSpaceBetweenBots()
		{
			return space_between_bots;
		}
	}
	
	public static class ConfigDirectShot
	{
		private final String	nodePath	= parentNodePath + "DIRECT_SHOT.";
		
		private final float	range_tolerance;
		
		
		public ConfigDirectShot(XMLConfiguration configFile)
		{
			range_tolerance = configFile.getFloat(nodePath + "RANGE_TOLERANCE");
		}
		
		
		/**
		 * @return range_tolerance
		 */
		public float getRangeTolerance()
		{
			return range_tolerance;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ConfigGameOffensePrepareWithTwo getGameOffensePrepareWithTwo()
	{
		return gameOffensePrepareWithTwo;
	}
	
	
	public ConfigGameOffensePrepareWithThree getGameOffensePrepareWithThree()
	{
		return gameOffensePrepareWithThree;
	}
	
	
	public ConfigPassToKeeper getPassToKeeper()
	{
		return passToKeeper;
	}
	
	
	public ConfigPositionImprovingNoBallWithOne getPositionImprovingNoBallWithOne()
	{
		return positionImprovingNoBallWithOne;
	}
	
	
	public ConfigPositionImprovingNoBallWithTwo getPositionImprovingNoBallWithTwo()
	{
		return positionImprovingNoBallWithTwo;
	}
	
	
	public ConfigAroundTheBall getAroundTheBall()
	{
		return aroundTheBall;
	}
	
	
	public ConfigPositioningOnKickOffThem getPositioningOnKickOffThem()
	{
		return positioningOnKickOffThem;
	}
	
	
	public ConfigPositioningOnStoppedPlayWithTwo getPositioningOnStoppedPlayWithTwo()
	{
		return positioningOnStoppedPlayWithTwo;
	}
	
	
	public ConfigPositioningOnStoppedPlayWithThree getPositioningOnStoppedPlayWithThree()
	{
		return positioningOnStoppedPlayWithThree;
	}
	
	
	public ConfigDirectShot getDirectShot()
	{
		return directShot;
	}
}