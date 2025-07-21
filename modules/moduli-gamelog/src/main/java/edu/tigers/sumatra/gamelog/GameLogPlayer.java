/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

import edu.tigers.sumatra.moduli.AModule;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


/**
 * This module can open (SSL) Game Logs and replay them. Messages are binary blobs and must
 * be processed by attached observers.
 * This player will only handle playback speed and seek actions (i.e. _how_ it is played, not _what_ is played).
 */
@Log4j2
public class GameLogPlayer extends AModule
{
	private static final double TOLERANCE_NEXT_FRAME_TIME_JUMP = 0.1;

	private Thread player;

	private GameLogReader newLogfile;

	@Setter
	private boolean pause = false;


	@Setter
	private double speed = 1.0;

	@Setter
	private int position = -1;

	private int doSteps = 0;
	private Function<GameLogMessage, GameLogCompareResult> seekCondition;

	private int currentFrame = 0;

	private long lastFrameTimestamp = 0;

	private final List<GameLogPlayerObserver> observers = new CopyOnWriteArrayList<>();


	@Override
	public void startModule()
	{
		player = new Thread(this::play, "GameLogPlayer");
		player.setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in log player", e));
		player.start();
	}


	@Override
	public void stopModule()
	{
		if (player != null)
		{
			player.interrupt();
			player = null;
		}
	}


	public void setLogfile(final GameLogReader logfile)
	{
		newLogfile = logfile;
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


	/**
	 * Seeks forward until the supplied functional returns MATCH.
	 * If the condition is MATCH for the current frame it seeks forward to the next occurrence.
	 *
	 * @param condition
	 */
	public void seekTo(final Function<GameLogMessage, GameLogCompareResult> condition)
	{
		seekCondition = condition;
	}


	public void addObserver(final GameLogPlayerObserver observer)
	{
		observers.add(observer);
	}


	public void removeObserver(final GameLogPlayerObserver observer)
	{
		observers.remove(observer);
	}


	private void play()
	{
		while (!Thread.interrupted())
		{
			// take new logfile if we have one
			GameLogReader currentLog = newLogfile;
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
				observers.forEach(GameLogPlayerObserver::onGameLogTimeJump);
			}
		}
	}


	public void playlogFast(final GameLogReader log)
	{
		long lastTimestamp = 0;

		for (int frameId = 0; frameId < log.getMessages().size(); frameId++)
		{
			var msg = log.getMessages().get(frameId);

			int finalCurrentFrame = frameId;
			observers.forEach(o -> o.onNewGameLogMessage(msg, finalCurrentFrame));

			double dt = (msg.getTimestampNs() - lastTimestamp) * 1e-9;
			if (lastTimestamp == 0 || dt < 0)
			{
				dt = 0;
			}

			if (lastTimestamp == 0)
			{
				observers.forEach(GameLogPlayerObserver::onGameLogTimeJump);
			}

			lastTimestamp = msg.getTimestampNs();

			if (dt > TOLERANCE_NEXT_FRAME_TIME_JUMP)
			{
				// time jump
				observers.forEach(GameLogPlayerObserver::onGameLogTimeJump);
			}
		}
	}


	private void playLog(final GameLogReader currentLog)
	{
		for (currentFrame = 0; currentFrame < currentLog.getMessages().size(); currentFrame++)
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

			publishFrameAndSleep(currentLog.getMessages().get(currentFrame));
		}
	}


	private void adjustCurrentFrame(final GameLogReader currentLog)
	{
		final int numPackets = currentLog.getMessages().size();

		while (pause)
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

		if (seekCondition != null)
		{
			seekForwardTo(currentLog, seekCondition);
			seekCondition = null;
		}

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

		if (position >= 0)
		{
			currentFrame = position;
			lastFrameTimestamp = 0;
			position = -1;
		}
	}


	private void seekForwardTo(final GameLogReader currentLog, Function<GameLogMessage, GameLogCompareResult> condition)
	{
		// seek forward until condition is no longer true (in case we are in the seek state already)
		int start = findFrameWithCondition(currentLog, currentFrame, condition, GameLogCompareResult.MISMATCH);
		if (start < 0)
		{
			return;
		}

		// and then seek forward until the condition is true again
		start = findFrameWithCondition(currentLog, start, condition, GameLogCompareResult.MATCH);
		if (start < 0)
		{
			return;
		}

		setPosition(start);
	}


	private int findFrameWithCondition(
			final GameLogReader currentLog, final int startFrame,
			final Function<GameLogMessage, GameLogCompareResult> condition, GameLogCompareResult requiredVerdict
	)
	{
		for (int frame = startFrame; frame < currentLog.getMessages().size(); frame++)
		{
			GameLogMessage msg = currentLog.getMessages().get(frame);
			if (condition.apply(msg) == requiredVerdict)
			{
				return frame;
			}
		}

		return -1;
	}


	private void publishFrameAndSleep(final GameLogMessage msg)
	{
		observers.forEach(o -> o.onNewGameLogMessage(msg, currentFrame));

		double dt = (msg.getTimestampNs() - lastFrameTimestamp) * 1e-9;
		if (lastFrameTimestamp == 0 || dt < 0)
		{
			dt = 0;
		}

		if (lastFrameTimestamp == 0)
		{
			observers.forEach(GameLogPlayerObserver::onGameLogTimeJump);
		}

		lastFrameTimestamp = msg.getTimestampNs();

		try
		{
			if (dt < TOLERANCE_NEXT_FRAME_TIME_JUMP) // only if no time jump
			{
				Thread.sleep((long) ((dt * 1000) / speed));
			} else
			{
				// time jump
				observers.forEach(GameLogPlayerObserver::onGameLogTimeJump);
			}
		} catch (InterruptedException e1)
		{
			Thread.currentThread().interrupt();
		}
	}


}
