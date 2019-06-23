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

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;


/**
 * Configuration object for the plays.
 * 
 * Please put parameters that could correspond to more than one role directly
 * into this class and give it a good name
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class Plays
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger		log			= Logger.getLogger(Plays.class.getName());
	
	// parent node for all inner classes
	private static final String		NODE_PATH	= "plays.";
	
	private ConfigTecShootChalPlay	tecShootCalPlay;
	private ConfigNDefenderPlay		nDefenderPlay;
	
	private ConfigKeeperSoloPlay		keeperSoloPlay;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param configFile
	 */
	public Plays(Configuration configFile)
	{
		tecShootCalPlay = new ConfigTecShootChalPlay(configFile);
		nDefenderPlay = new ConfigNDefenderPlay(configFile);
		keeperSoloPlay = new ConfigKeeperSoloPlay(configFile);
	}
	
	// --------------------------------------------------------------------------
	// --- inner classes --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public static class ConfigTecShootChalPlay
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "TEC_SHOOT_CHAL_TEST.";
		private final ETeam				goalOfTeam;
		private final float				speed;
		
		
		/**
		 * @param configFile
		 */
		public ConfigTecShootChalPlay(Configuration configFile)
		{
			String strGoalOfTeam = configFile.getString(NODE_PATH_LOC + "teamside");
			ETeam tmpGoalOfTeam = ETeam.TIGERS;
			try
			{
				tmpGoalOfTeam = ETeam.valueOf(strGoalOfTeam);
			} catch (IllegalArgumentException err)
			{
				log.warn("Could not read teamside: " + strGoalOfTeam);
			}
			goalOfTeam = tmpGoalOfTeam;
			speed = configFile.getFloat(NODE_PATH_LOC + "speed");
		}
		
		
		/**
		 * @return gap
		 */
		public ETeam getGoalOfTeam()
		{
			return goalOfTeam;
		}
		
		
		/**
		 * @return
		 */
		public float getSpeed()
		{
			return speed;
		}
	}
	
	
	/**
	 * Configuration from the NDefnederPlay
	 * 
	 * @author PhilippP {ph.posovszky@gmail.com
	 * 
	 */
	public static class ConfigNDefenderPlay
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "NDEFENDER.";
		private final float				blockRadius;
		private final float				allowedBallSpeed;
		private final boolean			allowedBlockModus;
		
		
		/**
		 * @param configFile
		 */
		public ConfigNDefenderPlay(Configuration configFile)
		{
			allowedBallSpeed = configFile.getFloat(NODE_PATH_LOC + "ballSpeedToProtect");
			allowedBlockModus = configFile.getBoolean(NODE_PATH_LOC + "blockmodus");
			blockRadius = configFile.getFloat(NODE_PATH_LOC + "radiusToProtect");
		}
		
		
		/**
		 * @return the allowedBallSpeed
		 */
		public float getAllowedBallSpeed()
		{
			return allowedBallSpeed;
		}
		
		
		/**
		 * @return the blockRadius
		 */
		public float getBlockRadius()
		{
			return blockRadius;
		}
		
		
		/**
		 * @return the allowedBlockModus
		 */
		public boolean isAllowedBlockModus()
		{
			return allowedBlockModus;
		}
	}
	
	/**
	 * Configuration from the NDefnederPlay
	 * 
	 * @author PhilippP {ph.posovszky@gmail.com
	 * 
	 */
	public static class ConfigKeeperSoloPlay
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "KEEPERSOLO.";
		private final float				initx;
		private final float				inity;
		
		
		/**
		 * @param configFile
		 */
		public ConfigKeeperSoloPlay(Configuration configFile)
		{
			initx = configFile.getFloat(NODE_PATH_LOC + "vector.initx");
			inity = configFile.getFloat(NODE_PATH_LOC + "vector.inity");
		}
		
		
		/**
		 * @return the init YValue
		 */
		public float getInitX()
		{
			return initx;
		}
		
		
		/**
		 * @return the init YValue
		 */
		public float getInitY()
		{
			return inity;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public ConfigTecShootChalPlay getTecShootCalPlay()
	{
		return tecShootCalPlay;
	}
	
	
	/**
	 * @return the nDefenderPlay
	 */
	public ConfigNDefenderPlay getnDefenderPlay()
	{
		return nDefenderPlay;
	}
	
	
	/**
	 * TODO Philipp, add comment!
	 * 
	 * @return
	 */
	public ConfigKeeperSoloPlay getKeeperSoloPlay()
	{
		return keeperSoloPlay;
	}
	
	
}