/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * A point with additional info
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DefensePoint extends Vector2
{
	private final IVector2		protectAgainst;
	private final FoeBotData	foeBotData;
										
										
	@SuppressWarnings("unused")
	private DefensePoint()
	{
		protectAgainst = null;
		foeBotData = null;
	}
	
	
	/**
	 * @param point
	 * @param protectAgainst
	 */
	public DefensePoint(final IVector2 point, final ITrackedObject protectAgainst)
	{
		super(point);
		this.protectAgainst = protectAgainst.getPos();
		foeBotData = null;
	}
	
	
	/**
	 * @param copyPoint
	 */
	public DefensePoint(final DefensePoint copyPoint)
	{
		
		super(copyPoint);
		protectAgainst = copyPoint.protectAgainst;
		foeBotData = copyPoint.foeBotData;
	}
	
	
	/**
	 * @param point
	 * @param foeBotData
	 */
	public DefensePoint(final IVector2 point, final FoeBotData foeBotData)
	{
		super(point);
		protectAgainst = foeBotData.getFoeBot().getPos();
		this.foeBotData = foeBotData;
	}
	
	
	/**
	 * @param point
	 * @param protectAgainst
	 */
	public DefensePoint(final IVector2 point, final IVector2 protectAgainst)
	{
		super(point);
		this.protectAgainst = protectAgainst;
		foeBotData = null;
	}
	
	
	/**
	 * @param point
	 */
	public DefensePoint(final IVector2 point)
	{
		super(point);
		protectAgainst = null;
		foeBotData = null;
	}
	
	
	/**
	 * @return the protectAgainst
	 */
	public final IVector2 getProtectAgainst()
	{
		return protectAgainst;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public final FoeBotData getFoeBotData()
	{
		return foeBotData;
	}
	
	
}
