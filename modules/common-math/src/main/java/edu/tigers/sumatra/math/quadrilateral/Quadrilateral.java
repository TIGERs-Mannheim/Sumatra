/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorAngleComparator;


/**
 * Implementation of a quadrilateral ("Viereck") defined by 4 corners and backed by 2 triangles.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public final class Quadrilateral extends AQuadrilateral
{
	
	private final List<IVector2> corners = new ArrayList<>(4);
	
	
	@SuppressWarnings("unused")
	private Quadrilateral()
	{
	}
	
	
	private Quadrilateral(final List<IVector2> corners)
	{
		// make sure we use an array list, which is persistable
		this.corners.addAll(corners);
	}
	
	
	/**
	 * Create a quadrilateral by its 4 corners
	 * 
	 * @param a corner a
	 * @param b corner b
	 * @param c corner c
	 * @param d corner d
	 * @return a new quadrilateral
	 */
	public static IQuadrilateral fromCorners(final IVector2 a, final IVector2 b, final IVector2 c, final IVector2 d)
	{
		Validate.notNull(a);
		Validate.notNull(b);
		Validate.notNull(c);
		Validate.notNull(d);
		List<IVector2> corners = new ArrayList<>(4);
		corners.add(a);
		corners.add(b);
		corners.add(c);
		corners.add(d);
		return fromCorners(corners);
	}
	
	
	/**
	 * Create a quadrilateral by its 4 corners
	 * 
	 * @param points 4 points that build the the quadrilateral
	 * @return a new quadrilateral
	 */
	public static IQuadrilateral fromCorners(final List<IVector2> points)
	{
		Validate.isTrue(points.size() == 4, "Exactly 4 corners required!");
		IVector2 center = points.stream()
				.reduce(IVector2::addNew)
				.orElseThrow(IllegalStateException::new)
				.multiplyNew(1.0 / 4.0);
		List<IVector2> sortedCorners = points.stream()
				.map(v -> v.subtractNew(center))
				.sorted(new VectorAngleComparator())
				.map(v -> v.addNew(center))
				.collect(Collectors.toList());
		return new Quadrilateral(sortedCorners);
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		return Collections.unmodifiableList(corners);
	}
	
	
	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final Quadrilateral that = (Quadrilateral) o;
		
		return new EqualsBuilder()
				.append(corners, that.corners)
				.isEquals();
	}
	
	
	@Override
	public final int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(corners)
				.toHashCode();
	}
}
