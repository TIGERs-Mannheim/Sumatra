/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.ballplacement;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.ABallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author MarkG
 */
public class DesiredBallPlacementBotsCalc extends ADesiredBotCalc
{
	@Configurable(defValue = "true")
	private static boolean useSecondBallPlacer = true;

	private long startTime = 0;
	private final Hysteresis insidePushRadiusHysteresis = new Hysteresis(
			ABallPlacementRole.getPushBallVsPassDistance() - 500,
			ABallPlacementRole.getPushBallVsPassDistance() + 500);


	public DesiredBallPlacementBotsCalc()
	{
		super(EPlay.BALL_PLACEMENT);
	}


	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (getWFrame().getTigerBotsAvailable().size() > 1
				// calculation required if placement pos set -> testing mode
				&& (getPlacementPos() != null || getAiFrame().getGamestate().isBallPlacementForUs()))
		{
			return true;
		}

		startTime = getWFrame().getTimestamp();
		return false;
	}


	@Override
	public void doCalc()
	{
		getNewTacticalField().setBallInPushRadius(isBallInsidePushRadius());
		if (getAiFrame().getGamestate().isBallPlacementForUs())
		{
			addDesiredBots(getDesiredBots());
		}
		drawShapes();
	}


	private IVector2 getPlacementPos()
	{
		return getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles(ERole.PRIMARY_BALL_PLACEMENT).stream()
				.findFirst()
				.map(r -> (PrimaryBallPlacementRole) r)
				.map(ABallPlacementRole::getPlacementPos)
				.orElse(getAiFrame().getGamestate().getBallPlacementPositionForUs());
	}


	private Set<BotID> getDesiredBots()
	{
		Set<BotID> lastDesiredBots = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.BALL_PLACEMENT, Collections.emptySet()).stream()
				.filter(this::isAssignable)
				.collect(Collectors.toSet());
		final Set<BotID> newDesiredBots = calcNewDesiredBots();
		if (lastDesiredBots.size() < newDesiredBots.size())
		{
			return newDesiredBots;
		} else
		{
			return lastDesiredBots;
		}
	}


	private Set<BotID> calcNewDesiredBots()
	{
		Set<BotID> desiredBots = new HashSet<>();

		List<ITrackedBot> availableBots = getNewTacticalField().getTigersToBallDist().stream()
				.map(BotDistance::getBot)
				.collect(Collectors.toList());
		availableBots.removeIf(b -> b.getBotId().equals(getAiFrame().getKeeperId()));
		availableBots.removeIf(b -> !getWFrame().getTigerBotsAvailable().keySet().contains(b.getBotId()));
		availableBots.removeIf(b -> getAlreadyAssignedBots().contains(b.getBotId()));

		if (availableBots.isEmpty())
		{
			return desiredBots;
		}

		if (getNewTacticalField().isInsaneKeeper() && getWFrame().getBots().containsKey(getAiFrame().getKeeperId()))
		{
			availableBots.add(0, getWFrame().getBot(getAiFrame().getKeeperId()));
		}

		desiredBots.add(availableBots.remove(0).getBotId());
		if (availableBots.isEmpty() || getNewTacticalField().isBallInPushRadius())
		{
			return desiredBots;
		}
		if (useSecondBallPlacer)
		{
			availableBots.sort(Comparator.comparingDouble(
					b -> b.getPos().distanceTo(getPlacementPos())));
			desiredBots.add(availableBots.remove(0).getBotId());
		}
		return desiredBots;
	}


	private boolean isBallInsidePushRadius()
	{
		insidePushRadiusHysteresis
				.update(getPlacementPos().distanceTo(getBall().getPos()));
		return insidePushRadiusHysteresis.isLower();
	}


	private void drawShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		String msg = "PREPARE_TIME: "
				+ (((int) (((getWFrame().getTimestamp() - startTime) * 1e-9) * 100)) / 100.0) + "s";

		GameState gameState = getAiFrame().getGamestate();
		showText(shapes, msg, 0);
		if (gameState.isBallPlacementForUs())
		{
			showText(shapes, getPlacementPos().toString(), 130);
		}

		DrawableCircle targetCircle = new DrawableCircle(
				Circle.createCircle(getPlacementPos(),
						RuleConstraints.getBallPlacementTolerance()),
				new Color(20, 255, 255, 120));
		targetCircle.setFill(true);
		shapes.add(targetCircle);
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_PLACEMENT).addAll(shapes);
	}


	private void showText(List<IDrawableShape> shapes, final String msg, final double offset)
	{
		DrawableBorderText text = new DrawableBorderText(Vector2.fromXY(450.0 + offset, 12.0), msg, Color.white);
		text.setFontSize(10);
		shapes.add(text);
	}
}
