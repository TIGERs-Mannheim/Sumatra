/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class PullBallDriver extends ABaseDriver
{
	
	// private double driveSpeed = 0;
	private boolean	finished		= false;
	
	private IVector2	direction	= null;
	private double		speed			= 1;
	
	
	/**
	 */
	public PullBallDriver()
	{
		clearSupportedCommands();
		addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	@Override
	public boolean isDone()
	{
		return finished;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		direction = direction.scaleToNew(speed);
		return new Vector3(direction.x(), direction.y(), 0);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.PULL_BALL;
	}
	
	
	/**
	 * @param dir
	 */
	public void setDirection(final IVector2 dir)
	{
		direction = dir.normalizeNew();
	}
	
	
	/**
	 * @param speed
	 */
	public void setSpeed(final double speed)
	{
		this.speed = speed;
	}
	
}
