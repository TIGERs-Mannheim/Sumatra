/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CombinationMotorModel extends AMotorModel
{
	
	private final double[]		in_pX				= new double[] { -25.846401f, -27.182764f, 36.287792f, 37.378927f };
	private final double[]		in_pY				= new double[] { 36.969432f, -38.955882f, -30.111243f, 31.881516f };
	private final double[]		in_nY				= new double[] { -35.170218f, 40.249140f, 27.810212f, -33.769436f };
	private final double[]		in_pXpY			= new double[] { 6.732757f, -43.085337f, 4.298287f, 44.974591f };
	private final double[]		in_pXnY			= new double[] { -42.464833f, 6.983651f, 46.657822f, 6.283304f };
	private final double[]		in_Z				= new double[] { 3, 3, 3, 3 };
	
	private final double[]		in_nX				= new double[] { 25.790134f, 27.498366f, -35.669685f, -37.064451f };
	private final double[]		in_nXpY			= new double[] { 42.513433f, -7.644002f, -44.513078f, -3.559704f };
	private final double[]		in_nXnY			= new double[] { -7.433700f, 43.052684f, -4.335821f, -45.557781f };
	
	private final double[]		supportAngles	= new double[] {
															-AngleMath.PI,
															-AngleMath.PI_HALF - AngleMath.PI_QUART,
															-AngleMath.PI_HALF,
															-AngleMath.PI_QUART,
															0,
															AngleMath.PI_QUART,
															AngleMath.PI_HALF,
															AngleMath.PI_HALF + AngleMath.PI_QUART,
															AngleMath.PI };
	private final double[][]	supportIns		= new double[][] {
															in_nX, in_nXnY, in_nY, in_pXnY, in_pX, in_pXpY, in_pY, in_nXpY, in_nX };
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public CombinationMotorModel()
	{
		// in_pX[0] = in_pX[1] = (in_pX[0] + in_pX[1]) / 2.0;
		// in_pX[2] = in_pX[3] = (in_pX[2] + in_pX[3]) / 2.0;
		//
		// in_pY[0] = (in_pY[0] - in_pY[1]) / 2.0;
		// in_pY[1] = -in_pY[0];
		// in_pY[3] = (-in_pY[2] + in_pY[3]) / 2.0;
		// in_pY[2] = -in_pY[3];
	}
	
	
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 xyw)
	{
		double velAngle = 0;
		if (!xyw.getXYVector().isZeroVector())
		{
			velAngle = xyw.getXYVector().getAngle();
		}
		double velAbs = xyw.getXYVector().getLength2();
		double[] in = new double[4];
		
		for (int s = 1; s < supportAngles.length; s++)
		{
			if (velAngle <= supportAngles[s])
			{
				double min = supportAngles[s - 1];
				double max = supportAngles[s];
				double relMax = (velAngle - min) / (max - min);
				
				for (int i = 0; i < 4; i++)
				{
					in[i] = (velAbs *
							((supportIns[s - 1][i] * (1 - relMax))
							+ (supportIns[s][i] * relMax)))
							+ (xyw.z() * in_Z[i]);
				}
				break;
			}
		}
		
		return new VectorN(in);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.COMBINATION;
	}
}
