/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;


/**
 * Kick with a single touch (taking care to not double touching the ball)
 */
public class AutoKickSampleSkill extends AMoveToSkill
{
	private final IVector2 target;
	private final double kickDuration;
	private final EKickerDevice device;


	public AutoKickSampleSkill(
			final IVector2 target,
			final EKickerDevice device,
			final double kickDuration
	)
	{
		this.target = target;
		this.device = device;
		this.kickDuration = kickDuration;

		final PrepareState prepareState = new PrepareState();
		final KickState kickState = new KickState();
		final CalmDownState calmDownState = new CalmDownState();
		setInitialState(prepareState);
		addTransition(EEvent.KICK_DONE, prepareState);
		addTransition(EEvent.PREPARED, calmDownState);
		addTransition(EEvent.CALMED_DOWN, kickState);
	}


	private enum EEvent implements IEvent
	{
		PREPARED,
		KICK_DONE,
		CALMED_DOWN,
	}

	private class PrepareState extends AState
	{
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
		}


		@Override
		public void doUpdate()
		{
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBallPos())
					.withTBot(getTBot())
					.withDestination(getDestination(20))
					.withMaxMargin(120)
					.withMinMargin(20)
					.build()
					.getAroundBallDest();

			updateDestination(dest);
			double targetOrientation = target.subtractNew(getBallPos()).getAngle(0);
			updateTargetAngle(targetOrientation);
			super.doUpdate();

			if (getPos().distanceTo(dest) < 10)
			{
				triggerEvent(EEvent.PREPARED);
			}
		}


		private IVector2 getDestination(double margin)
		{
			return LineMath.stepAlongLine(getBallPos(), target, -getDistance(margin));
		}


		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
		}


		private IVector2 getBallPos()
		{
			return getBall().getPos();
		}
	}

	private class CalmDownState extends AState
	{
		TimestampTimer timer = new TimestampTimer(1.0);


		@Override
		public void doEntryActions()
		{
			getMatchCtrl().setSkill(new BotSkillMotorsOff());
			setCurrentTrajectory(null);
			timer.reset();
		}


		@Override
		public void doUpdate()
		{
			timer.update(getWorldFrame().getTimestamp());
			if (timer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				triggerEvent(EEvent.CALMED_DOWN);
			}
		}
	}

	private class KickState extends AState
	{
		private static final double SAMPLE_TIME = 2;
		private long startTime;


		@Override
		public void doEntryActions()
		{
			startTime = getWorldFrame().getTimestamp();
			setKickParams(null);
			BotSkillLocalVelocity loc = new BotSkillLocalVelocity(Vector2.fromXY(0, 0.1), 0,
					getMoveConstraints());
			loc.getKickerDribbler().setKick(kickDuration, device, EKickerMode.ARM_TIME);
			getMatchCtrl().setSkill(loc);
			setCurrentTrajectory(null);
		}


		@Override
		public void doUpdate()
		{
			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > 1000.0)
			{
				getMatchCtrl().setSkill(new BotSkillMotorsOff());
			}

			if (((getWorldFrame().getTimestamp() - startTime) * 1e-9) > SAMPLE_TIME)
			{
				triggerEvent(EEvent.KICK_DONE);
			}
		}
	}
}
