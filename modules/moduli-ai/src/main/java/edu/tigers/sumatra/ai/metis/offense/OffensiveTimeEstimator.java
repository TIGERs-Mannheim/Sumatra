/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import static edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc.CatchBallResult;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.ai.metis.offense.data.OffensiveMoveAndTargetInformation;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.ITrajPathFinder;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;
import edu.tigers.sumatra.pathfinder.traj.TrajPathFinderRambo;
import edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OffensiveTimeEstimator
{
	private final BotID botID;
	private final CatchBallCalc catchBallCalc;
	private final ITrajPathFinder pathFinder = new TrajPathFinderRambo();
	private final MovementCon moveCon = new MovementCon();
	
	
	/**
	 * @param botID
	 */
	public OffensiveTimeEstimator(final BotID botID)
	{
		this.botID = botID;
		catchBallCalc = new CatchBallCalc(botID);
	}
	
	
	/**
	 * @param worldFrame
	 * @param shapes
	 * @param info
	 * @return
	 */
	public double estimateGetBallContactTime(final WorldFrame worldFrame, final List<IDrawableShape> shapes,
			OffensiveMoveAndTargetInformation info)
	{
		ITrackedBot tBot = worldFrame.getTiger(botID);
		moveCon.update(worldFrame, tBot);
		
		boolean receive = info.isReceiveActive();
		IPassTarget passTarget = info.getPassTarget();
		
		TrajPathFinderInput pathFinderInput = new TrajPathFinderInput(worldFrame.getTimestamp());
		pathFinderInput.setTrackedBot(tBot);
		pathFinderInput.setMoveConstraints(moveCon.getMoveConstraints());
		
		if (receive)
		{
			catchBallCalc.setMoveCon(moveCon);
			catchBallCalc.update(worldFrame);
			CatchBallResult result = catchBallCalc.calculate();
			
			pathFinderInput.setDest(result.getBotDest());
		} else
		{
			IVector2 dest = LineMath.stepAlongLine(worldFrame.getBall().getPos(), passTarget.getKickerPos(),
					-tBot.getCenter2DribblerDist() - Geometry.getBallRadius());
			pathFinderInput.setDest(dest);
			reset();
		}
		
		IPathFinderResult pathFinderResult = pathFinder.calcPath(pathFinderInput);
		double pathTime = pathFinderResult.getTrajectory().getTotalTime();
		
		shapes.add(
				new DrawableAnnotation(tBot.getPos(),
						String.format("primary: %.2f (%s)", pathTime, receive ? "recv" : "kick"),
						Vector2.fromXY(0, -220))
								.setCenterHorizontally(true));
		shapes.add(new DrawableTrajectoryPath(pathFinderResult.getTrajectory(), Color.gray));
		
		return pathTime;
	}
	
	
	/**
	 * Reset state
	 */
	public void reset()
	{
		catchBallCalc.reset();
	}
}
