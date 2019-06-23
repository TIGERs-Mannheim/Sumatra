/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;


/**
 * TODO Vendetta, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author GuntherB
 * 
 */
public class PointCloud implements Comparable<PointCloud>, IValueObject
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
	
	private FrameID				lastWFID				= null;
	
	// how large the ray becomes in each Evo
	private final float			SIZE_FIRST_EVO		= 50f;
	private final float			SIZE_SECOND_EVO	= 100f;
	private final float			SIZE_THIRD_EVO		= 150f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PointCloud(ValuePoint masterPoint)
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
	 */
	public void updateCloud(WorldFrame wf)
	{
		if (wf.id != lastWFID) // new WF
		{
			
			// creating new points regarding its evolution
			increaseRaySize();
			
			lifetime++;
		}
		
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
	

	@Override
	public float getValue()
	{
		return currentCloudValue;
	}
	

	@Override
	public void setValue(float value)
	{
		this.currentCloudValue = value;
	}
	

	/**
	 * TODO OliS: If compare is overwritten, equals (and thus hashcode) should be implemented, or write a comment (See
	 * {@link Comparable#compareTo(Object)}) (Gero)
	 */
	@Override
	public int compareTo(PointCloud o)
	{
		if (this.currentCloudValue > o.currentCloudValue)
		{
			return 1;
		} else
		{
			if (this.currentCloudValue < o.currentCloudValue)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	

	public ValuePoint getMasterPoint()
	{
		return masterPoint;
	}
	

	public int getEvolution()
	{
		return evolution;
	}
	

	public int getLifetime()
	{
		return lifetime;
	}
	

	public float getRaySize()
	{
		return raySize;
	}
}
