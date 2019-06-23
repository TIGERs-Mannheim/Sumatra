/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * @author "Lukas Magel"
 */
public class LastStopBallPositionCalc implements IRefereeCalc
{
	private IVector2 lastPos = Vector2f.ZERO_VECTOR;
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		if (frame.getGameState().getState() == EGameState.STOP)
		{
			lastPos = frame.getWorldFrame().getBall().getPos();
		}
		frame.setLastStopBallPosition(lastPos);
	}
	
}
