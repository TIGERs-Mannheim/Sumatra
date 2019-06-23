/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.Optional;

import edu.tigers.autoreferee.engine.states.impl.StopState;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Encapsulates all information required by the autoref to initiate a new game situation after the game has been
 * stopped. The {@link StopState} acts upon the contents of this type.
 * 
 * @author "Lukas Magel"
 */
public class FollowUpAction
{
	
	/**
	 * @author "Lukas Magel"
	 */
	public enum EActionType
	{
		/**  */
		INDIRECT_FREE,
		/**  */
		DIRECT_FREE,
		/**  */
		KICK_OFF,
		/**  */
		FORCE_START,
		/**  */
		PENALTY
	}
	
	private final ETeamColor teamInFavor;
	private final EActionType actionType;
	private final IVector2 newBallPos;
	
	
	/**
	 * @param actionType
	 * @param teamInFavor
	 * @param newBallPos
	 */
	public FollowUpAction(final EActionType actionType, final ETeamColor teamInFavor,
			final IVector2 newBallPos)
	{
		if (teamInFavor != null)
		{
			this.teamInFavor = teamInFavor;
		} else
		{
			this.teamInFavor = ETeamColor.NEUTRAL;
		}
		this.actionType = actionType;
		this.newBallPos = newBallPos;
	}
	
	
	/**
	 * @return the teamInFavor
	 */
	public ETeamColor getTeamInFavor()
	{
		return teamInFavor;
	}
	
	
	/**
	 * @return the action
	 */
	public EActionType getActionType()
	{
		return actionType;
	}
	
	
	/**
	 * @return the newBallPosition
	 */
	public Optional<IVector2> getNewBallPosition()
	{
		return Optional.ofNullable(newBallPos);
	}
	
	
	/**
	 * Returns the command to perform this action
	 * 
	 * @return
	 */
	public Command getCommand()
	{
		switch (actionType)
		{
			case DIRECT_FREE:
				return chooseCommandByTeam(Command.DIRECT_FREE_BLUE, Command.DIRECT_FREE_YELLOW);
			case INDIRECT_FREE:
				return chooseCommandByTeam(Command.INDIRECT_FREE_BLUE, Command.INDIRECT_FREE_YELLOW);
			case KICK_OFF:
				return chooseCommandByTeam(Command.PREPARE_KICKOFF_BLUE, Command.PREPARE_KICKOFF_YELLOW);
			case FORCE_START:
				return Command.FORCE_START;
			case PENALTY:
				return chooseCommandByTeam(Command.PREPARE_PENALTY_BLUE, Command.PREPARE_PENALTY_YELLOW);
			default:
				throw new IllegalArgumentException(
						"Please add the following action type to the switch case: " + actionType);
		}
	}
	
	
	private Command chooseCommandByTeam(
			final Command blue,
			final Command yellow)
	{
		if (teamInFavor == ETeamColor.BLUE)
		{
			return blue;
		} else if (teamInFavor == ETeamColor.YELLOW)
		{
			return yellow;
		} else if (Math.random() >= 0.5)
		{
			return blue;
		}
		return yellow;
	}
	
	
	@Override
	public boolean equals(final Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof FollowUpAction)
		{
			FollowUpAction otherAction = (FollowUpAction) other;
			return (otherAction.actionType == actionType)
					&& (otherAction.teamInFavor == teamInFavor)
					&& otherAction.getNewBallPosition().equals(getNewBallPosition());
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + actionType.hashCode();
		result = prime * result + teamInFavor.hashCode();
		result = (prime * result) + ((newBallPos == null) ? 0 : newBallPos.hashCode());
		return result;
	}
	
	
	@Override
	public String toString()
	{
		return "FollowUpAction{" +
				"actionType=" + actionType +
				", teamInFavor=" + teamInFavor +
				", newBallPos=" + newBallPos +
				'}';
	}
}
