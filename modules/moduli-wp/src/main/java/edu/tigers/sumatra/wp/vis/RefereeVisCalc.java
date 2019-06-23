/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeVisCalc implements IWpCalc
{
	private final DecimalFormat df2 = new DecimalFormat("00");
	private final DecimalFormat dfBallVel = new DecimalFormat("0.00");
	
	
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
		String timeStr = df2.format(min) + ":" + df2.format(sec);
		
		// Timeouts
		String timeoutYellowStr = getTimeoutString(msg.getTeamInfoYellow());
		String timeoutBlueStr = getTimeoutString(msg.getTeamInfoBlue());
		
		// Yellow cards
		String yellowCardYellowStr = getYellowCardString(msg.getTeamInfoYellow().getYellowCards(), msg
				.getTeamInfoYellow().getYellowCardsTimes());
		String yellowCardBlueStr = getYellowCardString(msg.getTeamInfoBlue().getYellowCards(), msg.getTeamInfoBlue()
				.getYellowCardsTimes());
		
		int[] off = getOffsets();
		
		double ballSpeed = wfw.getSimpleWorldFrame().getBall().getVel().getLength2();
		double ballHeight = wfw.getSimpleWorldFrame().getBall().getPos3().z();
		String ballVelStr = "Ball vel: " + dfBallVel.format(ballSpeed) + "; height: " + dfBallVel.format(ballHeight);
		
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[1], 35), ballVelStr,
				ballSpeed <= RuleConstraints.getMaxBallSpeed() ? Color.white
						: Color.red));
		
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[0], 11), msg.getStage().toString(), Color.white));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[0], 23), msg.getCommand().toString(), Color.white));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[0], 35), wfw.getGameState().getStateNameWithColor(),
				Color.white));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[0], 47), wfw.getRefereeMsg().getGameEvent().toString(),
				Color.white));
		
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[1], 11), timeStr, Color.white));
		
		txtShapes
				.add(new DrawableBorderText(Vector2.fromXY(off[4], 11), msg.getTeamInfoYellow().getName(), Color.yellow));
		txtShapes
				.add(new DrawableBorderText(Vector2.fromXY(off[2], 11), String.valueOf(msg.getTeamInfoYellow().getScore()),
						Color.yellow));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[5], 11), timeoutYellowStr, Color.yellow));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[6], 11), yellowCardYellowStr, Color.yellow));
		
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[4], 23), msg.getTeamInfoBlue().getName(), Color.blue));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[2], 23), String.valueOf(msg.getTeamInfoBlue().getScore()),
				Color.blue));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[5], 23), timeoutBlueStr, Color.blue));
		txtShapes.add(new DrawableBorderText(Vector2.fromXY(off[6], 23), yellowCardBlueStr, Color.blue));
		
		for (DrawableBorderText txt : txtShapes)
		{
			txt.setFontSize(10);
		}
		
		shapeMap.get(EWpShapesLayer.REFEREE).addAll(txtShapes);
		
		paintShapes(shapeMap.get(EWpShapesLayer.REFEREE), wfw);
	}
	
	
	private int[] getOffsets()
	{
		int[] offsets = new int[7];
		offsets[0] = 10;
		offsets[1] = offsets[0] + 135;
		offsets[2] = offsets[1] + 40;
		offsets[3] = offsets[2] + 13;
		offsets[4] = offsets[3] + 20;
		offsets[5] = offsets[4] + 100;
		offsets[6] = offsets[5] + 80;
		return offsets;
	}
	
	
	private void paintShapes(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		if (wfw.getGameState().isBallPlacement())
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
				Circle.createCircle(wfw.getSimpleWorldFrame().getBall().getPos(), 500), distToBallColor);
		shapes.add(distToBallCircle);
		
		DrawableCircle dtargetCircle = new DrawableCircle(
				Circle.createCircle(ballTargetPos, 100), targetCircleColor);
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
}
