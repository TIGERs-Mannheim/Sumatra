/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.util.Arrays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Transfers one hermite spline in compressed 16bit float form.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlSpline1D extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** Spline coefficients */
	@SerialData(type = ESerialDataType.FLOAT16)
	private float				a[]						= new float[HermiteSpline.SPLINE_SIZE];
	/** End time */
	@SerialData(type = ESerialDataType.FLOAT16)
	private float				tEnd;
	/** Options */
	@SerialData(type = ESerialDataType.UINT8)
	private int					option;
	
	/** Replace spline list with this spline */
	public static final int	OPTION_CLEAR			= 0x02;
	/** Append spline to current list */
	public static final int	OPTION_APPEND			= 0x01;
	
	/** Maximum spline list size on TigerBotV2 */
	public static final int	MAX_SPLINE_CAPACITY	= 10;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlSpline1D()
	{
		super(ECommand.CMD_CTRL_SPLINE_1D);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set the spline.
	 * 
	 * @param spline
	 */
	public void setSpline(HermiteSpline spline)
	{
		a = Arrays.copyOf(spline.getA(), spline.getA().length);
		
		tEnd = spline.getEndTime();
	}
	
	
	/**
	 * Set option.
	 * 
	 * @param option
	 */
	public void setOption(int option)
	{
		this.option = option;
	}
}
