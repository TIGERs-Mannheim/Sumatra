/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s): GuntherB
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;


/**
 * Evolving Point Cloud.
 * 
 * @author GuntherB
 */
public class PointCloud implements Comparable<PointCloud>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final ValuePoint	masterPoint;
	
	/** on which evolutional state is this cloud, how big is it? */
	private int						evolution;
	
	private float					raySize;
	
	/** how many frames does this cloud exist already? */
	private int						lifetime;
	
	/** latest nominal value of the cloud, is gotten by merging specific cloud-values */
	private float					currentCloudValue;
	
	
	// how large the ray becomes in each Evo
	private static final float	SIZE_FIRST_EVO		= 50f;
	private static final float	SIZE_SECOND_EVO	= 100f;
	private static final float	SIZE_THIRD_EVO		= 150f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param masterPoint
	 */
	public PointCloud(final ValuePoint masterPoint)
	{
		this.masterPoint = masterPoint;
		
		raySize = 0f;
		evolution = 0;
		lifetime = 0;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * updates each point in the cloud with newest information, lets
	 * the crowd grow if not on maximum evolution, evaluates cloud
	 * 
	 * @param wf
	 */
	public void updateCloud(final WorldFrame wf)
	{
		// creating new points regarding its evolution
		increaseRaySize();
		
		lifetime++;
	}
	
	
	private void increaseRaySize()
	{
		if (evolution == 1)
		{
			raySize = SIZE_FIRST_EVO;
		}
		
		if (evolution == 2)
		{
			raySize = SIZE_SECOND_EVO;
		}
		
		if (evolution == 3)
		{
			raySize = SIZE_THIRD_EVO;
		}
		
		if (evolution == 4)
		{
			// max evolution, no increasing necessary
			return;
		}
		
		evolution++;
		
		return;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public float getValue()
	{
		return currentCloudValue;
	}
	
	
	/**
	 * @param value
	 */
	public void setValue(final float value)
	{
		currentCloudValue = value;
	}
	
	
	@Override
	public int compareTo(final PointCloud o)
	{
		if (currentCloudValue > o.currentCloudValue)
		{
			return 1;
		}
		if (currentCloudValue < o.currentCloudValue)
		{
			return -1;
		}
		return 0;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Float.floatToIntBits(currentCloudValue);
		result = (prime * result) + evolution;
		result = (prime * result) + lifetime;
		result = (prime * result) + ((masterPoint == null) ? 0 : masterPoint.hashCode());
		result = (prime * result) + Float.floatToIntBits(raySize);
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		PointCloud other = (PointCloud) obj;
		if (Float.floatToIntBits(currentCloudValue) != Float.floatToIntBits(other.currentCloudValue))
		{
			return false;
		}
		if (evolution != other.evolution)
		{
			return false;
		}
		if (lifetime != other.lifetime)
		{
			return false;
		}
		if (masterPoint == null)
		{
			if (other.masterPoint != null)
			{
				return false;
			}
		} else if (!masterPoint.equals(other.masterPoint))
		{
			return false;
		}
		if (Float.floatToIntBits(raySize) != Float.floatToIntBits(other.raySize))
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * @return
	 */
	public ValuePoint getMasterPoint()
	{
		return masterPoint;
	}
	
	
	/**
	 * @return
	 */
	public int getEvolution()
	{
		return evolution;
	}
	
	
	/**
	 * @return
	 */
	public int getLifetime()
	{
		return lifetime;
	}
	
	
	/**
	 * @return
	 */
	public float getRaySize()
	{
		return raySize;
	}
}
