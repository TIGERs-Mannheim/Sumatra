/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;


/**
 * Show vision detection before WP
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisionLayer extends AFieldLayer
{
	
	/**
	 */
	public VisionLayer()
	{
		super(EFieldLayer.VISION);
	}
	
	
	private void drawBot(final Graphics2D g, final IVector2 pos, final float orientation, final Color color)
	{
		g.setColor(color);
		float r = AIConfig.getGeometry().getBotRadius();
		float center2DribblerDist = Geometry.getCenter2DribblerDistDefault();
		float alpha = (float) Math.acos(center2DribblerDist / r);
		float startAngleRad = (orientation - AngleMath.PI_HALF) + getFieldTurn().getAngle() + alpha;
		float startAngle = AngleMath.rad2deg(startAngleRad);
		float endAngle = 360 - AngleMath.rad2deg(2 * alpha);
		
		final IVector2 transBotPos = getFieldPanel().transformToGuiCoordinates(pos);
		final int robotRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBotRadius());
		int drawingX = (int) transBotPos.x() - robotRadius;
		int drawingY = (int) transBotPos.y() - robotRadius;
		
		Shape botShape = new Arc2D.Double(drawingX, drawingY, robotRadius * 2, robotRadius * 2, startAngle,
				endAngle, Arc2D.CHORD);
		g.draw(botShape);
	}
	
	
	@Override
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
		
		for (MergedCamDetectionFrame mergedCamFrame : frame.getCamFrames())
		{
			for (CamRobot bot : mergedCamFrame.getRobotsBlue())
			{
				drawBot(g, bot.getPos(), bot.getOrientation(), Color.blue.brighter());
			}
			
			for (CamRobot bot : mergedCamFrame.getRobotsYellow())
			{
				drawBot(g, bot.getPos(), bot.getOrientation(), Color.yellow.brighter());
			}
			
			g.setColor(new Color(50, 100, 20));
			int ballRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBallRadius());
			for (CamBall ball : mergedCamFrame.getBalls())
			{
				final IVector2 drawBall = getFieldPanel().transformToGuiCoordinates(ball.getPos().getXYVector());
				
				final int drawX = (int) (drawBall.x() - ballRadius);
				final int drawY = (int) (drawBall.y() - ballRadius);
				g.fillOval(drawX, drawY, ballRadius * 2, ballRadius * 2);
			}
			g.setColor(new Color(180, 80, 0));
			{
				final IVector2 drawBall = getFieldPanel().transformToGuiCoordinates(
						mergedCamFrame.getBall().getPos().getXYVector());
				
				final int drawX = (int) (drawBall.x() - ballRadius);
				final int drawY = (int) (drawBall.y() - ballRadius);
				g.fillOval(drawX, drawY, ballRadius * 2, ballRadius * 2);
			}
		}
	}
	
}
