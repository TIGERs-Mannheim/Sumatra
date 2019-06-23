/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author AndreR <andre@ryll.cc>
 */
public abstract class ARefBoxRemoteControlGeneratorPanel extends JPanel
{
	private static final long serialVersionUID = 4824407573372334034L;
	
	private final transient List<IRefBoxRemoteControlRequestObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRefBoxRemoteControlRequestObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRefBoxRemoteControlRequestObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifyNewControlRequest(final SSL_RefereeRemoteControlRequest event)
	{
		for (IRefBoxRemoteControlRequestObserver observer : observers)
		{
			observer.onNewControlRequest(event);
		}
	}
	
	
	protected void notifyGoalieChanged(final BotID keeperId)
	{
		for (IRefBoxRemoteControlRequestObserver observer : observers)
		{
			observer.onGoalieChanged(keeperId);
		}
	}
}
