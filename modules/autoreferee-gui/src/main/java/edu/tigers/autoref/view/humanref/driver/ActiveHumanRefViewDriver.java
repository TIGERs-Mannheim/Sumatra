/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.driver;

import edu.tigers.autoref.view.humanref.ActiveHumanRefPanel;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * @author "Lukas Magel"
 */
public class ActiveHumanRefViewDriver extends BaseHumanRefViewDriver
{
	private final ActiveHumanRefPanel panel;
	
	
	/**
	 * @param panel
	 */
	public ActiveHumanRefViewDriver(final ActiveHumanRefPanel panel)
	{
		super(panel);
		this.panel = panel;
	}
	
	
	@Override
	public void setNewGameLogEntry(final GameLogEntry entry)
	{
		switch (entry.getType())
		{
			case FOLLOW_UP:
				FollowUpAction action = entry.getFollowUpAction();
				setFollowUp(action);
				break;
			case GAME_EVENT:
				workGameEventEntry(entry);
				break;
			case GAME_STATE:
				workGameStateEntry(entry);
				break;
			default:
				break;
			
		}
	}
	
	
	private void workGameEventEntry(final GameLogEntry entry)
	{
		if (entry.isAcceptedByEngine())
		{
			IGameEvent event = entry.getGameEvent();
			setEvent(event);
			setFollowUp(event.getFollowUpAction());
		}
	}
	
	
	private void workGameStateEntry(final GameLogEntry entry)
	{
		if (entry.getGamestate().getState() == EGameState.RUNNING)
		{
			clearEvent();
			setFollowUp(null);
		}
	}
	
	
	private void setFollowUp(final FollowUpAction action)
	{
		if (action == null)
		{
			panel.clearFollowUp();
		} else
		{
			panel.setNextAction(action);
		}
	}
	
	
	private void setEvent(final IGameEvent event)
	{
		panel.setEvent(event);
	}
	
	
	private void clearEvent()
	{
		panel.clearEvent();
	}
	
}
