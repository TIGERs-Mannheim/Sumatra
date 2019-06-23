/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.gamelog.SSLGameLogReader.SSLGameLogfileEntry;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;


/**
 * This camera replays an SSL game log.
 * 
 * @author AndreR
 */
public class LogfileVisionCam extends ACam implements Runnable
{
	
	private static final Logger log = Logger
			.getLogger(LogfileVisionCam.class.getName());
	
	// Connection
	private Thread cam;
	
	private DirectRefereeMsgForwarder refForwarder;
	
	// Translation
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();
	
	private final TimeSync timeSync = new TimeSync();
	
	private SSLGameLogReader newLogfile;
	
	private boolean paused = false;
	private int doSteps = 0;
	private double speed = 1.0;
	private int setPos = -1;
	private List<SSL_Referee.Command> seekToRefCmdList;
	
	private int currentFrame = 0;
	
	private long lastFrameTimestamp = 0;
	
	private final List<ILogfileVisionCamObserver> observers = new CopyOnWriteArrayList<>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public LogfileVisionCam(final SubnodeConfiguration subnodeConfiguration)
	{
		super(subnodeConfiguration);
	}
	
	
	// --------------------------------------------------------------------------
	// --- init and start -------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		// nothing to do here
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		ConfigRegistration.applySpezis(this, "user",
				SumatraModel.getInstance().getGlobalConfiguration().getString("environment"));
		
		try
		{
			AReferee ref = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			ref.setActiveSource(ERefereeMessageSource.INTERNAL_FORWARDER);
			refForwarder = (DirectRefereeMsgForwarder) ref.getActiveSource();
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find cam module.", e);
		}
		
