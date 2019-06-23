/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.multiteammessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;


/**
 * @author JulianT
 */
public abstract class AMultiTeamMessage extends AModule
{
	/**  */
	public static final String								MODULE_TYPE	= "AMultiTeamMessage";
	/** */
	public static final String								MODULE_ID	= "multiTeamMessage";
																					
	private final List<IMultiTeamMessageConsumer>	consumers	= new CopyOnWriteArrayList<>();
																					
																					
	protected void notifyNewMultiTeamMessage(final MultiTeamMessage message)
	{
		for (final IMultiTeamMessageConsumer observer : consumers)
		{
			observer.onNewMultiTeamMessage(message);
		}
	}
	
	
	/**
	 * @param consumer
	 */
	public void addMultiTeamMessageConsumer(final IMultiTeamMessageConsumer consumer)
	{
		consumers.add(consumer);
	}
	
	
	/**
	 * @param consumer
	 */
	public void removeMultiTeamMessageConsumer(final IMultiTeamMessageConsumer consumer)
	{
		consumers.remove(consumer);
	}
	
	
	/**
	 * @return
	 */
	public final List<IMultiTeamMessageConsumer> getConsumers()
	{
		return consumers;
	}
}
