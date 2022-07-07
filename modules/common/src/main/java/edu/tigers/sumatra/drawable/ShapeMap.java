/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Data structure that contains shapes, organized in layers.
 */
@Persistent
public class ShapeMap
{
	private final Map<String, ShapeLayer> categories;

	private static boolean persistDebugShapes = true;


	/**
	 * Create new empty shape map
	 */
	public ShapeMap()
	{
		this(new HashMap<>());
	}


	private ShapeMap(Map<String, ShapeLayer> map)
	{
		categories = map;
	}


	public void addAll(ShapeMap shapeMap)
	{
		for (ShapeLayer sl : shapeMap.categories.values())
		{
			categories.put(sl.identifier.getId(), new ShapeLayer(sl));
		}
	}


	public static ShapeMap unmodifiableCopy(ShapeMap s)
	{
		Map<String, ShapeLayer> categories = new HashMap<>();
		for (ShapeLayer sl : s.categories.values())
		{
			categories.put(sl.identifier.getId(),
					new ShapeLayer(sl.identifier, Collections.unmodifiableList(sl.shapes), sl.inverted));
		}
		return new ShapeMap(Collections.unmodifiableMap(categories));
	}


	/**
	 * @param persistDebugShapes should debug shapes be persisted?
	 */
	public static void setPersistDebugShapes(final boolean persistDebugShapes)
	{
		ShapeMap.persistDebugShapes = persistDebugShapes;
	}


	/**
	 * Get list for layer and category
	 *
	 * @param identifier
	 * @return
	 */
	public List<IDrawableShape> get(final IShapeLayerIdentifier identifier)
	{
		return categories.computeIfAbsent(identifier.getId(), k -> new ShapeLayer(identifier)).shapes;
	}


	/**
	 * Remove all shapes that should not be persisted
	 */
	public void removeNonPersistent()
	{
		categories.entrySet().removeIf(en -> !persist(en.getValue().identifier));
	}


	private boolean persist(IShapeLayerIdentifier identifier)
	{
		return identifier.getPersistenceType() == EShapeLayerPersistenceType.ALWAYS_PERSIST ||
				(persistDebugShapes && identifier.getPersistenceType() == EShapeLayerPersistenceType.DEBUG_PERSIST);
	}


	/**
	 * @return
	 */
	public List<IShapeLayerIdentifier> getAllShapeLayersIdentifiers()
	{
		return categories.values().stream().sorted().map(sl -> sl.identifier).toList();
	}


	/**
	 * @return
	 */
	public List<ShapeLayer> getAllShapeLayers()
	{
		return categories.values().stream().sorted().toList();
	}


	/**
	 * @param inverted
	 */
	public void setInverted(final boolean inverted)
	{
		categories.values().forEach(sl -> sl.inverted = inverted);
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("categories", categories)
				.toString();
	}


	/**
	 * Type of persistence for shape layers
	 */
	public enum EShapeLayerPersistenceType
	{
		ALWAYS_PERSIST,
		NEVER_PERSIST,
		DEBUG_PERSIST
	}

	@Persistent
	public static class ShapeLayer implements Comparable<ShapeLayer>
	{
		final IShapeLayerIdentifier identifier;
		final List<IDrawableShape> shapes;
		boolean inverted = false;


		@SuppressWarnings("unused")
		private ShapeLayer()
		{
			identifier = ShapeLayerIdentifier.builder().build();
			shapes = Collections.emptyList();
		}


		/**
		 * @param identifier
		 */
		public ShapeLayer(final IShapeLayerIdentifier identifier)
		{
			this.identifier = identifier;
			shapes = new ArrayList<>();
		}


		public ShapeLayer(final IShapeLayerIdentifier identifier, final List<IDrawableShape> shapes,
				final boolean inverted)
		{
			this.identifier = identifier;
			this.shapes = shapes;
			this.inverted = inverted;
		}


		/**
		 * @param o
		 */
		public ShapeLayer(final ShapeLayer o)
		{
			identifier = o.identifier;
			shapes = new ArrayList<>(o.shapes);
			inverted = o.inverted;
		}


		public IShapeLayerIdentifier getIdentifier()
		{
			return identifier;
		}


		public List<IDrawableShape> getShapes()
		{
			return shapes;
		}


		public boolean isInverted()
		{
			return inverted;
		}


		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("identifier", identifier)
					.append("shapes", shapes)
					.append("inverted", inverted)
					.toString();
		}


		@Override
		public int compareTo(final ShapeLayer o)
		{
			return Integer.compare(identifier.getOrderId(), o.identifier.getOrderId());
		}


		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;

			if (o == null || getClass() != o.getClass())
				return false;

			final ShapeLayer that = (ShapeLayer) o;

			return new EqualsBuilder()
					.append(identifier, that.identifier)
					.isEquals();
		}


		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(identifier)
					.toHashCode();
		}
	}
}
