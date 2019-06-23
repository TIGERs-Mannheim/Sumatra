/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-robotControlUtility
 * Date: 19.11.2010
 * Authors: Clemens Teichmann <clteich@gmx.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import net.java.games.input.Component;
import net.java.games.input.Controller;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.NegativeAxis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.POVToButton;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local.PositiveAxis;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;


/**
 * @author Clemens
 * 
 */
public class ButtonSelectAction implements MouseListener, Runnable
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log			= Logger.getLogger(ButtonSelectAction.class.getName());
	
	private final Controller				controller;
	private Component[]						comps;
	private final JTextField				myTextField;
	private final AControllerPresenter	controllerPresenter;
	
	/** 50ms delay */
	private static final int				DELAY			= 50;
	/** Threshold */
	private static final double			THRESHOLD	= 0.3;
	
	
	// --- time for timeout ---
	private long								startSystemTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param selectedTextField
	 * @param newController
	 * @param newControllerPresenter
	 */
	public ButtonSelectAction(JTextField selectedTextField, Controller newController,
			AControllerPresenter newControllerPresenter)
	{
		super();
		myTextField = selectedTextField;
		controller = newController;
		controllerPresenter = newControllerPresenter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		log.info("Please press selected Button");
		Thread buttonSelectThread = new Thread(this, "ButtonSelect");
		buttonSelectThread.setName("ButtonSelectThread");
		// --- set start time ---
		startSystemTime = System.currentTimeMillis();
		// --- start Thread ---
		buttonSelectThread.start();
	}
	
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}
	
	
	@Override
	public void mouseExited(MouseEvent e)
	{
	}
	
	
	@Override
	public void mousePressed(MouseEvent e)
	{
	}
	
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
	}
	
	
	@Override
	public void run()
	{
		// --- check variable ---
		boolean componentPressed = false;
		// --- get all components of current controller ---
		comps = controller.getComponents();
		
		// --- run until thread interrupted by user or timeout ---
		while (!Thread.interrupted())
		{
			try
			{
				// --- wait a while ----
				Thread.sleep(DELAY);
			} catch (final InterruptedException e)
			{
				// --- TimeOut ---
				log.warn("ButtenSelect Timeout", e);
			}
			
			// --- update the controllers components ---
			controller.poll();
			
			// --- search trough all components ---
			for (Component comp : comps)
			{
				
				// --- get POV components ---
				if (comp.getIdentifier() == Component.Identifier.Axis.POV)
				{
					// --- if POV component pressed ---
					if (comp.getPollData() > THRESHOLD)
					{
						final float povDir = comp.getPollData();
						
						// --- handle POV as button ---
						final POVToButton pov = new POVToButton(comp, povDir);
						// --- add pov to config ---
						controllerPresenter.onNewSelectedButton(pov, myTextField.getName());
						// --- set textfield text ---
						myTextField.setText(pov.getIdentifier().toString());
						log.info("Component " + comp.getIdentifier().toString() + " selected");
						componentPressed = true;
					}
				}
				// --- get other components ---
				else
				{
					// --- get Buttons and positive Axis ---
					if (comp.getPollData() > THRESHOLD)
					{
						// --- positive Axis ---
						if (comp.isAnalog())
						{
							final PositiveAxis pAxis = new PositiveAxis(comp);
							controllerPresenter.onNewSelectedButton(pAxis, myTextField.getName());
							myTextField.setText(pAxis.getIdentifier().toString());
							log.info("Component " + pAxis.getIdentifier().toString() + " selected");
							componentPressed = true;
						}
						// --- Button ---
						else
						{
							controllerPresenter.onNewSelectedButton(comp, myTextField.getName());
							myTextField.setText(comp.getIdentifier().toString());
							log.info("Component " + comp.getIdentifier().toString() + " selected");
							componentPressed = true;
						}
					}
					
					// --- get negative Axis ---
					if (comp.getPollData() < -THRESHOLD)
					{
						final NegativeAxis nAxis = new NegativeAxis(comp);
						controllerPresenter.onNewSelectedButton(nAxis, myTextField.getName());
						myTextField.setText(nAxis.getIdentifier().toString());
						log.info("Component " + nAxis.getIdentifier().toString() + " selected");
						
						componentPressed = true;
					}
				}
			}
			// --- end thread if user pressed component or timeout ---
			if (componentPressed || ((System.currentTimeMillis() - startSystemTime) > 6000))
			{
				// --- reset MainFrame title ---
				Thread.currentThread().interrupt();
			}
		}
	}
}