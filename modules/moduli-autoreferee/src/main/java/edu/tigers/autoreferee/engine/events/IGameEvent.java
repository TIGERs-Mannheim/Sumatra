/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.MessagesRobocupSslGameEvent.SSL_Referee_Game_Event;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public interface IGameEvent
{
	/**
	 * @return
	 */
	EGameEvent getType();
	
	
	/**
	 * @return
	 */
	long getTimestamp();
	
	
	/**
	 * @return
	 */
	ETeamColor getResponsibleTeam();
	
	
	/**
	 * @return
	 */
	Optional<BotID> getResponsibleBot();
	
	
	/**
	 * @return the cardPenalty
	 */
	List<CardPenalty> getCardPenalties();
	
	
	/**
	 * @return
	 */
	String buildLogString();
	
	
	/**
	 * @return
	 */
	FollowUpAction getFollowUpAction();
	
	
	/**
	 * @return the protobuf data format of this game event
	 */
	SSL_Referee_Game_Event toProtobuf();
}
