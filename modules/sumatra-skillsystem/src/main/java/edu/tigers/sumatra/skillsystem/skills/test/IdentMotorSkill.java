/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import java.util.Locale;


/**
 * Motor identification and sampling skill.
 */
@Log4j2
@RequiredArgsConstructor
public class IdentMotorSkill extends AMoveSkill
{
	private final double maxSpeed;
	private final int numSteps;


	private long tStart;
	private int curStep = 0;
	private int dir = 1;
	private int phase = 0; // 0 = 0 to max, 1 = max to -max, 2 = -max to 0
	private MoveConstraints moveConstraints;
	private BotWatcher watch;


	@Override
	public void doEntryActions()
	{
		watch = new BotWatcher(getBotId(), EDataAcquisitionMode.MOTOR_MODEL, "ident-motor");

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

			log.info("Motor Identification complete. Recommended parameters:");
			log.info(() -> String.format(Locale.ENGLISH, "K: %.4f, T: %.4f", params[8], params[9]));
			log.info("Dataloss: {}%", () -> String.format(Locale.ENGLISH, "%.2f", params[10] * 100));
			for (int i = 0; i < 4; i++)
			{
				int paramM = i + 1;
				double paramK = params[i];
				double paramT = params[i + 4];
				log.info(() -> String.format(Locale.ENGLISH, "M%d => K: %.4f, T: %.4f%n", paramM, paramK, paramT));
			}
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
			changeState(IDLE_STATE);
			return;
		}

		double speed = (((double) curStep) / numSteps) * maxSpeed;

		BotSkillLocalVelocity skill = new BotSkillLocalVelocity(Vector2f.ZERO_VECTOR, speed, moveConstraints);
		getMatchCtrl().setSkill(skill);
	}
}
