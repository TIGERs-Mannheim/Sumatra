/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.gamelog.SSLGameLogReader.SSLGameLogfileEntry;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


/**
 * This camera replays an SSL game log.
 */
@Log4j2
public class LogfileVisionCam extends ACam implements Runnable
{
	private static final double TOLERANCE_NEXT_FRAME_TIME_JUMP = 0.1;

	// Connection
	private Thread cam;

	private DirectRefereeMsgForwarder refForwarder;

	// Translation
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();

	private SSLGameLogReader newLogfile;

	private boolean paused = false;
	private int doSteps = 0;
	private double speed = 1.0;
	private int setPos = -1;
	private List<Referee.Command> seekToRefCmdList;
	private List<SslGcGameEvent.GameEvent.Type> seekToGameEventList;

	private int currentFrame = 0;

	private long lastFrameTimestamp = 0;

	private final List<ILogfileVisionCamObserver> observers = new CopyOnWriteArrayList<>();


	@Override
	public void initModule()
	{
		// nothing to do here
	}


	@Override
	public void startModule()
	{
		ConfigRegistration.applySpezis(this, "user",
				SumatraModel.getInstance().getGlobalConfiguration().getString("environment"));

		try
		{
			AReferee ref = SumatraModel.getInstance().getModule(AReferee.class);
			refForwarder = (DirectRefereeMsgForwarder) ref.getSource(ERefereeMessageSource.INTERNAL_FORWARDER);
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
	public void seekForwardToRefCommand(final List<Referee.Command> commands)
	{
		if (commands.isEmpty())
		{
			return;
		}

		seekToRefCmdList = commands;
	}


	public void seekForwardToGameEvent(final List<SslGcGameEvent.GameEvent.Type> gameEventTypes)
	{
		if (gameEventTypes.isEmpty())
		{
			return;
		}
		seekToGameEventList = gameEventTypes;
	}


	private void seekForwardTo(final SSLGameLogReader currentLog, Function<Referee, Boolean> condition)
	{
		int start = findFrameWithCondition(currentLog, currentFrame, condition.andThen(b -> !b));
		if (start < 0)
		{
			return;
		}

		start = findFrameWithCondition(currentLog, start, condition);
		if (start < 0)
		{
			return;
		}

		setPosition(start);
	}


	private int findFrameWithCondition(final SSLGameLogReader currentLog, final int startFrame,
			final Function<Referee, Boolean> condition)
	{
		for (int frame = startFrame; frame < currentLog.getPackets().size(); frame++)
		{
			SSLGameLogfileEntry entry = currentLog.getPackets().get(frame);
			if (entry.getRefereePacket().isPresent())
			{
				Referee ref = entry.getRefereePacket().get();
				if (condition.apply(ref))
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

		while (!Thread.interrupted())
		{
			// take new logfile if we have one
			SSLGameLogReader currentLog = newLogfile;
			newLogfile = null;

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
			} else
			{
				playLog(currentLog);
				log.info("Replay finished");
				notifyVisionLost();
			}
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

		doSeekToGameEvent(currentLog);

		if (doSteps != 0)
		{
			currentFrame += doSteps - 1;

			if (doSteps < 0)
			{
				lastFrameTimestamp = 0;
			}
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


	private void doSeekToGameEvent(final SSLGameLogReader currentLog)
	{
		if (seekToRefCmdList != null)
		{
			Function<Referee, Boolean> condition = ref -> seekToRefCmdList.contains(ref.getCommand());
			seekForwardTo(currentLog, condition);
			seekToRefCmdList = null;
		}
		if (seekToGameEventList != null)
		{
			Function<Referee, Boolean> condition = ref -> ref.getGameEventsCount() > 0
					&& ref.getGameEventsList().stream()
					.map(SslGcGameEvent.GameEvent::getType)
					.anyMatch(gameEventType -> seekToGameEventList.contains(gameEventType));
			seekForwardTo(currentLog, condition);
			seekToGameEventList = null;
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
				final CamGeometry geometry = geometryTranslator.fromProtobuf(sslPacket.getGeometry());

				notifyNewCameraCalibration(geometry);
			}

			if (sslPacket.hasDetection())
			{
				notifyNewCameraFrame(sslPacket.getDetection());
			}
		}

		if (e.getRefereePacket().isPresent())
		{
			refForwarder.send(e.getRefereePacket().get());
		}

		notifyNewLogfileEntry(e, currentFrame);

		double dt = (e.getTimestamp() - lastFrameTimestamp) * 1e-9;
		if (lastFrameTimestamp == 0 || dt < 0)
		{
			dt = 0;
		}

		if (lastFrameTimestamp == 0)
		{
			notifyVisionLost();
		}

		lastFrameTimestamp = e.getTimestamp();

		try
		{
			if (dt < TOLERANCE_NEXT_FRAME_TIME_JUMP) // only if no time jump
			{
				Thread.sleep((long) ((dt * 1000) / speed));
			} else
			{
				// time jump
				notifyVisionLost();
			}
		} catch (InterruptedException e1)
		{
			Thread.currentThread().interrupt();
		}
	}


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
}
