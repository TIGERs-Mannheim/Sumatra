/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This driver can drive on plain a path with multiple nodes.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PathPointDriver extends PositionDriver
{
	
	private static final double	VEL_MAX			= 3.0;
																
	private final IPath				path;
	private final MovementCon		moveCon;
	private int							curDestIdx		= 0;
																
																
	@Configurable(comment = "If nearer than this dist [mm] to next point, go on to next point.")
	private static double			distTolerance	= 30;
																
	@Configurable(comment = "Min Raysize [mm] for p2pVisibility check (when standing)")
	private static double			raySizeMin		= 30;
																
	@Configurable(comment = "Max Raysize [mm] for p2pVisibility check (when vel=" + VEL_MAX + ")")
	private static double			raySizeMax		= 150;
																
																
	static
	{
		ConfigRegistration.registerClass("skills", PathPointDriver.class);
	}
	
	
	// private final IFunction1D raySizeFn;
	
	/**
	 * @param path
	 * @param moveCon
	 */
	public PathPointDriver(final IPath path, final MovementCon moveCon)
	{
		this.path = path;
		this.moveCon = moveCon;
		// raySizeFn = new Function1dPoly(new double[] { raySizeMin, (raySizeMax - raySizeMin) / VEL_MAX });
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		// find the nearest node. Next destination will be at least this node
		double smallestDist = Double.MAX_VALUE;
		for (int i = curDestIdx; i < path.getPathPoints().size(); i++)
		{
			double dist = GeoMath.distancePP(path.getPathPoints().get(i), bot.getPos());
			if (dist < smallestDist)
			{
				smallestDist = dist;
				curDestIdx = i;
			}
		}
		
		// step on to next node?
		for (int i = curDestIdx; i < path.getPathPoints().size(); i++)
		{
			IVector2 curDest = path.getPathPoints().get(i);
			if ((GeoMath.distancePP(curDest, bot.getPos()) > distTolerance)
			// IVector2 nextDest = pathPoints.get(1);
			// || ((moveToMode != EMoveToMode.SAVE) &&
			// GeoMath.p2pVisibilityBotBall(getWorldFrame(), getPos(), nextDest, raySizeFn.eval(getVel().getLength2()),
			// getBot().getBotID()))
			)
			{
				curDestIdx = i;
				break;
			}
		}
		
		IVector2 destination = path.getPathPoints().get(curDestIdx);
		double targetOrientation = bot.getAngle();
		// TODO interpolate orientation
		if ((path.getPathPoints().size() - curDestIdx) <= 2)
		{
			targetOrientation = moveCon.getTargetAngle();
		}
		setDestination(destination);
		setOrientation(targetOrientation);
		return super.getNextDestination(bot, wFrame);
	}
}
