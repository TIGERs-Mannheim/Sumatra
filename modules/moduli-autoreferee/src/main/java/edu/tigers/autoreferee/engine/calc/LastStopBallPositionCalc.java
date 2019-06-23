/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 1, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * @author "Lukas Magel"
 */
public class LastStopBallPositionCalc implements IRefereeCalc
{
	private IVector2 lastPos = Vector2.ZERO_VECTOR;
	
	
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
