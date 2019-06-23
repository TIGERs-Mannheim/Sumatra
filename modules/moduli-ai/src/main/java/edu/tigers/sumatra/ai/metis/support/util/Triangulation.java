/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.util;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A 2D Delaunay Triangulation (DT) with incremental site insertion.
 * This is not the fastest way to build a DT, but it's a reasonable way to build
 * a DT incrementally and it makes a nice interactive display. There are several
 * O(n log n) methods, but they require that the sites are all known initially.
 * A Triangulation is a Set of Triangles. A Triangulation is unmodifiable as a
 * Set; the only way to change it is to add sites (via delaunayPlace).
 *
 * @author Paul Chew
 *         Created July 2005. Derived from an earlier, messier version.
 *         Modified November 2007. Rewrote to use AbstractSet as parent class and to use
 *         the Graph class internally. Tried to make the DT algorithm clearer by
 *         explicitly creating a cavity. Added code needed to find a Voronoi cell.
 */
public class Triangulation extends AbstractSet<ITriangle>
{
	
	private ITriangle mostRecent = null; // Most recently "active" triangle
	private Graph<ITriangle> triGraph; // Holds triangles for navigation
	
	private static Logger logger = Logger.getLogger(Triangulation.class);
	
	
	/**
	 * All sites must fall within the initial triangle.
	 * 
	 * @param triangle the initial triangle
	 */
	public Triangulation(ITriangle triangle)
	{
		triGraph = new Graph<>();
		triGraph.add(triangle);
		mostRecent = triangle;
	}
	
	
	/* The following two methods are required by AbstractSet */
	
