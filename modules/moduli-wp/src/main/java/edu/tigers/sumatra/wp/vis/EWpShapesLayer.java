/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EWpShapesLayer implements IShapeLayer
{
	/**  */
	FIELD_BORDERS("Field Borders", "FIELD", true),
	/**  */
	@Deprecated COORDINATE_SYSTEM("Coordinate System", "FIELD"),
	/**  */
	REFEREE("Referee", "FIELD", true, 1000),
	/**  */
	CAM_INTERSECTION("Cam intersection", "VISION"),
	
	/**  */
	VISION("Vision", "VISION"),
	/**  */
	BALL_BUFFER("Ball buffer", "VISION"),
	/**  */
	BOT_BUFFER("Bot buffer", "VISION"),
	/**  */
	BOTS("Bots", "VISION", true),
	/**  */
	BALL("Ball", "VISION", true),
	/**  */
	VELOCITY("Velocity", "VISION"),
	/**  */
	AUTOREFEREE("AutoReferee", "VISION", true);
	
	
	private final String		name;
	private final String		category;
	private final boolean	visible;
	private final int			orderId;
	
	
	/**
	 * 
	 */
	private EWpShapesLayer(final String name, final String category)
	{
		this(name, category, false);
	}
	
	
	/**
	 * 
	 */
	private EWpShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		orderId = 10 + ordinal();
	}
	
	
	/**
	 * 
	 */
	private EWpShapesLayer(final String name, final String category, final boolean visible, final int orderId)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.orderId = orderId;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	/**
	 * @return the category
	 */
	@Override
	public final String getCategory()
	{
		return category;
	}
	
	
	/**
	 * @return the persist
	 */
	@Override
	public final boolean persist()
	{
		return true;
	}
	
	
	@Override
	public int getOrderId()
	{
		return orderId;
	}
	
	
	@Override
	public String getId()
	{
		return EWpShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
}
