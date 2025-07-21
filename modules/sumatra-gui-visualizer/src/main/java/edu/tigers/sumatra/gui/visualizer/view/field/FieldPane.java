/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.field;

import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.vis.RefereeVisCalc;
import lombok.Data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.List;


@Data
public class FieldPane
{
	private static final Color FIELD_COLOR_BACKGROUND = new Color(93, 93, 93);
	private static final double SCALE_MIN = 1e-4;
	private static final double SCALE_MAX = 10;

	private double offsetX;
	private double offsetY;
	private int width;
	private int height;
	private double scale = 1;
	private boolean addBorderOffset;
	private boolean fancyPainting = true;

	private final FieldTransformation transformation = new FieldTransformation();


	public void processFieldBackground(DrawableFieldBackground s)
	{
		transformation.setFieldGlobalBoundaryWidth(s.getBoundaryWidth());
		transformation.setFieldGlobalLength(s.getFieldWithBorder().xExtent() - 2 * s.getBoundaryWidth());
		transformation.setFieldGlobalWidth(s.getFieldWithBorder().yExtent() - 2 * s.getBoundaryWidth());
	}


	public IVector2 getFieldPos(int x, int y)
	{
		return Vector2.fromXY(x - offsetX, y - offsetY - getBorderOffset()).multiply(1f / scale);
	}


	public void scale(Point point, double scroll)
	{
		final double xLen = ((point.x - offsetX) / scale) * 2;
		final double yLen = ((point.y - offsetY) / scale) * 2;

		final double oldLenX = (xLen) * scale;
		final double oldLenY = (yLen) * scale;

		scale *= Math.exp(scroll);
		scale = SumatraMath.cap(scale, SCALE_MIN, SCALE_MAX);

		final double newLenX = (xLen) * scale;
		final double newLenY = (yLen) * scale;
		offsetX -= (newLenX - oldLenX) / 2;
		offsetY -= (newLenY - oldLenY) / 2;
		transformation.setScale(scale);
	}


	public void reset()
	{
		int borderOffset = getBorderOffset();
		int offsetHeight = height - borderOffset;
		transformation.setFieldTurn(EFieldTurn.bestFor(width, offsetHeight));
		scale = transformation.getFieldScale(width, offsetHeight);

		if (transformation.getFieldTurn() == EFieldTurn.T90)
		{
			offsetY = (offsetHeight - scale * (transformation.getFieldTotalHeight())) / 2;
			offsetX = (width - scale * (transformation.getFieldTotalWidth())) / 2;
		} else
		{
			offsetX = (width - scale * (transformation.getFieldTotalHeight())) / 2;
			offsetY = (offsetHeight - scale * (transformation.getFieldTotalWidth())) / 2;
		}
	}


	public int getBorderOffset()
	{
		if (addBorderOffset)
		{
			return (int) (RefereeVisCalc.BORDER_TEXT_HEIGHT * (double) width / DrawableBorderText.BORDER_TEXT_WIDTH);
		}
		return 0;
	}


	public void drag(int dx, int dy)
	{
		offsetX += dx;
		offsetY += dy;
	}


	public void paint(Graphics2D g2, List<ShapeMap.ShapeLayer> shapeLayers)
	{
		final BasicStroke defaultStroke = new BasicStroke(Math.max(1, transformation.scaleGlobalToGui(10)));
		g2.setColor(FIELD_COLOR_BACKGROUND);
		g2.fillRect(0, 0, width, height);

		if (fancyPainting)
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		g2.translate(offsetX, offsetY + getBorderOffset());
		g2.scale(scale, scale);
		shapeLayers.forEach(shapeLayer -> paintShapeMap(g2, shapeLayer, defaultStroke));
		g2.scale(1.0 / scale, 1.0 / scale);
		g2.translate(-offsetX, -offsetY - getBorderOffset());

		g2.setColor(FIELD_COLOR_BACKGROUND);
		g2.fillRect(0, 0, width, getBorderOffset());

		shapeLayers.forEach(shapeLayer -> paintShapeMapBorderText(g2, shapeLayer, defaultStroke));
	}


	private void paintShapeMapBorderText(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().forEach(s -> s.paintBorder(gDerived, width, height));
		gDerived.dispose();
	}


	private void paintShapeMap(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().forEach(s -> s.paintShape(gDerived, transformation, shapeLayer.isInverted()));
		gDerived.dispose();
	}
}
