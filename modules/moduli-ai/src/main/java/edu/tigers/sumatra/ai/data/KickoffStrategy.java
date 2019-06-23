/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 2, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class KickoffStrategy
{
	private IVector2	bestShotTarget	= Vector2.zero();
	
	private Map<BotID, IPassTarget> bestMovementPositions = new HashMap<>();

	private double passVelocity = 0.0f;
	
	
	/**
	 * @return the bestShotTarget
	 */
	public IVector2 getBestShotTarget()
	{
		return bestShotTarget;
	}
	
	
	/**
	 * @param bestShotTarget the bestShotTarget to set
	 */
	public void setBestShotTarget(final IVector2 bestShotTarget)
	{
		this.bestShotTarget = bestShotTarget;
	}

	/**
	 * @return The velocity the ball should have at the end of the pass
     */
	public double getPassVelocity() {
		return passVelocity;
	}

	/**
	 * @param passVelocity
     */
	public void setPassVelocity(final double passVelocity) {
		this.passVelocity = passVelocity;
	}
	
	
	public Map<BotID, IPassTarget> getBestMovementPositions()
	{
		return bestMovementPositions;
	}
	
	
	public void setBestMovementPositions(final Map<BotID, IPassTarget> bestMovementPositions)
	{
		this.bestMovementPositions = bestMovementPositions;
	}
}
