/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IShapeLayer
{
	/**
	 * @return
	 */
	String getCategory();
	
	
	/**
	 * @return
	 */
	String getLayerName();
	
	
	/**
	 * @return
	 */
	String getId();
	
	
	/**
	 * @return
	 */
	default EShapeLayerPersistenceType getPersistenceType()
	{
		return EShapeLayerPersistenceType.ALWAYS_PERSIST;
	}
	
	
	/**
	 * @return
	 */
	default boolean isVisibleByDefault()
	{
		return false;
	}
	
	
	/**
	 * With a low order id, the layer will be drawn before higher order ids.
	 *
	 * @return
	 */
	default int getOrderId()
	{
		return 50;
	}
}
