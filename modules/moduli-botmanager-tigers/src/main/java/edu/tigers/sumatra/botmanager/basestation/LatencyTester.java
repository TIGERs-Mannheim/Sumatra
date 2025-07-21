/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.basestation;

import edu.tigers.sumatra.botmanager.ICommandSink;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationPing;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.observer.EventSubscriber;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
public class LatencyTester
{
	private final ICommandSink commandSink;

	private ScheduledExecutorService pingService = null;
	private PingThread pingThread = null;

	private final EventDistributor<Double> onNewPingDelay = new EventDistributor<>();


	public void startPing(final int numPings, final int payloadLength)
	{
		stopPing();

		pingThread = new PingThread(payloadLength);
		pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
		pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
	}


	public void stopPing()
	{
		if (pingService == null)
		{
			return;
		}

		pingService.shutdown();
		pingService = null;
		pingThread = null;
	}


	public void processIncomingCommand(ACommand cmd)
	{
		if (Objects.requireNonNull(cmd.getType()) == ECommand.CMD_BASE_PING)
		{
			BaseStationPing ping = (BaseStationPing) cmd;
			if (pingThread != null)
			{
				pingThread.pongArrived((int) ping.getId());
			}
		}
	}


	public EventSubscriber<Double> getOnNewPingDelay()
	{
		return onNewPingDelay;
	}


	private class PingThread implements Runnable
	{
		private int id = 0;
		private final int payloadLength;

		private final Map<Integer, Long> activePings = new ConcurrentSkipListMap<>();


		public PingThread(final int payloadLength)
		{
			this.payloadLength = payloadLength;
		}


		@Override
		public void run()
		{
			activePings.put(id, System.nanoTime());

			commandSink.sendCommand(new BaseStationPing(id, payloadLength));
			id++;
		}


		public void pongArrived(final int id)
		{
			Long startTime;

			startTime = activePings.remove(id);

			if (startTime == null)
			{
				return;
			}

			final double delayPongArrive = (System.nanoTime() - startTime) / 1000000.0;

			onNewPingDelay.newEvent(delayPongArrive);
		}
	}
}
