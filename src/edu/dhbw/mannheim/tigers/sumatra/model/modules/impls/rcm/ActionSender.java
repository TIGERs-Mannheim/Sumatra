/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.IRCMObserver;


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
	
	private ICommandInterpreter		cmdInterpreter	= new CommandInterpreterStub();
	private final String					identifier;
	private final List<IRCMObserver>	observers		= new CopyOnWriteArrayList<IRCMObserver>();
	
	
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
	 */
	public void startSending()
	{
		if ((cmdInterpreter.getBot() != null))
		{
			cmdInterpreter.getBot().setControlledBy(identifier);
		}
	}
	
	
	/**
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
	 */
	public void notifyTimedout()
	{
		if (cmdInterpreter.getBot().getBotID().isBot())
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
