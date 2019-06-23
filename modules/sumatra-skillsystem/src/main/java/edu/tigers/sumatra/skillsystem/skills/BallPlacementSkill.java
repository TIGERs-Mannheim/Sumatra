/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;


/**
 * Ball placement to given position (when no disturbance expected)
 *
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de> on 04.03.2017.
 */
public class BallPlacementSkill extends AMoveSkill
{
	
	@Configurable(comment = "Speed when near ball", defValue = "0.1")
	private static double chillVel = 0.1;
	@Configurable(comment = "How fast to accelerate when near ball", defValue = "0.8")
	private static double chillAcc = 0.8;
	@Configurable(comment = "Tolerated distance of ball to target position", defValue = "20.0")
	private static double destTolerance = 20;
	@Configurable(comment = "[ms] Time to wait after Turnoff of dribbler and release of Ball", defValue = "500.0")
	private static double waitTimeBeforeRelease = 500;
	@Configurable(comment = "The distance between kicker and ball to keep before dribbling the ball", defValue = "100.0")
	private static double minDistBeforeDribble = 100;
	@Configurable(comment = "The distance the bot should go to after ball release", defValue = "200.0")
	private static double distToBallAfterRelease = 2 * Geometry.getBotRadius();
	@Configurable(comment = "dribbler speed for Pullback", defValue = "7000.0")
	private static double dribblerSpeed = 7000;
	@Configurable(comment = "Target detection precision", defValue = "10.0")
	private static double drivePrecision = 10;
	
	private final IVector2 target;
	private double newDribblerSpeed = 0;
	private double targetOrientation;
	private boolean destinationReached = false;
	
	
	/**
	 * @param target new ball position
	 */
	public BallPlacementSkill(final IVector2 target)
	{
		super(ESkill.BALL_PLACEMENT);
		this.target = target;
		
		setInitialState(new BeginDribblingState());
		addTransition(EPlacementEvent.MOVE_TO_BALL, new BeginDribblingState());
		addTransition(EPlacementEvent.CORRECT_BOT_POSITION, new GetBallState());
		addTransition(EPlacementEvent.DRIBBLING_BALL, new BallPlacementState());
		addTransition(EPlacementEvent.DESTINATION_REACHED, new BallReleaseState());
	}
	
	
	public static void setChillVel(final double chillVel)
	{
		BallPlacementSkill.chillVel = chillVel;
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		if (!destinationReached && getBall().getPos().distanceTo(target) < destTolerance)
		{
			destinationReached = true;
			triggerEvent(EPlacementEvent.DESTINATION_REACHED);
		}
	}
	
	
	@Override
	protected void onSkillStarted()
	{
		getMoveCon().setBotsObstacle(true);
		getMoveCon().setDestinationOutsideFieldAllowed(true);
		getMoveCon().setArmChip(false);
		getMoveCon().setPenaltyAreaAllowedOur(true);
		getMoveCon().setGoalPostObstacle(true);
	}
	
	
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		kickerDribblerOutput.setDribblerSpeed(newDribblerSpeed);
	}
	
	
	protected enum EPlacementEvent implements IEvent
	{
		MOVE_TO_BALL,
		CORRECT_BOT_POSITION,
		DESTINATION_REACHED,
		DRIBBLING_BALL
	}
	
	private class BeginDribblingState extends MoveToState
	{
		
		protected BeginDribblingState()
		{
			super(BallPlacementSkill.this);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getPos())
					.withTBot(getTBot())
					.withDestination(getTarget())
					.withMaxMargin(150)
					.withMinMargin(minDistBeforeDribble)
					.build()
					.getAroundBallDest();
			
			getMoveCon().updateDestination(dest);
			super.doUpdate();
			if (getPos().distanceTo(dest) < drivePrecision)
			{
				triggerEvent(EPlacementEvent.CORRECT_BOT_POSITION);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			newDribblerSpeed = dribblerSpeed;
		}
		
	}
	
	private class GetBallState implements IState
	{
		private IVector2 lastBallPos;
		private TimestampTimer timer;
		private TimestampTimer securityTimer;
		
		
		private double getTargetOrientation(final IVector2 dest)
		{
			return getBall().getPos().subtractNew(dest).getAngle(0);
		}
		
		
		@Override
		public void doEntryActions()
		{
			lastBallPos = getBall().getPos();
			targetOrientation = getTargetOrientation(getPos());
			getMoveCon().getMoveConstraints().setVelMax(chillVel);
			getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			timer = new TimestampTimer(0.5);
			securityTimer = new TimestampTimer(3);
		}
		
		
		@Override
		public void doUpdate()
		{
			setTargetPose(lastBallPos, targetOrientation);
			if (getTBot().hasBallContact())
			{
				triggerEvent(EPlacementEvent.DRIBBLING_BALL);
			} else if (Math.abs(getTBot().getBotKickerPos().distanceTo(lastBallPos) - Geometry.getBallRadius()) < 5)
			{
				timer.update(getWorldFrame().getTimestamp());
				if (timer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					triggerEvent(EPlacementEvent.MOVE_TO_BALL);
				}
			} else
			{
				securityTimer.update(getWorldFrame().getTimestamp());
				if (securityTimer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					triggerEvent(EPlacementEvent.MOVE_TO_BALL);
				}
			}
		}
	}
	
	private class BallPlacementState implements IState
	{
		IVector2 dest = Vector2.zero();
		private double nearBallTol = Geometry.getBotRadius() + Geometry.getBallRadius() + drivePrecision;
		
		
		@Override
		public void doEntryActions()
		{
			getMoveCon().getMoveConstraints().setVelMax(chillVel);
			getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			dest = LineMath.stepAlongLine(getTarget(), getPos(),
					-(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + 5));
		}
		
		
		@Override
		public void doUpdate()
		{
			setTargetPose(dest, targetOrientation);
			if (!getTBot().hasBallContact() && (getBall().getPos().distanceTo(getPos()) > nearBallTol))
			{
				destinationReached = false;
				triggerEvent(EPlacementEvent.MOVE_TO_BALL);
			} else if (getPos().distanceTo(dest) < destTolerance)
			{
				destinationReached = true;
				triggerEvent(EPlacementEvent.DESTINATION_REACHED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			newDribblerSpeed = 0;
			setTargetPose(getPos(), getAngle());
			
			IBotMovementLimits moveLimits = getBot().getBotParams().getMovementLimits();
			
			getMoveCon().getMoveConstraints().setVelMax(moveLimits.getVelMax());
			getMoveCon().getMoveConstraints().setAccMax(moveLimits.getAccMax());
		}
	}
	
	private class BallReleaseState implements IState
	{
		private long startTime;
		private IVector2 dest;
		
		
		@Override
		public void doEntryActions()
		{
			startTime = getWorldFrame().getTimestamp();
			IVector2 addOn = getPos().subtractNew(getBall().getPos()).scaleTo(distToBallAfterRelease);
			dest = getPos().addNew(addOn);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if ((((getWorldFrame().getTimestamp() - startTime) / 1e6) > waitTimeBeforeRelease)
					&& (getPos().distanceTo(dest) > drivePrecision))
			{
				setTargetPose(dest, getAngle());
				if (getBall().getPos().distanceTo(getPos()) < distToBallAfterRelease)
				{
					dest = LineMath.stepAlongLine(getPos(), getBall().getPos(), -distToBallAfterRelease);
				}
			}
		}
	}
}
