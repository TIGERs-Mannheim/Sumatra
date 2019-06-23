/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.module;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.AutoRefFramePreprocessor;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.autoreferee.engine.log.appender.GameLogFileAppender;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.autoreferee.remote.impl.LocalRefboxRemote;
import edu.tigers.autoreferee.remote.impl.ThreadedTCPRefboxRemote;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public class AutoRefRunner implements Runnable, IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(AutoRefRunner.class);
	
	private static final Path LOG_DIRECTORY = Paths.get("gamelogs/");
	
	
	private final List<IAutoRefStateObserver> observer;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final BlockingDeque<WorldFrameWrapper> consumableFrames = new LinkedBlockingDeque<>(1);
	private final BlockingDeque<RefStateChange> requestStateChanges = new LinkedBlockingDeque<>();
	private final AutoRefFramePreprocessor preprocessor = new AutoRefFramePreprocessor();
	private AutoRefState refState = AutoRefState.STOPPED;
	private boolean running = false;
	private IAutoRefEngine engine;
	private GameLogFileAppender gameLogAppender;
	private final ERemoteControlType remoteControlType;
	private final boolean log2File;
	private AWorldPredictor wp;
	
	
	/**
	 * @param observer
	 * @param remoteControlType
	 * @param log2File
	 */
	public AutoRefRunner(final List<IAutoRefStateObserver> observer,
			final ERemoteControlType remoteControlType, final boolean log2File)
	{
		this.observer = observer;
		this.remoteControlType = remoteControlType;
		this.log2File = log2File;
	}
	
	
	/**
	 * @return
	 */
	public IAutoRefEngine getEngine()
	{
		return engine;
	}
	
	
	/**
	 * @return
	 */
	public AutoRefState getState()
	{
		return refState;
	}
	
	
	private void setState(final AutoRefState state)
	{
		if (refState != state && state != AutoRefState.RUNNING)
		{
			wp.notifyClearShapeMap("AUTO_REF");
		}
		refState = state;
		try
		{
			observer.forEach(obs -> obs.onAutoRefStateChanged(state));
		} catch (Exception e)
		{
			log.error("Error in auto ref observer call", e);
		}
	}
	
	
	/**
	 * @param mode
	 * @throws StartModuleException
	 */
	public void start(final AutoRefMode mode) throws StartModuleException
	{
		wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		IRefboxRemote remote = null;
		IAutoRefEngine refEngine = null;
		
		if (log2File)
		{
			gameLogAppender = createGameLogAppender(mode);
			try
			{
				gameLogAppender.start();
			} catch (IOException e)
			{
				log.error("Could not start gameLogAppender.", e);
			}
		}
		
		try
		{
			setState(AutoRefState.STARTING);
			
			/*
			 * Create the AutoRef engine
			 */
			if (mode == AutoRefMode.ACTIVE)
			{
				if (remoteControlType == ERemoteControlType.LOCAL_MOCK)
				{
					remote = new LocalRefboxRemote();
				} else
				{
					AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
					String hostname = referee.getActiveSource().getRefBoxAddress().map(InetAddress::getHostAddress)
							.orElse(AutoRefConfig.getRefboxHostname());
					ThreadedTCPRefboxRemote tcpRefboxRemote = new ThreadedTCPRefboxRemote(hostname,
							AutoRefConfig.getRefboxPort());
					tcpRefboxRemote.start();
					remote = tcpRefboxRemote;
				}
				
				refEngine = new ActiveAutoRefEngine(remote);
			} else
			{
				refEngine = new PassiveAutoRefEngine();
			}
			if (gameLogAppender != null)
			{
				refEngine.getGameLog().addObserver(gameLogAppender);
			}
			
			registerWithWorldPredictor();
			
		} catch (ModuleNotFoundException | IOException e)
		{
			if (remote != null)
			{
				remote.stop();
			}
			if (refEngine != null)
			{
				refEngine.stop();
			}
			setState(AutoRefState.STOPPED);
			log.error("Error while starting up the autoref engine (" + mode + ")", e);
			throw new StartModuleException(e.getMessage(), e);
		}
		
		engine = refEngine;
		
		running = true;
		executorService.execute(this);
		
		setState(AutoRefState.STARTED);
		setState(AutoRefState.RUNNING);
	}
	
	
	private GameLogFileAppender createGameLogAppender(final AutoRefMode mode)
	{
		Path logName = Paths.get(String.format("%1$s-%2$tY-%2$tm-%2$td_%2$tH-%2$tM-%2$tS-%2$tL.log", mode.toString(),
				new Date()));
		Path fileName = LOG_DIRECTORY.resolve(logName);
		return new GameLogFileAppender(fileName);
	}
	
	
	/**
	 * Stop the auto referee which is currently being run
	 */
	public void stop()
	{
		if (running)
		{
			running = false;
			requestStateChanges.add(RefStateChange.STOP);
			try
			{
				executorService.shutdown();
				Validate.isTrue(executorService.awaitTermination(2, TimeUnit.SECONDS));
			} catch (InterruptedException e)
			{
				log.error("Interrupted while awaiting termination", e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	
	/**
	 * Pause the auto referee. Has no effect
	 */
	public void pause()
	{
		if (running)
		{
			requestStateChanges.add(RefStateChange.PAUSE);
		}
	}
	
	
	/**
	 * Resumes the auto referee after it has been paused
	 */
	public void resume()
	{
		if (running)
		{
			requestStateChanges.add(RefStateChange.RESUME);
		}
	}
	
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("AutoReferee");
		
		try
		{
			doLoop();
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (Exception e)
		{
			log.error("Unhandled exception during AutoRef execution", e);
		} finally
		{
			running = false;
			setState(AutoRefState.STOPPED);
			
			deregisterFromPredictor();
			engine.stop();
			if (gameLogAppender != null)
			{
				gameLogAppender.stop();
			}
		}
	}
	
	
	private void doLoop() throws InterruptedException
	{
		while (running)
		{
			WorldFrameWrapper frame = consumableFrames.poll(10, TimeUnit.MILLISECONDS);
			if (frame != null)
			{
				consumeWorldFrame(frame);
			}
			
			RefStateChange change = requestStateChanges.poll();
			if (change != null)
			{
				if (change == RefStateChange.PAUSE)
				{
					deregisterFromPredictor();
					setState(AutoRefState.PAUSED);
					engine.pause();
					change = pauseUntilResume();
				}
				
				if ((refState == AutoRefState.PAUSED) && (change == RefStateChange.RESUME))
				{
					registerWithWorldPredictor();
					engine.resume();
					setState(AutoRefState.RUNNING);
				}
				
				if (change == RefStateChange.STOP)
				{
					break;
				}
			}
		}
	}
	
	
	private RefStateChange pauseUntilResume() throws InterruptedException
	{
		RefStateChange change;
		do
		{
			change = requestStateChanges.take();
		} while (change == RefStateChange.PAUSE);
		return change;
	}
	
	
	private void consumeWorldFrame(final WorldFrameWrapper frame)
	{
		if (!preprocessor.hasLastFrame())
		{
			preprocessor.setLastFrame(frame);
			return;
		}
		
		AutoRefFrame currentFrame;
		try
		{
			currentFrame = preprocessor.process(frame);
		} catch (Exception t)
		{
			log.error("Error while running autoref calculators", t);
			return;
		}
		
		try
		{
			engine.process(currentFrame);
		} catch (Exception t)
		{
			log.error("Error while running autoref engine", t);
		}
		
		pushFrameToObserver(currentFrame);
	}
	
	
	private void pushFrameToObserver(final IAutoRefFrame frame)
	{
		for (IAutoRefStateObserver o : observer)
		{
			try
			{
				o.onNewAutoRefFrame(frame);
			} catch (Exception t)
			{
				log.error("Error in autoref state observer (" + o + ")", t);
			}
		}
		wp.notifyNewShapeMap(frame.getTimestamp(), frame.getShapes(), "AUTO_REF");
	}
	
	
	private void registerWithWorldPredictor()
	{
		wp.addObserver(this);
	}
	
	
	private void deregisterFromPredictor()
	{
		if (wp != null)
		{
			wp.removeObserver(this);
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		consumableFrames.pollLast();
		consumableFrames.offerFirst(wFrameWrapper);
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
		consumableFrames.clear();
	}
	
	
	private enum RefStateChange
	{
		PAUSE,
		RESUME,
		STOP
	}
}
