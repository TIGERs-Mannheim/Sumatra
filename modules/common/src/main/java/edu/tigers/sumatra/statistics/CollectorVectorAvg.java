/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.VectorN;
import edu.tigers.sumatra.statistics.CollectorVectorAvg.Accumulator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CollectorVectorAvg implements Collector<IVector, Accumulator, VectorN>
{
	private final int	dim;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param dim
	 */
	public CollectorVectorAvg(final int dim)
	{
		this.dim = dim;
	}
	
	
	@Override
	public Supplier<Accumulator> supplier()
	{
		return Accumulator::new;
	}
	
	
	@Override
	public BiConsumer<Accumulator, IVector> accumulator()
	{
		return (acc, vec) -> {
			acc.vector.add(vec);
			acc.count++;
		};
	}
	
	
	@Override
	public BinaryOperator<Accumulator> combiner()
	{
		return (v1, v2) -> {
			Accumulator acc = new Accumulator();
			acc.vector = v1.vector.addNew(v2.vector);
			acc.count = v1.count + v2.count;
			return acc;
		};
	}
	
	
	@Override
	public Function<Accumulator, VectorN> finisher()
	{
		return acc -> acc.count == 0 ? new VectorN(dim) : acc.vector.multiplyNew(1f / acc.count);
	}
	
	
	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics()
	{
		return EnumSet.of(Characteristics.UNORDERED);
	}
	
	protected class Accumulator
	{
		VectorN	vector	= new VectorN(dim);
		int		count		= 0;
	}
}
