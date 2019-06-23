/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.ballplacement;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.throwin.ABallPlacementRole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author MarkG
 */

public class DesiredBallPlacementBotsCalc extends ACalculator
{
	private long startTime = 0;
	private final Hysteresis insidePushRadiusHysteresis = new Hysteresis(
			ABallPlacementRole.getPushBallVsPassDistance() - 500,
			ABallPlacementRole.getPushBallVsPassDistance() + 500);
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (aiFrame.getGamestate().isBallPlacementForUs() && getWFrame().getTigerBotsAvailable().size() > 1)
		{
			return true;
		}
		
		startTime = getWFrame().getTimestamp();
		return false;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setBallInPushRadius(isBallInsidePushRadius());
		newTacticalField.addDesiredBots(EPlay.BALL_PLACEMENT, getDesiredBots(newTacticalField));
		drawShapes(newTacticalField);
	}
	
	
	private Set<BotID> getDesiredBots(final TacticalField newTacticalField)
	{
		Set<BotID> lastDesiredBots = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.get(EPlay.BALL_PLACEMENT);
		if (lastDesiredBots == null || lastDesiredBots.isEmpty())
		{
			return calcNewDesiredBots(newTacticalField);
		} else
		{
			return lastDesiredBots;
		}
	}
	
	
	private Set<BotID> calcNewDesiredBots(final TacticalField newTacticalField)
	{
		Set<BotID> desiredBots = new HashSet<>();
		
		List<BotDistance> availableBots = new ArrayList<>(newTacticalField.getTigersToBallDist());
		availableBots.removeIf(b -> b.getBot().getBotId().equals(getAiFrame().getKeeperId()));
		
		if (availableBots.isEmpty())
		{
			return desiredBots;
		}
		desiredBots.add(availableBots.remove(0).getBot().getBotId());
		if (availableBots.isEmpty() || newTacticalField.isBallInPushRadius())
		{
			return desiredBots;
		}
		availableBots.sort(Comparator.comparingDouble(
				b -> b.getBot().getPos().distanceTo(getAiFrame().getGamestate().getBallPlacementPositionForUs())));
		desiredBots.add(availableBots.remove(0).getBot().getBotId());
		return desiredBots;
	}
	
	
	private boolean isBallInsidePushRadius()
	{
		insidePushRadiusHysteresis
				.update(getAiFrame().getGamestate().getBallPlacementPositionForUs().distanceTo(getBall().getPos()));
		return insidePushRadiusHysteresis.isLower();
	}
	
	
	private void drawShapes(final TacticalField newTacticalField)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		String msg = "PREPARE_TIME: "
				+ (((int) (((getWFrame().getTimestamp() - startTime) * 1e-9) * 100)) / 100.0) + "s";
		
		GameState gameState = getAiFrame().getGamestate();
		showText(shapes, msg, 0);
		if (gameState.isBallPlacementForUs())
		{
			showText(shapes, gameState.getBallPlacementPositionForUs().toString(), 130);
		}
		
		DrawableCircle targetCircle = new DrawableCircle(
				Circle.createCircle(gameState.getBallPlacementPositionForUs(),
						RuleConstraints.getBallPlacementTolerance()),
				new Color(20, 255, 255, 120));
		targetCircle.setFill(true);
		shapes.add(targetCircle);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_BALL_PLACEMENT).addAll(shapes);
	}
	
	
	private void showText(List<IDrawableShape> shapes, final String msg, final double offset)
	{
		DrawableBorderText text = new DrawableBorderText(Vector2.fromXY(450.0 + offset, 12.0), msg, Color.white);
		text.setFontSize(10);
		shapes.add(text);
	}
}
