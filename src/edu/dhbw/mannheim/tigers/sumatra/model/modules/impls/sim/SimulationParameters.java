/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationParameters
{
	private final Map<BotID, SimulationObject>	initBots		= new HashMap<>();
	private SimulationObject							initBall		= new SimulationObject();
	
	private float											speedFactor	= 1;
	
	
	/**
	 * @return the initBall
	 */
	public final SimulationObject getInitBall()
	{
		return initBall;
	}
	
	
	/**
	 * @param initBall the initBall to set
	 */
	public final void setInitBall(final SimulationObject initBall)
	{
		this.initBall = initBall;
	}
	
	
	/**
	 * @return the initBots
	 */
	public final Map<BotID, SimulationObject> getInitBots()
	{
		return initBots;
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class SimulationObject
	{
		private IVector3	pos	= new Vector3();
		private IVector3	vel	= new Vector3();
		
		
		/**
		 * 
		 */
		public SimulationObject()
		{
		}
		
		
		/**
		 * @param pos
		 * @param vel
		 */
		public SimulationObject(final IVector3 pos, final IVector3 vel)
		{
			this.pos = pos;
			this.vel = vel;
		}
		
		
		/**
		 * @param pos
		 */
		public SimulationObject(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the pos
		 */
		public final IVector3 getPos()
		{
			return pos;
		}
		
		
		/**
		 * @param pos the pos to set
		 */
		public final void setPos(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the vel
		 */
		public final IVector3 getVel()
		{
			return vel;
		}
		
		
		/**
		 * @param vel the vel to set
		 */
		public final void setVel(final IVector3 vel)
		{
			this.vel = vel;
		}
	}
	
	
	/**
	 * @return the speedFactor
	 */
	public final float getSpeedFactor()
	{
		return speedFactor;
	}
	
	
	/**
	 * @param speedFactor the speedFactor to set
	 */
	public final void setSpeedFactor(final float speedFactor)
	{
		this.speedFactor = speedFactor;
	}
}
