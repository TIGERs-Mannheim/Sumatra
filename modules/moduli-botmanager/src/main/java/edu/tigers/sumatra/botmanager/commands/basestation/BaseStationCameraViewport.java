/*
 * *********************************************************
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.04.2017
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.rectangle.IRectangle;


/**
 * Viewport configuration of a single camera.
 * 
 * @author AndreR
 */
public class BaseStationCameraViewport extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private int	camId	= 0;
	@SerialData(type = ESerialDataType.INT16)
	private int	minX	= 0;
	@SerialData(type = ESerialDataType.INT16)
	private int	minY	= 0;
	@SerialData(type = ESerialDataType.INT16)
	private int	maxX	= 0;
	@SerialData(type = ESerialDataType.INT16)
	private int	maxY	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 */
	public BaseStationCameraViewport()
	{
		super(ECommand.CMD_BASE_CAM_VIEWPORT);
	}
	
	
	/**
	 * @param camId
	 * @param viewport
	 */
	public BaseStationCameraViewport(final int camId, final IRectangle viewport)
	{
		super(ECommand.CMD_BASE_CAM_VIEWPORT);
		
		this.camId = camId;
		minX = (int) viewport.minX();
		minY = (int) viewport.minY();
		maxX = (int) viewport.maxX();
		maxY = (int) viewport.maxY();
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the camId
	 */
	public int getCamId()
	{
		return camId;
	}
	
	
	/**
	 * @param camId the camId to set
	 */
	public void setCamId(final int camId)
	{
		this.camId = camId;
	}
	
	
	/**
	 * @return the minX
	 */
	public int getMinX()
	{
		return minX;
	}
	
	
	/**
	 * @param minX the minX to set
	 */
	public void setMinX(final int minX)
	{
		this.minX = minX;
	}
	
	
	/**
	 * @return the minY
	 */
	public int getMinY()
	{
		return minY;
	}
	
	
	/**
	 * @param minY the minY to set
	 */
	public void setMinY(final int minY)
	{
		this.minY = minY;
	}
	
	
	/**
	 * @return the maxX
	 */
	public int getMaxX()
	{
		return maxX;
	}
	
	
	/**
	 * @param maxX the maxX to set
	 */
	public void setMaxX(final int maxX)
	{
		this.maxX = maxX;
	}
	
	
	/**
	 * @return the maxY
	 */
	public int getMaxY()
	{
		return maxY;
	}
	
	
	/**
	 * @param maxY the maxY to set
	 */
	public void setMaxY(final int maxY)
	{
		this.maxY = maxY;
	}
}
