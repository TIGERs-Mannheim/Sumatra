/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;


/**
 * Outline of a bot with orientation
 */
@Persistent
public class DrawableBotShape implements IDrawableShape
{
	protected final IVector2 pos;
	protected final double angle;
	private double center2DribblerDist;
	protected double radius;
	private Color fillColor = null;
	private Color borderColor = Color.WHITE;
	private Color fontColor = Color.black;
	private String id = "";
	private boolean drawDirection = true;


	@SuppressWarnings("unused")
	private DrawableBotShape()
	{
		this(Vector2f.ZERO_VECTOR, 0, 0, 0);
	}


	public DrawableBotShape(final IVector2 pos, final double angle, final double radius,
			final double center2DribblerDist)
	{
		this.pos = pos;
		this.angle = angle;
		this.radius = radius;
		this.center2DribblerDist = center2DribblerDist;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final int robotRadius = tool.scaleGlobalToGui(radius);

		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transBotPos = tool.transformToGuiCoordinates(pos, invert);
		int drawingX = (int) transBotPos.x() - robotRadius;
		int drawingY = (int) transBotPos.y() - robotRadius;


		double r = radius;
		double alpha = SumatraMath.acos(center2DribblerDist / r);
		double startAngleRad = tool.transformToGuiAngle(angle, invert) + alpha;
		double startAngle = AngleMath.rad2deg(startAngleRad);
		double angleExtent = 360 - AngleMath.rad2deg(2 * alpha);

		Shape botShape = new Arc2D.Double(drawingX, drawingY, robotRadius * 2.0, robotRadius * 2.0, startAngle,
				angleExtent, Arc2D.CHORD);

		if (fillColor != null)
		{
			g.setColor(fillColor);
			g.fill(botShape);
		}
		if (borderColor != null)
		{
			g.setColor(borderColor);
			g.draw(botShape);
		}

		if (drawDirection)
		{
			g.setColor(Color.RED);
			final IVector2 kickerPos = tool.transformToGuiCoordinates(
					BotShape.getKickerCenterPos(pos, angle, center2DribblerDist - 20), invert);
			g.drawLine(drawingX + robotRadius, drawingY + robotRadius, (int) kickerPos.x(), (int) kickerPos.y());
		}

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
			return;
		}

		// --- draw id and direction-sign ---
		g.setColor(fontColor);
		g.setFont(new Font("Courier", Font.BOLD, (int) (robotRadius * 1.5)));
		g.drawString(id, idX, idY);
	}


	@Override
	public final DrawableBotShape setColor(final Color color)
	{
		this.fillColor = color;
		return this;
	}


	/**
	 * @param fontColor the fontColor to set
	 */
	public final void setFontColor(final Color fontColor)
	{
		this.fontColor = fontColor;
	}


	public void setFillColor(final Color fillColor)
	{
		this.fillColor = fillColor;
	}


	public void setBorderColor(final Color borderColor)
	{
		this.borderColor = borderColor;
	}


	/**
	 * @param id the id to set
	 */
	public final void setId(final String id)
	{
		this.id = id;
	}


	/**
	 * @param radius the radius to set
	 */
	public final void setRadius(final double radius)
	{
		this.radius = radius;
	}


	public void setDrawDirection(final boolean drawDirection)
	{
		this.drawDirection = drawDirection;
	}
}
