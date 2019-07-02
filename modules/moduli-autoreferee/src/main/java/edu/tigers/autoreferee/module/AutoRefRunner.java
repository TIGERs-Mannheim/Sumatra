/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.module;

import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.AutoRefFramePreprocessor;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.AutoRefEngine;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.IAutoRefEngineObserver;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Start the autoRef in a new thread and run an engine respective to the {@link EAutoRefMode}.
 */
public class AutoRefRunner implements Runnable, IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(AutoRefRunner.class);

	private final BlockingDeque<WorldFrameWrapper> consumableFrames = new LinkedBlockingDeque<>(1);
	private final AutoRefFramePreprocessor preprocessor = new AutoRefFramePreprocessor();
	private final Set<EGameEventDetectorType> activeDetectors = EGameEventDetectorType.valuesEnabledByDefault();

	private ExecutorService executorService;
	private AutoRefEngine engine = new AutoRefEngine(activeDetectors);
	private final IAutoRefEngineObserver callback;
	private EAutoRefMode mode = EAutoRefMode.OFF;
	private final Object engineSync = new Object();


	public AutoRefRunner(IAutoRefEngineObserver callback)
	{
		this.callback = callback;
	}


	public void setDetectorActive(final EGameEventDetectorType type, boolean active)
	{
		if (active)
		{
			activeDetectors.add(type);
		} else
		{
			activeDetectors.remove(type);
		}
	}


	/**
	 * Start the auto referee runner with an inactive engine
	 */
	public void start()
	{
		// make sure, the engine is initially in a clean off state
		changeMode(EAutoRefMode.OFF);
		// register to WP frames
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		// start runner thread
		executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("AutoRef"));
		executorService.execute(this);
	}


	/**
	 * Stop the auto referee runner
	 */
	public void stop()
	{
		// switch off engine first
		changeMode(EAutoRefMode.OFF);
		try
		{
			executorService.shutdown();
			Validate.isTrue(executorService.awaitTermination(2, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
		// deregister from WP frames
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		// clear auto ref shape map
		SumatraModel.getInstance().getModule(AWorldPredictor.class).notifyClearShapeMap("AUTO_REF");
	}


	public void changeMode(final EAutoRefMode mode)
	{
		synchronized (engineSync)
		{
			engine.stop();
			engine.removeObserver(callback);
			switch (mode)
			{
				case OFF:
					engine = new AutoRefEngine(activeDetectors);
					break;
				case ACTIVE:
					engine = new ActiveAutoRefEngine(activeDetectors);
					break;
				case PASSIVE:
					engine = new PassiveAutoRefEngine(activeDetectors);
					break;
			}
			this.mode = mode;
			engine.addObserver(callback);
			engine.start();
		}
	}


	@Override
	public void run()
	{
		while (!executorService.isShutdown())
		{
			try
			{
				WorldFrameWrapper frame = consumableFrames.poll(10, TimeUnit.MILLISECONDS);
				if (frame != null)
				{
					consumeWorldFrame(frame);
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			} catch (Throwable e)
			{
				log.error("Unhandled exception during AutoRef execution", e);
			}
		}
	}


	private void consumeWorldFrame(final WorldFrameWrapper frame)
	{
		AutoRefFrame currentFrame = preprocessor.process(frame);
		if (currentFrame.getPreviousFrame() != null)
		{
			synchronized (engineSync)
			{
				engine.process(currentFrame);
			}
		}
		SumatraModel.getInstance().getModule(AWorldPredictor.class)
				.notifyNewShapeMap(frame.getTimestamp(), currentFrame.getShapes(), "AUTO_REF");
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (SumatraModel.getInstance().isSimulation())
		{
			// process all frames, waiting and blocking if necessary
			try
			{
				consumableFrames.putFirst(wFrameWrapper);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		} else
		{
			consumableFrames.pollLast();
			consumableFrames.addFirst(wFrameWrapper);
		}
	}


	public AutoRefEngine getEngine()
	{
		return engine;
	}


	public EAutoRefMode getMode()
	{
		return mode;
	}
}
