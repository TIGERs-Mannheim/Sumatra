/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true)
public class ShapeLayerIdentifier implements IShapeLayerIdentifier
{
	String id;
	String layerName;
	@Singular
	List<String> categories;
	@Builder.Default
	ShapeMap.EShapeLayerPersistenceType persistenceType = ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
	boolean visibleByDefault;
	int orderId;


	@Override
	public String getCategory()
	{
		throw new IllegalStateException();
	}


	@Override
	public String toString()
	{
		return layerName;
	}
}
