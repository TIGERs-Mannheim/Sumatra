/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.10.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor;

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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;


/**
 * Pretty fancy input component for robot XY control.
 * 
 * @author AndreR
 * 
 */
public class MotorEnhancedInputPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= 6741030318432376797L;
	
	private static final int							SIZE					= 400;
	
	private Vector2										target				= new Vector2(0, 0);
	private Vector2										latest				= new Vector2(0, 0);
	private float											targetW				= 0.0f;
	private float											latestW				= 0.0f;
	private Vector2f										wpLatest				= new Vector2f(0, 0);
	private float											wpLatestW			= 0.0f;
	/** [m/s] */
	private static final float							MAX					= 6.0f;
	private static final float							MAX_W					= 10.0f;
	
	private final List<IMotorEnhancedInputPanel>	observers			= new ArrayList<IMotorEnhancedInputPanel>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
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
	public void paint(Graphics g)
	{
		super.paint(g);
		
		final Graphics2D g2 = (Graphics2D) g;
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		float red = 0.0f;
		float green = 1.0f;
		for (int i = 0; i < 20; i++)
		{
			final Color col = new Color(red, green, 0);
			g2.setColor(col);
			g2.drawOval((SIZE / 2) - (i * 10), (SIZE / 2) - (i * 10), i * 20, i * 20);
			
			if (red < 1.0f)
			{
				red += 0.1;
				if (red > 1.0f)
				{
					red = 1.0f;
				}
			} else
			{
				if (green > 0.0f)
				{
					green -= 0.1f;
					if (green < 0.0f)
					{
						green = 0.0f;
					}
				}
			}
		}
		
		g2.setColor(Color.GRAY);
		g2.drawLine(SIZE / 2, 0, SIZE / 2, SIZE);
		
		g2.setColor(Color.GRAY);
		g2.drawLine(0, SIZE / 2, SIZE, SIZE / 2);
		
		g2.setColor(Color.BLUE);
		g2.drawOval((int) (((SIZE / 2) + ((SIZE / MAX) * target.x)) - 4),
				(int) (((SIZE / 2) + ((SIZE / MAX) * -target.y)) - 4), 8, 8);
		
		g2.setColor(Color.RED);
		g2.fillOval((int) (((SIZE / 2) + ((SIZE / MAX) * latest.x)) - 3),
				(int) (((SIZE / 2) + ((SIZE / MAX) * -latest.y)) - 3), 6, 6);
		
		g2.setColor(Color.BLACK);
		g2.drawRect((int) (((SIZE / 2) + ((SIZE / MAX) * wpLatest.x())) - 3),
				(int) (((SIZE / 2) + ((SIZE / MAX) * -wpLatest.y())) - 3), 6, 6);
		
		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawArc(0, 0, SIZE - 1, SIZE - 1, 90, (int) ((targetW * 180) / MAX_W));
		
		g2.setColor(Color.RED);
		g2.drawArc(3, 3, SIZE - 6, SIZE - 6, 90, (int) ((latestW * 180) / MAX_W));
		
		g2.setColor(Color.BLACK);
		g2.drawArc(6, 6, SIZE - 11, SIZE - 11, 90, (int) ((wpLatestW * 180) / MAX_W));
		
		
		g2.setColor(Color.BLUE);
		g2.drawString("Target", 0, SIZE);
		g2.drawString(String.format(Locale.ENGLISH, "X: %1.2f", target.x), 0, SIZE + 10);
		g2.drawString(String.format(Locale.ENGLISH, "Y: %1.2f", target.y), 0, SIZE + 20);
		g2.drawString(String.format(Locale.ENGLISH, "W: %1.2f", targetW), 0, SIZE + 30);
		
		g2.setColor(Color.RED);
		g2.drawString("Latest", SIZE - 50, SIZE);
		g2.drawString(String.format(Locale.ENGLISH, "X: %1.2f", latest.x), SIZE - 50, SIZE + 10);
		g2.drawString(String.format(Locale.ENGLISH, "Y: %1.2f", latest.y), SIZE - 50, SIZE + 20);
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
	public void addObserver(IMotorEnhancedInputPanel observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IMotorEnhancedInputPanel observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewVelocity(Vector2 xy)
	{
		synchronized (observers)
		{
			for (final IMotorEnhancedInputPanel observer : observers)
			{
				observer.onNewVelocity(xy);
			}
		}
	}
	
	
	private void notifyNewAngularVelocity(float w)
	{
		synchronized (observers)
		{
			for (final IMotorEnhancedInputPanel observer : observers)
			{
				observer.onNewAngularVelocity(w);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param xy
	 */
	public void setLatestVelocity(Vector2 xy)
	{
		latest = xy;
		
		repaint();
	}
	
	
	/**
	 * @param w
	 */
	public void setLatestAngularVelocity(float w)
	{
		latestW = w;
		
		repaint();
	}
	
	
	/**
	 * @param xy
	 * @param w
	 */
	public void setLatestWPData(Vector2f xy, float w)
	{
		wpLatest = xy;
		wpLatestW = w;
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
		public void mouseClicked(MouseEvent e)
		{
			mouseUpdate(e);
		}
		
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			requestFocusInWindow();
		}
		
		
		@Override
		public void mousePressed(MouseEvent e)
		{
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			mouseUpdate(e);
		}
		
		
		private void mouseUpdate(MouseEvent e)
		{
			final int x = e.getX();
			final int y = e.getY();
			
			if ((y > SIZE) || (x > SIZE) || (y < 0) || (x < 0))
			{
				return;
			}
			
			float xf = ((x - (SIZE / 2.0f)) * MAX) / SIZE;
			float yf = (-(y - (SIZE / 2.0f)) * MAX) / SIZE;
			
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
				notifyNewAngularVelocity(targetW);
			} else
			{
				target = new Vector2(xf, yf);
				notifyNewVelocity(target);
			}
			
			repaint();
		}
		
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				target = new Vector2(0, 0);
				targetW = 0;
				notifyNewVelocity(target);
				notifyNewAngularVelocity(targetW);
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
		
		
		@Override
		public void keyReleased(KeyEvent e)
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
		public void keyTyped(KeyEvent arg0)
		{
		}
	}
}
