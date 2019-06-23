/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 16, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.triangle.Triangle;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * This calculator aims to create a defensive pattern which uses two bots to defense versus the ball and a single
 * bot for every other enemy offensive bot.
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DoubleDefPointCalc implements IDefensePointCalc
{
	private static final Logger		log				= Logger
			.getLogger(
					DoubleDefPointCalc.class.getName());
	
	private static double				botRadius		= Geometry.getBotRadius();
	
	@Configurable(comment = "Defenders will not drive behind this x value")
	private static double				maxXDefender	= -(Geometry.getCenterCircleRadius()
			+ (1.5f * Geometry.getBotRadius()));
	
	private final IDefensePointCalc	childCalc;
	
	
	static
	{
		ConfigRegistration.registerClass("defensive", DoubleDefPointCalc.class);
	}
	
	
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
	
	
	/*
	 * Ignoring forbidden points. Defending against the ball is the most important task of the defense.
	 */
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		
		ITacticalField tacticalField = frame.getTacticalField();
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
		
		optDistr.keySet().forEach(defRole -> tacticalField.addCrucialDefender(defRole.getBotID()));
		
		List<DefenderRole> remainingDefenders = new ArrayList<DefenderRole>();
		
		remainingDefenders.addAll(defenders.stream().filter(el -> !optDistr.keySet().contains(el))
				.collect(Collectors.toList()));
		
		optDistr.putAll(childCalc.getDefenderDistribution(frame, remainingDefenders, foeBotDataList));
		
		return optDistr;
	}
	
	
	private void modifyForMixedteam(final MetisAiFrame frame, final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList, final Map<DefenderRole, DefensePoint> optDistr)
	{
		MultiTeamMessage multiTeamMsg = frame.getMultiTeamMessage();
		
		if (null != multiTeamMsg)
		{
			Map<BotID, RobotPlan> ourRobots = multiTeamMsg.getRobotPlans(frame.getWorldFrame().getTeamColor());
			TeamPlan teamPlan = multiTeamMsg.getTeamPlan();
			
			List<RobotPlan> robotPlans = teamPlan.getPlansList().stream()
					.filter(robotPlan -> RobotRole.Defense == robotPlan.getRole()).collect(Collectors.toList());
			
			robotPlans = robotPlans.stream()
					.filter(robotPlan -> !containsOurRobot(ourRobots.keySet(), robotPlan.getRobotId()))
					.collect(Collectors.toList());
			
			Triangle directShotTriangle = new Triangle(Geometry.getGoalOur().getGoalPostLeft(),
					Geometry.getGoalOur().getGoalPostRight(), frame.getWorldFrame().getBall().getPos());
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
				coveredRange = new Circle(curFoeBot.getFoeBot().getPos(), 5 * Geometry.getBotRadius());
				
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
		double minDistDefenderBall = ZoneDefenseCalc.getMinDistanceDef2Ball();
		
		IVector2 ballPos = DefenseAux.getBallPosDefense(frame.getWorldFrame().getBall());
		ballPos = Geometry.getField().nearestPointInside(ballPos, Geometry.getBallRadius());
		double penaltyAreaMargin = DefenseAux.penAreaMargin();
		double width;
		
		IVector2 intersectionBisectorGoal = GeoMath.calculateBisector(ballPos, Geometry.getGoalOur()
				.getGoalPostLeft(), Geometry.getGoalOur().getGoalPostRight());
		
		double angleBallLeftGoal = GeoMath.angleBetweenVectorAndVector(Geometry.getGoalOur()
				.getGoalPostLeft().subtractNew(ballPos), intersectionBisectorGoal.subtractNew(ballPos));
		
		if ((1 == defenders.size()))
		{
			width = botRadius;
		} else
		{
			width = 2 * botRadius;
		}
		
		double distBall2DefPoint = width / AngleMath.tan(angleBallLeftGoal);
		distBall2DefPoint = Math.min(distBall2DefPoint, GeoMath.distancePP(ballPos, intersectionBisectorGoal)); // ?
		
		// calculate the valid range on the intersection
		IVector2 minXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(intersectionBisectorGoal, ballPos, penaltyAreaMargin);
		IVector2 maxXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, minDistDefenderBall));
		maxXOnIntersection = limitX(maxXOnIntersection, intersectionBisectorGoal);
		if (EGameStateTeam.STOPPED == frame.getTacticalField().getGameState())
		{
			maxXOnIntersection = modifyForStopSituation(maxXOnIntersection, ballPos, intersectionBisectorGoal);
		}
		maxXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(maxXOnIntersection, ballPos, penaltyAreaMargin);
		
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
		
		
		if ((1 == defenders.size()))
		{
			optDistr.put(getOptimalSingleDefender(defPoint, defenders), new DefensePoint(defPoint, ballPos));
		} else
		{
			// calculating the defense points; the bots will cover the posts instead the middle of the goal
			double triangleLength = GeoMath.triangleDistance(frame.getWorldFrame().getBall().getPos(),
					Geometry
							.getGoalOur()
							.getGoalPostLeft(),
					Geometry.getGoalOur()
							.getGoalPostRight(),
					defPoint);
			
			double dist2defPoint = Math.max(Geometry.getBallRadius(),
					(triangleLength / 2.0) - Geometry.getBotRadius());
			
			IVector2 defPointA = defPoint.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint)
					.turn(Math.PI / 2.0));
			IVector2 defPointB = defPoint.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint)
					.turn(-Math.PI / 2.0));
			
			defPointA = Geometry.getPenaltyAreaOur()
					.nearestPointOutside(defPointA, ballPos, penaltyAreaMargin);
			defPointB = Geometry.getPenaltyAreaOur()
					.nearestPointOutside(defPointB, ballPos, penaltyAreaMargin);
			
			optDistr.putAll(getOptimalDoubleDefenders(defPointA, defPointB, ballPos, defenders));
		}
	}
	
	
	private DefenderRole getOptimalSingleDefender(final IVector2 defPoint, final List<DefenderRole> defenders)
	{
		DefenderRole optDefender = null;
		double optDefenderTime = Double.MAX_VALUE;
		double curTime;
		
		for (DefenderRole curDefender : defenders)
		{
			
			BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(
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
			final IVector2 defPointB, final IVector2 ballPos, final List<DefenderRole> defenders)
	{
		Map<DefenderRole, DefensePoint> curOptDistr = new HashMap<DefenderRole, DefensePoint>(2);
		
		// get the defenders driving the shortest accumulated distance
		double curOptTime = Double.MAX_VALUE;
		double curTime = Double.MAX_VALUE;
		List<DefenderRole> closestDefender = new ArrayList<DefenderRole>();
		for (DefenderRole defender1 : defenders.subList(0, defenders.size() - 1))
		{
			for (DefenderRole defender2 : defenders.subList(defenders.indexOf(defender1) + 1, defenders.size()))
			{
				curTime = new TrajectoryGenerator().generatePositionTrajectory(
						defender1.getBot(), defPointA).getTotalTime() +
						new TrajectoryGenerator().generatePositionTrajectory(
								defender2.getBot(), defPointB).getTotalTime();
				
				if (curTime < curOptTime)
				{
					closestDefender.clear();
					closestDefender.add(defender1);
					closestDefender.add(defender2);
					curOptTime = curTime;
				}
				
				curTime = new TrajectoryGenerator().generatePositionTrajectory(
						defender2.getBot(), defPointA).getTotalTime() +
						new TrajectoryGenerator().generatePositionTrajectory(
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
			
			curOptDistr.put(closestDefender.get(0), new DefensePoint(defPointA, ballPos));
			curOptDistr.put(closestDefender.get(1), new DefensePoint(defPointB, ballPos));
		} else
		{
			
			curOptDistr.put(closestDefender.get(0), new DefensePoint(defPointB, ballPos));
			curOptDistr.put(closestDefender.get(1), new DefensePoint(defPointA, ballPos));
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
								maxXDefender, 0),
						new Vector2(0, AVector2.Y_AXIS.y()));
				point = ((GeoMath.stepAlongLine(intersect, intersectionBisectorGoal,
						-Geometry.getBotRadius())));
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
		double bot2ballStopDist = Geometry.getBotToBallDistanceStop();
		double botRadius = Geometry.getBotRadius();
		
		Circle stopCircle = new Circle(ballPos, bot2ballStopDist);
		if (stopCircle.isPointInShape(dest, botRadius))
		{
			return GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, bot2ballStopDist + botRadius);
		}
		return dest;
	}
}
