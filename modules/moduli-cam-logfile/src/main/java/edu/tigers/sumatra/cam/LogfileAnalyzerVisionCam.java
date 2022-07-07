/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;

import java.util.HashMap;
import java.util.Map;


public class LogfileAnalyzerVisionCam extends ACam
{
	/**
	 * If jump in time is larger than this, a visionLost() is triggered. [s]
	 */
	private static final double TIME_LEAP_TOLERANCE = 0.1;

	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();
	private final Map<Long, Long> timestampToFrameId = new HashMap<>();

	private DirectRefereeMsgForwarder refForwarder;
	private long lastFrameTimestamp = 0;


	@Override
	public void startModule()
	{
		AReferee ref = SumatraModel.getInstance().getModule(AReferee.class);
		refForwarder = (DirectRefereeMsgForwarder) ref.getSource(ERefereeMessageSource.INTERNAL_FORWARDER);
	}


	public void playLog(SSLGameLogReader logReader, LogfileAnalyzerConsumer consumer)
	{
		int amountFrames = logReader.getPackets().size();
		long groupId = 0;
		for (int currentFrame = 0; currentFrame < amountFrames; currentFrame++)
		{
			SSLGameLogReader.SSLGameLogfileEntry e = logReader.getPackets().get(currentFrame);

			if (groupId < e.getFrameId())
			{
				consumer.process(e.getFrameId());
				groupId = e.getFrameId();
			}

			if (e.getVisionPacket().isPresent())
			{
				var sslPacket = e.getVisionPacket().get();

				notifyNewVisionPacket(sslPacket);

				if (sslPacket.hasGeometry())
				{
					final CamGeometry geometry = geometryTranslator.fromProtobuf(sslPacket.getGeometry());

					notifyNewCameraCalibration(geometry);
				}

				if (sslPacket.hasDetection())
				{
					notifyNewCameraFrame(sslPacket.getDetection());
					long ts = (long) (sslPacket.getDetection().getTCapture() * 1e9);
					timestampToFrameId.put(ts, e.getFrameId());
				}
			}

			if (e.getRefereePacket().isPresent())
			{
				refForwarder.send(e.getRefereePacket().get());
			}

			if (lastFrameTimestamp != 0 && (e.getTimestamp() - lastFrameTimestamp) / 1e9 >= TIME_LEAP_TOLERANCE)
			{
				// time jump
				notifyVisionLost();
			}

			lastFrameTimestamp = e.getTimestamp();
		}

		notifyVisionLost();
	}


	public Map<Long, Long> getTimestampToFrameId()
	{
		return timestampToFrameId;
	}
}
