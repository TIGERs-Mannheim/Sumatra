/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Felix Bayer <bayer.fel@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.data;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.algorithms.ExtensiveFoeBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Felix Bayer <bayer.fel@googlemail.com>
 */
public final class DefenseAux
{
	/**  */
	public static final double	ballLookAheadDefenders								= 0.2;
	
	/**  */
	public static final double	foeLookAheadDefenders								= 0.1;
	
	/**  */
	public static final double	minDistDefender2Foe									= 4
			* Geometry.getBotRadius();
	
	/**  */
	@Configurable(comment = "Maximum num defenders per group")
	public static int				maxDefenderGroup										= 2;
	
	/** */
	@Configurable(comment = "Maximum num defenders per group during kickoff")
	public static int				maxDefenderGroupKickoff								= 4;
	
	/** */
	@Configurable(comment = "Force double defenders to defend against the ball")
	public static boolean		forceDoubleDefOnBall									= false;
	
	/**  */
	@Configurable(comment = "x coordinate of the line beyond which foe bots are ignored for our defender count")
	public static double			ignoreEnemyBotsLine									= 1000;
	
	
	/**  */
	@Configurable(comment = "Activate passive agressivity for outer defenders")
	public static boolean		outerDefendersPassiveAgressive					= true;
	
	/** */
	@Configurable(comment = "Force the inner man to man markers to the penalty area")
	public static boolean		innerMan2ManAtPenArea								= false;
	
	/** */
	@Configurable(comment = "Inner defenders passive agressive")
	public static boolean		innerPassiveAgressive								= false;
	
	/** */
	@Configurable(comment = "Maximum distance of inner defenders to our goal")
	public static double			maxDistInnerDefender2goal							= 2
			* Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea();
	
	/**  */
	@Configurable(comment = "If the defender is closer than this distance to its destination he will maybe update his destination")
	public static double			nearDefPointDist										= 4
			* Geometry.getBotRadius();
	
	/**  */
	@Configurable(comment = "maximum abs y position the defenders are allowed to drive to")
	public static double			yLimitDefenders										= (Geometry
			.getFieldWidth() / 2.)
			- Geometry.getBotRadius();
	
	/**  */
	@Configurable(comment = "Max velocity under which the ball is controllable by the foe")
	public static double			maxBallControlVel										= 6.;
	
	/**  */
	@Configurable(comment = "Bot blocking defenders will not drive behind this x value")
	public static double			maxXBotBlockingDefender								= -(1. / 3.) * Geometry.getFieldLength()
			* (1. / 2.);
	
	/**  */
	@Configurable(comment = "Bot blocking defenders will not drive behind this x value during kickoff")
	public static double			maxXBotBlockingDefenderKickOff					= -(Geometry.getCenterCircleRadius()
			+ (1.5f * Geometry.getBotRadius()));
	
	/** */
	@Configurable(comment = "Radius around the corners the direct shot is not blocked by the defenders")
	private static double		noDirecctShotDefRadius								= 8 * Geometry.getBotRadius();
	
	/** */
	@Configurable(comment = "Spare the direct shot defender when outnumbered")
	public static boolean		spareDirectShotDefActivated						= true;
	
	/** */
	@Configurable(comment = "Enable crucial defenders")
	public static boolean		crucialDefendersEnabled								= true;
	
	/** */
	@Configurable(comment = "Never block the direct shot in a static defensive situation")
	public static boolean		neverBlockStaticDefensive							= false;
	
	/** */
	@Configurable(comment = "Use trajectories for path costs")
	public static boolean		useTrajectoriesForPathCosts						= false;
	
	/** */
	@Configurable(comment = "Max defenders in any static defensive situation")
	public static boolean		onlyDefendersInStaticSituations					= true;
	
	/** */
	@Configurable(comment = "Max defenders during foe kickoff")
	public static boolean		maxDefendersAtKickoff								= true;
	
	/** */
	@Configurable(comment = "Activate fexible groups")
	public static boolean		flexibleGroupsActivated								= true;
	
	/** */
	@Configurable(comment = "Lower threshold factor for defense groups")
	public static double			lowerFlexibleGroupFactor							= 3. / 4.;
	
	/** */
	@Configurable(comment = "Upper threshold factor for defense groups")
	public static double			upperFlexibleGroupFactor							= 5. / 4.;
	
