/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillCircleBall;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysterese;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.AroundObstacleCalc;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * Normal kick mode. Drive around the ball and kick it. <br>
 * No catch. No chill. Simply kick ball fast.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickNormalSkill extends AKickSkill
{
	@Configurable(comment = "Use the circle bot skill for kicks", defValue = "false")
	private static boolean enableCircleSkill = false;
	
	@Configurable(comment = "The max margin to the ball for destinations", defValue = "20.0")
	private static double maxMarginToBall = 20.0;
	
	@Configurable(comment = "Dribble speed", defValue = "7000")
	private static int dribbleSpeed = 7000;
	
	@Configurable(defValue = "0.5")
	private static double maxLookahead = 0.5;
	
	@Configurable(defValue = "true")
	private static boolean pushBall = true;
	
	private boolean panic = false;
	private boolean circleActive = false;
	
	
	/**
	 * New Instance
	 * 
	 * @param receiver the shoot target
	 * @param kickMode
	 * @param kickerDevice
	 * @param kickSpeed
	 */
	public KickNormalSkill(final DynamicPosition receiver, EKickMode kickMode, EKickerDevice kickerDevice,
			double kickSpeed)
	{
		this(receiver);
		setKickSpeed(kickSpeed);
		setKickMode(kickMode);
		setDevice(kickerDevice);
	}
	
	
	/**
	 * New Instance
	 *
	 * @param receiver the shoot target
	 */
	public KickNormalSkill(final DynamicPosition receiver)
	{
		super(ESkill.KICK_NORMAL, receiver);
		
		setInitialState(new KickState());
	}
	
	
	@Override
	protected void updateKickerParams(final KickerParams kickerParams)
	{
		super.updateKickerParams(kickerParams);
		
		if (!panic && !isReadyAndFocussed())
		{
			kickerParams.setMode(EKickerMode.DISARM);
		}
		
		double targetOrientation = receiver.subtractNew(getBall().getPos()).getAngle(getAngle());
		if (Math.abs(AngleMath.difference(targetOrientation, getAngle())) < 0.2)
		{
			kickerParams.setDribbleSpeed(0);
		} else
		{
			kickerParams.setDribbleSpeed(dribbleSpeed);
		}
	}
	
	
	private class KickState extends MoveToState
	{
		private double dist2Ball = 0;
		
		DoubleChargingValue chargingValue = new DoubleChargingValue(
				new Hysterese(30, 50),
				0, 0.9, -0.9, 0, maxLookahead);
		
		
		protected KickState()
		{
			super(KickNormalSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			getMoveCon().setIgnoreGameStateObstacles(true);
			
			dist2Ball = 20;
			
			boolean opponentNearBall = getWorldFrame().getFoeBots().values().stream()
					.anyMatch(bot -> bot.getPos().distanceTo(getBall().getPos()) < 300);
			if (pushBall && opponentNearBall)
			{
				minMarginChargeValue = MinMarginChargeValue.aMinMargin()
						.withDefaultValue(0)
						.withChargeRate(-400)
						.withLowerThreshold(90)
						.withUpperThreshold(100)
						.withLimit(-250)
						.build();
			} else
			{
				minMarginChargeValue = MinMarginChargeValue.aMinMargin()
						.withDefaultValue(10)
						.withChargeRate(-200)
						.withLowerThreshold(70)
						.withUpperThreshold(90)
						.withLimit(-250)
						.build();
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			
			double circleSkillMargin = circleActive ? 50 : 20;
			if (enableCircleSkill
					&& (getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < Geometry.getBallRadius()
							+ circleSkillMargin)
					&& getBall().getVel().getLength2() < 0.5)
			{
				circleActive = true;
				updateCircleSkill();
				return;
			}
			circleActive = false;
			
			double curBot2BallDist = getBall().getPos().distanceTo(getPos());
			getMoveCon().setBotsObstacle(curBot2BallDist > 1000);
			
			double lookahead;
			double currentDist2Ball = getBall().getTrajectory().getTravelLine().distanceTo(getTBot().getBotKickerPos())
					- Geometry.getBallRadius();
			if (isBehindBall())
			{
				currentDist2Ball = getBall().getPos().distanceTo(getTBot().getBotKickerPos());
			}
			chargingValue.update(currentDist2Ball, getWorldFrame().getTimestamp());
			lookahead = chargingValue.getValue();
			IVector2 shift = getBall().getTrajectory().getPosByTime(lookahead).subtractNew(getBall().getPos());
			
			Pose pose = findTargetPose(0.0);
			IVector2 dest = pose.getPos().addNew(shift);
			double targetOrientation = pose.getOrientation();
			
			IVector2 desiredKickerDest = BotShape.getKickerCenterPos(dest, targetOrientation,
					getTBot().getCenter2DribblerDist());
			List<IPenaltyArea> consideredPenaltyAreas = new ArrayList<>();
			if (getMoveCon().isPenaltyAreaForbiddenOur())
			{
				consideredPenaltyAreas.add(Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2));
			}
			if (getMoveCon().isPenaltyAreaForbiddenTheir())
			{
				consideredPenaltyAreas.add(Geometry.getPenaltyAreaTheir());
			}
			desiredKickerDest = SkillUtil.movePosOutOfPenAreaWrtBall(desiredKickerDest, getBall(),
					consideredPenaltyAreas.toArray(new IPenaltyArea[consideredPenaltyAreas.size()]));
			desiredKickerDest = SkillUtil.moveBotKickerInsideFieldWrtBall(getTBot(),
					Pose.from(desiredKickerDest, targetOrientation), getBall().getPos());
			dest = BotShape.getCenterFromKickerPos(desiredKickerDest, targetOrientation,
					getTBot().getCenter2DribblerDist());
			dest = Geometry.getFieldWReferee().nearestPointInside(dest, -Geometry.getBotRadius() * 2);
			
			getMoveCon().updateDestination(dest);
			getMoveCon().updateTargetAngle(targetOrientation);
			super.doUpdate();
			
			dist2Ball = getDistanceToBall(dest, lookahead);
			
			getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(),
					String.format("%.2f|%.2f|%s", dist2Ball, lookahead,
							isReadyAndFocussed() ? "ready" : "aiming"))
									.setOffset(Vector2.fromY(300)));
		}
		
		
		private boolean isBehindBall()
		{
			IVector2 bot2Ball = getTBot().getPos().subtractNew(getBall().getPos());
			double angle = bot2Ball.angleToAbs(getBall().getVel()).orElse(0.0);
			return angle > AngleMath.PI_HALF;
		}
		
		
		private double getDistanceToBall(final IVector2 dest, double lookahead)
		{
			if (isReadyForKick())
			{
				double dist = dest.distanceTo(getPos());
				if (getBall().getVel().getLength2() > 0.1 && lookahead < 0.1)
				{
					dist = 0;
				}
				minMarginChargeValue.updateMinMargin(dist, getWorldFrame().getTimestamp());
				return minMarginChargeValue.getMinMargin();
			}
			minMarginChargeValue.reset();
			return minDistBeforeKick;
		}
		
		
		private void updateCircleSkill()
		{
			double speed = 1.0;
			double radius = 300;
			double friction = 0.05;
			double dribblerSpeed = 0;
			MoveConstraints moveConstraints = new MoveConstraints(getMoveCon().getMoveConstraints());
			moveConstraints.setAccMax(3.0);
			moveConstraints.setAccMaxW(50);
			moveConstraints.setJerkMax(30);
			moveConstraints.setJerkMaxW(500);
			
			double targetAngle = receiver.subtractNew(getBall().getPos()).getAngle(0);
			IVector2 ball2Dest = receiver.subtractNew(getBall().getPos());
			IVector2 ball2Bot = getPos().subtractNew(getBall().getPos());
			
			if (getWorldFrame().isInverted())
			{
				targetAngle = AngleMath.normalizeAngle(targetAngle + AngleMath.PI);
			}
			if (ball2Dest.angleTo(ball2Bot).orElse(0.0) < 0)
			{
				radius *= -1;
			}
			BotSkillCircleBall botSkill = new BotSkillCircleBall(speed, radius, targetAngle, friction, moveConstraints);
			updateKickerDribbler(botSkill.getKickerDribbler());
			botSkill.getKickerDribbler().setDribblerSpeed(dribblerSpeed);
			getMatchCtrl().setSkill(botSkill);
		}
		
		
		private boolean directlyTowardsBall()
		{
			return getWorldFrame().getFoeBots().values().stream()
					.anyMatch(tBot -> tBot.getPos().distanceTo(getBall().getPos()) < 300);
		}
		
		
		private Pose findTargetPose(double lookahead)
		{
			IVector2 desiredDestination = getDestination(0, lookahead);
			IVector2 dest = desiredDestination;
			double targetOrientation;
			
			Optional<IVector2> optObstacle = getWorldFrame().getFoeBots().values().stream()
					.map(ITrackedObject::getPos)
					.sorted(new VectorDistanceComparator(getPos()))
					.findFirst();
			if (optObstacle.isPresent())
			{
				IVector2 obstacle = optObstacle.get();
				AroundObstacleCalc aroundObstacleCalc = new AroundObstacleCalc(obstacle, getBallPos(lookahead), getTBot());
				
				if (aroundObstacleCalc.isAroundObstacleNeeded(desiredDestination))
				{
					dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
					dest = aroundBall(dest, lookahead, dist2Ball);
					dest = aroundObstacleCalc.avoidObstacle(dest);
					targetOrientation = getBallPos(lookahead).subtractNew(getPos()).getAngle(0);
					targetOrientation = aroundObstacleCalc.adaptTargetOrientation(targetOrientation);
				} else if (enableCircleSkill && directlyTowardsBall())
				{
					dest = LineMath.stepAlongLine(getBall().getPos(), getPos(), getTBot().getCenter2DribblerDist() - 5);
					dest = aroundObstacleCalc.getAroundObstacleDest().orElse(dest);
					dest = aroundObstacleCalc.avoidObstacle(dest);
					targetOrientation = getBall().getPos().subtractNew(dest).getAngle();
				} else
				{
					dest = aroundBall(dest, lookahead, dist2Ball);
					dest = aroundObstacleCalc.avoidObstacle(dest);
					if (dest.distanceTo(obstacle) < Geometry.getBotRadius() * 2)
					{
						// if dest is inside obstacle, look to ball
						targetOrientation = getBall().getPos().subtractNew(dest).getAngle();
					} else
					{
						targetOrientation = getTargetOrientation();
					}
				}
			} else
			{
				// fallback if no enemies present
				dest = aroundBall(dest, lookahead, dist2Ball);
				targetOrientation = getTargetOrientation();
			}
			return Pose.from(dest, targetOrientation);
		}
		
		
		private IVector2 aroundBall(IVector2 dest, double lookahead, double minMargin)
		{
			return AroundBallCalc.aroundBall()
					.withBallPos(getBallPos(lookahead))
					.withTBot(getTBot())
					.withDestination(dest)
					.withMaxMargin(maxMarginToBall)
					.withMinMargin(minMargin)
					.build()
					.getAroundBallDest();
		}
		
		
		private IVector2 getDestination(double margin, double lookahead)
		{
			return LineMath.stepAlongLine(getBallPos(lookahead), getReceiver(), -getDistance(margin));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
		}
		
		
		private IVector2 getBallPos(double lookahead)
		{
			return getBall().getTrajectory().getPosByTime(lookahead);
		}
	}
	
	
	/**
	 * @param panic if true, always arm kicker to kick ball away as fast as possible
	 */
	public void setPanic(final boolean panic)
	{
		this.panic = panic;
	}
}
