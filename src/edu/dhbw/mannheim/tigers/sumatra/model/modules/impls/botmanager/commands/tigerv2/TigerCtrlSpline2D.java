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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Transfers 2 hermite splines with same end time in compressed 16bit float form.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlSpline2D extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** Spline coefficients */
	private static class Spline2DParams
	{
		/** */
		@SerialData(type = ESerialDataType.FLOAT16)
		public float[]	a	= new float[HermiteSpline.SPLINE_SIZE];
	}
	
	@SerialData(type = ESerialDataType.EMBEDDED)
	private Spline2DParams[]	params					= new Spline2DParams[2];
	
	/** End time */
	@SerialData(type = ESerialDataType.FLOAT16)
	private float					tEnd;
	/** Options */
	@SerialData(type = ESerialDataType.UINT8)
	private int						option;
	
	/** Replace spline list with this spline */
	public static final int		OPTION_CLEAR			= 0x02;
	/** Append spline to current list */
	public static final int		OPTION_APPEND			= 0x01;
	
	/** Maximum spline list size on TigerBotV2 */
	public static final int		MAX_SPLINE_CAPACITY	= 10;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlSpline2D()
	{
		super(ECommand.CMD_CTRL_SPLINE_2D);
		
		params[0] = new Spline2DParams();
		params[1] = new Spline2DParams();
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
	public void setSpline(HermiteSpline2D spline)
	{
		params[0].a = Arrays.copyOf(spline.getXSpline().getA(), spline.getXSpline().getA().length);
		params[1].a = Arrays.copyOf(spline.getYSpline().getA(), spline.getYSpline().getA().length);
		
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
