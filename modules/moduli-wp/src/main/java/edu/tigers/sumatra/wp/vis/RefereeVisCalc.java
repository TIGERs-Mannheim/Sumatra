/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameEventProposalGroup;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Visualize referee stuff
 */
public class RefereeVisCalc implements IWpCalc
{
	public static final int BORDER_TEXT_HEIGHT = 55;
	private final DecimalFormat df2 = new DecimalFormat("00");
	private final DecimalFormat dfSeconds = new DecimalFormat("00.000");
	private final DecimalFormat dfBallVel = new DecimalFormat("0.00");
	private final double[] offsetsX = new double[9];


	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> txtShapes = new ArrayList<>();
		RefereeMsg msg = wfw.getRefereeMsg();

		if (msg == null)
		{
			return;
		}

		// Time
		final long min = TimeUnit.MICROSECONDS.toMinutes(msg.getStageTimeLeft());
		final long sec = TimeUnit.MICROSECONDS.toSeconds(msg.getStageTimeLeft()) - (60 * min);
		String timeStr =
				(msg.getStageTimeLeft() < 0 ? "-" : "") + df2.format(Math.abs(min)) + ":" + df2.format(Math.abs(sec));

		// Timeouts
		String timeoutYellowStr = getTimeoutString(msg.getTeamInfoYellow());
		String timeoutBlueStr = getTimeoutString(msg.getTeamInfoBlue());

		// Yellow cards
		String yellowCardYellowStr = getYellowCardString(msg.getTeamInfoYellow().getYellowCards(), msg
				.getTeamInfoYellow().getYellowCardsTimes());
		String yellowCardBlueStr = getYellowCardString(msg.getTeamInfoBlue().getYellowCards(), msg.getTeamInfoBlue()
				.getYellowCardsTimes());

		// Foul counter
		String foulCounterYellowStr = getFoulCounterString(msg.getTeamInfoYellow().getFoulCounter());
		String foulCounterBlueStr = getFoulCounterString(msg.getTeamInfoBlue().getFoulCounter());

		double ballSpeed = wfw.getSimpleWorldFrame().getBall().getVel3().getLength();
		double initBallSpeed = wfw.getSimpleWorldFrame().getKickFitState()
				.map(BallKickFitState::getAbsoluteKickSpeed).orElse(0.0);
		double ballHeight = wfw.getSimpleWorldFrame().getBall().getPos3().z();
		String ballVelStr = "Ball vel: " + dfBallVel.format(ballSpeed) + "| "
				+ dfBallVel.format(initBallSpeed) + "; height: " + dfBallVel.format(ballHeight);

		initializeOffsets(msg);

		Color ballSpeedColor = ballSpeed <= RuleConstraints.getMaxBallSpeed() ? Color.white : Color.red;
		txtShapes.add(text(1, 2, ballVelStr).setColor(ballSpeedColor));

		txtShapes.add(text(0, 0, msg.getStage().toString()));
		txtShapes.add(text(0, 1, msg.getCommand().toString()));
		txtShapes.add(text(0, 2, wfw.getGameState().getStateNameWithColor()));
		if (msg.getCurrentActionTimeRemaining() >= 0)
		{
			txtShapes.add(text(0, 3, dfSeconds.format(msg.getCurrentActionTimeRemaining())));
		}
		txtShapes.add(text(1, 0, timeStr));

		// Team YELLOW
		txtShapes.add(text(2, 0, String.valueOf(msg.getTeamInfoYellow().getScore())).setColor(Color.yellow));
		txtShapes.add(text(4, 0, msg.getTeamInfoYellow().getName()).setColor(Color.yellow));
		txtShapes.add(text(5, 0, timeoutYellowStr).setColor(Color.yellow));
		txtShapes.add(text(6, 0, foulCounterYellowStr).setColor(Color.yellow));
		txtShapes.add(text(7, 0, yellowCardYellowStr).setColor(Color.yellow));

		// Team BLUE
		txtShapes.add(text(2, 1, String.valueOf(msg.getTeamInfoBlue().getScore())).setColor(Color.blue));
		txtShapes.add(text(4, 1, msg.getTeamInfoBlue().getName()).setColor(Color.blue));
		txtShapes.add(text(5, 1, timeoutBlueStr).setColor(Color.blue));
		txtShapes.add(text(6, 1, foulCounterBlueStr).setColor(Color.blue));
		txtShapes.add(text(7, 1, yellowCardBlueStr).setColor(Color.blue));


		String nextCommand = msg.getNextCommand() == null ? "" : msg.getNextCommand().name();
		String nextState = wfw.getGameState().getNextStateNameWithColor();
		String nextStateAndCommand = "next: " + nextState + " (" + nextCommand + ")";
		String gameEvents = "Events: " + msg.getGameEvents().stream().map(IGameEvent::getType).map(Enum::name)
				.collect(Collectors.joining(","));
		String proposedGameEvents = "Proposed: " + proposedGameEventGroups(msg.getGameEventProposalGroups());


		txtShapes.add(text(8, 0, nextStateAndCommand));
		txtShapes.add(text(8, 1, gameEvents));
		txtShapes.add(text(8, 2, proposedGameEvents));
		txtShapes.add(text(8, 3, getSubstitutionString(msg)));

