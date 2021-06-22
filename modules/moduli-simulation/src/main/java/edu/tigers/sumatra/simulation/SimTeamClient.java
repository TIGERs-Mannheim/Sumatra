/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.simulation;

import com.google.protobuf.InvalidProtocolBufferException;
import edu.tigers.sumatra.network.UdpTransceiver;
import edu.tigers.sumatra.simulation.SslSimulationRobotControl.RobotControl;
import edu.tigers.sumatra.simulation.SslSimulationRobotFeedback.RobotControlResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;


@Log4j2
@RequiredArgsConstructor
public class SimTeamClient
{
	private UdpTransceiver transceiver;
	@Setter
	private Consumer<RobotControlResponse> responseConsumer = b -> {
	};


	public void start(String host, int port)
	{
		transceiver = new UdpTransceiver(host, port);
		transceiver.setResponseConsumer(this::parseResponse);
		transceiver.start();
	}


	private void parseResponse(byte[] data)
	{
		try
		{
			var response = RobotControlResponse.parseFrom(data);
			responseConsumer.accept(response);
		} catch (InvalidProtocolBufferException e)
		{
			log.warn("Failed to parse response from {}", transceiver, e);
		}
	}


	public void stop()
	{
		if (transceiver != null)
		{
			transceiver.stop();
			transceiver = null;
		}
	}


	public void sendRobotControl(RobotControl robotControl)
	{
		if (transceiver != null)
		{
			transceiver.send(robotControl.toByteArray());
		}
	}
}
