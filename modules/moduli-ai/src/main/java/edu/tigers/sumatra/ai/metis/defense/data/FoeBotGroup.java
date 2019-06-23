/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): Felix Bayer <bayer.fel@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.StopDefPointCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers.DefMath;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Felix Bayer <bayer.fel@googlemail.com>
 */
public class FoeBotGroup
{
	@SuppressWarnings("unused")
	private static final Logger									log						= Logger
			.getLogger(FoeBotGroup.class.getName());
	
	/**  */
	public static final Comparator<? super FoeBotGroup>	PRIORITY					= new PriorityComparator();
	/**  */
	public static final Comparator<? super FoeBotGroup>	ANGLE						= new AngleComparator();
	
	private List<FoeBotData>										member					= new ArrayList<>();
	
	private int															nAssignedDefender		= -1;
	
	private boolean													forcePossessesBall	= false;
	
	// flag to indicate this group is the split part of the ball possessing group
	private boolean													wasBallPossessing		= false;
	
	private boolean													invertPossessesBall	= false;
	
	private boolean													isFirst					= false;
	private boolean													isLast					= false;
	
	
	/**
	 * @param invertPossessesBall
	 */
	public FoeBotGroup(final boolean invertPossessesBall)
	{
		this.invertPossessesBall = invertPossessesBall;
	}
	
	
	/**
	 * @return the foeBotData
	 */
	public List<FoeBotData> getMember()
	{
		return member;
	}
	
	
	/**
	 * @param newMember
	 */
	public void addMember(final FoeBotData newMember)
	{
		member.add(newMember);
	}
	
	
	/**
	 * @return the nDesDefenders
	 */
	public int nMember()
	{
		return member.size();
	}
	
	
	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return nMember() == 0;
	}
	
	
	/**
	 * @return
	 */
	public boolean possessesBall()
	{
		if (wasBallPossessing)
		{
			return false;
		}
		
		if (forcePossessesBall)
		{
			return true;
		}
		
		for (FoeBotData curMember : member)
		{
			if (curMember.posessesBall())
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * @return
	 */
	public boolean containsBestRedirector()
	{
		for (FoeBotData curMember : member)
		{
			if (curMember.isBestRedirector())
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * @return
	 */
	public int nMinDefender()
	{
		return 1;
	}
	
	
	/**
	 * @param gameState
	 * @param worldFrame
	 * @return
	 */
	public int nDesDefenders(final EGameStateTeam gameState, final WorldFrame worldFrame)
	{
		int nDesDefenders = nMember();
		
		if (gameState == EGameStateTeam.PREPARE_KICKOFF_THEY)
		{
			nDesDefenders = Math.min(nDesDefenders, DefenseAux.maxDefenderGroupKickoff);
		} else
		{
			nDesDefenders = Math.min(nDesDefenders, DefenseAux.maxDefenderGroup);
		}
		
		if (possessesBall())
		{
			int nForDirectBlock = 1;
			if (DefenseAux.useDoubleDefender(gameState, worldFrame))
			{
				nForDirectBlock = 2;
				
			}
			nDesDefenders = Math.max(nDesDefenders, nForDirectBlock);
		}
		
		nDesDefenders = Math.max(nDesDefenders, nMinDefender());
		
		return nDesDefenders;
	}
	
	
	/**
	 * @param gameState
	 * @param worldFrame
	 * @return
	 */
	public int nMaxDefenders(final EGameStateTeam gameState, final WorldFrame worldFrame)
	{
		return nDesDefenders(gameState, worldFrame);
	}
	
	
	/**
	 * @return the nAssignedDefender
	 */
	public int getNAssignedDefender()
	{
		return nAssignedDefender;
	}
	
	
	/**
	 * @param nAssignedDefender the nAssignedDefender to set
	 */
	public void setNAssignedDefender(final int nAssignedDefender)
	{
		this.nAssignedDefender = nAssignedDefender;
	}
	
	
	/**
	 * @param defenders
	 * @param gameState
	 * @param tacticalField
	 * @param frame
	 * @return
	 */
	public Map<DefenderRole, DefensePoint> getDefenseDistribution(final List<DefenderRole> defenders,
			final EGameStateTeam gameState, final ITacticalField tacticalField, final WorldFrame frame)
	{
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		
		if (nMember() == 1)
		{
			distribution = getMan2ManMarker(defenders, member.get(0), gameState, tacticalField, frame);
		} else
		{
			distribution = getGroupDefDistribution(defenders, gameState, tacticalField, frame);
		}
		
		return distribution;
	}
	
	
	/**
	 * @param defenders
	 * @param gameState
	 * @return
	 */
	private Map<DefenderRole, DefensePoint> getGroupDefDistribution(final List<DefenderRole> defenders,
			final EGameStateTeam gameState, final ITacticalField tacticalField, final WorldFrame frame)
	{
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		
		List<DefensePoint> defPoints = getGroupDefPoints();
		List<DefenderRole> remainingDefenders = new ArrayList<>(defenders);
		
		if (nAssignedDefender < nMember())
		{
			for (DefensePoint defPoint : defPoints)
			{
				
				double curOptCost = Double.MAX_VALUE;
				DefenderRole curOptDefender = null;
				
				for (DefenderRole defender : remainingDefenders)
				{
					double curCost;
					if (!DefenseAux.useTrajectoriesForPathCosts)
					{
						BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(
								defender.getBot(),
								defPoint);
						curCost = pathToDest.getTotalTime();
					} else
					{
						curCost = DefMath.getDefMovementCost(frame, defender.getBot(), defPoint, gameState);
					}
					
					if (curCost < curOptCost)
					{
						curOptDefender = defender;
						curOptCost = curCost;
					}
				}
				
				distribution.put(curOptDefender, defPoint);
				remainingDefenders.remove(curOptDefender);
			}
		} else
		{
			List<FoeBotData> foeBots = new ArrayList<>(member);
			foeBots.sort(FoeBotData.DANGER_COMPARATOR);
			
			for (FoeBotData foeBot : foeBots)
			{
				Map<DefenderRole, DefensePoint> localDistr = getMan2ManMarker(remainingDefenders, foeBot, gameState,
						tacticalField, frame);
				
				distribution.putAll(localDistr);
				remainingDefenders.remove(localDistr.keySet());
			}
		}
		
		return distribution;
	}
	
	
	/**
	 * @param defenders
	 * @param gameState
	 * @return
	 */
	private Map<DefenderRole, DefensePoint> getMan2ManMarker(final List<DefenderRole> defenders, final FoeBotData foeBot,
			final EGameStateTeam gameState, final ITacticalField tacticalField, final WorldFrame frame)
	{
		Map<DefenderRole, DefensePoint> distribution = new HashMap<>();
		
		double curOptCost = Double.MAX_VALUE;
		DefenderRole curOptDefender = null;
		// IVector2 defPointPos = null;
		DefensePoint curOptDefPoint = null;
		
		for (DefenderRole defender : defenders)
		{
			DefensePoint localDefPoint = new DefensePoint(DefMath.calcNearestDefPoint(foeBot, defender.getBot()), foeBot);
			
			double curCost;
			if (!DefenseAux.useTrajectoriesForPathCosts)
			{
				BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(defender.getBot(),
						localDefPoint);
				curCost = pathToDest.getTotalTime();
				// tacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE).add(new DrawableTrajectoryPath(pathToDest));
			} else
			{
				curCost = DefMath.getDefMovementCost(frame, defender.getBot(), localDefPoint, gameState);
			}
			
			if (curCost < curOptCost)
			{
				curOptDefender = defender;
				curOptDefPoint = localDefPoint;
				curOptCost = curCost;
			}
		}
		
		curOptDefPoint = optimizeMan2Man(curOptDefPoint, curOptDefender, foeBot, gameState);
		curOptDefPoint = new DefensePoint(Geometry.getPenaltyAreaOur().nearestPointOutside(curOptDefPoint,
				Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin()), foeBot);
		
		distribution.put(curOptDefender, curOptDefPoint);
		return distribution;
	}
	
	
	/**
	 * @return
	 */
	private DefensePoint optimizeMan2Man(final DefensePoint curDefPoint, final DefenderRole defender,
			final FoeBotData foeBot, final EGameStateTeam gameState)
	{
		DefensePoint newDefPoint = new DefensePoint(curDefPoint);
		
		if (isOuter() && DefenseAux.outerDefendersPassiveAgressive)
		{
			if ((GeoMath.distancePP(newDefPoint, defender.getPos()) < DefenseAux.nearDefPointDist)
					&& !DefenseAux.isKickoffSituation(gameState))
			{
				newDefPoint = DefenseAux.getMan2ManDefPoint(foeBot, defender);
			}
		} else if (!isOuter())
		{
			if (GeoMath.distancePP(newDefPoint, defender.getPos()) < DefenseAux.nearDefPointDist)
			{
				if (DefenseAux.innerPassiveAgressive)
				{
					newDefPoint = new DefensePoint(foeBot.getBall2botNearestToBot(), foeBot);
				} else if (DefenseAux.innerMan2ManAtPenArea)
				{
					newDefPoint = new DefensePoint(foeBot.getBot2goalNearestToGoal(), foeBot);
				} else
				{
					List<IntersectionPoint> intersecs = foeBot.getBot2goalIntersecsBall2bot();
					intersecs.sort(IntersectionPoint.DIST_TO_GOAL);
					
					if (intersecs.size() > 0)
					{
						newDefPoint = new DefensePoint(intersecs.get(0), foeBot);
					} else
					{
						newDefPoint = new DefensePoint(foeBot.getBot2goalNearestToBot(), foeBot);
					}
					
				}
			}
			
			IVector2 foeBotPos = foeBot.getFoeBot().getPos();
			IVector2 bot2Goal = foeBot.getBot2goal();
			
			if (GeoMath.distancePP(foeBotPos.addNew(foeBot.getBot2goal()),
					newDefPoint) > DefenseAux.maxDistInnerDefender2goal)
			{
				newDefPoint = new DefensePoint(
						foeBotPos.addNew(bot2Goal.scaleToNew(bot2Goal.getLength() - DefenseAux.maxDistInnerDefender2goal)),
						foeBot);
			}
		}
		
		return newDefPoint;
	}
	
	
	private List<DefensePoint> getGroupDefPoints()
	{
		List<DefensePoint> defPoints = new ArrayList<>();
		int nDefender = nAssignedDefender;
		
		double pi = Math.PI;
		IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
		
		double minDefRadius = AngleDefenseData.getMinDefRadius();
		Arc defenseArcMin = new Arc(goalCenter, minDefRadius, -pi / 2, pi);
		double botWidthAngle = AngleDefenseData.getBotWidthAngle();
		
		FoeBotData firstFoeBot = getMember().get(0);
		FoeBotData lastFoeBot = getMember().get(nMember() - 1);
		
		double sectionAngle = (firstFoeBot.getGoalAngle() + lastFoeBot.getGoalAngle()) / 2;
		
		double startAngle = -1;
		if ((nDefender % 2) != 0)
		{
			startAngle = sectionAngle - ((nDefender / 2) * botWidthAngle);
		} else
		{
			startAngle = sectionAngle - ((((nDefender / 2) - 1) * botWidthAngle)) - (0.5 * botWidthAngle);
		}
		
		for (int i = 0; i < nDefender; ++i)
		{
			double angle = (startAngle + (i * botWidthAngle)) + pi + (pi / 2);
			
			IVector2 helperLine = Geometry.getGoalTheir().getGoalCenter().turnNew(-angle);
			
			ILine line = new Line(goalCenter, helperLine);
			
			List<IVector2> intersecs = defenseArcMin.lineIntersections(line);
			
			if (intersecs.size() != 1)
			{
				log.error("Interesting number of defense points. I do not know what to do!");
				continue;
			}
			
			IVector2 defPos = intersecs.get(0);
			defPos = Geometry.getPenaltyAreaOur().nearestPointOutside(defPos,
					Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin());
			
			defPoints.add(new DefensePoint(defPos));
		}
		
		return defPoints;
	}
	
	
	/**
	 * @param frame
	 * @param gameState
	 * @param ballPos
	 * @param defenders
	 * @return
	 */
	public Map<DefenderRole, DefensePoint> getBallBlockingDefPoints(final WorldFrame frame,
			final EGameStateTeam gameState,
			IVector2 ballPos, final List<DefenderRole> defenders)
	{
		if (!possessesBall())
		{
			log.warn("Blocking ball though not belonging to ball protecting group. This is not good!");
		}
		
		if (nAssignedDefender > 2)
		{
			log.warn(
					"More than two defenders assigned to the ball blocking. This should not happen anymore!Undefined (untested) behaviour!");
		}
		
		Map<DefenderRole, DefensePoint> distr = null;
		
		if (nMember() == 1)
		{
			ballPos = getMember().get(0).getFoeBot().getBotKickerPosByTime(DefenseAux.foeLookAheadDefenders);
		}
		
		Vector2 singleDefPos = new Vector2();
		Vector2 doubleDefPosA = new Vector2();
		Vector2 doubleDefPosB = new Vector2();
		
		getSingleDefenderPosition(gameState, ballPos, singleDefPos);
		getDoubleDefenderPositions(gameState, ballPos, doubleDefPosA, doubleDefPosB);
		
		if (nAssignedDefender == 1)
		{
			
			distr = getOptimalSingleDefender(singleDefPos, ballPos, defenders, frame, gameState);
		} else
		{
			distr = getOptimalDoubleDefenders(doubleDefPosA, doubleDefPosB, singleDefPos, ballPos, defenders, frame,
					gameState);
		}
		
		return distr;
	}
	
	
	/**
	 * @param gameState
	 * @param ballPosOrig
	 * @param defPoint
	 */
	private void getSingleDefenderPosition(final EGameStateTeam gameState, final IVector2 ballPosOrig,
			final Vector2 defPoint)
	{
		double penAreaMargin = Geometry.getPenaltyAreaMargin();
		double minDistanceDef2Ball = ZoneDefenseCalc.getMinDistanceDef2Ball();
		
		IVector2 ballPos = moveBallPosInsideField(ballPosOrig);
		double width = Geometry.getBotRadius();
		
		IVector2 ourGoalPostLeft = Geometry.getGoalOur().getGoalPostLeft();
		IVector2 ourGoalPostRight = Geometry.getGoalOur().getGoalPostRight();
		
		IVector2 intersectionBisectorGoal = GeoMath.calculateBisector(ballPos, ourGoalPostLeft, ourGoalPostRight);
		
		double angleBallLeftGoal = GeoMath.angleBetweenVectorAndVector(ourGoalPostLeft.subtractNew(ballPos),
				intersectionBisectorGoal.subtractNew(ballPos));
		
		double distBall2DefPoint = width / AngleMath.tan(angleBallLeftGoal);
		// to not land outside of the field behind our goal
		distBall2DefPoint = Math.min(distBall2DefPoint, GeoMath.distancePP(ballPos, intersectionBisectorGoal));
		
		// calculate the valid range on the intersection
		IVector2 minXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(intersectionBisectorGoal, ballPos, penAreaMargin);
		IVector2 maxXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, minDistanceDef2Ball));
		maxXOnIntersection = limitX(maxXOnIntersection, intersectionBisectorGoal);
		maxXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(maxXOnIntersection, ballPos, penAreaMargin);
		
		// Create the defense point on the bisector and assure it lies in a valid range
		defPoint.set(GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, distBall2DefPoint));
		
		if (!GeoMath.isVectorBetween(defPoint, minXOnIntersection, maxXOnIntersection))
		{
			if (GeoMath.distancePP(defPoint, minXOnIntersection) < GeoMath.distancePP(defPoint, maxXOnIntersection))
			{
				defPoint.set(minXOnIntersection);
			} else
			{
				defPoint.set(maxXOnIntersection);
			}
		}
	}
	
	
	/**
	 * @param gameState
	 * @param ballPos
	 * @param defPointA
	 * @param defPointB
	 */
	private void getDoubleDefenderPositions(final EGameStateTeam gameState, final IVector2 ballPos,
			final Vector2 defPointA, final Vector2 defPointB)
	{
		double botRadius = Geometry.getBotRadius();
		
		double penAreaMargin = DefenseAux.penAreaMargin();
		double minDistanceDef2Ball = ZoneDefenseCalc.getMinDistanceDef2Ball();
		
		IVector2 ourGoalPostLeft = Geometry.getGoalOur().getGoalPostLeft();
		IVector2 ourGoalPostRight = Geometry.getGoalOur().getGoalPostRight();
		
		double width = 2 * botRadius;
		
		IVector2 intersectionBisectorGoal = GeoMath.calculateBisector(ballPos, ourGoalPostLeft, ourGoalPostRight);
		
		double angleBallLeftGoal = GeoMath.angleBetweenVectorAndVector(ourGoalPostLeft.subtractNew(ballPos),
				intersectionBisectorGoal.subtractNew(ballPos));
		
		double distBall2DefPoint = width / AngleMath.tan(angleBallLeftGoal);
		// to not land outside of the field behind our goal
		distBall2DefPoint = Math.min(distBall2DefPoint, GeoMath.distancePP(ballPos, intersectionBisectorGoal));
		
		// calculate the valid range on the intersection
		IVector2 minXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(intersectionBisectorGoal, ballPos, penAreaMargin);
		IVector2 maxXOnIntersection = Geometry.getPenaltyAreaOur()
				.nearestPointOutside(GeoMath.stepAlongLine(ballPos, intersectionBisectorGoal, minDistanceDef2Ball));
		maxXOnIntersection = limitX(maxXOnIntersection, intersectionBisectorGoal);
		if (EGameStateTeam.STOPPED == gameState)
		{
			maxXOnIntersection = modifyForStopSituation(maxXOnIntersection, ballPos, intersectionBisectorGoal);
		}
		maxXOnIntersection = Geometry.getPenaltyAreaOur()
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
		
		// calculating the defense points; the bots will cover the posts instead the middle of the goal
		double triangleLength = GeoMath.triangleDistance(ballPos, ourGoalPostLeft, ourGoalPostRight, defPoint);
		double dist2defPoint = Math.max(Geometry.getBotRadius() + (0.5 * Geometry.getBallRadius()),
				(triangleLength / 2.0) - Geometry.getBotRadius());
		
		defPointA.set(defPoint
				.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint).turn(Math.PI / 2.0)));
		defPointB.set(defPoint
				.addNew(intersectionBisectorGoal.subtractNew(ballPos).scaleToNew(dist2defPoint).turn(-Math.PI / 2.0)));
		
		defPointA
				.set(Geometry.getPenaltyAreaOur().nearestPointOutside(defPointA, ballPos, Geometry.getPenaltyAreaMargin()));
		defPointB
				.set(Geometry.getPenaltyAreaOur().nearestPointOutside(defPointB, ballPos, Geometry.getPenaltyAreaMargin()));
		
		if (EGameStateTeam.STOPPED == gameState)
		{
			IVector2 a2b = null;
			double botDiameter = botRadius;
			double lengthA2b;
			
			defPointA.set(StopDefPointCalc.modifyPositionForStopSituation(ballPos, defPointA, ballPos));
			defPointB.set(StopDefPointCalc.modifyPositionForStopSituation(ballPos, defPointB, ballPos));
			
			a2b = defPointB.subtractNew(defPointA);
			lengthA2b = a2b.getLength2();
			if (lengthA2b < botDiameter)
			{
				
				IVector2 center = defPointA.addNew(a2b.scaleToNew(0.5 * lengthA2b));
				
				defPointA.set(center.addNew(a2b.scaleToNew(-botRadius)));
				defPointB.set(center.addNew(a2b.scaleToNew(botRadius)));
			}
		}
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
	
	
	/**
	 * @param point
	 * @return
	 */
	private IVector2 limitX(IVector2 point, final IVector2 intersectionBisectorGoal)
	{
		double maxXDirectShotDefender = ZoneDefenseCalc.getMaxXDirectShotDefender();
		
		if (point.x() > maxXDirectShotDefender)
		{
			try
			{
				IVector2 intersect = GeoMath.intersectionPoint(intersectionBisectorGoal,
						intersectionBisectorGoal.subtractNew(point), new Vector2(
								maxXDirectShotDefender, 0),
						new Vector2(0, AVector2.Y_AXIS.y()));
				point = ((GeoMath.stepAlongLine(intersect, intersectionBisectorGoal,
						-Geometry.getBotRadius())));
			} catch (MathException err)
			{
				// unlikely :P
				log.error("Unlikely error happended");
			}
		}
		return point;
	}
	
	
	/**
	 * @param ballPos
	 */
	private static IVector2 moveBallPosInsideField(final IVector2 ballPos)
	{
		return Geometry.getField().nearestPointInside(ballPos, Geometry.getBallRadius());
	}
	
	
	/**
	 * @param defPoint
	 * @param ballPos
	 * @param defenders
	 * @param frame
	 * @param gameState
	 * @return
	 */
	private static Map<DefenderRole, DefensePoint> getOptimalSingleDefender(final IVector2 defPoint,
			final IVector2 ballPos,
			final List<DefenderRole> defenders, final WorldFrame frame, final EGameStateTeam gameState)
	{
		Map<DefenderRole, DefensePoint> defenderDistr = new HashMap<>();
		
		if (defenders.size() < 1)
		{
			return defenderDistr;
		}
		
		DefenderRole optDefender = null;
		double optDefenderTime = Double.MAX_VALUE;
		double curTime;
		DefensePoint defPointToSet = new DefensePoint(defPoint, ballPos);
		
		for (DefenderRole curDefender : defenders)
		{
			
			if (!DefenseAux.useTrajectoriesForPathCosts)
			{
				BangBangTrajectory2D pathToDest = new TrajectoryGenerator().generatePositionTrajectory(curDefender.getBot(),
						defPoint);
				curTime = pathToDest.getTotalTime();
			} else
			{
				curTime = DefMath.getDefMovementCost(frame, curDefender.getBot(), defPoint, gameState);
			}
			
			if (curTime < optDefenderTime)
			{
				optDefender = curDefender;
				optDefenderTime = curTime;
			}
			break;
		}
		
		if (optDefender != null)
		{
			defenderDistr.put(optDefender, defPointToSet);
		}
		
		return defenderDistr;
	}
	
	
	/**
	 * @param defPointA
	 * @param defPointB
	 * @param defPointSingleDefender
	 * @param ballPos
	 * @param defenders
	 * @return
	 */
	private static Map<DefenderRole, DefensePoint> getOptimalDoubleDefenders(final IVector2 defPointA,
			final IVector2 defPointB, final IVector2 defPointSingleDefender, final IVector2 ballPos,
			final List<DefenderRole> defenders, final WorldFrame frame, final EGameStateTeam gameState)
	{
		Map<DefenderRole, DefensePoint> curOptDistr = new HashMap<>();
		
		// get the defenders driving the shortest accumulated distance
		double curOptTime = Double.MAX_VALUE;
		double curTime = Double.MAX_VALUE;
		List<DefenderRole> closestDefender = new ArrayList<>();
		for (DefenderRole defender1 : defenders.subList(0, defenders.size() - 1))
		{
			for (DefenderRole defender2 : defenders.subList(defenders.indexOf(defender1) + 1, defenders.size()))
			{
				if (!DefenseAux.useTrajectoriesForPathCosts)
				{
					curTime = new TrajectoryGenerator().generatePositionTrajectory(defender1.getBot(),
							defPointA).getTotalTime()
							+ new TrajectoryGenerator().generatePositionTrajectory(defender2.getBot(), defPointB)
									.getTotalTime();
				} else
				{
					curTime = DefMath.getDefMovementCost(frame, defender1.getBot(), defPointA, gameState)
							+ DefMath.getDefMovementCost(frame, defender2.getBot(), defPointB, gameState);
				}
				
				if (curTime < curOptTime)
				{
					closestDefender.clear();
					closestDefender.add(defender1);
					closestDefender.add(defender2);
					curOptTime = curTime;
				}
				
				if (!DefenseAux.useTrajectoriesForPathCosts)
				{
					curTime = new TrajectoryGenerator().generatePositionTrajectory(defender2.getBot(),
							defPointA).getTotalTime()
							+ new TrajectoryGenerator().generatePositionTrajectory(defender1.getBot(), defPointB)
									.getTotalTime();
				} else
				{
					curTime = DefMath.getDefMovementCost(frame, defender2.getBot(), defPointA, gameState)
							+ DefMath.getDefMovementCost(frame, defender1.getBot(), defPointB, gameState);
				}
				
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
		
		modifyDirectShotDistributionForOneBotFarAway(frame, gameState, closestDefender.get(0), closestDefender.get(1),
				curOptDistr,
				defPointSingleDefender);
		
		for (Entry<DefenderRole, DefensePoint> e : curOptDistr.entrySet())
		{
			DefenderRole role = e.getKey();
			DefensePoint defPoint = e.getValue();
			
			if (GeoMath.distancePP(role.getPos(), defPoint) > DefenseAux.nearDefPointDist)
			{
				IVector2 updatedDefPoint = DefMath.calcNearestDefPointBall(ballPos, role.getBot());
				defPoint.set(updatedDefPoint);
			}
		}
		
		return curOptDistr;
	}
	
	
	/**
	 * @param defRoleA
	 * @param defRoleB
	 * @param directShotDistr
	 * @param defPointSingleDefender
	 */
	private static void modifyDirectShotDistributionForOneBotFarAway(final WorldFrame frame,
			final EGameStateTeam gameState, final DefenderRole defRoleA,
			final DefenderRole defRoleB, final Map<DefenderRole, DefensePoint> directShotDistr,
			final IVector2 defPointSingleDefender)
	{
		double timeForUpdateAllowed = DefenseAux.timeDifferenceDoubleDefenderCoverSinglePoint;
		double botRadius = Geometry.getBotRadius();
		
		double distanceBots = GeoMath.distancePP(defRoleA.getPos(), defRoleB.getPos());
		
		if (distanceBots > (6 * botRadius))
		{
			double timePathA, timePathB;
			
			if (!DefenseAux.useTrajectoriesForPathCosts)
			{
				BangBangTrajectory2D pathToDestBotA = new TrajectoryGenerator().generatePositionTrajectory(
						defRoleA.getBot(),
						directShotDistr.get(defRoleA));
				BangBangTrajectory2D pathToDestBotB = new TrajectoryGenerator().generatePositionTrajectory(
						defRoleB.getBot(),
						directShotDistr.get(defRoleB));
				
				timePathA = pathToDestBotA.getTotalTime();
				timePathB = pathToDestBotB.getTotalTime();
			} else
			{
				timePathA = DefMath.getDefMovementCost(frame, defRoleA.getBot(), directShotDistr.get(defRoleA),
						gameState);
				timePathB = DefMath.getDefMovementCost(frame, defRoleB.getBot(), directShotDistr.get(defRoleB),
						gameState);
			}
			
			if ((timePathA < timePathB) && (timePathB > timeForUpdateAllowed))
			{
				double time2singleDefPoint;
				
				if (!DefenseAux.useTrajectoriesForPathCosts)
				{
					time2singleDefPoint = new TrajectoryGenerator().generatePositionTrajectory(
							defRoleA.getBot(), defPointSingleDefender).getTotalTime();
				} else
				{
					time2singleDefPoint = DefMath.getDefMovementCost(frame, defRoleA.getBot(), defPointSingleDefender,
							gameState);
				}
				
				if ((time2singleDefPoint - timePathA) < timeForUpdateAllowed)
				{
					directShotDistr.get(defRoleA).set(defPointSingleDefender);
				}
				
			} else if ((timePathB < timePathA) && (timePathA > timeForUpdateAllowed))
			{
				double time2singleDefPoint;
				
				if (!DefenseAux.useTrajectoriesForPathCosts)
				{
					time2singleDefPoint = new TrajectoryGenerator().generatePositionTrajectory(
							defRoleB.getBot(), defPointSingleDefender).getTotalTime();
				} else
				{
					time2singleDefPoint = DefMath.getDefMovementCost(frame, defRoleB.getBot(), defPointSingleDefender,
							gameState);
				}
				
				if ((time2singleDefPoint - timePathB) < timeForUpdateAllowed)
				{
					directShotDistr.get(defRoleB).set(defPointSingleDefender);
				}
			}
		}
	}
	
	
	private static class PriorityComparator implements Comparator<FoeBotGroup>
	{
		@Override
		public int compare(final FoeBotGroup group1, final FoeBotGroup group2)
		{
			if (group1.containsBestRedirector())
			{
				return -1;
			} else if (group2.containsBestRedirector())
			{
				return 1;
			}
			
			if (group1.invertPossessesBall && group1.possessesBall())
			{
				return 1;
			} else if (group2.invertPossessesBall && group2.possessesBall())
			{
				return -1;
			}
			
			if (group1.possessesBall())
			{
				return -1;
			} else if (group2.possessesBall())
			{
				return 1;
			}
			
			if (group2.nMember() != group1.nMember())
			{
				return group2.nMember() - group1.nMember();
			}
			
			FoeBotData mostDangerousA = group1.member.stream()
					.reduce((elA, elB) -> FoeBotData.DANGER_COMPARATOR.compare(elA, elB) >= 0 ? elA : elB).get();
			FoeBotData mostDangerousB = group2.member.stream()
					.reduce((elA, elB) -> FoeBotData.DANGER_COMPARATOR.compare(elA, elB) >= 0 ? elA : elB).get();
			
			return FoeBotData.DANGER_COMPARATOR.compare(mostDangerousA, mostDangerousB);
		}
	}
	
	
	private static class AngleComparator implements Comparator<FoeBotGroup>
	{
		@Override
		public int compare(final FoeBotGroup group1, final FoeBotGroup group2)
		{
			
			// comparing by comparing the angle of the first member
			if (group1.isEmpty())
			{
				return 1;
			} else if (group2.isEmpty())
			{
				return -1;
			}
			
			FoeBotData first1 = group1.getMember().get(0);
			FoeBotData first2 = group2.getMember().get(0);
			
			return (int) Math.signum(first1.getGoalAngle() - first2.getGoalAngle());
		}
	}
	
	
	/**
	 * @return the isFirst
	 */
	public boolean isFirst()
	{
		return isFirst;
	}
	
	
	/**
	 * @param isFirst the isFirst to set
	 */
	public void setFirst(final boolean isFirst)
	{
		this.isFirst = isFirst;
	}
	
	
	/**
	 * @return the isLast
	 */
	public boolean isLast()
	{
		return isLast;
	}
	
	
	/**
	 * @param isLast the isLast to set
	 */
	public void setLast(final boolean isLast)
	{
		this.isLast = isLast;
	}
	
	
	/**
	 * @return
	 */
	public boolean isOuter()
	{
		return isFirst || isLast;
	}
	
	
	/**
	 * @return the wasBallPossessing
	 */
	public boolean wasBallPossessing()
	{
		return wasBallPossessing;
	}
	
	
	/**
	 * @param wasBallPossessing the wasBallPossessing to set
	 */
	public void setWasBallPossessing(final boolean wasBallPossessing)
	{
		this.wasBallPossessing = wasBallPossessing;
	}
	
	
	/**
	 * @return the forcePossessesBall
	 */
	public boolean isForcePossessesBall()
	{
		return forcePossessesBall;
	}
	
	
	/**
	 * @param forcePossessesBall the forcePossessesBall to set
	 */
	public void setForcePossessesBall(final boolean forcePossessesBall)
	{
		this.forcePossessesBall = forcePossessesBall;
	}
	
	
	/**
	 * @return the ignoreGroup
	 */
	public boolean invertPossessesBall()
	{
		return invertPossessesBall;
	}
	
	
	/**
	 * @param invertPossessesBall
	 */
	public void setInvertPossessesBallp(final boolean invertPossessesBall)
	{
		this.invertPossessesBall = invertPossessesBall;
	}
}
