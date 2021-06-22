/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.vector.AVector;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Execute a list of {@link SkillCommand}
 * <p>
 * You can execute command sequences via a list or via bot center with a single string.
 * Commands are separated by <code>|</code>.
 * Each command consist of two or three parts, separated by a space: 1. time, 2. command type, 3. parameter of command
 * </p>
 * <p>
 * A command could like this: <code>0 vy 0.5|0 kd c|1.0 k 5|1.1 noop</code>.
 * A velocity of 0.5m/s in y (forward) direction will be set immediately. Kicker device is set to chip. After 1.0s,
 * kick speed is set to 5m/s. Sequence is finished after 1.1s.
 * </p>
 * <p>
 * After the sequence is finished, the robot will turn off the motors
 * </p>
 */
@Log4j2
public class CommandListSkill extends AMoveSkill
{
	@Configurable(defValue = "false", comment = "Log all commands to info log when executed")
	private static boolean logCommands = false;

	private final List<SkillCommand> commands = new ArrayList<>();

	private long tStart;
	private BotSkillLocalVelocity skill;


	public CommandListSkill(final List<SkillCommand> commands)
	{
		this.commands.addAll(commands);
		this.commands.sort(Comparator.comparingDouble(SkillCommand::getTime));
	}


	public void setCommandList(String commandList)
	{
		this.commands.addAll(parseCommandSequence(commandList));
		this.commands.sort(Comparator.comparingDouble(SkillCommand::getTime));
	}


	public boolean isFinished()
	{
		return commands.isEmpty();
	}


	private static List<SkillCommand> parseCommandSequence(final String commandList)
	{
		List<SkillCommand> parsedCommands = new ArrayList<>();
		String[] commandSequence = commandList.split("\\|");
		for (String command : commandSequence)
		{
			String[] split = command.split(" ");
			if (split.length < 2)
			{
				throw new IllegalArgumentException("Invalid command structure: " + command);
			}
			double t = Double.parseDouble(split[0]);
			SkillCommand skillCommand = new SkillCommand(t);
			String type = split[1];
			String value = split.length > 2 ? split[2] : "";
			addCommand(skillCommand, value, type);
			parsedCommands.add(skillCommand);
		}
		return parsedCommands;
	}


	@SuppressWarnings("squid:MethodCyclomaticComplexity") // accept this for the large switch case
	private static void addCommand(final SkillCommand skillCommand, final String value, final String type)
	{
		switch (type)
		{
			case "vx":
				skillCommand.setXyVel(Vector2.fromX(Double.parseDouble(value)));
				break;
			case "vy":
				skillCommand.setXyVel(Vector2.fromY(Double.parseDouble(value)));
				break;
			case "vxy":
				skillCommand.setXyVel(AVector2.valueOf(value));
				break;
			case "vw":
				skillCommand.setaVel(Double.parseDouble(value));
				break;
			case "vxyw":
				IVector vxyw = AVector.valueOf(value);
				skillCommand.setXyVel(vxyw.getXYVector());
				skillCommand.setaVel(vxyw.get(2));
				break;
			case "vxw":
				IVector2 vxw = AVector2.valueOf(value);
				if (vxw != null)
				{
					skillCommand.setXyVel(Vector2.fromX(vxw.x()));
					skillCommand.setaVel(vxw.y());
				}
				break;
			case "vyw":
				IVector2 vyw = AVector2.valueOf(value);
				if (vyw != null)
				{
					skillCommand.setXyVel(Vector2.fromY(vyw.x()));
					skillCommand.setaVel(vyw.y());
				}
				break;
			case "axy":
				skillCommand.setAccMaxXY(Double.parseDouble(value));
				break;
			case "aw":
				skillCommand.setAccMaxW(Double.parseDouble(value));
				break;
			case "k":
				skillCommand.setKickSpeed(Double.parseDouble(value));
				break;
			case "d":
				skillCommand.setDribbleSpeed(Integer.parseInt(value));
				break;
			case "kd":
				skillCommand.setKickerDevice(parseKickerDevice(value));
				break;
			case "noop":
				break;
			default:
				throw new IllegalArgumentException("Unknown command: " + type);
		}
	}


	private static EKickerDevice parseKickerDevice(final String value)
	{
		if (value.toLowerCase(Locale.GERMAN).startsWith("s"))
		{
			return EKickerDevice.STRAIGHT;
		} else if (value.toLowerCase(Locale.GERMAN).startsWith("c"))
		{
			return EKickerDevice.CHIP;
		}
		throw new IllegalArgumentException("Unknown kicker device: " + value);
	}


	@Override
	public void doEntryActions()
	{
		tStart = getWorldFrame().getTimestamp();
		skill = new BotSkillLocalVelocity(defaultMoveConstraints());
		setKickParams(null);
		getMatchCtrl().setSkill(skill);
	}


	@Override
	public void doUpdate()
	{
		double timeSinceLastVisible = (getWorldFrame().getTimestamp()
				- getWorldFrame().getBall().getLastVisibleTimestamp()) * 1e-9;
		if ((timeSinceLastVisible < 0.05) && (getTBot().getBotKickerPos().distanceTo(getBall().getPos()) > 100.0))
		{
			commands.clear();
		}

		double timePassed = (getWorldFrame().getTimestamp() - tStart) / 1e9;
		while (!commands.isEmpty()
				&& (commands.get(0).getTime() <= timePassed))
		{
			executeCommand(commands.remove(0));
		}
		if (commands.isEmpty())
		{
			setMotorsOff();
		}
	}


	private void executeCommand(final SkillCommand command)
	{
		logCmd(command);
		if (command.getXyVel() != null)
		{
			skill.setVelXy(command.getXyVel());
		}
		if (command.getaVel() != null)
		{
			skill.setVelW(command.getaVel());
		}
		if (command.getDribbleSpeed() != null)
		{
			skill.getKickerDribbler().setDribblerSpeed(command.getDribbleSpeed());
		}
		if (command.getKickerDevice() != null)
		{
			skill.getKickerDribbler().setDevice(command.getKickerDevice());
		}
		if (command.getKickSpeed() != null)
		{
			if (command.getKickSpeed() > 0)
			{
				skill.getKickerDribbler().setMode(EKickerMode.ARM);
				skill.getKickerDribbler().setKickSpeed(command.getKickSpeed());
			} else
			{
				skill.getKickerDribbler().setMode(EKickerMode.DISARM);
			}
		}
		if (command.getAccMaxXY() != null)
		{
			skill.setAccMax(command.getAccMaxXY());
		}
		if (command.getAccMaxW() != null)
		{
			skill.setAccMaxW(command.getAccMaxW());
		}
	}


	private void logCmd(final SkillCommand skillCommand)
	{
		if (logCommands)
		{
			log.info(skillCommand);
		}
	}
}
