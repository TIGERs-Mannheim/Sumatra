/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * Field Border Layer.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class BorderLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	FIELD_GOAL_HEIGHT_BASE		= 220;
	private static final int	FIELD_COLOR_RED				= 0;
	private static final int	FIELD_COLOR_GREEN				= 180;
	private static final int	FIELD_COLOR_GREEN_REFEREE	= 150;
	private static final int	FIELD_COLOR_BLUE				= 30;
	
	/** color of field background */
	public static final Color	FIELD_COLOR						= new Color(FIELD_COLOR_RED, FIELD_COLOR_GREEN,
																					FIELD_COLOR_BLUE);
	/** color of field background */
	public static final Color	FIELD_COLOR_REFEREE			= new Color(FIELD_COLOR_RED, FIELD_COLOR_GREEN_REFEREE,
																					FIELD_COLOR_BLUE);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public BorderLayer()
	{
		super(EFieldLayer.BORDER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayer(Graphics2D g)
	{
		final Graphics2D g2 = g;
		
		final int fieldCircleRadius = FieldPanel.scaleXLength(AIConfig.getGeometry().getCenterCircleRadius());
		final int fieldGoalWidth = FieldPanel.scaleXLength(AIConfig.getGeometry().getGoalSize());
		final int fieldGoalHeight = FieldPanel.scaleXLength(FIELD_GOAL_HEIGHT_BASE);
		final int fieldGoalMarginPenaltyarea = FieldPanel.scaleXLength(AIConfig.getGeometry().getDistanceToPenaltyArea());
		final int fieldGoalMarginPenlatyline = FieldPanel.scaleXLength(AIConfig.getGeometry()
				.getLengthOfPenaltyAreaFrontLine());
		final int fieldGoalPenaltyMark = FieldPanel.scaleXLength(AIConfig.getGeometry().getDistanceToPenaltyMark());
		
		final int fieldTotalWidth = FieldPanel.getFieldTotalWidth();
		final int fieldTotalHeight = FieldPanel.getFieldTotalHeight();
		final int fieldRefereeWidth = FieldPanel.scaleXLength(AIConfig.getGeometry().getJudgesBorderWidth());
		final int fieldRefereeLength = FieldPanel.scaleXLength(AIConfig.getGeometry().getJudgesBorderLength());
		
		// --- paint background ---
		g2.setColor(FIELD_COLOR_REFEREE);
		g2.fillRect(-fieldRefereeWidth, -fieldRefereeLength, fieldTotalWidth + (2 * fieldRefereeWidth), fieldTotalHeight
				+ (2 * fieldRefereeLength));
		g2.setColor(FIELD_COLOR);
		g2.fillRect(0, 0, fieldTotalWidth, fieldTotalHeight);
		
		// --- paint outline ---
		g2.setColor(Color.white);
		g2.drawRect(FieldPanel.FIELD_MARGIN, FieldPanel.FIELD_MARGIN, fieldTotalWidth - (2 * FieldPanel.FIELD_MARGIN),
				fieldTotalHeight - (2 * FieldPanel.FIELD_MARGIN));
		
		// --- paint halfway line ---
		g2.setColor(Color.white);
		g2.drawLine(FieldPanel.FIELD_MARGIN, (fieldTotalHeight / 2), (fieldTotalWidth - (2 * FieldPanel.FIELD_MARGIN))
				+ FieldPanel.FIELD_MARGIN, fieldTotalHeight / 2);
		
		// --- paint halfway circle ---
		g2.setColor(Color.white);
		g2.drawOval((fieldTotalWidth / 2) - fieldCircleRadius, ((fieldTotalHeight / 2) - fieldCircleRadius),
				fieldCircleRadius * 2, fieldCircleRadius * 2);
		
		// --- paint penalty area at top ---
		g2.setColor(Color.white);
		g2.drawArc((fieldTotalWidth / 2) - (fieldGoalMarginPenlatyline / 2) - fieldGoalMarginPenaltyarea,
				FieldPanel.FIELD_MARGIN - fieldGoalMarginPenaltyarea, fieldGoalMarginPenaltyarea * 2,
				fieldGoalMarginPenaltyarea * 2, 180, 90);
		g2.setColor(Color.white);
		g2.drawArc(((fieldTotalWidth / 2) + (fieldGoalMarginPenlatyline / 2)) - fieldGoalMarginPenaltyarea,
				FieldPanel.FIELD_MARGIN - fieldGoalMarginPenaltyarea, fieldGoalMarginPenaltyarea * 2,
				fieldGoalMarginPenaltyarea * 2, 0, -90);
		g2.setColor(Color.white);
		g2.drawLine((fieldTotalWidth / 2) - (fieldGoalMarginPenlatyline / 2), FieldPanel.FIELD_MARGIN
				+ fieldGoalMarginPenaltyarea, (+fieldTotalWidth / 2) + (fieldGoalMarginPenlatyline / 2),
				+FieldPanel.FIELD_MARGIN + fieldGoalMarginPenaltyarea);
		// -- penalty mark top
		g2.setColor(Color.white);
		g2.drawLine((fieldTotalWidth / 2) - (5 / 2), FieldPanel.FIELD_MARGIN + fieldGoalPenaltyMark,
				(+fieldTotalWidth / 2) + (5 / 2), +FieldPanel.FIELD_MARGIN + fieldGoalPenaltyMark);
		
		// --- paint penalty area at bottom ---
		g2.setColor(Color.white);
		g2.drawArc((fieldTotalWidth / 2) - (fieldGoalMarginPenlatyline / 2) - fieldGoalMarginPenaltyarea,
				fieldTotalHeight - FieldPanel.FIELD_MARGIN - fieldGoalMarginPenaltyarea, fieldGoalMarginPenaltyarea * 2,
				fieldGoalMarginPenaltyarea * 2, 180, -90);
		g2.setColor(Color.white);
		g2.drawArc(((fieldTotalWidth / 2) + (fieldGoalMarginPenlatyline / 2)) - fieldGoalMarginPenaltyarea,
				fieldTotalHeight - FieldPanel.FIELD_MARGIN - fieldGoalMarginPenaltyarea, fieldGoalMarginPenaltyarea * 2,
				fieldGoalMarginPenaltyarea * 2, 0, 90);
		g2.setColor(Color.white);
		g2.drawLine((fieldTotalWidth / 2) - (fieldGoalMarginPenlatyline / 2), fieldTotalHeight - FieldPanel.FIELD_MARGIN
				- fieldGoalMarginPenaltyarea, (fieldTotalWidth / 2) + (fieldGoalMarginPenlatyline / 2), fieldTotalHeight
				- FieldPanel.FIELD_MARGIN - fieldGoalMarginPenaltyarea);
		// -- penalty mark bottom
		g2.setColor(Color.white);
		g2.drawLine((fieldTotalWidth / 2) - (5 / 2), fieldTotalHeight - FieldPanel.FIELD_MARGIN - fieldGoalPenaltyMark,
				(fieldTotalWidth / 2) + (5 / 2), fieldTotalHeight - FieldPanel.FIELD_MARGIN - fieldGoalPenaltyMark);
		
		// if (TeamConfig.getInstance().getTeam().getPlayLeftToRight())
		// {
		if (TeamConfig.getInstance().getTeam().getTigersAreYellow())
		{
			g2.setColor(Color.yellow);
		} else
		{
			g2.setColor(Color.blue);
		}
		// } else
		// {
		// if (TeamConfig.getInstance().getTeam().getTigersAreYellow())
		// {
		// g2.setColor(Color.blue);
		// } else
		// {
		// g2.setColor(Color.yellow);
		// }
		// }
		
		// --- paint goal at top ---
		g2.setStroke(new BasicStroke(2));
		g2.drawLine((fieldTotalWidth - fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN,
				(fieldTotalWidth - fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN - fieldGoalHeight);
		g2.drawLine((fieldTotalWidth + fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN,
				(fieldTotalWidth + fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN - fieldGoalHeight);
		g2.drawLine((fieldTotalWidth - fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN - fieldGoalHeight,
				(fieldTotalWidth + fieldGoalWidth) / 2, FieldPanel.FIELD_MARGIN - fieldGoalHeight);
		
		if (g2.getColor().equals(Color.yellow))
		{
			g2.setColor(Color.blue);
		} else
		{
			g2.setColor(Color.yellow);
		}
		// --- paint goal at bottom ---
		g2.setStroke(new BasicStroke(2));
		g2.drawLine((fieldTotalWidth - fieldGoalWidth) / 2, fieldTotalHeight - FieldPanel.FIELD_MARGIN,
				(fieldTotalWidth - fieldGoalWidth) / 2, (fieldTotalHeight - FieldPanel.FIELD_MARGIN) + fieldGoalHeight);
		g2.drawLine((fieldTotalWidth + fieldGoalWidth) / 2, fieldTotalHeight - FieldPanel.FIELD_MARGIN,
				(fieldTotalWidth + fieldGoalWidth) / 2, (fieldTotalHeight - FieldPanel.FIELD_MARGIN) + fieldGoalHeight);
		g2.drawLine((fieldTotalWidth - fieldGoalWidth) / 2, (fieldTotalHeight - FieldPanel.FIELD_MARGIN)
				+ fieldGoalHeight, (fieldTotalWidth + fieldGoalWidth) / 2, (fieldTotalHeight - FieldPanel.FIELD_MARGIN)
				+ fieldGoalHeight);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
