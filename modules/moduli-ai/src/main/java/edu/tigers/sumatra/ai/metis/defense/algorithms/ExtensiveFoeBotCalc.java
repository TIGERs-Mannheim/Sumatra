/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.algorithms.interfaces.IFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.metis.defense.data.IntersectionPoint;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ExtensiveFoeBotCalc implements IFoeBotCalc
{
	private static double				penAreaMargin				= DefenseAux.penAreaMargin();
	
	private static double				ballPossessionDistance	= 1000;
	
	private List<List<ITrackedBot>>	passCombinations;
	
	@Configurable(comment = "lookahead of the defense calc")
	private static double				lookAhead					= 0.3;
	
	
	@Override
	public List<FoeBotData> getFoeBotData(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		ArrayList<FoeBotData> foeBotDataList = new ArrayList<FoeBotData>();
		List<ITrackedBot> foeBotList = new ArrayList<>();
		EGameStateTeam gameState = newTacticalField.getGameState();
		
		TrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		IVector2 ballPos = DefenseAux.getBallPosDefense(ball);
		IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
		
		foeBotList.addAll(baseAiFrame
				.getWorldFrame()
				.getFoeBots()
				.values()
				.stream()
				.filter(
						bot -> !bot.getBotId().equals(baseAiFrame.getKeeperFoeId())
								&& Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.collect(Collectors.toList()));
		
		foeBotList.forEach(bot -> foeBotDataList.add(new FoeBotData(bot)));
		
		fillPassVectors(foeBotList);
		
		foeBotDataList.forEach(bot -> setBot2goal(bot, gameState));
		foeBotDataList.forEach(bot -> setBall2Bot(bot, ballPos));
		foeBotDataList.forEach(bot -> setGoalAngle(bot, goalCenter));
		foeBotDataList.forEach(bot -> setBot2goalIntersecs(bot, foeBotDataList, ball));
		foeBotDataList.forEach(bot -> setBall2botIntersecs(bot, foeBotDataList));
		
		setBallPossession(foeBotDataList, ball);
		setBestRedirector(foeBotDataList, baseAiFrame.getWorldFrame());
		
		return foeBotDataList;
	}
	
	
	/**
	 * @param foeBotDataList
	 * @param worldFrame
	 */
	private void setBestRedirector(final ArrayList<FoeBotData> foeBotDataList, final WorldFrame worldFrame)
	{
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		foeBotDataList.forEach(data -> bots.put(data.getFoeBot().getBotId(), data.getFoeBot()));
		
		BotID bestRedirector = OffensiveMath.getBestRedirector(worldFrame, bots, worldFrame.getBall().getVel(), null);
		
		for (FoeBotData data : foeBotDataList)
		{
			if (data.getFoeBot().getBotId() == bestRedirector)
			{
				data.setBestRedirector(true);
				break;
			}
		}
	}
	
	
	/**
	 * @param foeBotData
	 * @param goalCenter
	 *           Angle in [0, PI]
	 */
	private void setGoalAngle(final FoeBotData foeBotData, final IVector2 goalCenter)
	{
		double xCoord = -Geometry.getFieldLength() / 2;
		double yCoord = Geometry.getFieldWidth() / 2;
		double botRadius = Geometry.getBotRadius();
		
		IVector2 botPos = foeBotData.getFoeBot().getBotKickerPosByTime(DefenseAux.foeLookAheadDefenders);
		IVector2 corner = new Vector2(xCoord, yCoord);
		IVector2 minAngleCorner = new Vector2(xCoord + (botRadius / 2), yCoord);
		
		IVector2 goal2Bot = botPos.subtractNew(goalCenter);
		IVector2 refVector = corner.subtractNew(goalCenter);
		IVector2 minAngleVec = minAngleCorner.subtractNew(goalCenter);
		
		double foeBotAngle = GeoMath.angleBetweenVectorAndVector(refVector, goal2Bot);
		double minAngle = GeoMath.angleBetweenVectorAndVector(refVector, minAngleVec);
		double maxAngle = Math.PI - minAngle;
		
		foeBotAngle = Math.max(minAngle, foeBotAngle);
		foeBotAngle = Math.min(maxAngle, foeBotAngle);
		
		foeBotData.setGoalAngle(foeBotAngle);
	}
	
	
	private void fillPassVectors(final List<ITrackedBot> foeBotList)
	{
		passCombinations = new ArrayList<List<ITrackedBot>>();
		if (foeBotList.size() <= 1)
		{
			return;
		}
		for (ITrackedBot frstBot : foeBotList.subList(0, foeBotList.size() - 1))
		{
			for (ITrackedBot scndBot : foeBotList.subList(foeBotList.indexOf(frstBot), foeBotList.size()))
			{
				List<ITrackedBot> curList = new ArrayList<>();
				curList.add(frstBot);
				curList.add(scndBot);
				passCombinations.add(curList);
			}
		}
	}
	
	
	private void setBot2goal(final FoeBotData foeBot, final EGameStateTeam gameState)
	{
		double yLimitDefenders = ZoneDefenseCalc.getYLimitDefenders();
		double botLookaheadOfDefenders = DefenseAux.foeLookAheadDefenders;
		double minDistanceDef2Foe = ZoneDefenseCalc.getMinDistanceDef2Foe();
		double xLimitDefenders = -1;
		
		if (DefenseAux.isKickoffSituation(gameState))
		{
			xLimitDefenders = DefenseAux.maxXBotBlockingDefenderKickOff;
		} else
		{
			xLimitDefenders = DefenseAux.maxXBotBlockingDefender;
		}
		
		Goal goalOur = Geometry.getGoalOur();
		IVector2 foeBotPos = foeBot.getFoeBot().getPosByTime(botLookaheadOfDefenders);
		IVector2 pointInGoal = GeoMath
				.calculateBisector(foeBotPos, goalOur.getGoalPostLeft(), goalOur.getGoalPostRight());
		
		IVector2 bot2goal = pointInGoal.subtractNew(foeBotPos);
		
		IVector2 nearest2bot = foeBotPos.addNew(bot2goal.scaleToNew(minDistanceDef2Foe));
		nearest2bot = limitXPointOnVector(foeBotPos, foeBotPos.addNew(bot2goal), nearest2bot, xLimitDefenders);
		nearest2bot = limitYPointOnVector(foeBotPos, foeBotPos.addNew(bot2goal), nearest2bot, yLimitDefenders);
		
		IVector2 nearest2goal = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(pointInGoal, foeBotPos, Geometry.getBotRadius() + penAreaMargin);
		if (pointInGoal.subtractNew(nearest2bot).getLength2() < pointInGoal.subtractNew(nearest2goal).getLength2())
		{
			nearest2bot = nearest2goal;
		}
		
		foeBot.setBot2goal(bot2goal, nearest2bot, nearest2goal);
	}
	
	
	private void setBall2Bot(final FoeBotData foeBot, final IVector2 ballPos)
	{
		double yLimitDefenders = ZoneDefenseCalc.getYLimitDefenders();
		double botLookaheadOfDefenders = DefenseAux.foeLookAheadDefenders;
		double minDistanceDef2Ball = ZoneDefenseCalc.getMinDistanceDef2Ball();
		double minDistanceDef2Foe = ZoneDefenseCalc.getMinDistanceDef2Foe();
		
		IVector2 foeBotPos = foeBot.getFoeBot().getPosByTime(botLookaheadOfDefenders);
		IVector2 ball2Bot = foeBotPos.subtractNew(ballPos);
		IVector2 nearest2Ball = ballPos.addNew(ball2Bot.scaleToNew(0.5f * minDistanceDef2Ball));
		IVector2 nearest2Bot = foeBotPos.subtractNew(ball2Bot.scaleToNew(minDistanceDef2Foe));
		nearest2Bot = limitYPointOnVector(foeBotPos, ballPos, nearest2Bot, yLimitDefenders);
		
		if (ballPos.subtractNew(nearest2Bot).getLength2() < ballPos.subtractNew(nearest2Ball).getLength2())
		{
			nearest2Ball = nearest2Bot = nearest2Ball.addNew(nearest2Bot).multiply(0.5f);
		}
		foeBot.setBall2bot(ball2Bot, nearest2Ball, nearest2Bot);
	}
	
	
	private void setBot2goalIntersecs(final FoeBotData foeBot, final List<FoeBotData> foeBotDataList,
			final ITrackedObject ball)
	{
		double botLookaheadOfDefenders = DefenseAux.foeLookAheadDefenders;
		
		List<IntersectionPoint> intersectionPointListBall2bot = new ArrayList<IntersectionPoint>();
		List<IntersectionPoint> intersectionPointListBot2bot = new ArrayList<IntersectionPoint>();
		IVector2 intersectionPoint = null;
		
		for (FoeBotData curFoeBotData : foeBotDataList)
		{
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBot2goalNearestToBot(),
					foeBot.getBot2goalNearestToGoal(), curFoeBotData.getBall2botNearestToBall(),
					curFoeBotData.getBall2botNearestToBot());
			if (null != intersectionPoint)
			{
				intersectionPointListBall2bot
						.add(new IntersectionPoint(intersectionPoint, ball, curFoeBotData.getFoeBot()));
			}
		}
		
		foeBot.setBot2goalIntersecsBall2bot(intersectionPointListBall2bot);
		
		for (List<ITrackedBot> entry : passCombinations)
		{
			ITrackedBot firstBot = entry.get(0);
			ITrackedBot secondBot = entry.get(1);
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBot2goalNearestToBot(),
					foeBot.getBot2goalNearestToGoal(), firstBot.getPosByTime(botLookaheadOfDefenders),
					secondBot.getPosByTime(botLookaheadOfDefenders));
			if (null != intersectionPoint)
			{
				intersectionPointListBot2bot
						.add(new IntersectionPoint(intersectionPoint, firstBot, secondBot));
			}
		}
		
		foeBot.setBot2goalIntersecsBot2bot(intersectionPointListBot2bot);
	}
	
	
	private void setBall2botIntersecs(final FoeBotData foeBot, final List<FoeBotData> foeBotDataList)
	{
		double botLookaheadOfDefenders = DefenseAux.foeLookAheadDefenders;
		
		List<IntersectionPoint> intersectionPointListBot2goal = new ArrayList<IntersectionPoint>();
		List<IntersectionPoint> intersectionPointListBot2bot = new ArrayList<IntersectionPoint>();
		IVector2 intersectionPoint = null;
		
		for (FoeBotData curFoeBotData : foeBotDataList)
		{
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBall2botNearestToBall(),
					foeBot.getBall2botNearestToBot(), curFoeBotData.getBot2goalNearestToBot(),
					curFoeBotData.getBot2goalNearestToGoal());
			if (null != intersectionPoint)
			{
				intersectionPointListBot2goal
						.add(new IntersectionPoint(intersectionPoint, curFoeBotData.getFoeBot(), null));
			}
		}
		
		foeBot.setBall2botIntersecsBot2goal(intersectionPointListBot2goal);
		
		for (List<ITrackedBot> entry : passCombinations)
		{
			ITrackedBot firstBot = entry.get(0);
			ITrackedBot secondBot = entry.get(1);
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBall2botNearestToBall(),
					foeBot.getBall2botNearestToBot(), firstBot.getPosByTime(botLookaheadOfDefenders),
					secondBot.getPosByTime(botLookaheadOfDefenders));
			if (null != intersectionPoint)
			{
				intersectionPointListBot2bot
						.add(new IntersectionPoint(intersectionPoint, firstBot, secondBot));
			}
		}
		
		foeBot.setBall2botIntersecsBot2bot(intersectionPointListBot2bot);
	}
	
	
	private void setBallPossession(final List<FoeBotData> foeBotDataList, final TrackedBall ball)
	{
		if (ball.getVel().getLength() > DefenseAux.maxBallControlVel)
		{
			return;
		}
		
		double botLookaheadOfDefenders = DefenseAux.foeLookAheadDefenders;
		
		FoeBotData ballPosessingBot = null;
		double curBestDistance = Double.MAX_VALUE;
		double curDistance = 0;
		
		for (FoeBotData curData : foeBotDataList)
		{
			curDistance = ball.getPos().subtractNew(curData.getFoeBot().getPosByTime(botLookaheadOfDefenders))
					.getLength2();
			if (curDistance < curBestDistance)
			{
				ballPosessingBot = curData;
				curBestDistance = curDistance;
			}
		}
		
		if (null != ballPosessingBot)
		{
			if (curBestDistance > ballPossessionDistance)
			{
				// do explicitly nothing
			} else
			{
				ballPosessingBot.setPossessesBall(true);
			}
		}
	}
	
	
	/**
	 * Moves a point on a vector to match an x Limit in our positive x direction.
	 * vector does not cut the field the old point is returned
	 * 
	 * @param supp
	 * @param dir
	 * @param point
	 * @param xLimit
	 * @return
	 */
	public static IVector2 limitXPointOnVector(final IVector2 supp, final IVector2 dir, final IVector2 point,
			final double xLimit)
	{
		if (point.x() > xLimit)
		{
			double halfFieldWidth = Geometry.getFieldWidth() / 2.;
			IVector2 newPoint = GeoMath.intersectionBetweenPaths(supp, dir,
					new Vector2(xLimit, halfFieldWidth), new Vector2(xLimit, -halfFieldWidth));
			
			return newPoint == null ? point : newPoint;
		}
		
		return point;
	}
	
	
	/**
	 * Moves a point on a vector to match an y Limit in both field directions.
	 * vector does not cut the field the old point is returned
	 * 
	 * @param supp
	 * @param dir
	 * @param point
	 * @param yLimit
	 * @return
	 */
	public static IVector2 limitYPointOnVector(final IVector2 supp, final IVector2 dir, final IVector2 point,
			final double yLimit)
	{
		if (point.y() > yLimit)
		{
			double halfFieldLength = Geometry.getFieldLength() / 2.;
			IVector2 newPoint = GeoMath.intersectionBetweenPaths(supp, dir,
					new Vector2(halfFieldLength, yLimit), new Vector2(-halfFieldLength, yLimit));
			
			return newPoint == null ? point : newPoint;
		} else if (point.y() < -yLimit)
		{
			double halfFieldLength = Geometry.getFieldLength() / 2.;
			IVector2 newPoint = GeoMath.intersectionBetweenPaths(supp, dir,
					new Vector2(halfFieldLength, -yLimit), new Vector2(-halfFieldLength, -yLimit));
			
			return newPoint == null ? point : newPoint;
		}
		
		return point;
	}
}
