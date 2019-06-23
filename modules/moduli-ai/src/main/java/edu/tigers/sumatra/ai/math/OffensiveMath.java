/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.kick.BestDirectShotBallPossessingBot;
import edu.tigers.sumatra.ai.math.kick.MaxAngleKickRater;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.OngoingPassInfo;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Math and methods for the Offensive
 * (There is just too much chaos in AiMath)
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveMath
{
	
	private static final double QUADLITERAL_WIDTH_OFFSET_BACK = 35;
	
	private static final double QUADLITERAL_WIDTH_OFFSET_FRONT = 30;
	
	
	private OffensiveMath()
	{
		// hide public constructor
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	public static IBotIDMap<ITrackedBot> getPotentialOffensiveBotMap(final ITacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.isOpponentWillDoIcing())
		{
			return new BotIDMap<>();
		}
		IBotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		botMap.putAll(baseAiFrame.getWorldFrame().getTigerBotsAvailable());
		for (BotID key : newTacticalField.getCrucialDefender())
		{
			botMap.remove(key);
		}
		botMap.remove(newTacticalField.getBotNotAllowedToTouchBall());
		botMap.remove(baseAiFrame.getKeeperId());
		return botMap;
	}
	
	
	/**
	 * @param wFrame
	 * @param ourPenAreaMargin
	 * @param theirPenAreaMargin
	 * @return
	 */
	public static boolean isBallNearPenaltyAreaOrOutsideField(final WorldFrame wFrame, final double ourPenAreaMargin,
			final double theirPenAreaMargin)
	{
		IPenaltyArea ourPenArea = Geometry.getPenaltyAreaOur();
		IPenaltyArea theirPenArea = Geometry.getPenaltyAreaTheir();
		IVector2 ballPos = wFrame.getBall().getPos();
		return ourPenArea.isPointInShape(ballPos, ourPenAreaMargin)
				|| theirPenArea.isPointInShape(ballPos, theirPenAreaMargin) || !Geometry.getField().isPointInShape(ballPos);
	}
	
	
	/**
	 * @param wFrame
	 * @param botPos
	 * @param target
	 * @return
	 */
	public static boolean isBallRedirectPossible(final WorldFrame wFrame,
			final IVector2 botPos, final IVector2 target)
	{
		IVector2 ballToBot = botPos.subtractNew(wFrame.getBall().getPos()).normalizeNew();
		IVector2 botToTarget = target.subtractNew(botPos).normalizeNew();
		
		double product = ballToBot.scalarProduct(botToTarget);
		return product < 0;
	}
	
	
	/**
	 * @param wf
	 * @param kickerPos
	 * @param target
	 * @return
	 */
	public static boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 kickerPos, final IVector2 target)
	{
		return isBallRedirectReasonable(wf, wf.getBall().getPos(), kickerPos, target);
	}
	
	
	/**
	 * @param wf
	 * @param kickerPos
	 * @param target
	 * @param antiToggle
	 * @return
	 */
	public static boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 kickerPos, final IVector2 target,
			final double antiToggle)
	{
		return isBallRedirectReasonable(wf, wf.getBall().getPos(), kickerPos, target, antiToggle);
	}
	
	
	/**
	 * @param wf
	 * @param source
	 * @param kickerPos
	 * @param target
	 * @return
	 */
	private static boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 source, final IVector2 kickerPos,
			final IVector2 target)
	{
		return isBallRedirectReasonable(wf, source, kickerPos, target, 0.0);
	}
	
	
	/**
	 * @param wf
	 * @param source
	 * @param kickerPos
	 * @param target
	 * @param antiToogle
	 * @return
	 */
	private static boolean isBallRedirectReasonable(final WorldFrame wf, final IVector2 source, final IVector2 kickerPos,
			final IVector2 target, final double antiToogle)
	{
		double atC = 0;
		if (antiToogle > 0)
		{
			atC = 0.05;
		}
		double redirectAngle = getRedirectAngle(source, kickerPos, target);
		return (MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(), kickerPos) >= (0.15 - atC))
				&& (redirectAngle <= (OffensiveConstants.getMaximumReasonableRedirectAngle() + antiToogle));
	}
	
	
	/**
	 * Redirect Pass
	 *
	 * @param wf
	 * @param source
	 * @param midPoint
	 * @param target
	 * @return
	 */
	public static double getRedirectPassScore(final WorldFrame wf, final IVector2 source, final IVector2 midPoint,
			final IVector2 target)
	{
		if ((Geometry.getGoalOur().getCenter().distanceTo(target) < 3000) || (midPoint.distanceTo(target) > 12500))
		{
			return 0;
		}
		double redirectAngle = Math.max(0, AngleMath.rad2deg(getRedirectAngle(source, midPoint, target)));
		redirectAngle = 1 - (Math.min(90, redirectAngle) / 90.0);
		double scoreChance = MaxAngleKickRater.getDirectShootScoreChance(wf.getFoeBots().values(), target);
		return (scoreChance + redirectAngle) / 2.0;
	}
	
	
	/**
	 * @param source
	 * @param kickerPos
	 * @param target
	 * @return
	 */
	public static double getRedirectAngle(final IVector2 source, final IVector2 kickerPos, final IVector2 target)
	{
		IVector2 botToBall = source.subtractNew(kickerPos);
		IVector2 botToTarget = target.subtractNew(kickerPos);
		return botToBall.angleToAbs(botToTarget).orElse(Math.PI);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots)
	{
		IVector2 endPos = wFrame.getBall().getTrajectory().getPosByVel(0);
		return getBestRedirector(wFrame, bots, endPos, null);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param endPos
	 * @param tacticalField
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 endPos, final ITacticalField tacticalField)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		
		BotID minID = null;
		double minDist = Double.MAX_VALUE;
		
		List<BotID> filteredBots = getPotentialRedirectors(wFrame, bots, endPos, tacticalField);
		for (BotID key : filteredBots)
		{
			IVector2 pos = bots.getWithNull(key).getPos();
			if (VectorMath.distancePP(pos, ballPos) < minDist)
			{
				minDist = VectorMath.distancePP(pos, ballPos);
				minID = key;
			}
		}
		if (minID != null)
		{
			return minID;
		}
		return BotID.noBot();
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param endPos
	 * @return
	 */
	private static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 endPos)
	{
		return getPotentialRedirectors(wFrame, bots, endPos, null);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param endPos
	 * @param tacticalField
	 * @return
	 */
	private static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots, final IVector2 endPos, final ITacticalField tacticalField)
	{
		List<BotID> filteredBots = new ArrayList<>();
		final double redirectTol = 350;
		IVector2 ballPos = wFrame.getBall().getPos();
		
		// input: endpoint, ballVel.vel = endpoint - curPos.getAngle().
		IVector2 ballVel = endPos.subtractNew(ballPos);
		
		if (ballVel.getLength() < 0.4)
		{
			// no potential redirector
			return filteredBots;
		}
		
		IVector2 left = Vector2.fromAngle(ballVel.getAngle() - 0.2).normalizeNew();
		IVector2 right = Vector2.fromAngle(ballVel.getAngle() + 0.2).normalizeNew();
		
		double dist = Math.max(VectorMath.distancePP(ballPos, endPos) - redirectTol, 10);
		
		IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
		IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
		
		DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 20));
		dtri.setFill(true);
		IVector2 normal = ballVel.getNormalVector().normalizeNew();
		IVector2 tleft = ballPos.addNew(normal.scaleToNew(160));
		IVector2 tright = ballPos.addNew(normal.scaleToNew(-160));
		IVector2 uleft = tleft.addNew(left.scaleToNew(dist)).addNew(normal.scaleToNew(100));
		IVector2 uright = tright.addNew(right.scaleToNew(dist)).addNew(normal.scaleToNew(-100));
		
		DrawableTriangle dtri3 = new DrawableTriangle(tleft, uleft, uright, new Color(255, 0, 0, 20));
		dtri3.setFill(true);
		
		DrawableTriangle dtri4 = new DrawableTriangle(tleft, tright, uright, new Color(255, 0, 0, 20));
		dtri4.setFill(true);
		
		for (Map.Entry<BotID, ITrackedBot> entry : bots.entrySet())
		{
			BotID botID = entry.getKey();
			ITrackedBot tBot = entry.getValue();
			if (tBot == null)
			{
				continue;
			}
			IVector2 pos = tBot.getPos();
			if (dtri3.getTriangle().isPointInShape(pos) || dtri4.getTriangle().isPointInShape(pos))
			{
				filteredBots.add(botID);
			}
			if (tacticalField != null)
			{
				DrawableCircle dcp = new DrawableCircle(pos, 150, Color.cyan);
				if (filteredBots.contains(botID))
				{
					tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dcp);
				}
				
				tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dtri);
				tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dtri3);
				tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dtri4);
			}
		}
		if (tacticalField != null)
		{
			tacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINDER).add(dtri);
		}
		return filteredBots;
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @return
	 */
	public static List<BotID> getPotentialRedirectors(final WorldFrame wFrame,
			final IBotIDMap<ITrackedBot> bots)
	{
		IVector2 endPos = wFrame.getBall().getTrajectory().getPosByVel(0);
		return getPotentialRedirectors(wFrame, bots, endPos);
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final TacticalField newTacticalField)
	{
		IBotIDMap<ITrackedBot> potentialOffensiveBots = getPotentialOffensiveBotMap(newTacticalField, baseAiFrame);
		return getBestGetter(baseAiFrame, potentialOffensiveBots, newTacticalField);
	}
	
	
	/**
	 * Check if bot is behind ball
	 * 
	 * @param ball current ball
	 * @param botPos current bot position
	 * @return true, if behind ball
	 */
	public static boolean isBehindBall(final ITrackedBall ball, final IVector2 botPos)
	{
		if (ball.getVel().getLength2() < 0.1)
		{
			return false;
		}
		
		IVector2 ball2Bot = botPos.subtractNew(ball.getPos());
		return ball2Bot.angleToAbs(ball.getVel()).orElse(0.0) > AngleMath.PI_QUART;
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param bots
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestGetter(final BaseAiFrame baseAiFrame, final IBotIDMap<ITrackedBot> bots,
			final TacticalField newTacticalField)
	{
		double minScore = Double.MAX_VALUE;
		BotID bestBot = BotID.noBot();
		
		for (BotID id : bots.keySet())
		{
			ITrackedBot bot = bots.getWithNull(id);
			IVector2 botPos = bot.getPos();
			double score = newTacticalField.getOffensiveTimeEstimations().get(id).getBallContactTime();
			double rawscore = score;
			
			String modifierInformation = "";
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			
			if (isPrimary(id, baseAiFrame))
			{
				score -= 0.45;
				modifierInformation = "isPrimary";
			} else if (isSecondary(id, baseAiFrame))
			{
				score -= 0.20;
				modifierInformation = "isSecondary";
			}
			
			if (isReceiver(newTacticalField, id))
			{
				score -= 0.40;
				modifierInformation += " isPassReceiver";
			}
			
			// this is a really ugly quick fix
			GameState gameState = newTacticalField.getGameState();
			if ((gameState.isPrepareKickoffForUs() || gameState.isStop())
					&& (VectorMath.distancePP(botPos, baseAiFrame.getWorldFrame().getBall().getPos()) < 850)
					&& isPrimary(id, baseAiFrame))
			{
				score -= 5;
				modifierInformation = "isKickoff or stop";
			}
			
			DrawableAnnotation dt = new DrawableAnnotation(botPos,
					"Score -> " + df.format(rawscore) + " " + modifierInformation + " = " + df.format(score),
					Color.black);
			dt.setOffset(Vector2.fromY(-120));
			dt.setCenterHorizontally(true);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_FINDER).add(dt);
			
			if (score < minScore)
			{
				minScore = score;
				bestBot = id;
			}
		}
		
		return bestBot;
	}
	
	
	private static boolean isReceiver(final TacticalField newTacticalField, final BotID id)
	{
		if (newTacticalField.getOngoingPassInfo().isPresent())
		{
			OngoingPassInfo info = newTacticalField.getOngoingPassInfo().get();
			if (info.getPassTarget().getBotId().equals(id) && info.getTimeSinceStart() < 0.2)
			{
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean isPrimary(final BotID id, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getPrevFrame() == null)
		{
			return false;
		}
		if (baseAiFrame.getPrevFrame().getPlayStrategy() == null)
		{
			return false;
		}
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE) == null)
		{
			return false;
		}
		
		for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE))
		{
			if ((role.getBotID() == id)
					&& (!role.getCurrentState().getIdentifier()
							.equals(EOffensiveStrategy.SUPPORTIVE_ATTACKER.name())))
			{
				// primary bot here
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean isSecondary(final BotID id, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getPrevFrame() == null)
		{
			return false;
		}
		if (baseAiFrame.getPrevFrame().getPlayStrategy() == null)
		{
			return false;
		}
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE) == null)
		{
			return false;
		}
		
		for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE))
		{
			if ((role.getBotID() == id)
					&& role.getCurrentState().getIdentifier().equals(EOffensiveStrategy.SUPPORTIVE_ATTACKER.name()))
			{
				// secondary bot here
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return
	 */
	public static ITrackedBot getSupportiveAttacker(final ITacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		for (Map.Entry<BotID, ITrackedBot> bot : getPotentialOffensiveBotMap(newTacticalField, baseAiFrame).entrySet())
		{
			if (isSecondary(bot.getKey(), baseAiFrame))
			{
				return bot.getValue();
			}
		}
		return null;
	}
	
	
	/**
	 * @param passSenderPos (most of the time, this will be the ballPos)
	 * @param passReceiverPos receiving robot
	 * @param target redirecting target of the receiving robot.
	 * @return
	 */
	public static double calcPassSpeedForReceivers(final IVector2 passSenderPos, final IVector2 passReceiverPos,
			final IVector2 target)
	{
		IVector2 senderToReceiver = passReceiverPos.subtractNew(passSenderPos);
		double distance = senderToReceiver.getLength2();
		double passEndVel = passEndVel(passSenderPos, passReceiverPos, target);
		double minPassTime = OffensiveConstants.getMinPassTime();
		return SkillUtil.passKickSpeed(BallFactory.createStraightConsultant(), distance, passEndVel, minPassTime);
	}
	
	
	private static double passEndVel(final IVector2 passSenderPos, final IVector2 passReceiverPos,
			final IVector2 target)
	{
		IVector2 targetToReceiver = passReceiverPos.subtractNew(target);
		IVector2 senderToReceiver = passReceiverPos.subtractNew(passSenderPos);
		
		double angleDeg = AngleMath.rad2deg(targetToReceiver.angleToAbs(senderToReceiver).orElse(0.0));
		if (angleDeg > AngleMath.rad2deg(OffensiveConstants.getMaximumReasonableRedirectAngle()))
		{
			// in catch case.
			return OffensiveConstants.getDefaultPassEndVelReceive();
		}
		
		double mFull = OffensiveConstants.getMaxAngleforPassMaxSpeed();
		double mRed = OffensiveConstants.getMaxAngleForReducedSpeed();
		if ((angleDeg > mFull) && (angleDeg < 100))
		{
			// lower pass speed on bad angles.
			return Math.max(
					OffensiveConstants.getDefaultPassEndVel()
							- (OffensiveConstants.getPassSpeedReductionForBadAngles() *
									((angleDeg - mFull) / (mRed - mFull))),
					0.5);
		}
		// Receiver will probably redirect the ball
		return OffensiveConstants.getDefaultPassEndVel();
	}
	
	
	/**
	 * @param baseAiFrame
	 * @param newTacticalField
	 * @return
	 */
	public static boolean isKeeperInsane(final BaseAiFrame baseAiFrame, final ITacticalField newTacticalField)
	{
		GameState gameState = newTacticalField.getGameState();
		return gameState.isStandardSituationForUs() && OffensiveConstants.isEnableInsanityMode()
				&& (baseAiFrame.getWorldFrame().getBall().getPos().x() > ((Geometry.getFieldLength() / 2) - 250));
	}
	
	
	/**
	 * @author Mark Geiger
	 * @param wf World Frame
	 * @return true -> Bot will shoot directly on the goal, false -> Bot will pass to another Bot
	 */
	public static boolean willBotShoot(final WorldFrame wf)
	{
		Optional<ValuePoint> scoreChance = BestDirectShotBallPossessingBot.getBestShot(Geometry.getGoalTheir(),
				wf.getBall().getPos(), new ArrayList<>(wf.getFoeBots().values()));
		
		return scoreChance.orElse(new ValuePoint(DefenseMath.getBisectionGoal(wf.getBall().getPos()), 0.0))
				.getValue() > OffensiveConstants.getMinDirectShotScore();
	}
	
	
	/**
	 * @param worldFrame wf
	 * @param kickingRobot robot that wants to kick the ball
	 * @param passTarget the pass target, needs a valid bot ID as passTarget
	 * @param initialKickVel initial kick velocity
	 * @return true, if chip kick is required
	 */
	public static boolean isChipKickRequired(WorldFrame worldFrame,
			BotID kickingRobot,
			final IPassTarget passTarget,
			double initialKickVel)
	{
		return isChipKickRequired(worldFrame, kickingRobot, passTarget, initialKickVel, new ArrayList<>());
	}
	
	
	/**
	 * @param worldFrame wf
	 * @param kickingRobot robot that wants to kick the ball
	 * @param passTarget the pass target, needs a valid bot ID as passTarget
	 * @param initialKickVel initial kick velocity
	 * @param shapes add drawable shapes, can be null
	 * @return true, if chip kick is required
	 */
	public static boolean isChipKickRequired(WorldFrame worldFrame,
			BotID kickingRobot,
			final IPassTarget passTarget,
			double initialKickVel,
			List<IDrawableShape> shapes)
	{
		IVector2 ballToTarget = passTarget.getKickerPos().subtractNew(worldFrame.getBall().getPos());
		
		ITrackedBot tBot = worldFrame.getBot(kickingRobot);
		if (tBot == null)
		{
			return false;
		}
		double chipAngle = tBot.getRobotInfo().getBotParams().getKickerSpecs().getChipAngle();
		IVector2 kickVel = ballToTarget.scaleToNew(initialKickVel * 1000);
		ABallTrajectory traj = BallFactory.createTrajectoryFrom2DKick(worldFrame.getBall().getPos(), kickVel, chipAngle,
				true);
		IVector2 initialTouchDown;
		IVector2 lastTochDown;
		
		double minDistOffset = Math.min(250,
				BallFactory.createChipConsultant().getMinimumDistanceToOverChip(initialKickVel,
						170));
		double dist;
		if (!traj.getTouchdownLocations().isEmpty())
		{
			// they can be the same point, which is perfectly fine
			initialTouchDown = traj.getTouchdownLocations().get(0);
			lastTochDown = traj.getTouchdownLocations().get(traj.getTouchdownLocations().size() - 1);
			addtouchDownShape(shapes, initialTouchDown);
			addtouchDownShape(shapes, lastTochDown);
			dist = worldFrame.getBall().getPos().distanceTo(initialTouchDown) - minDistOffset;
		} else
		{
			dist = OffensiveConstants.getChipKickCheckDistance() - minDistOffset;
		}
		dist = Math.max(500, dist - minDistOffset);
		if (ballToTarget.getLength2() > dist)
		{
			ballToTarget = ballToTarget.scaleToNew(dist);
		}
		
		
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		IVector2 triB1 = worldFrame.getBall().getPos()
				.addNew(normal.scaleToNew(QUADLITERAL_WIDTH_OFFSET_FRONT + Geometry.getBotRadius()))
				.addNew(ballToTarget.scaleToNew(minDistOffset));
		IVector2 triB2 = worldFrame.getBall().getPos()
				.addNew(normal.scaleToNew(-QUADLITERAL_WIDTH_OFFSET_FRONT - Geometry.getBotRadius()))
				.addNew(ballToTarget.scaleToNew(minDistOffset));
		IVector2 triT1 = triB1.addNew(ballToTarget).addNew(
				normal.scaleToNew(-QUADLITERAL_WIDTH_OFFSET_BACK + Geometry.getBotRadius()));
		IVector2 triT2 = triB2.addNew(ballToTarget).addNew(
				normal.scaleToNew(QUADLITERAL_WIDTH_OFFSET_BACK - Geometry.getBotRadius()));
		
		DrawableQuadrilateral quad = new DrawableQuadrilateral(Quadrilateral.fromCorners(triT1, triB2, triT2, triB1),
				Color.cyan);
		
		shapes.add(quad);
		
		quad.setColor(new Color(25, 40, 40, 65));
		quad.setFill(true);
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>(worldFrame.getBots());
		bots.remove(kickingRobot);
		bots.remove(passTarget.getBotId());
		
		// determine if offensive has to chip.
		boolean chip = false;
		double dist2Ball = worldFrame.getBall().getPos().distanceTo(passTarget.getKickerPos());
		shapes.add(new DrawableAnnotation(worldFrame.getBall().getPos(), "dist to Target: " + dist2Ball, Color.orange)
				.setOffset(Vector2.fromY(-200)));
		
		for (ITrackedBot bot : bots.values())
		{
			IVector2 futureBotPos = bot.getPosByTime(0.1);
			if (quad.getQuadrilateral().isPointInShape(bot.getPos())
					|| quad.getQuadrilateral().isPointInShape(futureBotPos))
			{
				ICircle c1 = Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 2);
				DrawableCircle dc1 = new DrawableCircle(c1, Color.orange);
				
				// check if ball has at least minimum distance to target. Otherwise a chipKick is not reasonable
				shapes.add(dc1);
				if (dist2Ball > OffensiveConstants.getChipKickMinDistToTarget())
				{
					chip = true;
				}
			}
		}
		shapes.add(new DrawableAnnotation(worldFrame.getBall().getPos(), "isChipKickRequired " + chip, Color.orange)
				.setOffset(Vector2.fromY(-300)));
		return chip;
	}
	
	
	private static void addtouchDownShape(final List<IDrawableShape> shapes, final IVector2 initialTouchDown)
	{
		ICircle c1 = Circle.createCircle(initialTouchDown, 50);
		DrawableCircle dc1 = new DrawableCircle(c1, Color.red);
		shapes.add(dc1);
	}
	
	
	/**
	 * @param timeToPassTarget
	 * @param origin
	 * @param redirectPos
	 * @param target
	 * @return
	 */
	public static double calcPassSpeedRedirect(double timeToPassTarget, IVector2 origin, final IVector2 redirectPos,
			IVector2 target)
	{
		double passSpeed = OffensiveMath.calcPassSpeedForReceivers(origin,
				redirectPos, target);
		
		IStraightBallConsultant consultant = BallFactory.createStraightConsultant();
		double passDist = origin.distanceTo(redirectPos);
		
		for (; passSpeed > 0; passSpeed -= 0.1)
		{
			double travelTime = consultant.getTimeForKick(passDist, passSpeed);
			IBallTrajectory ballTrajectory = BallFactory.createTrajectoryFromKick(Vector2.zero(),
					Vector3.from2d(Vector2.fromX(1).scaleTo(passSpeed * 1000), 0), false);
			double passEndVel = ballTrajectory.getVelByTime(travelTime).getLength2();
			
			boolean ballCantReachPassTarget = Double
					.isInfinite(BallFactory.createStraightConsultant().getTimeForKick(passDist, passSpeed - 0.1));
			
			if ((travelTime > timeToPassTarget) || (passEndVel < 1.5) || ballCantReachPassTarget)
			{
				break;
			}
		}
		
		double minPassSpeed = BallFactory.createStraightConsultant().getInitVelForDist(passDist, 1.0);
		passSpeed = Math.max(passSpeed, minPassSpeed);
		return passSpeed;
	}
	
}
