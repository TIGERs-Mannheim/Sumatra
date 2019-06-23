/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 4, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Debug layer for quick drawing stuff
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MiscLayer extends AFieldLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public MiscLayer()
	{
		super(EFieldLayer.MISC, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		IVector2 shootTarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		float shootSpeed = 4.0f;
		for (TrackedTigerBot bot : frame.getWorldFrame().getTigerBotsAvailable().values())
		{
			IVector2 kickerPos = AiMath.getBotKickerPos(bot);
			IVector3 poss = AiMath.calcRedirectPositions(bot, frame.getWorldFrame().getBall(), shootTarget, shootSpeed);
			
			DrawableLine dLineOrient = new DrawableLine(new Line(kickerPos, new Vector2(bot.getAngle()).scaleTo(5000)),
					Color.black);
			DrawableLine dLineBallKicker = new DrawableLine(Line.newLine(frame.getWorldFrame().getBall().getPos(),
					kickerPos),
					Color.red);
			DrawableCircle dCircleDest = new DrawableCircle(new Circle(poss.getXYVector(), AIConfig.getGeometry()
					.getBotRadius()), Color.blue);
			DrawableLine dLineRedirect = new DrawableLine(
					new Line(poss.getXYVector(), new Vector2(poss.z()).scaleTo(5000)), Color.blue);
			
			dLineBallKicker.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			dLineOrient.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			dLineRedirect.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
			dCircleDest.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
	}
	
	
	@Override
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
