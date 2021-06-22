/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.CiRefereeSyncedReceiver;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.NetworkRefereeReceiver;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Implementation of {@link AReferee} which can use various referee message sources.
 */
@Log4j2
public class Referee extends AReferee
{
	private static final int DEFAULT_GC_UI_PORT = 50543;
	private final Map<ERefereeMessageSource, ARefereeMessageSource> msgSources = new EnumMap<>(
			ERefereeMessageSource.class);

	private ARefereeMessageSource source;
	private SslGameControllerProcess sslGameControllerProcess;
	private boolean internalGameControlledActive = false;
	private List<SslGcApi.Input> changeQueue = new CopyOnWriteArrayList<>();


	public Referee()
	{
		msgSources.put(ERefereeMessageSource.NETWORK, new NetworkRefereeReceiver());
		msgSources.put(ERefereeMessageSource.INTERNAL_FORWARDER, new DirectRefereeMsgForwarder());
		msgSources.put(ERefereeMessageSource.CI, new CiRefereeSyncedReceiver());
	}


	@Override
	public void startModule()
	{
		ERefereeMessageSource activeSource = ERefereeMessageSource
				.valueOf(getSubnodeConfiguration().getString("source", ERefereeMessageSource.NETWORK.name()));
		boolean useGameController = getSubnodeConfiguration().getBoolean("gameController", false);
		int port = getPort();
		((NetworkRefereeReceiver) msgSources.get(ERefereeMessageSource.NETWORK)).setPort(port);

		source = msgSources.get(activeSource);

		if (useGameController)
		{
			startGameController();
		}

		source.addObserver(this);
		source.start();

		notifyRefereeMsgSourceChanged(source);
	}


	private int getPort()
	{
		return getSubnodeConfiguration().getInt("port", 10003);
	}


	public void startGameController()
	{
		var port = getSubnodeConfiguration().getInt("gc-ui-port", DEFAULT_GC_UI_PORT);
		var timeAcquisitionMode = source.getType() == ERefereeMessageSource.CI ? "ci" : "system";
		var publishRefereeMessages = getSubnodeConfiguration().getBoolean("publishRefereeMessages", false);
		var publishAddress = publishRefereeMessages ? "224.5.23.1:" + getPort() : "";
		sslGameControllerProcess = new SslGameControllerProcess(port, publishAddress, timeAcquisitionMode);

		File stateStoreFile = new File("build/state-store.json.stream");
		if (stateStoreFile.getParentFile().mkdirs())
		{
			log.debug("state store dir created: {}", stateStoreFile);
		}
		if (stateStoreFile.exists())
		{
			try
			{
				Files.delete(stateStoreFile.toPath());
			} catch (IOException e)
			{
				log.warn("Could not remove state store file, although it exists", e);
			}
		}

		new Thread(sslGameControllerProcess).start();
		initGameController();
		internalGameControlledActive = true;
	}


	public void stopGameController()
	{
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.stop();
			sslGameControllerProcess = null;
		}
		internalGameControlledActive = false;
	}


	@Override
	public void initGameController()
	{
		sendGameControllerEvent(GcEventFactory.teamName(ETeamColor.BLUE, "BLUE AI"));
		sendGameControllerEvent(GcEventFactory.teamName(ETeamColor.YELLOW, "YELLOW AI"));
		sendGameControllerEvent(GcEventFactory.stage(SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF));
	}


	public int getGameControllerUiPort()
	{
		return sslGameControllerProcess.getGcUiPort();
	}


	@Override
	public void stopModule()
	{
		source.stop();
		source.removeObserver(this);
		stopGameController();
	}


	@Override
	public void onNewRefereeMessage(final SslGcRefereeMessage.Referee msg)
	{
		notifyNewRefereeMsg(msg);
	}


	@Override
	public void sendGameControllerEvent(final SslGcApi.Input event)
	{
		if (sslGameControllerProcess != null)
		{
			changeQueue.add(event);
		}
	}


	@Override
	public ARefereeMessageSource getActiveSource()
	{
		return source;
	}


	@Override
	public ARefereeMessageSource getSource(final ERefereeMessageSource type)
	{
		return msgSources.get(type);
	}


	@Override
	public boolean isInternalGameControllerUsed()
	{
		return internalGameControlledActive;
	}


	public List<SslGcApi.Input> flushChanges()
	{
		var currentQueue = changeQueue;
		changeQueue = new CopyOnWriteArrayList<>();
		return currentQueue;
	}
}
