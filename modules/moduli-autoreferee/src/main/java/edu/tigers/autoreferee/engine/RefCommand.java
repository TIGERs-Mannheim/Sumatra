/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Optional;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;


/**
 * This class is a wrapper around the {@link Command} enum. It stores an additional position for the ball placement
 * commands.
 * 
 * @author "Lukas Magel"
 */
public class RefCommand
{
	/**
	 * @author "Lukas Magel"
	 */
	public enum CommandType
	{
		/**  */
		COMMAND,
		/**  */
		CARD
	}
	
	
	private final CommandType	type;
	
	private final Command		command;
	private final IVector2		kickPos;
	
	private final CardType		cardType;
	private final ETeamColor	cardTeam;
	
	
	/**
	 * @param command
	 */
	public RefCommand(final Command command)
	{
		this(command, null);
	}
	
	
	/**
	 * @param command
	 * @param kickPos
	 */
	public RefCommand(final Command command, final IVector2 kickPos)
	{
		this(CommandType.COMMAND, command, kickPos, null, null);
	}
	
	
	/**
	 * @param cardType
	 * @param cardTeam
	 */
	public RefCommand(final CardType cardType, final ETeamColor cardTeam)
	{
		this(CommandType.CARD, null, null, cardType, cardTeam);
	}
	
	
	private RefCommand(final CommandType type, final Command command, final IVector2 kickPos,
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
	public CommandType getType()
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
		if (obj instanceof RefCommand)
		{
			RefCommand other = (RefCommand) obj;
			return equalsCommand(other);
		}
		return false;
	}
	
	
	private boolean equalsCommand(final RefCommand other)
	{
		if (type != other.type)
		{
			return false;
		}
		
		switch (type)
		{
			case CARD:
				return (cardType == other.cardType) && (cardTeam == other.cardTeam);
			case COMMAND:
				if (command != other.command)
				{
					return false;
				}
				if (kickPos == null)
				{
					if (other.kickPos == null)
					{
						return true;
					}
					return false;
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
		switch (type)
		{
			case CARD:
				result = (prime * result) + cardType.hashCode();
				result = (prime * result) + cardTeam.hashCode();
				break;
			case COMMAND:
				result = (prime * result) + command.hashCode();
				if (kickPos != null)
				{
					result = (prime * result) + kickPos.hashCode();
				}
				break;
		}
		return result;
	}
}
