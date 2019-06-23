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
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.DrawableTree;


/**
 * Layer for data from ares (paths)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AresLayer extends AFieldLayer
{
	private boolean	showPaths					= false;
	private boolean	showDecoration				= false;
	private boolean	showDebug					= false;
	private boolean	showPotentialPaths		= false;
	private boolean	showPotentialDecoration	= false;
	private boolean	showPotentialDebug		= false;
	private boolean	showUnsmoothedPath		= false;
	private boolean	showRamboTree				= false;
	
	
	/**
	 */
	public AresLayer()
	{
		super(EFieldLayer.ARES);
	}
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g2, final IRecordFrame frame)
	{
		boolean invert = frame.getWorldFrame().isInverted();
		for (Map.Entry<BotID, DrawablePath> entry : frame.getAresData().getPaths().entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getBot(entry.getKey());
			if (showPaths)
			{
				drawPathPoints(g2, bot, entry.getValue().getPath(), Color.red, invert);
			}
			if (showDecoration)
			{
				drawDrawableShapes(g2, entry.getValue().getPathShapes(), invert);
			}
			if (showDebug)
			{
				drawDrawableShapes(g2, entry.getValue().getPathDebugShapes(), invert);
			}
			if (showUnsmoothedPath)
			{
				drawPathPointsUnsmoothed(g2, bot, entry.getValue().getPath(), Color.magenta, invert);
			}
			if (showRamboTree && (entry.getValue().getPath() != null) && (entry.getValue().getPath().getTree() != null))
			{
				IDrawableShape treeShape = new DrawableTree(entry.getValue().getPath().getTree().getRoot(), Color.red);
				treeShape.paintShape(g2, getFieldPanel(), invert);
			}
		}
		for (Map.Entry<BotID, DrawablePath> entry : frame.getAresData().getLatestPaths().entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getBot(entry.getKey());
			if (showPotentialPaths)
			{
				drawPathPoints(g2, bot, entry.getValue().getPath(), Color.magenta, invert);
			}
			if (showPotentialDecoration)
			{
				drawDrawableShapes(g2, entry.getValue().getPathShapes(), invert);
			}
			if (showPotentialDebug)
			{
				drawDrawableShapes(g2, entry.getValue().getPathDebugShapes(), invert);
			}
		}
		
		markRambo(g2, frame);
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	@Override
	public void setInitialVisibility()
	{
		super.setInitialVisibility();
		showPaths = false;
		showDecoration = false;
		showDebug = false;
		showPotentialPaths = false;
		showPotentialDecoration = false;
		showPotentialDebug = false;
	}
	
	
	private void markRambo(final Graphics2D g, final IRecordFrame frame)
	{
		Map<BotID, DrawablePath> paths = frame.getAresData().getPaths();
		for (TrackedTigerBot bot : frame.getWorldFrame().getTigerBotsAvailable().values())
		{
			DrawablePath dPath = paths.get(bot.getId());
			if ((dPath != null) && (dPath.getPath() != null) && dPath.getPath().isRambo())
			{
				// --- mark rambo ---
				g.setColor(Color.red);
				IVector2 pos = bot.getPos();
				drawBotCircle(g, pos, frame.getWorldFrame().isInverted(), -2);
			}
		}
	}
	
	
	private void drawPathPointsUnsmoothed(final Graphics2D g, final TrackedTigerBot bot, final IPath path,
			final Color color,
			final boolean invert)
	{
		if ((path == null) || (bot == null))
		{
			return;
		}
		
		g.setColor(color);
		g.setStroke(new BasicStroke(1));
		final GeneralPath drawPath = new GeneralPath();
		
		IVector2 startPos = path.getStartPos() == null ? bot.getPos() : path.getStartPos();
		final IVector2 transBotPos = getFieldPanel().transformToGuiCoordinates(startPos, invert);
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		drawPath.moveTo(robotX, robotY);
		
		for (IVector2 point : path.getUnsmoothedPathPoints())
		{
			final IVector2 transPathPoint = getFieldPanel().transformToGuiCoordinates(point, invert);
			g.drawOval((int) transPathPoint.x() - 1, (int) transPathPoint.y() - 1, 3, 3);
			drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
		}
		g.draw(drawPath);
	}
	
	
	private void drawPathPoints(final Graphics2D g, final TrackedTigerBot bot, final IPath path, final Color color,
			final boolean invert)
	{
		if ((path == null) || (bot == null))
		{
			return;
		}
		
		g.setColor(color);
		g.setStroke(new BasicStroke(1));
		final GeneralPath drawPath = new GeneralPath();
		if (path.getCurrentDestinationNodeIdx() > 0)
		{
			IVector2 point = path.getPathPoints().get(path.getCurrentDestinationNodeIdx() - 1);
			final IVector2 transPathPoint = getFieldPanel().transformToGuiCoordinates(point, invert);
			g.drawOval((int) transPathPoint.x() - 1, (int) transPathPoint.y() - 1, 3, 3);
			drawPath.moveTo((int) transPathPoint.x(), (int) transPathPoint.y());
		} else
		{
			IVector2 startPos = path.getStartPos() == null ? bot.getPos() : path.getStartPos();
			final IVector2 transBotPos = getFieldPanel().transformToGuiCoordinates(startPos, invert);
			final int robotX = (int) transBotPos.x();
			final int robotY = (int) transBotPos.y();
			drawPath.moveTo(robotX, robotY);
		}
		for (int i = path.getCurrentDestinationNodeIdx(); i < path.getPathPoints().size(); i++)
		{
			IVector2 point = path.getPathPoints().get(i);
			final IVector2 transPathPoint = getFieldPanel().transformToGuiCoordinates(point, invert);
			g.drawOval((int) transPathPoint.x() - 1, (int) transPathPoint.y() - 1, 3, 3);
			drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
		}
		g.draw(drawPath);
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
	public void showDecoration(final boolean visible)
	{
		showDecoration = visible;
	}
	
	
	/**
	 * @param showPPErrorTree the showPPErrorTree to set
	 */
	public void showDebug(final boolean showPPErrorTree)
	{
		showDebug = showPPErrorTree;
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
	public void showPotentialDecoration(final boolean visible)
	{
		showPotentialDecoration = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showPotentialDebug(final boolean visible)
	{
		showPotentialDebug = visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void showUnsmoothedPaths(final boolean visible)
	{
		showUnsmoothedPath = visible;
	}
	
	
	/**
	 * @param isSelected
	 */
	public void showRambo(final boolean isSelected)
	{
		showRamboTree = isSelected;
	}
}
