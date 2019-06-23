/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class DoubleTouchEvaderCalc extends ACalculator
{
	
	private static final Set<EGameState> VALID_PREVIOUS_STATES = Collections.unmodifiableSet(EnumSet.of(
			EGameState.KICKOFF, EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE));
	
	private IVector2 ballKickPos = null;
	private BotID kickerID = BotID.noBot();
	private boolean passedValidPrevState = false;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return aiFrame.getGamestate().getState() == EGameState.RUNNING;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		GameState prevState = baseAiFrame.getPrevFrame().getGamestate();
		if (prevState.isGameStateForUs() && VALID_PREVIOUS_STATES.contains(prevState.getState()))
		{
			passedValidPrevState = true;
			kickerID = newTacticalField.getBotsLastTouchedBall().stream().findAny().orElse(BotID.noBot());
			ballKickPos = baseAiFrame.getPrevFrame().getWorldFrame().getBall().getPos();
		} else if (passedValidPrevState)
		{
			if (!newTacticalField.getBotsLastTouchedBall().contains(kickerID))
			{
				newTacticalField.setBotNotAllowedToTouchBall(BotID.noBot());
				reset();
			} else if (getBall().getPos().distanceTo(ballKickPos) > 50)
			{
				newTacticalField.setBotNotAllowedToTouchBall(kickerID);
				drawShape(newTacticalField);
			}
		}
	}
	
	
	private void drawShape(ITacticalField tacticalField)
	{
		if (!kickerID.equals(BotID.noBot()))
		{
			tacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_POSSESSION)
					.add(new DrawableCircle(
							Circle.createCircle(getWFrame().getBot(kickerID).getPos(), Geometry.getBotRadius() * 2),
							Color.cyan));
		}
	}
	
	
	private void reset()
	{
		ballKickPos = null;
		kickerID = BotID.noBot();
		passedValidPrevState = false;
	}
}
