/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.clock.NanoTime;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogPlayerObserver;
import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcCommon.Division;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.CiRefereeSyncedReceiver;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.NetworkRefereeReceiver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

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
	private static final int DEFAULT_GC_UI_PORT = 11000;

	@Setter
	private static int customPort;

	@Setter
	private static String customAddress;


	private final Map<ERefereeMessageSource, ARefereeMessageSource> msgSources = new EnumMap<>(
			ERefereeMessageSource.class);

	private ARefereeMessageSource source;
	private SslGameControllerProcess sslGameControllerProcess;
	private boolean internalGameControlledActive = false;
	private List<SslGcApi.Input> changeQueue = new CopyOnWriteArrayList<>();

	private GameLogRecorder gameLogRecorder;
	private GameLogPlayer gameLogPlayer;
	private GameLogForwarder gameLogForwarder;


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
		((NetworkRefereeReceiver) msgSources.get(ERefereeMessageSource.NETWORK)).setPort(getPort());
		((NetworkRefereeReceiver) msgSources.get(ERefereeMessageSource.NETWORK)).setAddress(getAddress());

		source = msgSources.get(activeSource);

		gameLogPlayer = SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).orElse(null);
		gameLogRecorder = SumatraModel.getInstance().getModuleOpt(GameLogRecorder.class).orElse(null);

		if (activeSource == ERefereeMessageSource.INTERNAL_FORWARDER && gameLogPlayer != null)
		{
			gameLogForwarder = new GameLogForwarder((DirectRefereeMsgForwarder) source);
			gameLogPlayer.addObserver(gameLogForwarder);
		}

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
		return customPort > 0 ? customPort : getSubnodeConfiguration().getInt("port", 10003);
	}


	private String getAddress()
	{
		return StringUtils.isNotBlank(customAddress) ?
				customAddress :
				getSubnodeConfiguration().getString("address", "224.5.23.1");
	}


	public void startGameController()
	{
		var port = getSubnodeConfiguration().getInt("gc-ui-port", DEFAULT_GC_UI_PORT);
		var timeAcquisitionMode = source.getType() == ERefereeMessageSource.CI ? "ci" : "system";
		var publishRefereeMessages = getSubnodeConfiguration().getBoolean("publishRefereeMessages", false);
		var useSystemBinary = getSubnodeConfiguration().getBoolean("useSystemBinary", false);
		var publishAddress = publishRefereeMessages ? getAddress() + ":" + getPort() : "";
		sslGameControllerProcess = new SslGameControllerProcess(port, publishAddress, timeAcquisitionMode);
		sslGameControllerProcess.setUseSystemBinary(useSystemBinary);
		sslGameControllerProcess.killAllRunningProcesses();

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


	private void initGameController()
	{
		sendGameControllerEvent(GcEventFactory.teamName(ETeamColor.BLUE, "BLUE AI"));
		sendGameControllerEvent(GcEventFactory.teamName(ETeamColor.YELLOW, "YELLOW AI"));
		sendGameControllerEvent(GcEventFactory.matchType(SslGcRefereeMessage.MatchType.FRIENDLY));
		var division = Division.valueOf(getSubnodeConfiguration().getString("division", Division.DIV_A.name()));
		sendGameControllerEvent(GcEventFactory.division(division));
	}


	@Override
	public void resetGameController()
	{
		log.debug("Resetting game controller");
		flushChanges();
		sendGameControllerEvent(GcEventFactory.triggerResetMatch());
		initGameController();
	}


	public int getGameControllerUiPort()
	{
		return sslGameControllerProcess.getGcUiPort();
	}


	@Override
	public void stopModule()
	{
		if (gameLogForwarder != null)
		{
			gameLogPlayer.removeObserver(gameLogForwarder);
		}

		source.stop();
		source.removeObserver(this);
		stopGameController();
	}


	@Override
	public void onNewRefereeMessage(final SslGcRefereeMessage.Referee msg)
	{
		notifyNewRefereeMsg(msg);

		if (gameLogRecorder != null)
		{
			gameLogRecorder.writeMessage(
					new GameLogMessage(NanoTime.getTimestampNow(), EMessageType.SSL_REFBOX_2013, msg.toByteArray()));
		}
	}


	@Override
	public void sendGameControllerEvent(final SslGcApi.Input event)
	{
		if (getActiveSource().getType() == ERefereeMessageSource.CI)
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


	@RequiredArgsConstructor
	private static class GameLogForwarder implements GameLogPlayerObserver
	{
		private final DirectRefereeMsgForwarder forwarder;


		@Override
		public void onNewGameLogMessage(GameLogMessage message, int index)
		{
			if (message.getType() != EMessageType.SSL_REFBOX_2013)
				return;

			try
			{
				var sslReferee = SslGcRefereeMessage.Referee.parseFrom(message.getData());
				forwarder.send(sslReferee);
			} catch (Exception err)
			{
				log.error("Invalid SSL_REFBOX_2013 package.", err);
			}
		}


		@Override
		public void onGameLogTimeJump()
		{
			// No action required.
		}
	}
}
