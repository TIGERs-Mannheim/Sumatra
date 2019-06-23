/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import java.util.Locale;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


/**
 * Motor identification and sampling skill.
 * 
 * @author AndreR
 */
public class IdentMotorSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(IdentMotorSkill.class.getName());
	
	private double maxSpeed = 4 * AngleMath.PI_TWO;
	private int numSteps = 10;
	
	
	/**
	 * Default
	 */
	public IdentMotorSkill()
	{
		super(ESkill.IDENT_MOTOR);
		
		setInitialState(new RotateState());
		addTransition(EEvent.DONE, IDLE_STATE);
	}
	
	
	/**
	 * @param maxSpeed
	 * @param numSteps
	 */
	public IdentMotorSkill(final double maxSpeed, final int numSteps)
	{
		this();
		
		this.maxSpeed = maxSpeed;
		this.numSteps = numSteps;
	}
	
	
	private enum EEvent implements IEvent
	{
		DONE
	}
	
	private class RotateState extends AState
	{
		private long tStart;
		private int curStep = 0;
		private int dir = 1;
		private int phase = 0; // 0 = 0 to max, 1 = max to -max, 2 = -max to 0
		private MoveConstraints moveConstraints;
		private BotWatcher watch;
		
		
		@Override
		public void doEntryActions()
		{
			watch = new BotWatcher(getBot(), EDataAcquisitionMode.MOTOR_MODEL);
			
			tStart = getWorldFrame().getTimestamp();
			
			moveConstraints = new MoveConstraints(getBot().getBotParams().getMovementLimits());
			
			moveConstraints.setAccMaxW(DriveLimits.MAX_ACC_W);
			moveConstraints.setJerkMaxW(DriveLimits.MAX_JERK_W);
			
			watch.start();
		}
		
		
		@Override
		public void doExitActions()
		{
			watch.stop();
			
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval("addpath('identification')");
				Object[] values = mp.returningFeval("motor", 1, watch.getAbsoluteFileName());
				double[] params = (double[]) values[0];
				
				StringBuilder sb = new StringBuilder();
				
				sb.append("Motor Identification complete. Recommended parameters:\n");
				sb.append(String.format(Locale.ENGLISH, "K: %.4f, T: %.4f%nDataloss: %.2f%%%n", params[8], params[9],
						params[10] * 100));
				for (int i = 0; i < 4; i++)
				{
					sb.append(String.format(Locale.ENGLISH, "M%d => K: %.4f, T: %.4f%n", i + 1, params[i], params[i + 4]));
				}
				
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
			if (t > 2.0)
			{
				tStart = getWorldFrame().getTimestamp();
				
				if (Math.abs(curStep) == numSteps)
				{
					dir *= -1;
					phase++;
				}
				
				curStep += dir;
			}
			
			if ((phase == 2) && (curStep == 1))
			{
				getMatchCtrl().setSkill(new BotSkillMotorsOff());
				triggerEvent(EEvent.DONE);
				return;
			}
			
			double speed = (((double) curStep) / numSteps) * maxSpeed;
			
			BotSkillLocalVelocity skill = new BotSkillLocalVelocity(Vector2f.ZERO_VECTOR, speed, moveConstraints);
			getMatchCtrl().setSkill(skill);
		}
		
		
	}
}
