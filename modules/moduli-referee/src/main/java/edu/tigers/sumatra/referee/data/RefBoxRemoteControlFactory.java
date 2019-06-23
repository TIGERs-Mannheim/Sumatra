/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.data;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardTeam;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Can create {@link SSL_RefereeRemoteControlRequest} messages via static factory methods.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class RefBoxRemoteControlFactory
{
	private RefBoxRemoteControlFactory()
	{
	}
	
	
	/**
	 * @param command
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromCommand(final Command command)
	{
		SSL_RefereeRemoteControlRequest.Builder builder = SSL_RefereeRemoteControlRequest.newBuilder();
		builder.setMessageId(0);
		builder.setCommand(command);
		return builder.build();
	}
	
	
	/**
	 * @param teamColor
	 * @param placementPos
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromBallPlacement(final ETeamColor teamColor,
			final IVector2 placementPos)
	{
		SSL_RefereeRemoteControlRequest.Builder builder = SSL_RefereeRemoteControlRequest.newBuilder();
		builder.setMessageId(0);
		if (teamColor == ETeamColor.YELLOW)
		{
			builder.setCommand(Command.BALL_PLACEMENT_YELLOW);
		} else
		{
			builder.setCommand(Command.BALL_PLACEMENT_BLUE);
		}
		
		Referee.SSL_Referee.Point.Builder point = Referee.SSL_Referee.Point.newBuilder();
		point.setX((float) placementPos.x());
		point.setY((float) placementPos.y());
		builder.setDesignatedPosition(point);
		
		return builder.build();
	}
	
	
	/**
	 * @param stage
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromStage(final Stage stage)
	{
		SSL_RefereeRemoteControlRequest.Builder builder = SSL_RefereeRemoteControlRequest.newBuilder();
		builder.setMessageId(0);
		builder.setStage(stage);
		return builder.build();
	}
	
	
	/**
	 * @param teamColor
	 * @param type
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromCard(final ETeamColor teamColor,
			final CardType type)
	{
		SSL_RefereeRemoteControlRequest.Builder builder = SSL_RefereeRemoteControlRequest.newBuilder();
		builder.setMessageId(0);
		
		SSL_RefereeRemoteControlRequest.CardInfo.Builder card = builder.getCardBuilder();
		card.setType(type);
		if (teamColor == ETeamColor.YELLOW)
		{
			card.setTeam(CardTeam.TEAM_YELLOW);
		} else
		{
			card.setTeam(CardTeam.TEAM_BLUE);
		}
		
		builder.setCard(card);
		
		return builder.build();
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromTimeout(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.TIMEOUT_YELLOW);
		}
		
		return fromCommand(Command.TIMEOUT_BLUE);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromDirect(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.DIRECT_FREE_YELLOW);
		}
		
		return fromCommand(Command.DIRECT_FREE_BLUE);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromIndirect(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.INDIRECT_FREE_YELLOW);
		}
		
		return fromCommand(Command.INDIRECT_FREE_BLUE);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromPenalty(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.PREPARE_PENALTY_YELLOW);
		}
		
		return fromCommand(Command.PREPARE_PENALTY_BLUE);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromKickoff(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.PREPARE_KICKOFF_YELLOW);
		}
		
		return fromCommand(Command.PREPARE_KICKOFF_BLUE);
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromGoal(final ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return fromCommand(Command.GOAL_YELLOW);
		}
		
		return fromCommand(Command.GOAL_BLUE);
	}
	
	
	/**
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromHalt()
	{
		return fromCommand(Command.HALT);
	}
	
	
	/**
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromStop()
	{
		return fromCommand(Command.STOP);
	}
	
	
	/**
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromNormalStart()
	{
		return fromCommand(Command.NORMAL_START);
	}
	
	
	/**
	 * @return
	 */
	public static SSL_RefereeRemoteControlRequest fromForceStart()
	{
		return fromCommand(Command.FORCE_START);
	}

}
