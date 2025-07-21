/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.referee.view;

import edu.tigers.sumatra.referee.proto.SslGcApi;

import javax.swing.JPanel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


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


	protected void sendGameControllerEvent(final SslGcApi.Input event)
	{
		for (IRefBoxRemoteControlRequestObserver observer : observers)
		{
			observer.sendGameControllerEvent(event);
		}
	}
}
