/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2015
 * Author(s): chris
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Calculates different Chances of hitting an aim area.
 * 
 * @author chris
 */
public final class ProbabilityMath
{
	@Configurable(comment = "Scaling of distance between bot and goal")
	private static double	distanceScaling	= 0.2;
	@Configurable(comment = "Assumed inaccuracy per distance")
	private static double	shootSpread			= 1.0 / 10.0;
	@Configurable(comment = "Offset of time to lookAHead and calculate therefore the HitChance")
	private static double	lookAHead			= 0.5;
	
	
	private static class Parameter
	{
		public IVector2	rotatedVect1;
		public IVector2	rotatedVect2;
		public IVector2	rotatedOrigin;
		public IVector2	positionLargestGapCenter;
		public double		perspectiveGoalLineLength;
	}
	
	
	static
	{
		ConfigRegistration.registerClass("math", ProbabilityMath.class);
	}
	
	
	/**
	 * Calculate Chance scoring a goal
	 * 
	 * @param wf
	 * @param origin
	 * @param chipKick
	 * @return
	 */
	public static double getDirectShootScoreChance(final WorldFrame wf, final IVector2 origin, final boolean chipKick)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		Parameter param = new Parameter();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.foeBots);
		lookAhead(rotatedBots, wf);
		rotateField(Geometry.getGoalTheir().getGoalPostLeft(), Geometry.getGoalTheir()
				.getGoalPostRight(), origin, rotatedBots, param);
		return calculateChance(
				calculateFreeSpace(ignoredBots(chipKick, rotatedBots, ignoredBots, param), rotatedBots, ignoredBots, param),
				param.perspectiveGoalLineLength, param);
	}
	
	
	/**
	 * Calculates Score chance for the FOE with Defender
	 * 
	 * @param wf
	 * @param foeBot
	 * @return Chance between 0 and 1
	 */
	public static double getFoeScoreChanceWithDefender(final WorldFrame wf, final IVector2 foeBot)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		Parameter param = new Parameter();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.tigerBotsVisible);
		lookAhead(rotatedBots, wf);
		rotateField(Geometry.getGoalOur().getGoalPostRight(), Geometry.getGoalOur()
				.getGoalPostLeft(), foeBot, rotatedBots, param);
		
		return calculateChance(
				calculateFreeSpace(ignoredBots(false, rotatedBots, ignoredBots, param), rotatedBots, ignoredBots, param),
				param.perspectiveGoalLineLength, param);
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
	public static double getDirectHitChance(final WorldFrame wf, final IVector2 origin, final IVector2 vect1,
			final IVector2 vect2,
			final boolean chipKick)
	{
		List<BotID> ignoredBots = new ArrayList<BotID>();
		Parameter param = new Parameter();
		HashMap<BotID, IVector2> rotatedBots = castTigerBotsToBots(wf.foeBots);
		lookAhead(rotatedBots, wf);
		rotateField(vect1, vect2, origin, rotatedBots, param);
		return calculateChance(
				calculateFreeSpace(ignoredBots(chipKick, rotatedBots, ignoredBots, param), rotatedBots, ignoredBots, param),
				param.perspectiveGoalLineLength, param);
	}
	
	
	private static void lookAhead(final HashMap<BotID, IVector2> rotatedBots, final WorldFrame wf)
	{
		
		for (Entry<BotID, IVector2> bot : rotatedBots.entrySet())
		{
			IVector2 vel = wf.getBot(bot.getKey()).getVel();
			bot.setValue(bot.getValue().addNew(vel.scaleToNew(lookAHead)));
		}
	}
	
	
	private static void rotateField(final IVector2 vect1, final IVector2 vect2,
			final IVector2 origin, final HashMap<BotID, IVector2> rotatedBots,
			final Parameter param)
	{
		IVector2 center = vect1.subtractNew(vect2).multiply(0.5f).addNew(vect2);
		double rotateAngle = vect1.subtractNew(vect2).getAngle() - (Math.PI / 2.0);
		
		center = center.subtractNew(center).add(new Vector2(center.getLength2(), 0));
		param.rotatedVect1 = vect1.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		param.rotatedVect2 = vect2.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		param.rotatedOrigin = origin.subtractNew(center).turn(-rotateAngle).add(new Vector2(center.getLength2(), 0));
		for (Entry<BotID, IVector2> bot : rotatedBots.entrySet())
		{
			rotatedBots.put(
					bot.getKey(),
					bot.getValue().subtractNew(center).turn(-rotateAngle)
							.add(new Vector2(center.getLength2(), 0)));
		}
		
	}
	
	
	/**
	 * Fill Map "ignoredBots" with Bots that can be ignored
	 * 
	 * @param param TODO
	 */
	private static int ignoredBots(final boolean chipKick,
			final HashMap<BotID, IVector2> rotatedBots, final List<BotID> ignoredBots, final Parameter param)
	{
		ignoredBots.clear();
		final double rBot = Geometry.getBotRadius();
		
		int recognizedFoeBots = rotatedBots.size();
		
		// ignore irrelevant Bots
		for (Entry<BotID, IVector2> foeBot : rotatedBots.entrySet())
		{
			if (((GeoMath.distancePP(param.rotatedOrigin, foeBot.getValue()) < 1000) && chipKick)
					|| (foeBot.getValue().x() <= (param.rotatedOrigin.x() - rBot)))
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
	 * @param param TODO
	 * @return
	 */
	private synchronized static double calculateFreeSpace(final int recognizedFoeBots,
			final HashMap<BotID, IVector2> rotatedBots, final List<BotID> ignoredBots, final Parameter param)
	{
		IntersectionGoalPoint[] intersections = new IntersectionGoalPoint[(recognizedFoeBots * 2)];
		boolean isPerspectiveRight;
		IVector2 nearestGoalPost, otherGoalPost;
		IVector2 translationOriginNearestGoalPost, translationOriginOtherGoalPost;
		IVector2 perspectiveRightGoalPost, perspectiveLeftGoalPost;
		double angleNearestOtherGoalPost;
		IVector2 perspectiveOtherGoalPost;
		int nIterations = 0;
		IVector2 translationOriginFoe;
		double tempAngle;
		IVector2 intersectionLeft, intersectionRight;
		
		if (param.rotatedOrigin.y() > 0) // origin right of goal?
		{
			isPerspectiveRight = false;
			if (param.rotatedVect1.y() > param.rotatedVect2.y())
			{
				nearestGoalPost = param.rotatedVect1;
				otherGoalPost = param.rotatedVect2;
			} else
			{
				nearestGoalPost = param.rotatedVect2;
				otherGoalPost = param.rotatedVect1;
			}
		} else
		{
			isPerspectiveRight = true;
			if (param.rotatedVect1.y() > param.rotatedVect2.y())
			{
				nearestGoalPost = param.rotatedVect2;
				otherGoalPost = param.rotatedVect1;
			} else
			{
				nearestGoalPost = param.rotatedVect1;
				otherGoalPost = param.rotatedVect2;
			}
		}
		
		translationOriginNearestGoalPost = nearestGoalPost.subtractNew(param.rotatedOrigin);
		translationOriginOtherGoalPost = otherGoalPost.subtractNew(param.rotatedOrigin);
		
		angleNearestOtherGoalPost = GeoMath.angleBetweenVectorAndVector(translationOriginNearestGoalPost,
				translationOriginOtherGoalPost);
		
		param.perspectiveGoalLineLength = 2 * Math.sin(angleNearestOtherGoalPost / 2.0) * translationOriginNearestGoalPost
				.getLength2();
		
		perspectiveOtherGoalPost = translationOriginOtherGoalPost.multiplyNew(
				(translationOriginNearestGoalPost.getLength2() / translationOriginOtherGoalPost.getLength2())).addNew(
						param.rotatedOrigin);
		
		// Which goalpost is perspective?
		if (isPerspectiveRight)
		{
			perspectiveRightGoalPost = nearestGoalPost;
			perspectiveLeftGoalPost = perspectiveOtherGoalPost;
		} else
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
			
			translationOriginFoe = foeBot.getValue().subtractNew(param.rotatedOrigin);
			
			// beta = arccos(1 - (rBot²/2a²)) a = distance origin <-> foeBot
			double cosbeta = 1 - (Math.pow(Geometry.getBotRadius(), 2) / (2.0 * Math.pow(
					translationOriginFoe.getLength2(), 2)));
			cosbeta = (cosbeta > 1.0) ? 1.0 : ((cosbeta < -1.0) ? -1.0 : cosbeta);
			tempAngle = Math.acos(cosbeta);
			
			try
			{
				intersectionLeft = GeoMath.intersectionPoint(param.rotatedOrigin,
						translationOriginFoe.turnNew(tempAngle), nearestGoalPost,
						perspectiveOtherGoalPost.subtractNew(nearestGoalPost));
				
				intersectionRight = GeoMath.intersectionPoint(param.rotatedOrigin,
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
		
		double largestGap = 0;
		IVector2 currentRightmostIntersection = perspectiveRightGoalPost;
		param.positionLargestGapCenter = nearestGoalPost;
		int overlapCounter = 0;
		for (int i = 0; i < (intersections.length); i++)
		{
			if ((overlapCounter == 0) && (intersections[i].isRight()))
			{
				double tempgap = intersections[i].getLocation().subtractNew(currentRightmostIntersection).getLength2();
				largestGap = (tempgap > largestGap) ? tempgap : largestGap;
				param.positionLargestGapCenter = (tempgap > largestGap) ? intersections[i].getLocation()
						: param.positionLargestGapCenter; // approximation!
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
			largestGap = param.perspectiveGoalLineLength;
			param.positionLargestGapCenter = perspectiveLeftGoalPost; // approximation!
		} else
		{
			double tempgap = intersections[intersections.length - 1].getLocation().subtractNew(perspectiveLeftGoalPost)
					.getLength2();
			largestGap = (tempgap > largestGap) ? tempgap : largestGap;
			param.positionLargestGapCenter = perspectiveLeftGoalPost;
		}
		return largestGap;
	}
	
	
	/**
	 * Calculate the Chance of Hitting an Area
	 * 
	 * @param largestGap
	 * @param perspectiveGoalLineLength
	 * @param param TODO
	 * @return Chance between 0 and 1
	 */
	private static double calculateChance(final double largestGap, final double perspectiveGoalLineLength,
			final Parameter param)
	{
		double result;
		if (largestGap < (Geometry.getBallRadius() * 2))
		{
			result = 0;
		} else
		{
			result = (largestGap
					- (distanceScaling * param.rotatedOrigin.subtractNew(param.positionLargestGapCenter).getLength2()
							* shootSpread))
					/ perspectiveGoalLineLength;
		}
		if (result < 0)
		{
			result = 0.0;
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
		private final IVector2	location;
		private final boolean	isRight;
		
		
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
	
	
	private static HashMap<BotID, IVector2> castTigerBotsToBots(final BotIDMapConst<ITrackedBot> foeBots)
	{
		HashMap<BotID, IVector2> bots = new HashMap<BotID, IVector2>();
		
		for (Entry<BotID, ITrackedBot> foeBot : foeBots)
		{
			bots.put(foeBot.getKey(), foeBot.getValue().getPos());
		}
		return bots;
	}
	
}
