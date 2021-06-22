/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;


/**
 * A source for a shape map, with a name and an optional list of categories.
 */
@Persistent
public final class ShapeMapSource implements Comparable<ShapeMapSource>
{
	private final String name;
	private final Set<String> categories;


	@SuppressWarnings("unused") // for berkeley
	private ShapeMapSource()
	{
		name = null;
		categories = Collections.emptySortedSet();
	}


	private ShapeMapSource(final String name, final Set<String> categories)
	{
		this.name = name;
		this.categories = categories;
	}


	public static ShapeMapSource of(final String name, final String... category)
	{
		final Set<String> categories = new HashSet<>(Arrays.asList(category));
		return new ShapeMapSource(name, categories);
	}


	public String getName()
	{
		return name;
	}


	public Set<String> getCategories()
	{
		return Collections.unmodifiableSet(categories);
	}


	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final ShapeMapSource that = (ShapeMapSource) o;

		return new EqualsBuilder()
				.append(name, that.name)
				.append(categories, that.categories)
				.isEquals();
	}


	@Override
	public final int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(name)
				.append(categories)
				.toHashCode();
	}


	@Override
	public int compareTo(final ShapeMapSource o)
	{
		return ObjectUtils.compare(name, o.name);
	}
}
