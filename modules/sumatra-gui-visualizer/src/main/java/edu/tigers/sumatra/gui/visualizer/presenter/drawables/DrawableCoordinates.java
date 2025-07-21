/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.drawables;

import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.util.ScalingUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;


public record DrawableCoordinates(IVector2 point, ETeamColor teamColor) implements IDrawableShape
{
	private static final int FONT_SIZE = ScalingUtil.getFontSize(EFontSize.LARGE);
	private static final DecimalFormat DF = new DecimalFormat("#####");


	@Override
	public void paintBorder(Graphics2D g, int width, int height)
	{
		g.setFont(new Font("", Font.PLAIN, FONT_SIZE));
		g.setColor(getColor());

		int y = height - (int) (FONT_SIZE * 1.5);
		int x = getX(width);

		IVector2 pointForTeam = pointForTeam();
		g.drawString("x:" + DF.format(pointForTeam.x()), x, y);
		g.drawString("y:" + DF.format(pointForTeam.y()), x, y + FONT_SIZE + 1);
	}


	private IVector2 pointForTeam()
	{
		if (teamColor.isNonNeutral() && Geometry.getNegativeHalfTeam() != teamColor)
		{
			return point.multiplyNew(-1);
		}
		return point;
	}


	private Color getColor()
	{
		return switch (teamColor)
				{
					case YELLOW -> Color.YELLOW;
					case BLUE -> Color.BLUE;
					default -> Color.WHITE;
				};
	}


	private int getX(int width)
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			return 10;
		} else if (teamColor == ETeamColor.BLUE)
		{
			return width - (FONT_SIZE * 5);
		}
		return width / 2 - (FONT_SIZE * 5);
	}
}
