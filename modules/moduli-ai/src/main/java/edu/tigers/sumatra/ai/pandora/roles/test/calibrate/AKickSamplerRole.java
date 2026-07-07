package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ATouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


/**
 * Base role for sampling kicks for ball calibration.
 */
public abstract class AKickSamplerRole extends ARole
{
	protected final List<SamplePoint> samples = new ArrayList<>();


	protected AKickSamplerRole(ERole role, EKickMode kickMode)
	{
		super(role);

		var waitState = new WaitState();
		var sampleDoneState = new SampleDoneState();
		waitState.addTransition("ballStopped", waitState::ballStopped, sampleDoneState);

		if (kickMode == EKickMode.SINGLE_TOUCH)
		{
			var touchState = new SingleTouchState();
			touchState.addTransition("ballMoved", touchState::ballMoved, waitState);
			sampleDoneState.addTransition("hasMoreSamples", sampleDoneState::hasMoreSamples, touchState);

			setInitialState(touchState);
		} else if (kickMode == EKickMode.TOUCH)
		{
			var moveBehindBallAndWaitState = new MoveBehindBallAndWaitState();
			var spinUpBallState = new SpinUpBallState();
			var touchState = new TouchState();

			moveBehindBallAndWaitState.addTransition(
					moveBehindBallAndWaitState::ballStoppedAndDestinationReached, spinUpBallState);
			spinUpBallState.addTransition(spinUpBallState::strongTractionAchieved, touchState);
			spinUpBallState.addTransition(spinUpBallState::timeout, waitState);
			touchState.addTransition("ballMoved", touchState::ballMoved, waitState);
			sampleDoneState.addTransition("hasMoreSamples", sampleDoneState::hasMoreSamples, moveBehindBallAndWaitState);

			setInitialState(moveBehindBallAndWaitState);
		} else
		{
			throw new IllegalArgumentException("Unsupported kick mode: " + kickMode);
		}
	}


	protected static class SamplePoint
	{
		IVector2 kickPos;
		IVector2 kickTarget;
		double targetAngle;
		double durationMs;
		EKickerDevice device;
		double rightOffset;
	}


	private class ATouchKickState<T extends ATouchKickSkill> extends RoleState<T>
	{
		private IVector2 initBallPos;


		public ATouchKickState(Supplier<T> skillSupplier)
		{
			super(skillSupplier);
		}


		@Override
		protected void onEntry()
		{
			initBallPos = getBall().getPos();
			var sample = samples.getFirst();
			skill.setTarget(sample.kickTarget);
			skill.setKickerDevice(sample.device);
			skill.setKickArmTime(sample.durationMs);
			skill.setMoveCon(skill.getMoveCon().withPhysicalObstaclesOnly());
		}


		@Override
		protected void onExit()
		{
			samples.removeFirst();
		}


		boolean ballMoved()
		{
			return getBall().getPos().distanceTo(initBallPos) > 50;
		}
	}

	private class SingleTouchState extends ATouchKickState<SingleTouchKickSkill>
	{
		public SingleTouchState()
		{
			super(SingleTouchKickSkill::new);
		}
	}


	private class TouchState extends ATouchKickState<TouchKickSkill>
	{
		public TouchState()
		{
			super(TouchKickSkill::new);
		}
	}

	private class SpinUpBallState extends MoveState
	{
		IVector2 destination;
		IVector2 initialBallPos;

		TimestampTimer successTimer = new TimestampTimer(0.3);
		TimestampTimer failTimer = new TimestampTimer(2.0);


		@Override
		protected void onEntry()
		{
			super.onEntry();
			destination = getPos();
			initialBallPos = getBall().getPos();
			failTimer.reset();
			successTimer.reset();
		}


		@Override
		protected void onUpdate()
		{
			super.onUpdate();

			var sample = samples.getFirst();
			var ballToTarget = sample.kickTarget.subtractNew(initialBallPos);
			destination = initialBallPos.subtractNew(ballToTarget.scaleToNew(-50));

			skill.setVelMax(0.3);
			skill.updateTargetAngle(ballToTarget.getAngle());
			skill.setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
			skill.setMoveCon(skill.getMoveCon().withBallObstacle(false));

			if (getBot().getBallContact().hasContact())
			{
				skill.setComeToAStop(true);
			}

			skill.updateDestination(destination);
		}


		public boolean strongTractionAchieved()
		{
			if (getBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
			{
				successTimer.update(getWFrame().getTimestamp());
				return successTimer.isTimeUp(getWFrame().getTimestamp());
			}

			successTimer.reset();
			return false;
		}


		public boolean timeout()
		{
			failTimer.update(getWFrame().getTimestamp());
			return failTimer.isTimeUp(getWFrame().getTimestamp());
		}
	}

	private class MoveBehindBallAndWaitState extends MoveState
	{
		IVector2 destination;

		TimestampTimer successTimer = new TimestampTimer(1.0);


		@Override
		protected void onEntry()
		{
			super.onEntry();
			destination = getPos();
			successTimer.reset();
		}


		@Override
		protected void onUpdate()
		{
			super.onUpdate();

			var sample = samples.getFirst();
			var ballToTarget = sample.kickTarget.subtractNew(getBall().getPos());
			destination = getBall().getPos().subtractNew(ballToTarget.scaleToNew(300));
			skill.updateDestination(destination);
			skill.updateTargetAngle(ballToTarget.getAngle());
			skill.setComeToAStop(getWFrame().getBall().getVel().getLength() > 2.0);
			skill.setMoveCon(skill.getMoveCon().withBallObstacle(true));
		}


		private boolean ballStoppedAndDestinationReached()
		{
			if (getPos().distanceTo(destination) > 30)
			{
				successTimer.reset();
				return false;
			}

			successTimer.update(getWFrame().getTimestamp());
			return getBall().getVel().getLength() < 0.2 && successTimer.isTimeUp(getWFrame().getTimestamp());
		}
	}

	private class WaitState extends RoleState<IdleSkill>
	{
		public WaitState()
		{
			super(IdleSkill::new);
		}


		private boolean ballStopped()
		{
			return getBall().getVel().getLength() < 0.2 || !Geometry.getField().isPointInShape(getBall().getPos());
		}
	}

	private class SampleDoneState extends RoleState<IdleSkill>
	{
		public SampleDoneState()
		{
			super(IdleSkill::new);
		}


		@Override
		public void onEntry()
		{
			if (samples.isEmpty())
			{
				// note: this triggers doExitActions, even if called from doExitActions!
				setCompleted();
			}
		}


		private boolean hasMoreSamples()
		{
			return !samples.isEmpty();
		}
	}


	public enum EKickMode
	{
		TOUCH,
		SINGLE_TOUCH,
	}
}