/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
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
	public EGameEvent getType();
	
	
	/**
	 * @return
	 */
	public EEventCategory getCategory();
	
	
	/**
	 * @return
	 */
	public long getTimestamp();
	
	
	/**
	 * @return
	 */
	public ETeamColor getResponsibleTeam();
	
	
	/**
	 * @return
	 */
	public Optional<BotID> getResponsibleBot();
	
	
	/**
	 * @return the cardPenalty
	 */
	public Optional<CardPenalty> getCardPenalty();
	
	
	/**
	 * @return
	 */
	public String buildLogString();
	
	
	/**
	 * @return
	 */
	public FollowUpAction getFollowUpAction();
}
