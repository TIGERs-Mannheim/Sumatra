/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.basestation;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationAuth;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationConfigV3;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.RobustUnicastUdpTransceiver;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.observer.StateDistributor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Base64;


/**
 * Base station implementation for TIGERs bots
 */
@Log4j2
public class TigersBaseStation implements IConfigObserver
{
	private static final String CONFIG_CAT = "botmgr";
	private static final int BASE_STATION_TIMEOUT = 1000;

	@Configurable(defValue = "192.168.60.210", spezis = { "ROBOCUP", "LAB", "ANDRE", "TISCH" })
	private static String host = "192.168.60.210";

	@Configurable(defValue = "10201")
	private static int dstPort = 10201;

	@Getter
	@Configurable(spezis = { "ROBOCUP", "LAB", "ANDRE", "TISCH" }, defValue = "20")
	private static int channel = 20;

	@Configurable(comment = "Fix the runtime regardless of the number of bot that are connected.", defValue = "false")
	private static boolean fixedRuntime = false;

	@Configurable(comment = "Max communication slots to open for communication to bots", defValue = "11")
	private static int maxBots = 11;

	private RobustUnicastUdpTransceiver transceiver;

	@Getter
	private final StateDistributor<ENetworkState> networkState = new StateDistributor<>(ENetworkState.OFFLINE);

	@Getter
	private final EventDistributor<ACommand> onIncomingCommand = new EventDistributor<>();

	private int visionPort = -1;
	private String visionAddress = "";
	private boolean isAnyCmdReceived = false;


	static
	{
		ConfigRegistration.registerClass(CONFIG_CAT, TigersBaseStation.class);
	}


	public void enqueueCommand(final ACommand cmd)
	{
		if (transceiver != null)
		{
			byte[] data = CommandFactory.getInstance().encode(cmd);
			if (data.length > 0)
			{
				transceiver.send(data);
			}
		}
	}


	public void connect()
	{
		ConfigRegistration.applySpezi(CONFIG_CAT, SumatraModel.getInstance().getEnvironment());

		if (transceiver == null)
		{
			SumatraModel.getInstance().getModuleOpt(SSLVisionCam.class).ifPresent(cam -> {
				visionAddress = cam.getAddress();
				visionPort = cam.getPort();
			});

			transceiver = new RobustUnicastUdpTransceiver(host, dstPort, BASE_STATION_TIMEOUT);
			transceiver.setStateConsumer(this::onTransceiverStateChanged);
			transceiver.setResponseConsumer(this::onTransceiverDataReceived);
			transceiver.start();
		}

		// user config is needed for vision port.
		ConfigRegistration.registerConfigurableCallback("user", this);
		// get notified when bot manager config has been updated
		ConfigRegistration.registerConfigurableCallback(CONFIG_CAT, this);
	}


	public void disconnect()
	{
		if (transceiver != null)
		{
			transceiver.stop();
			transceiver = null;
		}

		ConfigRegistration.unregisterConfigurableCallback(CONFIG_CAT, this);
		ConfigRegistration.unregisterConfigurableCallback("user", this);
	}


	public void activateDefaultConfig()
	{
		BaseStationConfigV3 config = new BaseStationConfigV3();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		config.setChannel(channel);
		config.setFixedRuntime(fixedRuntime);
		config.setMaxBots(maxBots);
		enqueueCommand(config);
	}


	public void activateDataBurstMode()
	{
		BaseStationConfigV3 config = new BaseStationConfigV3();
		config.setVisionIp(visionAddress);
		config.setVisionPort(visionPort);
		config.setChannel(channel);
		config.setFixedRuntime(true);
		config.setMaxBots(1);
		enqueueCommand(config);
	}


	@Override
	public void afterApply(final IConfigClient configClient)
	{
		// reconnect to make sure all config changes are applied
		disconnect();
		connect();
	}


	private void onTransceiverStateChanged(RobustUnicastUdpTransceiver.State newState)
	{
		switch (newState)
		{
			case DISCONNECTED ->
			{
				if (isAnyCmdReceived)
				{
					log.info("Disconnected base station");
					isAnyCmdReceived = false;
				}

				networkState.set(ENetworkState.OFFLINE);
			}
			case CONNECTING ->
			{
				log.debug("Base station connecting");
				networkState.set(ENetworkState.CONNECTING);
			}
			case CONNECTED ->
			{
				isAnyCmdReceived = false;
				enqueueCommand(new BaseStationAuth());
				activateDefaultConfig();
			}
		}
	}


	private void onTransceiverDataReceived(byte[] bytes)
	{
		final ACommand cmd = CommandFactory.getInstance().decode(bytes);
		if (cmd == null)
		{
			log.warn("Error decoding command: {}", Base64.getEncoder().encode(bytes));
		} else
		{
			if (!isAnyCmdReceived)
			{
				log.info("Connected base station");
				networkState.set(ENetworkState.ONLINE);
				isAnyCmdReceived = true;
			}

			onIncomingCommand.newEvent(cmd);
		}
	}
}
