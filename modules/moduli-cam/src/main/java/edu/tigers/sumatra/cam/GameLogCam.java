/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.SslVisionWrapper;
import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogPlayerObserver;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;


/**
 * This camera listens to messages from GameLogPlayer.
 * If a vision message is received it is parsed from protobuf and published.
 */
@Log4j2
public class GameLogCam extends ACam implements GameLogPlayerObserver
{
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();


	@Override
	public void startModule()
	{
		SumatraModel.getInstance().getModule(GameLogPlayer.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(GameLogPlayer.class).removeObserver(this);
	}


	@Override
	public void onNewGameLogMessage(GameLogMessage message, int index)
	{
		if (message.getType() != EMessageType.SSL_VISION_2014)
			return;

		SslVisionWrapper.SSL_WrapperPacket sslPacket;
		try
		{
			sslPacket = SslVisionWrapper.SSL_WrapperPacket.parseFrom(message.getData());
		} catch (Exception err)
		{
			log.error("Invalid SSL_VISION_2014 package.", err);
			return;
		}

		notifyNewVisionPacket(sslPacket);

		if (sslPacket.hasGeometry())
		{
			final CamGeometry geometry = geometryTranslator.fromProtobuf(sslPacket.getGeometry());

			notifyNewCameraCalibration(geometry);
		}

		if (sslPacket.hasDetection())
		{
			notifyNewCameraFrame(sslPacket.getDetection());
		}
	}


	@Override
	public void onGameLogTimeJump()
	{
		notifyVisionLost();
	}
}
