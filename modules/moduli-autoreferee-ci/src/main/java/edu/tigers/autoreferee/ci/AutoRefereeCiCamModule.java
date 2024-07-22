/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.ci;

import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.SSLVisionCamGeometryTranslator;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.SslVisionDetection;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.TrackerPacketGenerator;
import edu.tigers.sumatra.wp.WorldInfoCollector;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked.TrackerWrapperPacket;


public class AutoRefereeCiCamModule extends ACam implements IWorldFrameObserver
{
	private final SSLVisionCamGeometryTranslator geometryTranslator = new SSLVisionCamGeometryTranslator();
	private final TrackedFrameToFilteredVisionMapper trackedFrameToFilteredVisionMapper = new TrackedFrameToFilteredVisionMapper();
	private final TrackerPacketGenerator trackerPacketGenerator = new TrackerPacketGenerator("TIGERs");
	private final AutoRefereeCiServer autoRefereeCiServer = new AutoRefereeCiServer(
			this::publishDetection,
			this::publishGeometry,
			this::publishReferee,
			this::publishTrackedWrapperFrame
	);

	private DirectRefereeMsgForwarder refForwarder;


	@Override
	public void startModule() throws StartModuleException
	{
		super.startModule();
		SumatraModel.getInstance().getModule(WorldInfoCollector.class).addObserver(this);
		AReferee ref = SumatraModel.getInstance().getModule(AReferee.class);
		refForwarder = (DirectRefereeMsgForwarder) ref.getSource(ERefereeMessageSource.INTERNAL_FORWARDER);
		int port = getSubnodeConfiguration().getInt("port", 10013);
		autoRefereeCiServer.setPort(port);
		autoRefereeCiServer.start();
	}


	@Override
	public void stopModule()
	{
		autoRefereeCiServer.stop();
		super.stopModule();
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfw)
	{
		SslVisionWrapperTracked.TrackerWrapperPacket packet = trackerPacketGenerator.generate(wfw.getSimpleWorldFrame());
		autoRefereeCiServer.publish(packet);
	}


	private void publishReferee(SslGcRefereeMessage.Referee referee)
	{
		refForwarder.send(referee);
	}


	private void publishGeometry(SslVisionGeometry.SSL_GeometryData geometryData)
	{
		CamGeometry geometry = geometryTranslator.fromProtobuf(geometryData);
		notifyNewCameraCalibration(geometry);
	}


	private void publishDetection(SslVisionDetection.SSL_DetectionFrame detectionFrame)
	{
		notifyNewCameraFrame(detectionFrame);
	}


	private void publishTrackedWrapperFrame(TrackerWrapperPacket wrapper)
	{
		FilteredVisionFrame filteredVisionFrame = trackedFrameToFilteredVisionMapper.map(wrapper.getTrackedFrame());
		SumatraModel.getInstance().getModule(AVisionFilter.class).publishFilteredVisionFrame(filteredVisionFrame);
	}
}
