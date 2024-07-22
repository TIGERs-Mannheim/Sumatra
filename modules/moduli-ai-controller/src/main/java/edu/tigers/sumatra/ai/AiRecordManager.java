/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.model.SumatraModel;
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
import lombok.extern.log4j.Log4j2;


/**
 * Manager for central control of recordings
 */
@Log4j2
public class AiRecordManager extends RecordManager
{
	private GameLogRecorder gameLogRecorder;


	@Override
	protected void onNewBerkeleyDb(final BerkeleyDb db)
	{
		super.onNewBerkeleyDb(db);
		db.add(BerkeleyAiFrame.class, new BerkeleyAccessor<>(BerkeleyAiFrame.class, true));
		db.add(BerkeleyCamDetectionFrame.class, new BerkeleyAccessor<>(BerkeleyCamDetectionFrame.class, true));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));

		db.getEnv().getStoreConfig().setMutations(getMutations());
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
	public void startModule()
	{
		super.startModule();
		gameLogRecorder = SumatraModel.getInstance().getModule(GameLogRecorder.class);
	}


	@Override
	protected void startRecording()
	{
		super.startRecording();
		gameLogRecorder.setMatchInfo(matchType, matchStage, teamYellow, teamBlue);
		gameLogRecorder.setRecording(true);
	}


	@Override
	protected void stopRecording()
	{
		super.stopRecording();
		gameLogRecorder.setRecording(false);
	}
}
