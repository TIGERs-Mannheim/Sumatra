/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 8, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DummySpline implements ISpline
{
	private IVector3	initPos;
	
	
	@SuppressWarnings("unused")
	private DummySpline()
	{
		initPos = AVector3.ZERO_VECTOR;
	}
	
	
	/**
	 * @param pos
	 * @param orientation
	 */
	public DummySpline(final IVector2 pos, final float orientation)
	{
		initPos = new Vector3(pos, orientation);
	}
	
	
	@Override
	public IVector3 getPositionByTime(final float t)
	{
		return initPos;
	}
	
	
	@Override
	public IVector3 getVelocityByTime(final float t)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public IVector3 getAccelerationByTime(final float t)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public float getTotalTime()
	{
		return 0;
	}
	
	
	@Override
	public float getCurrentTime()
	{
		return 0;
	}
}
