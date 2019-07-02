package edu.tigers.sumatra.referee.control;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This factory helps in creating new game controller events that can be send to the game-controller.
 */
public final class GcEventFactory
{
	private GcEventFactory()
	{
	}
	
	
	public static Event command(Referee.SSL_Referee.Command command)
	{
		return new Event(map(command));
	}
	
	
	public static Event commandDirect(ETeamColor teamColor)
	{
		return new Event(new EventCommand(
				EventCommand.Command.DIRECT,
				EventTeam.fromTeamColor(teamColor)));
	}
	
	
	public static Event commandIndirect(ETeamColor teamColor)
	{
		return new Event(new EventCommand(
				EventCommand.Command.INDIRECT,
				EventTeam.fromTeamColor(teamColor)));
	}
	
	
	public static Event commandKickoff(ETeamColor teamColor)
	{
		return new Event(new EventCommand(
				EventCommand.Command.KICKOFF,
				EventTeam.fromTeamColor(teamColor)));
	}
	
	
	public static Event commandPenalty(ETeamColor teamColor)
	{
		return new Event(new EventCommand(
				EventCommand.Command.PENALTY,
				EventTeam.fromTeamColor(teamColor)));
	}
	
	
	public static Event commandTimeout(ETeamColor teamColor)
	{
		return new Event(new EventCommand(
				EventCommand.Command.TIMEOUT,
				EventTeam.fromTeamColor(teamColor)));
	}
	
	
	public static Event goals(ETeamColor teamColor, int goals)
	{
		return new Event(EventModifyValue.goals(EventTeam.fromTeamColor(teamColor), goals));
	}
	
	
	public static Event goalkeeper(ETeamColor teamColor, int id)
	{
		return new Event(EventModifyValue.goalkeeper(EventTeam.fromTeamColor(teamColor), id));
	}
	
	
	public static Event teamName(ETeamColor teamColor, String name)
	{
		return new Event(EventModifyValue.teamName(EventTeam.fromTeamColor(teamColor), name));
	}
	
	
	public static Event ballPlacement(ETeamColor teamColor, IVector2 location)
	{
		return new Event(new EventCommand(
				EventCommand.Command.BALL_PLACEMENT,
				EventTeam.fromTeamColor(teamColor),
				new EventLocation(location)));
	}
	
	
	public static Event nextStage()
	{
		return new Event(new EventStage(EventStage.Operation.NEXT));
	}
	
	
	public static Event previousStage()
	{
		return new Event(new EventStage(EventStage.Operation.PREVIOUS));
	}
	
	
	public static Event endGame()
	{
		return new Event(new EventStage(EventStage.Operation.END_GAME));
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity") // simple mapping only
	public static EventCommand map(Referee.SSL_Referee.Command command)
	{
		switch (command)
		{
			case HALT:
				return new EventCommand(EventCommand.Command.HALT);
			case STOP:
				return new EventCommand(EventCommand.Command.STOP);
			case NORMAL_START:
				return new EventCommand(EventCommand.Command.NORMAL_START);
			case FORCE_START:
				return new EventCommand(EventCommand.Command.FORCE_START);
			case PREPARE_KICKOFF_YELLOW:
				return new EventCommand(EventCommand.Command.KICKOFF, EventTeam.YELLOW);
			case PREPARE_KICKOFF_BLUE:
				return new EventCommand(EventCommand.Command.KICKOFF, EventTeam.BLUE);
			case PREPARE_PENALTY_YELLOW:
				return new EventCommand(EventCommand.Command.PENALTY, EventTeam.YELLOW);
			case PREPARE_PENALTY_BLUE:
				return new EventCommand(EventCommand.Command.PENALTY, EventTeam.BLUE);
			case DIRECT_FREE_YELLOW:
				return new EventCommand(EventCommand.Command.DIRECT, EventTeam.YELLOW);
			case DIRECT_FREE_BLUE:
				return new EventCommand(EventCommand.Command.DIRECT, EventTeam.BLUE);
			case INDIRECT_FREE_YELLOW:
				return new EventCommand(EventCommand.Command.INDIRECT, EventTeam.YELLOW);
			case INDIRECT_FREE_BLUE:
				return new EventCommand(EventCommand.Command.INDIRECT, EventTeam.BLUE);
			case TIMEOUT_YELLOW:
				return new EventCommand(EventCommand.Command.TIMEOUT, EventTeam.YELLOW);
			case TIMEOUT_BLUE:
				return new EventCommand(EventCommand.Command.TIMEOUT, EventTeam.BLUE);
			case BALL_PLACEMENT_YELLOW:
			case BALL_PLACEMENT_BLUE:
				throw new IllegalArgumentException("Can not map ball placement command to event. Location is missing!");
			default:
				throw new IllegalArgumentException("Unsupported referee command: " + command);
		}
	}
	
	
	public static Event yellowCard(ETeamColor teamColor)
	{
		return new Event(
				new EventCard(EventCard.Type.YELLOW, EventTeam.fromTeamColor(teamColor), EventCard.Operation.ADD));
	}
	
	
	public static Event redCard(ETeamColor teamColor)
	{
		return new Event(
				new EventCard(EventCard.Type.RED, EventTeam.fromTeamColor(teamColor), EventCard.Operation.ADD));
	}
	
	
	public static Event triggerResetMatch()
	{
		return new Event(new EventTrigger(EventTrigger.Type.RESET_MATCH));
	}
	
	
	public static Event triggerSwitchColor()
	{
		return new Event(new EventTrigger(EventTrigger.Type.SWITCH_COLOR));
	}
	
	
	public static Event triggerSwitchSides()
	{
		return new Event(new EventTrigger(EventTrigger.Type.SWITCH_SIDES));
	}
	
	
	public static Event triggerUndo()
	{
		return new Event(new EventTrigger(EventTrigger.Type.UNDO));
	}
	
	
	public static Event triggerContinue()
	{
		return new Event(new EventTrigger(EventTrigger.Type.CONTINUE));
	}
}
