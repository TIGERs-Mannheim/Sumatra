/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.move.path;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.testplays.commands.ACommand;
import edu.tigers.sumatra.testplays.commands.KickCommand;
import edu.tigers.sumatra.testplays.commands.PassCommand;
import edu.tigers.sumatra.testplays.commands.PathCommand;
import edu.tigers.sumatra.testplays.commands.ReceiveCommand;
import edu.tigers.sumatra.testplays.commands.RedirectCommand;
import edu.tigers.sumatra.testplays.commands.SynchronizeCommand;
import edu.tigers.sumatra.testplays.util.Point;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class MoveAlongPathRole extends ARole
{
	
	private List<ACommand>	commands			= null;
	
	private List<ACommand>	commandQueue	= null;
	private ACommand			nextCommand		= null;
	
	private int					commandId;
	private boolean			repeat			= false;
	
	private long lastCommandCounter = 0;
	
	/**
	 * The state events
	 */
	public enum EEvent implements IEvent
	{
		NEXT,
		MOVE,
		SYNCHRONIZE,
		KICK,
		PASS,
		RECEIVE,
		REDIRECT,
		DONE,
		RESTART
	}
	
	
	/**
	 * Creates a new role with the given command queue
	 * 
	 * @param commands
	 * @param id
	 */
	public MoveAlongPathRole(List<ACommand> commands, int id)
	{
		
		this(commands, id, false);
	}
	
	
	/**
	 * Creates a new role with the given command queue; if repeat is set to true, the queue will reset when done.
	 * 
	 * @param commands
	 * @param id
	 * @param repeat
	 */
	public MoveAlongPathRole(List<ACommand> commands, int id, boolean repeat)
	{
		super(ERole.MOVE_ALONG_PATH);
		
		this.repeat = repeat;
		this.commandId = id;
		this.commands = commands;
		this.commandQueue = new ArrayList<>(commands);
		
		IState initialState = new InitialState();
		IState moveState = new MoveState();
		IState syncState = new SynchronizeState();
		IState idleState = new IdleState();
		IState kickState = new KickState();
		IState passState = new PassState();
		IState receiveState = new ReceiveState();
		IState redirectState = new RedirectState();
		
		setInitialState(initialState);
		
		addTransition(initialState, EEvent.MOVE, moveState);
		addTransition(initialState, EEvent.SYNCHRONIZE, syncState);
		addTransition(initialState, EEvent.KICK, kickState);
		addTransition(initialState, EEvent.PASS, passState);
		addTransition(initialState, EEvent.RECEIVE, receiveState);
		addTransition(initialState, EEvent.REDIRECT, redirectState);
		
		addTransition(EEvent.NEXT, initialState);
		addTransition(EEvent.RESTART, initialState);
		
		if (repeat)
		{
			addTransition(EEvent.DONE, initialState);
		} else
		{
			addTransition(EEvent.DONE, idleState);
		}
	}
	
	
	public int getCommandId()
	{
		
		return commandId;
	}
	
	private class InitialState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			if (commandQueue.isEmpty())
			{
				
				if (repeat)
				{
					commandQueue = new ArrayList<>(commands);
				} else
				{
					
					triggerEvent(EEvent.DONE);
					return;
				}
			}
			
			nextCommand = commandQueue.get(0);
			commandQueue.remove(0);
			switch (nextCommand.getCommandType())
			{
				case PATH:
					triggerEvent(EEvent.MOVE);
					break;
				case SYNCHRONIZE:
					triggerEvent(EEvent.SYNCHRONIZE);
					break;
				case KICK:
					triggerEvent(EEvent.KICK);
					break;
				case PASS:
					triggerEvent(EEvent.PASS);
					break;
				case RECEIVE:
					triggerEvent(EEvent.RECEIVE);
					break;
				case REDIRECT:
					triggerEvent(EEvent.REDIRECT);
					break;
				default:
					break;
			}
			
		}
	}
	
	private class MoveState implements IState
	{
		
		private List<Point>	path	= null;
		private AMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			PathCommand command = (PathCommand) nextCommand;
			
			path = new ArrayList<>(command.getPoints());
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (!isDestinationReached())
			{
				return;
			}
			
			if (path.isEmpty())
			{
				triggerEvent(EEvent.NEXT);
				return;
			}
			
			Point point = path.get(0);
			IVector2 destination = point.createVector2();
			
			skill.getMoveCon().updateDestination(destination);
			skill.getMoveCon().setFastPosMode(point.isFastPos());
			skill.getMoveCon().setPenaltyAreaAllowedOur(point.isAllowPenaltyAreaOur());
			skill.getMoveCon().setPenaltyAreaAllowedTheir(point.isAllowPenaltyAreaTheir());
			
			path.remove(0);
			
		}
		
		
		final boolean isDestinationReached()
		{
			return skill.getMoveCon().getDestination() == null
					|| VectorMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 70;
		}
	}
	
	private class SynchronizeState implements IState
	{
		
		private SynchronizeCommand command;
		
		
		@Override
		public synchronized void doEntryActions()
		{
			
			command = (SynchronizeCommand) nextCommand;
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			
			skill.getMoveCon().updateDestination(getBot().getPos());
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			// The number of bots required to switch to initState
			final int groupMembersCount = 2;
			
			int groupMembersFound = 0;
			
			for (ARole role : getAiFrame().getPlayStrategy().getActiveRoles(ERole.MOVE_ALONG_PATH))
			{
				
				if (groupMembersCount == groupMembersFound)
				{
					// Exit loop if all group members are in sync state
					break;
				}
				
				if ("SynchronizeState".equals(role.getCurrentState().getIdentifier()))
				{
					SynchronizeState syncState = (SynchronizeState) role.getCurrentState();
					if (syncState.command.getSyncGroup() == command.getSyncGroup())
					{
						groupMembersFound++;
					}
				}
			}
			
			if (groupMembersCount != groupMembersFound)
			{
				// Exit loop if all group members are in sync state
				return;
			}
			
			triggerEvent(EEvent.NEXT);
		}
	}
	
	private class KickState implements IState
	{
		
		private AKickSkill	skill	= null;
		private KickCommand	command;
		
		
		@Override
		public void doEntryActions()
		{
			
			command = (KickCommand) nextCommand;
			skill = new KickNormalSkill(new DynamicPosition(command.getDestination().createVector2()));
			
			if (command.getKickSpeed() > 0)
			{
				skill.setKickMode(AKickSkill.EKickMode.FIXED_SPEED);
				skill.setKickSpeed(command.getKickSpeed());
			} else
			{
				skill.setKickMode(AKickSkill.EKickMode.PASS);
			}
			
			skill.setDevice(command.getKickerDevice());
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (isKickCompleted())
			{
				triggerEvent(EEvent.NEXT);
			}
		}
		
		
		final boolean isKickCompleted()
		{
			
			IVector2 ballVelVec = getWFrame().getBall().getVel().absNew();
			double ballVel = ballVelVec.x() + ballVelVec.y();
			
			if (getPos().distanceTo(getWFrame().getBall().getPos()) >= Geometry.getBallRadius() + 50 + 100 * ballVel)
			{
				return false;
			}
			
			IVector2 destinationPoint = command.getDestination().createVector2();
			if (getWFrame().getBall().getTrajectory().getTravelLine().nearestPointOnLine(destinationPoint)
					.distanceTo(destinationPoint) >= 100)
			{
				return false;
			}
			
			IVector2 ballVelPrevVec = getAiFrame().getPrevFrame().getWorldFrame().getBall().getVel().absNew();
			double ballVelPrev = ballVelPrevVec.x() + ballVelPrevVec.y();
			
			// noinspection SimplifiableIfStatement
			if (ballVelPrev <= ballVel)
			{
				return false;
			}
			
			return ballVel > Math.max(skill.getKickSpeed() * 0.75, 0.5);
			
		}
	}
	
	private class PassState implements IState
	{
		
		private PassCommand	command;
		private AKickSkill	kickSkill;
		ITrackedBot				foreignBot;
		
		
		@Override
		public void doEntryActions()
		{
			kickSkill = null;
			foreignBot = null;
			command = (PassCommand) nextCommand;
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (kickSkill == null)
			{
				foreignBot = findReceivingBot(command.getPassGroup());
				
				if (foreignBot == null)
				{
					return;
				}
				
				kickSkill = new KickNormalSkill(new DynamicPosition(foreignBot));
				kickSkill.setKickMode(AKickSkill.EKickMode.PASS);
				kickSkill.setDevice(command.getKickerDevice());
				setNewSkill(kickSkill);
			}
			
			if (foreignBot.getPos().distanceTo(getWFrame().getBall().getPos()) <= Geometry.getBallRadius() + 200)
			{
				triggerEvent(EEvent.NEXT);
			}
			
		}
		
		
		private ITrackedBot findReceivingBot(int group)
		{
			for (ARole role : getAiFrame().getPlayStrategy().getActiveRoles(ERole.MOVE_ALONG_PATH))
			{
				final boolean roleIsReceiver = "ReceiveState".equals(role.getCurrentState().getIdentifier());
				final boolean roleIsRedirector = "RedirectState".equals(role.getCurrentState().getIdentifier());
				
				if (roleIsReceiver)
				{
					ReceiveState receiveState = (ReceiveState) role.getCurrentState();
					if (receiveState.command.getPassGroup() == group)
					{
						return role.getBot();
					}
				} else if (roleIsRedirector)
				{
					RedirectState redirectState = (RedirectState) role.getCurrentState();
					if (redirectState.command.getRedirectGroup() == group)
					{
						return role.getBot();
					}
				}
			}
			
			return null;
		}
	}
	
	
	private class ReceiveState implements IState
	{
		
		private ReceiveCommand	command;
		private ReceiverSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = null;
			command = (ReceiveCommand) nextCommand;
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (skill == null)
			{
				boolean roleFound = false;
				
				
				for (ARole role : getAiFrame().getPlayStrategy().getActiveRoles(ERole.MOVE_ALONG_PATH))
				{
					
					if (!"PassState".equals(role.getCurrentState().getIdentifier()))
					{
						continue;
					}
					
					PassState passState = (PassState) role.getCurrentState();
					if (passState.command.getPassGroup() == command.getPassGroup())
					{
						roleFound = true;
					}
					
				}
				
				if (!roleFound)
				{
					return;
				}
				
				skill = new ReceiverSkill();
				setNewSkill(skill);
			}
			
			if (getPos().distanceTo(getWFrame().getBall().getPos()) <= Geometry.getBallRadius() + 100)
			{
				triggerEvent(EEvent.NEXT);
			}
			
		}
	}
	
	private class RedirectState implements IState
	{
		private RedirectSkill skill = null;
		private RedirectCommand command;
		
		
		@Override
		public void doEntryActions()
		{
			
			command = (RedirectCommand) nextCommand;
			skill = new RedirectSkill(new DynamicPosition(command.getDestination().createVector2()));
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			
			
			if (isKickCompleted())
			{
				triggerEvent(EEvent.NEXT);
			}
		}
		
		
		final boolean isKickCompleted()
		{
			
			IVector2 ballVelVec = getWFrame().getBall().getVel().absNew();
			double ballVel = ballVelVec.x() + ballVelVec.y();
			
			if (getPos().distanceTo(getWFrame().getBall().getPos()) >= Geometry.getBallRadius() + 50 + 100 * ballVel)
			{
				return false;
			}
			
			IVector2 destinationPoint = command.getDestination().createVector2();
			if (getWFrame().getBall().getTrajectory().getTravelLine().nearestPointOnLine(destinationPoint)
					.distanceTo(destinationPoint) >= 100)
			{
				return false;
			}
			
			IVector2 ballVelPrevVec = getAiFrame().getPrevFrame().getWorldFrame().getBall().getVel().absNew();
			double ballVelPrev = ballVelPrevVec.x() + ballVelPrevVec.y();
			
			// noinspection SimplifiableIfStatement
			if (ballVelPrev <= ballVel)
			{
				return false;
			}
			
			return ballVel > Math.max(8 * 0.75, 0.5);
			
		}
	}
	
	private class IdleState implements IState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new IdleSkill());
		}
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		if (getBot() == null && getCurrentState().getClass() != IdleState.class)
		{
			Logger.getAnonymousLogger().info("Got role for bot which is not in play. You should try to assign other bots");
			
			triggerEvent(EEvent.DONE);
			
			return;
		}
		
		super.beforeUpdate();
		
		final Referee.SSL_Referee.Command restartCommand = Referee.SSL_Referee.Command.FORCE_START;
		
		final RefereeMsg currentMsg = getAiFrame().getRefereeMsg();
		
		final boolean replayCommandSent = currentMsg.getCommand()
				.equals(restartCommand);
		
		final boolean isNewCommand = currentMsg.getCommandCounter() != lastCommandCounter;
		
		if (replayCommandSent && isNewCommand)
		{
			lastCommandCounter = currentMsg.getCommandCounter();
			
			commandQueue = new ArrayList<>(commands);
			triggerEvent(EEvent.RESTART);
		}
	}
}