/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee;

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


public class AutoRefRecordManager extends RecordManager
{

	@Override
	protected void onNewPersistenceDb(PersistenceDb db)
	{
		super.onNewPersistenceDb(db);
		db.add(PersistenceCamDetectionFrame.class, EPersistenceKeyType.ARBITRARY);
		db.add(PersistenceShapeMapFrame.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
	}


	@Override
	protected void onNewPersistanceRecorder(final PersistenceAsyncRecorder recorder)
	{
		super.onNewPersistanceRecorder(recorder);
		recorder.add(new CamFramePersistenceRecorder(recorder.getDb()));
		recorder.add(new WfwPersistenceRecorder(recorder.getDb()));
		recorder.add(new ShapeMapPersistenceRecorder(recorder.getDb()));
	}

}
