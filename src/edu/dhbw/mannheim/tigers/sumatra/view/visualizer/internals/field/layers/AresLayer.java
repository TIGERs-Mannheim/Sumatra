/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Layer for data from ares (paths)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AresLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private boolean	showPaths				= false;
	private boolean	showSplines				= false;
	private boolean	showPPErrorTree		= false;
	private boolean	showPotentialPaths	= false;
	private boolean	showPotentialSplines	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public AresLayer()
	{
		super(EFieldLayer.ARES);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerAif(final Graphics2D g2, final IRecordFrame frame)
	{
		drawPaths(g2, frame);
		drawRambo(g2, frame);
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	@Override
	public void setInitialVisibility()
	{
		super.setInitialVisibility();
		showPaths = false;
		showSplines = false;
		showPPErrorTree = false;
	}
	
	
	private void drawRambo(final Graphics2D g, final IRecordFrame frame)
	{
		Map<BotID, Path> paths = frame.getAresData().getPaths();
		if (paths == null)
		{
			return;
		}
		for (TrackedTigerBot bot : frame.getWorldFrame().getTigerBotsAvailable().values())
		{
			Path path = paths.get(bot.getId());
			if ((path != null) && path.isRambo())
			{
				// --- mark rambo ---
				g.setColor(Color.red);
				IVector2 pos = bot.getPos();
				drawBotCircle(g, pos, frame.getWorldFrame().isInverted(), -2);
			}
		}
	}
	
	
	private void drawPaths(final Graphics2D g, final IRecordFrame frame)
	{
		Map<BotID, Path> paths = frame.getAresData().getPaths();
		Map<BotID, Path> latestPaths = frame.getAresData().getLatestPaths();
		// --- draw paths ---
		if (showPotentialPaths || showPotentialSplines)
		{
			for (final Path p : latestPaths.values())
			{
				// --- get robot position ---
				TrackedTigerBot bot = frame.getWorldFrame().getBot(p.getBotID());
				if (bot != null)
				{
					drawPath(g, bot, p, true, frame.getWorldFrame().isInverted());
				}
			}
		}
		if ((showPaths || showSplines || showPPErrorTree))
		{
			for (final Path p : paths.values())
			{
				// --- get robot position ---
				TrackedTigerBot bot = frame.getWorldFrame().getBot(p.getBotID());
				if (bot != null)
				{
					drawPath(g, bot, p, false, frame.getWorldFrame().isInverted());
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
	private void drawPath(final Graphics2D g, final TrackedTigerBot bot, final Path path, final boolean latestPath,
			final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transBotPos = getFieldPanel().transformToGuiCoordinates(bot.getPos(), invert);
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		
		// --- draw waypoints ---
		if ((!latestPath && showPaths) || (latestPath && showPotentialPaths))
		{
			if (latestPath)
			{
				g.setColor(Color.black);
			} else
			{
				g.setColor(Color.red);
			}
			g.setStroke(new BasicStroke(1));
			final GeneralPath drawPath = new GeneralPath();
			drawPath.moveTo(robotX, robotY);
			for (final IVector2 point : path.getPath())
			{
				final IVector2 transPathPoint = getFieldPanel().transformToGuiCoordinates(point, invert);
				g.drawOval((int) transPathPoint.x(), (int) transPathPoint.y(), 2, 2);
				drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
			}
			g.draw(drawPath);
		}
		
		// draw splines
		if ((!latestPath && showSplines) || (latestPath && showPotentialSplines))
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
					curvature = 1 - (curvature / Sisyphus.maxLinearVelocity);
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
					float colorBlue = 0;
					if (latestPath)
					{
						colorBlue = 1;
					}
					
					g.setColor(new Color(colorRed, colorGreen, colorBlue));
					if (path.getFirstCollisionAt() != null)
					{
						float diff = Math.abs(path.getFirstCollisionAt().getTime() - t);
						if (diff < 0.2)
						{
							g.setColor(new Color(0, 0, 255));
						}
					}
					// flip x and y since field is vertically drawn
					final IVector2 pointToDrawGUI = getFieldPanel().transformToGuiCoordinates(pointToDraw,
							invert);
					g.fillOval((int) pointToDrawGUI.x() - 1, (int) pointToDrawGUI.y() - 1, 2, 2);
				}
				
				IVector2 curPoint = spline.getValueByTime(path.getHermiteSpline().getTrajectoryTime()).multiplyNew(1000);
				IVector2 curPointGui = getFieldPanel().transformToGuiCoordinates(curPoint, invert);
				g.setColor(Color.magenta);
				g.fillOval((int) curPointGui.x() - 1, (int) curPointGui.y() - 1, 2, 2);
				
			}
		}
		if (showPPErrorTree && (path.getTree() != null))
		{
			Node root = path.getTree().getRoot();
			for (Node children : root.getChildrenRecursive())
			{
				IVector2 childrenGUI = getFieldPanel().transformToGuiCoordinates(children, invert);
				IVector2 parentGUI = getFieldPanel().transformToGuiCoordinates(children.getParent(), invert);
				g.setColor(new Color(0, 255, 0));
				g.drawLine((int) childrenGUI.x(), (int) childrenGUI.y(), (int) parentGUI.x(), (int) parentGUI.y());
			}
		}
	}
	
	
	/**
	 * @param visible
	 */
	public void showPaths(final boolean visible)
	{
		showPaths = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showSplines(final boolean visible)
	{
		showSplines = visible;
	}
	
	
	/**
	 * @param showPPErrorTree the showPPErrorTree to set
	 */
	public void showPPErrorTree(final boolean showPPErrorTree)
	{
		this.showPPErrorTree = showPPErrorTree;
	}
	
	
	/**
	 * @param visible
	 */
	public void showPotentialPaths(final boolean visible)
	{
		showPotentialPaths = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showPotentialSplines(final boolean visible)
	{
		showPotentialSplines = visible;
	}
}