		cam = new Thread(this, "LogfileVisionCam");
		cam.start();
	}
	
	
	public void setLogfile(final SSLGameLogReader logfile)
	{
		newLogfile = logfile;
	}
	
	
	public void setPause(final boolean enable)
	{
		paused = enable;
	}
	
	
	/**
	 * Steps some frames.
	 *
	 * @param numSteps
	 */
	public void doSteps(final int numSteps)
	{
		doSteps = numSteps;
	}
	
	
	public void setSpeed(final double speed)
	{
		this.speed = speed;
	}
	
	
	public void setPosition(final int pos)
	{
		setPos = pos;
	}
	
	
	/**
	 * Seek forward to the next frame that contains the given referee command(s).
	 * 
	 * @param commands
	 */
	public void seekForwardToRefCommand(final List<SSL_Referee.Command> commands)
	{
		if (commands.isEmpty())
		{
			return;
		}
		
		seekToRefCmdList = commands;
	}
	
	
	private void seekForwardToRefCommand(final SSLGameLogReader currentLog, final List<SSL_Referee.Command> cmdList)
	{
		int start = findFrameExclRefCommand(currentLog, currentFrame, cmdList);
		if (start < 0)
		{
			return;
		}
		
		start = findFrameWithRefCommand(currentLog, start, cmdList);
		if (start < 0)
		{
			return;
		}
		
		setPosition(start);
	}
	
	
	private int findFrameWithRefCommand(final SSLGameLogReader currentLog, final int startFrame,
			final List<SSL_Referee.Command> commands)
	{
		for (int frame = startFrame; frame < currentLog.getPackets().size(); frame++)
		{
			SSLGameLogfileEntry entry = currentLog.getPackets().get(frame);
			if (entry.getRefereePacket().isPresent())
			{
				SSL_Referee ref = entry.getRefereePacket().get();
				if (commands.contains(ref.getCommand()))
				{
					return frame;
				}
			}
		}
		
		return -1;
	}
	
	
	private int findFrameExclRefCommand(final SSLGameLogReader currentLog, final int startFrame,
			final List<SSL_Referee.Command> commands)
	{
		for (int frame = startFrame; frame < currentLog.getPackets().size(); frame++)
		{
			SSLGameLogfileEntry entry = currentLog.getPackets().get(frame);
			if (entry.getRefereePacket().isPresent())
			{
				SSL_Referee ref = entry.getRefereePacket().get();
				if (!commands.contains(ref.getCommand()))
				{
					return frame;
				}
			}
		}
		
		return -1;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ILogfileVisionCamObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ILogfileVisionCamObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyNewLogfileEntry(final SSLGameLogfileEntry e, final int index)
	{
		for (ILogfileVisionCamObserver observer : observers)
		{
			observer.onNewLogfileEntry(e, index);
		}
	}
	
	
	@Override
	public void run()
	{
		SSLGameLogReader currentLog = null;
		
		while (!Thread.interrupted())
		{
			// take new logfile if we have one
			if (newLogfile != null)
			{
				currentLog = newLogfile;
				newLogfile = null;
			}
			
			// no log to play? nothing to do!
			if (currentLog == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e1)
				{
					Thread.currentThread().interrupt();
					return;
				}
				
				continue;
			}
			
			// play the logfile
			playLog(currentLog);
			
			log.info("Replay finished");
			
			notifyVisionLost();
		}
	}
	
	
	private void playLog(final SSLGameLogReader currentLog)
	{
		for (currentFrame = 0; currentFrame < currentLog.getPackets().size(); currentFrame++)
		{
			if (newLogfile != null)
			{
				return;
			}
			
			adjustCurrentFrame(currentLog);
			
			if (Thread.currentThread().isInterrupted())
			{
				return;
			}
			
			publishFrameAndSleep(currentLog.getPackets().get(currentFrame));
		}
	}
	
	
	private void adjustCurrentFrame(final SSLGameLogReader currentLog)
	{
		final int numPackets = currentLog.getPackets().size();
		
		while (paused)
		{
			if ((doSteps != 0) || (newLogfile != null))
			{
				break;
			}
			
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e1)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
		
		if (seekToRefCmdList != null)
		{
			seekForwardToRefCommand(currentLog, seekToRefCmdList);
			seekToRefCmdList = null;
		}
		
		if (doSteps != 0)
		{
			currentFrame += doSteps - 1;
		}
		
		doSteps = 0;
		
		if (currentFrame < 0)
		{
			currentFrame = 0;
		}
		if (currentFrame > (numPackets - 1))
		{
			currentFrame = numPackets - 1;
		}
		
		if (setPos >= 0)
		{
			currentFrame = setPos;
			lastFrameTimestamp = 0;
			setPos = -1;
		}
	}
	
	
	private void publishFrameAndSleep(final SSLGameLogfileEntry e)
	{
		if (e.getVisionPacket().isPresent())
		{
			SSL_WrapperPacket sslPacket = e.getVisionPacket().get();
			
			notifyNewVisionPacket(sslPacket);
			
			if (sslPacket.hasGeometry())
			{
				final CamGeometry geometry = geometryTranslator.translate(sslPacket.getGeometry());
				
				notifyNewCameraCalibration(geometry);
			}
			
			if (sslPacket.hasDetection())
			{
				notifyNewCameraFrame(sslPacket.getDetection(), timeSync);
			}
		}
		
		if (e.getRefereePacket().isPresent())
		{
			refForwarder.send(e.getRefereePacket().get());
		}
		
		notifyNewLogfileEntry(e, currentFrame);
		
		int sleepMilli = 0;
		if (lastFrameTimestamp != 0)
		{
			sleepMilli = (int) (((e.getTimestamp() - lastFrameTimestamp) * 1e-6) / speed);
		}
		
		if (sleepMilli < 0)
		{
			sleepMilli = 0;
		}
		
		if (lastFrameTimestamp == 0)
		{
			notifyVisionLost();
		}
		
		lastFrameTimestamp = e.getTimestamp();
		
		try
		{
			Thread.sleep(sleepMilli);
		} catch (InterruptedException e1)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- deinit and stop ------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void stopModule()
	{
		if (cam != null)
		{
			cam.interrupt();
			cam = null;
		}
	}
	
	/**
	 * LogfileVisionCam observer.
	 */
	@FunctionalInterface
	public interface ILogfileVisionCamObserver
	{
		/**
		 * @param e
		 * @param index
		 */
		void onNewLogfileEntry(SSLGameLogfileEntry e, int index);
	}
	
	
	/**
	 * @return the currentFrame
	 */
	public int getCurrentFrame()
	{
		return currentFrame;
	}
}
