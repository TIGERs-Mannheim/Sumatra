/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerSystemMatchPosVel extends ACommand
{
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	destination[]	= new int[3];
	
	/** local [m/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int	vel[]				= new int[3];
	
	
	/**
	  * 
	  */
	@SuppressWarnings("unused")
	private TigerSystemMatchPosVel()
	{
		this(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR);
	}
	
	
	/**
	 * @param destination [mm,mm,rad]
	 * @param vel [m/s,m/s,rad/s]
	 */
	public TigerSystemMatchPosVel(final IVector3 destination, final IVector3 vel)
	{
		super(ECommand.CMD_SYSTEM_MATCH_POS_VEL);
		
		this.destination[0] = (int) destination.x();
		this.destination[1] = (int) destination.y();
		this.destination[2] = (int) (1000.0f * destination.z());
		
		this.vel[0] = (int) (vel.x() * 1000.0f);
		this.vel[1] = (int) (vel.y() * 1000.0f);
		this.vel[2] = (int) (vel.z() * 1000.0f);
	}
	
	
	/**
	 * @return the destination [mm]
	 */
	public final IVector2 getDestination()
	{
		return new Vector2(destination[0], destination[1]);
	}
	
	
	/**
	 * @return orientation [rad]
	 */
	public final float getOrientation()
	{
		return destination[2] / 1000.0f;
	}
	
	
	/**
	 * @return [m/s]
	 */
	public final IVector3 getVelocity()
	{
		return new Vector3(vel[0], vel[1], vel[2]).multiplyNew(0.001f);
	}
}
