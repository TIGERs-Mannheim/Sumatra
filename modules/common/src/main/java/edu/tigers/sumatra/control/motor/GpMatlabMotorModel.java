/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 25, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;
import edu.tigers.sumatra.matlab.MatlabConnection;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GpMatlabMotorModel extends AMotorModel
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(GpMatlabMotorModel.class.getName());
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public GpMatlabMotorModel()
	{
		try
		{
			MatlabConnection.getMatlabProxy().eval("if ~exist('mm','var'), mm = loadGpModel; end");
		} catch (MatlabInvocationException | MatlabConnectionException err)
		{
			log.error("Error calling Matlab.", err);
		}
	}
	
	
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 targetVel)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("mm.getWheelSpeed([");
			sb.append(targetVel.x());
			sb.append(',');
			sb.append(targetVel.y());
			sb.append(',');
			sb.append(targetVel.z());
			sb.append("])");
			Object[] result = MatlabConnection.getMatlabProxy().returningEval(sb.toString(), 1);
			return VectorN.from((double[]) result[0]);
		} catch (MatlabInvocationException | MatlabConnectionException err)
		{
			log.error("Error calling Matlab.", err);
		}
		return VectorN.zero(4);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("mm.getXywSpeed([");
			sb.append(wheelSpeed.get(0));
			sb.append(',');
			sb.append(wheelSpeed.get(1));
			sb.append(',');
			sb.append(wheelSpeed.get(2));
			sb.append(',');
			sb.append(wheelSpeed.get(3));
			sb.append("])");
			Object[] result = MatlabConnection.getMatlabProxy().returningEval(sb.toString(), 1);
			return Vector3.fromArray((double[]) result[0]);
		} catch (MatlabInvocationException | MatlabConnectionException err)
		{
			log.error("Error calling Matlab.", err);
		}
		return Vector3.zero();
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.GP_MATLAB;
	}
}
