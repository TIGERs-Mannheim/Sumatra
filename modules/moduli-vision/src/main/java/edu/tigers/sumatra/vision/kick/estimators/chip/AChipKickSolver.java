/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators.chip;

import java.util.Map;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.kick.estimators.IKickSolver;


/**
 * @author AndreR <andre@ryll.cc>
 */
public abstract class AChipKickSolver implements IKickSolver
{
	protected IVector2 kickPosition;
	protected long kickTimestamp;
	private final Map<Integer, CamCalibration> camCalib;
	
	
	/**
	 * @param kickPosition
	 * @param kickTimestamp
	 * @param camCalib
	 */
	public AChipKickSolver(final IVector2 kickPosition, final long kickTimestamp,
			final Map<Integer, CamCalibration> camCalib)
	{
		this.kickPosition = kickPosition;
		this.kickTimestamp = kickTimestamp;
		this.camCalib = camCalib;
	}
	
	
	protected IVector3 getCameraPosition(final int camId)
	{
		if (camCalib.containsKey(camId))
		{
			return camCalib.get(camId).getCameraPosition();
		}
		
		// return an arbitrary value => fitting will fail with bad values
		return Vector3.fromXYZ(0, 0, 2000.0);
	}
	
	
	/**
	 * @return the kickPosition
	 */
	public IVector2 getKickPosition()
	{
		return kickPosition;
	}
	
	
	/**
	 * @return the kickTimestamp
	 */
	public long getKickTimestamp()
	{
		return kickTimestamp;
	}
}
