/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.referee.TeamInfo;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeVisCalc implements IWpCalc
{
	private final DecimalFormat	df2			= new DecimalFormat("00");
	private final DecimalFormat	dfBallVel	= new DecimalFormat("0.00");
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
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
		
		int[] off = getOffsets(msg);
		
		double ballSpeed = wfw.getSimpleWorldFrame().getBall().getVel().getLength2();
		double ballHeight = wfw.getSimpleWorldFrame().getBall().getPos3().z();
		String ballVelStr = "Ball vel: " + dfBallVel.format(ballSpeed) + "; height: " + dfBallVel.format(ballHeight);
		
		txtShapes.add(new DrawableBorderText(new Vector2(off[1], 35), ballVelStr, ballSpeed <= 8 ? Color.white
				: Color.red));
		
		txtShapes.add(new DrawableBorderText(new Vector2(off[0], 11), msg.getStage().toString(), Color.white));
		txtShapes.add(new DrawableBorderText(new Vector2(off[0], 23), msg.getCommand().toString(), Color.white));
		txtShapes.add(new DrawableBorderText(new Vector2(off[0], 35), wfw.getGameState().name(), Color.white));
		txtShapes.add(new DrawableBorderText(new Vector2(off[1], 11), timeStr, Color.white));
		
		txtShapes.add(new DrawableBorderText(new Vector2(off[4], 11), msg.getTeamInfoYellow().getName(), Color.yellow));
		txtShapes.add(new DrawableBorderText(new Vector2(off[2], 11), String.valueOf(msg.getTeamInfoYellow().getScore()),
				Color.yellow));
		txtShapes.add(new DrawableBorderText(new Vector2(off[5], 11), timeoutYellowStr, Color.yellow));
		txtShapes.add(new DrawableBorderText(new Vector2(off[6], 11), yellowCardYellowStr, Color.yellow));
		
		txtShapes.add(new DrawableBorderText(new Vector2(off[4], 23), msg.getTeamInfoBlue().getName(), Color.blue));
		txtShapes.add(new DrawableBorderText(new Vector2(off[2], 23), String.valueOf(msg.getTeamInfoBlue().getScore()),
				Color.blue));
		txtShapes.add(new DrawableBorderText(new Vector2(off[5], 23), timeoutBlueStr, Color.blue));
		txtShapes.add(new DrawableBorderText(new Vector2(off[6], 23), yellowCardBlueStr, Color.blue));
		
		for (DrawableBorderText txt : txtShapes)
		{
			txt.setFontSize(10);
		}
		
		wfw.getShapeMap().get(EWpShapesLayer.REFEREE).addAll(txtShapes);
		
		paintShapes(wfw.getShapeMap().get(EWpShapesLayer.REFEREE), wfw);
	}
	
	
	private int[] getOffsets(final RefereeMsg msg)
	{
		int teamNameMaxWidth = Math.max(getStringWidth(msg.getTeamInfoBlue().getName()),
				getStringWidth(msg.getTeamInfoYellow().getName()));
		int[] offsets = new int[7];
		offsets[0] = 10;
		offsets[1] = offsets[0] + 135;
		offsets[2] = offsets[1] + 40;
		offsets[3] = offsets[2] + 10 + teamNameMaxWidth;
		offsets[4] = offsets[3] + 20;
		offsets[5] = offsets[4] + 100;
		offsets[6] = offsets[5] + 80;
		return offsets;
	}
	
	
	private void paintShapes(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		switch (wfw.getRefereeMsg().getCommand())
		{
			case BALL_PLACEMENT_BLUE:
			case BALL_PLACEMENT_YELLOW:
				paintAutomatedPlacementShapes(shapes, wfw);
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
			case STOP:
				paintBallDistanceCircle(shapes, wfw);
				break;
			default:
				break;
		}
	}
	
	
	private void paintBallDistanceCircle(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		double radius = 100;
		Color circleColor = Color.red;
		IVector2 ballPos = wfw.getSimpleWorldFrame().getBall().getPos();
		
		Command refCmd = wfw.getRefereeMsg().getCommand();
		switch (refCmd)
		{
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
				radius = Geometry.getCenterCircleRadius();
				break;
			case STOP:
				radius = Geometry.getBotToBallDistanceStop();
				break;
			case BALL_PLACEMENT_BLUE:
			case BALL_PLACEMENT_YELLOW:
				radius = Geometry.getBotToBallDistanceStop();
				circleColor = refCmd == Command.BALL_PLACEMENT_BLUE ? Color.YELLOW : Color.BLUE;
				break;
			default:
				break;
		}
		
		DrawableCircle circle = new DrawableCircle(new Circle(ballPos, radius), circleColor);
		shapes.add(circle);
		DrawablePoint point = new DrawablePoint(ballPos, Color.red);
		point.setSize(2);
		shapes.add(point);
	}
	
	
	/**
	 * @param shapes
	 * @param wfw
	 */
	private void paintAutomatedPlacementShapes(final List<IDrawableShape> shapes, final WorldFrameWrapper wfw)
	{
		RefereeMsg refMsg = wfw.getRefereeMsg();
		IVector2 ballTargetPos = refMsg.getBallPlacementPos();
		
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
				new Circle(wfw.getSimpleWorldFrame().getBall().getPos(), 500), distToBallColor);
		shapes.add(distToBallCircle);
		
		DrawableCircle dtargetCircle = new DrawableCircle(
				new Circle(ballTargetPos, 100), targetCircleColor);
		DrawablePoint dtargetPoint = new DrawablePoint(ballTargetPos, Color.RED);
		shapes.add(dtargetCircle);
		shapes.add(dtargetPoint);
	}
	
	
	private int getStringWidth(final String str)
	{
		return 3;
		// Rectangle2D teamStrRect = font.getStringBounds(str, g.getFontRenderContext());
		// return (int) teamStrRect.getWidth();
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
		String timeoutStr = "TO: " + teamInfo.getTimeouts() + " (" + df2.format(minTo) + ":"
				+ df2.format(secTo)
				+ ")";
		return timeoutStr;
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
