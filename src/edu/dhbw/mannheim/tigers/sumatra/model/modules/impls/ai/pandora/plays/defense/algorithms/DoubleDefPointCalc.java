/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 16, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.Triangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This calculator aims to create a defensive pattern which uses two bots to defense versus the ball and a single
 * bot for every other enemy offensive bot.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DoubleDefPointCalc implements IDefensePointCalc
{
	
	
	private static final Logger		log						= Logger.getLogger(DoubleDefPointCalc.class.getName());
	
	private static float					botRadius				= AIConfig.getGeometry().getBotRadius();
	
	private static float					penAreaMargin			= Geometry.getPenaltyAreaMargin();
	
	@Configurable(comment = "Defenders will not drive behind this x value")
	private static float					maxXDefender			= -(AIConfig.getGeometry().getCenterCircleRadius() + (1.5f * AIConfig
																					.getGeometry().getBotRadius()));
	
	@Configurable(comment = "Minimum distance of the double block to the ball")
	private static float					minDistDefenderBall	= 1.5f * botRadius;
	
	private final IDefensePointCalc	childCalc;
	
	
	/**
	 * @param childCalc
	 */
	public DoubleDefPointCalc(final IDefensePointCalc childCalc)
	{
		this.childCalc = childCalc;
	}
	
	
	private boolean containsOurRobot(final Set<BotID> botIds, final int id)
	{
		for (BotID curId : botIds)
		{
			if (curId.getNumber() == id)
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		
		Map<DefenderRole, DefensePoint> optDistr = new HashMap<DefenderRole, DefensePoint>();
		if (defenders.isEmpty())
		{
			return optDistr;
		}
		
		if (EAIControlState.MIXED_TEAM_MODE == frame.getPrevFrame().getPlayStrategy().getAIControlState())
		{
			modifyForMixedteam(frame, defenders, foeBotDataList, optDistr);
		} else
		{
			getDirectShotDistr(frame, defenders, optDistr);
		}
		
		for (FoeBotData curFoeBot : new ArrayList<FoeBotData>(foeBotDataList))
		{
			if (curFoeBot.posessesBall())
			{
				foeBotDataList.remove(curFoeBot);
			}
		}
		
		List<DefenderRole> remainingDefenders = new ArrayList<DefenderRole>();
		
		remainingDefenders.addAll(defenders.stream().filter(el -> !optDistr.keySet().contains(el))
				.collect(Collectors.toList()));
		
		optDistr.putAll(childCalc.getDefenderDistribution(frame, remainingDefenders, foeBotDataList));
		
		optDistr.forEach(
				(defender, defensePoint) -> MovementCon.assertDestinationValid(frame.getWorldFrame(),
						defender.getBotID(), defensePoint, false, false));
		
		return optDistr;
	}
	
	
	private void modifyForMixedteam(final MetisAiFrame frame, final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList, final Map<DefenderRole, DefensePoint> optDistr)
	{
		MultiTeamMessage multiTeamMsg = frame.getTacticalField().getMultiTeamMessage();
		
		if (null != multiTeamMsg)
		{
			Map<BotID, RobotPlan> ourRobots = multiTeamMsg.getRobotPlans(frame.getWorldFrame().getTeamColor());
			TeamPlan teamPlan = multiTeamMsg.getTeamPlan();
			
			List<RobotPlan> robotPlans = teamPlan.getPlansList().stream()
					.filter(robotPlan -> RobotRole.Defense == robotPlan.getRole()).collect(Collectors.toList());
			
			robotPlans = robotPlans.stream()
					.filter(robotPlan -> !containsOurRobot(ourRobots.keySet(), robotPlan.getRobotId()))
					.collect(Collectors.toList());
			
			Triangle directShotTriangle = new Triangle(AIConfig.getGeometry().getGoalOur().getGoalPostLeft(), AIConfig
					.getGeometry().getGoalOur().getGoalPostRight(), frame.getWorldFrame().getBall().getPos());
			IVector2 posVector = null;
			boolean directShotCovered = false;
			
			for (RobotPlan curPlan : robotPlans)
			{
				posVector = new Vector2(curPlan.getNavTarget().getLoc().getX(), curPlan.getNavTarget().getLoc().getY());
				if (directShotTriangle.isPointInShape(posVector))
				{
					directShotCovered = true;
					break;
				}
			}
			
			if (!directShotCovered)
			{
				getDirectShotDistr(frame, defenders, optDistr);
			}
			
			Circle coveredRange = null;
			
			for (FoeBotData curFoeBot : new ArrayList<FoeBotData>(foeBotDataList))
			{
				coveredRange = new Circle(curFoeBot.getFoeBot().getPos(), 5 * AIConfig.getGeometry().getBotRadius());
				
				for (RobotPlan curPlan : robotPlans)
				{
					posVector = new Vector2(curPlan.getNavTarget().getLoc().getX(), curPlan.getNavTarget().getLoc()
							.getY());
					if (coveredRange.isPointInShape(posVector))
					{
						if (foeBotDataList.contains(curFoeBot))
						{
							foeBotDataList.remove(curFoeBot);
						}
						break;
					}
				}
			}
			
		} else
		{
			getDirectShotDistr(frame, defenders, optDistr);
		}
	}
	
	
	private void getDirectShotDistr(final MetisAiFrame frame, final List<DefenderRole> defenders,
			final Map<DefenderRole, DefensePoint> optDistr)
	{
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPosByTime(DefenseCalc.ballLookaheadOfDefenders);
		ballPos = AIConfig.getGeometry().getField().nearestPointInside(ballPos, AIConfig.getGeometry().getBallRadius());
		float width;
		
		IVector2 intersectionBisectorGoal = GeoMath.calculateBisector(ballPos, AIConfig.getGeometry().getGoalOur()
				.getGoalPostLeft(), AIConfig.getGeometry().getGoalOur().getGoalPostRight());
		
		float angleBallLeftGoal = GeoMath.angleBetweenVectorAndVector(AIConfig.getGeometry().getGoalOur()
				.getGoalPostLeft().subtractNew(ballPos), intersectionBisectorGoal.subtractNew(ballPos));
		
		if ((1 == defenders.size()) || !frame.getTacticalField().needTwoForBallBlock())
		{
			width = botRadius;
		} else
		{
			width = 2f * botRadius;
		}
		
		float distBall2DefPoint = width / AngleMath.tan(angleBallLeftGoal);
		distBall2DefPoint = Math.min(distBall2DefPoint, GeoMath.distancePP(ballPos, intersectionBisectorGoal)); // ?
		
		// calculate the valid range on the intersection
		IVector2 minXOnIntersection = AIConfig.getGeometry().getPenaltyAreaOur()
				.nearestPointOutside(intersectionBisectorGoal, ballPos, penAreaMargin);
		IVector2 maxXOnIntersection = AIConfig.getGeometry().getPenaltyAreaOur()
				.nearestPointOutside(GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, minDistDefenderBall));
		maxXOnIntersection = limitX(maxXOnIntersection, intersectionBisectorGoal);
		if (EGameState.STOPPED == frame.getTacticalField().getGameState())
		{
			maxXOnIntersection = modifyForStopSituation(maxXOnIntersection, ballPos, intersectionBisectorGoal);
		}
		maxXOnIntersection = AIConfig.getGeometry().getPenaltyAreaOur()
				.nearestPointOutside(maxXOnIntersection, ballPos, penAreaMargin);
		
		// Create the defense point on the bisector and assure it lies in a valid range
		IVector2 defPoint = GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, distBall2DefPoint);
		
		if (!GeoMath.isVectorBetween(defPoint, minXOnIntersection, maxXOnIntersection))
		{
			if (GeoMath.distancePP(defPoint, minXOnIntersection) < GeoMath.distancePP(defPoint, maxXOnIntersection))
			{
				defPoint = minXOnIntersection;
			} else
			{
				defPoint = maxXOnIntersection;
			}
		}
		
		if ((EGameState.PREPARE_KICKOFF_THEY == frame.getTacticalField().getGameState())
				&& DefenseCalc.forceDoubleBlockAtPenAreaKickoff)
		{
			defPoint = minXOnIntersection;
		}
		
		
		if ((1 == defenders.size()) || !frame.getTacticalField().needTwoForBallBlock())
		{
			optDistr.put(getOptimalSingleDefender(defPoint, defenders), new DefensePoint(defPoint));
		} else
		{
			// calculating the defense points; the bots will cover the posts instead the middle of the goal
			float triangleLength = GeoMath.triangleDistance(frame.getWorldFrame().getBall().getPos(),
					AIConfig.getGeometry()
							.getGoalOur()
							.getGoalPostLeft(), AIConfig.getGeometry().getGoalOur()
							.getGoalPostRight(), defPoint);
			
			if ((EGameState.PREPARE_KICKOFF_THEY == frame.getTacticalField().getGameState())
					&& DefenseCalc.forceDoubleBlockAtPenAreaKickoff)
			{
				triangleLength = 4 * AIConfig.getGeometry().getBotRadius();
			}
			
			float dist2defPoint = Math.max(AIConfig.getGeometry().getBallRadius(),
					(triangleLength / 2) - AIConfig.getGeometry().getBotRadius());
			
			IVector2 defPointA = defPoint.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint)
					.turn((float) Math.PI / 2));
			IVector2 defPointB = defPoint.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint)
					.turn((float) -Math.PI / 2));
			
			defPointA = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(defPointA, Geometry.getPenaltyAreaMargin());
			defPointB = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(defPointB, Geometry.getPenaltyAreaMargin());
			
			optDistr.putAll(getOptimalDoubleDefenders(defPointA, defPointB, defenders));
		}
	}
	
	
	private DefenderRole getOptimalSingleDefender(final IVector2 defPoint, final List<DefenderRole> defenders)
	{
		DefenderRole optDefender = null;
		float optDefenderTime = Float.MAX_VALUE;
		float curTime;
		
		for (DefenderRole curDefender : defenders)
		{
			
			BangBangTrajectory2D pathToDest = TrajectoryGenerator.generatePositionTrajectory(
					curDefender.getBot(), defPoint);
			curTime = pathToDest.getTotalTime();
			
			if (curTime < optDefenderTime)
			{
				optDefender = curDefender;
				optDefenderTime = curTime;
			}
		}
		
		return optDefender;
	}
	
	
	private Map<DefenderRole, DefensePoint> getOptimalDoubleDefenders(final IVector2 defPointA,
			final IVector2 defPointB, final List<DefenderRole> defenders)
	{
		Map<DefenderRole, DefensePoint> curOptDistr = new HashMap<DefenderRole, DefensePoint>(2);
		
		// get the defenders driving the shortest accumulated distance
		float curOptTime = Float.MAX_VALUE;
		float curTime = Float.MAX_VALUE;
		List<DefenderRole> closestDefender = new ArrayList<DefenderRole>();
		for (DefenderRole defender1 : defenders.subList(0, defenders.size() - 1))
		{
			for (DefenderRole defender2 : defenders.subList(defenders.indexOf(defender1) + 1, defenders.size()))
			{
				curTime = TrajectoryGenerator.generatePositionTrajectory(
						defender1.getBot(), defPointA).getTotalTime() +
						TrajectoryGenerator.generatePositionTrajectory(
								defender2.getBot(), defPointB).getTotalTime();
				
				if (curTime < curOptTime)
				{
					closestDefender.clear();
					closestDefender.add(defender1);
					closestDefender.add(defender2);
					curOptTime = curTime;
				}
				
				curTime = TrajectoryGenerator.generatePositionTrajectory(
						defender2.getBot(), defPointA).getTotalTime() +
						TrajectoryGenerator.generatePositionTrajectory(
								defender1.getBot(), defPointB).getTotalTime();
				
				if (curTime < curOptTime)
				{
					closestDefender.clear();
					closestDefender.add(defender2);
					closestDefender.add(defender1);
					curOptTime = curTime;
				}
			}
		}
		
		// to avoid the defenders crossing their paths the defenders and the defense points will be mapped according to
		// their y coordinates
		if (((defPointA.y() > defPointB.y()) && (closestDefender.get(0).getPos().y() > closestDefender
				.get(1).getPos().y()))
				||
				((defPointA.y() < defPointB.y()) && (closestDefender.get(0).getPos().y() < closestDefender
						.get(1).getPos().y())))
		{
			
			curOptDistr.put(closestDefender.get(0), new DefensePoint(defPointA));
			curOptDistr.put(closestDefender.get(1), new DefensePoint(defPointB));
		} else
		{
			
			curOptDistr.put(closestDefender.get(0), new DefensePoint(defPointB));
			curOptDistr.put(closestDefender.get(1), new DefensePoint(defPointA));
		}
		
		return curOptDistr;
	}
	
	
	/**
	 * TODO: implement a universal version for a point on a vector
	 * 
	 * @param point
	 * @return
	 */
	private IVector2 limitX(IVector2 point, final IVector2 intersectionBisectorGoal)
	{
		if (point.x() > maxXDefender)
		{
			try
			{
				IVector2 intersect = GeoMath.intersectionPoint(intersectionBisectorGoal,
						intersectionBisectorGoal.subtractNew(point), new Vector2(
								maxXDefender, 0), new Vector2(0, AVector2.Y_AXIS.y()));
				point = (
						(GeoMath.stepAlongLine(intersect, intersectionBisectorGoal,
								-AIConfig.getGeometry().getBotRadius())));
			} catch (MathException err)
			{
				// unlikely :P
				log.error("Unlikely error happended in " + DoubleDefPointCalc.class.getName());
			}
		}
		return point;
	}
	
	
	private IVector2 modifyForStopSituation(final IVector2 dest, final IVector2 ballPos,
			final IVector2 intersectionBisectorGoal)
	{
		float bot2ballStopDist = AIConfig.getGeometry().getBotToBallDistanceStop();
		float botRadius = AIConfig.getGeometry().getBotRadius();
		
		Circle stopCircle = new Circle(ballPos, bot2ballStopDist);
		if (stopCircle.isPointInShape(dest, botRadius))
		{
			return GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, bot2ballStopDist + botRadius);
		}
		return dest;
	}
}
