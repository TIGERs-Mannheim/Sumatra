/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Tiger move command.
 * 
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
	private int			x;
	/** y component, [mm/s], int16_t */
	private int			y;
	/** non-compensated angular velocity */
	private int			w;
	/** compensated angular velocity */
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
	}
	
	
	/**
	 * Set bot speed and rotation.
	 * 
	 * @param vec Vector [m/s]
	 * @param turnVelocity Rotation [rad/s]
	 */
	public TigerMotorMoveV2(IVector2 vec, float turnVelocity)
	{
		setX(vec.x());
		setY(vec.y());
		setW(turnVelocity);
	}
	
	
	/**
	 * 
	 * @param vec
	 * @param turnVelocity
	 * @param compensatedVelocity
	 */
	public TigerMotorMoveV2(IVector2 vec, float turnVelocity, float compensatedVelocity)
	{
		setX(vec.x());
		setY(vec.y());
		setW(turnVelocity);
		setV(compensatedVelocity);
	}
	
	
	/**
	 * 
	 * @param vec
	 */
	public TigerMotorMoveV2(IVector2 vec)
	{
		setX(vec.x());
		setY(vec.y());
	}
	
	
	/**
	 * 
	 * @param turnVelocity
	 */
	public TigerMotorMoveV2(float turnVelocity)
	{
		setW(turnVelocity);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		x = byteArray2Short(data, 0);
		y = byteArray2Short(data, 2);
		w = byteArray2Short(data, 4);
		v = byteArray2Short(data, 6);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, x);
		short2ByteArray(data, 2, y);
		short2ByteArray(data, 4, w);
		short2ByteArray(data, 6, v);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_MOVE_V2;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 8;
	}
	
	
	/**
	 * Set XY vector [m/s]
	 * 
	 * @param v vector
	 */
	public void setXY(Vector2 v)
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
	public void setX(float x)
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
	public void setY(float y)
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
	public void setW(float w)
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
	public void setV(float v)
	{
		this.v = (int) (v * 1000);
		
		vUsed = true;
	}
	
	
	/**
	 * 
	 * @param vec
	 * @param vw
	 * @param vv
	 */
	public void setUnusedComponents(Vector2 vec, float vw, float vv)
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
}
