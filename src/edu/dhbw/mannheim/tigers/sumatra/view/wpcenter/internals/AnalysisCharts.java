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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;

import net.miginfocom.swing.MigLayout;


/**
 * TODO Administrator, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Administrator
 * 
 */
public class AnalysisCharts extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// x: -3678.5364 - 3678.5044 / y: -2678.5173 - 2678.505 
	
	private static final long	serialVersionUID	= 20111895766433L;
	
	private List<IAnalysisChartsObserver> observers = new ArrayList<IAnalysisChartsObserver>();
	
	private Graph xPos;
	private Graph yPos;
	
	private Graph xErr;
	private Graph yErr;
	
	private Graph aPos;
	private Graph aErr;

	
	private Graph absErr;
	
	private ITrace2D yPosReal;
	private ITrace2D yPosPred;
	private ITrace2D xPosReal;
	private ITrace2D xPosPred;
	
	private ITrace2D aPosReal;
	private ITrace2D aPosPred;
	private ITrace2D aPosReal2;
	private ITrace2D aPosPred2;
	
	private ITrace2D xErrGraph;
	private ITrace2D yErrGraph;
	private ITrace2D aErrGraph;
	
	private ITrace2D xErrNull;
	private ITrace2D yErrNull;
	private ITrace2D aErrNull;
	
	private ITrace2D absErrGraph;
	
	private JTextField object_id;
	private JTextField fpsUp;
	private JTextField fpsIn;
	private JTextField fpsOut;
	private JButton   idBut;
	private JButton   xBut;
	private JButton   yBut;
	private JButton   aBut;
	private JButton   absBut;
	
	private JPanel    graphs1;
	private JPanel    graphs2;
	
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public AnalysisCharts()
	{
		xPos     = new Graph(-3750, 3750, "XPos [mm]");
		yPos     = new Graph(-2750, 2750, "YPos [mm]");
		aPos     = new Graph(-0.5f,    7, "aPos [Rad]");
		xErr     = new Graph(-250,   250, "XErr [mm]");
		yErr     = new Graph(-250,   250, "YErr [mm]");
		aErr     = new Graph(-2,       2, "aErr [Rad]");
		absErr   = new Graph(0,      250, "absolute Error [mm]");
		
		
		Color real = Color.BLUE;
		Color pred = Color.GREEN;
		Color erro = Color.RED;
		Color zero = Color.BLACK;
		
		xPosReal  = xPos.addTrace(real, "real x");
		xPosPred  = xPos.addTrace(pred, "pred x");
		yPosReal  = yPos.addTrace(real, "real y");
		yPosPred  = yPos.addTrace(pred, "pred y");
		aPosReal  = aPos.addTrace(real, "real alpha");
		aPosPred  = aPos.addTrace(pred, "pred alpha");
		aPosReal2 = aPos.addTrace(real, "");
		aPosPred2 = aPos.addTrace(pred, "");
		
		
		xErrNull  = xErr.addTrace(zero, "zero");
		xErrGraph = xErr.addTrace(erro, "error x");
		
		
		yErrNull  = yErr.addTrace(zero, "zero");
		yErrGraph = yErr.addTrace(erro, "error y");
		
		aErrNull  = aErr.addTrace(zero, "zero");
		aErrGraph = aErr.addTrace(erro, "error alpha");
		
		absErrGraph = absErr.addTrace(erro, "error absolute");
		
	
		
		object_id = new JTextField("0");
		object_id.addActionListener(new SetId());
		fpsUp = new JTextField();
		fpsIn = new JTextField();
		fpsOut = new JTextField();
		idBut = new JButton("Apply");
		xBut =  new JButton("X");
		yBut =  new JButton("Y");
		aBut =  new JButton("Alpha");
		absBut= new JButton("abs");
		
		
		idBut.addActionListener(new SetId());
		xBut.addActionListener(new ShowX());
		yBut.addActionListener(new ShowY());
		aBut.addActionListener(new ShowA());
		absBut.addActionListener(new ShowAbs());
		
		graphs1 = new JPanel();
		graphs1.setLayout(new MigLayout("fill, insets 0","[grow]1",""));
		
		graphs2 = new JPanel();
		graphs2.setLayout(new MigLayout("fill, insets 0","[grow]1","[grow]"));
		
	
		
		setLayout(new MigLayout("fill, insets 0, wrap 1","[grow]1","[min!]1[grow]"));
		add(new JLabel("ID:"), "split 10, span");
		add(object_id, "width 50");
		add(idBut,     "");
		add(xBut,      "");
		add(yBut,      "");
		add(aBut,      "");
		add(absBut,    "");
		add(fpsUp,    "grow");
		add(fpsIn,    "grow");
		add(fpsOut,    "grow");
		
		add(graphs1,    "grow");
		add(graphs2,    "grow");
		graphs1.add(xPos,      "grow");
		graphs1.add(yPos,      "grow");
		graphs1.add(aPos,      "grow");
		graphs2.add(xErr,      "grow");
		graphs2.add(yErr,      "grow");
		graphs2.add(aErr,      "grow");
		add(absErr,    "grow");
//		add(xPos,      "grow, cell 0 1");
//		add(yPos,      "grow, cell 1 1");
//		add(aPos,      "grow, cell 2 1");
//		add(xErr,      "grow, cell 0 2");
//		add(yErr,      "grow, cell 1 2");
//		add(aErr,      "grow, cell 2 2");
//		add(absErr,    "grow, cell 0 3 3 3");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void xRemoveAllPoints()
	{
		xPosReal   .removeAllPoints(); 
		xPosPred   .removeAllPoints();
		xErrGraph  .removeAllPoints();
		xErrNull   .removeAllPoints();
	}
	
	private void yRemoveAllPoints()
	{
		yPosReal   .removeAllPoints(); 
		yPosPred   .removeAllPoints();
		yErrGraph  .removeAllPoints();
		yErrNull   .removeAllPoints();
	}
	
	private void aRemoveAllPoints()
	{
		aPosReal   .removeAllPoints();
		aPosPred   .removeAllPoints();
		aPosReal2  .removeAllPoints();
		aPosPred2  .removeAllPoints();
		aErrGraph  .removeAllPoints();
	}
	
	private void absRemoveAllPoints()
	{
		absErrGraph.removeAllPoints();	
	}
	
	public void clearChart()
	{ 
		xRemoveAllPoints();
		yRemoveAllPoints();
		aRemoveAllPoints();
		absRemoveAllPoints();
	}
	
	public void updateErrors(long time,
			float xError, float yError, float aError,
			float absError)
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
	
	public void updateCF(long time,	float xReal,  float yReal,  float aReal)
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
			aPos.updateTrace(aPosReal,  time, aReal);
			aPos.updateTrace(aPosReal2, time, aReal+ AIMath.PI_TWO);

		}
	}

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
			aPos.updateTrace(aPosPred,  time, aPred);
			aPos.updateTrace(aPosPred2, time, aPred+ AIMath.PI_TWO);
		
			aErr.updateTrace(aErrNull, time, 0.0f);
		}
	}

	
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
	
	public boolean isVisibleX()
	{
		return xPos.isVisible();
	}
	
	public boolean isVisibleY()
	{
		return yPos.isVisible();
	}
	
	public boolean isVisibleA()
	{
		return aPos.isVisible();
	}
	
	public boolean isVisibleAbs()
	{
		return absErr.isVisible();
	}
	
	public void setVisibleX(boolean visible)
	{
		xPos.setVisible(visible);
		xErr.setVisible(visible);
		if (!visible)
		{		
			graphs1.remove(xPos);
			graphs2.remove(xErr);
			xRemoveAllPoints();
		}
		else
		{
			graphs1.add(xPos,      "grow");
			graphs2.add(xErr,      "grow");
		}
		changeVisible(visible);
	}
	
	public void setVisibleY(boolean visible)
	{
		yPos.setVisible(visible);
		yErr.setVisible(visible);
		if (!visible)
		{		
			graphs1.remove(yPos);
			graphs2.remove(yErr);
			yRemoveAllPoints();
		}
		else
		{
			graphs1.add(yPos,      "grow");
			graphs2.add(yErr,      "grow");
		}
		changeVisible(visible);
	}
	
	public void setVisibleA(boolean visible)
	{
		aPos.setVisible(visible);
		aErr.setVisible(visible);
		if (!visible)
		{		
			graphs1.remove(aPos);
			graphs2.remove(aErr);
			aRemoveAllPoints();
		}
		else
		{
			graphs1.add(aPos,      "grow");
			graphs2.add(aErr,      "grow");
		}
		changeVisible(visible);
	}
	
	public void setVisibleAbs(boolean visible)
	{
		absErr.setVisible(visible);
		if (!visible)
		{		
			remove(absErr);
			absRemoveAllPoints();
		}
		else
		{
			add(absErr,      "grow");
		}
		changeVisible(visible);
	}
	
	public void setFpsUp(float fps)
	{
		fpsUp.setText(fps+" ups");
	}
	
	public void setFpsIn(float fps)
	{
		fpsIn.setText(fps+" fps In");
	}
	
	public void setFpsOut(float fps)
	{
		fpsOut.setText(fps+" fps Out");
	}
	
	
	public void addObserver(IAnalysisChartsObserver o)
	{
		observers.add(o);
	}
	
	public void removeObserver(IAnalysisChartsObserver o)
	{
		observers.remove(o);
	}
	
	private void notifyShowX()
	{
		synchronized(observers)
		{
			for (IAnalysisChartsObserver observer : observers)
			{
				observer.onShowX();
			}
		}
	}
	
	private void notifyShowY()
	{
		synchronized(observers)
		{
			for (IAnalysisChartsObserver observer : observers)
			{
				observer.onShowY();
			}
		}
	}
	
	private void notifyShowA()
	{
		synchronized(observers)
		{
			for (IAnalysisChartsObserver observer : observers)
			{
				observer.onShowA();
			}
		}
	}
	
	private void notifyShowAbs()
	{
		synchronized(observers)
		{
			for (IAnalysisChartsObserver observer : observers)
			{
				observer.onShowAbs();
			}
		}
	}
	
	private void notifySetId(int id)
	{
		synchronized(observers)
		{
			for (IAnalysisChartsObserver observer : observers)
			{
				observer.onSetId(id);
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
				notifySetId( Integer.parseInt(object_id.getText()));
			}
			catch (Exception ex)
			{
				object_id.setText("?");
			}
		}
	}
}
