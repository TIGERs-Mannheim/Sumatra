/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.Oracle_extKalman;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * Watch chip kicks and gather data to learn for future
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABallWatcher implements IWorldPredictorObserver, Runnable
{
	private static final Logger	log						= Logger.getLogger(ABallWatcher.class.getName());
	private final long				startTime				= System.nanoTime();
	private static final long		TIMEOUT_NS				= (long) 10e9;
	private static final int		MIN_SAMPLES_NEEDED	= 50;
	
	private IVector2					lastBallPos				= null;
	private IVector2					initBallPos				= null;
	private List<ChipKickDataSet>	data						= new LinkedList<ChipKickDataSet>();
	
	private boolean					processing				= false;
	
	private long						timeStartBallVelZero	= System.nanoTime();
	
	private static class ChipKickDataSet
	{
		private long		timestamp	= 0;
		private IVector2	pos			= Vector2.ZERO_VECTOR;
		private int			camId			= 0;
		
		
		@Override
		public String toString()
		{
			return "[pos=" + pos + ", timestamp=" + timestamp + "]";
		}
	}
	
	
	/**
	 * 
	 */
	public final void start()
	{
		try
		{
			Oracle_extKalman wp = (Oracle_extKalman) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}
	
	
	private void checkTimeout()
	{
		if ((System.nanoTime() - startTime) > TIMEOUT_NS)
		{
			log.debug("ChipKickWatcher timeout");
			startProcess();
		}
	}
	
	
	private IVector2 getBallPosFromList(final List<CamBall> balls)
	{
		IVector2 ballPos = balls.get(0).pos.getXYVector();
		if (balls.size() > 1)
		{
			if (lastBallPos != null)
			{
				float minDist = Float.MAX_VALUE;
				for (CamBall ball : balls)
				{
					IVector2 p = ball.pos.getXYVector();
					float dist = GeoMath.distancePP(lastBallPos, p);
					if (dist < minDist)
					{
						minDist = dist;
						ballPos = p;
					}
				}
			}
		}
		return ballPos;
	}
	
	
	protected boolean checkIsFailed(final IVector2 ballPos)
	{
		// if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
		// {
		// log.info("ball out of field, data invalid...");
		// return true;
		// }
		return false;
	}
	
	
	protected boolean checkIsDone(final IVector2 ballPos)
	{
		if ((data.size() > 10) && (lastBallPos != null) && lastBallPos.equals(ballPos, 5)
				&& (!initBallPos.equals(ballPos, 50)))
		{
			if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timeStartBallVelZero) > 1500)
			{
				log.debug("ball stopped, data size: " + data.size());
				return true;
			}
		} else
		{
			timeStartBallVelZero = System.nanoTime();
		}
		return false;
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame frame)
	{
		checkTimeout();
		if (processing)
		{
			return;
		}
		
		if (frame.balls.isEmpty())
		{
			return;
		}
		
		IVector2 ballPos = getBallPosFromList(frame.balls);
		
		if (initBallPos == null)
		{
			initBallPos = ballPos;
		}
		
		ChipKickDataSet dataSet = new ChipKickDataSet();
		dataSet.timestamp = System.currentTimeMillis();
		dataSet.pos = ballPos;
		dataSet.camId = frame.cameraId;
		data.add(dataSet);
		
		if (checkIsFailed(ballPos))
		{
			stop();
		}
		
		if (checkIsDone(ballPos))
		{
			startProcess();
		}
		
		lastBallPos = ballPos;
	}
	
	
	private void startProcess()
	{
		stop();
		if (!processing)
		{
			processing = true;
			new Thread(this, "BallWatcher").start();
		}
	}
	
	
	private void stop()
	{
		try
		{
			Oracle_extKalman wp = (Oracle_extKalman) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		stop();
	}
	
	
	@Override
	public void onNewWorldFrame(final SimpleWorldFrame wf)
	{
	}
	
	
	@Override
	public void run()
	{
		if (data.size() < MIN_SAMPLES_NEEDED)
		{
			log.warn("Not enough data for learning: " + data.size());
			return;
		}
		String moduli = SumatraModel.getInstance().getCurrentModuliConfig().split("\\.")[0];
		String file = moduli + "/" + getFileName();
		CSVExporter exporter = new CSVExporter(file, file, false);
		
		for (ChipKickDataSet ds : data)
		{
			exporter.addValues(ds.camId, ds.timestamp, ds.pos.x(), ds.pos.y());
		}
		
		export(exporter);
		
		exporter.close();
		
		postProcessing(exporter.getAbsoluteFileName());
	}
	
	
	protected void export(final CSVExporter exporter)
	{
	}
	
	
	protected void postProcessing(final String fileName)
	{
		
	}
	
	
	protected abstract String getFileName();
	
	
	/**
	 * @return the lastBallPos
	 */
	public final IVector2 getLastBallPos()
	{
		return lastBallPos;
	}
	
	
	/**
	 * @return the initBallPos
	 */
	public final IVector2 getInitBallPos()
	{
		return initBallPos;
	}
}