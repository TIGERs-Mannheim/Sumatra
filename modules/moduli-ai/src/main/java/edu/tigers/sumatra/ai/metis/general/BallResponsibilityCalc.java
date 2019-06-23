/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EBallResponsibility;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;


/**
 * Determine who is responsible for handling the ball
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallResponsibilityCalc extends ACalculator
{
	@Configurable(comment = "Additional margin to give to defense", defValue = "0.0")
	private static double margin = 0;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		EBallResponsibility lastBallResponsibility = baseAiFrame.getPrevFrame().getTacticalField()
				.getBallResponsibility();
		double hystMargin = 0;
		if (lastBallResponsibility == EBallResponsibility.DEFENSE)
		{
			hystMargin = 100;
		}

		if (!Geometry.getField().isPointInShape(getBall().getPos()))
		{
			newTacticalField.setBallResponsibility(EBallResponsibility.NO_ONE);
		} else if (Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(),
				Geometry.getBotRadius() * 4 + Geometry.getBallRadius() + margin + hystMargin)
				&& !newTacticalField.getGameState().isStandardSituationForUs())
		{
			newTacticalField.setBallResponsibility(EBallResponsibility.DEFENSE);
		} else
		{
			newTacticalField.setBallResponsibility(EBallResponsibility.OFFENSE);
		}
	}
}
