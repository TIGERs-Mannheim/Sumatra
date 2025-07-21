/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


public class DrawableBotPattern extends DrawableBotShape
{
	private final BotID botID;

	private static final List<Color> blobColorUpperLeft = new ArrayList<>();
	private static final List<Color> blobColorUpperRight = new ArrayList<>();
	private static final List<Color> blobColorLowerLeft = new ArrayList<>();
	private static final List<Color> blobColorLowerRight = new ArrayList<>();

	static
	{
		Color magenta = Color.magenta;
		Color green = Color.green;

		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(green);
		blobColorUpperLeft.add(magenta);
		blobColorUpperLeft.add(magenta);

		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(magenta);
		blobColorUpperRight.add(green);
		blobColorUpperRight.add(green);

		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(magenta);
		blobColorLowerLeft.add(green);
		blobColorLowerLeft.add(magenta);

		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(magenta);
		blobColorLowerRight.add(green);
		blobColorLowerRight.add(magenta);
	}


	public DrawableBotPattern(
			final IVector2 pos,
			final double angle,
			final double radius,
			final double center2DribblerDist,
			final BotID botID)
	{
		super(pos, angle, radius, center2DribblerDist);
		this.botID = botID;

		setBorderColor(botID.getTeamColor() == ETeamColor.YELLOW ? Color.white : Color.black);
		setFillColor(botID.getTeamColor() == ETeamColor.YELLOW ? Color.darkGray : Color.black);
		setDrawDirection(false);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		int centerDiameterX = tool.scaleGlobalToGui(50);
		int centerDiameterY = tool.scaleGlobalToGui(50);
		int otherDiameterX = tool.scaleGlobalToGui(40);
		int otherDiameterY = tool.scaleGlobalToGui(40);

		final IVector2 center = tool.transformToGuiCoordinates(pos, invert);

		g.setColor(botID.getTeamColor().getColor());
		fillBlob(g, center, centerDiameterX, centerDiameterY);

		IVector2 upperLeft = tool.transformToGuiCoordinates(pos.addNew(Vector2.fromXY(35, 54.772).turn(angle)), invert);
		g.setColor(blobColorUpperLeft.get(botID.getNumber()));
		fillBlob(g, upperLeft, otherDiameterX, otherDiameterY);

		IVector2 upperRight = tool.transformToGuiCoordinates(pos.addNew(Vector2.fromXY(35, -54.772).turn(angle)), invert);
		g.setColor(blobColorUpperRight.get(botID.getNumber()));
		fillBlob(g, upperRight, otherDiameterX, otherDiameterY);

		IVector2 lowerLeft = tool.transformToGuiCoordinates(pos.addNew(Vector2.fromXY(-54.772, 35).turn(angle)), invert);
		g.setColor(blobColorLowerLeft.get(botID.getNumber()));
		fillBlob(g, lowerLeft, otherDiameterX, otherDiameterY);

		IVector2 lowerRight = tool.transformToGuiCoordinates(pos.addNew(Vector2.fromXY(-54.772, -35).turn(angle)),
				invert);
		g.setColor(blobColorLowerRight.get(botID.getNumber()));
		fillBlob(g, lowerRight, otherDiameterX, otherDiameterY);
	}


	private void fillBlob(final Graphics2D g, final IVector2 p, final int w, final int h)
	{
		g.fillArc((int) p.x() - w / 2,
				(int) p.y() - h / 2,
				w,
				h,
				0,
				360);
	}
}
