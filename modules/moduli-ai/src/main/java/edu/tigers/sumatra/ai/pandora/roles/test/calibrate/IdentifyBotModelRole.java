/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Role for bot model identification.
 */
public class IdentifyBotModelRole extends ARole
{
	private static final Logger log = LogManager.getLogger(IdentifyBotModelRole.class);

	private static final double FINISH_W_VEL = 0.5;
	private static final double FINISH_XY_VEL = 0.2;

	private BotWatcher bw = null;
	private double accMaxXY;
	private double accMaxW;
	private List<String> dataFiles = new ArrayList<>();

	private enum EEvent implements IEvent
	{
		DONE,
	}


	public IdentifyBotModelRole(final IVector2 startPos, final IVector2 endPos, final double accMaxXY,
			final double accMaxW, final Double[] velMaxXY, final Double[] velMaxW, final int iterations)
	{
		super(ERole.IDENTIFY_BOT_MODEL);

		this.accMaxXY = accMaxXY;
		this.accMaxW = accMaxW;

		IVector2 spinPos = startPos.addNew(endPos).multiply(0.5);

		IState entryState = new InitState();
		setInitialState(entryState);

		IState lastState = entryState;

		for (int i = 0; i < iterations; i++)
		{
			lastState = addMoveTransitions(velMaxXY, startPos, endPos, lastState, 0., true);
			lastState = addMoveTransitions(velMaxXY, startPos, endPos, lastState, 0., false);
			lastState = addMoveTransitions(velMaxXY, startPos, endPos, lastState, AngleMath.deg2rad(90), false);
			lastState = addOrientationTransitions(velMaxW, spinPos, lastState);
			Double[] velMaxWInverted = Arrays.stream(velMaxW).map(vel -> -vel).toArray(Double[]::new);
			lastState = addOrientationTransitions(velMaxWInverted, spinPos, lastState);
		}

		addTransition(lastState, EEvent.DONE, new EndState());
	}


