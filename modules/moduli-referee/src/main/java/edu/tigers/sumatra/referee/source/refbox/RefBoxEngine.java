/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.source.refbox;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardTeam;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.Referee.SSL_Referee.TeamInfo;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.source.refbox.time.ITimeProvider;
import edu.tigers.sumatra.referee.source.refbox.time.SystemTimeProvider;


/**
 * Handles RefBox states and control requests.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class RefBoxEngine
{
	private static final int HALF_TIME_MIN = 5;
	private static final int EXTRA_TIME_MIN = 5;
	private static final int BREAK_TIME_MIN = 5;
	private static final int SHOOTOUT_BREAK_TIME_MIN = 2;

	private Stage stage = Stage.NORMAL_FIRST_HALF_PRE;
	private Command command = Command.HALT;
	private int stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(HALF_TIME_MIN);
	private int commandCounter = 0;
	private Map<ETeamColor, TeamData> teams = new EnumMap<>(ETeamColor.class);
	private ITimeProvider timeProdivder = new SystemTimeProvider();
	private Vector2 placementPos = Vector2.fromXY(0, 0);
	private ETeamColor timeoutTeam = ETeamColor.NEUTRAL;
	
	private int lastCommandCounter;
	private long lastTime;
	private long lastCommandTimestamp;
	
	
	/**
	 * Constructor.
	 */
	public RefBoxEngine()
	{
		teams.put(ETeamColor.YELLOW, new TeamData(ETeamColor.YELLOW.name()));
		teams.put(ETeamColor.BLUE, new TeamData(ETeamColor.BLUE.name()));
		
		lastCommandCounter = 0;
		lastTime = timeProdivder.getTimeInMicroseconds();
		lastCommandTimestamp = lastTime;
	}
	
	
	/**
	 * Spin the engine once.
	 * 
	 * @return
	 */
	public SSL_Referee spin()
	{
		// determine how much time passed by
		long timeNow = timeProdivder.getTimeInMicroseconds();
		int timePassed = (int) (timeNow - lastTime);
		lastTime = timeNow;
		
		// update command timestamp
		if (lastCommandCounter != commandCounter)
		{
			lastCommandTimestamp = timeNow;
		}
		
		lastCommandCounter = commandCounter;
		
		// run clocks only when we are not in halt or stop (changed 2018)
		if (command != Command.HALT && command != Command.STOP)
		{
			// subtract passed time from stage time and yellow cards
			if ((command != Command.TIMEOUT_YELLOW) && (command != Command.TIMEOUT_BLUE))
			{
				stageTimeLeft -= timePassed;
			}
			
			teams.values().forEach(t -> t.tickYellowCards(timePassed));
			
			// if there is a timeout active, decrement the teams timeout time
			if (timeoutTeam != ETeamColor.NEUTRAL)
			{
				teams.get(timeoutTeam).tickTimeoutTime(timePassed);
			}
		}
		
		// construct referee message
		Referee.SSL_Referee.Builder msgBuilder = Referee.SSL_Referee.newBuilder();
		msgBuilder.setPacketTimestamp(timeNow);
		msgBuilder.setStage(stage);
		msgBuilder.setStageTimeLeft(stageTimeLeft);
		msgBuilder.setCommand(command);
		msgBuilder.setCommandCounter(commandCounter);
		msgBuilder.setCommandTimestamp(lastCommandTimestamp);
		msgBuilder.setBlue(getTeamInfo(ETeamColor.BLUE));
		msgBuilder.setYellow(getTeamInfo(ETeamColor.YELLOW));
		
		if ((command == Command.BALL_PLACEMENT_BLUE) || (command == Command.BALL_PLACEMENT_YELLOW))
		{
			Referee.SSL_Referee.Point.Builder point = Referee.SSL_Referee.Point.newBuilder();
			point.setX((float) placementPos.x());
			point.setY((float) placementPos.y());
			msgBuilder.setDesignatedPosition(point);
		}
		
		return msgBuilder.build();
	}
	
	
	private TeamInfo.Builder getTeamInfo(final ETeamColor color)
	{
		TeamData data = teams.get(color);
		
		TeamInfo.Builder builder = TeamInfo.newBuilder();
		builder.setGoalie(data.getGoalie());
		builder.setName(color.name());
		builder.setScore(data.getScore());
		builder.setYellowCards(data.getYellowCards());
		builder.setRedCards(data.getRedCards());
		builder.setTimeouts(data.getTimeouts());
		builder.setTimeoutTime(data.getTimeoutTime());
		builder.addAllYellowCardTimes(data.getYellowCardTimes());
		
		return builder;
	}
	
	
	/**
	 * Handle a control request.
	 * 
	 * @param request
	 * @return
	 */
	public Outcome handleControlRequest(final SSL_RefereeRemoteControlRequest request)
	{
		// verify that there is one action at maximum...
		int numActions = 0;
		if (request.hasStage())
		{
			++numActions;
		}
		
		if (request.hasCommand())
		{
			++numActions;
		}
		
		if (request.hasCard())
		{
			++numActions;
		}
		
		if (numActions > 1)
		{
			return Outcome.MULTIPLE_ACTIONS;
		}
		
		// ...pass. Handle action.
		if (request.hasStage())
		{
			return handleStage(request);
		} else if (request.hasCard())
		{
			return handleCard(request);
		} else if (request.hasCommand())
		{
			return handleCommand(request);
		}
		
		// Note: no action needs actually to be present. Control request can be empty.
		
		return Outcome.OK;
	}
	
	
	/**
	 * Set new time provider.
	 * 
	 * @param provider
	 */
	public void setTimeProvider(final ITimeProvider provider)
	{
		timeProdivder = provider;
		lastTime = timeProdivder.getTimeInMicroseconds();
	}
	
	
	/**
	 * @param request
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private Outcome handleCommand(final SSL_RefereeRemoteControlRequest request)
	{
		switch (request.getCommand())
		{
			case BALL_PLACEMENT_BLUE:
			case BALL_PLACEMENT_YELLOW:
				if (!request.hasDesignatedPosition())
				{
					return Outcome.BAD_DESIGNATED_POSITION;
				}
				placementPos.setX(request.getDesignatedPosition().getX());
				placementPos.setY(request.getDesignatedPosition().getY());
				break;
			case GOAL_BLUE:
				teams.get(ETeamColor.BLUE).addScore();
				break;
			case GOAL_YELLOW:
				teams.get(ETeamColor.YELLOW).addScore();
				break;
			case NORMAL_START:
				handleNormalStart();
				break;
			case STOP:
				timeoutTeam = ETeamColor.NEUTRAL;
				break;
			case TIMEOUT_BLUE:
				if (timeoutTeam == ETeamColor.NEUTRAL)
				{
					timeoutTeam = ETeamColor.BLUE;
					teams.get(ETeamColor.BLUE).takeTimeout();
				} else
				{
					return Outcome.BAD_COMMAND;
				}
				break;
			case TIMEOUT_YELLOW:
				if (timeoutTeam == ETeamColor.NEUTRAL)
				{
					timeoutTeam = ETeamColor.YELLOW;
					teams.get(ETeamColor.YELLOW).takeTimeout();
				} else
				{
					return Outcome.BAD_COMMAND;
				}
				break;
			default:
				break;
		}
		
		command = request.getCommand();
		++commandCounter;
		
		return Outcome.OK;
	}
	
	
	private void handleNormalStart()
	{
		switch (stage)
		{
			case EXTRA_FIRST_HALF_PRE:
				stage = Stage.EXTRA_FIRST_HALF;
				stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(EXTRA_TIME_MIN);
				teams.values().forEach(t -> t.setTimeouts(2));
				teams.values().forEach(t -> t.setTimeoutTime((int) TimeUnit.MINUTES.toMicros(5)));
				break;
			case EXTRA_SECOND_HALF_PRE:
				stage = Stage.EXTRA_SECOND_HALF;
				stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(EXTRA_TIME_MIN);
				break;
			case NORMAL_FIRST_HALF_PRE:
				stage = Stage.NORMAL_FIRST_HALF;
				stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(HALF_TIME_MIN);
				break;
			case NORMAL_SECOND_HALF_PRE:
				stage = Stage.NORMAL_SECOND_HALF;
				stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(HALF_TIME_MIN);
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * @param request
	 */
	private Outcome handleCard(final SSL_RefereeRemoteControlRequest request)
	{
		CardInfo card = request.getCard();
		TeamData team;
		if (card.getTeam() == CardTeam.TEAM_YELLOW)
		{
			team = teams.get(ETeamColor.YELLOW);
		} else
		{
			team = teams.get(ETeamColor.BLUE);
		}
		
		if (card.getType() == CardType.CARD_RED)
		{
			team.addRedCard();
		} else
		{
			team.addYellowCard();
		}
		
		return Outcome.OK;
	}
	
	
	/**
	 * @param request
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private Outcome handleStage(final SSL_RefereeRemoteControlRequest request)
	{
		List<Stage> forbiddenStages = new ArrayList<>();
		forbiddenStages.add(Stage.NORMAL_FIRST_HALF);
		forbiddenStages.add(Stage.NORMAL_SECOND_HALF);
		forbiddenStages.add(Stage.EXTRA_FIRST_HALF);
		forbiddenStages.add(Stage.EXTRA_SECOND_HALF);
		
		if (forbiddenStages.contains(request.getStage()))
		{
			return Outcome.BAD_STAGE;
		}
		
		if (request.getStage() != stage)
		{
			stage = request.getStage();
			
			switch (stage)
			{
				case NORMAL_FIRST_HALF_PRE:
					stageTimeLeft = 0;
					teams.values().forEach(TeamData::reset);
					break;
				case EXTRA_TIME_BREAK:
				case NORMAL_HALF_TIME:
					stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(BREAK_TIME_MIN);
					break;
				case EXTRA_HALF_TIME:
				case PENALTY_SHOOTOUT_BREAK:
					stageTimeLeft = (int) TimeUnit.MINUTES.toMicros(SHOOTOUT_BREAK_TIME_MIN);
					break;
				case PENALTY_SHOOTOUT:
				case POST_GAME:
					stageTimeLeft = 0;
					break;
				default:
					break;
			}
		}
		
		return Outcome.OK;
	}
	
	
	/**
	 * Update the keeper id of the given team
	 * 
	 * @param teamColor the team color
	 * @param id the keeper id
	 */
	public void setKeeperId(ETeamColor teamColor, int id)
	{
		teams.get(teamColor).setGoalie(id);
	}
}
