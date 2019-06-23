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
 * Configuration object for the roles.
 * 
 * @author FlorianS
 * 
 */
public class Roles
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// parent node for all inner classes
	protected final static String	parentNodePath	= "roles.";
	
	ConfigDefenderK1D					defenderK1D;
	ConfigDefenderK2D					defenderK2D;
	ConfigKeeperK1D					keeperK1D;
	ConfigKeeperK2D					keeperK2D;
	ConfigKeeperSolo					keeperSolo;
	ConfigBallGetter					ballGetter;
	ConfigIndirectShooter			indirectShooter;
	ConfigShooter						shooter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public Roles(XMLConfiguration configFile)
	{
		defenderK1D = new ConfigDefenderK1D(configFile);
		defenderK2D = new ConfigDefenderK2D(configFile);
		keeperK1D = new ConfigKeeperK1D(configFile);
		keeperK2D = new ConfigKeeperK2D(configFile);
		ballGetter = new ConfigBallGetter(configFile);
		keeperSolo = new ConfigKeeperSolo(configFile);
		indirectShooter = new ConfigIndirectShooter(configFile);
		shooter = new ConfigShooter(configFile);
	}
	
	
	// --------------------------------------------------------------------------
	// --- inner classes --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public static class ConfigDefenderK1D
	{
		private final String	nodePath	= parentNodePath + "DEFENDER_K1D.";
		private final float	gap;
		private final float	radius;
		
		
		public ConfigDefenderK1D(XMLConfiguration configFile)
		{
			gap = configFile.getFloat(nodePath + "GAP");
			radius = configFile.getFloat(nodePath + "RADIUS");
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
	
	
	public static class ConfigDefenderK2D
	{
		private final String	nodePath	= parentNodePath + "DEFENDER_K2D.";
		private final float	space_before_keeper;
		private final float	space_beside_keeper;
		
		
		public ConfigDefenderK2D(XMLConfiguration configFile)
		{
			space_before_keeper = configFile.getFloat(nodePath + "SPACE_BEFORE_KEEPER");
			space_beside_keeper = configFile.getFloat(nodePath + "SPACE_BESIDE_KEEPER");
		}
		

		/**
		 * @return space_before_keeper
		 */
		public float getSpaceBeforeKeeper()
		{
			return space_before_keeper;
		}
		

		/**
		 * @return space_beside_keeper
		 */
		public float getSpaceBesideKeeper()
		{
			return space_beside_keeper;
		}
	}
	
	
	public static class ConfigKeeperK1D
	{
		private final String	nodePath	= parentNodePath + "KEEPER_K1D.";
		private final float	gap;
		private final float	radius;
		
		
		public ConfigKeeperK1D(XMLConfiguration configFile)
		{
			gap = configFile.getFloat(nodePath + "GAP");
			radius = configFile.getFloat(nodePath + "RADIUS");
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
	
	
	public static class ConfigKeeperK2D
	{
		private final String	nodePath	= parentNodePath + "KEEPER_K2D.";
		private final int		radius;
		
		
		public ConfigKeeperK2D(XMLConfiguration configFile)
		{
			radius = configFile.getInt(nodePath + "RADIUS");
		}
		

		/**
		 * @return radius
		 */
		public int getRadius()
		{
			return radius;
		}
	}
	
	
	public static class ConfigKeeperSolo
	{
		private final String	nodePath	= parentNodePath + "KEEPER_SOLO.";
		private final int		radius;
		
		
		public ConfigKeeperSolo(XMLConfiguration configFile)
		{
			radius = configFile.getInt(nodePath + "RADIUS");
		}
		

		/**
		 * @return radius
		 */
		public int getRadius()
		{
			return radius;
		}
	}
	
	
	public static class ConfigBallGetter
	{
		private final String	nodePath	= parentNodePath + "BALL_GETTER.";
		private final float	space_min;
		private final float	space_max;
		private final float	velocity_tolerance;
		private final float	dribbling_distance;
		private final float 	positioning_pre_aiming;
		
		
		public ConfigBallGetter(XMLConfiguration configFile)
		{
			space_min = configFile.getFloat(nodePath + "SPACE_MIN");
			space_max = configFile.getFloat(nodePath + "SPACE_MAX");
			velocity_tolerance = configFile.getFloat(nodePath + "VELOCITY_TOLERANCE");
			dribbling_distance = configFile.getFloat(nodePath + "DRIBBLING_DISTANCE");
			positioning_pre_aiming = configFile.getFloat(nodePath + "POSITIONING_PRE_AIMING");
		}
		

		/**
		 * @return space_min
		 */
		public float getSpaceMin()
		{
			return space_min;
		}
		

		/**
		 * @return space_max
		 */
		public float getSpaceMax()
		{
			return space_max;
		}
		

		/**
		 * @return velocity_tolerance
		 */
		public float getVelocityTolerance()
		{
			return velocity_tolerance;
		}
		

		/**
		 * @return dribbling_distance
		 */
		public float getDribblingDistance()
		{
			return dribbling_distance;
		}
		
		
		/**
		 * @return the positioning_pre_aiming
		 */
		public float getPositioningPreAiming()
		{
			return positioning_pre_aiming;
		}
	}
	
	
	public static class ConfigIndirectShooter
	{
		private final String	nodePath	= parentNodePath + "INDIRECT_SHOOTER.";
		private final int		memorysize;
		private final int		tries_per_cycle;
		
		
		public ConfigIndirectShooter(XMLConfiguration configFile)
		{
			memorysize = configFile.getInt(nodePath + "MEMORYSIZE");
			tries_per_cycle = configFile.getInt(nodePath + "TRIES_PER_CYCLE");
		}
		

		/**
		 * @return memorysize
		 */
		public int getMemorysize()
		{
			return memorysize;
		}
		

		/**
		 * @return tries_per_cycle
		 */
		public int getTriesPerCycle()
		{
			return tries_per_cycle;
		}
	}
	
	
	public static class ConfigShooter
	{
		private final String	nodePath	= parentNodePath + "SHOOTER.";
		private final int		memorysize;
		private final int		tries_per_cycle;
		
		
		public ConfigShooter(XMLConfiguration configFile)
		{
			memorysize = configFile.getInt(nodePath + "MEMORYSIZE");
			tries_per_cycle = configFile.getInt(nodePath + "TRIES_PER_CYCLE");
		}
		

		/**
		 * @return memorysize
		 */
		public int getMemorysize()
		{
			return memorysize;
		}
		

		/**
		 * @return tries_per_cycle
		 */
		public int getTriesPerCycle()
		{
			return tries_per_cycle;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ConfigDefenderK1D getDefenderK1D()
	{
		return defenderK1D;
	}
	

	public ConfigDefenderK2D getDefenderK2D()
	{
		return defenderK2D;
	}
	

	public ConfigKeeperK1D getKeeperK1D()
	{
		return keeperK1D;
	}
	

	public ConfigKeeperK2D getKeeperK2D()
	{
		return keeperK2D;
	}
	

	public ConfigKeeperSolo getKeeperSolo()
	{
		return keeperSolo;
	}
	

	public ConfigBallGetter getBallGetter()
	{
		return ballGetter;
	}
	

	public ConfigIndirectShooter getIndirectShooter()
	{
		return indirectShooter;
	}
	

	public ConfigShooter getShooter()
	{
		return shooter;
	}
}