/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


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
	
	
	/** */
	public TigerSystemPerformance()
	{
		super(ECommand.CMD_SYSTEM_PERFORMANCE);
	}
	
	
	/**
	 * @return the accMax
	 */
	public float getAccMax()
	{
		return accMax * 0.001f;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public void setAccMax(final float accMax)
	{
		this.accMax = (int) (accMax * 1000.0f);
	}
	
	
	/**
	 * @return the accMaxW
	 */
	public float getAccMaxW()
	{
		return accMaxW * 0.01f;
	}
	
	
	/**
	 * @param accMaxW the accMaxW to set
	 */
	public void setAccMaxW(final float accMaxW)
	{
		this.accMaxW = (int) (accMaxW * 100.0f);
	}
	
	
	/**
	 * @return the brkMax
	 */
	public float getBrkMax()
	{
		return brkMax * 0.001f;
	}
	
	
	/**
	 * @param brkMax the brkMax to set
	 */
	public void setBrkMax(final float brkMax)
	{
		this.brkMax = (int) (brkMax * 1000.0f);
	}
	
	
	/**
	 * @return the brkMaxW
	 */
	public float getBrkMaxW()
	{
		return brkMaxW * 0.01f;
	}
	
	
	/**
	 * @param brkMaxW the brkMaxW to set
	 */
	public void setBrkMaxW(final float brkMaxW)
	{
		this.brkMaxW = (int) (brkMaxW * 100.0f);
	}
	
	
	/**
	 * @return the velMax
	 */
	public float getVelMax()
	{
		return velMax * 0.001f;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public void setVelMax(final float velMax)
	{
		this.velMax = (int) (velMax * 1000.0f);
	}
	
	
	/**
	 * @return the velMaxW
	 */
	public float getVelMaxW()
	{
		return velMaxW * 0.01f;
	}
	
	
	/**
	 * @param velMaxW the velMaxW to set
	 */
	public void setVelMaxW(final float velMaxW)
	{
		this.velMaxW = (int) (velMaxW * 100.0f);
	}
}
