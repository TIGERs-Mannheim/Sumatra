/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.move.path;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
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
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class MoveAlongPathRole extends ARole
{

	private List<ACommand> commands;

	private List<ACommand> commandQueue;
	private ACommand nextCommand = null;

	private int commandId;
	private boolean repeat;

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


	private class InitialState extends AState
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

	private class MoveState extends AState
	{

		private List<Point> path = null;
		private MoveToSkill skill = null;


		@Override
		public void doEntryActions()
		{
			PathCommand command = (PathCommand) nextCommand;

			path = new ArrayList<>(command.getPoints());
			skill = MoveToSkill.createMoveToSkill();
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

			skill.updateDestination(destination);
			skill.getMoveCon().setPenaltyAreaOurObstacle(!point.isAllowPenaltyAreaOur());
			skill.getMoveCon().setPenaltyAreaTheirObstacle(!point.isAllowPenaltyAreaTheir());

			path.remove(0);

		}


		final boolean isDestinationReached()
		{
			return skill.getDestination() == null
					|| VectorMath.distancePP(getPos(), skill.getDestination()) < 70;
		}
	}

	private class SynchronizeState extends AState
	{

		private SynchronizeCommand command;


		@Override
		public void doEntryActions()
		{

			command = (SynchronizeCommand) nextCommand;
			MoveToSkill skill = MoveToSkill.createMoveToSkill();

			skill.updateDestination(getBot().getPos());
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);

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

	private class KickState extends AState
	{
		private KickCommand command;


		@Override
		public void doEntryActions()
		{

			command = (KickCommand) nextCommand;

			KickParams kickParams;
			if (command.getKickSpeed() > 0)
			{
				kickParams = KickParams.of(command.getKickerDevice(), command.getKickSpeed());
			} else
			{
				double distance = command.getDestination().createVector2().distanceTo(getBall().getPos());
				double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, 3.0);
				kickParams = KickParams.of(command.getKickerDevice(), kickSpeed);
			}

			var skill = new TouchKickSkill(command.getDestination().createVector2(), kickParams);

			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
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
			if (getWFrame().getBall().getTrajectory().getTravelLine().closestPointOnLine(destinationPoint)
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

			return ballVel > 0.5;

		}
	}

	private class PassState extends RoleState<TouchKickSkill>
	{
		private PassCommand command;
		ITrackedBot foreignBot;


		PassState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onInit()
		{
			command = (PassCommand) nextCommand;

			foreignBot = findReceivingBot(command.getPassGroup());

			if (foreignBot == null)
			{
				return;
			}

			double distance = foreignBot.getPos().distanceTo(getBall().getPos());
			double kickSpeed = getBall().getStraightConsultant().getInitVelForDist(distance, 3.0);
			skill.setDesiredKickParams(KickParams.of(command.getKickerDevice(), kickSpeed));
		}


		@Override
		protected void onUpdate()
		{
			if (foreignBot == null)
			{
				return;
			}

			skill.setTarget(foreignBot.getBotKickerPos());

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


	private class ReceiveState extends AState
	{
		private ReceiveCommand command;
		ReceiveBallSkill skill;


		@Override
		public void doEntryActions()
		{
			command = (ReceiveCommand) nextCommand;

			boolean roleFound = false;

			for (ARole role : getAiFrame().getPlayStrategy().getActiveRoles(ERole.MOVE_ALONG_PATH))
			{
				if(!role.getCurrentState().getClass().equals(PassState.class))
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

			skill = new ReceiveBallSkill();
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setBallObstacle(false);
			if (getPos().distanceTo(getWFrame().getBall().getPos()) <= Geometry.getBallRadius() + 100)
			{
				triggerEvent(EEvent.NEXT);
			}
		}
	}

	private class RedirectState extends AState
	{
		private RedirectCommand command;


		@Override
		public void doEntryActions()
		{
			command = (RedirectCommand) nextCommand;
			var skill = new RedirectBallSkill();
			skill.setTarget(command.getDestination().createVector2());
			skill.setDesiredKickParams(KickParams.maxStraight());

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
			if (getWFrame().getBall().getTrajectory().getTravelLine().closestPointOnLine(destinationPoint)
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

	private class IdleState extends AState
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

		final SslGcRefereeMessage.Referee.Command restartCommand = SslGcRefereeMessage.Referee.Command.FORCE_START;

		final RefereeMsg currentMsg = getAiFrame().getRefereeMsg();

		final boolean replayCommandSent = currentMsg.getCommand()
				.equals(restartCommand);

		final boolean isNewCommand = currentMsg.getCmdCounter() != lastCommandCounter;

		if (replayCommandSent && isNewCommand)
		{
			lastCommandCounter = currentMsg.getCmdCounter();

			commandQueue = new ArrayList<>(commands);
			triggerEvent(EEvent.RESTART);
		}
	}
}
