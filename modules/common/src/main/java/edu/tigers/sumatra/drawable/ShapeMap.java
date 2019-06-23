/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class ShapeMap
{
	private static final int					DEFAULT_ORDER_ID		= 50;
	private final Map<String, ShapeLayer> categories = new ConcurrentHashMap<>();
	
	private static boolean						persistDebugShapes	= true;
	
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
		default boolean persist()
		{
			EShapeLayerPersistenceType type = getPersistenceType();
			return type == EShapeLayerPersistenceType.ALWAYS_PERSIST
					|| (persistDebugShapes && type == EShapeLayerPersistenceType.DEBUG_PERSIST);
		}
		
		
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
			return DEFAULT_ORDER_ID;
		}
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
	private static class ShapeLayer implements Comparable<ShapeLayer>
	{
		final IShapeLayer			layer;
		final List<ShapeList>	shapes	= new ArrayList<>();
		
		
		@SuppressWarnings("unused")
		private ShapeLayer()
		{
			this((IShapeLayer) null);
		}
		
		
		/**
		 * @param layer
		 */
		public ShapeLayer(final IShapeLayer layer)
		{
			this.layer = layer;
			shapes.add(new ShapeList());
		}
		
		
		/**
		 * @param o
		 */
		public ShapeLayer(final ShapeLayer o)
		{
			layer = o.layer;
			for (ShapeList l : o.shapes)
			{
				shapes.add(new ShapeList(l));
			}
		}
		
		
		@Override
		public int compareTo(final ShapeLayer o)
		{
			return Integer.compare(layer.getOrderId(), o.layer.getOrderId());
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((layer == null) ? 0 : layer.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			ShapeLayer other = (ShapeLayer) obj;
			if (layer == null)
			{
				if (other.layer != null)
				{
					return false;
				}
			} else if (!layer.equals(other.layer))
			{
				return false;
			}
			return true;
		}
		
	}
	
	@Persistent
	private static class ShapeList
	{
		List<IDrawableShape>	shapes	= new ArrayList<>();
		boolean					inverted	= false;
		
		
		/**
		 * Create new empty shape list
		 */
		public ShapeList()
		{
			// empty
		}
		
		
		/**
		 * @param o
		 */
		public ShapeList(final ShapeList o)
		{
			shapes = new ArrayList<>(o.shapes);
			inverted = o.inverted;
		}
	}
	
	
	/**
	 * Create new empty shape map
	 */
	public ShapeMap()
	{
		// empty
	}
	
	
	/**
	 * Deep copy (shapes not copied)
	 * 
	 * @param o original map
	 */
	public ShapeMap(final ShapeMap o)
	{
		for (ShapeLayer sl : o.categories.values())
		{
			categories.put(sl.layer.getId(), new ShapeLayer(sl));
		}
	}
	
	
	/**
	 * Get list for layer and category
	 * 
	 * @param shapeLayer
	 * @return
	 */
	public List<IDrawableShape> get(final IShapeLayer shapeLayer)
	{
		ShapeLayer sl = categories.computeIfAbsent(shapeLayer.getId(), k -> new ShapeLayer(shapeLayer));
		
		return sl.shapes.get(0).shapes;
	}
	
	
	/**
	 * @param shapeLayer
	 * @param shapes
	 */
	public void put(final IShapeLayer shapeLayer, final List<IDrawableShape> shapes)
	{
		for (IDrawableShape shape : shapes)
		{
			assert shape != null;
		}
		ShapeLayer sl = new ShapeLayer(shapeLayer);
		ShapeList l = new ShapeList();
		l.shapes = shapes;
		sl.shapes.add(l);
		categories.put(shapeLayer.getId(), sl);
	}
	
	
	/**
	 * @param layerId
	 */
	public void remove(final String layerId)
	{
		categories.remove(layerId);
	}
	
	
	/**
	 * @param map
	 */
	public void merge(final ShapeMap map)
	{
		// thread safe iteration
		Collection<ShapeLayer> catsCopy = new ArrayList<>(map.categories.values());
		for (ShapeLayer newsl : catsCopy)
		{
			ShapeLayer sl = categories.computeIfAbsent(newsl.layer.getId(), k -> new ShapeLayer(newsl.layer));
			sl.shapes.addAll(newsl.shapes);
		}
	}
	
	
	/**
	 * Remove all shapes that should not be persisted
	 */
	public void removeNonPersistent()
	{
		categories.entrySet().removeIf(en -> !en.getValue().layer.persist());
	}
	
	
	/**
	 * @return
	 */
	public List<IShapeLayer> getAllShapeLayers()
	{
		return categories.values().stream().sorted().map(sl -> sl.layer)
				.collect(Collectors.toList());
	}
	
	
	/**
	 * @param inverted
	 */
	public void setInverted(final boolean inverted)
	{
		categories.values().forEach(sl -> sl.shapes.forEach(l -> l.inverted = inverted));
	}
	
	
	/**
	 * @param g
	 * @param tool
	 * @param defaultStroke
	 */
	public void paint(final Graphics2D g, final IDrawableTool tool, final Stroke defaultStroke)
	{
		categories.values().stream()
				.sorted()
				.forEach(sl -> sl.shapes
						.forEach(s -> s.shapes
								.forEach(ss -> {
									Graphics2D gDerived = (Graphics2D) g.create();
									gDerived.setStroke(defaultStroke);
									ss.paintShape(gDerived, tool, s.inverted);
									gDerived.dispose();
								})));
	}
	
	
	/**
	 * @param persistDebugShapes should debug shapes be persisted?
	 */
	public static void setPersistDebugShapes(final boolean persistDebugShapes)
	{
		ShapeMap.persistDebugShapes = persistDebugShapes;
	}
}
