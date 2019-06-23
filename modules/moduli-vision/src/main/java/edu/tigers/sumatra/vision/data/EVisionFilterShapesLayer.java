/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVisionFilterShapesLayer implements IShapeLayer
{
	QUALITY_SHAPES("Quality Inspector"),
	CAM_INFO_SHAPES("Cam Info"),
	VIEWPORT_SHAPES("Viewports"),
	ROBOT_TRACKER_SHAPES("Robot Trackers", false, EShapeLayerPersistenceType.DEBUG_PERSIST),
	BALL_TRACKER_SHAPES("Ball Trackers", false, EShapeLayerPersistenceType.DEBUG_PERSIST);
	
	private final String id;
	private final String name;
	private final boolean visible;
	private final int orderId;
	private final EShapeLayerPersistenceType persistenceType;
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name)
	{
		this(name, false, EShapeLayerPersistenceType.ALWAYS_PERSIST);
	}
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name, final boolean visible,
			final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.visible = visible;
		orderId = 10 + ordinal();
		id = EVisionFilterShapesLayer.class.getCanonicalName() + name();
		this.persistenceType = persistenceType;
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
	
	
	@Override
	public int getOrderId()
	{
		return orderId;
	}
	
	
	@Override
	public String getId()
	{
		return id;
	}
	
	
	@Override
	public EShapeLayerPersistenceType getPersistenceType()
	{
		return persistenceType;
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
}
