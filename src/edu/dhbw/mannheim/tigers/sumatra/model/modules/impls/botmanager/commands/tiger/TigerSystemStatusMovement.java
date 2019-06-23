/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 */
public class TigerSystemStatusMovement extends ACommand
{
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT32)
	private int	vx;
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT32)
	private int	vy;
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT32)
	private int	vw;
	
	
	/**
	 * 
	 */
	public TigerSystemStatusMovement()
	{
		super(ECommand.CMD_SYSTEM_STATUS_MOVEMENT);
	}
	
	
	/**
	 * 
	 * @param vx
	 * @param vy
	 * @param vw
	 */
	public TigerSystemStatusMovement(float vx, float vy, float vw)
	{
		this();
		
		setVx(vx);
		setVy(vy);
		setVw(vw);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Vector2 getVelocity()
	{
		return new Vector2(vx / 1000.0f, vy / 1000.0f);
	}
	
	
	/**
	 * Get angular velocity.
	 * 
	 * @return aV in [rad/s]
	 */
	public float getAngularVelocity()
	{
		return vw / 1000f;
	}
	
	
	/**
	 * @param vx [m/s]
	 */
	public void setVx(float vx)
	{
		this.vx = (int) (vx * 1000);
	}
	
	
	/**
	 * @param vy [m/s]
	 */
	public void setVy(float vy)
	{
		this.vy = (int) (vy * 1000);
	}
	
	
	/**
	 * @param vw [rad/s]
	 */
	public void setVw(float vw)
	{
		this.vw = (int) (vw * 1000);
	}
}
