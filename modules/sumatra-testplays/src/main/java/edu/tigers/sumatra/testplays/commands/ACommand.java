/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PathCommand.class, name = "Path"),
		@JsonSubTypes.Type(value = SynchronizeCommand.class, name = "Synchronize"),
})
public abstract class ACommand
{
	
	@JsonProperty("commandType")
	private CommandType commandType;
	
	/**
	 * The command's type.
	 * May be used to determine which implementation to use.
	 */
	public enum CommandType
	{
		PATH(PathCommand.class),
		SYNCHRONIZE(SynchronizeCommand.class),
		KICK(KickCommand.class),
		PASS(PassCommand.class),
		RECEIVE(ReceiveCommand.class),
		REDIRECT(RedirectCommand.class);
		
		Class clazz;
		
		
		CommandType(Class clazz)
		{
			this.clazz = clazz;
		}
		
		
		public Class getClazz()
		{
			return clazz;
		}
	}
	
	
	protected ACommand(CommandType commandType)
	{
		
		this.commandType = commandType;
	}
	
	
	public CommandType getCommandType()
	{
		
		return commandType;
	}
	
	
	@Override
	public String toString()
	{
		return getCommandType().toString();
	}
}
