/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

public class CTSetSpeed extends ACommand
{
	private final Logger log = Logger.getLogger(getClass());

	private int left;	// [mm/s]
	private int right;
	private Vector2 dir;
	private float angularVelocity;	// [deg/s]
	
	private boolean dirUsed = false;
	private boolean avUsed = false;
	
	public CTSetSpeed()
	{
	}
	
	public CTSetSpeed(Vector2 dir)
	{
		this.dir = dir;
		dirUsed = true;
	}
	
	public CTSetSpeed(float angularVelocity)
	{
		this.angularVelocity = angularVelocity;
		avUsed = true;
	}
	
	public CTSetSpeed(Vector2 dir, float angularVelocity)
	{
		this.dir = dir;
		this.angularVelocity = angularVelocity;
		dirUsed = true;
		avUsed = true;
	}
	
	public void setData(byte[] data)
	{
		left = byteArray2Short(data, 0);
		right = byteArray2Short(data, 2);
	}
	
	public byte[] getData()
	{
		calcMove();
		
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, left);
		short2ByteArray(data, 2, right);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CT_SET_SPEED;
	}
	
	@Override
	public int getDataLength()
	{
		return 4;
	}

	
	/**
	 * 
	 * @param left Wheelspeed in [m/s]
	 * @param right Wheelspeed in [m/s]
	 */
	public void setSpeed(float left, float right)
	{
		this.left = (int)(left*1000);
		this.right = (int)(right*1000);
	}
	
	/**
	 * @return left wheel speed in [m/s]
	 */
	public float getLeft()
	{
		return this.left / 1000f;
	}
	
	/**
	 * @return right wheel speed in [m/s]
	 */
	public float getRight()
	{
		return this.right / 1000f;
	}
	
	public Vector2 getDir()
	{
		return dir;
	}

	public float getAngularVelocity()
	{
		return angularVelocity;
	}

	public boolean isDirUsed()
	{
		return dirUsed;
	}

	public boolean isAvUsed()
	{
		return avUsed;
	}
	
	public void setUnusedComponents(Vector2 dir, float aV)
	{
		if(!dirUsed)
		{
			this.dir = dir;
		}
		
		if(!avUsed)
		{
			this.angularVelocity = aV;
		}
		
		dirUsed = true;
		avUsed = true;
	}
	
	private void calcMove()
	{
		if (dir.x != 0)
		{
			log.warn("Sorry, I am too stupid to make non-straight moves :(");
		}
		
		/*
		 * Angular Velocity calculations
		 * aV is clock-wise (top-view)
		 * Transforming aV to distance on bot circumference
		 * bC = bot circumference
		 * Full bot circumference: bC = botDiameter * Pi
		 * bot Turns = aV / 2Pi
		 * rightWay = aV * bC = bD * aV/2
		 * leftWay = -(aV * bC) = -(bD * aV/2)
		 */

		// x m/U = 0.056m *Pi
		// y m/T = x / 60 (1U = 60T)
		// Target-Ticks = s / y
		
		// float wheelDiameter = 0.056f;
		float botDiameter = 0.15f;
		
		float leftWheel = dir.y;
		float rightWheel = leftWheel;
		
		float turnInfluence = botDiameter * (angularVelocity / 2);
		
		leftWheel += turnInfluence;
		rightWheel -= turnInfluence;
		
		setSpeed(leftWheel, rightWheel);
	}
}
