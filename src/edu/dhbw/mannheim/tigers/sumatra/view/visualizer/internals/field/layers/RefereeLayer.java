/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Display referee msgs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RefereeLayer extends AFieldLayer
{
	private final DecimalFormat	df2			= new DecimalFormat("00");
	private final DecimalFormat	dfBallVel	= new DecimalFormat("0.00");
	private final Font				font			= new Font("", Font.PLAIN, 10);
	
	
	/**
	  * 
	  */
	public RefereeLayer()
	{
		super(EFieldLayer.REFEREE);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		if (frame.getLatestRefereeMsg() != null)
		{
			g.scale(1f / getFieldPanel().getScaleFactor(), 1f / getFieldPanel().getScaleFactor());
			g.translate(-getFieldPanel().getFieldOriginX(), -getFieldPanel().getFieldOriginY());
			g.setColor(frame.getTeamColor() == ETeamColor.YELLOW ? Color.YELLOW : Color.BLUE);
			g.setFont(font);
			
			int[] off = getOffsets(frame.getLatestRefereeMsg(), g);
			
			int y;
			if (frame.getTeamColor() == ETeamColor.YELLOW)
			{
				y = 11;
			} else
			{
				y = 23;
			}
			g.drawString(frame.getTacticalField().getGameState().name(), off[4], y);
			
			g.translate(getFieldPanel().getFieldOriginX(), getFieldPanel().getFieldOriginY());
			g.scale(getFieldPanel().getScaleFactor(), getFieldPanel().getScaleFactor());
		}
		
		paintShapes(g, frame);
	}
	
	
	@Override
	protected void paintLayerReferee(final Graphics2D g, final RefereeMsg msg)
	{
		g.scale(1f / getFieldPanel().getScaleFactor(), 1f / getFieldPanel().getScaleFactor());
		g.translate(-getFieldPanel().getFieldOriginX(), -getFieldPanel().getFieldOriginY());
		g.setFont(font);
		
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
		
		int[] off = getOffsets(msg, g);
		
		g.setColor(Color.white);
		g.drawString(msg.getStage().toString(), off[0], 11);
		g.drawString(msg.getCommand().toString(), off[0], 23);
		g.drawString(timeStr, off[1], 11);
		g.setColor(Color.yellow);
		g.drawString(msg.getTeamInfoYellow().getName(), off[2], 11);
		g.drawString(String.valueOf(msg.getTeamInfoYellow().getScore()), off[3], 11);
		g.drawString(timeoutYellowStr, off[5], 11);
		g.drawString(yellowCardYellowStr, off[6], 11);
		g.setColor(Color.blue);
		g.drawString(msg.getTeamInfoBlue().getName(), off[2], 23);
		g.drawString(String.valueOf(msg.getTeamInfoBlue().getScore()), off[3], 23);
		g.drawString(timeoutBlueStr, off[5], 23);
		g.drawString(yellowCardBlueStr, off[6], 23);
		
		g.translate(getFieldPanel().getFieldOriginX(), getFieldPanel().getFieldOriginY());
		g.scale(getFieldPanel().getScaleFactor(), getFieldPanel().getScaleFactor());
	}
	
	
	@Override
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
		g.scale(1f / getFieldPanel().getScaleFactor(), 1f / getFieldPanel().getScaleFactor());
		g.translate(-getFieldPanel().getFieldOriginX(), -getFieldPanel().getFieldOriginY());
		g.setFont(font);
		
		float ballSpeed = frame.getBall().getVel().getLength2();
		float ballHeight = frame.getBall().getPos3().z();
		String ballVelStr = "Ball vel: " + dfBallVel.format(ballSpeed) + " ball height: " + dfBallVel.format(ballHeight);
		int xCenter = getFieldPanel().getWidth() / 2;
		int x = xCenter - (getStringWidth(ballVelStr, g) / 2);
		int y = getFieldPanel().getHeight() - 5;
		
		g.setColor(ballSpeed <= 8 ? Color.white : Color.red);
		g.drawString(ballVelStr, x, y);
		
		g.translate(getFieldPanel().getFieldOriginX(), getFieldPanel().getFieldOriginY());
		g.scale(getFieldPanel().getScaleFactor(), getFieldPanel().getScaleFactor());
	}
	
	
	private int[] getOffsets(final RefereeMsg msg, final Graphics2D g)
	{
		int teamNameMaxWidth = Math.max(getStringWidth(msg.getTeamInfoBlue().getName(), g),
				getStringWidth(msg.getTeamInfoYellow().getName(), g));
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
	
	
	private void paintShapes(final Graphics2D g, final IRecordFrame frame)
	{
		IVector2 marker = null;
		float radius = 100;
		float fWidth = AIConfig.getGeometry().getFieldWidth();
		
		marker = frame.getTacticalField().getGameState().getRequiredBallPos(frame);
		
		switch (frame.getTacticalField().getGameState())
		{
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
				radius = AIConfig.getGeometry().getCenterCircleRadius();
				break;
			case STOPPED:
				radius = AIConfig.getGeometry().getBotToBallDistanceStop();
				DrawableCircle circle = new DrawableCircle(new Circle(marker, 700), Color.yellow);
				circle.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
				break;
			case PREPARE_PENALTY_THEY:
			{
				ILine line = Line.newLine(AIConfig.getGeometry().getPenaltyLineOur().addNew(new Vector2(0, fWidth / 2)),
						AIConfig.getGeometry().getPenaltyLineOur().addNew(new Vector2(0, -fWidth / 2)));
				DrawableLine dLine = new DrawableLine(line, Color.red, false);
				dLine.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			}
				break;
			case PREPARE_PENALTY_WE:
			{
				ILine line = Line.newLine(AIConfig.getGeometry().getPenaltyLineTheir().addNew(new Vector2(0, fWidth / 2)),
						AIConfig.getGeometry().getPenaltyLineTheir().addNew(new Vector2(0, -fWidth / 2)));
				DrawableLine dLine = new DrawableLine(line, Color.red, false);
				dLine.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			}
				break;
			default:
				break;
		}
		
		if (marker != null)
		{
			DrawableCircle circle = new DrawableCircle(new Circle(marker, radius), Color.red);
			circle.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			DrawablePoint point = new DrawablePoint(marker, Color.red);
			point.setSize(2);
			point.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
		
		if (frame.getTacticalField().getBallLeftFieldPos() != null)
		{
			DrawablePoint point = new DrawablePoint(frame.getTacticalField().getBallLeftFieldPos(), Color.red);
			point.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
	}
	
	
	private int getStringWidth(final String str, final Graphics2D g)
	{
		Rectangle2D teamStrRect = font.getStringBounds(str, g.getFontRenderContext());
		return (int) teamStrRect.getWidth();
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
}
