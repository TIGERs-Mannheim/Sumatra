/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * The TrajectoryGenerator is a factory for trajectories!
 * 
 * @author AndreR
 * 
 */
public class TrajectoryGenerator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default constructor.
	 */
	public TrajectoryGenerator()
	{
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param maxVelocity Maximum usable velocity.
	 * @param acceleration Maximum usable acceleration.
	 */
	public TrajectoryGenerator(float maxVelocity, float acceleration)
	{
		this.maxVelocity = maxVelocity;
		this.acceleration = acceleration;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private float	maxVelocity		= 2.0f;
	private float	acceleration	= 1.0f;
	
	
	/**
	 * Generate a trajectory using maximum permissible values to result in
	 * shortest time.
	 * 
	 * @param s The way to go
	 * @param vb Initial velocity
	 * @param ve Desired end velocity
	 * @return
	 */
	public ITrajectory1D createTrajectory(float s, float vb, float ve)
	{
		// note: only stable for positive s and a values
		
		boolean invert = false;
		
		if (s < 0)
		{
			s *= -1.0f;
			vb *= -1.0f;
			ve *= -1.0f;
			invert = true;
		}
		
		// calc maximum end velocity assuming single accel or deccel phase
		if (ve > vb)
		{
			float maxEndVelocity = getMaxEndVelocity(s, acceleration, vb);
			
			// check if the desired end velocity can be reached (limit it otherwise)
			if (ve > maxEndVelocity)
			{
				ve = maxEndVelocity; // TODO AndreR exception? inform caller?
			}
		} else
		{
			float maxEndVelocity = getMaxEndVelocity(s, -acceleration, vb);
			
			if (ve < maxEndVelocity)
			{
				ve = maxEndVelocity;
			}
		}
		
		// calc accel time (tm) and deccel time (tp) assuming unlimited max velocity
		// due to limiting ve there should be no negative times in the end
		float rawTp = getAccelerationTime(s, acceleration, vb, ve);
		float rawTm = getDeccelerationTime(s, acceleration, vb, ve);
		
		assert (rawTp > 0);
		assert (rawTm > 0);
		
		// calculate peak velocity (still assuming infinite max velocity)
		float peakVelocity = getPeakVelocity(s, acceleration, vb, ve);
		
		if (peakVelocity < maxVelocity)
		{
			// only accel and deccel phases needed, no plateau
			return new Trajectory1D2Phase(rawTp, rawTm, acceleration, vb, peakVelocity, ve, invert);
		}
		
		// else
		// limit velocity, introduce plateau phase
		rawTp = getAccelerationTime(acceleration, s, maxVelocity, vb, ve);
		float rawTc = getPlateauTime(acceleration, s, maxVelocity, vb, ve);
		rawTm = getDeccelerationTime(acceleration, s, maxVelocity, vb, ve);
		float rawTt = rawTp + rawTc + rawTm;
		
		// TODO AndreR use rawTt to sync with other trajectories
		
		// trajectory done, use above parameters to get velocity at any point
		
		return new Trajectory1D3Phase(rawTp, rawTm, rawTc, rawTt, acceleration, vb, maxVelocity, ve, invert);
	}
	
	
	/**
	 * Generate a trajectory which ends after t seconds
	 * 
	 * @param s The way to go
	 * @param vb Initial velocity
	 * @param ve Desired end velocity
	 * @param t trajectory time
	 * @return
	 */
	public ITrajectory1D createTrajectory(float s, float vb, float ve, float t)
	{
		boolean invert = false;
		
		if (s < 0)
		{
			s *= -1.0f;
			vb *= -1.0f;
			ve *= -1.0f;
			invert = true;
		}
		
		// calculate maximum way assuming only accel and deccel phases
		float maxDistance = getMaxDistance(t, acceleration, vb, ve);
		
		// check if the requested way can be driven in time
		if (s > maxDistance)
		{
			s = maxDistance; // TODO AndreR is that an exception?
			
			float rawTp = getAccelerationTime(s, acceleration, vb, ve);
			float rawTm = getDeccelerationTime(s, acceleration, vb, ve);
			
			// this favors the time constraint! s is reduced!
			return new Trajectory1D2Phase(rawTp, rawTm, acceleration, vb, vb + (rawTp * acceleration), ve, invert);
		}
		
		// calculate velocity for plateau phase
		float vc = getMaxVelocity(t, s, acceleration, vb, ve);
		
		// check if needed velocity can be reached
		if (vc > maxVelocity)
		{
			vc = maxVelocity; // TODO AndreR exception? inform caller?
		}
		
		// limit velocity, introduce plateau phase
		float rawTp = getAccelerationTime(acceleration, s, vc, vb, ve);
		float rawTc = getPlateauTime(acceleration, s, vc, vb, ve);
		float rawTm = getDeccelerationTime(acceleration, s, vc, vb, ve);
		float rawTt = getTotalTime(rawTp, rawTc, rawTm);
		
		// trajectory done, use above parameters to get velocity at any point
		return new Trajectory1D3Phase(rawTp, rawTm, rawTc, rawTt, acceleration, vb, vc, ve, invert);
	}
	
	
	private float getPeakVelocity(float s, float a, float vb, float ve)
	{
		return SumatraMath.sqrt((ve * ve) + (vb * vb) + (2 * a * s)) / SumatraMath.sqrt(2);
	}
	
	
	private float getAccelerationTime(float a, float s, float vc, float vb, float ve)
	{
		return Math.abs((vc - vb) / a);
	}
	
	
	/**
	 * Calculate acceleration time assuming infinite max velocity
	 * 
	 * @param s The way to go
	 * @param a available acceleration
	 * @param vb initial velocity
	 * @param ve end velocity
	 * @return time in [s]
	 */
	private float getAccelerationTime(float s, float a, float vb, float ve)
	{
		return (SumatraMath.sqrt((ve * ve) + (vb * vb) + (2 * a * s)) - (SumatraMath.sqrt(2) * vb))
				/ (SumatraMath.sqrt(2) * a);
	}
	
	
	private float getDeccelerationTime(float a, float s, float vc, float vb, float ve)
	{
		return Math.abs((ve - vc) / a);
	}
	
	
	/**
	 * Calculate decceleration time assuming infinite max velocity.
	 * 
	 * @param s The way to go
	 * @param a available acceleration
	 * @param vb initial velocity
	 * @param ve end velocity
	 * @return time in [s]
	 */
	private float getDeccelerationTime(float s, float a, float vb, float ve)
	{
		return (SumatraMath.sqrt((ve * ve) + (vb * vb) + (2 * a * s)) - (SumatraMath.sqrt(2) * ve))
				/ (SumatraMath.sqrt(2) * a);
	}
	
	
	private float getPlateauTime(float a, float s, float vc, float vb, float ve)
	{
		if ((vb < vc) && (ve < vc)) // positive accel, negative deccel
		{
			return Math.abs((((ve * ve) - (2 * vc * vc)) + (vb * vb) + (2 * a * s)) / (2 * a * vc));
		}
		
		if ((vb < vc) && (ve > vc)) // positive accel, positive deccel
		{
			return Math.abs(((ve * ve) - (vb * vb) - (2 * a * s)) / (2 * a * vc));
		}
		
		if ((vb > vc) && (ve < vc)) // negative accel, negative deccel
		{
			return Math.abs((((ve * ve) - (vb * vb)) + (2 * a * s)) / (2 * a * vc));
		}
		
		if ((vb > vc) && (ve > vc)) // negative accel, positive deccel
		{
			return Math.abs(((((ve * ve) - (2 * vc * vc)) + (vb * vb)) - (2 * a * s)) / (2 * a * vc));
		}
		
		return 0;
	}
	
	
	private float getTotalTime(float tp, float tc, float tm)
	{
		return Math.abs(tp) + Math.abs(tc) + Math.abs(tm);
	}
	
	
	private float getMaxEndVelocity(float s, float a, float vb)
	{
		return SumatraMath.sqrt((vb * vb) + (2 * a * s));
	}
	
	
	/**
	 * Get the maximum velocity for a plateau phase to reach s in t seconds
	 * 
	 * @param t Time of the complete trajectory
	 * @param s Way to go with this trajectory
	 * @param a Max acceleration
	 * @param vb initial velocity
	 * @param ve end velocity
	 * @return
	 */
	private float getMaxVelocity(float t, float s, float a, float vb, float ve)
	{
		if (((vb * t) < s) && ((ve * t) < s)) // positive accel, negative deccel
		{
			float tmp = SumatraMath
					.sqrt((((-(ve * ve) + (2 * vb * ve) + (2 * a * t * ve)) - (vb * vb)) + (2 * a * t * vb) + (a * a * t * t))
							- (4 * a * s));
			
			return ((((ve * (tmp - (2 * vb) - (2 * a * t))) + (vb * (tmp - (2 * a * t))) + (a * t * tmp) + (ve * ve) + (vb * vb)) - (a
					* a * t * t)) + (4 * a * s))
					/ (2 * tmp);
		}
		
		if (((vb * t) < s) && ((ve * t) > s)) // positive accel, positive deccel
		{
			return ((ve * ve) - (vb * vb) - (2 * a * s)) / ((2 * ve) - (2 * vb) - (2 * a * t));
		}
		
		if (((vb * t) > s) && ((ve * t) > s)) // negative accel, positive deccel
		{
			float tmp = SumatraMath.sqrt(((-(ve * ve) + (2 * vb * ve)) - (2 * a * t * ve) - (vb * vb) - (2 * a * t * vb))
					+ (a * a * t * t) + (4 * a * s));
			
			return -(((a * t * tmp) + (ve * ((-tmp - (2 * vb)) + (2 * a * t))) + (vb * ((2 * a * t) - tmp)) + (ve * ve) + (vb * vb))
					- (a * a * t * t) - (4 * a * s))
					/ (2 * tmp);
		}
		
		if (((vb * t) > s) && ((ve * t) < s)) // negative accel, negative deccel
		{
			return (((ve * ve) - (vb * vb)) + (2 * a * s)) / (((2 * ve) - (2 * vb)) + (2 * a * t));
		}
		
		return 0;
	}
	
	
	/**
	 * Get the maximum driving distance if a 2-phase trajectory with infinite max velocity is assumed
	 * 
	 * @param t Time of the complete trajectory
	 * @param a max acceleration
	 * @param vb initial velocity
	 * @param ve end velocity
	 * @return
	 */
	private float getMaxDistance(float t, float a, float vb, float ve)
	{
		return -(((ve * ve) + (((-2 * vb) - (2 * a * t)) * ve) + (vb * vb)) - (2 * a * t * vb) - (a * a * t * t))
				/ (4 * a);
	}
	
	
	/**
	 * Simple test function
	 */
	public void test()
	{
		
		// testing trajectory
		CSVExporter.createInstance("traj", "traj", false);
		CSVExporter exporter = CSVExporter.getInstance("traj");
		exporter.setHeader("t", "v1", "v2");
		
		// make trajectory
		// ITrajectory1D traj = TrajectoryGenerator.getInstance().createTrajectory(10, 1, -0.2f, 20);
		ITrajectory1D trajX = createTrajectory(5, 1, 0);
		// ITrajectory1D trajY = trajX.reduce(0.2f);
		ITrajectory1D trajY = createTrajectory(1, 0, 0, trajX.getTotalTime());
		
		for (float t = 0.0f; t < 30.0f; t += 0.01f)
		{
			exporter.addValues(t, trajX.getVelocity(t), trajY.getVelocity(t));
		}
		
		exporter.close();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return max velocity
	 */
	public float getMaxVelocity()
	{
		return maxVelocity;
	}
	
	
	/**
	 * @param maxVelocity
	 */
	public void setMaxVelocity(float maxVelocity)
	{
		this.maxVelocity = maxVelocity;
	}
	
	
	/**
	 * @return max accleration
	 */
	public float getAcceleration()
	{
		return acceleration;
	}
	
	
	/**
	 * @param acceleration
	 */
	public void setAcceleration(float acceleration)
	{
		this.acceleration = acceleration;
	}
}
