/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Set PID parameters for trajectory following.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlSetPIDParams extends ACommand
{
	/** */
	public static enum PIDParamType
	{
		/** */
		UNKNOWN(0x00),
		/** */
		POS_X(0x01),
		/** */
		POS_Y(0x02),
		/** */
		POS_W(0x03),
		/** */
		VEL_X(0x04),
		/** */
		VEL_Y(0x05),
		/** */
		VEL_W(0x06),
		/** */
		ACC_X(0x07),
		/** */
		ACC_Y(0x08),
		/** */
		ACC_W(0x09),
		/** */
		DRIBBLER(0x0A);
		
		private final int	val;
		
		
		private PIDParamType(int value)
		{
			val = value;
		}
		
		
		/**
		 * 
		 * @return
		 */
		public int getValue()
		{
			return val;
		}
		
		
		/**
		 * Convert a type to an enum.
		 * 
		 * @param type param type value
		 * @return enum
		 */
		public static PIDParamType getPIDParamTypeConstant(int type)
		{
			for (PIDParamType t : values())
			{
				if (t.getValue() == type)
				{
					return t;
				}
			}
			
			return UNKNOWN;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private PIDParameters	params;
	private PIDParamType		type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlSetPIDParams()
	{
		type = PIDParamType.UNKNOWN;
	}
	
	
	/**
	 * @param type
	 * @param params
	 */
	public TigerCtrlSetPIDParams(PIDParamType type, PIDParameters params)
	{
		this.type = type;
		setParams(params);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		params.setKp(byteArray2Float(data, 0));
		params.setKi(byteArray2Float(data, 4));
		params.setKd(byteArray2Float(data, 8));
		type = PIDParamType.getPIDParamTypeConstant(byteArray2UByte(data, 12));
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		float2ByteArray(data, 0, params.getKp());
		float2ByteArray(data, 4, params.getKi());
		float2ByteArray(data, 8, params.getKd());
		byte2ByteArray(data, 12, type.getValue());
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_SET_PID_PARAMS;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 13;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public PIDParamType getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(PIDParamType type)
	{
		this.type = type;
	}
	
	
	/**
	 * @param params
	 */
	public void setParams(PIDParameters params)
	{
		this.params = params;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public PIDParameters getParams()
	{
		return params;
	}
	
}
