/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class SimTimeBlocker implements IWorldFrameObserver
{
	private final CountDownLatch latch = new CountDownLatch(1);
	private final double duration;
	private Long startTime;
	
	
	public SimTimeBlocker(final double duration)
	{
		this.duration = duration;
	}
	
	
	public void start()
	{
		try
		{
			AWorldPredictor wic = SumatraModel.getInstance()
					.getModule(AWorldPredictor.class);
			wic.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			fail("Could not find module.", e);
		}
	}
	
	
	public void stop()
	{
		try
		{
			AWorldPredictor wic = SumatraModel.getInstance()
					.getModule(AWorldPredictor.class);
			wic.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			fail("Could not find module.", e);
		}
	}
	
	
	@SuppressWarnings("squid:S899") // Return value is not relevant
	public void await()
	{
		start();
		try
		{
			latch.await(20, TimeUnit.MINUTES);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} finally
		{
			stop();
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		long timestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
		if (startTime == null)
		{
			startTime = timestamp;
		}
		if ((timestamp - startTime) / 1e9 > duration)
		{
			latch.countDown();
			stop();
			
		}
	}
}
