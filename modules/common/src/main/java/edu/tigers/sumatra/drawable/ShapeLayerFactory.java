/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ShapeLayerFactory
{
	private final Class<?> type;
	private int order;


	public ShapeLayerIdentifier.ShapeLayerIdentifierBuilder category(String name)
	{
		return ShapeLayerIdentifier.builder()
				.category(name)
				.orderId(order++);
	}


	public ShapeLayerIdentifier create(ShapeLayerIdentifier.ShapeLayerIdentifierBuilder builder)
	{
		ShapeLayerIdentifier layer = builder.build();
		return layer.toBuilder()
				.id(escape(createId(layer)))
				.build();
	}


	private String createId(ShapeLayerIdentifier layer)
	{
		return type.getCanonicalName() + "." + String.join(".", layer.getCategories()) + "." + layer.getLayerName();
	}


	private String escape(String value)
	{
		return value.replace(" ", "_");
	}
}
