/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.driver;

import java.util.LinkedList;

import edu.tigers.autoref.view.humanref.PassiveHumanRefPanel;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry;


/**
 * @author "Lukas Magel"
 */
public class PassiveHumanRefViewDriver extends BaseHumanRefViewDriver
{
	private final static int				eventLogSize	= 5;
	private final PassiveHumanRefPanel	panel;
	
	private LinkedList<IGameEvent>		events			= new LinkedList<>();
	
	
	/**
	 * @param panel
	 */
	public PassiveHumanRefViewDriver(final PassiveHumanRefPanel panel)
	{
		super(panel);
		this.panel = panel;
	}
	
	
	@Override
	public void setNewGameLogEntry(final GameLogEntry entry)
	{
		switch (entry.getType())
		{
			case GAME_EVENT:
				IGameEvent event = entry.getGameEvent();
				addToList(event);
				updateEvents();
				break;
			default:
				break;
		
		}
	}
	
	
	private void addToList(final IGameEvent event)
	{
		if (events.size() >= eventLogSize)
		{
			events.pollLast();
		}
		events.offerFirst(event);
	}
	
	
	private void updateEvents()
	{
		panel.setEvents(events);
	}
	
}
