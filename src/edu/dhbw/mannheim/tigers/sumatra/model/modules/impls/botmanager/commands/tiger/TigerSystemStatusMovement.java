/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


public class TigerSystemStatusMovement extends ACommand
{
	/** [mm/s] */
	private int	vx;
	/** [mm/s] */
	private int	vy;
	/** [mm/s] */
	private int	vw;
	
	
	public TigerSystemStatusMovement()
	{
	}
	

	public TigerSystemStatusMovement(float vx, float vy, float vw)
	{
		this();
		
		setVx(vx);
		setVy(vy);
		setVw(vw);
	}
	

	@Override
	public void setData(byte[] data)
	{
		vx = byteArray2Int(data, 0);
		vy = byteArray2Int(data, 4);
		vw = byteArray2Int(data, 8);
	}
	

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, vx);
		int2ByteArray(data, 4, vy);
		int2ByteArray(data, 8, vw);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_STATUS_MOVEMENT;
	}
	
	@Override
	public int getDataLength()
	{
		return 12;
	}

	

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
		return (float) (vw / 1000f);
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
