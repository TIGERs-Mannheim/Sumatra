/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribblerTemperature;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Bot command interpreter for RCM.
 */
public class CommandInterpreter implements ICommandInterpreter
{
	private static final Logger log = LogManager.getLogger(CommandInterpreter.class.getName());

	@Configurable(defValue = "FULL")
	private static EMoveControllerType moveControllerType = EMoveControllerType.FULL;

	@Configurable(comment = "Maximum kickSpeed [m/s]", defValue = "6.5")
	private static double maxKickSpeed = 6.0;
	@Configurable(comment = "Maximum kickSpeed [m/s]", defValue = "5.0")
	private static double maxChipSpeed = 5.0;

	static
	{
		ConfigRegistration.registerClass("rcm", CommandInterpreter.class);
	}

	private final ControllerState controllerState;
	private final KickerDribblerCommands kdOut = new KickerDribblerCommands();

	private long lastForceKick = 0;
	private long lastArmKick = 0;
	private boolean paused = false;

	private IMoveController moveController = new FullMoveController();


	private enum EMoveControllerType
	{
		FULL(FullMoveController.class),
		SIMPLIFIED(SimplifiedMoveController.class),

		;

		private Class<?> clazz;

		EMoveControllerType(final Class<?> clazz)
		{
			this.clazz = clazz;
		}
	}

	/**
	 * @param bot to be controlled
	 */
	public CommandInterpreter(final ABot bot)
	{
		controllerState = new ControllerState(bot);
	}


	@Override
	public void interpret(final BotActionCommand command)
	{
		if (paused)
		{
			return;
		}

		updateMoveController();

		final AMoveBotSkill skill = moveController.control(command, controllerState);

		boolean isDribblerOverheated = controllerState.getBot().getDribblerTemperature() == EDribblerTemperature.OVERHEATED;

		double dribbleForce = controllerState.getBot().getBotParams().getDribblerSpecs().getDefaultForce();
		double dribbleSpeed = controllerState.getBot().getBotParams().getDribblerSpecs().getDefaultSpeed();

		if (command.hasDribble() && (command.getDribble() > 0.25) && !isDribblerOverheated)
		{
			final int speed = (int) (command.getDribble() * dribbleSpeed);
			kdOut.setDribbler(speed, dribbleForce);
		} else
		{
			kdOut.setDribbler(0, dribbleForce);
		}

		interpretKick(command);

		skill.setKickerDribbler(kdOut);
		controllerState.getBot().getMatchCtrl().setSkill(skill);
		controllerState.getBot().sendMatchCommand();
	}


	private void updateMoveController()
	{
		if (!moveControllerType.clazz.equals(moveController.getClass()))
		{
			switch (moveControllerType)
			{
				case FULL:
					moveController = new FullMoveController();
					break;
				case SIMPLIFIED:
					moveController = new SimplifiedMoveController();
					break;
				default:
					throw new IllegalStateException("Unknown move controller type: " + moveControllerType);
			}
		}
	}


	private void interpretKick(final BotActionCommand command)
	{
		if (command.hasKickForce())
		{
			lastForceKick = System.nanoTime();
			kdOut.setKick(command.getKickForce() * maxKickSpeed, EKickerDevice.STRAIGHT, EKickerMode.FORCE);
		}
		if (command.hasChipForce())
		{
			lastForceKick = System.nanoTime();
			kdOut.setKick(command.getChipForce() * maxKickSpeed, EKickerDevice.CHIP, EKickerMode.FORCE);
		}
		if (command.hasKickArm())
		{
			if (controllerState.getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
			{
				lastForceKick = 0;
				lastArmKick = System.nanoTime();
			} else
			{
				lastForceKick = System.nanoTime();
			}
			kdOut.setKick(command.getKickArm() * maxKickSpeed, EKickerDevice.STRAIGHT, EKickerMode.ARM);
		}
		if (command.hasChipArm())
		{
			lastForceKick = 0;
			lastArmKick = System.nanoTime();
			kdOut.setKick(command.getChipArm() * maxChipSpeed, EKickerDevice.CHIP, EKickerMode.ARM);
		}

		if ((command.hasDisarm() && command.getDisarm())
				|| isArmKickTimedOut()
				|| isForceKickTimedOut())
		{
			kdOut.setKick(0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
		}
	}


	private boolean isForceKickTimedOut()
	{
		return (lastForceKick != 0) && ((System.nanoTime() - lastForceKick) > 5e7);
	}


	private boolean isArmKickTimedOut()
	{
		return (lastArmKick != 0) && ((System.nanoTime() - lastArmKick) > 15e9);
	}


	@Override
	public void stopAll()
	{
		controllerState.getBot().getMatchCtrl().setSkill(new BotSkillMotorsOff());
	}


	@Override
	public ABot getBot()
	{
		return controllerState.getBot();
	}


	/**
	 * @param compassThreshold the compassThreshold to set
	 */
	public void setCompassThreshold(final double compassThreshold)
	{
		controllerState.setCompassThreshold(compassThreshold);
	}


	@Override
	public boolean isHighSpeedMode()
	{
		return controllerState.isHighSpeedMode();
	}


	@Override
	public void setHighSpeedMode(final boolean highSpeedMode)
	{
		controllerState.setHighSpeedMode(highSpeedMode);
		if (highSpeedMode)
		{
			log.info("High Speed Mode activated");
		} else
		{
			log.info("High Speed Mode deactivated");
		}
	}


	@Override
	public boolean isPaused()
	{
		return paused;
	}


	@Override
	public void setPaused(final boolean paused)
	{
		this.paused = paused;
	}


	@Override
	public double getCompassThreshold()
	{
		return controllerState.getCompassThreshold();
	}
}
