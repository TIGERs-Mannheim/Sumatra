/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.algorithms.interfaces.IFoeBotCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.IntersectionPoint;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ExtensiveFoeBotCalc implements IFoeBotCalc
{
	private static Goal						ourGoal						= AIConfig.getGeometry().getGoalOur();
	
	private static float						intersecTolerance			= 2 * AIConfig.getGeometry().getBotRadius();
	
	private static float						penAreaMargin				= Geometry.getPenaltyAreaMargin();
	
	private static float						ballPossessionDistance	= 1000f;
	
	private List<List<TrackedTigerBot>>	passCombinations;
	
	@Configurable(comment = "lookahead of the defense calc")
	private static float						lookAhead					= 0.5f;
	
	
	@Override
	public List<FoeBotData> getFoeBotData(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		ArrayList<FoeBotData> foeBotDataList = new ArrayList<FoeBotData>();
		List<TrackedTigerBot> foeBotList = new ArrayList<TrackedTigerBot>();
		
		TrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		IVector2 ballPos = ball.getPosByTime(lookAhead);
		
		foeBotList.addAll(baseAiFrame
				.getWorldFrame()
				.getFoeBots()
				.values()
				.stream()
				.filter(
						bot -> !bot.getId().equals(baseAiFrame.getKeeperFoeId())
								&& AIConfig.getGeometry().getFieldWBorders().isPointInShape(bot.getPos()))
				.collect(Collectors.toList()));
		
		foeBotList.forEach(bot -> foeBotDataList.add(new FoeBotData(bot)));
		
		fillPassVectors(foeBotList);
		foeBotDataList.forEach(bot -> setBot2goal(bot));
		foeBotDataList.forEach(bot -> setBall2Bot(bot, ballPos));
		foeBotDataList.forEach(bot -> setBot2goalIntersecs(bot, foeBotDataList, ball));
		foeBotDataList.forEach(bot -> setBall2botIntersecs(bot, foeBotDataList));
		
		setBallPossession(foeBotDataList, ballPos);
		
		foeBotDataList.sort(FoeBotData.DANGER_COMPARATOR);
		return foeBotDataList;
	}
	
	
	private void fillPassVectors(final List<TrackedTigerBot> foeBotList)
	{
		passCombinations = new ArrayList<List<TrackedTigerBot>>();
		if (foeBotList.size() <= 1)
		{
			return;
		}
		for (TrackedTigerBot frstBot : foeBotList.subList(0, foeBotList.size() - 1))
		{
			for (TrackedTigerBot scndBot : foeBotList.subList(foeBotList.indexOf(frstBot), foeBotList.size()))
			{
				List<TrackedTigerBot> curList = new ArrayList<TrackedTigerBot>();
				curList.add(frstBot);
				curList.add(scndBot);
				passCombinations.add(curList);
			}
		}
	}
	
	
	private void setBot2goal(final FoeBotData foeBot)
	{
		IVector2 foeBotPos = foeBot.getFoeBot().getPosByTime(lookAhead);
		IVector2 pointInGoal = GeoMath
				.calculateBisector(foeBotPos, ourGoal.getGoalPostLeft(), ourGoal.getGoalPostRight());
		IVector2 bot2goal = pointInGoal.subtractNew(foeBotPos);
		
		IVector2 nearest2bot = foeBotPos.addNew(bot2goal.scaleToNew(intersecTolerance));
		
		IVector2 nearest2goal = AIConfig.getGeometry().getPenaltyAreaOur()
				.nearestPointOutside(pointInGoal, foeBotPos, penAreaMargin);
		if (pointInGoal.subtractNew(nearest2bot).getLength2() < pointInGoal.subtractNew(nearest2goal).getLength2())
		{
			nearest2bot = nearest2goal;
		}
		foeBot.setBot2goal(bot2goal, nearest2bot, nearest2goal);
	}
	
	
	private void setBall2Bot(final FoeBotData foeBot, final IVector2 ballPos)
	{
		IVector2 foeBotPos = foeBot.getFoeBot().getPosByTime(lookAhead);
		IVector2 ball2Bot = foeBotPos.subtractNew(ballPos);
		IVector2 nearest2Ball = ballPos.addNew(ball2Bot.scaleToNew(0.5f * intersecTolerance));
		IVector2 nearest2Bot = foeBotPos.subtractNew(ball2Bot.scaleToNew(intersecTolerance));
		if (ballPos.subtractNew(nearest2Bot).getLength2() < ballPos.subtractNew(nearest2Ball).getLength2())
		{
			nearest2Ball = nearest2Bot = nearest2Ball.addNew(nearest2Bot).multiply(0.5f);
		}
		foeBot.setBall2bot(ball2Bot, nearest2Ball, nearest2Bot);
	}
	
	
	private void setBot2goalIntersecs(final FoeBotData foeBot, final List<FoeBotData> foeBotDataList,
			final ATrackedObject ball)
	{
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
		
		for (List<TrackedTigerBot> entry : passCombinations)
		{
			TrackedTigerBot firstBot = entry.get(0);
			TrackedTigerBot secondBot = entry.get(1);
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBot2goalNearestToBot(),
					foeBot.getBot2goalNearestToGoal(), firstBot.getPosByTime(lookAhead), secondBot.getPosByTime(lookAhead));
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
		
		for (List<TrackedTigerBot> entry : passCombinations)
		{
			TrackedTigerBot firstBot = entry.get(0);
			TrackedTigerBot secondBot = entry.get(1);
			intersectionPoint = GeoMath.intersectionBetweenPaths(foeBot.getBall2botNearestToBall(),
					foeBot.getBall2botNearestToBot(), firstBot.getPosByTime(lookAhead), secondBot.getPosByTime(lookAhead));
			if (null != intersectionPoint)
			{
				intersectionPointListBot2bot
						.add(new IntersectionPoint(intersectionPoint, firstBot, secondBot));
			}
		}
		
		foeBot.setBall2botIntersecsBot2bot(intersectionPointListBot2bot);
	}
	
	
	private void setBallPossession(final List<FoeBotData> foeBotDataList, final IVector2 ballPos)
	{
		FoeBotData ballPosessingBot = null;
		float curBestDistance = Float.MAX_VALUE;
		float curDistance = 0;
		
		for (FoeBotData curData : foeBotDataList)
		{
			curDistance = ballPos.subtractNew(curData.getFoeBot().getPosByTime(lookAhead)).getLength2();
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
}
