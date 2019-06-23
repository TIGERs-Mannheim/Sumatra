/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;


/**
 * A point with additional info
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DefensePoint extends Vector2
{
	private final ATrackedObject	protectAgainst;
	private final FoeBotData		foeBotData;
	
	
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
	public DefensePoint(final IVector2 point, final ATrackedObject protectAgainst)
	{
		super(point);
		this.protectAgainst = protectAgainst;
		foeBotData = null;
	}
	
	
	/**
	 * @param point
	 * @param foeBotData
	 */
	public DefensePoint(final IVector2 point, final FoeBotData foeBotData)
	{
		super(point);
		protectAgainst = foeBotData.getFoeBot();
		this.foeBotData = foeBotData;
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
	public final ATrackedObject getProtectAgainst()
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
