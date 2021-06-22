/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Test;

import junit.framework.AssertionFailedError;


public class DataSyncTest
{

	@Test
	public void add()
	{
		DataSync<Data> ds = new DataSync<>(10);
		ds.add(1, new Data(10.0));
		ds.add(3, new Data(20.0));
		ds.add(5, new Data(30.0));

		assertThat(ds.get(0)).isNotPresent();
		assertThat(ds.get(1)).isPresent();
		assertThat(ds.get(2)).isPresent();
		assertThat(ds.get(3)).isPresent();
		assertThat(ds.get(5)).isPresent();
		assertThat(ds.get(6)).isNotPresent();
		DataSync<Data>.DataPair dp1 = ds.get(1).orElseThrow(AssertionFailedError::new);
		assertThat(dp1.getFirst().getTimestamp()).isEqualTo(1L);
		assertThat(dp1.getSecond().getTimestamp()).isEqualTo(3L);
		assertThat(dp1.getFirst().getData()).isEqualTo(new Data(10.0));
		assertThat(dp1.getSecond().getData()).isEqualTo(new Data(20.0));

		ds.add(0, new Data(50.0));
		assertThat(ds.get(1)).isNotPresent();
		assertThat(ds.get(0)).isNotPresent();
	}


	@Test
	public void getLatest()
	{
		DataSync<Data> ds = new DataSync<>(10);
		ds.add(1, new Data(10.0));
		ds.add(3, new Data(20.0));
		ds.add(5, new Data(30.0));

		Optional<DataSync<Data>.DataStore> latest = ds.getLatest();
		assertThat(latest).isPresent();
		assertThat(latest.get().getTimestamp()).isEqualTo(5);
		assertThat(latest.get().getData()).isEqualTo(new Data(30.0));
	}

	private static class Data implements IInterpolatable<Data>
	{
		double value;

		public Data(final double value)
		{
			this.value = value;
		}


		@Override
		public Data interpolate(final Data other, final double percentage)
		{
			double diff = other.value - value;
			return new Data(value + diff * percentage);
		}


		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;

			if (o == null || getClass() != o.getClass())
				return false;

			final Data data = (Data) o;

			return new EqualsBuilder()
					.append(value, data.value)
					.isEquals();
		}


		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(value)
					.toHashCode();
		}
	}
}