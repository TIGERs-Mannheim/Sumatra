/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PassCommand extends ACommand
{
	
	/**
	 * The pass command will determine the
	 * target bot by searching for a receive
	 * command with the same group id
	 */
	private int passGroup = 0;

	@JsonProperty("device")
	private EKickerDevice kickerDevice = EKickerDevice.STRAIGHT;
	
	
	/**
	 * Creates a new PassCommand
	 */
	public PassCommand()
	{
		
		super(CommandType.PASS);
	}
	
	
	public int getPassGroup()
	{
		
		return passGroup;
	}
	
	
	public void setPassGroup(final int passGroup)
	{
		
		this.passGroup = passGroup;
	}

	public EKickerDevice getKickerDevice() {
		return kickerDevice;
	}

	public void setKickerDevice(final EKickerDevice kickerDevice) {
		this.kickerDevice = kickerDevice;
	}
	
	@Override
	public String toString()
	{
		return getCommandType() + " [" + getPassGroup() + "]";
	}
}
