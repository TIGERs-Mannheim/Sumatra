/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.gamelog.GameLogRecorder;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.EPersistenceKeyType;
import edu.tigers.sumatra.persistence.PersistenceAsyncRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.wp.CamFramePersistenceRecorder;
import edu.tigers.sumatra.wp.PersistenceShapeMapFrame;
import edu.tigers.sumatra.wp.ShapeMapPersistenceRecorder;
import edu.tigers.sumatra.wp.WfwPersistenceRecorder;
import edu.tigers.sumatra.wp.data.PersistenceCamDetectionFrame;
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
	protected void onNewPersistenceDb(PersistenceDb db)
	{
		super.onNewPersistenceDb(db);
		db.add(PersistenceAiFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(PersistenceCamDetectionFrame.class, EPersistenceKeyType.ARBITRARY);
		db.add(PersistenceShapeMapFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
	}


	@Override
	protected void onNewPersistanceRecorder(final PersistenceAsyncRecorder recorder)
	{
		super.onNewPersistanceRecorder(recorder);
		recorder.add(new AiPersistenceRecorder(recorder.getDb()));
		recorder.add(new CamFramePersistenceRecorder(recorder.getDb()));
		recorder.add(new WfwPersistenceRecorder(recorder.getDb()));
		recorder.add(new ShapeMapPersistenceRecorder(recorder.getDb()));
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
