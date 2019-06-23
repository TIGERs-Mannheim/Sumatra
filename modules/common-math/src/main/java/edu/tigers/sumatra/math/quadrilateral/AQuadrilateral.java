/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class AQuadrilateral implements IQuadrilateral
{
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return getTriangles().stream().anyMatch(t -> t.isPointInShape(point));
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return getTriangles().stream().anyMatch(t -> t.isPointInShape(point, margin));
	}
	
	
	@Override
	public List<ITriangle> getTriangles()
	{
		List<IVector2> corners = getCorners();
		List<ITriangle> triangles = new ArrayList<>(2);
		triangles.add(Triangle.fromCorners(corners.get(0), corners.get(1), corners.get(2)));
		triangles.add(Triangle.fromCorners(corners.get(3), corners.get(0), corners.get(2)));
		return triangles;
	}
	
	
	@Override
	public List<ILine> getEdges()
	{
		List<IVector2> corners = getCorners();
		List<ILine> edges = new ArrayList<>(4);
		for (int i = 0; i < 4; i++)
		{
			edges.add(Line.fromPoints(corners.get(i), corners.get((i + 1) % 4)));
		}
		return edges;
	}
}
