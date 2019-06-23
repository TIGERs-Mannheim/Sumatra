/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class RedirectCommand extends ACommand
{
	
	private int redirectGroup = 0;
	
	private Point destination = new Point(4500, 0);
	
	
	/**
	 * Default constructor
	 */
	public RedirectCommand()
	{
		super(CommandType.REDIRECT);
	}
	
	
	/**
	 * Constructor with quick setting
	 *
	 * @param group
	 */
	public RedirectCommand(final int group)
	{
		this();
		
		redirectGroup = group;
	}
	
	
	public int getRedirectGroup()
	{
		return redirectGroup;
	}
	
	
	public void setRedirectGroup(final int redirectGroup)
	{
		this.redirectGroup = redirectGroup;
	}
	
	
	public Point getDestination()
	{
		return destination;
	}
	
	
	public void setDestination(final Point destination)
	{
		this.destination = destination;
	}
}
