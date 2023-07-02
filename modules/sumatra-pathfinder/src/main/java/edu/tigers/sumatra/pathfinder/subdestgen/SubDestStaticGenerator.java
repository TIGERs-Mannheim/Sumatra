/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.subdestgen;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class SubDestStaticGenerator implements SubDestGenerator
{
	private final List<IVector2> subDestNormalized;


	public SubDestStaticGenerator()
	{
		subDestNormalized = generateNormalizedSubDestinations();
	}


	private List<IVector2> generateNormalizedSubDestinations()
	{
		List<IVector2> list = new ArrayList<>();
		list.addAll(normalizedSubDestinations(1.5, 20));
		list.addAll(normalizedSubDestinations(1.5, 40));
		list.addAll(normalizedSubDestinations(1.5, 60));
		list.addAll(normalizedSubDestinations(1.5, 80));
		list.addAll(normalizedSubDestinations(1.5, 100));
		list.addAll(normalizedSubDestinations(1.5, 120));
		list.addAll(normalizedSubDestinations(1.0, 140));
		return Collections.unmodifiableList(list);
	}


	@Override
	public Iterator<IVector2> subDestIterator(PathFinderInput input)
	{
		IVector2 startToDest = input.getDest().subtractNew(input.getPos());
		double startToDestDist = startToDest.getLength2();
		double startToDestDir = startToDest.getAngle();
		return new SubDestIterator(input.getPos(), startToDestDist, startToDestDir, subDestNormalized.iterator());
	}


	private List<IVector2> normalizedSubDestinations(double scale, double angleDeg)
	{
		return List.of(
				Vector2.fromAngleLength(AngleMath.deg2rad(angleDeg), scale),
				Vector2.fromAngleLength(AngleMath.deg2rad(-angleDeg), scale)
		);
	}


	@RequiredArgsConstructor
	private static class SubDestIterator implements Iterator<IVector2>
	{
		final IVector2 start;
		final double startToDestDist;
		final double startToDestDir;
		final Iterator<IVector2> iterator;


		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}


		@Override
		public IVector2 next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}

			double dir = startToDestDir + Math.random() * 0.6 - 0.3;
			double dist = startToDestDist + Math.random() * 200 - 100;
			return iterator.next().turnNew(dir).multiply(dist).add(start);
		}
	}
}
