/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;


/**
 * Implementation of the traj-based path finder
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderV4 extends ATrajPathFinder
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(TrajPathFinderV4.class.getName());
	
	private IVector2					lastSubDest	= null;
	
	
	private PathCollision findPath(
			final TrajPathFinderInput input,
			PathCollision bestPathCollision)
	{
		PathCollision newPathCollision = bestPathCollision;
		IVector2 dir = getDirection(input);
		double angleStep = 0.4;
		double angleMax = AngleMath.PI;
		double angle = 0;
		int timeoutSteps = 10;
		int timeoutCtr = -1;
		do
		{
			angle *= -1;
			if (angle >= 0)
			{
				angle += angleStep;
			}
			
			for (double len = 100; len <= 3100; len += 1000)
			{
				IVector2 subDest = input.getPos().addNew(dir.turnNew(angle).scaleTo(len));
				
				newPathCollision = getDestPath(input, subDest, newPathCollision);
				if ((timeoutCtr < 0) && newPathCollision.hasIntermediateCollision())
				{
					timeoutCtr = timeoutSteps;
				}
			}
			timeoutCtr--;
		} while ((Math.abs(angle) < angleMax) && (timeoutCtr != 0));
		
		return newPathCollision;
	}
	
	
	@Override
	protected PathCollision generatePath(final TrajPathFinderInput input)
	{
		PathCollision bestPathCollision = getPath(input, input.getDest());
		if (bestPathCollision.isOptimal())
		{
			return bestPathCollision;
		}
		
		PathCollision destPathCollision = getDestPath(input, lastSubDest, bestPathCollision);
		return findPath(input, destPathCollision);
	}
	
	
	private PathCollision getDestPath(final TrajPathFinderInput input,
			final IVector2 subDest, PathCollision bestPathCollision)
	{
		if (subDest == null)
		{
			return bestPathCollision;
		}
		PathCollision resultingBestPathCollision = bestPathCollision;
		PathCollision subPathCollision = getPath(input, subDest);
		PathCollision pc = subPathCollision;
		for (double t = 0.2; t <= subPathCollision.getLastValidTime(); t += 0.2)
		{
			TrajPathV2 path = gen.append(subPathCollision.getTrajPath(), input.getMoveConstraints(), t,
					input.getDest());
			pc = getCollision(input, path, pc, t);
			if ((resultingBestPathCollision == null) || pc.isBetterThan(resultingBestPathCollision))
			{
				lastSubDest = subDest;
				resultingBestPathCollision = pc;
			}
		}
		return resultingBestPathCollision;
	}
}
