/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.penarea.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


@Persistent
public class AnimatedArrowsOnFinisherMoveShape extends AAnimatedShape
{

	private final FinisherMoveShape shape;
	private final INumberAnimator stepPos;

	private double stepStart;
	private double stepEnd;
	private int transformedArrowSize;


	private AnimatedArrowsOnFinisherMoveShape()
	{
		// Berkeley
		shape = null;
		stepPos = null;
	}


	private AnimatedArrowsOnFinisherMoveShape(FinisherMoveShape shape, double stepStart, double stepStop,
			INumberAnimator stepPos)
	{
		this.shape = shape;
		this.stepPos = stepPos;
		this.stepStart = stepStart;
		this.stepEnd = stepStop;
	}


	public static AnimatedArrowsOnFinisherMoveShape createArrowsOnFinisherMoveShape(FinisherMoveShape shape,
			double stepStart, double stepStop)
	{
		return new AnimatedArrowsOnFinisherMoveShape(shape, stepStart, stepStop,
				new NumberAnimatorMinMax(stepStart, stepStop, new AnimationTimerUp(3)));
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		double directionOffset = stepStart > stepEnd ? -10 : 10;
		int numOfArrows = 8;
		double arrowDistances = 80;
		double stepPosNumber = stepPos.getNumber();
		for (int i = 0; i < numOfArrows; i++)
		{
			double pos = getPos(stepPosNumber);
			IVector2 basePoint = shape.stepOnShape(pos);
			IVector2 direction = shape.stepOnShape(pos + directionOffset).subtractNew(basePoint);
			IVector2 guiPosition = tool.transformToGuiCoordinates(basePoint, invert);
			IVector2 guiDestination = tool.transformToGuiCoordinates(basePoint.addNew(direction), invert);

			transformedArrowSize = tool.scaleGlobalToGui(55);
			if (stepEnd > stepStart)
			{
				if (pos < stepEnd)
				{
					Color color = g.getColor();
					int alpha = (int) (SumatraMath.relative(pos, stepEnd, stepStart + ((stepEnd - stepStart) / 2.0)) * 255);
					Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
					drawArrow(g, (int) guiPosition.x(), (int) guiPosition.y(), (int) guiDestination.x(),
							(int) guiDestination.y(), newColor, new Color(0, 0, 0, alpha));
				}
				stepPosNumber -= arrowDistances;
			} else
			{
				if (pos > stepEnd)
				{
					Color color = g.getColor();
					int alpha = (int) (SumatraMath.relative(pos, stepEnd - ((stepStart - stepEnd) / 2.0), stepStart) * 255);
					Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
					drawArrow(g, (int) guiPosition.x(), (int) guiPosition.y(), (int) guiDestination.x(),
							(int) guiDestination.y(), newColor, new Color(0, 0, 0, alpha));
				}
				stepPosNumber += arrowDistances;
			}

		}
	}


	private double getPos(double stepPosNumber)
	{
		double pos = stepPosNumber;
		if (stepEnd > stepStart)
		{
			if (stepPosNumber < stepStart)
			{
				return stepEnd - (stepStart - stepPosNumber);
			}
		} else
		{
			if (stepPosNumber > stepStart)
			{
				return stepEnd - (stepStart - stepPosNumber);
			}
		}
		return pos;
	}


	private void drawArrow(Graphics2D g1, int x1, int y1, int x2, int y2, Color fillColor, Color borderColor)
	{
		Graphics2D g = (Graphics2D) g1.create();
		double dx = x2 - (double) x1;
		double dy = y2 - (double) y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.setColor(fillColor);
		g.fillPolygon(new int[] { len, len - transformedArrowSize, len - transformedArrowSize, len },
				new int[] { 0, -transformedArrowSize, transformedArrowSize, 0 }, 4);

		g.setColor(borderColor);
		g.drawPolygon(new int[] { len, len - transformedArrowSize, len - transformedArrowSize, len },
				new int[] { 0, -transformedArrowSize, transformedArrowSize, 0 }, 4);
	}
}
