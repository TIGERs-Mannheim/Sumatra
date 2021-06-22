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
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
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
 * Delay identification and sampling skill.
 */
@Log4j2
@RequiredArgsConstructor
public class IdentDelaysSkill extends AMoveSkill
{
	private final double amplitude;
	private final double frequency;
	private final double runtime;

	private long tStart;
	private MoveConstraints moveConstraints;
	private BotWatcher watch;


	@Override
	public void doEntryActions()
	{
		watch = new BotWatcher(getBotId(), EDataAcquisitionMode.DELAYS, "ident-delays");

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
			Object[] values = mp.returningFeval("delays", 1, watch.getAbsoluteFileName());
			double[] params = (double[]) values[0];

			log.info("Delay Identification complete. Recommended parameters:");
			log.info("visCaptureDelay: {}", (int) params[0]);
			log.info("visProcessingDelayMax: {}", (int) params[1]);
			log.info("gyrDelay: {}", (int) params[2]);
			log.info("Dataloss: {}%", () -> String.format(Locale.ENGLISH, "%.2f", params[3] * 100));
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
			changeState(IDLE_STATE);
			return;
		}

		double speed = AngleMath.PI_TWO * amplitude * frequency * SumatraMath.sin(AngleMath.PI_TWO * frequency * t);

		BotSkillLocalVelocity skill = new BotSkillLocalVelocity(Vector2f.ZERO_VECTOR, speed, moveConstraints);
		getMatchCtrl().setSkill(skill);
	}
}
