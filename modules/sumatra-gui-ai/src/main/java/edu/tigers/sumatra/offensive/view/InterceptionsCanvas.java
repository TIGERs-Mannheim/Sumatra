/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.InterceptionIteration;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.InterceptionZeroAxisCrossing;
import edu.tigers.sumatra.ids.BotID;
import lombok.Setter;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.Optional;


public class InterceptionsCanvas extends JPanel
{
	@Setter
	private transient BotID selectedBot;
	@Setter
	private transient Map<BotID, BallInterceptionInformation> interceptionInformation;

	private int w = getWidth();
	private int h = getHeight();

	private static final int PADDING = 50;
	private static final int MAXY = 3;
	private static final int MINY = -4;
	private static final int MAXX = 4;
	private static final int MINX = 0;
	private int axisH = h - 2 * PADDING;
	private int axisW = w - 2 * PADDING;


	public InterceptionsCanvas()
	{
		setLayout(new BorderLayout());
	}


	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		w = getWidth();
		h = getHeight();
		axisH = h - 2 * PADDING;
		axisW = w - 2 * PADDING;

		if (axisH < 30 || axisW < 60)
		{
			// Window is really small, it is not reasonable to draw anything
			return;
		}

		drawAxis(g2);

		// draw second run zero line
		Point target1 = convertDataPointToGUICoordinates(MINX, 1);
		Point target2 = convertDataPointToGUICoordinates(MAXX, 1);
		g2.setColor(Color.MAGENTA);
		g2.drawLine(target1.x, target1.y, target2.x, target2.y);

		drawAxisLabels(g2);

		if (selectedBot == null || interceptionInformation == null || !interceptionInformation.containsKey(selectedBot))
		{
			return;
		}

		BallInterceptionInformation information = interceptionInformation.get(selectedBot);

