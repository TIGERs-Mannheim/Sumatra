/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * data holder for the prediction information of a field obstacle (ball or bot)
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Embeddable
public class FieldPredictionInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final IVector2	pos;
	private final IVector2	vel;
	@SuppressWarnings("unused")
	private final IVector2	acc;
	
	/* ms, initialized with int may */
	private final float		firstCrashAfterTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param firstCrashAfterTime
	 */
	public FieldPredictionInformation(IVector2 pos, IVector2 vel, IVector2 acc, float firstCrashAfterTime)
	{
		super();
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
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
	
	
}
