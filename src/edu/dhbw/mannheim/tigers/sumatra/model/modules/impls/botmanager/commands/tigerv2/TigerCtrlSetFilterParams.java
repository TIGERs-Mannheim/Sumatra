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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Set kalman filter parameters.
 * 
 * @author AndreR
 * 
 */
public class TigerCtrlSetFilterParams extends ACommand
{
	/** */
	public static enum ParamType
	{
		/** */
		UNKNOWN(0x00),
		/** */
		EX_POS(0x01),
		/** */
		EX_VEL(0x02),
		/** */
		EX_ACC(0x03),
		/** */
		EZ_VISION(0x04),
		/** */
		EZ_ENCODER(0x05),
		/** */
		EZ_ACC_GYRO(0x06),
		/** */
		EZ_MOTOR(0x07);
		
		private final int	val;
		
		
		private ParamType(int value)
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
		public static ParamType getParamTypeConstant(int type)
		{
			for (ParamType t : values())
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
	private float[]	params	= new float[3];
	private ParamType	type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlSetFilterParams()
	{
		type = ParamType.UNKNOWN;
	}
	
	
	/**
	 * @param type
	 * @param params
	 */
	public TigerCtrlSetFilterParams(ParamType type, float params[])
	{
		this.type = type;
		
		if (params.length < 3)
		{
			return;
		}
		
		this.params[0] = params[0];
		this.params[1] = params[1];
		this.params[2] = params[2];
	}
	
	
	/**
	 * @param type
	 * @param params
	 */
	public TigerCtrlSetFilterParams(ParamType type, Vector3 params)
	{
		this.type = type;
		
		this.params[0] = params.x();
		this.params[1] = params.y();
		this.params[2] = params.z();
	}
	
	
	/**
	 * @param type
	 * @param params
	 */
	public TigerCtrlSetFilterParams(ParamType type, Vector2 params)
	{
		this.type = type;
		
		this.params[0] = params.x();
		this.params[1] = params.y();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		params[0] = byteArray2Float(data, 0);
		params[1] = byteArray2Float(data, 4);
		params[2] = byteArray2Float(data, 8);
		
		type = ParamType.getParamTypeConstant(byteArray2UByte(data, 12));
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		float2ByteArray(data, 0, params[0]);
		float2ByteArray(data, 4, params[1]);
		float2ByteArray(data, 8, params[2]);
		byte2ByteArray(data, 12, type.getValue());
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CTRL_SET_FILTER_PARAMS;
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
	 * @return the params
	 */
	public float[] getParams()
	{
		return params.clone();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Vector3 getParamsVector3()
	{
		return new Vector3(params[0], params[1], params[2]);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Vector2 getParamsVector2()
	{
		return new Vector2(params[0], params[1]);
	}
	
	
	/**
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public void setParams(float p1, float p2, float p3)
	{
		params[0] = p1;
		params[1] = p2;
		params[2] = p3;
	}
	
	
	/**
	 * @return the type
	 */
	public ParamType getType()
	{
		return type;
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setType(ParamType type)
	{
		this.type = type;
	}
}
