/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.testplays.util.Point;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class KickCommand extends ACommand
{
	
	@JsonProperty("dest")
	private Point	destination;
	
	@JsonProperty("speed")
	private double	kickSpeed;

	@JsonProperty("device")
	private EKickerDevice kickerDevice;

	/**
	 * Creates a new KickCommand
	 */
	public KickCommand()
	{
		
		super(CommandType.KICK);
	}

	/**
	 * Creates a new KickCommand
	 *
	 * @param dest The destination
	 * @param speed How fast to kick the ball. May be zero to use automatic value.
	 * @param device The kicker device
	 */
	public KickCommand(Point dest, double speed, EKickerDevice device)
	{
		
		this();
		
		setDestination(dest);
		setKickSpeed(speed);
		setKickerDevice(device);
	}
	
	
	public void setDestination(Point destination)
	{
		
		this.destination = destination;
	}
	
	
	public Point getDestination()
	{
		
		return destination;
	}
	
	
	public double getKickSpeed()
	{
		
		return kickSpeed;
	}
	
	
	public void setKickSpeed(final double kickSpeed)
	{
		
		this.kickSpeed = kickSpeed;
	}

	@Override
	public String toString() {
		return getCommandType().name();
	}

	public EKickerDevice getKickerDevice() {
		return kickerDevice;
	}

	public void setKickerDevice(final EKickerDevice kickerDevice) {
		this.kickerDevice = kickerDevice;
	}
}
