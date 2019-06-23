/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.drawable.ShapeMap;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVisionFilterShapesLayer implements ShapeMap.IShapeLayer
{
	QUALITY_SHAPES("Quality Inspector"),
	CAM_INFO_SHAPES("Cam Info"),
	VIEWPORT_SHAPES("Viewports"),
	ROBOT_TRACKER_SHAPES("Robot Trackers"),
	BALL_TRACKER_SHAPES("Ball Trackers");
	
	
	private final String		name;
	private final boolean	visible;
	private final int			orderId;
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name)
	{
		this(name, false);
	}
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name, final boolean visible)
	{
		this.name = name;
		this.visible = visible;
		orderId = 10 + ordinal();
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
		return "Vision Filter";
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
		return EVisionFilterShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
}
