/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Tiger move command.
 * - velocities in [mm/s]
 * 
 * @author AndreR
 */
public class TigerMotorMoveV2 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** x component, [mm/s], int16_t */
	@SerialData(type = ESerialDataType.INT16)
	private int			x;
	/** y component, [mm/s], int16_t */
	@SerialData(type = ESerialDataType.INT16)
	private int			y;
	/** non-compensated angular velocity */
	@SerialData(type = ESerialDataType.INT16)
	private int			w;
	/** compensated angular velocity */
	@SerialData(type = ESerialDataType.INT16)
	private int			v;
	
	private boolean	dirUsed	= false;
	private boolean	wUsed		= false;
	private boolean	vUsed		= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorMoveV2()
	{
		super(ECommand.CMD_MOTOR_MOVE_V2);
	}
	
	
	/**
	 * Set bot speed and rotation.
	 * 
	 * @param vec Vector [m/s]
	 * @param turnVelocity Rotation [rad/s]
	 */
	public TigerMotorMoveV2(final IVector2 vec, final float turnVelocity)
	{
		super(ECommand.CMD_MOTOR_MOVE_V2);
		
		setX(vec.x());
		setY(vec.y());
		setW(turnVelocity);
	}
	
	
	/**
	 * @param vec
	 * @param turnVelocity
	 * @param compensatedVelocity
	 */
	public TigerMotorMoveV2(final IVector2 vec, final float turnVelocity, final float compensatedVelocity)
	{
		super(ECommand.CMD_MOTOR_MOVE_V2);
		
		setX(vec.x());
		setY(vec.y());
		setW(turnVelocity);
		setV(compensatedVelocity);
	}
	
	
	/**
	 * @param vec
	 */
	public TigerMotorMoveV2(final IVector2 vec)
	{
		super(ECommand.CMD_MOTOR_MOVE_V2);
		
		setX(vec.x());
		setY(vec.y());
	}
	
	
	/**
	 * @param turnVelocity
	 */
	public TigerMotorMoveV2(final float turnVelocity)
	{
		super(ECommand.CMD_MOTOR_MOVE_V2);
		
		setW(turnVelocity);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set XY vector [m/s]
	 * 
	 * @param v vector
	 */
	public void setXY(final Vector2 v)
	{
		setX(v.x);
		setY(v.y);
	}
	
	
	/**
	 * Get XY vector [m/s]
	 * 
	 * @return vector
	 */
	public Vector2 getXY()
	{
		final Vector2 vec = new Vector2();
		
		vec.x = getX();
		vec.y = getY();
		
		return vec;
	}
	
	
	/**
	 * Get X component [m/s]
	 * 
	 * @return x velocity
	 */
	public float getX()
	{
		return x / 1000f;
	}
	
	
	/**
	 * Set X component [m/s]
	 * 
	 * @param x velocity
	 */
	public void setX(final float x)
	{
		this.x = (int) (x * 1000);
		
		dirUsed = true;
	}
	
	
	/**
	 * Get Y component [m/s]
	 * 
	 * @return y velocity
	 */
	public float getY()
	{
		return y / 1000f;
	}
	
	
	/**
	 * Set Y component [m/s]
	 * 
	 * @param y velocity
	 */
	public void setY(final float y)
	{
		this.y = (int) (y * 1000);
		
		dirUsed = true;
	}
	
	
	/**
	 * Get turn speed (angular velocity) [rad/s]
	 * 
	 * @return turn speed
	 */
	public float getW()
	{
		return (w / 1000f);
	}
	
	
	/**
	 * Set turn speed (angular velocity) [rad/s]
	 * 
	 * @param w turn speed
	 */
	public void setW(final float w)
	{
		this.w = (int) (w * 1000);
		
		wUsed = true;
	}
	
	
	/**
	 * Get compensated turn speed (angular velocity) [rad/s]
	 * 
	 * @return turn speed
	 */
	public float getV()
	{
		return (v / 1000f);
	}
	
	
	/**
	 * Set compensated turn speed (angular velocity) [rad/s]
	 * 
	 * @param v turn speed
	 */
	public void setV(final float v)
	{
		this.v = (int) (v * 1000);
		
		vUsed = true;
	}
	
	
	/**
	 * @param vec
	 * @param vw
	 * @param vv
	 */
	public void setUnusedComponents(final Vector2 vec, final float vw, final float vv)
	{
		if (!dirUsed)
		{
			setXY(vec);
		}
		
		if (!wUsed)
		{
			setW(vw);
		}
		
		if (!vUsed)
		{
			setV(vv);
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isAllFilled()
	{
		return dirUsed && wUsed;
	}
}
