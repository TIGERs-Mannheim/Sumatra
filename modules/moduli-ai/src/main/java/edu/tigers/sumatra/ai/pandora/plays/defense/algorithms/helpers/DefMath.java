/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.ITrajPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.PathFinderPrioMap;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DefMath
{
	private static ObstacleGenerator	obstacleGen	= new ObstacleGenerator();
	
	@SuppressWarnings("unused")
	private static final Logger		log			= Logger.getLogger(DefMath.class.getName());
	
	private static final int			numberSteps	= 10;
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param curFoeBot
	 * @param defender
	 * @return
	 */
	public static IVector2 calcNearestDefPoint(final FoeBotData curFoeBot, final ITrackedBot defender)
	{
		IVector2 bestDefPoint = null;
		
		IVector2 nearestToGoal = curFoeBot.getBot2goalNearestToGoal();
		IVector2 nearestToBot = curFoeBot.getBot2goalNearestToBot();
		
		IVector2 goal2bot = nearestToBot.subtractNew(nearestToGoal);
		
		double delta = goal2bot.getLength2() / (numberSteps - 1);
		
		double optimalCost = Double.MAX_VALUE;
		
		for (int i = 0; i < numberSteps; ++i)
		{
			IVector2 curDefPoint = nearestToGoal.addNew(goal2bot.scaleToNew(i * delta));
			double curCost = 0.;
			
			BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(
					defender, curDefPoint);
			curCost = pathToDest.getTotalTime();
			
			if (curCost < optimalCost)
			{
				
				bestDefPoint = curDefPoint;
				optimalCost = curCost;
			}
		}
		
		return bestDefPoint;
	}
	
	
	/**
	 * @param ballPos
	 * @param defender
	 * @return
	 */
	public static IVector2 calcNearestDefPointBall(final IVector2 ballPos, final ITrackedBot defender)
	{
		IVector2 bestDefPoint = null;
		
		double minDefRadius = AngleDefenseData.getMinDefRadius();
		double minDist2ball = DefenseAux.minDistDefender2Foe;
		IVector2 bisectorGoal = bisectorToGoal(ballPos);
		IVector2 ball2goal = bisectorGoal.subtractNew(ballPos);
		
		IVector2 nearestToGoal = bisectorGoal.addNew(ballPos.subtractNew(bisectorGoal).scaleTo(minDefRadius));
		IVector2 nearestToBall = ballPos.addNew(ball2goal.scaleToNew(minDist2ball));
		
		if (bisectorGoal.subtractNew(nearestToBall).getLength() < bisectorGoal.subtractNew(nearestToGoal).getLength())
		{
			return nearestToGoal;
		}
		
		IVector2 goal2ball = nearestToBall.subtractNew(nearestToGoal);
		
		double delta = goal2ball.getLength2() / (numberSteps - 1);
		
		double optimalCost = Double.MAX_VALUE;
		
		for (int i = 0; i < numberSteps; ++i)
		{
			IVector2 curDefPoint = nearestToGoal.addNew(goal2ball.scaleToNew(i * delta));
			double curCost = 0.;
			
			BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(
					defender, curDefPoint);
			curCost = pathToDest.getTotalTime();
			
			if (curCost < optimalCost)
			{
				
				bestDefPoint = curDefPoint;
				optimalCost = curCost;
			}
		}
		
		return bestDefPoint;
	}
	
	
	/**
	 * @param pos
	 * @return
	 */
	public static IVector2 bisectorToGoal(final IVector2 pos)
	{
		IVector2 leftPost = Geometry.getGoalOur().getGoalPostLeft();
		IVector2 rightPost = Geometry.getGoalOur().getGoalPostRight();
		
		return GeoMath.calculateBisector(pos, leftPost, rightPost);
	}
	
	
	/**
	 * @param frame
	 * @param bot
	 * @param pos
	 * @param gameState
	 * @return
	 */
	public static double getDefMovementCost(final WorldFrame frame, final ITrackedBot bot, final IVector2 pos,
			final EGameStateTeam gameState)
	{
		ITrajPathFinder finder = new TrajPathFinderV4();
		TrajPathFinderInput finderInput = new TrajPathFinderInput(frame.getTimestamp());
		
		double dist2DefPoint = GeoMath.distancePP(bot.getPos(), pos);
		
		finderInput.setTrackedBot(bot);
		finderInput.setDest(pos);
		
		obstacleGen.setUsePenAreaOur(true);
		obstacleGen.setUsePenAreaTheir(false);
		obstacleGen.setUseGoalPostsOur(false);
		obstacleGen.setUseGoalPostsTheir(false);
		obstacleGen.setUseFieldBorders(false);
		obstacleGen.setUseOurBots(true);
		
		if ((dist2DefPoint > dist2DefPoint) || (gameState == EGameStateTeam.STOPPED))
		{
			obstacleGen.setUseTheirBots(true);
			obstacleGen.setUseBall(true);
		} else
		{
			obstacleGen.setUseTheirBots(false);
			obstacleGen.setUseBall(true);
		}
		
		finderInput.setObstacles(obstacleGen.generateObstacles(frame, bot.getBotId(), PathFinderPrioMap.empty()));
		
		Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(finderInput);
		
		if (!path.isPresent())
		{
			return Double.MAX_VALUE - 1;
		}
		return path.get().getRemainingTrajectoryTime(frame.getTimestamp());
	}
}
