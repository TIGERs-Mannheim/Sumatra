/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.Optional;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This class is a wrapper around the {@link Command} enum. It stores an additional position for the ball placement
 * commands.
 * 
 * @author "Lukas Magel"
 */
public class RefboxRemoteCommand
{
	private final RemoteCommandType type;
	private final Command command;
	private final IVector2 kickPos;
	private final CardType cardType;
	private final ETeamColor cardTeam;
	
	
	/**
	 * @param command
	 */
	public RefboxRemoteCommand(final Command command)
	{
		this(command, null);
	}
	
	
	/**
	 * @param command
	 * @param kickPos
	 */
	public RefboxRemoteCommand(final Command command, final IVector2 kickPos)
	{
		this(RemoteCommandType.COMMAND, command, kickPos, null, null);
	}
	
	
	/**
	 * @param cardType
	 * @param cardTeam
	 */
	public RefboxRemoteCommand(final CardType cardType, final ETeamColor cardTeam)
	{
		this(RemoteCommandType.CARD, null, null, cardType, cardTeam);
	}
	
	
	private RefboxRemoteCommand(final RemoteCommandType type, final Command command, final IVector2 kickPos,
			final CardType cardType, final ETeamColor cardTeam)
	{
		this.type = type;
		
		this.command = command;
		this.kickPos = kickPos;
		
		this.cardType = cardType;
		this.cardTeam = cardTeam;
	}
	
	
	/**
	 * @return the type
	 */
	public RemoteCommandType getType()
	{
		return type;
	}
	
	
	/**
	 * @return the command
	 */
	public Command getCommand()
	{
		return command;
	}
	
	
	/**
	 * @return the kickPos
	 */
	public Optional<IVector2> getKickPos()
	{
		return Optional.ofNullable(kickPos);
	}
	
	
	/**
	 * @return the cardCommand
	 */
	public CardType getCardType()
	{
		return cardType;
	}
	
	
	/**
	 * @return
	 */
	public ETeamColor getCardTeam()
	{
		return cardTeam;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof RefboxRemoteCommand)
		{
			RefboxRemoteCommand other = (RefboxRemoteCommand) obj;
			return equalsCommand(other);
		}
		return false;
	}
	
	
	private boolean equalsCommand(final RefboxRemoteCommand other)
	{
		if (type != other.type)
		{
			return false;
		}
		
		if (type == RemoteCommandType.CARD)
		{
			return (cardType == other.cardType) && (cardTeam == other.cardTeam);
		} else if (type == RemoteCommandType.COMMAND)
		{
			if (command != other.command)
			{
				return false;
			}
			if (kickPos == null)
			{
				return other.kickPos == null;
			}
			return kickPos.equals(other.kickPos);
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		
		result = (prime * result) + type.hashCode();
		if (type == RemoteCommandType.CARD)
		{
			result = (prime * result) + cardType.hashCode();
			result = (prime * result) + cardTeam.hashCode();
		} else if (type == RemoteCommandType.COMMAND)
		{
			result = (prime * result) + command.hashCode();
			if (kickPos != null)
			{
				result = (prime * result) + kickPos.hashCode();
			}
		}
		return result;
	}
	
	
	@Override
	public String toString()
	{
		switch (type)
		{
			case COMMAND:
				return command + " " + kickPos;
			case CARD:
				return cardType + " for " + cardTeam;
			default:
				throw new IllegalStateException();
		}
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public enum RemoteCommandType
	{
		/**  */
		COMMAND,
		/**  */
		CARD
	}
}
