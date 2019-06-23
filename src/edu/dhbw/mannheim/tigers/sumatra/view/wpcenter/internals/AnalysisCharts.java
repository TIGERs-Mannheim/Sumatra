/*
 * ***i******************************************************
 * Copysiright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.10.2010
 * Author(s): Administrator
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals;

import info.monitorenter.gui.chart.ITrace2D;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;


/**
 * GUI Panel for the Analysis chart.
 * 
 */
public class AnalysisCharts extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// x: -3678.5364 - 3678.5044 / y: -2678.5173 - 2678.505
	
	private static final long							serialVersionUID	= 20111895766433L;
	
	private final List<IAnalysisChartsObserver>	observers			= new ArrayList<IAnalysisChartsObserver>();
	
	private final Graph									xPos;
	private final Graph									yPos;
	
	private final Graph									xErr;
	private final Graph									yErr;
	
	private final Graph									aPos;
	private final Graph									aErr;
	
	private final Graph									absErr;
	private final Graph									ballVel;
	
	private final ITrace2D								yPosReal;
	private final ITrace2D								yPosPred;
	private final ITrace2D								xPosReal;
	private final ITrace2D								xPosPred;
	
	private final ITrace2D								aPosReal;
	private final ITrace2D								aPosPred;
	private final ITrace2D								aPosReal2;
	private final ITrace2D								aPosPred2;
	
	private final ITrace2D								xErrGraph;
	private final ITrace2D								yErrGraph;
	private final ITrace2D								aErrGraph;
	
	private final ITrace2D								xErrNull;
	private final ITrace2D								yErrNull;
	private final ITrace2D								aErrNull;
	
	private final ITrace2D								absErrGraph;
	private final ITrace2D								ballVelGraph;
	private final ITrace2D								ballVelGraphRef;
	private final List<ITrace2D>						ballVelGraphMarks	= new ArrayList<ITrace2D>(10);
	
	private final JTextField							object_id;
	private final JTextField							fpsUp;
	private final JTextField							fpsIn;
	private final JTextField							fpsOut;
	private final JButton								idBut;
	private final JButton								xBut;
	private final JButton								yBut;
	private final JButton								aBut;
	private final JButton								absBut;
	private final JButton								ballVelBut;
	
	private final JPanel									graphs1;
	private final JPanel									graphs2;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AnalysisCharts()
	{
		xPos = new Graph(-3750, 3750, "XPos [mm]");
		yPos = new Graph(-2750, 2750, "YPos [mm]");
		aPos = new Graph(-0.5f, 7, "aPos [Rad]");
		xErr = new Graph(-250, 250, "XErr [mm]");
		yErr = new Graph(-250, 250, "YErr [mm]");
		aErr = new Graph(-2, 2, "aErr [Rad]");
		absErr = new Graph(0, 250, "absolute Error [mm]");
		ballVel = new Graph(0, 15, "vel [m/s]");
		
		
		final Color real = Color.BLUE;
		final Color pred = Color.GREEN;
		final Color erro = Color.RED;
		final Color zero = Color.BLACK;
		
		xPosReal = xPos.addTrace(real, "real x");
		xPosPred = xPos.addTrace(pred, "pred x");
		yPosReal = yPos.addTrace(real, "real y");
		yPosPred = yPos.addTrace(pred, "pred y");
		aPosReal = aPos.addTrace(real, "real alpha");
		aPosPred = aPos.addTrace(pred, "pred alpha");
		aPosReal2 = aPos.addTrace(real, "");
		aPosPred2 = aPos.addTrace(pred, "");
		
		
		xErrNull = xErr.addTrace(zero, "zero");
		xErrGraph = xErr.addTrace(erro, "error x");
		
		
		yErrNull = yErr.addTrace(zero, "zero");
		yErrGraph = yErr.addTrace(erro, "error y");
		
		aErrNull = aErr.addTrace(zero, "zero");
		aErrGraph = aErr.addTrace(erro, "error alpha");
		
		absErrGraph = absErr.addTrace(erro, "error absolute");
		ballVelGraph = ballVel.addTrace(pred, "ballSpeed");
		ballVelGraphRef = ballVel.addTrace(erro, "ref");
		
		for (int i = 0; i < 7; i++)
		{
			ballVelGraphMarks.add(ballVel.addTrace(zero, ""));
		}
		
		
		object_id = new JTextField("0");
		object_id.addActionListener(new SetId());
		fpsUp = new JTextField();
		fpsIn = new JTextField();
		fpsOut = new JTextField();
		idBut = new JButton("Apply");
		xBut = new JButton("X");
		yBut = new JButton("Y");
		aBut = new JButton("Alpha");
		absBut = new JButton("abs");
		ballVelBut = new JButton("ballvel");
		
		
		idBut.addActionListener(new SetId());
		xBut.addActionListener(new ShowX());
		yBut.addActionListener(new ShowY());
		aBut.addActionListener(new ShowA());
		absBut.addActionListener(new ShowAbs());
		ballVelBut.addActionListener(new BallVel());
		
		graphs1 = new JPanel();
		graphs1.setLayout(new MigLayout("fill, insets 0", "[grow]1", ""));
		
		graphs2 = new JPanel();
		graphs2.setLayout(new MigLayout("fill, insets 0", "[grow]1", "[grow]"));
		
		
		setLayout(new MigLayout("fill, insets 0, wrap 1", "[grow]1", "[min!]1[grow]"));
		add(new JLabel("ID:"), "split 10, span");
		add(object_id, "width 50");
		add(idBut, "");
		add(xBut, "");
		add(yBut, "");
		add(aBut, "");
		add(absBut, "");
		add(fpsUp, "grow");
		add(fpsIn, "grow");
		add(fpsOut, "grow");
		
		add(graphs1, "grow");
		add(graphs2, "grow");
		graphs1.add(xPos, "grow");
		graphs1.add(yPos, "grow");
		graphs1.add(aPos, "grow");
		graphs2.add(xErr, "grow");
		graphs2.add(yErr, "grow");
		graphs2.add(aErr, "grow");
		add(absErr, "grow");
		add(ballVel, "grow");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void xRemoveAllPoints()
	{
		xPosReal.removeAllPoints();
		xPosPred.removeAllPoints();
		xErrGraph.removeAllPoints();
		xErrNull.removeAllPoints();
	}
	
	
	private void yRemoveAllPoints()
	{
		yPosReal.removeAllPoints();
		yPosPred.removeAllPoints();
		yErrGraph.removeAllPoints();
		yErrNull.removeAllPoints();
	}
	
	
	private void aRemoveAllPoints()
	{
		aPosReal.removeAllPoints();
		aPosPred.removeAllPoints();
		aPosReal2.removeAllPoints();
		aPosPred2.removeAllPoints();
		aErrGraph.removeAllPoints();
	}
	
	
	private void absRemoveAllPoints()
	{
		absErrGraph.removeAllPoints();
	}
	
	
	/**
	 *
	 */
	public void clearChart()
	{
		xRemoveAllPoints();
		yRemoveAllPoints();
		aRemoveAllPoints();
		absRemoveAllPoints();
	}
	
	
	/**
	 * 
	 * @param time
	 * @param xError
	 * @param yError
	 * @param aError
	 * @param absError
	 */
	public void updateErrors(long time, float xError, float yError, float aError, float absError)
	{
		if (xPos.isVisible())
		{
			
			xErr.updateTrace(xErrGraph, time, xError);
			xErr.updateTrace(xErrNull, time, 0.0f);
		}
		
		if (yPos.isVisible())
		{
			
			yErr.updateTrace(yErrGraph, time, yError);
			yErr.updateTrace(yErrNull, time, 0.0f);
		}
		
		if (aPos.isVisible())
		{
			
			aErr.updateTrace(aErrGraph, time, aError);
			aErr.updateTrace(aErrNull, time, 0.0f);
		}
		
		if (absErr.isVisible())
		{
			absErr.updateTrace(absErrGraph, time, absError);
		}
		
	}
	
	
	/**
	 * 
	 * @param time
	 * @param xReal
	 * @param yReal
	 * @param aReal
	 */
	public void updateCF(long time, float xReal, float yReal, float aReal)
	{
		
		if (xPos.isVisible())
		{
			xPos.updateTrace(xPosReal, time, xReal);
			
		}
		
		if (yPos.isVisible())
		{
			yPos.updateTrace(yPosReal, time, yReal);
			
		}
		
		if (aPos.isVisible())
		{
			aPos.updateTrace(aPosReal, time, aReal);
			aPos.updateTrace(aPosReal2, time, aReal + AngleMath.PI_TWO);
			
		}
	}
	
	
	/**
	 * 
	 * @param time
	 * @param xPred
	 * @param yPred
	 * @param aPred
	 */
	public void updateWF(long time, float xPred, float yPred, float aPred)
	{
		
		if (xPos.isVisible())
		{
			xPos.updateTrace(xPosPred, time, xPred);
		}
		
		if (yPos.isVisible())
		{
			yPos.updateTrace(yPosPred, time, yPred);
		}
		
		if (aPos.isVisible())
		{
			aPos.updateTrace(aPosPred, time, aPred);
			aPos.updateTrace(aPosPred2, time, aPred + AngleMath.PI_TWO);
			
			aErr.updateTrace(aErrNull, time, 0.0f);
		}
	}
	
	
	/**
	 * 
	 * @param time
	 * @param vel
	 */
	public void updateBallVel(long time, float vel)
	{
		if (ballVel.isVisible())
		{
			ballVel.updateTrace(ballVelGraph, time, vel);
			ballVel.updateTrace(ballVelGraphRef, time, 8f);
			float mark = 1;
			for (ITrace2D trace : ballVelGraphMarks)
			{
				ballVel.updateTrace(trace, time, mark);
				mark++;
			}
		}
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	private void changeVisible(boolean visible)
	{
		
		if (visible && !graphs1.isVisible() && (aPos.isVisible() || yPos.isVisible() || xPos.isVisible()))
		{
			graphs1.setVisible(true);
			graphs2.setVisible(true);
			add(graphs1, "grow");
			add(graphs2, "grow");
		}
		
		if (!visible && !aPos.isVisible() && !yPos.isVisible() && !xPos.isVisible())
		{
			graphs1.setVisible(false);
			graphs2.setVisible(false);
			remove(graphs1);
			remove(graphs2);
		}
		
		
		updateUI();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public boolean isVisibleX()
	{
		return xPos.isVisible();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisibleY()
	{
		return yPos.isVisible();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisibleA()
	{
		return aPos.isVisible();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisibleAbs()
	{
		return absErr.isVisible();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisibleBallVel()
	{
		return ballVel.isVisible();
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisibleX(boolean visible)
	{
		xPos.setVisible(visible);
		xErr.setVisible(visible);
		if (!visible)
		{
			graphs1.remove(xPos);
			graphs2.remove(xErr);
			xRemoveAllPoints();
		} else
		{
			graphs1.add(xPos, "grow");
			graphs2.add(xErr, "grow");
		}
		changeVisible(visible);
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisibleY(boolean visible)
	{
		yPos.setVisible(visible);
		yErr.setVisible(visible);
		if (!visible)
		{
			graphs1.remove(yPos);
			graphs2.remove(yErr);
			yRemoveAllPoints();
		} else
		{
			graphs1.add(yPos, "grow");
			graphs2.add(yErr, "grow");
		}
		changeVisible(visible);
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisibleA(boolean visible)
	{
		aPos.setVisible(visible);
		aErr.setVisible(visible);
		if (!visible)
		{
			graphs1.remove(aPos);
			graphs2.remove(aErr);
			aRemoveAllPoints();
		} else
		{
			graphs1.add(aPos, "grow");
			graphs2.add(aErr, "grow");
		}
		changeVisible(visible);
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisibleAbs(boolean visible)
	{
		absErr.setVisible(visible);
		if (!visible)
		{
			remove(absErr);
			absRemoveAllPoints();
		} else
		{
			add(absErr, "grow");
		}
		changeVisible(visible);
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisibleBallVel(boolean visible)
	{
		ballVel.setVisible(visible);
		if (!visible)
		{
			remove(ballVel);
		} else
		{
			add(ballVel, "grow");
		}
	}
	
	
	/**
	 * 
	 * @param fps
	 */
	public void setFpsUp(float fps)
	{
		fpsUp.setText(fps + " ups");
	}
	
	
	/**
	 * 
	 * @param fps
	 */
	public void setFpsIn(float fps)
	{
		fpsIn.setText(fps + " fps In");
	}
	
	
	/**
	 * 
	 * @param fps
	 */
	public void setFpsOut(float fps)
	{
		fpsOut.setText(fps + " fps Out");
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void addObserver(IAnalysisChartsObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(IAnalysisChartsObserver o)
	{
		observers.remove(o);
	}
	
	
	private void notifyShowX()
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onShowX();
			}
		}
	}
	
	
	private void notifyShowY()
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onShowY();
			}
		}
	}
	
	
	private void notifyShowA()
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onShowA();
			}
		}
	}
	
	
	private void notifyShowAbs()
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onShowAbs();
			}
		}
	}
	
	
	private void notifySetId(int id)
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onSetId(id);
			}
		}
	}
	
	
	private void notifyShowBallVel()
	{
		synchronized (observers)
		{
			for (final IAnalysisChartsObserver observer : observers)
			{
				observer.onShowBallVel();
			}
		}
	}
	
	
	protected class ShowX implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyShowX();
		}
	}
	
	protected class ShowY implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyShowY();
		}
	}
	
	protected class ShowA implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyShowA();
		}
	}
	
	protected class ShowAbs implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyShowAbs();
		}
	}
	
	protected class SetId implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				notifySetId(Integer.parseInt(object_id.getText()));
			} catch (final Exception ex)
			{
				object_id.setText("?");
			}
		}
	}
	
	protected class BallVel implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyShowBallVel();
		}
	}
}