		drawDataSamples(g2, information);
		drawZeroAxisIntersections(g2, information);
		drawCorridors(g2, information);
		drawWithoutBonusLines(g2, information);
		drawOldPositionCross(g2, information);
	}


	private void drawWithoutBonusLines(final Graphics2D g2, final BallInterceptionInformation information)
	{
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(125, 125, 125, 200));
		var oldInterception = Optional.ofNullable(information.getOldInterception());
		double ballTimeMid = oldInterception.map(InterceptionIteration::getBallTravelTime).orElse(0.0);
		double slackTimeBonus = oldInterception.map(InterceptionIteration::getIncludedSlackTimeBonus).orElse(0.0);
		double slackTimeMid = oldInterception.map(InterceptionIteration::getSlackTime).orElse(0.0) + slackTimeBonus;

		int idx = -1;
		double minDist = Double.MAX_VALUE;
		double midSlackTime = 0;
		for (int i = 0; i < information.getInitialIterations().size(); i++)
		{
			var iteration = information.getInitialIterations().get(i);
			double dist = Math.abs(iteration.getBallTravelTime() - ballTimeMid);
			if (dist < minDist)
			{
				minDist = dist;
				idx = i;
				midSlackTime = iteration.getSlackTime();
			}
		}

		if (idx == -1)
		{
			return;
		}

		if (idx > 0 && idx < information.getInitialIterations().size() - 1)
		{
			double ballTimeStart = information.getInitialIterations().get(idx - 1).getBallTravelTime();
			double slackTimeStart = information.getInitialIterations().get(idx - 1).getSlackTime();
			double ballTimeEnd = information.getInitialIterations().get(idx + 1).getBallTravelTime();
			double slackTimeEnd = information.getInitialIterations().get(idx + 1).getSlackTime();

			if (ballTimeStart > MAXX || slackTimeStart > MAXY || ballTimeEnd < MINX
					|| slackTimeEnd < MINY)
			{
				return;
			}

			Point start1 = convertDataPointToGUICoordinates(ballTimeStart, slackTimeStart);
			Point end1 = convertDataPointToGUICoordinates(ballTimeMid, slackTimeMid);

			Point start2 = convertDataPointToGUICoordinates(ballTimeMid, slackTimeMid);
			Point end2 = convertDataPointToGUICoordinates(ballTimeEnd, slackTimeEnd);

			g2.drawLine(start1.x, start1.y, end1.x, end1.y);
			g2.drawLine(start2.x, start2.y, end2.x, end2.y);

			Point end3 = convertDataPointToGUICoordinates(ballTimeMid, midSlackTime);

			g2.setColor(new Color(10, 10, 200, 50));
			int[] xPoints = { start1.x, end1.x, end2.x, end3.x };
			int[] yPoints = { start1.y, end1.y, end2.y, end3.y };
			g2.fillPolygon(xPoints, yPoints, 4);
		}
	}


	private void drawOldPositionCross(final Graphics2D g2, final BallInterceptionInformation information)
	{
		var oldInterception = information.getOldInterception();
		if (oldInterception == null)
		{
			return;
		}
		g2.setColor(Color.GREEN.darker());
		g2.setStroke(new BasicStroke(3));
		double ballTravelTime = oldInterception.getBallTravelTime();
		double originalSlackTime = oldInterception.getSlackTime() + oldInterception.getIncludedSlackTimeBonus();
		Point start = convertDataPointToGUICoordinates(ballTravelTime - 0.1, originalSlackTime - 0.1);
		Point end = convertDataPointToGUICoordinates(ballTravelTime + 0.1, originalSlackTime + 0.1);
		Point start2 = convertDataPointToGUICoordinates(ballTravelTime - 0.1, originalSlackTime + 0.1);
		Point end2 = convertDataPointToGUICoordinates(ballTravelTime + 0.1, originalSlackTime - 0.1);
		g2.drawLine(start.x, start.y, end.x, end.y);
		g2.drawLine(end.x, start.y, start.x, end.y);
		g2.drawLine(start2.x, start2.y, end2.x, end2.y);
		g2.drawLine(end2.x, start2.y, start2.x, end2.y);

		g2.setColor(Color.MAGENTA);
		double fallBackTravelTime = information.getInterceptionTargetTimeFallback();
		Point startF = convertDataPointToGUICoordinates(fallBackTravelTime - 0.1, 1 - 0.1);
		Point endF = convertDataPointToGUICoordinates(fallBackTravelTime + 0.1, 1 + 0.1);
		Point start2F = convertDataPointToGUICoordinates(fallBackTravelTime - 0.1, 1 + 0.1);
		Point end2F = convertDataPointToGUICoordinates(fallBackTravelTime + 0.1, 1 - 0.1);
		g2.drawLine(startF.x, startF.y, endF.x, endF.y);
		g2.drawLine(endF.x, startF.y, startF.x, endF.y);
		g2.drawLine(start2F.x, start2F.y, end2F.x, end2F.y);
		g2.drawLine(end2F.x, start2F.y, start2F.x, end2F.y);
	}


	private void drawCorridors(final Graphics2D g2, final BallInterceptionInformation information)
	{
		for (var corr : information.getInterceptionCorridors())
		{
			g2.setColor(Color.BLUE.darker());
			g2.setStroke(new BasicStroke(3));
			Point start = convertDataPointToGUICoordinates(corr.getStartTime(), -2.0);
			Point end = convertDataPointToGUICoordinates(Math.min(MAXX, corr.getEndTime()), -2.0);

			if (end.x - start.x > 20)
			{
				drawArrow(g2, start.x, start.y, end.x, end.y, 8);
				drawArrow(g2, end.x, start.y, start.x, end.y, 8);
			} else
			{
				g2.drawLine(start.x, start.y, end.x, end.y);
				g2.drawLine(end.x, start.y, start.x, end.y);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			String corridorWidthString = String.format("%.0fmm", corr.getWidth());
			String corridorTimeString = String.format("%.2fs", corr.getEndTime() - corr.getStartTime());
			int xOffset = 0;
			if (end.x - start.x < 100)
			{
				xOffset = end.x - start.x;
				if (start.x + 10 + xOffset > PADDING + axisW)
				{
					xOffset = -65;
				}
			}
			g2.drawString(corridorWidthString, start.x + 10 + xOffset, start.y + 20);
			g2.drawString(corridorTimeString, start.x + 10 + xOffset, start.y + 35);

			g2.setStroke(new BasicStroke(4));
			g2.setColor(new Color(255, 0, 0, 77));
			if (!information.getInterceptionCorridors().isEmpty())
			{
				Point target1 = convertDataPointToGUICoordinates(information.getInterceptionTargetTime(), MINY);
				Point target2 = convertDataPointToGUICoordinates(information.getInterceptionTargetTime(), MAXY);
				g2.drawLine(target1.x, target1.y, target2.x, target2.y);
			}
		}
	}


	private void drawZeroAxisIntersections(final Graphics2D g2, final BallInterceptionInformation information)
	{
		g2.setColor(Color.BLUE.darker());
		for (InterceptionZeroAxisCrossing point : information.getZeroAxisChanges())
		{
			Point data = convertDataPointToGUICoordinates(point.getBallTravelTime(), 0);
			Point dataStart = convertDataPointToGUICoordinates(point.getBallTravelTime(), MINY);
			g2.drawLine(data.x, data.y, dataStart.x, dataStart.y);
		}
	}


	private void drawDataSamples(final Graphics2D g2, final BallInterceptionInformation information)
	{
		g2.setStroke(new BasicStroke(3));
		Point old = null;
		for (InterceptionIteration point : information.getInitialIterations())
		{
			if (point.getBallTravelTime() > MAXX || point.getSlackTime() > MAXY || point.getBallTravelTime() < MINX
					|| point.getSlackTime() < MINY)
			{
				continue;
			}
			Point data = convertDataPointToGUICoordinates(point.getBallTravelTime(), point.getSlackTime());
			Point dataStart = convertDataPointToGUICoordinates(point.getBallTravelTime(), MINY);
			g2.setColor(new Color(141, 141, 141, 128));
			g2.drawLine(data.x, data.y, dataStart.x, dataStart.y);
			g2.setColor(Color.BLACK);
			if (old != null)
			{
				g2.drawLine(old.x, old.y, data.x, data.y);
			}
			old = data;
		}
	}


	private void drawAxisLabels(final Graphics2D g2)
	{
		g2.setColor(Color.black);
		double yStepSize = (1.0 / axisH) * 200;
		for (double u = MINY; u < MAXY; u = u + yStepSize)
		{
			double progress = 1 - (u - MINY) / (MAXY - MINY);
			g2.drawLine(
					PADDING - 10,
					(int) (progress * (axisH) + PADDING),
					PADDING,
					(int) (progress * (axisH) + PADDING));
			String time = String.format("%.2f", u);
			g2.drawString(time, PADDING - 30, (int) (progress * (axisH) + PADDING) - 3);
		}
		g2.drawString("Slack Time [s]", PADDING - 30, PADDING - 20);

		double xStepSize = (1.0 / axisW) * 300;
		for (double u = MINX; u < MAXX; u = u + xStepSize)
		{
			double progress = (u - MINX) / (MAXX - MINX);
			g2.drawLine(
					(int) (progress * (axisW) + PADDING),
					h - PADDING + 10,
					(int) (progress * (axisW) + PADDING),
					h - PADDING);
			String time = String.format("%.2f", u);
			g2.drawString(time, (int) (progress * (axisW) + PADDING) - 15, h - PADDING + 25);
		}
		g2.drawString("Ball Travel Time [s]", axisW + PADDING - 60, h - PADDING + 40);
	}


	private void drawAxis(final Graphics2D g2)
	{
		Point zeroLine1 = convertDataPointToGUICoordinates(0, 0);
		Point zeroLine2 = convertDataPointToGUICoordinates(MAXX, 0);

		g2.setColor(new Color(215, 255, 187, 128));
		Point zeroLine3 = convertDataPointToGUICoordinates(MAXX, MINY);
		Point zeroLine4 = convertDataPointToGUICoordinates(MAXX, MAXY);
		g2.fillRect(zeroLine1.x, zeroLine1.y, zeroLine3.x - zeroLine1.x, zeroLine3.y - zeroLine1.y);

		g2.setColor(new Color(255, 190, 190, 128));
		g2.fillRect(zeroLine1.x, zeroLine4.y, zeroLine4.x - zeroLine1.x, zeroLine1.y - zeroLine4.y);

		g2.setColor(Color.RED.darker());
		g2.drawLine(zeroLine1.x, zeroLine1.y, zeroLine2.x, zeroLine2.y);

		g2.setColor(Color.BLACK);
		drawArrow(g2, PADDING, h - PADDING, PADDING, PADDING, 5);
		drawArrow(g2, PADDING, h - PADDING, w - PADDING, h - PADDING, 5);
	}


	private Point convertDataPointToGUICoordinates(double ballTravelTime, double slackTime)
	{
		double progressY = 1 - (slackTime - MINY) / (MAXY - MINY);
		double progressX = (ballTravelTime - MINX) / (MAXX - MINX);
		int posY = (int) (progressY * (axisH) + PADDING);
		int posX = (int) (progressX * (axisW) + PADDING);
		return new Point(posX, posY);
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
