/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum EWpShapesLayer implements IShapeLayer
{
	/**  */
	FIELD_BORDERS("Field Borders", "Field", true),
	/**  */
	REFEREE("Referee", "Field", true, 1000),
	/**  */
	BALL_BUFFER("Ball buffer", "Field"),
	/**  */
	BOT_BUFFER("Bot buffer", "Field"),
	/**  */
	BOTS("Bots", "Field", true),
	/**  */
	BALL("Ball", "Field", true),
	/**  */
	VELOCITY("Velocities", "Field"),
	
	/** */
	@Deprecated
	BOTS_AVAILABLE("Bots available", "Field"),
	/**  */
	@Deprecated
	VISION("Vision", "Field"),
	/**  */
	@Deprecated
	CAM_INTERSECTION("Cam intersection", "Field"),
	/**  */
	@Deprecated
	COORDINATE_SYSTEM("Coordinate System", "Field"),
	/**  */
	@Deprecated
	AUTOREFEREE("AutoReferee", "Field", true),;
	
	
	private final String		name;
	private final String		category;
	private final boolean	visible;
	private final int			orderId;
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category)
	{
		this(name, category, false);
	}
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		orderId = 10 + ordinal();
	}
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible, final int orderId)
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
