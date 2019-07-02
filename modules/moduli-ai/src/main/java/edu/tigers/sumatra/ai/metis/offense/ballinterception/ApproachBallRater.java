/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class ApproachBallRater
{
	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.7, 1.0);
	private final BallInterceptionRater ballInterceptionRater = new BallInterceptionRater();
	
	private boolean debug = false;
	
	
	public BallInterception rate(final WorldFrame worldFrame, final BotID botID, final IVector2 target,
			final BallInterception prevBallInterception)
	{
		ITrackedBot bot = worldFrame.getBot(botID);
		ballSpeedHysteresis.update(worldFrame.getBall().getVel().getLength2());
		
		if (ballStoppedMoving())
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			BangBangTrajectory2D trajectory = TrajectoryGenerator.generatePositionTrajectory(bot,
					worldFrame.getBall().getPos());
			if (debug)
			{
				shapes.add(new DrawableTrajectoryPath(trajectory, Color.lightGray));
			}
			return new BallInterception(botID, true, trajectory.getTotalTime(), -1, shapes, null);
		}
		
		return ballInterceptionRater.rate(worldFrame, botID, target, prevBallInterception);
	}
	
	
	private boolean ballStoppedMoving()
	{
		return ballSpeedHysteresis.isLower();
	}
	
	
	public void setDebug(final boolean debug)
	{
		this.debug = debug;
		ballInterceptionRater.setDebug(debug);
	}
}
