/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.basestation.LatencyTester;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationBroadcast;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationCameraViewport;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.clock.NanoTime;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.Safe;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.Viewport;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * TIGERs bot manager implementation
 */
public class TigersBotManager extends ACommandBasedBotManager implements IWorldFrameObserver
{
	private static final String PROP_AUTO_CHARGE = TigersBotManager.class.getName() + ".autoCharge";

	@Getter
	private final ConfigFileDatabaseManager configDatabase = new ConfigFileDatabaseManager();

	@Getter
	private final TigersBaseStation baseStation = new TigersBaseStation();
	private GameLogRecorder gameLogRecorder;

	@Getter
	private LatencyTester latencyTester = new LatencyTester(this);

	private ScheduledExecutorService scheduledExecutorService;


	@Override
	public void startModule()
	{
		super.startModule();

		matchBroadcast.setAutoCharge(SumatraModel.getInstance().getUserProperty(PROP_AUTO_CHARGE, true));

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);

		SumatraModel.getInstance().getModule(AVisionFilter.class).getViewport()
				.subscribe(getClass().getCanonicalName(), this::onViewportUpdated);

		gameLogRecorder = SumatraModel.getInstance().getModuleOpt(GameLogRecorder.class).orElse(null);

		baseStation.getOnIncomingCommand().subscribe(getClass().getCanonicalName(), this::onIncomingCommand);
		baseStation.getNetworkState().subscribe(getClass().getCanonicalName(), this::onNetworkStateChanged);

		baseStation.connect();

		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
				new NamedThreadFactory("TIGERs Broadcast Sender"));

		scheduledExecutorService
				.scheduleAtFixedRate(() -> Safe.run(this::sendBroadcast), 0, 10L, TimeUnit.MILLISECONDS);
	}


	@Override
	public void stopModule()
	{
		if (scheduledExecutorService != null)
		{
			scheduledExecutorService.shutdown();
			scheduledExecutorService = null;
		}

		baseStation.disconnect();

		baseStation.getOnIncomingCommand().unsubscribe(getClass().getCanonicalName());
		baseStation.getNetworkState().unsubscribe(getClass().getCanonicalName());

		gameLogRecorder = null;

		SumatraModel.getInstance().getModule(AVisionFilter.class).getViewport()
				.unsubscribe(getClass().getCanonicalName());

		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);

		super.stopModule();
	}


	@Override
	public Map<BotID, TigerBot> getBots()
	{
		return super.getBots().values().stream().map(TigerBot.class::cast)
				.collect(Collectors.toMap(TigerBot::getBotId, Function.identity()));
	}


	@Override
	public Optional<TigerBot> getBot(BotID botID)
	{
		return super.getBot(botID).map(TigerBot.class::cast);
	}


	@Override
	protected ABot createBot(BotID botId, ICommandSink commandSink)
	{
		return new TigerBot(botId, commandSink, configDatabase.getDatabase());
	}


	@Override
	public void onNewWorldFrame(WorldFrameWrapper wFrameWrapper)
	{
		matchBroadcast.setStrictVelocityLimit(wFrameWrapper.getGameState().isVelocityLimited());
	}


	@Override
	public void sendCommand(ACommand cmd)
	{
		super.sendCommand(cmd);
		baseStation.enqueueCommand(cmd);
		recordCommand(cmd, EMessageType.TIGERS_BASE_STATION_CMD_SENT);
	}


	private void sendBroadcast()
	{
		BaseStationBroadcast broadcast = new BaseStationBroadcast();

		broadcast.setKickerAutocharge(matchBroadcast.isAutoCharge());
		broadcast.setStrictVelocityLimit(matchBroadcast.isStrictVelocityLimit());
		broadcast.setUnixTime(System.currentTimeMillis() / 1000L);
		broadcast.setAllocatedBotIds(getAllocatedBots());

		sendCommand(broadcast);
	}


	private void onIncomingCommand(ACommand cmd)
	{
		recordCommand(cmd, EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED);
		latencyTester.processIncomingCommand(cmd);
		processIncommingCommand(cmd);
	}


	private void recordCommand(final ACommand cmd, EMessageType type)
	{
		if (gameLogRecorder != null)
		{
			byte[] data = CommandFactory.getInstance().encode(cmd);
			gameLogRecorder.writeMessage(new GameLogMessage(NanoTime.getTimestampNow(), type, data));
		}
	}


	private void onNetworkStateChanged(ENetworkState oldState, ENetworkState newState)
	{
		if (newState != ENetworkState.ONLINE)
		{
			var connectedBots = new HashSet<>(getBots().keySet());
			connectedBots.forEach(this::removeBot);
		}
	}


	public void chargeAll()
	{
		setAutoCharge(true);
	}


	public void dischargeAll()
	{
		setAutoCharge(false);
	}


	private void setAutoCharge(final boolean autoCharge)
	{
		matchBroadcast.setAutoCharge(autoCharge);
		SumatraModel.getInstance().setUserProperty(PROP_AUTO_CHARGE, autoCharge);
	}


	private void onViewportUpdated(Viewport viewport)
	{
		BaseStationCameraViewport cmd = new BaseStationCameraViewport(viewport.getCameraId(), viewport.getArea());
		getBaseStation().enqueueCommand(cmd);
	}


	@Override
	protected void removeBot(BotID botId)
	{
		getBot(botId).ifPresent(TigerBot::stop);
		super.removeBot(botId);
	}
}

