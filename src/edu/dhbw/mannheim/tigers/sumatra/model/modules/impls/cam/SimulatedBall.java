/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Ball simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulatedBall implements ISimulatedBall
{
	
	private Vector2		pos		= new Vector2();
	private float			height	= 0;
	private Vector2		vel		= new Vector2();
	
	private final Object	sync		= new Object();
	
	@Configurable()
	private static float	friction	= 0.9f;
	
	
	@Override
	public void step(final float dt)
	{
		synchronized (sync)
		{
			vel.scaleTo(vel.getLength2() - (vel.getLength2() * friction * dt));
			pos.add(vel.multiplyNew(dt * 1000));
			
			if (!AIConfig.getGeometry().getFieldWBorders().isPointInShape(pos))
			{
				vel = new Vector2(0, 0);
				pos = new Vector2(AIConfig.getGeometry().getFieldWReferee().nearestPointInside(pos));
			}
		}
	}
	
	
	/**
	 * @return the pos
	 */
	@Override
	public IVector3 getPos()
	{
		return new Vector3(pos, height);
	}
	
	
	/**
	 * @return the vel
	 */
	@Override
	public IVector3 getVel()
	{
		return new Vector3(vel, 0);
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	@Override
	public void setPos(final IVector3 pos)
	{
		synchronized (sync)
		{
			this.pos = new Vector2(pos.getXYVector());
			height = pos.z();
		}
	}
	
	
	/**
	 * @param vector3
	 */
	@Override
	public void addVel(final IVector3 vector3)
	{
		synchronized (sync)
		{
			vel.add(vector3.getXYVector());
		}
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	@Override
	public final void setVel(final IVector2 vel)
	{
		synchronized (sync)
		{
			this.vel = new Vector2(vel);
		}
	}
	
	
	@Override
	public CamBall getCamBall()
	{
		return new CamBall(1, 0, pos.x, pos.y, 0, 0, 0, SumatraClock.nanoTime(), 0);
	}
}
