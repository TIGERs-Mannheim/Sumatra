/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PIDController
{
	private double		p;
	private double		i;
	private double		d;
	private double		maximumOutput	= Double.MAX_VALUE;
	private double		minimumOutput	= -Double.MAX_VALUE;
	private double		maximumInput	= 0.0;
	private double		minimumInput	= 0.0;
	private boolean	continuous		= false;
	private double		prevError		= 0.0;
	private double		totalError		= 0.0;
	private double		setpoint			= 0.0;
	private double		error				= 0.0;
	private double		result			= 0.0;
	private double		input				= 0.0;
												
												
	/**
	 * Create a PID object with the given constants for P, I, D
	 * 
	 * @param Kp the proportional coefficient
	 * @param Ki the integral coefficient
	 * @param Kd the derivative coefficient
	 */
	public PIDController(final double Kp, final double Ki, final double Kd)
	{
		p = Kp;
		i = Ki;
		d = Kd;
	}
	
	
	/**
	 * Create a PID object with the given constants for P, I, D
	 * 
	 * @param Kp the proportional coefficient
	 * @param Ki the integral coefficient
	 * @param Kd the derivative coefficient
	 * @param continuous
	 */
	public PIDController(final double Kp, final double Ki, final double Kd, final boolean continuous)
	{
		p = Kp;
		i = Ki;
		d = Kd;
		this.continuous = continuous;
	}
	
	
	/**
	 * @param input
	 */
	public void update(final double input)
	{
		this.input = input;
		
		// Calculate the error signal
		error = setpoint - input;
		
		// If continuous is set to true allow wrap around
		if (continuous)
		{
			if (Math.abs(error) > ((maximumInput - minimumInput) / 2.0))
			{
				if (error > 0)
				{
					error = (error - maximumInput) + minimumInput;
				} else
				{
					error = (error +
							maximumInput) - minimumInput;
				}
			}
		}
		
		/*
		 * Integrate the errors as long as the upcoming integrator does
		 * not exceed the minimum and maximum output thresholds
		 */
		if ((((totalError + error) * i) < maximumOutput) &&
				(((totalError + error) * i) > minimumOutput))
		{
			totalError += error;
		}
		
		// Perform the primary PID calculation
		result = ((p * error) + (i * totalError) + (d * (error - prevError)));
		
		// Set the current error to the previous error for the next cycle
		prevError = error;
		
		// Make sure the final result is within bounds
		if (result > maximumOutput)
		{
			result = maximumOutput;
		} else if (result < minimumOutput)
		{
			result = minimumOutput;
		}
	}
	
	
	/**
	 * Get the Proportional coefficient
	 * 
	 * @return proportional coefficient
	 */
	public double getP()
	{
		return p;
	}
	
	
	/**
	 * Get the Integral coefficient
	 * 
	 * @return integral coefficient
	 */
	public double getI()
	{
		return i;
	}
	
	
	/**
	 * Get the Differential coefficient
	 * 
	 * @return differential coefficient
	 */
	public double getD()
	{
		return d;
	}
	
	
	/**
	 * Set the PID controller to consider the input to be continuous,
	 * Rather then using the max and min in as constraints, it considers them to
	 * be the same point and automatically calculates the shortest route to
	 * the setpoint.
	 * 
	 * @param continuous Set to true turns on continuous, false turns off continuous
	 */
	public void setContinuous(final boolean continuous)
	{
		this.continuous = continuous;
	}
	
	
	/**
	 * Set the PID controller to consider the input to be continuous,
	 * Rather then using the max and min in as constraints, it considers them to
	 * be the same point and automatically calculates the shortest route to
	 * the setpoint.
	 */
	public void setContinuous()
	{
		this.setContinuous(true);
	}
	
	
	/**
	 * Sets the maximum and minimum values expected from the input.
	 *
	 * @param minimumInput the minimum value expected from the input
	 * @param maximumInput the maximum value expected from the output
	 */
	public void setInputRange(final double minimumInput, final double maximumInput)
	{
		this.minimumInput = minimumInput;
		this.maximumInput = maximumInput;
		setSetpoint(setpoint);
	}
	
	
	/**
	 * Sets the minimum and maximum values to write.
	 *
	 * @param minimumOutput the minimum value to write to the output
	 * @param maximumOutput the maximum value to write to the output
	 */
	public void setOutputRange(final double minimumOutput, final double maximumOutput)
	{
		this.minimumOutput = minimumOutput;
		this.maximumOutput = maximumOutput;
	}
	
	
	/**
	 * Set the setpoint for the PIDController
	 * 
	 * @param setpoint the desired setpoint
	 */
	public void setSetpoint(final double setpoint)
	{
		if (maximumInput > minimumInput)
		{
			if (setpoint > maximumInput)
			{
				this.setpoint = maximumInput;
			} else if (setpoint < minimumInput)
			{
				this.setpoint = minimumInput;
			} else
			{
				this.setpoint = setpoint;
			}
		} else
		{
			this.setpoint = setpoint;
		}
	}
	
	
	/**
	 * @return the current setpoint
	 */
	public double getSetpoint()
	{
		return setpoint;
	}
	
	
	/**
	 * @return the current error
	 */
	public double getError()
	{
		return error;
	}
	
	
	/**
	 * @return
	 */
	public double getResult()
	{
		return result;
	}
	
	
	/**
	 */
	public void reset()
	{
		prevError = 0;
		totalError = 0;
		result = 0;
	}
	
	
	/**
	 * @return the input
	 */
	public final double getInput()
	{
		return input;
	}
	
	
	/**
	 * @param p the p to set
	 */
	public void setP(final double p)
	{
		this.p = p;
	}
	
	
	/**
	 * @param i the i to set
	 */
	public void setI(final double i)
	{
		this.i = i;
	}
	
	
	/**
	 * @param d the d to set
	 */
	public void setD(final double d)
	{
		this.d = d;
	}
}