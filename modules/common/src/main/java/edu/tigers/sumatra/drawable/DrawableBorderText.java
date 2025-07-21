/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.Font;
import java.awt.Graphics2D;


/**
 * Draw text on the borders (detached from the field).
 */
@RequiredArgsConstructor
public class DrawableBorderText extends ADrawable
{
	public static final int BORDER_TEXT_WIDTH = 750;

	private final IVector2 pos;
	private final String text;
	@Setter
	@Accessors(chain = true)
	private EFontSize fontSize = EFontSize.SMALL;


	@Override
	public void paintBorder(Graphics2D g, int width, int height)
	{
		super.paintBorder(g, width, height);

		double scale = (double) width / BORDER_TEXT_WIDTH;
		int x = (int) (ScalingUtil.scale(pos.x()) * scale);
		int y = (int) (ScalingUtil.scale(pos.y()) * scale);
		int scaledFontSize = (int) (ScalingUtil.getFontSize(this.fontSize) * scale);

		Font font = new Font("", Font.PLAIN, scaledFontSize);
		g.setFont(font);
		g.drawString(text, x, y);
	}
}
