/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.Locale;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


/**
 * Delay identification and sampling skill.
 * 
 * @author AndreR
 */
public class IdentDelaysSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(IdentDelaysSkill.class.getName());
	
	private double						amplitude	= 0.5;
	private double						frequency	= 2;
	private double						runtime		= 10;
	
	
	/**
	 * Default
	 **/
	public IdentDelaysSkill()
	{
		super(ESkill.IDENT_DELAYS);
		
		setInitialState(new RotateState());
		addTransition(EEvent.DONE, IDLE_STATE);
	}
	
	
	/**
	 * @param amplitude
	 * @param frequency
	 * @param runtime
	 */
	public IdentDelaysSkill(final double amplitude, final double frequency, final double runtime)
	{
		this();
		
		this.amplitude = amplitude;
		this.frequency = frequency;
		this.runtime = runtime;
	}
	
	
	private enum EEvent implements IEvent
	{
		DONE
	}
	
	private class RotateState implements IState
	{
		private long				tStart;
		private MoveConstraints	moveConstraints;
		private BotWatcher		watch;
		
		
		@Override
		public void doEntryActions()
		{
			watch = new BotWatcher(getBot(), EDataAcquisitionMode.DELAYS);
			getMatchCtrl().setDataAcquisitionMode(EDataAcquisitionMode.DELAYS);
			
			tStart = getWorldFrame().getTimestamp();
			
			moveConstraints = new MoveConstraints(getBot().getBotParams().getMovementLimits());
			
			moveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
			moveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);
			
			watch.start();
		}
		
		
		@Override
		public void doExitActions()
		{
			getMatchCtrl().setDataAcquisitionMode(EDataAcquisitionMode.NONE);
			watch.stop();
			
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval("addpath('identification')");
				Object[] values = mp.returningFeval("delays", 1, watch.getAbsoluteFileName());
				double[] params = (double[]) values[0];
				
				StringBuilder sb = new StringBuilder();
				
				sb.append("Delay Identification complete. Recommended parameters:\n");
				
				sb.append(String.format(Locale.ENGLISH, "visCaptureDelay: %d%n", (int) params[0]));
				sb.append(String.format(Locale.ENGLISH, "visProcessingDelayMax: %d%n", (int) params[1]));
				sb.append(String.format(Locale.ENGLISH, "gyrDelay: %d%n", (int) params[2]));
				sb.append(String.format(Locale.ENGLISH, "Dataloss: %.2f%%%n", params[3] * 100));
				
				log.info(sb.toString());
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			} catch (Exception err)
			{
				log.error("An error occurred.", err);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			double t = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			
			if (t > runtime)
			{
				getMatchCtrl().setSkill(new BotSkillMotorsOff());
				triggerEvent(EEvent.DONE);
				return;
			}
			
			double speed = AngleMath.PI_TWO * amplitude * frequency * Math.sin(AngleMath.PI_TWO * frequency * t);
			
			BotSkillLocalVelocity skill = new BotSkillLocalVelocity(AVector2.ZERO_VECTOR, speed, moveConstraints);
			getMatchCtrl().setSkill(skill);
		}
		
		
	}
}
