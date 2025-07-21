/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABufferedPersistenceRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;


/**
 * Persistance recorder for AI data
 */
@Log4j2
public class WfwPersistenceRecorder extends ABufferedPersistenceRecorder<WorldFrameWrapper>
		implements IWorldFrameObserver
{

	public WfwPersistenceRecorder(PersistenceDb db)
	{
		super(db, WorldFrameWrapper.class);
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(this);
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		queue(wFrameWrapper);
	}
}
