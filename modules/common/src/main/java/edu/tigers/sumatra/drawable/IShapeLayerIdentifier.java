/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;

import java.util.List;


/**
 * Shape layer interface.
 */
public interface IShapeLayerIdentifier
{
	/**
	 * @return
	 */
	String getCategory();

	default List<String> getCategories()
	{
		return List.of(getCategory());
	}


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
