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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;


/**
 * Configuration object for the roles.
 * 
 * Please put parameters that could correspond to more than one role directly
 * into this class and give it a good name
 * 
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class Roles
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// parent node for all inner classes
	private static final String	NODE_PATH	= "roles.";
	
	private ConfigDefenderK1D		defenderK1D;
	private ConfigDefenderK2D		defenderK2D;
	private ConfigKeeperK1D			keeperK1D;
	private ConfigKeeperK2D			keeperK2D;
	private ConfigKeeperSolo		keeperSolo;
	private ConfigIndirectShooter	indirectShooter;
	private ConfigShooter			shooter;
	
	private final float				passSenderBallEndVel;
	private final float				indirectReceiverBallVelCorrection;
	private final float				indirectReceiverMaxAngle;
	private final float				chipPassDistFactor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param configFile
	 */
	public Roles(Configuration configFile)
	{
		defenderK1D = new ConfigDefenderK1D(configFile);
		defenderK2D = new ConfigDefenderK2D(configFile);
		keeperK1D = new ConfigKeeperK1D(configFile);
		keeperK2D = new ConfigKeeperK2D(configFile);
		keeperSolo = new ConfigKeeperSolo(configFile);
		indirectShooter = new ConfigIndirectShooter(configFile);
		shooter = new ConfigShooter(configFile);
		
		passSenderBallEndVel = configFile.getFloat(NODE_PATH + "passSenderBallEndVel");
		indirectReceiverBallVelCorrection = configFile.getFloat(NODE_PATH + "indirectReceiverBallVelCorrection");
		indirectReceiverMaxAngle = AngleMath.deg2rad(configFile.getFloat(NODE_PATH + "indirectReceiverMaxAngle"));
		chipPassDistFactor = configFile.getFloat(NODE_PATH + "chipPassDistFactor");
	}
	
	
	// --------------------------------------------------------------------------
	// --- inner classes --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public static class ConfigDefenderK1D
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "DEFENDER_K1D.";
		private final float				gap;
		private final float				radius;
		
		
		/**
		 * @param configFile
		 */
		public ConfigDefenderK1D(Configuration configFile)
		{
			gap = configFile.getFloat(NODE_PATH_LOC + "GAP");
			radius = configFile.getFloat(NODE_PATH_LOC + "RADIUS");
		}
		
		
		/**
		 * @return gap
		 */
		public float getGap()
		{
			return gap;
		}
		
		
		/**
		 * @return radius
		 */
		public float getRadius()
		{
			return radius;
		}
	}
	
	/**
	 */
	public static class ConfigDefenderK2D
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "DEFENDER_K2D.";
		private final float				spaceBeforeKeeper;
		private final float				spaceBesideKeeper;
		
		
		/**
		 * @param configFile
		 */
		public ConfigDefenderK2D(Configuration configFile)
		{
			spaceBeforeKeeper = configFile.getFloat(NODE_PATH_LOC + "SPACE_BEFORE_KEEPER");
			spaceBesideKeeper = configFile.getFloat(NODE_PATH_LOC + "SPACE_BESIDE_KEEPER");
		}
		
		
		/**
		 * @return spaceBeforeKeeper
		 */
		public float getSpaceBeforeKeeper()
		{
			return spaceBeforeKeeper;
		}
		
		
		/**
		 * @return spaceBesideKeeper
		 */
		public float getSpaceBesideKeeper()
		{
			return spaceBesideKeeper;
		}
	}
	
	/**
	 */
	public static class ConfigKeeperK1D
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "KEEPER_K1D.";
		private final float				gap;
		private final float				radius;
		
		
		/**
		 * @param configFile
		 */
		public ConfigKeeperK1D(Configuration configFile)
		{
			gap = configFile.getFloat(NODE_PATH_LOC + "GAP");
			radius = configFile.getFloat(NODE_PATH_LOC + "RADIUS");
		}
		
		
		/**
		 * @return gap
		 */
		public float getGap()
		{
			return gap;
		}
		
		
		/**
		 * @return radius
		 */
		public float getRadius()
		{
			return radius;
		}
	}
	
	/**
	 */
	public static class ConfigKeeperK2D
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "KEEPER_K2D.";
		private final int					radius;
		
		
		/**
		 * @param configFile
		 */
		public ConfigKeeperK2D(Configuration configFile)
		{
			radius = configFile.getInt(NODE_PATH_LOC + "RADIUS");
		}
		
		
		/**
		 * @return radius
		 */
		public int getRadius()
		{
			return radius;
		}
	}
	
	/**
	 */
	public static class ConfigKeeperSolo
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "KEEPER_SOLO.";
		private final int					radius;
		
		
		/**
		 * @param configFile
		 */
		public ConfigKeeperSolo(Configuration configFile)
		{
			radius = configFile.getInt(NODE_PATH_LOC + "RADIUS");
		}
		
		
		/**
		 * @return radius
		 */
		public int getRadius()
		{
			return radius;
		}
	}
	
	/**
	 */
	public static class ConfigIndirectShooter
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "INDIRECT_SHOOTER.";
		private final int					memorysize;
		private final int					triesPerCycle;
		
		
		/**
		 * @param configFile
		 */
		public ConfigIndirectShooter(Configuration configFile)
		{
			memorysize = configFile.getInt(NODE_PATH_LOC + "MEMORYSIZE");
			triesPerCycle = configFile.getInt(NODE_PATH_LOC + "TRIES_PER_CYCLE");
		}
		
		
		/**
		 * @return memorysize
		 */
		public int getMemorysize()
		{
			return memorysize;
		}
		
		
		/**
		 * @return triesPerCycle
		 */
		public int getTriesPerCycle()
		{
			return triesPerCycle;
		}
	}
	
	/**
	 */
	public static class ConfigShooter
	{
		private static final String	NODE_PATH_LOC	= NODE_PATH + "SHOOTER.";
		private final int					memorysize;
		private final int					triesPerCycle;
		
		
		/**
		 * @param configFile
		 */
		public ConfigShooter(Configuration configFile)
		{
			memorysize = configFile.getInt(NODE_PATH_LOC + "MEMORYSIZE");
			triesPerCycle = configFile.getInt(NODE_PATH_LOC + "TRIES_PER_CYCLE");
		}
		
		
		/**
		 * @return memorysize
		 */
		public int getMemorysize()
		{
			return memorysize;
		}
		
		
		/**
		 * @return triesPerCycle
		 */
		public int getTriesPerCycle()
		{
			return triesPerCycle;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public ConfigDefenderK1D getDefenderK1D()
	{
		return defenderK1D;
	}
	
	
	/**
	 * @return
	 */
	public ConfigDefenderK2D getDefenderK2D()
	{
		return defenderK2D;
	}
	
	
	/**
	 * @return
	 */
	public ConfigKeeperK1D getKeeperK1D()
	{
		return keeperK1D;
	}
	
	
	/**
	 * @return
	 */
	public ConfigKeeperK2D getKeeperK2D()
	{
		return keeperK2D;
	}
	
	
	/**
	 * @return
	 */
	public ConfigKeeperSolo getKeeperSolo()
	{
		return keeperSolo;
	}
	
	
	/**
	 * @return
	 */
	public ConfigIndirectShooter getIndirectShooter()
	{
		return indirectShooter;
	}
	
	
	/**
	 * @return
	 */
	public ConfigShooter getShooter()
	{
		return shooter;
	}
	
	
	/**
	 * @return the passSenderBallEndVel
	 */
	public final float getPassSenderBallEndVel()
	{
		return passSenderBallEndVel;
	}
	
	
	/**
	 * @return the indirectReceiverBallVelCorrection
	 */
	public final float getIndirectReceiverBallVelCorrection()
	{
		return indirectReceiverBallVelCorrection;
	}
	
	
	/**
	 * @return the indirectReceiverMaxAngle
	 */
	public final float getIndirectReceiverMaxAngle()
	{
		return indirectReceiverMaxAngle;
	}
	
	
	/**
	 * @return the chipPassDistFactor
	 */
	public final float getChipPassDistFactor()
	{
		return chipPassDistFactor;
	}
}