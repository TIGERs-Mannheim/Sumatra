/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2015
 * Author(s): chris
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculates different Chances of hitting an aim area.
 * 
 * @author chris
 */
public final class ProbabilityMath
{
	
	@Configurable(comment = "Scaling of distance between bot and goal")
	private static float		distanceScaling	= 0.2f;
	@Configurable(comment = "Assumed inaccuracy per distance")
	private static float		shootSpread			= 1 / 10f;
	
	private static IVector2	rotatedVect1;
	private static IVector2	rotatedVect2;
	private static IVector2	rotatedOrigin;
	private static IVector2	positionLargestGapCenter;
	private static float		perspectiveGoalLineLength;
	
	
	/**
	 * Calculate Chance scoring a goal
	 * 
	 * @param wf
	 * @param origin
	 * @param chipKick
	 * @return
	 */
	public static float getDirectShootScoreChanceNew(final WorldFrame wf, final IVector2 origin, final boolean chipKick)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.foeBots);
		rotateField(AIConfig.getGeometry().getGoalTheir().getGoalPostLeft(), AIConfig.getGeometry().getGoalTheir()
				.getGoalPostRight(), origin, wf.foeBots, rotatedBots);
		return calculateChance(
				calculateFreeSpace(ignoredBots(chipKick, rotatedBots, ignoredBots), rotatedBots, ignoredBots),
				perspectiveGoalLineLength);
	}
	
	
	/**
	 * Calculates Score chance for the FOE with Defender
	 * 
	 * @param wf
	 * @param foeBot
	 * @return Chance between 0 and 1
	 */
	public static float getFoeScoreChanceWithDefender(final WorldFrame wf, final IVector2 foeBot)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.tigerBotsVisible);
		rotateField(AIConfig.getGeometry().getGoalOur().getGoalPostRight(), AIConfig.getGeometry().getGoalOur()
				.getGoalPostLeft(), foeBot, wf.tigerBotsVisible, rotatedBots);
		
		return calculateChance(
				calculateFreeSpace(ignoredBots(false, rotatedBots, ignoredBots), rotatedBots, ignoredBots),
				perspectiveGoalLineLength);
	}
	
	
	/**
	 * Calculates Score chance for the FOE without Defender
	 * 
	 * @param aiFrame
	 * @param foeBot
	 * @return chance between 0 and 1
	 */
	public static float getFoeScoreChanceWithoutDefender(final BaseAiFrame aiFrame, final IVector2 foeBot)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		HashMap<BotID, IVector2> rotatedBots = new HashMap<BotID, IVector2>();
		rotatedBots.put(aiFrame.getKeeperId(), aiFrame.getWorldFrame().getTigerBotsAvailable().get(aiFrame.getKeeperId())
				.getPos());
		rotateField(AIConfig.getGeometry().getGoalOur().getGoalPostRight(), AIConfig.getGeometry().getGoalOur()
				.getGoalPostLeft(), foeBot, aiFrame.getWorldFrame().tigerBotsVisible, rotatedBots);
		
		return calculateChance(
				calculateFreeSpace(ignoredBots(false, rotatedBots, ignoredBots), rotatedBots, ignoredBots),
				perspectiveGoalLineLength);
	}
	
	
	/**
	 * Calculate Chance hitting between vect1 and vect2
	 * 
	 * @param wf
	 * @param origin
	 * @param vect1
	 * @param vect2
	 * @param chipKick
	 * @return
	 */
	public static float getDirectHitChance(final WorldFrame wf, final IVector2 origin, final IVector2 vect1,
			final IVector2 vect2,
			final boolean chipKick)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.foeBots);
		rotateField(vect1, vect2, origin, wf.foeBots, rotatedBots);
		return calculateChance(
				calculateFreeSpace(ignoredBots(chipKick, rotatedBots, ignoredBots), rotatedBots, ignoredBots),
				perspectiveGoalLineLength);
	}
	
	
	private static void rotateField(final IVector2 vect1, final IVector2 vect2,
			final IVector2 origin, final BotIDMapConst<TrackedTigerBot> foeBots, final HashMap<BotID, IVector2> rotatedBots)
	{
		IVector2 center = vect1.subtractNew(vect2).multiply(0.5f).addNew(vect2);
		float rotateAngle = vect1.subtractNew(vect2).getAngle() - ((float) Math.PI / 2);
		
		center = center.subtractNew(center).add(new Vector2(center.getLength2(), 0));
		rotatedVect1 = vect1.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		rotatedVect2 = vect2.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		rotatedOrigin = origin.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		rotatedBots.clear();
		for (Entry<BotID, TrackedTigerBot> foeBot : foeBots)
		{
			rotatedBots.put(
					foeBot.getKey(),
					foeBot.getValue().getPos().subtractNew(center).turn(-rotateAngle)
							.add(new Vector2(center.getLength2(), 0)));
		}
		
	}
	
	
	/**
	 * Fill Map "ignoredBots" with Bots that can be ignored
	 */
	private static int ignoredBots(final boolean chipKick,
			final HashMap<BotID, IVector2> rotatedBots, final List<BotID> ignoredBots)
	{
		ignoredBots.clear();
		final float rBot = AIConfig.getGeometry().getBotRadius();
		
		int recognizedFoeBots = rotatedBots.size();
		
		// ignore irrelevant Bots
		for (Entry<BotID, IVector2> foeBot : rotatedBots.entrySet())
		{
			if (((GeoMath.distancePP(rotatedOrigin, foeBot.getValue()) < 1000) && chipKick)
					|| (foeBot.getValue().x() <= (rotatedOrigin.x() - rBot)))
			{
				ignoredBots.add(foeBot.getKey());
				recognizedFoeBots--;
			}
		}
		
		return recognizedFoeBots;
	}
	
	
	/**
	 * Calculated the largest free Space between two Vectors
	 * (requires setting rotatedVect1 and rotatedVect2 above)
	 * 
	 * @return
	 */
	private synchronized static float calculateFreeSpace(final int recognizedFoeBots,
			final HashMap<BotID, IVector2> rotatedBots, final List<BotID> ignoredBots)
	{
		IntersectionGoalPoint[] intersections = new IntersectionGoalPoint[(recognizedFoeBots * 2)];
		boolean isPerspectiveRight;
		IVector2 nearestGoalPost, otherGoalPost;
		IVector2 translationOriginNearestGoalPost, translationOriginOtherGoalPost;
		IVector2 perspectiveRightGoalPost, perspectiveLeftGoalPost;
		float angleNearestOtherGoalPost;
		IVector2 perspectiveOtherGoalPost;
		int nIterations = 0;
		IVector2 translationOriginFoe;
		float tempAngle;
		IVector2 intersectionLeft, intersectionRight;
		
		if (rotatedOrigin.y() > 0) // origin right of goal?
		{
			isPerspectiveRight = false;
			if (rotatedVect1.y() > rotatedVect2.y())
			{
				nearestGoalPost = rotatedVect1;
				otherGoalPost = rotatedVect2;
			}
			else
			{
				nearestGoalPost = rotatedVect2;
				otherGoalPost = rotatedVect1;
			}
		}
		else
		{
			isPerspectiveRight = true;
			if (rotatedVect1.y() > rotatedVect2.y())
			{
				nearestGoalPost = rotatedVect2;
				otherGoalPost = rotatedVect1;
			}
			else
			{
				nearestGoalPost = rotatedVect1;
				otherGoalPost = rotatedVect2;
			}
		}
		
		translationOriginNearestGoalPost = nearestGoalPost.subtractNew(rotatedOrigin);
		translationOriginOtherGoalPost = otherGoalPost.subtractNew(rotatedOrigin);
		
		angleNearestOtherGoalPost = GeoMath.angleBetweenVectorAndVector(translationOriginNearestGoalPost,
				translationOriginOtherGoalPost);
		
		perspectiveGoalLineLength = (float) (2 * Math.sin(angleNearestOtherGoalPost / 2) * translationOriginNearestGoalPost
				.getLength2());
		
		perspectiveOtherGoalPost = translationOriginOtherGoalPost.multiplyNew(
				(translationOriginNearestGoalPost.getLength2() / translationOriginOtherGoalPost.getLength2())).addNew(
				rotatedOrigin);
		
		// Which goalpost is perspective?
		if (isPerspectiveRight)
		{
			perspectiveRightGoalPost = nearestGoalPost;
			perspectiveLeftGoalPost = perspectiveOtherGoalPost;
		}
		else
		{
			perspectiveRightGoalPost = perspectiveOtherGoalPost;
			perspectiveLeftGoalPost = nearestGoalPost;
		}
		
		for (Entry<BotID, IVector2> foeBot : rotatedBots.entrySet())
		{
			if (ignoredBots.contains(foeBot.getKey()))
			{
				continue; // current bot ignored? --> next bot
			}
			
			translationOriginFoe = foeBot.getValue().subtractNew(rotatedOrigin);
			
			// beta = arccos(1 - (rBot²/2a²)) a = distance origin <-> foeBot
			float cosbeta = 1 - (float) (Math.pow(AIConfig.getGeometry().getBotRadius(), 2) / (2 * Math.pow(
					translationOriginFoe.getLength2(), 2)));
			cosbeta = (float) ((cosbeta > 1.0) ? 1.0 : ((cosbeta < -1.0) ? -1.0 : cosbeta));
			tempAngle = (float) Math.acos(cosbeta);
			
			try
			{
				intersectionLeft = GeoMath.intersectionPoint(rotatedOrigin,
						translationOriginFoe.turnNew(tempAngle), nearestGoalPost,
						perspectiveOtherGoalPost.subtractNew(nearestGoalPost));
				
				intersectionRight = GeoMath.intersectionPoint(rotatedOrigin,
						translationOriginFoe.turnNew(-tempAngle), nearestGoalPost,
						perspectiveOtherGoalPost.subtractNew(nearestGoalPost));
				
			} catch (MathException err)
			{
				err.printStackTrace();
				// array has to be filled completely (due to Array.sort())
				intersections[nIterations++] = new IntersectionGoalPoint(perspectiveLeftGoalPost, false);
				intersections[nIterations++] = new IntersectionGoalPoint(perspectiveRightGoalPost, true);
				continue; // no intersections? --> continue
			}
			
			// Intersections should not be outside of field
			if (intersectionLeft.y() > perspectiveLeftGoalPost.y())
			{
				intersectionLeft = perspectiveLeftGoalPost;
			}
			
			if (intersectionLeft.y() < perspectiveRightGoalPost.y())
			{
				intersectionLeft = perspectiveRightGoalPost;
			}
			
			if (intersectionRight.y() > perspectiveLeftGoalPost.y())
			{
				intersectionRight = perspectiveLeftGoalPost;
			}
			
			if (intersectionRight.y() < perspectiveRightGoalPost.y())
			{
				intersectionRight = perspectiveRightGoalPost;
			}
			
			// intersection wrong? --> correct
			if ((intersectionLeft.y() - intersectionRight.y()) < 0)
			{
				intersectionLeft = perspectiveLeftGoalPost;
				intersectionRight = perspectiveLeftGoalPost;
			}
			
			intersections[nIterations++] = new IntersectionGoalPoint(intersectionLeft, false);
			intersections[nIterations++] = new IntersectionGoalPoint(intersectionRight, true);
		}
		if (recognizedFoeBots != 0)
		{
			Arrays.sort(intersections);
		}
		
		float largestGap = 0;
		IVector2 currentRightmostIntersection = perspectiveRightGoalPost;
		positionLargestGapCenter = nearestGoalPost;
		int overlapCounter = 0;
		for (int i = 0; i < (intersections.length); i++)
		{
			if ((overlapCounter == 0) && (intersections[i].isRight()))
			{
				float tempgap = intersections[i].getLocation().subtractNew(currentRightmostIntersection).getLength2();
				largestGap = (tempgap > largestGap) ? tempgap : largestGap;
				positionLargestGapCenter = (tempgap > largestGap) ? intersections[i].getLocation()
						: positionLargestGapCenter; // approximation!
			}
			
			if (intersections[i].isRight())
			{
				overlapCounter++;
			} else
			{
				overlapCounter--;
			}
			
			if ((overlapCounter == 0) && (!intersections[i].isRight()))
			{
				currentRightmostIntersection = intersections[i].getLocation();
			}
		}
		if (intersections.length == 0)
		{
			largestGap = perspectiveGoalLineLength;
			positionLargestGapCenter = perspectiveLeftGoalPost; // approximation!
		}
		else
		{
			float tempgap = intersections[intersections.length - 1].getLocation().subtractNew(perspectiveLeftGoalPost)
					.getLength2();
			largestGap = (tempgap > largestGap) ? tempgap : largestGap;
			positionLargestGapCenter = perspectiveLeftGoalPost;
		}
		return largestGap;
	}
	
	
	/**
	 * Calculate the Chance of Hitting an Area
	 * 
	 * @param largestGap
	 * @param perspectiveGoalLineLength
	 * @return Chance between 0 and 1
	 */
	private static float calculateChance(final float largestGap, final float perspectiveGoalLineLength)
	{
		float result;
		if (largestGap < (AIConfig.getGeometry().getBallRadius() * 2))
		{
			result = 0;
		}
		else
		{
			result = (largestGap - (distanceScaling * rotatedOrigin.subtractNew(positionLargestGapCenter).getLength2() * shootSpread))
					/ perspectiveGoalLineLength;
		}
		if (result < 0)
		{
			result = 0.0f;
		}
		return result;
	}
	
	
	/**
	 * @author Arne Sachtler
	 * @author Dominik Engelhardt
	 * @author Chris Carstensen
	 * @author Lukas Schmierer
	 *         IntersectionGoalPoint consists of Vector2-Type with additional left/right-information
	 *         is used by getDirectShootScoreChanceNew-Function.
	 *         During sort-Algorithm the IntersectionGoalPoints are sorted by the y-component and
	 *         left-right information.
	 */
	private static class IntersectionGoalPoint implements Comparable<IntersectionGoalPoint>
	{
		private IVector2	location;
		private boolean	isRight;
		
		
		IntersectionGoalPoint(final IVector2 location, final boolean isRight)
		{
			this.location = location;
			this.isRight = isRight;
		}
		
		
		@Override
		public int compareTo(final IntersectionGoalPoint o)
		{
			if ((int) (location.y() - o.location.y()) == 0)
			{
				return (isRight()) ? -1 : +1;
			}
			return (int) (location.y() - o.location.y());
		}
		
		
		public IVector2 getLocation()
		{
			return location;
		}
		
		
		public boolean isRight()
		{
			return isRight;
		}
	}
	
	
	private static HashMap<BotID, IVector2> castTigerBotsToBots(final BotIDMapConst<TrackedTigerBot> foeBots)
	{
		HashMap<BotID, IVector2> bots = new HashMap<BotID, IVector2>();
		
		for (Entry<BotID, TrackedTigerBot> foeBot : foeBots)
		{
			bots.put(foeBot.getKey(), foeBot.getValue().getPos());
		}
		return bots;
	}
	
}
