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
import java.awt.geom.GeneralPath;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.ETeamColors;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.NoObjectWithThisIDException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


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
	
	private List<Path>				paths					= null;
	
	private boolean					showVelocity		= false;
	private boolean					showAcceleration	= false;
	private boolean					showPaths			= false;
	private boolean					showSplines			= false;
	private boolean					showPPErrorTree	= false;
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
	protected void paintLayer(Graphics2D g)
	{
		if (getAiFrame() != null)
		{
			final IRecordWfFrame wFrame = getAiFrame().getRecordWfFrame();
			
			final Graphics2D g2 = g;
			
			
			final ETeamColors tigerColor = wFrame.getTeamProps().getTigersColor();
			final ETeamColors foeColor = ETeamColors.opposite(tigerColor);
			
			
			// --- draw tiger robots ---
			for (final TrackedTigerBot bot : wFrame.getTigerBotsVisible().values())
			{
				drawRobot(g2, bot, tigerColor, String.valueOf(bot.getId().getNumber()), bot.isManualControl());
			}
			
			// --- draw foe robots ---
			for (final TrackedBot bot : wFrame.getFoeBots().values())
			{
				drawRobot(g2, bot, foeColor, String.valueOf(bot.getId().getNumber()), false);
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
			
			drawPaths(g2, Color.CYAN);
		}
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	@Override
	public void setInitialVisibility()
	{
		super.setInitialVisibility();
		showAcceleration = false;
		showPaths = false;
		showSplines = false;
		showPPErrorTree = false;
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
		final IVector2 drawPoint = FieldPanel.transformToGuiCoordinates(ballPos.getXYVector());
		
		int ballRadius = FieldPanel.scaleXLength(AIConfig.getGeometry().getBallRadius());
		
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
	private void drawRobot(Graphics2D g, TrackedBot bot, ETeamColors teamColor, String id, boolean alternateColor)
	{
		final Color botColor = teamColor == ETeamColors.YELLOW ? Color.yellow : Color.blue;
		final Color bgColor = teamColor == ETeamColors.YELLOW ? Color.black : Color.white;
		final Color dirColor = teamColor == ETeamColors.YELLOW ? Color.red : Color.yellow;
		final Color ramboColor = Color.red;
		
		final int robotRadius = FieldPanel.scaleXLength(AIConfig.getGeometry().getBotRadius());
		
		// --- get needed data ---
		final float angle = bot.getAngle();
		final Vector2 vel = new Vector2(bot.getVel());
		final Vector2 acc = new Vector2(bot.getAcc());
		
		// --- determinate drawing-position ---
		int drawingX = 0;
		int drawingY = 0;
		
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transBotPos = FieldPanel.transformToGuiCoordinates(bot.getPos());
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
		if (bot instanceof TrackedTigerBot)
		{
			Path path = ((TrackedTigerBot) bot).getPath();
			if ((path != null) && path.isRambo())
			{
				// --- mark rambo ---
				g.setColor(ramboColor);
			}
		}
		g.fillOval(drawingX, drawingY, robotRadius * 2, robotRadius * 2);
		
		// --- draw id and direction-sign ---
		g.setColor(bgColor);
		g.setFont(new Font("Courier", Font.BOLD, (int) (robotRadius * 1.5)));
		g.drawString(id, idX, idY);
		
		g.setColor(dirColor);
		g.setStroke(new BasicStroke(3));
		
		g.drawLine((int) ((drawingX + robotRadius) + (5 * Math.sin(angle))),
				(int) ((drawingY + robotRadius) + (5 * Math.cos(angle))),
				(int) ((drawingX + robotRadius) + (9 * Math.sin(angle))),
				(int) ((drawingY + robotRadius) + (9 * Math.cos(angle))));
		
		
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
		// final IVector2 pointToDrawGUI = FieldPanel.transformToGuiCoordinates(pointToDraw);
		// g.fillOval((int) pointToDrawGUI.x() - 1, (int) pointToDrawGUI.y() - 1, 2, 2);
		// }
		// }
	}
	
	
	private void drawPaths(Graphics2D g, Color color)
	{
		drawFutureBallPos(g);
		// --- draw paths ---
		if ((showPaths || showSplines || showPPErrorTree) && (paths != null) && !paths.isEmpty())
		{
			IVector2 botPos = null;
			
			for (final Path p : paths)
			{
				// --- if path==null ---
				if (p == null)
				{
					continue;
				}
				
				// --- get robot position ---
				ATrackedObject bot;
				try
				{
					bot = getAiFrame().getRecordWfFrame().getTigerBotsVisible().get(p.getBotID());
				} catch (final NoObjectWithThisIDException e)
				{
					// robot disappeared, so do not draw path
					continue;
				}
				botPos = bot.getPos();
				
				// --- draw path if botPos!=null ---
				if (botPos != null)
				{
					drawPath(g, bot, p);
				}
				
			}
		}
	}
	
	
	/**
	 * Draws a path.
	 * 
	 * @param g
	 * @param bot
	 * @param path
	 */
	private void drawPath(Graphics2D g, ATrackedObject bot, Path path)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transBotPos = FieldPanel.transformToGuiCoordinates(bot.getPos());
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		
		// --- draw waypoints ---
		if (showPaths)
		{
			g.setColor(Color.red);
			g.setStroke(new BasicStroke(1));
			final GeneralPath drawPath = new GeneralPath();
			drawPath.moveTo(robotX, robotY);
			for (final IVector2 point : path.getPath())
			{
				final IVector2 transPathPoint = FieldPanel.transformToGuiCoordinates(point);
				g.drawOval((int) transPathPoint.x(), (int) transPathPoint.y(), 2, 2);
				drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
			}
			g.draw(drawPath);
		}
		
		// draw splines
		if (showSplines)
		{
			final ISpline spline = path.getSpline();
			if (spline != null)
			{
				for (float t = 0; t < spline.getTotalTime(); t += 0.020)
				{
					if (t > 10)
					{
						// stop drawing here, because this would slow down Sumatra extensivly
						break;
					}
					IVector2 pointToDraw = new Vector2(spline.getValueByTime(t));
					pointToDraw = DistanceUnit.METERS.toMillimeters(pointToDraw);
					float curvature = Math.abs(spline.getAccelerationByTime(t).getLength2());
					curvature = 1 - (curvature / AIConfig.getBotConfig(((TrackedTigerBot) bot).getBotType()).getSkills()
							.getMaxLinearVelocity());
					if (curvature > 1)
					{
						curvature = 1;
					}
					
					float colorRed = curvature * 2;
					if (colorRed > 1)
					{
						colorRed = 1;
					}
					if (colorRed < 0)
					{
						colorRed = 0;
					}
					float colorGreen = 1 - curvature;
					colorGreen *= 2;
					if (colorGreen > 1)
					{
						colorGreen = 1;
					}
					if (colorGreen < 0)
					{
						colorGreen = 0;
					}
					
					g.setColor(new Color(colorRed, colorGreen, 0));
					if (path.getFirstCollisionAt() != null)
					{
						float diff = Math.abs(path.getFirstCollisionAt().getTime() - t);
						if (diff < 0.2)
						{
							g.setColor(new Color(0, 0, 255));
						}
					}
					// fip x and y since field is vertically drawn
					final IVector2 pointToDrawGUI = FieldPanel.transformToGuiCoordinates(pointToDraw);
					g.fillOval((int) pointToDrawGUI.x() - 1, (int) pointToDrawGUI.y() - 1, 2, 2);
				}
				if (path.getPathGuiFeatures().getVirtualVehicle() != null)
				{
					final Vector2f pointToDraw = new Vector2f(spline.getValueByTime(path.getPathGuiFeatures()
							.getVirtualVehicle()));
					g.setColor(new Color(0, 100, 255));
					// fip x and y since field is vertically drawn
					g.fillOval(((int) pointToDraw.y() / 10) + (FieldPanel.getFieldTotalWidth() / 2),
							((int) pointToDraw.x() / 10) + (FieldPanel.getFieldTotalHeight() / 2), 4, 4);
				}
				
				final IVector2 moveVec = path.getPathGuiFeatures().getCurrentMove();
				if (moveVec != null)
				{
					final Vector2 line = moveVec.multiplyNew(100).addNew(bot.getPos());
					g.setColor(new Color(50, 50, 50));
					g.drawLine(robotX, robotY, ((int) line.y() / 10) + (FieldPanel.getFieldTotalWidth() / 2),
							((int) line.x() / 10) + (FieldPanel.getFieldTotalHeight() / 2));
				}
				
			}
		}
		if (showPPErrorTree && (path.getTree() != null))
		{
			Node root = path.getTree().getRoot();
			for (Node children : root.getChildrenRecursive())
			{
				IVector2 childrenGUI = FieldPanel.transformToGuiCoordinates(children);
				IVector2 parentGUI = FieldPanel.transformToGuiCoordinates(children.getParent());
				g.setColor(new Color(0, 255, 0));
				g.drawLine((int) childrenGUI.x(), (int) childrenGUI.y(), (int) parentGUI.x(), (int) parentGUI.y());
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param paths
	 */
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}
	
	
	/**
	 * @param visible
	 */
	public void showPaths(boolean visible)
	{
		showPaths = visible;
	}
	
	
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
	public void showSplines(boolean visible)
	{
		showSplines = visible;
	}
	
	
	/**
	 * @param showPPErrorTree the showPPErrorTree to set
	 */
	public void showPPErrorTree(boolean showPPErrorTree)
	{
		this.showPPErrorTree = showPPErrorTree;
	}
	
	
	/**
	 * @param visible
	 */
	public void showBallBuffer(boolean visible)
	{
		showBallBuffer = visible;
	}
}
