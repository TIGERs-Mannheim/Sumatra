package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerDataAcqVelocity extends ACommand
{
	/** [us] */
	@SerialData(type = SerialData.ESerialDataType.UINT32)
	private long	timestamp	= 0;
	
	/** [mm/s] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int[]	setVel		= new int[3];
	
	/** [mm/s^2] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int[]	setAcc		= new int[3];
	
	/** [mm/s] */
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int[]	outVel		= new int[3];
	
	
	@SuppressWarnings("unused")
	private TigerDataAcqVelocity()
	{
		super(ECommand.CMD_DATA_ACQ_VELOCITY);
	}
	
	
	/**
	 * @param timestamp [ns]
	 */
	public TigerDataAcqVelocity(final long timestamp)
	{
		this();
		this.timestamp = timestamp / 1000L;
	}
	
	
	/**
	 * @param timestamp
	 */
	public void setTimestamp(final long timestamp)
	{
		this.timestamp = timestamp / 1000L;
	}
	
	
	/**
	 * @param setVel
	 */
	public void setSetVel(final IVector3 setVel)
	{
		for (int i = 0; i < 3; i++)
		{
			this.setVel[i] = (int) (setVel.get(i) * 1000.0);
		}
	}
	
	
	/**
	 * @param setAcc
	 */
	public void setSetAcc(final IVector3 setAcc)
	{
		for (int i = 0; i < 3; i++)
		{
			this.setAcc[i] = (int) (setAcc.get(i) * 1000.0);
		}
	}
	
	
	/**
	 * @param outVel
	 */
	public void setOutVel(final IVector3 outVel)
	{
		for (int i = 0; i < 3; i++)
		{
			this.outVel[i] = (int) (outVel.get(i) * 1000.0);
		}
	}
	
	
	/**
	 * @return timestamp in [ns]
	 */
	public long getTimestamp()
	{
		return timestamp * 1000L;
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getSetVel()
	{
		return Vector3.fromXYZ(setVel[0] / 1000.0, setVel[1] / 1000.0, setVel[2] / 1000.0);
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getSetAcc()
	{
		return Vector3.fromXYZ(setAcc[0] / 1000.0, setAcc[1] / 1000.0, setAcc[2] / 1000.0);
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getOutVel()
	{
		return Vector3.fromXYZ(outVel[0] / 1000.0, outVel[1] / 1000.0, outVel[2] / 1000.0);
	}
}
