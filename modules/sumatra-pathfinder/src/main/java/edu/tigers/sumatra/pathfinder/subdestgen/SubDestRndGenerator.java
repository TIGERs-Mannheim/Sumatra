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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


public class SubDestRndGenerator implements SubDestGenerator
{
	private static final double MIN_ANGLE = AngleMath.deg2rad(20);
	private static final double MAX_ANGLE = AngleMath.deg2rad(140);
	private static final double MIN_SCALE = 0.5;
	private static final double MAX_SCALE = 1.5;
	private static final int NUM_SUB_DESTINATIONS = 5;

	private final Random rnd = new Random();
	private IVector2 lastNormalizedSubDest;


	private List<IVector2> generateNormalizedSubDestinations()
	{
		List<IVector2> list = new ArrayList<>();
		if (lastNormalizedSubDest != null)
		{
			list.add(lastNormalizedSubDest);
		}
		for (int i = 0; i < NUM_SUB_DESTINATIONS; i++)
		{
			double angleRange = MAX_ANGLE - MIN_ANGLE;
			double angle = rnd.nextDouble(-angleRange, angleRange);
			angle += Math.signum(angle) * MIN_ANGLE;
			double scale = rnd.nextDouble(MIN_SCALE, MAX_SCALE);
			list.add(Vector2.fromAngleLength(angle, scale));
		}
		list.sort(Comparator.comparing(d -> Math.abs(d.getAngle())));
		return Collections.unmodifiableList(list);
	}


	@Override
	public Iterator<IVector2> subDestIterator(PathFinderInput input)
	{
		IVector2 startToDest = input.getDest().subtractNew(input.getPos());
		double startToDestDist = startToDest.getLength2();
		double startToDestDir = startToDest.getAngle();
		List<IVector2> subDestNormalized = generateNormalizedSubDestinations();
		return new SubDestIterator(input.getPos(), startToDestDist, startToDestDir, subDestNormalized.iterator());
	}


	@RequiredArgsConstructor
	private class SubDestIterator implements Iterator<IVector2>
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


			IVector2 next = iterator.next();
			lastNormalizedSubDest = next;
			return next.turnNew(startToDestDir).multiply(startToDestDist).add(start);
		}
	}
}