		shapeMap.get(EWpShapesLayer.REFEREE).addAll(txtShapes);
		paintShapes(shapeMap.get(EWpShapesLayer.REFEREE), wfw);
	}


	private IDrawableShape text(int column, int row, String text)
	{
		Vector2 position = getPosition(column, row);

		return new DrawableBorderText(position, text)
				.setColor(Color.white);
	}


	private String getSubstitutionString(RefereeMsg msg)
	{
		String substString = "";
		if (msg.getTeamInfo(ETeamColor.BLUE).isBotSubstitutionIntent())
		{
			substString += " [blue]";
		}
		if (msg.getTeamInfo(ETeamColor.YELLOW).isBotSubstitutionIntent())
		{
			substString += " [yellow]";
		}
		if (substString.length() > 0)
		{
			substString = "Substitution Intents:" + substString;
		}
		return substString;
	}


	private void initializeOffsets(RefereeMsg msg)
	{
		offsetsX[0] = 1.0;
		offsetsX[1] = offsetsX[0] + 12.5;
		offsetsX[2] = offsetsX[1] + 4.0;
		offsetsX[3] = offsetsX[2] + 1.3;
		offsetsX[4] = offsetsX[3] + 2.0;
		offsetsX[5] = offsetsX[4] + 9.0;
		offsetsX[6] = offsetsX[5] + 7.0;
		offsetsX[7] = offsetsX[6] + 4.0;
		// Set the offset after the yellow cards string to grow with the number of timers to prevent overlaps
		offsetsX[8] = offsetsX[7] + 4.0 +
				2.5 * Math.max(msg.getTeamInfoYellow().getYellowCardsTimes().size(),
						msg.getTeamInfoBlue().getYellowCardsTimes().size());
	}


	private Vector2 getPosition(int column, int row)
	{
		return Vector2.fromXY(
				offsetsX[column],
				row + 1.1
		);
	}


	private void paintShapes(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		if (wfw.getGameState().getBallPlacementPositionNeutral() != null)
		{
			paintAutomatedPlacementShapes(shapes, wfw);
		}
		if (wfw.getGameState().isDistanceToBallRequired())
		{
			paintBallDistanceCircle(shapes, wfw);
		}
	}


	private void paintBallDistanceCircle(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		double radius = RuleConstraints.getStopRadius();
		Color circleColor = Color.red;
		if (wfw.getGameState().isBallPlacement())
		{
			circleColor = wfw.getGameState().getForTeam() == ETeamColor.BLUE ? Color.YELLOW : Color.BLUE;
		}

		IVector2 ballPos = wfw.getSimpleWorldFrame().getBall().getPos();
		DrawableCircle circle = new DrawableCircle(Circle.createCircle(ballPos, radius), circleColor);
		shapes.add(circle);

		double dist = Geometry.getPenaltyAreaOur().nearestPointInside(ballPos).distanceTo(ballPos) + radius
				+ RuleConstraints.getBotToPenaltyAreaMarginStandard() + 200;
		if (Geometry.getGoalOur().getCenter().distanceTo(ballPos) < dist ||
				Geometry.getGoalTheir().getCenter().distanceTo(ballPos) < dist)
		{
			DrawableCircle outerCircle = new DrawableCircle(Circle.createCircle(ballPos,
					radius + RuleConstraints.getBotToPenaltyAreaMarginStandard()), Color.orange);
			shapes.add(outerCircle);
		}
	}


	/**
	 * @param shapes
	 * @param wfw
	 */
	private void paintAutomatedPlacementShapes(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		RefereeMsg refMsg = wfw.getRefereeMsg();
		IVector2 ballTargetPos = wfw.getGameState().getBallPlacementPositionNeutral();

		Color distToBallColor = new Color(255, 0, 0, 100);
		Color targetCircleColor = new Color(20, 255, 255, 210);
		switch (refMsg.getCommand())
		{
			case BALL_PLACEMENT_BLUE ->
			{
				distToBallColor = Color.YELLOW;
				targetCircleColor = Color.BLUE;
			}
			case BALL_PLACEMENT_YELLOW ->
			{
				distToBallColor = Color.BLUE;
				targetCircleColor = Color.YELLOW;
			}
			default ->
			{
				// nothing
			}
		}

		shapes.add(new DrawableCircle(
				Circle.createCircle(wfw.getSimpleWorldFrame().getBall().getPos(), RuleConstraints.getStopRadius()),
				distToBallColor
		));
		shapes.add(new DrawableCircle(
				Circle.createCircle(ballTargetPos, RuleConstraints.getBallPlacementTolerance()),
				targetCircleColor
		));
		shapes.add(new DrawablePoint(ballTargetPos, Color.RED));
	}


	private String getYellowCardString(final int cards, final List<Integer> times)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("YC: ");
		sb.append(cards);
		for (Integer t : times)
		{
			long minYcTo = TimeUnit.MICROSECONDS.toMinutes(t);
			long secYcTo = TimeUnit.MICROSECONDS.toSeconds(t) - (60 * minYcTo);
			sb.append(" (");
			sb.append(df2.format(minYcTo));
			sb.append(":");
			sb.append(df2.format(secYcTo));
			sb.append(")");
		}
		return sb.toString();
	}


	private String getFoulCounterString(int count)
	{
		return "FC: " + count;
	}


	private String getTimeoutString(final TeamInfo teamInfo)
	{
		long minTo = TimeUnit.MICROSECONDS.toMinutes(teamInfo.getTimeoutTime());
		long secTo = TimeUnit.MICROSECONDS.toSeconds(teamInfo.getTimeoutTime()) - (60 * minTo);
		return "TO: " + teamInfo.getTimeouts() + " (" + df2.format(minTo) + ":"
				+ df2.format(secTo)
				+ ")";
	}


	private String proposedGameEventGroups(List<GameEventProposalGroup> groups)
	{
		return groups.stream()
				.map(this::proposedGameEventGroup)
				.collect(Collectors.joining(","));
	}


	private String proposedGameEventGroup(GameEventProposalGroup group)
	{
		return "[" +
				group.getGameEvents().stream()
						.map(IGameEvent::getType)
						.map(Enum::name)
						.collect(Collectors.joining(","))
				+ "]";
	}
}
