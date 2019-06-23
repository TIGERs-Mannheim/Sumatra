/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2015
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;


/**
 * TODO dirk, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author dirk
 * @param <T>
 */

public class FieldHash<T>
{
	private List<MultiValueMap>	positionHashs	= new ArrayList<MultiValueMap>();
	
	private final int					binSize;
	
	
	/**
	 * @param binSize
	 */
	public FieldHash(final int binSize)
	{
		this.binSize = binSize;
		for (int i = 0; i < 4; i++)
		{
			MultiValueMap map = new MultiValueMap();
			MultiValueMap.decorate(new HashMap<Integer, T>());
			positionHashs.add(map);
		}
	}
	
	
	/**
	 * TODO dirk, add comment!
	 * 
	 * @param x
	 * @param y
	 * @param object
	 */
	public void add(final float x, final float y, final T object)
	{
		// feeding the four grids with positions of the bots
		for (int i = 0; i < 4; i++)
		{
			positionHashs.get(i).put(hash(x, y, i), object);
		}
	}
	
	
	/**
	 * find not only the entries in the bin given by x and y but search in the surrounding bins as long as a something is
	 * found.
	 * DOES NOT WORK
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	// public Collection<T> findExtensively(final float x, final float y)
	// {
	// Collection<T> returnCollection = find(x, y);
	// int dist = 0;
	// while ((returnCollection == null) && ((dist * binSize) < AIConfig.getGeometry().getFieldLength()))
	// {
	// dist++;
	// for (int i = -dist; i < dist; i++)
	// {
	// for (int j = -dist; j < dist; j++)
	// {
	// Collection<T> elems = find(x + (dist * binSize), y + (dist * binSize));
	// if (returnCollection != null)
	// {
	// returnCollection.addAll(elems);
	// } else
	// {
	// returnCollection = elems;
	// }
	// }
	// }
	// }
	// if (returnCollection == null)
	// {
	// returnCollection = Collections.emptyList();
	// }
	// return returnCollection;
	// }
	
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<T> find(final float x, final float y)
	{
		Set<T> returnCollection = new HashSet<T>();
		// feeding the four grids with positions of the bots
		for (int i = 0; i < 4; i++)
		{
			Collection<T> elems = positionHashs.get(i).getCollection(hash(x, y, i));
			
			if (elems != null)
			{
				Set<T> elemsSet = new HashSet<T>(elems);
				returnCollection.addAll(elemsSet);
				// if (returnCollection != null)
				// {
				// returnCollection.addAll(elems);
				// } else
				// {
				// returnCollection = elems;
				// }
			}
		}
		return returnCollection;
	}
	
	
	private int hash(final float x, final float y, final int i)
	{
		// offset for the four different grids
		int offset_x = (binSize / 2) * (i % 2);
		int offset_y = 0;
		if (i > 2)
		{
			offset_y = (binSize / 2);
		}
		
		// calculate the bin matching to the x and y combination given grid number i
		int a = (int) Math.ceil((x + offset_x) / binSize);
		int b = (int) Math.ceil((y + offset_y) / binSize);
		
		// convert to only positive values
		a = convertToOnlyPositives(a);
		b = convertToOnlyPositives(b);
		
		// Hashing function (Cantors pairing function)
		return (int) ((1f / 2f) * (((a + b) * (a + b + 1)))) + b;
	}
	
	
	private int convertToOnlyPositives(final int possibleNegative)
	{
		if (possibleNegative < 0)
		{
			return (-possibleNegative * 2) + 1;
		}
		return (possibleNegative * 2);
	}
}
