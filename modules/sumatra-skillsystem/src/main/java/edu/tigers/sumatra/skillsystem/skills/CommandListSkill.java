/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;


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
public class CommandListSkill extends AMoveSkill
{
	private static final Logger log = Logger.getLogger(CommandListSkill.class.getName());
	private final List<SkillCommand> commands = new ArrayList<>();
	private boolean finished = false;
	
	@Configurable(defValue = "false", comment = "Log all commands to info log when executed")
	private static boolean logCommands = false;
	
	
	private CommandListSkill()
	{
		super(ESkill.COMMAND_LIST);
		
		setInitialState(new ExecutionState());
		addTransition(EEvent.DONE, new DoneState());
	}
	
	
	@SuppressWarnings("unused") // used by UI
	public CommandListSkill(String commandList)
	{
		this();
		parseCommandSequence(commandList);
		this.commands.sort(Comparator.comparingDouble(SkillCommand::getTime));
	}
	
	
	public CommandListSkill(List<SkillCommand> commands)
	{
		this();
		this.commands.addAll(commands);
		this.commands.sort(Comparator.comparingDouble(SkillCommand::getTime));
		
		setInitialState(new ExecutionState());
		addTransition(EEvent.DONE, new DoneState());
	}
	
	
	public boolean isFinished()
	{
		return finished;
	}
	
	
	private void parseCommandSequence(final String commandList)
	{
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
			commands.add(skillCommand);
		}
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity") // accept this for the large switch case
	private void addCommand(final SkillCommand skillCommand, final String value, final String type)
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
				IVector vxyw = AVector3.valueOf(value);
				skillCommand.setXyVel(vxyw.getXYVector());
				skillCommand.setaVel(vxyw.get(2));
				break;
			case "vxw":
				IVector2 vxw = AVector2.valueOf(value);
				skillCommand.setXyVel(Vector2.fromX(vxw.x()));
				skillCommand.setaVel(vxw.y());
				break;
			case "vyw":
				IVector2 vyw = AVector2.valueOf(value);
				skillCommand.setXyVel(Vector2.fromY(vyw.x()));
				skillCommand.setaVel(vyw.y());
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
	
	
	private EKickerDevice parseKickerDevice(final String value)
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
	
	
	private enum EEvent implements IEvent
	{
		DONE
	}
	
	private class ExecutionState extends AState
	{
		private long tStart;
		private BotSkillLocalVelocity skill;
		
		
		@Override
		public void doEntryActions()
		{
			tStart = getWorldFrame().getTimestamp();
			skill = new BotSkillLocalVelocity(getMoveCon().getMoveConstraints());
			getMatchCtrl().setSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			double timePassed = (getWorldFrame().getTimestamp() - tStart) / 1e9;
			while (!commands.isEmpty()
					&& commands.get(0).getTime() <= timePassed)
			{
				executeCommand(commands.remove(0));
				
			}
			if (commands.isEmpty())
			{
				triggerEvent(EEvent.DONE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			finished = true;
			setMotorsOff();
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
		}
		
		
		private void logCmd(SkillCommand skillCommand)
		{
			if (logCommands)
			{
				log.info(skillCommand);
			}
		}
	}
	
	private class DoneState extends AState
	{
	}
}
