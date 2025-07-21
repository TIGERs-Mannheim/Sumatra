/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ATouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;

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
			var getBallState = new GetBallState();
			var touchState = new TouchState();

			getBallState.addTransition(ESkillState.SUCCESS, touchState);
			touchState.addTransition("ballMoved", touchState::ballMoved, waitState);
			sampleDoneState.addTransition("hasMoreSamples", sampleDoneState::hasMoreSamples, touchState);

			setInitialState(getBallState);
		} else
		{
			throw new IllegalArgumentException("Unsupported kick mode: " + kickMode);
		}
	}


	private IVector2 getKickTarget()
	{
		var sample = samples.getFirst();
		return sample.kickPos.addNew(Vector2.fromAngleLength(sample.targetAngle, 1000));
	}


	protected static class SamplePoint
	{
		IVector2 kickPos;
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
		protected void onInit()
		{
			initBallPos = getBall().getPos();
			var sample = samples.getFirst();
			skill.setTarget(getKickTarget());
			skill.setKickerDevice(sample.device);
			skill.setKickArmTime(sample.durationMs);
			skill.getMoveCon().physicalObstaclesOnly();
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

	private class GetBallState extends RoleState<ProtectiveGetBallSkill>
	{
		public GetBallState()
		{
			super(ProtectiveGetBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.setTarget(getKickTarget());
			skill.setStrongDribblerContactNeeded(true);
			skill.getMoveCon().physicalObstaclesOnly();
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
		public void doEntryActions()
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