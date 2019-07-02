/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.kickoff;

import static edu.tigers.sumatra.math.vector.Vector2f.ZERO_VECTOR;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class KickoffStrategy
{
	private IVector2 bestShotTarget = ZERO_VECTOR;
	
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
