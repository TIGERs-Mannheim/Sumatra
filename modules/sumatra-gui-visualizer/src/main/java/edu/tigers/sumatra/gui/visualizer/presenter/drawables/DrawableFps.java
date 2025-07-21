/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.drawables;

import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;


@RequiredArgsConstructor
public class DrawableFps implements IDrawableShape
{
	private static final DecimalFormat DF = new DecimalFormat("#.0");
	private final FpsCounter fpsCounter;


	@Override
	public void paintBorder(Graphics2D g, int width, int height)
	{
		int fontSize = ScalingUtil.getFontSize(EFontSize.SMALL);
		g.setFont(new Font("", Font.PLAIN, fontSize));
		g.setColor(Color.black);

		int x = width - fontSize * 3;
		int y = 20;
		g.drawString(DF.format(fpsCounter.getAvgFps()), x, y);
	}
}
