/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This Layer does the painting of the robots, their velocities and
 * accelerations and the ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class BallBotLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log					= Logger.getLogger(BallBotLayer.class.getName());
	private static final int		BALL_BUFFER_SIZE	= 100;
	private static final float		BALL_FLY_TOL		= 2f;
	
	private boolean					showVelocity		= false;
	private boolean					showAcceleration	= false;
	private boolean					showBallBuffer		= false;
	
	private List<IVector3>			ballBuffer			= new LinkedList<IVector3>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BallBotLayer()
	{
		super(EFieldLayer.BALL_BOTS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerSwf(Graphics2D g2, SimpleWorldFrame wFrame)
	{
		// --- draw tiger robots ---
		for (final TrackedTigerBot bot : wFrame.getBots().values())
		{
			drawRobot(g2, bot, bot.getTeamColor(), String.valueOf(bot.getId().getNumber()), bot.isManualControl());
		}
		
		// --- draw ballBuffer ---
		if (showBallBuffer)
		{
			for (IVector3 ballPos : ballBuffer)
			{
				drawBall(g2, ballPos, wFrame.getBall().getVel(), true);
			}
			ballBuffer.add(wFrame.getBall().getPos3());
			if (ballBuffer.size() > BALL_BUFFER_SIZE)
			{
				ballBuffer.remove(0);
			}
		} else
		{
			ballBuffer.clear();
		}
		
		// --- draw balls ---
		drawBall(g2, wFrame.getBall().getPos3(), wFrame.getBall().getVel(), false);
		
		drawFutureBallPos(g2);
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	@Override
	public void setInitialVisibility()
	{
		super.setInitialVisibility();
		showAcceleration = false;
		showVelocity = false;
	}
	
	
	/**
	 * Draws a ball on the field.
	 * 
	 * @param g graphics object
	 * @param ballPos
	 * @param vel
	 */
	private void drawBall(Graphics2D g, IVector3 ballPos, IVector2 vel, boolean ballBuffer)
	{
		final IVector2 drawPoint = getFieldPanel().transformToGuiCoordinates(ballPos.getXYVector());
		
		int ballRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBallRadius());
		
		if (ballBuffer)
		{
			ballRadius *= 0.9;
		}
		
		final int drawingX = (int) (drawPoint.x() - ballRadius);
		final int drawingY = (int) (drawPoint.y() - ballRadius);
		
		g.setColor(Color.orange);
		if (ballPos.z() > BALL_FLY_TOL)
		{
			g.setColor(Color.red);
		}
		
		if (ballBuffer)
		{
			g.setColor(g.getColor().brighter());
		}
		
		g.fillOval(drawingX, drawingY, ballRadius * 2, ballRadius * 2);
		
		if (showVelocity && !ballBuffer)
		{
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + ballRadius)), ((drawingY + ballRadius)),
					(int) ((drawingX + ballRadius) + (vel.y() * 100)), (int) ((drawingY + ballRadius) + (vel.x() * 100)));
		}
	}
	
	
	/**
	 * Draws a robot on the field.
	 * 
	 * @param g graphics object
	 * @param bot
	 * @param teamColor yellow or blue
	 * @param id 1-12
	 */
	private void drawRobot(Graphics2D g, TrackedBot bot, ETeamColor teamColor, String id, boolean alternateColor)
	{
		final Color botColor = teamColor == ETeamColor.YELLOW ? Color.yellow : Color.blue;
		final Color bgColor = teamColor == ETeamColor.YELLOW ? Color.black : Color.white;
		final Color dirColor = teamColor == ETeamColor.YELLOW ? Color.red : Color.yellow;
		
		final int robotRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBotRadius());
		
		// --- get needed data ---
		final Vector2 vel = new Vector2(bot.getVel());
		final Vector2 acc = new Vector2(bot.getAcc());
		
		// --- determinate drawing-position ---
		int drawingX = 0;
		int drawingY = 0;
		
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transBotPos = getFieldPanel().transformToGuiCoordinates(bot.getPos());
		drawingX = (int) transBotPos.x() - robotRadius;
		drawingY = (int) transBotPos.y() - robotRadius;
		
		// --- check and determinate id-length for margin ---
		int idX;
		int idY;
		
		if (id.length() == 1)
		{
			idX = drawingX + (int) (robotRadius * 0.5);
			idY = drawingY + (int) (robotRadius * 1.5);
		} else if (id.length() == 2)
		{
			idX = drawingX + (int) (robotRadius * 0.1);
			idY = drawingY + (int) (robotRadius * 1.5);
		} else
		{
			log.warn("Can't paint robot (id=" + id + ") . Wrong color (either blue or yellow)");
			return;
		}
		
		// --- draw bot-oval ---
		if (alternateColor)
		{
			g.setColor(botColor.darker());
		} else
		{
			g.setColor(botColor);
		}
		g.fillOval(drawingX, drawingY, robotRadius * 2, robotRadius * 2);
		
		// --- draw id and direction-sign ---
		g.setColor(bgColor);
		g.setFont(new Font("Courier", Font.BOLD, (int) (robotRadius * 1.5)));
		g.drawString(id, idX, idY);
		
		g.setColor(dirColor);
		g.setStroke(new BasicStroke(3));
		
		IVector2 p1 = bot.getPos().addNew(new Vector2(bot.getAngle()).scaleTo(AIConfig.getGeometry().getBotRadius() / 2));
		IVector2 p2 = bot.getPos().addNew(new Vector2(bot.getAngle()).scaleTo(AIConfig.getGeometry().getBotRadius()));
		IVector2 pt1 = getFieldPanel().transformToGuiCoordinates(p1);
		IVector2 pt2 = getFieldPanel().transformToGuiCoordinates(p2);
		g.drawLine((int) pt1.x(), (int) pt1.y(), (int) pt2.x(), (int) pt2.y());
		
		
		// --- velocity ---
		if (showVelocity)
		{
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + robotRadius)), ((drawingY + robotRadius)),
					(int) ((drawingX + robotRadius) + (vel.y * 100)), (int) ((drawingY + robotRadius) + (vel.x * 100)));
		}
		
		// --- acceleration ---
		if (showAcceleration)
		{
			g.setColor(Color.magenta);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + robotRadius)), ((drawingY + robotRadius)),
					(int) ((drawingX + robotRadius) + (acc.y * 100)), (int) ((drawingY + robotRadius) + (acc.x * 100)));
		}
		
	}
	
	
	private void drawFutureBallPos(Graphics2D g)
	{
		// TODO DirkK inspect NPE in Sumatrav3
		// if (showPPErrorTree)
		// {
		// IRecordWfFrame wFrame = getAiFrame().getRecordWfFrame();
		// FieldPredictionInformation ball = wFrame.getWorldFramePrediction().getBall();
		// for (float t = 0; t < 1; t += 0.020)
		// {
		// IVector2 pointToDraw = new Vector2(ball.getPosAt(t));
		//
		// g.setColor(new Color(255, 255, 255));
		//
		// // fip x and y since field is vertically drawn
		// final IVector2 pointToDrawGUI = getFieldPanel().transformToGuiCoordinates(pointToDraw);
		// g.fillOval((int) pointToDrawGUI.x() - 1, (int) pointToDrawGUI.y() - 1, 2, 2);
		// }
		// }
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param visible
	 */
	public void showVelocity(boolean visible)
	{
		showVelocity = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showAcceleration(boolean visible)
	{
		showAcceleration = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showBallBuffer(boolean visible)
	{
		showBallBuffer = visible;
	}
}
