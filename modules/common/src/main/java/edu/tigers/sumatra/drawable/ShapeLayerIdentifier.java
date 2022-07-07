/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Value
@Builder(toBuilder = true)
@Persistent
public class ShapeLayerIdentifier implements IShapeLayerIdentifier
{
	String id;
	String layerName;
	@Singular
	List<String> categories;
	ShapeMap.EShapeLayerPersistenceType persistenceType;
	boolean visibleByDefault;
	int orderId;


	@SuppressWarnings("unused") // berkeley
	private ShapeLayerIdentifier()
	{
		this("", "", List.of(), ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST, false, 0);
	}


	public ShapeLayerIdentifier(
			String id,
			String layerName,
			List<String> categories,
			ShapeMap.EShapeLayerPersistenceType persistenceType,
			boolean visibleByDefault,
			int orderId
	)
	{
		this.id = id;
		this.layerName = layerName;
		// use a simple list to avoid berkeley-incompatible implementations...
		this.categories = new ArrayList<>(categories);
		this.persistenceType =
				persistenceType != null ? persistenceType : ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
		this.visibleByDefault = visibleByDefault;
		this.orderId = orderId;
	}


	public static ShapeLayerIdentifierBuilder create(Class<?> type, String layerName)
	{
		return ShapeLayerIdentifier.builder().id(type.getCanonicalName() + "." + layerName).layerName(layerName);
	}


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
