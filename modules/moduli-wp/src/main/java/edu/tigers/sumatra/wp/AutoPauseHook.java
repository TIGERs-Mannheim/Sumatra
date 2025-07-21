/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.IRecorderHook;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class AutoPauseHook implements IWorldFrameObserver, IRecorderHook
{

	@Override
	public void start()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stop()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (SumatraModel.getInstance().isTournamentMode() && wFrameWrapper.getGameState().isIdleGame())
		{
			SumatraModel.getInstance().getModule(RecordManager.class).pauseRecorder();
		} else
		{
			SumatraModel.getInstance().getModule(RecordManager.class).resumeRecorder();
		}
	}
}
