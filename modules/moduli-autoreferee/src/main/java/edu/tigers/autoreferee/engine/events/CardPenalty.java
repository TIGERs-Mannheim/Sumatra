/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

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
}
