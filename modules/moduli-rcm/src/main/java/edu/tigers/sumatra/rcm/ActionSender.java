/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * *********************************************************
 */

package edu.tigers.sumatra.rcm;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;


/**
 * This class opens SendingThreads that connect to Sumatra.
 * 
 * @author Lukas
 */

public class ActionSender
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	private ICommandInterpreter cmdInterpreter = new CommandInterpreterStub();
	private final String identifier;
	private final List<IRCMObserver> observers = new CopyOnWriteArrayList<>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param identifier
	 */
	public ActionSender(final String identifier)
	{
		this.identifier = identifier;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Start sending
	 */
	public void startSending()
	{
		if (cmdInterpreter.getBot() != null)
		{
			cmdInterpreter.getBot().setControlledBy(identifier);
		}
	}
	
	
	/**
	 * Stop sending
	 */
	public void stopSending()
	{
		cmdInterpreter.stopAll();
	}
	
	
	/**
	 * @param newCmd
	 */
	public void execute(final BotActionCommand newCmd)
	{
		cmdInterpreter.interpret(newCmd);
	}
	
	
	/**
	 * Change the interpreter
	 * 
	 * @param interpreter
	 */
	public void setInterpreter(final ICommandInterpreter interpreter)
	{
		if (interpreter == null)
		{
			cmdInterpreter = new CommandInterpreterStub();
		} else
		{
			cmdInterpreter = interpreter;
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRCMObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRCMObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * Notify about the next bot
	 */
	public void notifyNextBot()
	{
		cmdInterpreter.stopAll();
		
		for (IRCMObserver observer : observers)
		{
			observer.onNextBot(this);
		}
	}
	
	
	/**
	 * Notify about the previous bot
	 */
	public void notifyPrevBot()
	{
		cmdInterpreter.stopAll();
		
		for (IRCMObserver observer : observers)
		{
			observer.onPrevBot(this);
		}
	}
	
	
	/**
	 * Notify about unassigned bot
	 */
	public void notifyBotUnassigned()
	{
		cmdInterpreter.stopAll();
		cmdInterpreter.getBot().setControlledBy("");
		cmdInterpreter = new CommandInterpreterStub();
		
		for (IRCMObserver observer : observers)
		{
			observer.onBotUnassigned(this);
		}
	}
	
	
	/**
	 * Notify about timeout
	 */
	public void notifyTimedOut()
	{
		if (cmdInterpreter.getBot().getBotId().isBot())
		{
			notifyBotUnassigned();
		}
	}
	
	
	/**
	 * @return the cmdInterpreter
	 */
	public final ICommandInterpreter getCmdInterpreter()
	{
		return cmdInterpreter;
	}
	
	
	/**
	 * @return the identifier
	 */
	public final String getIdentifier()
	{
		return identifier;
	}
}
