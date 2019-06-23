/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class IdentifyBotModelRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MoveTestRole.class.getName());
	
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
		bw = new BotWatcher(getABot(), EDataAcquisitionMode.BOT_MODEL_V2);
	}
	
	
	@Override
	protected void onCompleted()
	{
		super.onCompleted();
		
		// Make sure the data acquisition mode of the robot is stopped
		bw.stop();
	}
	
	
	private ABot getABot()
	{
		ABot aBot = null;
		try
		{
			ABotManager botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			aBot = botManager.getBots().get(getBotID());
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find botManager module", e);
		}
		return aBot;
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
			final IState entryState,
			final Double angle, final boolean rollout)
	{
		IState lastState = entryState;
		for (double vel : velMaxXY)
		{
			double startToEndOrient = endPos.subtractNew(startPos).getAngle() + angle;
			IState gotoStartA = new PrepareState(startPos, startToEndOrient);
			IState moveA = new MoveStateXY(endPos, startToEndOrient, vel, rollout);
			
			IState gotoStartB = new PrepareState(endPos, startToEndOrient);
			IState moveB = new MoveStateXY(startPos, startToEndOrient, vel, rollout);
			
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
		private TimestampTimer standStillTimer = new TimestampTimer(0.5);
		
		
		private PrepareState(final IVector2 dest, final double orientation)
		{
			this.dest = dest;
			this.orientation = orientation;
		}
		
		
		@Override
		public void doEntryActions()
		{
			standStillTimer.reset();
			
			AMoveToSkill move = AMoveToSkill.createMoveToSkill();
			move.getMoveCon().updateDestination(dest);
			move.getMoveCon().updateTargetAngle(orientation);
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getBot().getVel().getLength2() < FINISH_XY_VEL) && (Math.abs(getBot().getAngularVel()) < FINISH_W_VEL))
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
		private boolean rollout = false;
		
		
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
			posSkill.getMoveCon().getMoveConstraints().setAccMax(accMaxXY);
			posSkill.getMoveCon().getMoveConstraints().setVelMax(vMax);
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
			MoveConstraints mc = new MoveConstraints(getBot().getMoveConstraints());
			mc.setAccMax(accMaxXY);
			mc.setAccMaxW(accMaxW);
			BotSkillLocalVelocity botSkill = new BotSkillLocalVelocity(Vector2.zero(), wSpeed, mc);
			spinSkill.setSkill(botSkill);
			setNewSkill(spinSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (!isRollout && getBot().getFilteredState().isPresent()
					&& (Math.abs(wSpeed) <= Math.abs(getBot().getFilteredState().get().getAngularVel()))
					&& (tHoldSpeed == Long.MAX_VALUE))
			{
				tHoldSpeed = getWFrame().getTimestamp();
			}
			final double HOLD_SPIN_SPEED = 3e9;
			if (!isRollout && ((getWFrame().getTimestamp() - tHoldSpeed) >= HOLD_SPIN_SPEED))
			{
				MoveConstraints mc = new MoveConstraints();
				mc.setAccMax(accMaxXY);
				mc.setAccMaxW(accMaxW);
				BotSkillLocalForce rolloutSkill = new BotSkillLocalForce(mc);
				
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
						dataFiles.toArray(new Object[dataFiles.size()]));
				double[] params = (double[]) values[0];
				
				double[] friction = new double[4];
				System.arraycopy(params, 0, friction, 0, friction.length);
				double[] efficiency = new double[2];
				System.arraycopy(params, friction.length, efficiency, 0, efficiency.length);
				double[] encoderScaling = new double[4];
				System.arraycopy(params, friction.length + efficiency.length, encoderScaling, 0, encoderScaling.length);
				
				log.info("Friction (viscousX coulombX viscousW coulombW): " + Arrays.toString(friction));
				log.info("Efficiency (XY W): " + Arrays.toString(efficiency));
				log.info("Encoder (xNum xDenom yNum yDenom): " + Arrays.toString(encoderScaling));
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			}
		}
	}
}
