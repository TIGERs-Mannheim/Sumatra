/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee;

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


public class AutoRefRecordManager extends RecordManager
{

	@Override
	protected void onNewBerkeleyDb(final BerkeleyDb db)
	{
		super.onNewBerkeleyDb(db);
		db.add(BerkeleyCamDetectionFrame.class, new BerkeleyAccessor<>(BerkeleyCamDetectionFrame.class, true));
		db.add(BerkeleyShapeMapFrame.class, new BerkeleyAccessor<>(BerkeleyShapeMapFrame.class, true));
		db.add(WorldFrameWrapper.class, new BerkeleyAccessor<>(WorldFrameWrapper.class, true));

		db.getEnv().getStoreConfig().setMutations(getMutations());
	}


	@Override
	protected void onNewBerkeleyRecorder(final BerkeleyAsyncRecorder recorder)
	{
		super.onNewBerkeleyRecorder(recorder);
		recorder.add(new CamFrameBerkeleyRecorder(recorder.getDb()));
		recorder.add(new WfwBerkeleyRecorder(recorder.getDb()));
		recorder.add(new ShapeMapBerkeleyRecorder(recorder.getDb()));
	}

}