	@Override
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		bw = new BotWatcher(getBotID(), EDataAcquisitionMode.BOT_MODEL_V2, "ident-bot-model");
	}


	@Override
	protected void onCompleted()
	{
		super.onCompleted();

		// Make sure the data acquisition mode of the robot is stopped
		bw.stop();
	}


	private IState addOrientationTransitions(final Double[] velMaxW, final IVector2 position, final IState entryState)
	{
		IState lastState = entryState;
		for (double vel : velMaxW)
		{
			IState gotoStartA = new PrepareState(position, 0.0);
			IState spinBot = new SpinBotState(position, 0.0, vel);

			addTransition(lastState, EEvent.DONE, gotoStartA);
			addTransition(gotoStartA, EEvent.DONE, spinBot);
			lastState = spinBot;
		}
		return lastState;
	}


	private IState addMoveTransitions(final Double[] velMaxXY, final IVector2 startPos, final IVector2 endPos,
			final IState entryState, final Double angle, final boolean rollout)
	{
		IState lastState = entryState;
		for (double vel : velMaxXY)
		{
			double startToEndOrient = endPos.subtractNew(startPos).getAngle() + angle;
			IState gotoStartA = new PrepareState(startPos, startToEndOrient);
			IState gotoStartB = new PrepareState(endPos, startToEndOrient);

			IState moveA;
			IState moveB;

			if (rollout)
			{
				IVector2 shortEndPos = LineMath.stepAlongLine(endPos, startPos, vel * 0.5 * 1000.0);
				moveA = new MoveStateXY(shortEndPos, startToEndOrient, vel, true);

				IVector2 shortStartPos = LineMath.stepAlongLine(startPos, endPos, vel * 0.5 * 1000.0);
				moveB = new MoveStateXY(shortStartPos, startToEndOrient, vel, true);
			} else
			{
				moveA = new MoveStateXY(endPos, startToEndOrient, vel, false);
				moveB = new MoveStateXY(startPos, startToEndOrient, vel, false);
			}

			addTransition(lastState, EEvent.DONE, gotoStartA);
			addTransition(gotoStartA, EEvent.DONE, moveA);
			addTransition(moveA, EEvent.DONE, gotoStartB);
			addTransition(gotoStartB, EEvent.DONE, moveB);
			lastState = moveB;
		}
		return lastState;
	}


	private class InitState extends AState
	{
		@Override
		public void doEntryActions()
		{
			triggerEvent(EEvent.DONE);
		}
	}

	private class PrepareState extends AState
	{
		protected IVector2 dest;
		protected final double orientation;
		private TimestampTimer standStillTimer = new TimestampTimer(1.0);


		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}


		@Override
		public void doEntryActions()
		{
			standStillTimer.reset();

			MoveToSkill move = MoveToSkill.createMoveToSkill();
			move.getMoveCon().setPenaltyAreaOurObstacle(false);
			move.getMoveCon().setPenaltyAreaTheirObstacle(false);
			move.updateDestination(dest);
			move.updateTargetAngle(orientation);
			setNewSkill(move);
		}


		@Override
		public void doUpdate()
		{
			if ((getBot().getRobotInfo().getInternalState().map(BotState::getVel2).orElse(getBot().getVel()).getLength2()
					< FINISH_XY_VEL)
					&& (Math.abs(
					getBot().getRobotInfo().getInternalState().map(BotState::getAngularVel).orElse(getBot().getAngularVel()))
					< FINISH_W_VEL))
			{
				standStillTimer.update(getWFrame().getTimestamp());
			} else
			{
				standStillTimer.reset();
			}

			if (standStillTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				onDone();
				triggerEvent(EEvent.DONE);
			}
		}


		protected void onDone()
		{
			// can be overwritten
		}
	}

	private class MoveStateXY extends PrepareState
	{
		private double vMax;
		private boolean rollout;


		private MoveStateXY(final IVector2 dest, final double orientation, final double vMax, final boolean rollout)
		{
			super(dest, orientation);
			this.vMax = vMax;
			this.rollout = rollout;
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();

			bw.start();

			MoveBangBangSkill posSkill = new MoveBangBangSkill(dest, orientation, EBotSkill.GLOBAL_POSITION);
			posSkill.setAccMax(accMaxXY);
			posSkill.setVelMax(vMax);
			posSkill.setRollOut(rollout);
			setNewSkill(posSkill);
		}


		@Override
		protected void onDone()
		{
			bw.stop();
			dataFiles.add(bw.getAbsoluteFileName());
		}
	}

	private class SpinBotState extends PrepareState
	{

		private double wSpeed;
		private boolean isRollout = false;
		private long tHoldSpeed = Long.MAX_VALUE;


		private SpinBotState(final IVector2 pos, final double orientation, final double wSpeed)
		{
			super(pos, orientation);
			this.wSpeed = wSpeed;
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();

			bw.start();

			BotSkillWrapperSkill spinSkill = new BotSkillWrapperSkill();
			MoveConstraints mc = new MoveConstraints(getBot().getRobotInfo().getBotParams().getMovementLimits());
			mc.setAccMax(accMaxXY);
			mc.setAccMaxW(accMaxW);
			BotSkillLocalVelocity botSkill = new BotSkillLocalVelocity(Vector2.zero(), wSpeed, mc);
			spinSkill.setSkill(botSkill);
			setNewSkill(spinSkill);
		}


		@Override
		public void doUpdate()
		{
			final Optional<State> filteredState = getBot().getFilteredState();
			if (!isRollout && filteredState.isPresent()
					&& (Math.abs(wSpeed) <= Math.abs(filteredState.get().getAngularVel()))
					&& (tHoldSpeed == Long.MAX_VALUE))
			{
				tHoldSpeed = getWFrame().getTimestamp();
			}
			final double HOLD_SPIN_SPEED = 3e9;
			if (!isRollout && ((getWFrame().getTimestamp() - tHoldSpeed) >= HOLD_SPIN_SPEED))
			{
				MoveConstraints mc = new MoveConstraints(getBot().getRobotInfo().getBotParams().getMovementLimits());
				mc.setAccMax(accMaxXY);
				mc.setAccMaxW(accMaxW);
				BotSkillLocalForce rolloutSkill = new BotSkillLocalForce();

				BotSkillWrapperSkill skill = new BotSkillWrapperSkill();
				skill.setSkill(rolloutSkill);
				setNewSkill(skill);
				isRollout = true;
			}
			if (isRollout)
			{
				super.doUpdate();
			}
		}


		@Override
		protected void onDone()
		{
			bw.stop();
			dataFiles.add(bw.getAbsoluteFileName());
		}
	}

	private class EndState extends AState
	{
		@Override
		public void doEntryActions()
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval("addpath('identification')");
				Object[] values = mp.returningFeval("identifyBotModelV2", 1,
						dataFiles.toArray());
				double[] params = (double[]) values[0];

				double[] friction = new double[4];
				System.arraycopy(params, 0, friction, 0, friction.length);
				double[] efficiency = new double[2];
				System.arraycopy(params, friction.length, efficiency, 0, efficiency.length);
				double[] encoderModel = new double[4];
				System.arraycopy(params, friction.length + efficiency.length, encoderModel, 0, encoderModel.length);

				final String format = "%7.4f";
				log.info("xy/coulomb [N]:      {}", () -> String.format(format, friction[1]));
				log.info("xy/viscous [Ns/m]:   {}", () -> String.format(format, friction[0]));
				log.info(" w/coulomb [N]:      {}", () -> String.format(format, friction[3]));
				log.info(" w/viscous [Ns/m]:   {}", () -> String.format(format, friction[2]));
				log.info("xy/efficiency [-]:   {}", () -> String.format(format, efficiency[0]));
				log.info(" w/efficiency [-]:   {}", () -> String.format(format, efficiency[1]));
				log.info("enc/gain/x [-]:      {}", () -> String.format(format, encoderModel[0]));
				log.info("enc/gain/y [-]:      {}", () -> String.format(format, encoderModel[1]));
				log.info("enc/time/x [-]:      {}", () -> String.format(format, encoderModel[2]));
				log.info("enc/time/y [-]:      {}", () -> String.format(format, encoderModel[3]));
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			}
			setCompleted();
		}
	}
}
