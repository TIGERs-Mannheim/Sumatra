/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 17, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class CardPenalty
{
	private final CardType		type;
	private final ETeamColor	cardTeam;
	
	
	/**
	 * @param type
	 * @param cardTeam
	 */
	public CardPenalty(final CardType type, final ETeamColor cardTeam)
	{
		this.type = type;
		this.cardTeam = cardTeam;
	}
	
	
	/**
	 * @return the type
	 */
	public CardType getType()
	{
		return type;
	}
	
	
	/**
	 * @return the cardTeam
	 */
	public ETeamColor getCardTeam()
	{
		return cardTeam;
	}
	
	
	/**
	 * Create a {@link RefCommand} instance that can be sent to the refbox
	 * 
	 * @return ref command representing this card penalty
	 */
	public RefCommand toRefCommand()
	{
		return new RefCommand(type, cardTeam);
	}
}
