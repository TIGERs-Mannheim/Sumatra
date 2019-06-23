/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.models.ALearnedModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LearnedBallModel extends ALearnedModel
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(LearnedBallModel.class.getName());
	
	
	/**
	 * @param identifier
	 * @throws IllegalStateException if there is now config for identifier
	 */
	public LearnedBallModel(final String identifier)
	{
		super("ball", identifier);
		
	}
	
	
	/**
	 * gets the theoretical position of the ball after a given time
	 * 
	 * @param currentPos [mm]
	 * @param currentVel [m/s]
	 * @param time [s]
	 * @return the position
	 */
	public IVector2 getPosByTime(final IVector2 currentPos, final IVector2 currentVel, final float time)
	{
		float x = currentVel.getLength2() * 1000;
		float y = time * 1e3f;
		
		float result = p[0] + (p[1] * x) + (p[2] * y) + (p[3] * x * x) + (p[4] * x * y) + (p[5] * y * y);
		if (result < 0)
		{
			return currentPos;
		}
		return currentPos.addNew(currentVel.scaleToNew(result));
	}
	
	
	/**
	 * @param currentVel
	 * @param time
	 * @return
	 */
	public float getDistByTime(final float currentVel, final float time)
	{
		float x = currentVel * 1000;
		float y = time * 1e3f;
		
		float result = p[0] + (p[1] * x) + (p[2] * y) + (p[3] * x * x) + (p[4] * x * y) + (p[5] * y * y);
		if (result < 0)
		{
			return 0;
		}
		return result;
	}
	
	
	/**
	 * gets the theoretical position of the ball when it reaches a given velocity
	 * 
	 * @param currentPos [mm]
	 * @param currentVel [m/s]
	 * @param velocity [m/s]
	 * @return the position
	 */
	public IVector2 getPosByVel(final IVector2 currentPos, final IVector2 currentVel, final float velocity)
	{
		return getPosByTime(currentPos, currentVel, getTimeByVel(currentVel.getLength2(), velocity));
	}
	
	
	/**
	 * @param currentVel
	 * @param velocity
	 * @return
	 */
	public float getDistByVel(final float currentVel, final float velocity)
	{
		return getDistByTime(currentVel, getTimeByVel(currentVel, velocity));
	}
	
	
	/**
	 * gets the theoretical needed time for the ball to travel dist
	 * 
	 * @param currentVel [m/s]
	 * @param dist [mm]
	 * @return the time [s]
	 */
	public float getTimeByDist(final float currentVel, final float dist)
	{
		float x = currentVel * 1000;
		
		// see: http://www.wolframalpha.com/input/?i=solve+g%3Da%2Bb*x%2Bc*y%2Bd*x*x%2Be*x*y%2Bf*y*y+for+y
		final float t;
		if (p[5] != 0)
		{
			float breaket2 = (p[0] + (p[1] * x) + (p[3] * x * x)) - dist;
			float sqrt = SumatraMath.square(p[2] + (p[4] * x)) - (4 * p[5] * breaket2);
			if (sqrt < 0)
			{
				// numerical issue
				sqrt = 0;
			}
			float counter = SumatraMath.sqrt(sqrt) - p[2] - (p[4] * x);
			float denominator = 2 * p[5];
			float y = counter / denominator;
			if (y < 0)
			{
				counter = -SumatraMath.sqrt(sqrt) - p[2] - (p[4] * x);
				t = counter / denominator;
			} else
			{
				t = y;
			}
		} else
		{
			// this part should usually not be called, but just in case...
			float counter = (-p[0] - (p[1] * x) - (p[3] * x * x)) + dist;
			float denominator = (p[2] + (p[4] * x));
			if (denominator == 0)
			{
				return 0;
			}
			t = counter / denominator;
		}
		return Math.max(0, t / 1000);
	}
	
	
	/**
	 * gets the theoretical time where the ball reaches a given velocity
	 * 
	 * @param currentVel [m/s]
	 * @param velocity [m/s]
	 * @return the time [s]
	 */
	public float getTimeByVel(final float currentVel, final float velocity)
	{
		// d/dt f(vel,t) = newVel -> transpose to t
		return Math.max(0, ((velocity - p[2] - (p[4] * currentVel * 1000)) / (2 * p[5])) / 1000);
		// return (getVel().getLength2() - velocity) / (0.02f * 9.81f);
	}
	
	
	/**
	 * gets the theoretical velocity of the ball at a given position
	 * 
	 * @param currentVel [m/s]
	 * @param dist [mm]
	 * @return the velocity [m/s]
	 */
	public float getVelByDist(final float currentVel, final float dist)
	{
		return getVelByTime(currentVel, getTimeByDist(currentVel, dist));
	}
	
	
	/**
	 * gets the theoretical velocity of the ball after a given time
	 * 
	 * @param currentVel [m/s]
	 * @param time [s]
	 * @return the velocity [m/s]
	 */
	public float getVelByTime(final float currentVel, final float time)
	{
		float x = currentVel * 1000;
		float y = time * 1e3f;
		// d/dt f(vel,t) = newvel
		float velocity = ((p[2] + (p[4] * x) + (2 * p[5] * y)));
		// float velocity = getVel().getLength2() - (time * (0.02f * 9.81f));
		if (velocity < 0)
		{
			velocity = 0;
		}
		return velocity;
	}
	
	
	/**
	 * Get required initial velocity if the ball should have a speed of endVel after given time
	 * 
	 * @param endVel
	 * @param time
	 * @return
	 */
	public float getVelForTime(final float endVel, final float time)
	{
		float t = time * 1000;
		// f'(v,t) = vd (note: vd is m/s)
		float v = (endVel - p[2] - (2 * p[5] * t)) / p[4];
		
		if (v < 0)
		{
			v = 0;
		}
		return v / 1000;
	}
	
	
	/**
	 * Get the required initial velocity if ball should have a speed of endVel after traveling given dist
	 * 
	 * @param dist [mm]
	 * @param endVel [m/s]
	 * @return
	 * @see <a href=
	 *      "http://www.wolframalpha.com/input/?i=solve+a+%2B+b*v+%2B+c*%28%28w-e*v-c%29%2F%282*f%29%29+%2B+d*v*v+%2B+e*v*%28%28w-e*v-c%29%2F%282*f%29%29+%2B+f*%28%28w-e*v-c%29%2F%282*f%29%29%5E2+%3D+g+for+v"
	 *      >wolframalpha.com</a>
	 */
	public float getVelForDist(final float dist, final float endVel)
	{
		/**
		 * f(v,t) = dist
		 * f'(v,t) = endVel
		 * -> solve for v
		 */
		float a = p[0], b = p[1], c = p[2], d = p[3], e = p[4], f = p[5], g = dist, w = endVel;
		float denominator = (e * e) - (4 * d * f);
		if ((denominator != 0) && (f != 0))
		{
			float sqrt = SumatraMath.square((2 * c * e) - (4 * b * f))
					- (4 * ((e * e) - (4 * d * f)) * (((-4 * a * f) + (c * c) + (4 * f * g)) - (w * w)));
			float rest = (2 * b * f) - (c * e);
			if (sqrt < 0)
			{
				sqrt = 0;
			}
			float v = ((-SumatraMath.sqrt(sqrt) / 2f) + rest) / denominator;
			if (v < 0)
			{
				v = ((SumatraMath.sqrt(sqrt) / 2f) + rest) / denominator;
			}
			return v / 1000;
		}
		throw new IllegalStateException("Oh, we get here? :/");
	}
	
	
	/**
	 * @return acc [m/s^2]
	 */
	public float getAcc()
	{
		return 2000 * p[5];
	}
	
	
	@Override
	protected void onNewParameters()
	{
	}
}
