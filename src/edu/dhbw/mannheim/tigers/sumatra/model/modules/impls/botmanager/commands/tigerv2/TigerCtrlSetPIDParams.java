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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


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
		MOTOR(0x07),
		/**  */
		DRIBBLER(0x0A),
		/** */
		SPLINE_X(0x0B),
		/** */
		SPLINE_Y(0x0C),
		/** */
		SPLINE_W(0x0D), ;
		
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
	@SerialData(type = ESerialDataType.EMBEDDED)
	private PIDParameters	params	= new PIDParameters();
	@SerialData(type = ESerialDataType.UINT8)
	private int					type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerCtrlSetPIDParams()
	{
		super(ECommand.CMD_CTRL_SET_PID_PARAMS);
		
		type = PIDParamType.UNKNOWN.getValue();
	}
	
	
	/**
	 * @param type
	 * @param params
	 */
	public TigerCtrlSetPIDParams(PIDParamType type, PIDParameters params)
	{
		super(ECommand.CMD_CTRL_SET_PID_PARAMS);
		
		this.type = type.getValue();
		setParams(params);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the type
	 */
	public PIDParamType getParamType()
	{
		return PIDParamType.getPIDParamTypeConstant(type);
	}
	
	
	/**
	 * @param type the type to set
	 */
	public void setParamType(PIDParamType type)
	{
		this.type = type.getValue();
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
