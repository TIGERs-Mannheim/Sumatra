/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.math3.util.Pair;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.math.InterceptBallMath;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.ITrajPathFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderNoObs;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.TrajPathFinderV4;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.CircularObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.KickBallObstacleV2;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.Hysterese;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ExtendedPenaltyArea;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickSkillCalc
{
	private final DynamicPosition									receiver;
	
	private final ITrajPathFinder									finder						= new TrajPathFinderV4();
	private final ITrajPathFinder									interceptionPathFinder	= new TrajPathFinderNoObs();
	private final ObstacleGenerator								obstacleGen					= new ObstacleGenerator();
	private TrajPathFinderInput									finderInput					= null;
	private MovementCon												moveCon						= new MovementCon();
	
	private boolean													armKicker					= false;
	private boolean													roleReady4Kick				= true;
	private boolean													skillReady4Kick			= false;
	private boolean													stateReady4Kick			= true;
	private boolean													enableDribbler				= false;
	private double														lastLookahead				= 0;
	private long														tLastDribblerActive		= 0;
	private long														lastBallContact			= 0;
	private String														stateDesc					= "";
	private EKickSkillState											kickState					= EKickSkillState.STILL;
	private IVector3													unFilteredDest				= Vector3.ZERO_VECTOR;
	private Optional<IVector2>										moveDest						= Optional.empty();
	private Optional<IVector2>										protectPos					= Optional.empty();
	
	private EMoveMode													moveMode						= EMoveMode.NORMAL;
	private double														distBehindBallHitTarget	= 10;
	
	private final Map<EKickSkillState, IKickSkillState>	kickSkillStates			= new EnumMap<>(
			EKickSkillState.class);
	
	
	private IVector3													catchDest					= Vector3.ZERO_VECTOR;
	private final Hysterese											hystDist2Ball				= new Hysterese(10, 70);
	private final Hysterese											hystDist2CatchDest		= new Hysterese(10, 400);
	private final Hysterese											hystBallMoving				= new Hysterese(0.2, 0.7);
	
	@Configurable(comment = "Enable dribbler if ball is that near to bot center")
	private static double											distForEnablingDribbler	= 200;
	
	
	@Configurable(comment = "Speed in chill mode")
	private static double											chillVel						= 1;
	
	@Configurable(comment = "Acceleration in chill model")
	private static double											chillAcc						= 4;
	
	@Configurable(comment = "offset margin that is allowed in their penArea")
	private static double											theirPenAreaOffset		= 300;
	
	static
	{
		ConfigRegistration.registerClass("skills", KickSkillCalc.class);
	}
	
	/**
	 */
	public enum EKickSkillState
	{
		/**  */
		CATCH,
		/**  */
		STILL,
		/**  */
		PUSH,
		/**  */
		PANIC,
		/**  */
		PULL,
		/**  */
		PUSH_SLOW,
		/**  */
		PROTECT,
	}
	
	
	/**
	 * @param receiver
	 */
	public KickSkillCalc(final DynamicPosition receiver)
	{
		this.receiver = receiver;
		
		kickSkillStates.put(EKickSkillState.CATCH, new CatchKickSkillState());
		kickSkillStates.put(EKickSkillState.STILL, new StillKickSkillState());
		kickSkillStates.put(EKickSkillState.PUSH, new PushKickSkillStateV2());
		kickSkillStates.put(EKickSkillState.PANIC, new PushKickSkillStateV2());
		kickSkillStates.put(EKickSkillState.PULL, new PullBackKickSkillStateV2());
		kickSkillStates.put(EKickSkillState.PUSH_SLOW, new PushKickSkillStateV2());
		kickSkillStates.put(EKickSkillState.PROTECT, new ProtectBallState());
	}
	
	
	@SuppressWarnings("unused")
	private IVector3 estimateBallInterceptionTime(final ITrackedBot bot, final WorldFrame wFrame,
			final Function<CalcDestInput, IVector3> calcDestFunc, final ITrajPathFinder finder)
	{
		TrackedBall ball = wFrame.getBall();
		final TrajPathFinderInput localPathFinderInput = new TrajPathFinderInput(finderInput, wFrame.getTimestamp());
		
		double time = InterceptBallMath.getBallInterceptionTime(wFrame.getBall(), (final double tBall) -> {
			CalcDestInput cdInput = new CalcDestInput(bot, wFrame, tBall);
			IVector3 dest = calcDestFunc.apply(cdInput);
			localPathFinderInput.setDest(dest.getXYVector());
			localPathFinderInput.setTargetAngle(dest.z());
			Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localPathFinderInput);
			if (path.isPresent())
			{
				return path.get().getTrajectory().getTotalTime();
			}
			return Double.MAX_VALUE;
			
		});
		
		CalcDestInput cdInput = new CalcDestInput(bot, wFrame, time);
		return calcDestFunc.apply(cdInput);
	}
	
	
	private IVector3 estimatePush(final ITrackedBot bot, final WorldFrame wFrame,
			final Function<CalcDestInput, IVector3> calcDestFunc, final ITrajPathFinder finder)
	{
		double breakTime = wFrame.getBall().getVel().getLength()
				/ finderInput.getMoveCon().getMoveConstraints().getAccMax();
		// breakTime += 0.1; // TODO
		breakTime *= 0.9;
		breakTime = Math.min(0.5, breakTime);
		
		double angle = GeoMath.angleBetweenVectorAndVector(wFrame.getBall().getVel(),
				receiver.subtractNew(wFrame.getBall().getPos()));
		if (angle > AngleMath.PI_HALF)
		{
			angle = (AngleMath.PI - angle);
		}
		double relAngle = angle / AngleMath.PI_HALF;
		
		double sideTime = relAngle * 0.3;
		
		double lookahead = Math.max(breakTime, sideTime);
		
		CalcDestInput cdInput = new CalcDestInput(bot, wFrame, lookahead);
		IVector3 dest = calcDestFunc.apply(cdInput);
		return dest;
		
		
		// final TrajPathFinderInput localPathFinderInput = new TrajPathFinderInput(finderInput, wFrame.getTimestamp());
		//
		// double lastMinDist = Double.MAX_VALUE;
		// for (double timeOffset = 0; timeOffset < 1; timeOffset += 0.1)
		// {
		// CalcDestInput cdInput = new CalcDestInput(bot, wFrame, timeOffset);
		// IVector3 dest = calcDestFunc.apply(cdInput);
		// localPathFinderInput.setDest(dest.getXYVector());
		// localPathFinderInput.setTargetAngle(dest.z());
		// Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localPathFinderInput);
		// if (path.isPresent())
		// {
		// double botTime = path.get().getTrajectory().getTotalTime();
		// double minDist2Ball = Double.MAX_VALUE;
		// for (double t = 0; t < botTime; t += 0.1)
		// {
		// IVector2 ballPos = wFrame.getBall().getPosByTime(t);
		// IVector2 botPos = path.get().getTrajectory().getPositionMM(t);
		// double dist2Ball = GeoMath.distancePP(ballPos, botPos);
		// if (dist2Ball < minDist2Ball)
		// {
		// minDist2Ball = dist2Ball;
		// }
		// }
		//
		// // System.out.println(timeOffset + " " + minDist2Ball);
		// double diff = Math.abs(minDist2Ball - lastMinDist);
		// lastMinDist = minDist2Ball;
		// // if (diff < 1)
		// if (minDist2Ball < 10)
		// {
		// return dest;
		// }
		// }
		//
		// }
		//
		// CalcDestInput cdInput = new CalcDestInput(bot, wFrame, 1);
		// IVector3 dest = calcDestFunc.apply(cdInput);
		// return dest;
	}
	
	
	private IVector3 estimateBallInterceptionIter(final ITrackedBot bot, final WorldFrame wFrame,
			final Function<CalcDestInput, IVector3> calcDestFunc, final ITrajPathFinder finder)
	{
		if (bot.hasBallContact())
		{
			CalcDestInput cdInput = new CalcDestInput(bot, wFrame, 0);
			IVector3 dest = calcDestFunc.apply(cdInput);
			return dest;
		}
		
		final TrajPathFinderInput localPathFinderInput = new TrajPathFinderInput(finderInput, wFrame.getTimestamp());
		
		double bestDiff = Double.MAX_VALUE;
		double tMax = wFrame.getBall().getTimeByVel(0);
		lastLookahead = Math.min(lastLookahead, tMax);
		double bestLookahead = Math.min(10, lastLookahead + 1);
		IVector3 bestDest = null;
		
		double[] times = new double[] { -0.7, -0.3, -0.1, -0.05, 0, 0.05, 0.1, 0.3, 0.7 };
		
		for (double time2 : times)
		{
			double t = lastLookahead + time2;
			if ((t < 0) || (t > tMax))
			{
				continue;
			}
			CalcDestInput cdInput = new CalcDestInput(bot, wFrame, t);
			IVector3 dest = calcDestFunc.apply(cdInput);
			localPathFinderInput.setDest(dest.getXYVector());
			localPathFinderInput.setTargetAngle(dest.z());
			Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localPathFinderInput);
			if (path.isPresent())
			{
				double time = path.get().getTrajectory().getTotalTime();
				if (wFrame.getBall().getVel().getLength() > 0.1)
				{
					ILine line = new Line(wFrame.getBall().getPos(), wFrame.getBall().getVel());
					for (double tt = 0; tt < time; tt += 0.05)
					{
						IVector2 pos = path.get().getPositionMM(localPathFinderInput.getTimestamp() + (long) (tt * 1e9));
						IVector2 vel = path.get().getVelocity(localPathFinderInput.getTimestamp() + (long) (tt * 1e9));
						double dist = GeoMath.distancePL(pos, line);
						double angleVelDiff = GeoMath.angleBetweenVectorAndVector(vel, wFrame.getBall().getVel());
						
						if ((dist < 30) && (angleVelDiff < 0.2))
						{
							time = tt;
							break;
						}
					}
				}
				double diff = Math.abs(t - time);
				if ((diff >= 0.01) &&
						(diff < bestDiff))
				{
					if ((bestDiff > 0.2) || (bestLookahead > time))
					{
						bestDiff = diff;
						bestLookahead = time;
						bestDest = dest;
					}
				}
			}
		}
		lastLookahead = bestLookahead;
		
		// if (bestDest == null)
		{
			bestDest = calcDestFunc.apply(new CalcDestInput(bot, wFrame, bestLookahead));
		}
		
		return bestDest;
	}
	
	
	/**
	 * @return
	 */
	private IVector3 validateDest(final ITrackedBot bot, final WorldFrame wFrame, final IVector3 dest)
	{
		TrackedBall ball = wFrame.getBall();
		IVector2 newDest = dest.getXYVector();
		double targetAngle = dest.z();
		
		// #####################################
		IVector2 ballPos = ball.getPos();
		if (!Geometry.getFieldWBorders().isPointInShape(newDest, 0))
		{
			try
			{
				newDest = Geometry.getFieldWBorders()
						.getNearIntersectionPoint(Line.newLine(ballPos, newDest));
			} catch (MathException e)
			{
				// backup
				newDest = Geometry.getField().nearestPointInside(newDest, -Geometry.getBotRadius());
			}
			// targetAngle = ball.getPos().subtractNew(bot.getPos()).getAngle();
		}
		
		// #####################################
		IVector2 dest2Ball = newDest.subtractNew(ball.getPos());
		double botAngle = bot.getAngle();
		if (!dest2Ball.isZeroVector())
		{
			botAngle = dest2Ball.getAngle();
		}
		double inFieldTol = 20;
		IVector2 kickerPos = GeoMath.getBotKickerPos(newDest, botAngle,
				(bot.getCenter2DribblerDist()) - inFieldTol);
		if (!Geometry.getField().isPointInShape(kickerPos) &&
				Geometry.getField().isPointInShape(ballPos) &&
				(kickState != EKickSkillState.STILL))
		{
			try
			{
				IVector2 intersec = Geometry.getField()
						.getNearIntersectionPoint(Line.newLine(ball.getPos(), newDest));
				newDest = GeoMath.stepAlongLine(intersec, ballPos,
						(bot.getCenter2DribblerDist()) - inFieldTol);
			} catch (MathException e)
			{
				// backup
				newDest = Geometry.getField().nearestPointInside(newDest, -Geometry.getBotRadius() - inFieldTol);
			}
		}
		
		// #####################################
		List<Pair<ExtendedPenaltyArea, Double>> penAreas = new ArrayList<>(2);
		if (obstacleGen.isUsePenAreaOur())
		{
			penAreas.add(new Pair<>(Geometry.getPenaltyAreaOurExtended(), 0.0));
		}
		if (obstacleGen.isUsePenAreaTheir())
		{
			penAreas.add(new Pair<>(Geometry.getPenaltyAreaTheirExtended(), -100.0));
		}
		for (Pair<ExtendedPenaltyArea, Double> pair : penAreas)
		{
			ExtendedPenaltyArea penArea = pair.getFirst();
			double margin = pair.getSecond();
			if (penArea.isPointInShape(newDest, 200 - margin))
			{
				if (penArea.isPointInShape(ballPos, Geometry.getBallRadius()))
				{
					IVector2 potBallPosOutside = ballPos.addNew(wFrame.getBall().getVel().scaleToNew(2000));
					if (penArea.isPointInShape(potBallPosOutside, Geometry.getBallRadius()))
					{
						if (Geometry.getField().isPointInShape(potBallPosOutside))
						{
							newDest = penArea.nearestPointOutside(ballPos,
									bot.getCenter2DribblerDist());
						} else
						{
							newDest = penArea.nearestPointOutside(ballPos, bot.getPos(),
									bot.getCenter2DribblerDist());
						}
					} else
					{
						newDest = penArea.nearestPointOutside(ballPos, potBallPosOutside,
								bot.getCenter2DribblerDist());
					}
				} else
				{
					newDest = penArea.nearestPointOutside(newDest, ballPos, 100);
				}
				targetAngle = ball.getPos().subtractNew(bot.getPos()).getAngle();
			}
		}
		
		// Circle theirPenAreaSmall = new Circle(Geometry.getGoalTheir().getGoalCenter(),
		// (Geometry.getGoalSize() / 2) + 300);
		// if (theirPenAreaSmall.isPointInShape(newDest))
		// {
		// newDest = Geometry.getPenaltyAreaTheir().nearestPointOutside(newDest, ballPos, 0);
		// }
		
		return new Vector3(newDest, targetAngle);
	}
	
	
	private boolean isBehindBallFromReceiver(final ITrackedBot bot, final WorldFrame wFrame)
	{
		IVector2 dir = wFrame.getBall().getPos().subtractNew(receiver);
		IVector2 ball2Bot = bot.getPos().subtractNew(wFrame.getBall().getPos());
		double angle = AngleMath.getShortestRotation(dir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(angle) < 0.2)
		{
			return true;
		}
		return false;
	}
	
	
	private boolean isBehindMovingBall(final ITrackedBot bot, final WorldFrame wFrame)
	{
		if (wFrame.getBall().getVel().getLength() < 0.1)
		{
			return true;
		}
		
		IVector2 dir = wFrame.getBall().getVel();
		IVector2 ball2Bot = bot.getPos().subtractNew(wFrame.getBall().getPos());
		double angle = AngleMath.getShortestRotation(dir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(angle) < 0.3)
		{
			return true;
		}
		return false;
	}
	
	
	private boolean armKicker(final ITrackedBot bot, final WorldFrame wFrame)
	{
		boolean armKicker = (moveMode == EMoveMode.PANIC) || (moveMode == EMoveMode.CHILL);
		double targetAngle = receiver.subtractNew(wFrame.getBall().getPos()).getAngle();
		
		// if bot is moving above threshold, make sure it moves towards ball or
		// back from ball
		if (((bot.getVel().getLength() < 0.5)) ||
				(Math.abs(AngleMath.getShortestRotation(
						bot.getVel().getAngle(),
						targetAngle)) < 0.5)
				||
				(Math.abs(AngleMath.getShortestRotation(
						bot.getVel().getAngle() + AngleMath.PI,
						targetAngle)) < 0.5))
		{
			armKicker = true;
		}
		
		return armKicker;
	}
	
	
	private boolean checkCatchBall(final ITrackedBot bot, final WorldFrame wFrame)
	{
		double dist2CatchDest = GeoMath.distancePP(bot.getPos(), catchDest.getXYVector());
		hystDist2CatchDest.update(dist2CatchDest);
		double dist2Ball = GeoMath.distancePP(bot.getBotKickerPos(), wFrame.getBall().getPos())
				- Geometry.getBallRadius();
		hystDist2Ball.update(dist2Ball);
		hystBallMoving.update(wFrame.getBall().getVel().getLength());
		
		
		if (hystBallMoving.isLower())
		{
			return false;
		}
		
		if (hystDist2Ball.isLower())
		{
			return false;
		}
		
		// if ball is moving towards receiver, do not try to catch!
		double angleBetweenBallVelAndReceiver = GeoMath.angleBetweenVectorAndVector(wFrame.getBall().getVel(),
				receiver.subtractNew(wFrame.getBall().getPos()));
		if (angleBetweenBallVelAndReceiver < 0.5)
		{
			return false;
		}
		
		return true;
	}
	
	
	private boolean checkPullBall(final ITrackedBot bot, final WorldFrame wFrame)
	{
		return !roleReady4Kick && moveDest.isPresent();
	}
	
	
	private boolean checkProtectBall(final ITrackedBot bot, final WorldFrame wFrame)
	{
		return !roleReady4Kick && protectPos.isPresent();
	}
	
	
	private EKickSkillState chooseSituation(final ITrackedBot bot, final WorldFrame wFrame)
	{
		if (moveMode == EMoveMode.PANIC)
		{
			return EKickSkillState.PANIC;
		} else if (moveMode == EMoveMode.AGGRESSIVE)
		{
			return EKickSkillState.PUSH;
		} else if (((moveMode == EMoveMode.CHILL)))
		{
			return EKickSkillState.STILL;
		} else if (moveMode == EMoveMode.CHILL_TOUCH)
		{
			return EKickSkillState.PUSH_SLOW;
		} else if (checkCatchBall(bot, wFrame))
		{
			return EKickSkillState.CATCH;
		}
		
		if (checkPullBall(bot, wFrame))
		{
			stateReady4Kick = false;
			return EKickSkillState.PULL;
		}
		if (checkProtectBall(bot, wFrame))
		{
			stateReady4Kick = false;
			return EKickSkillState.PROTECT;
		}
		return EKickSkillState.PUSH;
	}
	
	
	/**
	 * @param bot
	 * @param wFrame
	 * @return
	 */
	public IVector3 getDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		moveCon.update(wFrame, bot);
		
		finderInput = new TrajPathFinderInput(wFrame.getTimestamp());
		finderInput.setMoveCon(moveCon);
		
		obstacleGen.setUsePenAreaOur(!moveCon.isPenaltyAreaAllowedOur());
		obstacleGen.setUsePenAreaTheir(!moveCon.isPenaltyAreaAllowedTheir());
		obstacleGen.setUseGoalPostsOur(moveCon.isGoalPostObstacle());
		obstacleGen.setUseBall(false);
		stateReady4Kick = false;
		
		// roleReady4Kick = false;
		
		MoveConstraints mc = finderInput.getMoveCon().getMoveConstraints();
		if (moveMode == EMoveMode.CHILL)
		{
			mc.setAccMax(chillAcc);
			mc.setVelMax(chillVel);
		} else
		{
			mc.setAccMax(mc.getDefConstraints().getAccMax());
			mc.setVelMax(mc.getDefConstraints().getVelMax());
		}
		
		finderInput.getMoveCon().getMoveConstraints().setAccMaxW(20);
		finderInput.getMoveCon().getMoveConstraints().setVelMaxW(10);
		
		// ignore all bots, if we are near ball
		obstacleGen.setUseBots(GeoMath.distancePP(bot.getPos(), wFrame.getBall().getPos()) > 1000);
		
		// update input
		finderInput.setTrackedBot(bot);
		
		if (bot.hasBallContact())
		{
			lastBallContact = wFrame.getTimestamp();
		}
		
		final IKickSkillState catchState = kickSkillStates.get(EKickSkillState.CATCH);
		catchDest = estimateBallInterceptionIter(bot, wFrame, (in) -> catchState.getDestByTime(in),
				interceptionPathFinder);
		
		kickState = chooseSituation(bot, wFrame);
		
		StringBuilder sb = new StringBuilder();
		sb.append(kickState.name());
		sb.append('_');
		sb.append(moveMode.name());
		stateDesc = sb.toString();
		
		IKickSkillState kickSkillState = kickSkillStates.get(kickState);
		
		kickSkillState.beforePathPlanning(bot, wFrame);
		
		IVector3 dest;
		if (kickState == EKickSkillState.CATCH)
		{
			dest = catchDest;
		} else if (kickState == EKickSkillState.PUSH)
		{
			dest = estimatePush(bot, wFrame, (in) -> kickSkillStates.get(EKickSkillState.PUSH).getDestByTime(in), finder);
		} else
		{
			dest = kickSkillState.getDestByTime(new CalcDestInput(bot, wFrame, 0));
		}
		
		// generate obstacles
		finderInput.getObstacles().addAll(obstacleGen.generateObstacles(wFrame, bot.getBotId(),
				moveCon.getPrioMap()));
		
		unFilteredDest = dest;
		
		// check dest
		dest = validateDest(bot, wFrame, dest);
		finderInput.setDest(dest.getXYVector());
		finderInput.setTargetAngle(dest.z());
		
		return dest;
	}
	
	
	/**
	 * @param bot
	 * @param wFrame
	 * @return
	 */
	public Optional<TrajectoryWithTime<IVector2>> estimatePath(final ITrackedBot bot, final WorldFrame wFrame)
	{
		getDestination(bot, wFrame);
		
		// pathplanning
		final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput, wFrame.getTimestamp());
		Optional<TrajectoryWithTime<IVector2>> path = interceptionPathFinder.calcPath(localInput);
		return path;
	}
	
	
	/**
	 * @param bot
	 * @param wFrame
	 * @return
	 */
	public TrajectoryWithTime<IVector2> calculatePath(final ITrackedBot bot, final WorldFrame wFrame)
	{
		getDestination(bot, wFrame);
		
		skillReady4Kick = armKicker(bot, wFrame);
		armKicker = skillReady4Kick && roleReady4Kick && stateReady4Kick;
		
		if (!roleReady4Kick)
		{
			stateDesc += " role";
		}
		if (!stateReady4Kick)
		{
			stateDesc += " state";
		}
		if (!skillReady4Kick)
		{
			stateDesc += " skill";
		}
		
		double distBot2Ball = GeoMath.distancePP(wFrame.getBall().getPos(), bot.getPos());
		
		// only if near ball
		enableDribbler = (distBot2Ball < distForEnablingDribbler);
		// never in chill mode
		enableDribbler &= moveMode != EMoveMode.CHILL;
		// keep dribbling in pull mode
		if (kickState != EKickSkillState.PULL)
		{
			// stop if ball contact
			// enableDribbler &= !bot.hasBallContact();
			// do not dribble if ball is not rolling
			// enableDribbler &= wFrame.getBall().getVel().getLength() > 0.2;
		}
		
		if (enableDribbler)
		{
			tLastDribblerActive = wFrame.getTimestamp();
		}
		
		// pathplanning
		final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput, wFrame.getTimestamp());
		Optional<TrajectoryWithTime<IVector2>> path = finder.calcPath(localInput);
		return path.orElse(null);
	}
	
	
	private boolean isDribblerActive(final long curTimestamp)
	{
		return ((curTimestamp - tLastDribblerActive) / 1e9) < 0.1;
	}
	
	
	private boolean hasBallContact(final long curTimestamp)
	{
		return (((curTimestamp - lastBallContact) / 1e9) < 0.1);
	}
	
	
	/**
	 * @return the armKicker
	 */
	public boolean isArmKicker()
	{
		return armKicker;
	}
	
	
	/**
	 * @return the state
	 */
	public String getState()
	{
		return stateDesc;
	}
	
	
	/**
	 * @return the finderInput
	 */
	public TrajPathFinderInput getFinderInput()
	{
		return finderInput;
	}
	
	
	/**
	 * @param moveMode the moveMode to set
	 */
	public void setMoveMode(final EMoveMode moveMode)
	{
		this.moveMode = moveMode;
	}
	
	
	/**
	 * @return the obstacles
	 */
	public List<IObstacle> getObstacles()
	{
		return finderInput.getObstacles();
	}
	
	
	/**
	 * @return the obstacleGen
	 */
	public ObstacleGenerator getObstacleGen()
	{
		return obstacleGen;
	}
	
	
	/**
	 * @param distBehindBallHitTarget the distBehindBallHitTarget to set
	 */
	public void setDistBehindBallHitTarget(final double distBehindBallHitTarget)
	{
		this.distBehindBallHitTarget = distBehindBallHitTarget;
	}
	
	
	/**
	 * @return the kickState
	 */
	public EKickSkillState getKickState()
	{
		return kickState;
	}
	
	
	/**
	 * @return the enableDribbler
	 */
	public boolean isEnableDribbler()
	{
		return enableDribbler;
	}
	
	
	/**
	 * @return the receiver
	 */
	public DynamicPosition getReceiver()
	{
		return receiver;
	}
	
	
	/**
	 * @param moveCon the moveCon to set
	 */
	public void setMoveCon(final MovementCon moveCon)
	{
		this.moveCon = moveCon;
	}
	
	
	/**
	 * @return the unFilteredDest
	 */
	public IVector3 getUnFilteredDest()
	{
		return unFilteredDest;
	}
	
	
	/**
	 * @return the catchDest
	 */
	public IVector3 getCatchDest()
	{
		return catchDest;
	}
	
	
	/**
	 * @param ready4Kick
	 */
	public void setRoleReady4Kick(final boolean ready4Kick)
	{
		roleReady4Kick = ready4Kick;
	}
	
	
	/**
	 * @return
	 */
	public boolean isSkillReady4Kick()
	{
		return skillReady4Kick;
	}
	
	
	/**
	 * @param dest
	 */
	public void setDestForAvoidingOpponent(final IVector2 dest)
	{
		moveDest = Optional.ofNullable(dest);
	}
	
	
	/**
	 * 
	 */
	public void unsetDestForAvoidingOpponent()
	{
		moveDest = Optional.empty();
	}
	
	
	/**
	 * @param pos
	 */
	public void setProtectPos(final IVector2 pos)
	{
		protectPos = Optional.ofNullable(pos);
	}
	
	private static class CalcDestInput
	{
		final ITrackedBot	bot;
		final WorldFrame	wFrame;
		final double		lookahead;
		
		
		/**
		 * @param bot
		 * @param wFrame
		 * @param lookahead
		 */
		public CalcDestInput(final ITrackedBot bot, final WorldFrame wFrame, final double lookahead)
		{
			super();
			this.bot = bot;
			this.wFrame = wFrame;
			this.lookahead = lookahead;
		}
		
	}
	
	
	private interface IKickSkillState
	{
		IVector3 getDestByTime(CalcDestInput input);
		
		
		void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame);
	}
	
	private class CatchKickSkillState implements IKickSkillState
	{
		
		@Override
		public IVector3 getDestByTime(final CalcDestInput input)
		{
			stateReady4Kick = false;
			TrackedBall ball = input.wFrame.getBall();
			double lookahead = input.lookahead;
			ITrackedBot bot = input.bot;
			
			// get future ball pos
			IVector2 ballPos = ball.getPosByTime(lookahead);
			IVector2 ballVel = ball.getVel();
			IVector2 dest;
			if (ballVel.getLength() > 0)
			{
				dest = ballPos.addNew(ballVel.scaleToNew(bot.getCenter2DribblerDist() + Geometry.getBallRadius()));
			} else
			{
				dest = ballPos.addNew(
						ballPos.subtractNew(receiver).scaleTo(bot.getCenter2DribblerDist() + Geometry.getBallRadius()));
			}
			
			if (ball.getVel().getLength2() > 0)
			{
				IVector2 lp = GeoMath.leadPointOnLine(bot.getPos(), new Line(ballPos, ballVel));
				if (GeoMath.distancePP(lp, dest) < 30)
				{
					// make the destination more stable
					dest = lp;
				}
			}
			
			// IVector2 dir = ball.getPos().subtractNew(bot.getPos());
			IVector2 dir = ball.getPos().subtractNew(dest);
			if (dir.isZeroVector())
			{
				return new Vector3(dest, 0);
			}
			return new Vector3(dest, dir.getAngle());
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			IVector2 dir = wFrame.getBall().getVel();
			if (!dir.isZeroVector())
			{
				IVector2 ball2Bot = bot.getPos().subtractNew(wFrame.getBall().getPos());
				double angle = AngleMath.getShortestRotation(dir.getAngle(), ball2Bot.getAngle());
				if (Math.abs(angle) > (AngleMath.PI_HALF + 0.3))
				{
					finderInput.getObstacles().add(CircularObstacle.circleWithMargin(wFrame.getBall().getPos(), 150, 50));
				}
			} else if (!isBehindBallFromReceiver(bot, wFrame))
			{
				double approxAngle = receiver.subtractNew(wFrame.getBall().getPos()).getAngle();
				finderInput.getObstacles()
						.add(new KickBallObstacleV2(wFrame.getBall(), new Vector2(approxAngle).multiplyNew(-1), bot));
			}
			
			finderInput.getMoveCon().getMoveConstraints().setVelMaxW(3);
		}
		
	}
	
	
	@SuppressWarnings("unused")
	private class PushKickSkillState implements IKickSkillState
	{
		@Override
		public IVector3 getDestByTime(final CalcDestInput cdInput)
		{
			double lookahead;
			WorldFrame wFrame = cdInput.wFrame;
			ITrackedBot bot = cdInput.bot;
			
			IVector2 ballPos = wFrame.getBall().getPos();
			double toBallAngle = ballPos.subtractNew(bot.getPos()).getAngle(0);
			double targetAngle = receiver.subtractNew(ballPos).getAngle(0);
			
			double stepXy = 1.5;
			double stepOri = 0.3;
			double angleTol = 0.1;
			double angleDiff = Math.abs(AngleMath.getShortestRotation(toBallAngle, targetAngle));
			
			double pushDist = 20;
			// double rel = Math.min(1, angleDiff / 0.7);
			// double behindDistDef = 10;
			// pushDist -= rel * (behindDistDef + pushDist);
			
			if (hasBallContact(cdInput.wFrame.getTimestamp()))
			{
				lookahead = cdInput.lookahead;
			} else
			{
				// pushDist = 0;
				lookahead = cdInput.lookahead;
			}
			if (lookahead < 0.1)
			{
				lookahead = 0;
			}
			
			if (!roleReady4Kick)
			{
				pushDist = Math.min(-5, pushDist);
			}
			
			stateDesc += "_" + String.valueOf((int) pushDist);
			
			double nextDestAngle = targetAngle;
			if (angleDiff > stepXy)
			{
				if (Math.abs(AngleMath.getShortestRotation(toBallAngle + stepXy, targetAngle)) > Math
						.abs(AngleMath.getShortestRotation(toBallAngle - stepXy, targetAngle)))
				{
					nextDestAngle = toBallAngle - stepXy;
				} else
				{
					nextDestAngle = toBallAngle + stepXy;
				}
			}
			
			double dist = (cdInput.bot.getCenter2DribblerDist() - pushDist) + Geometry.getBallRadius();
			// IVector2 botDestForHit = cdInput.wFrame.getBall().getPosByTime(lookahead)
			// .addNew(new Vector2(nextDestAngle).scaleTo(-dist));
			
			// IVector2 destDir = botDestForHit.subtractNew(bot.getPos()).normalize();
			// if (Math.abs(AngleMath.getShortestRotation(destDir.getAngle(), targetAngle)) > (AngleMath.PI_HALF
			// + AngleMath.PI_QUART))
			// {
			// destDir = destDir.turnNew(AngleMath.PI);
			// }
			// IVector2 dest = botDestForHit.subtractNew(destDir.scaleToNew(distBehindBallHitTarget));
			
			IVector2 dest = cdInput.wFrame.getBall().getPosByTime(lookahead)
					.subtractNew(new Vector2(nextDestAngle).scaleTo(dist));
			double toDestAngle = toBallAngle; // ballPos.subtractNew(dest).getAngle(0);
			// double toDestAngle = wFrame.getBall().getPos()
			// .subtractNew(bot.getPosByTime(0.1)).getAngle(0);
			angleDiff = Math.abs(AngleMath.getShortestRotation(toDestAngle, targetAngle));
			
			double nextAngle = targetAngle;
			if (angleDiff > angleTol)
			{
				double step = Math.min(stepOri, angleDiff);
				if (Math.abs(AngleMath.getShortestRotation(toDestAngle + step, targetAngle)) > Math
						.abs(AngleMath.getShortestRotation(toDestAngle - step, targetAngle)))
				{
					nextAngle = toDestAngle - step;
				} else
				{
					nextAngle = toDestAngle + step;
				}
			} else
			{
				angleDiff = 0;
			}
			
			if (skillReady4Kick)
			{
				nextAngle = targetAngle;
			}
			stateReady4Kick = true;
			
			return new Vector3(dest, nextAngle);
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			if ((wFrame.getBall().getVel().getLength() > 0.1)
					&& (bot.getVel().getLength() > 0.1)
					&& isBehindMovingBall(bot, wFrame))
			{
				IVector2 dir = wFrame.getBall().getVel();
				IVector2 botDir = bot.getVel();
				double angle = AngleMath.getShortestRotation(dir.getAngle(), botDir.getAngle());
				if (Math.abs(angle) < 0.1)
				{
					finderInput.setFastStop(true);
				}
			}
			
			if (((wFrame.getTimestamp() - lastBallContact) / 1e9) < 0.1)
			{
				finderInput.getMoveCon().getMoveConstraints().setAccMaxW(10);
			} else
			{
				finderInput.getMoveCon().getMoveConstraints().setAccMaxW(25);
			}
		}
		
	}
	
	
	private class PushKickSkillStateV2 implements IKickSkillState
	{
		double	maxPushDist	= 100;
		IVector3	lastDest		= null;
		
		
		double getTargetAngle(final CalcDestInput input, final IVector2 ballPos)
		{
			return receiver.subtractNew(ballPos).getAngle(0);
		}
		
		
		IVector2 getTargetDest(final IVector2 ballPos, final double dist)
		{
			return GeoMath.stepAlongLine(ballPos, receiver, -dist);
		}
		
		
		@Override
		public IVector3 getDestByTime(final CalcDestInput cdInput)
		{
			ITrackedBot bot = cdInput.bot;
			IVector2 ballPos = cdInput.wFrame.getBall().getPosByTime(cdInput.lookahead);
			IVector2 curBallPos = cdInput.wFrame.getBall().getPos();
			
			if ((lastDest != null) &&
					!enableDribbler &&
					hasBallContact(cdInput.wFrame.getTimestamp()) &&
					isDribblerActive(cdInput.wFrame.getTimestamp()))
			{
				stateDesc += " wait";
				// TODO
				// return lastDest;
			}
			
			double targetAngle = getTargetAngle(cdInput, curBallPos);
			double curAngle = curBallPos.subtractNew(bot.getPos().getXYVector()).getAngle(0);
			
			double pushDist = 10;
			double angleDiff = Math.abs(AngleMath.getShortestRotation(curAngle, targetAngle));
			if ((angleDiff < 0.3) && (cdInput.wFrame.getBall().getVel().getLength() < 0.3))
			{
				pushDist = maxPushDist; // * (1 - (angleDiff / 0.2));
			}
			if (!roleReady4Kick)
			{
				pushDist = -5;
			}
			
			// double relAngleDiff = Math.min(1, angleDiff / AngleMath.PI_HALF);
			// if (relAngleDiff > AngleMath.PI_QUART)
			// {
			// relAngleDiff = AngleMath.PI_HALF - relAngleDiff;
			// }
			double dist = (cdInput.bot.getCenter2DribblerDist() - pushDist) + Geometry.getBallRadius();
			IVector2 dest = GeoMath.stepAlongLine(ballPos, bot.getPos(), dist);
			
			IVector2 targetDest = getTargetDest(ballPos, dist);
			double max = 150;
			// max = Math.min(max, GeoMath.distancePL(dest, receiver, targetDest));
			double dirDist = GeoMath.distancePP(targetDest, dest);
			dirDist = Math.min(dirDist, max);
			double turnAngle = AngleMath.PI_HALF;
			IVector2 sideDir = new Vector2(curAngle).turn(turnAngle).scaleTo(dirDist);
			
			IVector2 sideDest1 = dest.addNew(sideDir);
			IVector2 sideDest2 = dest.addNew(sideDir.turnNew(-2 * turnAngle));
			IVector2 sideDest = sideDest1;
			if (GeoMath.distancePP(sideDest1, targetDest) > GeoMath.distancePP(sideDest2, targetDest))
			{
				sideDest = sideDest2;
				// sideDest = dest.addNew(sideDir.turnNew(-2 * turnAngle));
			}
			
			double angleDiff2 = Math.abs(AngleMath.getShortestRotation(bot.getAngle(), targetAngle));
			
			double nextAngle = targetAngle;
			if (angleDiff > 0.5)
			{
				nextAngle = curBallPos.subtractNew(bot.getPos()).getAngle(0);
				
				double diff = AngleMath.getShortestRotation(nextAngle, targetAngle);
				nextAngle = nextAngle + (Math.signum(diff) * 0.5);
				
				// nextAngle = curBallPos.subtractNew(sideDest).getAngle(0);
			}
			
			if (angleDiff > 0.3)
			{
				dest = sideDest;
			} else
			{
				dest = targetDest;
			}
			
			stateReady4Kick = (angleDiff2 < 0.15); // && (bot.getaVel() < 0.5);
			DecimalFormat df = new DecimalFormat("0.00");
			stateDesc += " " + df.format(angleDiff) + " " + df.format(angleDiff2);
			
			lastDest = new Vector3(dest, nextAngle);
			return lastDest;
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			if ((wFrame.getBall().getVel().getLength() > 0.1)
					&& (bot.getVel().getLength() > 0.1)
					&& isBehindMovingBall(bot, wFrame))
			{
				IVector2 dir = wFrame.getBall().getVel();
				IVector2 botDir = bot.getVel();
				double angle = AngleMath.getShortestRotation(dir.getAngle(), botDir.getAngle());
				if (Math.abs(angle) < 0.1)
				{
					finderInput.setFastStop(true);
				}
			}
			
			if (kickState == EKickSkillState.PUSH_SLOW)
			{
				finderInput.getMoveCon().getMoveConstraints().setVelMax(0.5);
				finderInput.getMoveCon().getMoveConstraints().setVelMaxW(6);
				finderInput.getMoveCon().getMoveConstraints().setAccMax(1);
			}
		}
	}
	
	
	private class ProtectBallState extends PushKickSkillStateV2
	{
		/**
		 * 
		 */
		public ProtectBallState()
		{
			maxPushDist = -5;
		}
		
		
		@Override
		double getTargetAngle(final CalcDestInput input, final IVector2 ballPos)
		{
			return ballPos.subtractNew(protectPos.get()).getAngle(0);
		}
		
		
		@Override
		IVector2 getTargetDest(final IVector2 ballPos, final double dist)
		{
			return GeoMath.stepAlongLine(ballPos, protectPos.get(), dist);
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			
		}
	}
	
	
	private class StillKickSkillState implements IKickSkillState
	{
		
		@Override
		public IVector3 getDestByTime(final CalcDestInput input)
		{
			stateReady4Kick = true;
			WorldFrame wFrame = input.wFrame;
			ITrackedBot bot = input.bot;
			
			// direction to receiver
			IVector2 ballPos = wFrame.getBall().getPos();
			IVector2 dir = receiver.subtractNew(ballPos);
			double angle = dir.getAngle();
			
			double angleDiff = Math.abs(AngleMath.difference(bot.getAngle(), dir.getAngle(0)));
			
			double toBallDist = distBehindBallHitTarget;
			if (!roleReady4Kick
					|| !isBehindBallFromReceiver(bot, wFrame)
					|| (angleDiff > 0.2)
					|| ((bot.getVel().getLength() > 0.2)
							&& (Math.abs(AngleMath.getShortestRotation(bot.getVel().getAngle(),
									angle)) > 0.2)))
			{
				toBallDist = -100;
			}
			
			IVector2 dest = ballPos.addNew(dir
					.scaleToNew(-bot.getBot().getCenter2DribblerDist() + toBallDist));
			
			return new Vector3(dest, angle);
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			if (wFrame.getBall().getVel().getLength() > 0.2)
			{
				finderInput.setFastStop(true);
			}
			
			if (!isBehindBallFromReceiver(bot, wFrame))
			{
				// double approxAngle = receiver.subtractNew(wFrame.getBall().getPos()).getAngle();
				// finderInput.getObstacles()
				// .add(new KickBallObstacleV2(wFrame.getBall(), new Vector2(approxAngle).multiplyNew(-1), bot));
				
				obstacleGen.setUseBall(true);
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	private class PullBackKickSkillState implements IKickSkillState
	{
		double lastAngle = 0;
		
		
		@Override
		public IVector3 getDestByTime(final CalcDestInput input)
		{
			IVector2 dest = moveDest.get();
			IVector2 dest2Ball = input.wFrame.getBall().getPos().subtractNew(dest);
			
			IVector2 dest2Target = receiver.subtractNew(dest);
			double pullAngle = dest2Ball.getAngle(0);
			double pushAngle = dest2Ball.multiplyNew(-1).getAngle(0);
			IVector2 dir;
			if (Math.abs(AngleMath.getShortestRotation(pullAngle, dest2Target.getAngle(0))) < Math.abs(AngleMath
					.getShortestRotation(pushAngle, dest2Target.getAngle(0))))
			{
				// pull
				dir = dest2Ball;
			} else
			{
				// push
				dir = dest2Ball.multiplyNew(-1);
			}
			
			double targetAngle = lastAngle;
			
			// IVector2 bot2Ball = input.wFrame.getBall().getPos().subtractNew(input.bot.getPos());
			// if (bot2Ball.getLength() < 150)
			// {
			// close to ball
			if (dest2Ball.getLength() > 150)
			{
				// as long as we have some distance to dest
				targetAngle = dir.getAngle(0);
			}
			// } else
			// {
			// // far from ball -> look to ball
			// targetAngle = bot2Ball.getAngle();
			// }
			
			if (hasBallContact(input.wFrame.getTimestamp()))
			{
				dest = input.wFrame.getBall().getPosByTime(input.lookahead)
						.addNew(dir.scaleToNew((-input.bot.getCenter2DribblerDist() - Geometry.getBallRadius()) + 10));
			}
			
			// TODO avoid obstacles
			
			lastAngle = targetAngle;
			return new Vector3(dest, targetAngle);
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			// if (bot.hasBallContact())
			// {
			finderInput.getMoveCon().getMoveConstraints().setAccMax(1);
			finderInput.getMoveCon().getMoveConstraints().setVelMax(2);
			// } else
			// {
			// finderInput.getMoveCon().getMoveConstraints().setDefaultAccLimit();
			// finderInput.getMoveCon().getMoveConstraints().setDefaultVelLimit();
			// }
		}
		
	}
	
	private class PullBackKickSkillStateV2 extends PushKickSkillStateV2
	{
		boolean push = true;
		
		
		@Override
		double getTargetAngle(final CalcDestInput input, final IVector2 ballPos)
		{
			IVector2 ball2Dest = moveDest.get().subtractNew(ballPos);
			
			double targetAngle;
			if (push)
			{
				// push
				targetAngle = ball2Dest.getAngle();
			} else
			{
				// pull
				targetAngle = ball2Dest.multiplyNew(-1).getAngle();
			}
			return targetAngle;
		}
		
		
		@Override
		IVector2 getTargetDest(final IVector2 ballPos, final double dist)
		{
			if (push)
			{
				return GeoMath.stepAlongLine(ballPos, moveDest.get(), -dist);
			}
			return GeoMath.stepAlongLine(ballPos, moveDest.get(), dist);
		}
		
		
		@Override
		public IVector3 getDestByTime(final CalcDestInput input)
		{
			IVector2 ballPos = input.wFrame.getBall().getPos();
			IVector2 ball2Dest = moveDest.get().subtractNew(ballPos);
			IVector2 ball2Bot = input.bot.getPos().subtractNew(ballPos);
			
			if (GeoMath.angleBetweenVectorAndVector(ball2Dest, ball2Bot) > AngleMath.PI_HALF)
			{
				push = true;
			} else
			{
				push = false;
			}
			
			if (hasBallContact(input.wFrame.getTimestamp()))
			{
				if (push)
				{
					maxPushDist = 100;
				} else
				{
					maxPushDist = -100;
				}
			} else
			{
				maxPushDist = 0;
			}
			return super.getDestByTime(input);
		}
		
		
		@Override
		public void beforePathPlanning(final ITrackedBot bot, final WorldFrame wFrame)
		{
			if (!push)
			{
				finderInput.getMoveCon().getMoveConstraints().setAccMax(1);
				finderInput.getMoveCon().getMoveConstraints().setVelMax(1);
			} else
			{
				finderInput.getMoveCon().getMoveConstraints().setAccMax(2);
				finderInput.getMoveCon().getMoveConstraints().setVelMax(1);
			}
		}
		
	}
}
