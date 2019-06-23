/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import com.sleepycat.persist.evolve.Mutations;

import edu.tigers.sumatra.gamelog.SSLGameLogRecorder;
import edu.tigers.sumatra.persistence.BerkeleyAccessor;
import edu.tigers.sumatra.persistence.BerkeleyAsyncRecorder;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.wp.BerkeleyShapeMapFrame;
import edu.tigers.sumatra.wp.CamFrameBerkeleyRecorder;
import edu.tigers.sumatra.wp.ShapeMapBerkeleyRecorder;
import edu.tigers.sumatra.wp.WfwBerkeleyRecorder;
import edu.tigers.sumatra.wp.data.BerkeleyCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Manager for central control of recordings
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiRecordManager extends RecordManager
{
	private SSLGameLogRecorder gamelogRecorder = new SSLGameLogRecorder();
	
	
	@Override
	protected void onNewBerkeleyDb(final BerkeleyDb db)
	{
		super.onNewBerkeleyDb(db);
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.add(BerkeleyCamDetectionFrame.class, new BerkeleyAccessor<>(BerkeleyCamDetectionFrame.class, true));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));
		
		Mutations mutations = new Mutations();
		// add future mutations here
		
		db.getEnv().getStoreConfig().setMutations(mutations);
	}
	
	
	@Override
	protected void onNewBerkeleyRecorder(final BerkeleyAsyncRecorder recorder)
	{
		super.onNewBerkeleyRecorder(recorder);
		recorder.add(new AiBerkeleyRecorder(recorder.getDb()));
		recorder.add(new CamFrameBerkeleyRecorder(recorder.getDb()));
		recorder.add(new WfwBerkeleyRecorder(recorder.getDb()));
		recorder.add(new ShapeMapBerkeleyRecorder(recorder.getDb()));
	}
	
	
	@Override
	protected void startRecording()
	{
		super.startRecording();
		gamelogRecorder.start();
	}
	
	
	@Override
	protected void stopRecording()
	{
		super.stopRecording();
		gamelogRecorder.stop();
	}
}
