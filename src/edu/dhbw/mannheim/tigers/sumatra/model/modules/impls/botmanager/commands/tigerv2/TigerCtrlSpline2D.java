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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	private float[]			a							= new float[2];
	private float[]			b							= new float[2];
	private float[]			c							= new float[2];
	private float[]			d							= new float[2];
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
		int offset = 0;
		
		for (int i = 0; i < 2; i++)
		{
			a[i] = byteArray2HalfFloat(data, offset);
			offset += 2;
			b[i] = byteArray2HalfFloat(data, offset);
			offset += 2;
			c[i] = byteArray2HalfFloat(data, offset);
			offset += 2;
			d[i] = byteArray2HalfFloat(data, offset);
			offset += 2;
		}
		
		tEnd = byteArray2HalfFloat(data, offset);
		offset += 2;
		
		option = byteArray2UByte(data, offset);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int offset = 0;
		
		for (int i = 0; i < 2; i++)
		{
			halfFloat2ByteArray(data, offset, a[i]);
			offset += 2;
			halfFloat2ByteArray(data, offset, b[i]);
			offset += 2;
			halfFloat2ByteArray(data, offset, c[i]);
			offset += 2;
			halfFloat2ByteArray(data, offset, d[i]);
			offset += 2;
		}
		
		halfFloat2ByteArray(data, offset, tEnd);
		offset += 2;
		
		byte2ByteArray(data, offset, option);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_SPLINE_2D;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 19;
	}
	
	
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
		a[0] = spline.getXSpline().getA();
		b[0] = spline.getXSpline().getB();
		c[0] = spline.getXSpline().getC();
		d[0] = spline.getXSpline().getD();
		a[1] = spline.getYSpline().getA();
		b[1] = spline.getYSpline().getB();
		c[1] = spline.getYSpline().getC();
		d[1] = spline.getYSpline().getD();
		
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
