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
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
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
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final DecimalFormat	df2	= new DecimalFormat("00");
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public RefereeLayer()
	{
		super(EFieldLayer.REFEREE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		g.setColor(Color.white);
		g.setFont(new Font("", Font.PLAIN, 8));
		
		final int x;
		int xoff1 = 130;
		if (frame.getTeamColor() == ETeamColor.YELLOW)
		{
			x = 10;
		} else
		{
			xoff1 = -70;
			switch (getFieldPanel().getFieldTurn())
			{
				case NORMAL:
				case T180:
					x = getFieldPanel().getFieldTotalWidth() - 110;
					break;
				case T270:
				case T90:
					x = getFieldPanel().getFieldTotalHeight() - 110;
					break;
				default:
					throw new IllegalStateException();
			}
		}
		
		RefereeMsg msg = frame.getLatestRefereeMsg();
		if (msg != null)
		{
			g.drawString(msg.getCommand().toString(), x, 11);
			g.drawString(msg.getStage().toString(), x, 31);
			
			int ourScore = msg.getTeamInfoTigers().getScore();
			int theirScore = msg.getTeamInfoThem().getScore();
			g.drawString(String.format("%d : %d", ourScore, theirScore), x + xoff1, 11);
			
			// Time
			final long min = TimeUnit.MICROSECONDS.toMinutes(msg.getStageTimeLeft());
			final long sec = TimeUnit.MICROSECONDS.toSeconds(msg.getStageTimeLeft()) - (60 * min);
			g.drawString("Time: " + df2.format(min) + ":" + df2.format(sec), x + xoff1, 21);
			
			// Timeouts yellow
			long minTo = TimeUnit.MICROSECONDS.toMinutes(msg.getTeamInfoTigers().getTimeoutTime());
			long secTo = TimeUnit.MICROSECONDS.toSeconds(msg.getTeamInfoTigers().getTimeoutTime()) - (60 * minTo);
			g.drawString("TO:" + msg.getTeamInfoTigers().getTimeouts() + " (" + df2.format(minTo) + ":"
					+ df2.format(secTo)
					+ ")", x + xoff1, 31);
		} else
		{
			g.drawString("NO_REFEREE_CMD", x, 11);
		}
		g.drawString(frame.getTacticalField().getGameState().name(), x, 21);
		
		paintShapes(g, frame);
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
