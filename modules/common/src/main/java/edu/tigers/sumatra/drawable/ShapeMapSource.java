/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * A source for a shape map, with a name and an optional list of categories.
 */
@Value
@Persistent
@RequiredArgsConstructor
public class ShapeMapSource implements Comparable<ShapeMapSource>
{
	String name;
	ShapeMapSource parent;


	@SuppressWarnings("unused") // for berkeley
	private ShapeMapSource()
	{
		name = null;
		parent = null;
	}


	public static ShapeMapSource of(String name)
	{
		return new ShapeMapSource(name, null);
	}


	public static ShapeMapSource of(String name, ShapeMapSource parent)
	{
		return new ShapeMapSource(name, parent);
	}


	@Override
	public int compareTo(ShapeMapSource o)
	{
		int parentCompare = ObjectUtils.compare(parent, o.parent);
		if (parentCompare != 0)
		{
			return parentCompare;
		}
		return ObjectUtils.compare(name, o.name);
	}


	public boolean contains(ShapeMapSource source)
	{
		if (this.equals(source))
		{
			return true;
		}
		if (parent != null)
		{
			return parent.contains(source);
		}
		return false;
	}


	public String getId()
	{
		if (parent == null)
		{
			return name;
		}
		return name + "/" + parent.getId();
	}


	public List<ShapeMapSource> getPath()
	{
		List<ShapeMapSource> path = new ArrayList<>();
		ShapeMapSource source = getParent();
		while (source != null)
		{
			path.add(0, source);
			source = source.getParent();
		}
		return path;
	}
}
