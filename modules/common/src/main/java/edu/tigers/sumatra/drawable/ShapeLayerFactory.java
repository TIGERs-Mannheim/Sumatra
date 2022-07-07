/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class ShapeLayerFactory
{
	private final Class<?> type;
	@Getter
	private final List<IShapeLayerIdentifier> layers = new ArrayList<>();
	private int order;


	public ShapeLayerIdentifier.ShapeLayerIdentifierBuilder layer(String name)
	{
		return ShapeLayerIdentifier.builder()
				.layerName(name)
				.orderId(order++);
	}


	public ShapeLayerIdentifier create(ShapeLayerIdentifier.ShapeLayerIdentifierBuilder builder)
	{
		ShapeLayerIdentifier layer = builder.build();
		String id = escape(createId(layer));
		ShapeLayerIdentifier layerWithId = layer.toBuilder()
				.id(id)
				.build();
		layers.add(layerWithId);
		return layerWithId;
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
