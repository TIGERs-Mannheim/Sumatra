/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.view.statepanel;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
public class StateVertice
{
	private int x;
	private int y;
	private int w;
	private int h;
	private String name;

	enum HorizontalConnectorPosition
	{
		LEFT,
		MIDDLE,
		RIGHT
	}

	enum VerticalConnectorPosition
	{
		BOTTOM,
		MIDDLE,
		TOP
	}


	public void draw(Graphics2D g2)
	{
		int width = w - 6;
		int height = h / 2;
		int posX = x + 3 + width / 2;
		int posY = y + height / 2;
		g2.setColor(Color.black);
		g2.drawRect(x, y, w, h);
		g2.setColor(new Color(170, 170, 255, 100));
		g2.fillRect(x, y, w, h);
		g2.setColor(Color.black);
		g2.drawString(name, posX - width / 2, (int) (posY - height / 2.0 + height * 1.25));
	}


	public void drawEdges(Graphics2D g2,
			final List<StateEdge> stateEdges,
			final Map<String, StateVertice> drawableVertices)
	{
		if (stateEdges == null)
		{
			return;
		}

		g2.setColor(Color.BLACK);
		for (StateEdge edge : stateEdges)
		{
			drawEdge(g2, drawableVertices, edge);
		}
	}


	private void drawEdge(Graphics2D g2, Map<String, StateVertice> drawableVertices, StateEdge edge)
	{
		StateVertice to = drawableVertices.get(edge.getToVertice());
		HorizontalConnectorPosition fromHConnector = HorizontalConnectorPosition.MIDDLE;
		HorizontalConnectorPosition toHConnector = HorizontalConnectorPosition.MIDDLE;
		if (x + w < to.x)
		{
			fromHConnector = HorizontalConnectorPosition.RIGHT;
			toHConnector = HorizontalConnectorPosition.LEFT;
		} else if (x > to.x + to.w)
		{
			fromHConnector = HorizontalConnectorPosition.LEFT;
			toHConnector = HorizontalConnectorPosition.RIGHT;
		}

		VerticalConnectorPosition fromVConnector = VerticalConnectorPosition.MIDDLE;
		VerticalConnectorPosition toVConnector = VerticalConnectorPosition.MIDDLE;
		if (y + h < to.y)
		{
			fromVConnector = VerticalConnectorPosition.BOTTOM;
			toVConnector = VerticalConnectorPosition.TOP;
		} else if (y > to.y + to.h)
		{
			fromVConnector = VerticalConnectorPosition.TOP;
			toVConnector = VerticalConnectorPosition.BOTTOM;
		}

		int fromX = getFromX(fromHConnector);
		int fromY = getFromY(fromVConnector);
		int toX = getToX(to, toHConnector);
		int toY = getToY(to, toVConnector);

		drawArrow(g2, fromX, fromY, toX, toY, 5);
		IVector2 textPos = Vector2.fromXY(x, y)
				.addNew(Vector2.fromXY(to.x, to.y).subtractNew(Vector2.fromXY(x, y)).multiplyNew(0.75));
		g2.drawString(edge.getEventText(), (int) textPos.x(), (int) textPos.y());
	}


	private int getToY(StateVertice to, VerticalConnectorPosition toVConnector)
	{
		int toY = 0;
		switch (toVConnector)
		{
			case BOTTOM:
				toY = to.y + to.h;
				break;
			case MIDDLE:
				toY = to.y + to.h / 2;
				break;
			case TOP:
				toY = to.y;
				break;
		}
		return toY;
	}


	private int getToX(StateVertice to, HorizontalConnectorPosition toHConnector)
	{
		int toX = 0;
		switch (toHConnector)
		{
			case LEFT:
				toX = to.x;
				break;
			case MIDDLE:
				toX = to.x + to.w / 2;
				break;
			case RIGHT:
				toX = to.x + to.w;
				break;
		}
		return toX;
	}


	private int getFromY(VerticalConnectorPosition fromVConnector)
	{
		return getToY(StateVertice.this, fromVConnector);
	}


	private int getFromX(HorizontalConnectorPosition fromHConnector)
	{
		return getToX(StateVertice.this, fromHConnector);
	}


	private void drawArrow(Graphics2D g1, int x1, int y1, int x2, int y2, int arrowTipSize)
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
		g.drawLine(0, 0, len - arrowTipSize, 0);
		g.fillPolygon(new int[] { len, len - arrowTipSize, len - arrowTipSize, len },
				new int[] { 0, -arrowTipSize, arrowTipSize, 0 }, 4);
	}
}