	/** */
	@Configurable(comment = "Do not defend against the ball during a mixed team game")
	public static boolean		doNotDefendBallInMixedTeamMode					= false;
	
	/** */
	@Configurable(comment = "Dead switch - clearing by the defense not activated at the moment.")
	public static boolean		enableClearingByDefense								= false;
	
	/** */
	@Configurable(comment = "Distance our offense need to have to the ball to forbid clearing by the defense")
	public static double			noClearingBecauseOffenseDist						= 6 * Geometry.getBotRadius();
	
	/** */
	@Configurable(comment = "Chip distanc used when a defender clears the ball")
	public static double			clearingChipDistance									= 2000.;
	
	/** */
	@Configurable(comment = "Heigth of the triangle used to check if a defender could switch to clearing state.")
	public static double			triangleHeightClearing								= 10 * Geometry.getBotRadius();
	
	/** */
	@Configurable(comment = "Width of the triangle used to check if a defender could switch to clearing state.")
	public static double			triangleWidthClearing								= 10 * Geometry.getBotRadius();
	
	/** */
	@Configurable(comment = "time[s] between the positioning of both double defenders the first defender will overtake all work")
	public static double			timeDifferenceDoubleDefenderCoverSinglePoint	= 0.4;
	
	
	static
	{
		ConfigRegistration.registerClass("defense", DefenseAux.class);
	}
	
	
	/**
	 * @param ballWorldFrame
	 * @return
	 */
	public static IVector2 getBallPosDefense(final TrackedBall ballWorldFrame)
	{
		return ballWorldFrame.getPosByTime(ballLookAheadDefenders);
	}
	
	
	/**
	 * @param gameState
	 * @param ball
	 * @return
	 */
	public static boolean isFoeCorner(final EGameStateTeam gameState, final TrackedBall ball)
	{
		ICircle uncriticalCornerCircleLeft = new Circle(Geometry.getCornerLeftOur(), noDirecctShotDefRadius);
		ICircle uncriticalCornerCircleRight = new Circle(Geometry.getCornerRightOur(), noDirecctShotDefRadius);
		
		IVector2 ballPos = getBallPosDefense(ball);
		
		boolean ballInZone = uncriticalCornerCircleLeft.isPointInShape(ballPos)
				|| uncriticalCornerCircleRight.isPointInShape(ballPos);
		boolean rightGameMode = ((gameState == EGameStateTeam.DIRECT_KICK_THEY)
				|| (gameState == EGameStateTeam.CORNER_KICK_THEY)
				|| (gameState == EGameStateTeam.THROW_IN_THEY));
		
		return ballInZone && rightGameMode;
	}
	
	
	/**
	 * @param foeBotData
	 * @param curDefender
	 * @return
	 */
	public static DefensePoint getMan2ManDefPoint(final FoeBotData foeBotData, final DefenderRole curDefender)
	{
		double defendDistance = ZoneDefenseCalc.getMinDistanceDef2Foe();
		double zoneDefenseAngleCorrection = ZoneDefenseCalc.getZoneDefenseAngleCorrection();
		
		IVector2 foeBotPos = foeBotData.getFoeBot().getPos();
		
		if (Circle.getNewCircle(foeBotPos, DriveOnLinePointCalc.nearEnemyBotDist).isPointInShape(curDefender.getPos()))
		{
			IVector2 defPos = null;
			
			IVector2 ourGoalCenter = Geometry.getGoalOur().getGoalCenter();
			
			IVector2 foeBot2Ball = foeBotData.getBall2bot().multiplyNew(-1);
			IVector2 foeBot2Goal = foeBotData.getBot2goal();
			
			double angleBallBotGoal = GeoMath.angleBetweenVectorAndVectorWithNegative(foeBot2Goal, foeBot2Ball);
			
			if (Math.abs(angleBallBotGoal) < zoneDefenseAngleCorrection)
			{
				// Calculate a defense point on the bisector in the angle ball->foeBot and foeBot->goal
				IVector2 bisector = GeoMath.calculateBisector(foeBotPos, foeBotPos.addNew(foeBot2Ball),
						foeBotPos.addNew(foeBot2Goal)).subtract(foeBotPos);
				defPos = foeBotPos.addNew(bisector.scaleToNew(defendDistance));
			} else
			{
				// correct the defense position according to the angle
				double pi = Math.PI;
				double angleToCorrect = pi - angleBallBotGoal;
				double correctionAngle = (angleToCorrect * (angleBallBotGoal / angleToCorrect)) - pi;
				
				defPos = foeBotPos.addNew(
						foeBot2Goal.scaleToNew(defendDistance).turn(correctionAngle));
			}
			
			defPos = ExtensiveFoeBotCalc.limitYPointOnVector(defPos, ourGoalCenter.subtractNew(defPos), defPos,
					yLimitDefenders);
			
			if (Geometry.getPenaltyAreaOur().isPointInShape(defPos,
					Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin()))
			{
				defPos = foeBotData.getBot2goalNearestToGoal();
			}
			
			return new DefensePoint(defPos, foeBotData);
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToBot(), foeBotData);
	}
	
	
	/**
	 * @param gameState
	 * @return
	 */
	public static boolean isKickoffSituation(final EGameStateTeam gameState)
	{
		return (gameState == EGameStateTeam.PREPARE_KICKOFF_THEY) || (gameState == EGameStateTeam.PREPARE_KICKOFF_WE);
	}
	
	
	/**
	 * @param gameState
	 * @param worldFrame
	 * @return
	 */
	public static boolean useDoubleDefender(final EGameStateTeam gameState, final WorldFrame worldFrame)
	{
		double doubleDefXValue = ZoneDefenseCalc.getDoubleDefXValue();
		double blockAngleDoubleDefender = ZoneDefenseCalc.getBlockAngleDoubleDefenders();
		
		/*
		 * If we have got only three bots we will use one as keeper and one as offensive. Therefore we can only
		 * use one bot for the defense.
		 */
		if (worldFrame.getTigerBotsAvailable().size() <= 3)
		{
			return false;
		}
		
		if (DefenseAux.forceDoubleDefOnBall)
		{
			return true;
		}
		
		/*
		 * If the ball is in an area around the penalty area where a direct shot is more dangerous than a
		 * indirect we want double defenders.
		 */
		IVector2 ballPos = DefenseAux.getBallPosDefense(worldFrame.getBall());
		IVector2 goalOurCenter = Geometry.getGoalOur().getGoalCenter();
		
		if ((ballPos.x() < doubleDefXValue)
				&& !(Math.abs(GeoMath.angleBetweenXAxisAndLine(goalOurCenter, ballPos)) > blockAngleDoubleDefender))
		{
			
			return true;
		}
		
		/*
		 * If we have got no reason for double defenders we do not want double defenders.
		 */
		return false;
	}
	
	
	/**
	 * Move a defender position out of our penalty area
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 movePointOutsideOuPenArea(final IVector2 pos)
	{
		return Geometry.getPenaltyAreaOur().nearestPointOutside(pos, penAreaMargin());
	}
	
	
	/**
	 * Move a defender position out of our penalty area
	 * 
	 * @param pos
	 * @param towards
	 * @return
	 */
	public static IVector2 movePointOutsideOuPenArea(final IVector2 pos, final IVector2 towards)
	{
		if (towards != null)
		{
			return Geometry.getPenaltyAreaOur().nearestPointOutside(pos, towards, penAreaMargin());
		}
		return movePointOutsideOuPenArea(pos);
	}
	
	
	/**
	 * Get the penalty area margin considering the bot radius
	 * 
	 * @return
	 */
	public static double penAreaMargin()
	{
		return Geometry.getPenaltyAreaMargin() + Geometry.getBotRadius();
	}
	
	
	/**
	 * @param gameState
	 * @return
	 */
	public static boolean isAnyStaticDefensiveSituation(final EGameStateTeam gameState)
	{
		return (gameState == EGameStateTeam.CORNER_KICK_THEY) || (gameState == EGameStateTeam.DIRECT_KICK_THEY)
				|| (gameState == EGameStateTeam.GOAL_KICK_THEY) || (gameState == EGameStateTeam.THROW_IN_THEY);
	}
	
	
	/**
	 * @param gameState
	 * @return
	 */
	public static boolean isStaticDefenseIncludingKickoff(final EGameStateTeam gameState)
	{
		return isAnyStaticDefensiveSituation(gameState) || (gameState == EGameStateTeam.PREPARE_KICKOFF_THEY);
	}
}