	@Override
	public Iterator<ITriangle> iterator()
	{
		return triGraph.nodeSet().iterator();
	}
	
	
	@Override
	public int size()
	{
		return triGraph.nodeSet().size();
	}
	
	
	@Override
	public String toString()
	{
		return "Triangulation with " + size() + " triangles";
	}
	
	
	/**
	 * True iff triangle is a member of this triangulation.
	 * This method isn't required by AbstractSet, but it improves efficiency.
	 * 
	 * @param triangle the object to check for membership
	 */
	@Override
	public boolean contains(Object triangle)
	{
		return triGraph.nodeSet().contains(triangle);
	}
	
	
	/**
	 * Report neighbor opposite the given vertex of triangle.
	 * 
	 * @param site a vertex of triangle
	 * @param triangle we want the neighbor of this triangle
	 * @return the neighbor opposite site in triangle; null if none
	 * @throws IllegalArgumentException if site is not in this triangle
	 */
	private ITriangle neighborOpposite(IVector2 site, ITriangle triangle)
	{
		for (ITriangle neighbor : triGraph.neighbors(triangle))
		{
			for (IVector2 corner : neighbor.getCorners())
			{
				if (site == corner)
				{
					break;
				}
			}
			if (!neighbor.isPointInShape(site))
				return neighbor;
		}
		return null;
	}
	
	
	/**
	 * Return the set of triangles adjacent to triangle.
	 * 
	 * @param triangle the triangle to check
	 * @return the neighbors of triangle
	 */
	public Set<ITriangle> neighbors(ITriangle triangle)
	{
		return triGraph.neighbors(triangle);
	}
	
	
	/**
	 * Locate the triangle with point inside it or on its boundary.
	 * 
	 * @param point the point to locate
	 * @return the triangle that holds point; null if no such triangle
	 */
	private ITriangle locate(IVector2 point)
	{
		ITriangle triangle = mostRecent;
		if (!this.contains(triangle))
			triangle = null;
		
		// Try a directed walk (this works fine in 2D, but can fail in 3D)
		Set<ITriangle> visited = new HashSet<>();
		while (triangle != null)
		{
			if (visited.contains(triangle))
			{
				break;
			}
			visited.add(triangle);
			if (triangle.isPointInShape(point))
				return triangle;
			// Corner opposite point
			
			Optional<IVector2> lineA = Lines.lineFromPoints(triangle.getC(), triangle.getB())
					.intersectSegment(Lines.segmentFromPoints(triangle.getA(), point));
			
			Optional<IVector2> lineB = Lines.lineFromPoints(triangle.getC(), triangle.getA())
					.intersectSegment(Lines.segmentFromPoints(triangle.getB(), point));
			
			Optional<IVector2> lineC = Lines.lineFromPoints(triangle.getA(), triangle.getB())
					.intersectSegment(Lines.segmentFromPoints(triangle.getC(), point));
			if (lineA.isPresent())
			{
				triangle = this.neighborOpposite(triangle.getA(), triangle);
			} else if (lineB.isPresent())
			{
				triangle = this.neighborOpposite(triangle.getB(), triangle);
			} else if (lineC.isPresent())
			{
				triangle = this.neighborOpposite(triangle.getC(), triangle);
			}
		}
		
		// No luck; try brute force
		for (ITriangle tri : this)
		{
			if (tri.withMargin(1).isPointInShape(point))
				return tri;
		}
		// No such triangle
		logger.warn("Warning: No triangle holds " + point);
		return null;
	}
	
	
	/**
	 * Place a new site into the DT.
	 * Nothing happens if the site matches an existing DT vertex.
	 * 
	 * @param site the new Pnt
	 * @throws IllegalArgumentException if site does not lie in any triangle
	 */
	public void delaunayPlace(IVector2 site)
	{
		// Uses straightforward scheme rather than best asymptotic time
		
		// Locate containing triangle
		ITriangle triangle = locate(site);
		// Give up if no containing triangle or if site is already in DT
		if (triangle == null)
			throw new IllegalArgumentException("No containing triangle");
		if (!triangle.isPointInShape(site))
			return;
		
		// Determine the cavity and update the triangulation
		Set<ITriangle> cavity = getCavity(site, triangle);
		mostRecent = update(site, cavity);
	}
	
	
	/**
	 * Determine the cavity caused by site.
	 * 
	 * @param site the site causing the cavity
	 * @param triangle the triangle containing site
	 * @return set of all triangles that have site in their circumcircle
	 */
	private Set<ITriangle> getCavity(IVector2 site, ITriangle triangle)
	{
		Set<ITriangle> encroached = new HashSet<>();
		Queue<ITriangle> toBeChecked = new LinkedList<>();
		Set<ITriangle> marked = new HashSet<>();
		toBeChecked.add(triangle);
		marked.add(triangle);
		while (!toBeChecked.isEmpty())
		{
			triangle = toBeChecked.remove();
			Optional<ICircle> circle = Circle.fromNPoints(triangle.getCorners());
			if (!circle.isPresent() || !circle.get().isPointInShape(site))
				continue; // Site outside triangle => triangle not in cavity
			encroached.add(triangle);
			// Check the neighbors
			for (ITriangle neighbor : triGraph.neighbors(triangle))
			{
				if (marked.contains(neighbor))
					continue;
				marked.add(neighbor);
				toBeChecked.add(neighbor);
			}
		}
		return encroached;
	}
	
	
	/**
	 * Update the triangulation by removing the cavity triangles and then
	 * filling the cavity with new triangles.
	 * 
	 * @param site the site that created the cavity
	 * @param cavity the triangles with site in their circumcircle
	 * @return one of the new triangles
	 */
	private ITriangle update(IVector2 site, Set<ITriangle> cavity)
	{
		Set<Set<IVector2>> boundary = new HashSet<>();
		Set<ITriangle> theTriangles = new HashSet<>();
		
		// Find boundary facets and adjacent triangles
		for (ITriangle triangle : cavity)
		{
			theTriangles.addAll(neighbors(triangle));
			for (IVector2 vertex : triangle.getCorners())
			{
				Set<IVector2> facet = new HashSet<>();
				facet.addAll(triangle.getCorners());
				facet.remove(vertex);
				if (boundary.contains(facet))
					boundary.remove(facet);
				else
					boundary.add(facet);
			}
		}
		theTriangles.removeAll(cavity); // Adj triangles only
		
		// Remove the cavity triangles from the triangulation
		for (ITriangle triangle : cavity)
			triGraph.remove(triangle);
		
		// Build each new triangle and add it to the triangulation
		Set<ITriangle> newTriangles = buildNewTriangles(site, boundary);
		
		if (newTriangles.isEmpty())
		{
			return this.iterator().next();
		}
		
		// Update the graph links for each new triangle
		theTriangles.addAll(newTriangles); // Adj triangle + new triangles
		for (ITriangle triangle : newTriangles)
			for (ITriangle other : theTriangles)
				if (triangle.isNeighbour(other))
					triGraph.add(triangle, other);
				
		// Return one of the new triangles
		return newTriangles.iterator().next();
	}
	
	
	private Set<ITriangle> buildNewTriangles(IVector2 site, Set<Set<IVector2>> boundary)
	{
		Set<ITriangle> newTriangles = new HashSet<>();
		
		for (Set<IVector2> vertices : boundary)
		{
			vertices.add(site);
			if (vertices.size() == 3)
			{
				Iterator<IVector2> iterator = vertices.iterator();
				Triangle tri = Triangle.fromCorners(iterator.next(), iterator.next(), iterator.next());
				triGraph.add(tri);
				newTriangles.add(tri);
			}
		}
		return newTriangles;
	}
	
}