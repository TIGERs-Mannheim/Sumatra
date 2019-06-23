/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statistics;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.VectorN;
import edu.tigers.sumatra.statistics.CollectorVectorStd.Accumulator;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CollectorVectorStd implements Collector<IVectorN, Accumulator, VectorN>
{
	private final IVectorN avg;
	
	
	/**
	 * @param avg
	 */
	public CollectorVectorStd(final IVectorN avg)
	{
		this.avg = avg;
	}
	
	
	@Override
	public Supplier<Accumulator> supplier()
	{
		return Accumulator::new;
	}
	
	
	@Override
	public BiConsumer<Accumulator, IVectorN> accumulator()
	{
		return (acc, vec) ->
		{
			IVectorN tmp = vec.subtractNew(avg);
			acc.vector.add(tmp.multiplyNew(tmp));
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
		return acc -> acc.count == 0 ? VectorN.zero(avg.getNumDimensions()) : acc.vector.multiplyNew(1f / acc.count)
				.applyNew(
								SumatraMath::sqrt);
	}
	
	
	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics()
	{
		return EnumSet.of(Characteristics.UNORDERED);
	}
	
	protected class Accumulator
	{
		VectorN	vector	= VectorN.zero(avg.getNumDimensions());
		int		count		= 0;
	}
}
