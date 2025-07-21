/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.simulation;

import com.google.protobuf.InvalidProtocolBufferException;
import edu.tigers.sumatra.network.RobustUnicastUdpTransceiver;
import edu.tigers.sumatra.simulation.SslSimulationControl.SimulatorCommand;
import edu.tigers.sumatra.simulation.SslSimulationControl.SimulatorResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;


@Log4j2
@RequiredArgsConstructor
public class SimControlClient
{
	private RobustUnicastUdpTransceiver transceiver;
	@Setter
	private Consumer<SimulatorResponse> responseConsumer = b -> {
	};


	public void start(String host, int port)
	{
		transceiver = new RobustUnicastUdpTransceiver(host, port);
		transceiver.setResponseConsumer(this::parseResponse);
		transceiver.start();
	}


	private void parseResponse(byte[] data)
	{
		try
		{
			var response = SimulatorResponse.parseFrom(data);
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


	public void sendControlCommand(SimulatorCommand command)
	{
		if (transceiver != null)
		{
			transceiver.send(command.toByteArray());
		}
	}
}
