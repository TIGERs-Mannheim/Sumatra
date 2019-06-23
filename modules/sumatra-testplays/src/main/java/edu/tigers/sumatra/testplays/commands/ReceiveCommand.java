/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class ReceiveCommand extends ACommand
{
	
	/**
	 * The receive command will wait for a
	 * ball from a pass command with the same
	 * pass group id
	 */
	private int passGroup = 0;
	
	
	/**
	 * Creates a new ReceiveCommand
	 */
	public ReceiveCommand()
	{
		
		super(CommandType.RECEIVE);
	}
	
	
	public int getPassGroup()
	{
		
		return passGroup;
	}
	
	
	public void setPassGroup(final int passGroup)
	{
		
		this.passGroup = passGroup;
	}
	
	
	@Override
	public String toString()
	{
		return getCommandType() + " [" + getPassGroup() + "]";
	}
}
