/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.EFontSize;
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
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeVisCalc implements IWpCalc
{
	private final DecimalFormat df2 = new DecimalFormat("00");
	private final DecimalFormat dfSeconds = new DecimalFormat("00.000");
	private final DecimalFormat dfBallVel = new DecimalFormat("0.00");
	private final double[] offsetsX = new double[8];


	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<DrawableBorderText> txtShapes = new ArrayList<>();
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

		double ballSpeed = wfw.getSimpleWorldFrame().getBall().getVel3().getLength();
		double initBallSpeed = wfw.getSimpleWorldFrame().getKickFitState()
				.map(BallKickFitState::getAbsoluteKickSpeed).orElse(0.0);
		double ballHeight = wfw.getSimpleWorldFrame().getBall().getPos3().z();
		String ballVelStr = "Ball vel: " + dfBallVel.format(ballSpeed) + "| "
				+ dfBallVel.format(initBallSpeed) + "; height: " + dfBallVel.format(ballHeight);

		initializeOffsets(msg);

		txtShapes.add(new DrawableBorderText(getPosition(1, 2), ballVelStr,
				ballSpeed <= RuleConstraints.getMaxBallSpeed() ? Color.white
						: Color.red));

		txtShapes.add(new DrawableBorderText(getPosition(0, 0), msg.getStage().toString(),
				Color.white));
		txtShapes.add(new DrawableBorderText(getPosition(0, 1), msg.getCommand().toString(),
				Color.white));
		txtShapes
				.add(new DrawableBorderText(getPosition(0, 2), wfw.getGameState().getStateNameWithColor(),
						Color.white));
		if (msg.getCurrentActionTimeRemaining() >= 0)
		{
			txtShapes
					.add(new DrawableBorderText(getPosition(0, 3),
							dfSeconds.format(msg.getCurrentActionTimeRemaining()),
							Color.white));
		}

		txtShapes.add(new DrawableBorderText(getPosition(1, 0), timeStr, Color.white));


		// Team YELLOW
		txtShapes
				.add(new DrawableBorderText(getPosition(2, 0),
						String.valueOf(msg.getTeamInfoYellow().getScore()),
						Color.yellow));
		txtShapes
				.add(new DrawableBorderText(getPosition(4, 0),
						msg.getTeamInfoYellow().getName(),
						Color.yellow));
		txtShapes.add(new DrawableBorderText(getPosition(5, 0), timeoutYellowStr, Color.yellow));
		txtShapes.add(new DrawableBorderText(getPosition(6, 0), yellowCardYellowStr, Color.yellow));


		// Team BLUE
		txtShapes.add(new DrawableBorderText(getPosition(2, 1),
				String.valueOf(msg.getTeamInfoBlue().getScore()),
				Color.blue));

		txtShapes.add(
				new DrawableBorderText(getPosition(4, 1),
						msg.getTeamInfoBlue().getName(), Color.blue));
		txtShapes.add(new DrawableBorderText(getPosition(5, 1), timeoutBlueStr, Color.blue));
		txtShapes.add(new DrawableBorderText(getPosition(6, 1), yellowCardBlueStr, Color.blue));


		String nextCommand = msg.getNextCommand() == null ? "" : msg.getNextCommand().name();
		String nextState = wfw.getGameState().getNextStateNameWithColor();
		String nextStateAndCommand = "next: " + nextState + " (" + nextCommand + ")";
		String gameEvents = "Events: " + msg.getGameEvents().stream().map(IGameEvent::getType).map(Enum::name)
				.collect(Collectors.joining(","));
		String proposedGameEvents = "Proposed: " + proposedGameEventGroups(msg.getGameEventProposalGroups());


		txtShapes.add(new DrawableBorderText(getPosition(7, 0), nextStateAndCommand, Color.white));
		txtShapes.add(new DrawableBorderText(getPosition(7, 1), gameEvents, Color.white));
		txtShapes.add(new DrawableBorderText(getPosition(7, 2), proposedGameEvents, Color.white));
		txtShapes
				.add(new DrawableBorderText(getPosition(7, 3), getSubstitutionString(msg), Color.WHITE));

		for (DrawableBorderText txt : txtShapes)
		{
			txt.setFontSize(EFontSize.SMALL);
		}

		shapeMap.get(EWpShapesLayer.REFEREE).addAll(txtShapes);

		paintShapes(shapeMap.get(EWpShapesLayer.REFEREE), wfw);
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
		offsetsX[1] = offsetsX[0] + 15.0;
		offsetsX[2] = offsetsX[1] + 4.0;
		offsetsX[3] = offsetsX[2] + 1.3;
		offsetsX[4] = offsetsX[3] + 2.0;
		offsetsX[5] = offsetsX[4] + 10.0;
		offsetsX[6] = offsetsX[5] + 8.0;
		// Set the offset after the yellow cards string to grow with the number of timers to prevent overlaps
		offsetsX[7] = offsetsX[6] + 5.2 +
				3.8 * Math.max(msg.getTeamInfoYellow().getYellowCardsTimes().size(),
						msg.getTeamInfoBlue().getYellowCardsTimes().size());
	}


	private Vector2 getPosition(int column, int row)
	{
		return Vector2.fromXY(
				offsetsX[column],
				(row + 1.1));

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
			case BALL_PLACEMENT_BLUE:
				distToBallColor = Color.YELLOW;
				targetCircleColor = Color.BLUE;
				break;
			case BALL_PLACEMENT_YELLOW:
				distToBallColor = Color.BLUE;
				targetCircleColor = Color.YELLOW;
				break;
			default:
				break;
		}

		DrawableCircle distToBallCircle = new DrawableCircle(
				Circle.createCircle(wfw.getSimpleWorldFrame().getBall().getPos(), RuleConstraints.getStopRadius()),
				distToBallColor);
		shapes.add(distToBallCircle);

		DrawableCircle dtargetCircle = new DrawableCircle(
				Circle.createCircle(ballTargetPos, RuleConstraints.getBallPlacementTolerance()), targetCircleColor);
		DrawablePoint dtargetPoint = new DrawablePoint(ballTargetPos, Color.RED);
		shapes.add(dtargetCircle);
		shapes.add(dtargetPoint);
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


	private String getTimeoutString(final TeamInfo teamInfo)
	{
		long minTo = TimeUnit.MICROSECONDS.toMinutes(teamInfo.getTimeoutTime());
		long secTo = TimeUnit.MICROSECONDS.toSeconds(teamInfo.getTimeoutTime()) - (60 * minTo);
		return "TO: " + teamInfo.getTimeouts() + " (" + df2.format(minTo) + ":"
				+ df2.format(secTo)
				+ ")";
	}


	@SuppressWarnings("unused")
	private IVector2 getRequiredBallPos(final WorldFrameWrapper wfw)
	{
		IVector2 marker = null;
		switch (wfw.getRefereeMsg().getCommand())
		{
			case DIRECT_FREE_BLUE:
			case DIRECT_FREE_YELLOW:
			case INDIRECT_FREE_BLUE:
			case INDIRECT_FREE_YELLOW:
			case STOP:
				marker = wfw.getSimpleWorldFrame().getBall().getPos();
				break;
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
				marker = Geometry.getCenter();
				break;
			default:
				break;
		}
		return marker;
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
