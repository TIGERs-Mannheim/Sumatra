/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import junit.framework.AssertionFailedError;


public class DataSyncTest
{
	
	@Test
	public void add()
	{
		DataSync<Double> ds = new DataSync<>(10);
		ds.add(1, 10.0);
		ds.add(3, 20.0);
		ds.add(5, 30.0);
		
		assertThat(ds.get(0)).isNotPresent();
		assertThat(ds.get(1)).isPresent();
		assertThat(ds.get(2)).isPresent();
		assertThat(ds.get(3)).isPresent();
		assertThat(ds.get(5)).isPresent();
		assertThat(ds.get(6)).isNotPresent();
		DataSync<Double>.DataPair dp1 = ds.get(1).orElseThrow(AssertionFailedError::new);
		assertThat(dp1.getFirst().getTimestamp()).isEqualTo(1L);
		assertThat(dp1.getSecond().getTimestamp()).isEqualTo(3L);
		assertThat(dp1.getFirst().getData()).isEqualTo(10.0);
		assertThat(dp1.getSecond().getData()).isEqualTo(20.0);
		
		ds.add(0, 50.0);
		assertThat(ds.get(1)).isNotPresent();
		assertThat(ds.get(0)).isNotPresent();
	}
	
	
	@Test
	public void getLatest()
	{
		DataSync<Double> ds = new DataSync<>(10);
		ds.add(1, 10.0);
		ds.add(3, 20.0);
		ds.add(5, 30.0);
		
		Optional<DataSync<Double>.DataStore> latest = ds.getLatest();
		assertThat(latest).isPresent();
		assertThat(latest.get().getTimestamp()).isEqualTo(5);
		assertThat(latest.get().getData()).isEqualTo(30.0);
	}
}