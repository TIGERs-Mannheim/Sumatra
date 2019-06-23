/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class CommandList
{

	private String title = getClass().getSimpleName();
	private List<ACommand> commands = new ArrayList<>();
	
	
	public List<ACommand> getCommands()
	{
		return commands;
	}
	
	
	public void setCommands(final List<ACommand> commands)
	{
		this.commands = commands;
	}
	
	
	/**
	 * Adds a command to this list
	 * 
	 * @param command
	 */
	public void add(ACommand command)
	{
		commands.add(command);
	}
	
	
	/**
	 * Removes a command from this list
	 * 
	 * @param command
	 */
	public void remove(ACommand command)
	{
		commands.remove(command);
	}

	@Override
	public String toString() {
		return title + " [" + commands.size() + "]";
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(final String title) {

		this.title = title;
	}
}
