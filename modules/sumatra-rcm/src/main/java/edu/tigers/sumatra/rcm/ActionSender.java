/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


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
	 * @param newCmd
	 */
	public ABotSkill execute(final BotActionCommand newCmd)
	{
		return cmdInterpreter.interpret(newCmd);
	}
	
	
	/**
	 * Change the interpreter
	 * 
	 * @param interpreter
	 */
	public void setInterpreter(final ICommandInterpreter interpreter)
	{
		cmdInterpreter = Objects.requireNonNullElseGet(interpreter, CommandInterpreterStub::new);
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
