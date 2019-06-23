/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This Layer does the painting of the robots, their velocities and
 * accelerations and the ball.
 * 
 * @author Oliver Steinbrecher
 */
public class BallBotLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log					= Logger.getLogger(BallBotLayer.class.getName());
	private static final float		BALL_FLY_TOL		= 15f;
	
	private boolean					showVelocity		= false;
	private boolean					showAcceleration	= false;
	
	
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
	protected void paintLayerSwf(final Graphics2D g2, final SimpleWorldFrame wFrame)
	{
		// --- draw tiger robots ---
		for (final TrackedTigerBot bot : wFrame.getBots().values())
		{
			drawRobot(g2, bot, bot.getTeamColor(), String.valueOf(bot.getId().getNumber()), bot.isBlocked());
		}
		
		// --- draw balls ---
		drawBall(g2, wFrame);
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
	 */
	private void drawBall(final Graphics2D g, final SimpleWorldFrame wFrame)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		final IVector2 drawPoint = getFieldPanel().transformToGuiCoordinates(wFrame.getBall().getPos());
		
		int ballRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBallRadius());
		
		final int drawingX = (int) (drawPoint.x() - ballRadius);
		final int drawingY = (int) (drawPoint.y() - ballRadius);
		
		g.setColor(Color.orange);
		if (wFrame.getBall().getPos3().z() > BALL_FLY_TOL)
		{
			g.setColor(Color.red);
		}
		
		g.setColor(g.getColor().brighter());
		
		g.fillOval(drawingX, drawingY, ballRadius * 2, ballRadius * 2);
		g.setStroke(new BasicStroke(1));
		DrawableCircle circle1 = new DrawableCircle(new Circle(ballPos, 120), g.getColor());
		circle1.paintShape(g, getFieldPanel(), false);
		DrawableCircle circle2 = new DrawableCircle(new Circle(ballPos, 105), g.getColor());
		circle2.paintShape(g, getFieldPanel(), false);
		
		if (showVelocity)
		{
			IVector2 pVel = wFrame.getBall().getPos().addNew(wFrame.getBall().getVel().multiplyNew(200));
			IVector2 velGui = getFieldPanel().transformToGuiCoordinates(pVel);
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + ballRadius)), ((drawingY + ballRadius)),
					(int) velGui.x(), (int) velGui.y());
		}
		
		drawBallStandingStillPos(g, wFrame);
	}
	
	
	private void drawBallStandingStillPos(final Graphics2D g, final SimpleWorldFrame wFrame)
	{
		IVector2 ballPos = wFrame.getBall().getPosByVel(0);
		DrawableCircle dCircle = new DrawableCircle(ballPos, AIConfig.getGeometry().getBallRadius() + 5, Color.red);
		dCircle.paintShape(g, getFieldPanel(), false);
	}
	
	
	private Color getBotColor(final TrackedTigerBot bot)
	{
		final Color botColor;
		if (!bot.isVisible())
		{
			botColor = bot.getTeamColor() == ETeamColor.YELLOW ? Color.green : Color.magenta;
		} else
		{
			botColor = bot.getTeamColor() == ETeamColor.YELLOW ? Color.yellow : Color.blue;
		}
		return botColor;
	}
	
	
	/**
	 * Draws a robot on the field.
	 * 
	 * @param g graphics object
	 * @param bot
	 * @param teamColor yellow or blue
	 * @param id 1-12
	 */
	private void drawRobot(final Graphics2D g, final TrackedTigerBot bot, final ETeamColor teamColor, final String id,
			final boolean alternateColor)
	{
		final Color botColor = getBotColor(bot);
		final Color bgColor = teamColor == ETeamColor.YELLOW ? Color.black : Color.white;
		final Color dirColor = teamColor == ETeamColor.YELLOW ? Color.red : Color.yellow;
		
		
		final int robotRadius = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBotRadius());
		
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
		
		// --- draw bot ---
		g.setColor(botColor);
		float r = AIConfig.getGeometry().getBotRadius();
		float center2DribblerDist = bot.getBot().getCenter2DribblerDist();
		float alpha = (float) Math.acos(center2DribblerDist / r);
		float startAngleRad = (bot.getAngle() - AngleMath.PI_HALF) + getFieldTurn().getAngle() + alpha;
		float startAngle = AngleMath.rad2deg(startAngleRad);
		float endAngle = 360 - AngleMath.rad2deg(2 * alpha);
		
		Shape botShape = new Arc2D.Double(drawingX, drawingY, robotRadius * 2, robotRadius * 2, startAngle,
				endAngle, Arc2D.CHORD);
		
		g.fill(botShape);
		
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));
		final IVector2 kickerPos = getFieldPanel().transformToGuiCoordinates(
				AiMath.getBotKickerPos(bot.getPos(), bot.getAngle(), bot.getBot().getCenter2DribblerDist() - 20));
		g.drawLine(drawingX + robotRadius, drawingY + robotRadius, (int) kickerPos.x(), (int) kickerPos.y());
		
		// --- draw id and direction-sign ---
		g.setColor(bgColor);
		g.setFont(new Font("Courier", Font.BOLD, (int) (robotRadius * 1.5)));
		g.drawString(id, idX, idY);
		
		g.setColor(dirColor);
		g.setStroke(new BasicStroke(3));
		
		if (bot.isBlocked())
		{
			g.setColor(Color.red);
			g.drawArc(drawingX, drawingY, robotRadius * 2, robotRadius * 2, 0, 360);
		}
		
		// IVector2 p1 = bot.getPos().addNew(new Vector2(bot.getAngle()).scaleTo(AIConfig.getGeometry().getBotRadius() /
		// 2));
		// IVector2 p2 = bot.getPos().addNew(new Vector2(bot.getAngle()).scaleTo(AIConfig.getGeometry().getBotRadius()));
		// IVector2 pt1 = getFieldPanel().transformToGuiCoordinates(p1);
		// IVector2 pt2 = getFieldPanel().transformToGuiCoordinates(p2);
		// g.drawLine((int) pt1.x(), (int) pt1.y(), (int) pt2.x(), (int) pt2.y());
		
		
		// --- velocity ---
		if (showVelocity)
		{
			IVector2 pVel = bot.getPos().addNew(bot.getVel().multiplyNew(1000));
			IVector2 velGui = getFieldPanel().transformToGuiCoordinates(pVel);
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + robotRadius)), ((drawingY + robotRadius)),
					(int) velGui.x(), (int) velGui.y());
		}
		
		// --- acceleration ---
		if (showAcceleration)
		{
			IVector2 pAcc = bot.getPos().addNew(bot.getAcc().multiplyNew(1000));
			IVector2 accGui = getFieldPanel().transformToGuiCoordinates(pAcc);
			g.setColor(Color.magenta);
			g.setStroke(new BasicStroke(3));
			g.drawLine(((drawingX + robotRadius)), ((drawingY + robotRadius)),
					(int) accGui.x(), (int) accGui.y());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param visible
	 */
	public void showVelocity(final boolean visible)
	{
		showVelocity = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showAcceleration(final boolean visible)
	{
		showAcceleration = visible;
	}
	
}
