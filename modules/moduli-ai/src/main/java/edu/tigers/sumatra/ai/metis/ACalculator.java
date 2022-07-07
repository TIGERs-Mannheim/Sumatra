/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * abstract superclass for every subordinal calculator of the Analyzer
 */
@Log4j2
public abstract class ACalculator
{
	@Getter
	private boolean executionStatusLastFrame = false;
	@Getter
	private long lastProcessingTimeNs;

	private boolean started = false;
	private Exception lastException = null;
	private BaseAiFrame aiFrame = null;


	/**
	 * Calculation call from extern
	 *
	 * @param baseAiFrame
	 */
	public final void calculate(BaseAiFrame baseAiFrame)
	{
		long tStart = System.nanoTime();
		aiFrame = baseAiFrame;
		executionStatusLastFrame = false;

		if (!started)
		{
			start();
			reset();
			started = true;
		}

		if (!isCalculationNecessary())
		{
			reset();
			return;
		}

		try
		{
			doCalc();
			executionStatusLastFrame = true;
			lastException = null;
		} catch (Exception err)
		{
			if ((lastException == null) || ((err.getMessage() != null)
					&& !err.getMessage().equals(lastException.getMessage())))
			{
				log.error("Error in calculator " + getClass().getSimpleName(), err);
			}
			lastException = err;
			reset();
		}
		long tEnd = System.nanoTime();
		lastProcessingTimeNs = (tEnd - tStart);
	}


	/**
	 * Specifies if the doCalc method has to be executed
	 *
	 * @return
	 */
	protected boolean isCalculationNecessary()
	{
		return true;
	}


	/**
	 * Reset any state when no calculation is necessary
	 */
	protected void reset()
	{
	}


	/**
	 * Do the calculations.
	 */
	protected void doCalc()
	{
	}


	/**
	 * Called when calculator gets activated or AI gets started
	 */
	protected void start()
	{
		// can be overwritten
	}


	/**
	 * Called when calculator gets deactivated or AI gets stopped
	 */
	protected void stop()
	{
		// can be overwritten
	}


	/**
	 * Tear down this calculator by calling stop() if required
	 */
	public final void tearDown()
	{
		if (started)
		{
			stop();
			started = false;
		}
	}


	protected final BaseAiFrame getAiFrame()
	{
		return aiFrame;
	}


	protected final WorldFrame getWFrame()
	{
		return aiFrame.getWorldFrame();
	}


	protected final ITrackedBall getBall()
	{
		return aiFrame.getWorldFrame().getBall();
	}


	protected final List<IDrawableShape> getShapes(IShapeLayerIdentifier shapeLayer)
	{
		return getAiFrame().getShapeMap().get(shapeLayer);
	}
}
