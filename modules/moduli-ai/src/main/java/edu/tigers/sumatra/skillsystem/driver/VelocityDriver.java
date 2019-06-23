/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 6, 2016
 * Author(s): ArneS <arne.sachtler@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
public class VelocityDriver extends ABaseDriver
{
	private double		rotationalSpeed		= 0.0;
	private double		translationalSpeed	= 0.0;
	private IVector2	direction				= null;
														
														
	/**
	 * @author ArneS <arne.sachtler@dlr.de>
	 */
	public enum EVelocityMode
	{
		/** use wheel specific velocity botskill */
		WHEEL_VELOCITY,
		/** use botskill for cartesian velocity */
		CARTESIAN_LOCAL_VELOCITY;
	}
	
	
	/**
	 * drive with given velocity, use
	 */
	public VelocityDriver()
	{
		this(EVelocityMode.CARTESIAN_LOCAL_VELOCITY);
	}
	
	
	/**
	 * @param mode
	 */
	public VelocityDriver(final EVelocityMode mode)
	{
		clearSupportedCommands();
		switch (mode)
		{
			case CARTESIAN_LOCAL_VELOCITY:
				addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
				break;
			case WHEEL_VELOCITY:
				addSupportedCommand(EBotSkill.WHEEL_VELOCITY);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.VELOCITY;
	}
	
	
	/**
	 * @param omega
	 */
	public void setRotationalSpeed(final double omega)
	{
		rotationalSpeed = omega;
	}
	
	
	/**
	 * @param speed
	 */
	public void setTranslationalSpeed(final double speed)
	{
		translationalSpeed = speed;
	}
	
	
	/**
	 * @param translSpeed
	 * @param rotSpeed
	 */
	public void setSpeed(final double translSpeed, final double rotSpeed)
	{
		setTranslationalSpeed(translSpeed);
		setRotationalSpeed(rotSpeed);
	}
	
	
	/**
	 * @param direction
	 */
	public void setDirection(final IVector2 direction)
	{
		this.direction = direction;
	}
	
	
	/**
	 * @return the translationalSpeed
	 */
	public double getTranslationalSpeed()
	{
		return translationalSpeed;
	}
	
	
	/**
	 * @return the rotationalSpeed
	 */
	public double getRotationalSpeed()
	{
		return rotationalSpeed;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		IVector3 vel;
		if (direction == null)
		{
			vel = new Vector3(0.0, 0.0, rotationalSpeed);
		} else
		{
			vel = new Vector3(direction.scaleToNew(translationalSpeed), rotationalSpeed);
		}
		return vel;
	}
	
}
