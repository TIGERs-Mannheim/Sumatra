/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.rectangle;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


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
	public IVector2 getCorner(final ECorner pos)
	{
		return getCorners().get(pos.getIndex());
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		IVector2 relPoint = point.subtractNew(center());
		double xw = xExtent() / 2.0;
		double yw = yExtent() / 2.0;
		return relPoint.x() >= -xw && relPoint.x() <= xw && relPoint.y() >= -yw && relPoint.y() <= yw;
	}


	@Override
	public boolean isCircleInShape(final ICircle circle)
	{
		return SumatraMath.isBetween(circle.center().x() + circle.radius(), minX(), maxX())
				&& SumatraMath.isBetween(circle.center().x() - circle.radius(), minX(), maxX())
				&& SumatraMath.isBetween(circle.center().y() + circle.radius(), minY(), maxY())
				&& SumatraMath.isBetween(circle.center().y() - circle.radius(), minY(), maxY());
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
	public IVector2 nearestPointOnPerimeterPath(final IVector2 point)
	{
		double x;
		boolean onXEdge;
		if (point.x() < minX())
		{
			x = minX();
			onXEdge = true;
		} else if (point.x() > maxX())
		{
			onXEdge = true;
			x = maxX();
		} else
		{
			x = point.x();
			onXEdge = false;
		}

		double y;
		boolean onYEdge;
		if (point.y() > maxY())
		{
			onYEdge = true;
			y = maxY();
		} else if (point.y() < minY())
		{
			onYEdge = true;
			y = minY();
		} else
		{
			y = point.y();
			onYEdge = false;
		}

		if (!onXEdge && !onYEdge)
		{
			var distX = point.x() < center().x() ? minX() - point.x() : maxX() - point.x();
			var distY = point.y() < center().y() ? minY() - point.y() : maxY() - point.y();
			if (Math.abs(distX) <= Math.abs(distY))
			{
				x += distX;
			} else
			{
				y += distY;
			}
		}
		return Vector2f.fromXY(x, y);
	}


	@Override
	public IVector2 nearestPointInside(IVector2 point)
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
	public IVector2 nearestPointInside(IVector2 point, IVector2 pointToBuildLine)
	{
		if (isPointInShape(point))
		{
			return point;
		}
		return point.nearestToOpt(intersectPerimeterPath(Lines.lineFromPoints(point, pointToBuildLine)))
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
	public List<ILineSegment> getEdges()
	{
		List<ILineSegment> lines = new ArrayList<>(4);
		List<IVector2> corners = getCorners();

		for (int i = 0; i < 4; i++)
		{
			int j = (i + 1) % 4;
			lines.add(Lines.segmentFromPoints(corners.get(i), corners.get(j)));
		}

		return lines;
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		return getEdges().stream().map(IBoundedPath.class::cast).toList();
	}


	@Override
	public double getPerimeterLength()
	{
		return 2 * yExtent() + 2 * xExtent();
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
