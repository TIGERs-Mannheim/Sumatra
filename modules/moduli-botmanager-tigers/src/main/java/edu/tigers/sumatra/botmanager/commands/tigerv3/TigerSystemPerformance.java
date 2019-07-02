/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Information about the robots performance values.
 * Maximum acceleration, deceleration and velocity. Can be used for path planning.
 * 
 * @note This class employs reasonable defaults so that path planning never needs to check the values for consistency.
 * @author AndreR
 */
public class TigerSystemPerformance extends ACommand
{
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	accMax	= 2000;
	
	/** [crad/s^2] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	accMaxW	= 3000;
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	brkMax	= 5000;
	
	/** [crad/s^2] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	brkMaxW	= 6000;
	
	/** [mm/s] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	velMax	= 1800;
	
	/** [crad/s] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	velMaxW	= 3000;
	
	
	/** Constructor. */
	public TigerSystemPerformance()
	{
		super(ECommand.CMD_SYSTEM_PERFORMANCE);
	}
	
	
	/**
	 * @return the accMax
	 */
	public double getAccMax()
	{
		return accMax * 0.001;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public void setAccMax(final double accMax)
	{
		this.accMax = (int) (accMax * 1000.0);
	}
	
	
	/**
	 * @return the accMaxW
	 */
	public double getAccMaxW()
	{
		return accMaxW * 0.01;
	}
	
	
	/**
	 * @param accMaxW the accMaxW to set
	 */
	public void setAccMaxW(final double accMaxW)
	{
		this.accMaxW = (int) (accMaxW * 100.0);
	}
	
	
	/**
	 * @return the brkMax
	 */
	public double getBrkMax()
	{
		return brkMax * 0.001;
	}
	
	
	/**
	 * @param brkMax the brkMax to set
	 */
	public void setBrkMax(final double brkMax)
	{
		this.brkMax = (int) (brkMax * 1000.0);
	}
	
	
	/**
	 * @return the brkMaxW
	 */
	public double getBrkMaxW()
	{
		return brkMaxW * 0.01;
	}
	
	
	/**
	 * @param brkMaxW the brkMaxW to set
	 */
	public void setBrkMaxW(final double brkMaxW)
	{
		this.brkMaxW = (int) (brkMaxW * 100.0);
	}
	
	
	/**
	 * @return the velMax
	 */
	public double getVelMax()
	{
		return velMax * 0.001;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public void setVelMax(final double velMax)
	{
		this.velMax = (int) (velMax * 1000.0);
	}
	
	
	/**
	 * @return the velMaxW
	 */
	public double getVelMaxW()
	{
		return velMaxW * 0.01;
	}
	
	
	/**
	 * @param velMaxW the velMaxW to set
	 */
	public void setVelMaxW(final double velMaxW)
	{
		this.velMaxW = (int) (velMaxW * 100.0);
	}
}
