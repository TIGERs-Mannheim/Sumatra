/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.MotorInputPanel.IMotorInputPanelObserver;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Pretty fancy input component for robot XY control.
 * 
 * @author AndreR
 */
public class MotorEnhancedInputPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= 6741030318432376797L;
	
	private static final int							SIZE					= 400;
	
	private Vector2										target				= Vector2.fromXY(0, 0);
	private Vector2										latest				= Vector2.fromXY(0, 0);
	private double											targetW				= 0.0;
	private double											latestW				= 0.0;
	private Vector2f										wpLatest				= Vector2f.fromXY(0, 0);
	private double											wpLatestW			= 0.0;
	/** [m/s] */
	private static final double						MAX					= 6.0;
	private static final double						MAX_W					= 10.0;
	
	private final List<IMotorInputPanelObserver>	observers			= new ArrayList<>();
	
	
	/** Constructor. */
	public MotorEnhancedInputPanel()
	{
		setMinimumSize(new Dimension(SIZE, SIZE + 55));
		
		final InputListener input = new InputListener();
		
		addMouseListener(input);
		addMouseMotionListener(input);
		addKeyListener(input);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	@SuppressWarnings("squid:S1192")
	public void paint(final Graphics g)
	{
		super.paint(g);
		
		final Graphics2D g2 = (Graphics2D) g;
		
		double red = 0.0;
		double green = 1.0;
		for (int i = 0; i < 20; i++)
		{
			final Color col = new Color((float) red, (float) green, 0);
			g2.setColor(col);
			g2.drawOval((SIZE / 2) - (i * 10), (SIZE / 2) - (i * 10), i * 20, i * 20);
			
			if (red < 1.0)
			{
				red += 0.1;
				if (red > 1.0)
				{
					red = 1.0;
				}
			} else
			{
				if (green > 0.0)
				{
					green -= 0.1;
					green = Math.max(green, 0);
				}
			}
		}
		
		g2.setColor(Color.GRAY);
		g2.drawLine(SIZE / 2, 0, SIZE / 2, SIZE);
		
		g2.setColor(Color.GRAY);
		g2.drawLine(0, SIZE / 2, SIZE, SIZE / 2);
		
		g2.setColor(Color.BLUE);
		g2.drawOval((int) (((SIZE / 2.0) + ((SIZE / MAX) * target.x())) - 4),
				(int) (((SIZE / 2.0) + ((SIZE / MAX) * -target.y())) - 4), 8, 8);
		
		g2.setColor(Color.RED);
		g2.fillOval((int) (((SIZE / 2.0) + ((SIZE / MAX) * latest.x())) - 3),
				(int) (((SIZE / 2.0) + ((SIZE / MAX) * -latest.y())) - 3), 6, 6);
		
		g2.setColor(Color.BLACK);
		g2.drawRect((int) (((SIZE / 2.0) + ((SIZE / MAX) * wpLatest.x())) - 3),
				(int) (((SIZE / 2.0) + ((SIZE / MAX) * -wpLatest.y())) - 3), 6, 6);
		
		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawArc(0, 0, SIZE - 1, SIZE - 1, 90, (int) ((targetW * 180) / MAX_W));
		
		g2.setColor(Color.RED);
		g2.drawArc(3, 3, SIZE - 6, SIZE - 6, 90, (int) ((latestW * 180) / MAX_W));
		
		g2.setColor(Color.BLACK);
		g2.drawArc(6, 6, SIZE - 11, SIZE - 11, 90, (int) ((wpLatestW * 180) / MAX_W));
		
		
		g2.setColor(Color.BLUE);
		g2.drawString("Target", 0, SIZE);
		g2.drawString(String.format(Locale.ENGLISH, "X: %1.2f", target.x()), 0, SIZE + 10);
		g2.drawString(String.format(Locale.ENGLISH, "Y: %1.2f", target.y()), 0, SIZE + 20);
		g2.drawString(String.format(Locale.ENGLISH, "W: %1.2f", targetW), 0, SIZE + 30);
		
		g2.setColor(Color.RED);
		g2.drawString("Latest", SIZE - 50, SIZE);
		g2.drawString(String.format(Locale.ENGLISH, "X: %1.2f", latest.x()), SIZE - 50, SIZE + 10);
		g2.drawString(String.format(Locale.ENGLISH, "Y: %1.2f", latest.y()), SIZE - 50, SIZE + 20);
		g2.drawString(String.format(Locale.ENGLISH, "W: %1.2f", latestW), SIZE - 50, SIZE + 30);
		
		g2.setColor(Color.BLACK);
		g2.drawString("WP", SIZE - 100, SIZE);
		g2.drawString(String.format(Locale.ENGLISH, "X: %1.2f", wpLatest.x()), SIZE - 100, SIZE + 10);
		g2.drawString(String.format(Locale.ENGLISH, "Y: %1.2f", wpLatest.y()), SIZE - 100, SIZE + 20);
		g2.drawString(String.format(Locale.ENGLISH, "W: %1.2f", wpLatestW), SIZE - 100, SIZE + 30);
		
		g2.setColor(Color.BLACK);
		g2.drawString("Modifiers: ALT, CTRL, SHIFT. Zero all: SPACE", 0, SIZE + 45);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IMotorInputPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IMotorInputPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param xy
	 */
	public void setLatestVelocity(final Vector2 xy)
	{
		latest = xy;
		
		repaint();
	}
	
	
	/**
	 * @param w
	 */
	public void setLatestAngularVelocity(final double w)
	{
		latestW = w;
		
		repaint();
	}
	
	
	/**
	 * @param xy
	 * @param w
	 */
	public void setLatestWPData(final Vector2f xy, final double w)
	{
		wpLatest = xy;
		wpLatestW = w;
		
		SwingUtilities.invokeLater(this::repaint);
	}
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	private class InputListener extends MouseAdapter implements KeyListener
	{
		private boolean	xOnly			= false;
		private boolean	yOnly			= false;
		private boolean	wModifier	= false;
		
		
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			mouseUpdate(e);
		}
		
		
		@Override
		public void mouseEntered(final MouseEvent e)
		{
			requestFocusInWindow();
		}
		
		
		@Override
		public void mouseDragged(final MouseEvent e)
		{
			mouseUpdate(e);
		}
		
		
		private void mouseUpdate(final MouseEvent e)
		{
			final int x = e.getX();
			final int y = e.getY();
			
			if ((y > SIZE) || (x > SIZE) || (y < 0) || (x < 0))
			{
				return;
			}
			
			double xf = ((x - (SIZE / 2.0)) * MAX) / SIZE;
			double yf = (-(y - (SIZE / 2.0)) * MAX) / SIZE;
			
			if (xOnly)
			{
				yf = 0;
			}
			
			if (yOnly)
			{
				xf = 0;
			}
			
			if (wModifier)
			{
				targetW = ((-xf * MAX_W) / MAX) * 2;
			} else
			{
				target = Vector2.fromXY(xf, yf);
			}
			notifyNewVelocity(target, targetW);
			
			repaint();
		}
		
		
		@Override
		public void keyPressed(final KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				target = Vector2.fromXY(0, 0);
				targetW = 0;
				
				notifyNewVelocity(target, targetW);
				repaint();
			}
			
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			{
				xOnly = true;
			}
			
			if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			{
				yOnly = true;
			}
			
			if (e.getKeyCode() == KeyEvent.VK_ALT)
			{
				wModifier = true;
			}
		}
		
		
		private void notifyNewVelocity(final Vector2 xy, final double w)
		{
			synchronized (observers)
			{
				for (final IMotorInputPanelObserver observer : observers)
				{
					observer.onSetSpeed(xy.x(), xy.y(), w);
				}
			}
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			{
				xOnly = false;
			}
			
			if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			{
				yOnly = false;
			}
			
			if (e.getKeyCode() == KeyEvent.VK_ALT)
			{
				wModifier = false;
			}
		}
		
		
		@Override
		public void keyTyped(final KeyEvent arg0)
		{
			// not used
		}
	}
}
