/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.TrackerPacketGenerator;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;


/**
 * Export standardized vision tracking data.
 */
@Log4j2
public class VisionTrackerSender extends AModule implements IWorldFrameObserver
{
	private MulticastUDPTransmitter transmitter;
	private TrackerPacketGenerator trackerPacketGenerator;

	@Setter
	private static int customPort;

	@Setter
	private static String customAddress;


	@Override
	public void startModule()
	{
		String address = StringUtils.isNotBlank(customAddress) ?
				customAddress :
				getSubnodeConfiguration().getString("address", "224.5.23.2");
		int port = customPort > 0 ? customPort : getSubnodeConfiguration().getInt("port", 10010);
		transmitter = new MulticastUDPTransmitter(address, port);

		String nifName = getSubnodeConfiguration().getString("interface", null);
		if (nifName != null)
		{
			log.info("Publishing vision tracking packets to {}:{} ({})", address, port, nifName);
			transmitter.connectTo(nifName);
		} else
		{
			log.info("Publishing vision tracking packets to {}:{} (all interfaces)", address, port);
			transmitter.connectToAllInterfaces();
		}

		String sourceName = getSubnodeConfiguration().getString("source-name", "TIGERs");
		trackerPacketGenerator = new TrackerPacketGenerator(sourceName);

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfw)
	{
		SslVisionWrapperTracked.TrackerWrapperPacket packet = trackerPacketGenerator.generate(wfw.getSimpleWorldFrame());
		transmitter.send(packet.toByteArray());
	}

}
