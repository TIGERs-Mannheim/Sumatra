/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * data holder for the prediction information of a field obstacle (ball or bot)
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Persistent(version = 3)
public class FieldPredictionInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final IVector2	pos;
	private final IVector2	vel;
	
	/* ms, initialized with int may */
	private final float		firstCrashAfterTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private FieldPredictionInformation()
	{
		pos = new Vector2(Vector2.ZERO_VECTOR);
		vel = new Vector2(Vector2.ZERO_VECTOR);
		firstCrashAfterTime = 0;
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param firstCrashAfterTime
	 */
	public FieldPredictionInformation(IVector2 pos, IVector2 vel, float firstCrashAfterTime)
	{
		super();
		this.pos = new Vector2(pos);
		this.vel = new Vector2(vel);
		this.firstCrashAfterTime = firstCrashAfterTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * gets the theoretical position of a bot after a given time
	 * 
	 * @param time [s]
	 * @return
	 */
	public IVector2 getPosAt(float time)
	{
		if (time > firstCrashAfterTime)
		{
			time = firstCrashAfterTime;
		}
		// TODO DirkK ball should not stop but change direction
		IVector2 velocityLength = vel.multiplyNew(time);
		// if (time > 1)
		// {
		// velocityLength.scaleToNew(1 / time);
		// }
		return pos.addNew(velocityLength);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the firstCrashAfterTime
	 */
	public float getFirstCrashAfterTime()
	{
		return firstCrashAfterTime;
	}
	
	
	/**
	 * @return
	 */
	public FieldPredictionInformation mirror()
	{
		FieldPredictionInformation fpi = new FieldPredictionInformation(pos.multiplyNew(-1), vel.multiplyNew(-1),
				firstCrashAfterTime);
		return fpi;
	}
}
