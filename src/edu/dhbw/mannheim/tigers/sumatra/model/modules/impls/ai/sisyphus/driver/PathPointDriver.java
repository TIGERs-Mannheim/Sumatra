/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This driver can drive on plain a path with multiple nodes.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PathPointDriver extends PositionDriver
{
	private static final float	VEL_MAX			= 3.0f;
	
	private final IPath			path;
	private final MovementCon	moveCon;
	
	
	@Configurable(comment = "If nearer than this dist [mm] to next point, go on to next point.")
	private static float			distTolerance	= 30;
	
	@Configurable(comment = "Min Raysize [mm] for p2pVisibility check (when standing)")
	private static float			raySizeMin		= 30;
	
	@Configurable(comment = "Max Raysize [mm] for p2pVisibility check (when vel=" + VEL_MAX + ")")
	private static float			raySizeMax		= 150;
	
	
	// private final IFunction1D raySizeFn;
	
	/**
	 * @param path
	 * @param moveCon
	 */
	public PathPointDriver(final IPath path, final MovementCon moveCon)
	{
		this.path = path;
		this.moveCon = moveCon;
		// raySizeFn = new Function1dPoly(new float[] { raySizeMin, (raySizeMax - raySizeMin) / VEL_MAX });
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (path.getStartPos() == null)
		{
			path.setStartPos(bot.getPos());
		}
		
		// find the nearest node. Next destination will be at least this node
		float smallestDist = Float.MAX_VALUE;
		for (int i = path.getCurrentDestinationNodeIdx(); i < path.getPathPoints().size(); i++)
		{
			float dist = GeoMath.distancePP(path.getPathPoints().get(i), bot.getPos());
			if (dist < smallestDist)
			{
				smallestDist = dist;
				path.setCurrentDestinationNodeIdx(i);
			}
		}
		
		// step on to next node?
		for (int i = path.getCurrentDestinationNodeIdx(); i < path.getPathPoints().size(); i++)
		{
			IVector2 curDest = path.getPathPoints().get(i);
			if ((GeoMath.distancePP(curDest, bot.getPos()) > distTolerance)
			// IVector2 nextDest = pathPoints.get(1);
			// || ((moveToMode != EMoveToMode.SAVE) &&
			// GeoMath.p2pVisibilityBotBall(getWorldFrame(), getPos(), nextDest, raySizeFn.eval(getVel().getLength2()),
			// getBot().getBotID()))
			)
			{
				path.setCurrentDestinationNodeIdx(i);
				break;
			}
		}
		
		IVector2 destination = path.getCurrentDestination();
		float targetOrientation = bot.getAngle();
		// TODO interpolate orientation
		if ((path.getPathPoints().size() - path.getCurrentDestinationNodeIdx()) <= 2)
		{
			targetOrientation = moveCon.getAngleCon().getTargetAngle();
		}
		setDestination(destination);
		setOrientation(targetOrientation);
		return super.getNextDestination(bot, wFrame);
	}
}
