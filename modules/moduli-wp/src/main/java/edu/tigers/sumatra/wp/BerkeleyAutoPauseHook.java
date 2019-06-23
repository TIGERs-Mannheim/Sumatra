/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.IBerkeleyRecorderHook;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class BerkeleyAutoPauseHook implements IWorldFrameObserver, IBerkeleyRecorderHook
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
		if (SumatraModel.getInstance().isProductive() && wFrameWrapper.getGameState().isIdleGame())
		{
			SumatraModel.getInstance().getModule(RecordManager.class).pauseRecorder();
		} else
		{
			SumatraModel.getInstance().getModule(RecordManager.class).resumeRecorder();
		}
	}
}
