/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import edu.tigers.autoreferee.engine.calc.AllowedDistancesAutoRefVisCalc;
import edu.tigers.autoreferee.engine.calc.BallLeftFieldAutoRefCalc;
import edu.tigers.autoreferee.engine.calc.BotBallContactAutoRefCalc;
import edu.tigers.autoreferee.engine.calc.GameStateHistoryAutoRefCalc;
import edu.tigers.autoreferee.engine.calc.IAutoRefereeCalc;
import edu.tigers.autoreferee.engine.calc.PassDetectionAutoRefCalc;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.util.ArrayList;
import java.util.List;


/**
 * The preprocessor runs some calculators to gather some generic information.
 */
public class AutoRefFramePreprocessor
{
	private List<IAutoRefereeCalc> calculators = new ArrayList<>();
	private AutoRefFrame lastFrame;
	
	
	public AutoRefFramePreprocessor()
	{
		calculators.add(new BallLeftFieldAutoRefCalc());
		calculators.add(new BotBallContactAutoRefCalc());
		calculators.add(new GameStateHistoryAutoRefCalc());
		calculators.add(new AllowedDistancesAutoRefVisCalc());
		calculators.add(new PassDetectionAutoRefCalc());
	}
	
	
	public AutoRefFrame process(final WorldFrameWrapper wFrame)
	{
		AutoRefFrame frame = new AutoRefFrame(lastFrame, wFrame);
		
		if (lastFrame != null)
		{
			// We can only run the calculators if we have a last frame.
			runCalculators(frame);
		}
		setLastFrame(frame);
		return frame;
	}
	
	
	private void setLastFrame(final AutoRefFrame frame)
	{
		if (lastFrame != null)
		{
			lastFrame.cleanUp();
		}
		lastFrame = frame;
	}
	
	
	private void runCalculators(final AutoRefFrame frame)
	{
		for (IAutoRefereeCalc calc : calculators)
		{
			calc.process(frame);
		}
	}
	
	
	public boolean hasLastFrame()
	{
		return lastFrame != null;
	}
}
