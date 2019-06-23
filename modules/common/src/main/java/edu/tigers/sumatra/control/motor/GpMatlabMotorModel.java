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

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;
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
			return new VectorN((double[]) result[0]);
		} catch (MatlabInvocationException | MatlabConnectionException err)
		{
			log.error("Error calling Matlab.", err);
		}
		return new VectorN(4);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("mm.getXywSpeed([");
			sb.append(wheelSpeed.x());
			sb.append(',');
			sb.append(wheelSpeed.y());
			sb.append(',');
			sb.append(wheelSpeed.z());
			sb.append(',');
			sb.append(wheelSpeed.w());
			sb.append("])");
			Object[] result = MatlabConnection.getMatlabProxy().returningEval(sb.toString(), 1);
			return new Vector3((double[]) result[0]);
		} catch (MatlabInvocationException | MatlabConnectionException err)
		{
			log.error("Error calling Matlab.", err);
		}
		return new Vector3();
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.GP_MATLAB;
	}
}
