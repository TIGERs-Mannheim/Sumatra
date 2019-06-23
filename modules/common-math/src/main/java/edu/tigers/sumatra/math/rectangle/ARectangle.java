/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * This abstract class represents a rectangle. It is used i. e. to describe a part-rectangle of the field.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, MalteM
 */
@Persistent
abstract class ARectangle implements IRectangle
{
	ARectangle()
	{
	}
	
	
	@Override
	public double maxX()
	{
		return center().x() + (xExtent() / 2.0);
	}
	
	
	@Override
	public double minX()
	{
		return center().x() - (xExtent() / 2.0);
	}
	
	
	@Override
	public double maxY()
	{
		return center().y() + (yExtent() / 2.0);
	}
	
	
	@Override
	public double minY()
	{
		return center().y() - (yExtent() / 2.0);
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		double halfWidth = xExtent() / 2.0;
		double halfHeight = yExtent() / 2.0;
		List<IVector2> corners = new ArrayList<>(4);
		corners.add(Vector2.fromXY(-halfWidth, -halfHeight).add(center()));
		corners.add(Vector2.fromXY(-halfWidth, +halfHeight).add(center()));
		corners.add(Vector2.fromXY(+halfWidth, +halfHeight).add(center()));
		corners.add(Vector2.fromXY(+halfWidth, -halfHeight).add(center()));
		return corners;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return withMargin(margin).isPointInShape(point);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return SumatraMath.isBetween(point.x(), minX(), maxX())
				&& SumatraMath.isBetween(point.y(), minY(), maxY());
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (isPointInShape(point))
		{
			IVector2 nearestPoint;
			double distance;
			
			// left
			distance = Math.abs(point.x() - minX());
			nearestPoint = Vector2f.fromXY(minX(), point.y());
			
			// right
			if (distance > Math.abs(maxX() - point.x()))
			{
				distance = maxX() - point.x();
				nearestPoint = Vector2f.fromXY(maxX(), point.y());
			}
			
			// top
			if (distance > Math.abs(point.y() - minY()))
			{
				distance = point.y() - minY();
				nearestPoint = Vector2f.fromXY(point.x(), minY());
			}
			
			// bottom
			if (distance > Math.abs(maxY() - point.y()))
			{
				nearestPoint = Vector2f.fromXY(point.x(), maxY());
			}
			
			return nearestPoint;
		}
		
		// else return point
		return point;
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		double x;
		if (point.x() < minX())
		{
			x = minX();
		} else if (point.x() > maxX())
		{
			x = maxX();
		} else
		{
			x = point.x();
		}
		
		double y;
		if (point.y() > maxY())
		{
			y = maxY();
		} else if (point.y() < minY())
		{
			y = minY();
		} else
		{
			y = point.y();
		}
		
		return Vector2f.fromXY(x, y);
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point, final double margin)
	{
		return withMargin(margin).nearestPointInside(point);
	}
	
	
	@Override
	public IVector2 nearestPointInside(IVector2 point, IVector2 pointToBuildLine)
	{
		if (isPointInShape(point))
		{
			return point;
		}
		return point.nearestToOpt(lineIntersections(Line.fromPoints(point, pointToBuildLine)))
				.orElse(point);
	}
	
	
	@Override
	public IVector2 getRandomPointInShape(final Random rnd)
	{
		double x = (center().x() + (rnd.nextDouble() * xExtent())) - (xExtent() / 2);
		double y = (center().y() + (rnd.nextDouble() * yExtent())) - (yExtent() / 2);
		return Vector2f.fromXY(x, y);
	}
	
	
	@Override
	public List<ILine> getEdges()
	{
		List<ILine> lines = new ArrayList<>(4);
		List<IVector2> corners = getCorners();
		
		for (int i = 0; i < 4; i++)
		{
			int j = (i + 1) % 4;
			lines.add(Line.fromPoints(corners.get(i), corners.get(j)));
		}
		
		return lines;
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return getEdges().stream()
				.map(edge -> edge.intersectionWith(line))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(this::isPointInShape)
				.distinct()
				.collect(Collectors.toList());
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<>();
		jsonMapping.put("center", center().toJSONArray());
		jsonMapping.put("extent", Vector2f.fromXY(xExtent(), yExtent()).toJSONArray());
		return new JSONObject(jsonMapping);
	}
}
