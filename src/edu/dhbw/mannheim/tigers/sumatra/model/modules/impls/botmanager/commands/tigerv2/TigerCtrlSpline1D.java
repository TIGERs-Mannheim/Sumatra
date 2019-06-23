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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	private float				a;
	private float				b;
	private float				c;
	private float				d;
	/** End time */
	private float				tEnd;
	/** Options */
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
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		a = byteArray2HalfFloat(data, 0);
		b = byteArray2HalfFloat(data, 2);
		c = byteArray2HalfFloat(data, 4);
		d = byteArray2HalfFloat(data, 6);
		
		tEnd = byteArray2HalfFloat(data, 8);
		
		option = byteArray2UByte(data, 10);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		halfFloat2ByteArray(data, 0, a);
		halfFloat2ByteArray(data, 2, b);
		halfFloat2ByteArray(data, 4, c);
		halfFloat2ByteArray(data, 6, d);
		
		halfFloat2ByteArray(data, 8, tEnd);
		
		byte2ByteArray(data, 10, option);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_SPLINE_1D;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 11;
	}
	
	
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
		a = spline.getA();
		b = spline.getB();
		c = spline.getC();
		d = spline.getD();
		
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
