/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.field;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.io.Serial;


/**
 * Visualization of the field.
 */
@Log4j2
public class FieldPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 4330620225157027091L;

	@Getter
	@Setter
	private transient Image offImage;


	public void addMouseAdapter(MouseAdapter mouseAdapter)
	{
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
	}


	public void removeMouseAdapter(MouseAdapter mouseAdapter)
	{
		removeMouseListener(mouseAdapter);
		removeMouseMotionListener(mouseAdapter);
		removeMouseWheelListener(mouseAdapter);
	}


	@Override
	public void paint(final Graphics g1)
	{
		Image image = offImage;
		if (image != null)
		{
			g1.drawImage(image, 0, 0, this);
		} else
		{
			g1.clearRect(0, 0, getWidth(), getHeight());
		}
	}
}
