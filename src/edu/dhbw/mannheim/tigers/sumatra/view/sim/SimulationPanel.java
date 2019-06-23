/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.sim;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.ASimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.ESimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.InstanceablePanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPanel extends JPanel implements ISumatraView
{
	/**  */
	private static final long							serialVersionUID	= 4936408016928626573L;
	private static final Logger						log					= Logger.getLogger(SimulationPanel.class.getName());
	
	private final InstanceablePanel					instPanel;
	private final JTextField							txtSpeedFactor;
	private final JButton								btnStopSim;
	private final JButton								btnPauseSim;
	
	
	private final List<ISimulationPanelObserver>	observers			= new CopyOnWriteArrayList<ISimulationPanelObserver>();
	
	
	/**
	 * 
	 */
	public SimulationPanel()
	{
		
		instPanel = new InstanceablePanel(ESimulationScenario.values());
		instPanel.addObserver(new ScenarioObserver());
		add(instPanel);
		
		txtSpeedFactor = new JTextField("1", 4);
		add(txtSpeedFactor);
		
		btnStopSim = new JButton("Stop simulation");
		btnStopSim.addActionListener(new StopSimulationAction());
		add(btnStopSim);
		
		btnPauseSim = new JButton("Pause simulation");
		btnPauseSim.addActionListener(new PauseSimulationAction());
		add(btnPauseSim);
	}
	
	
	/**
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		EventQueue.invokeLater(() -> instPanel.setEnabled(active));
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISimulationPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISimulationPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>();
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public interface ISimulationPanelObserver
	{
		/**
		 * @param scenario
		 * @param speedFactor
		 */
		void onRunSimulation(ASimulationScenario scenario, float speedFactor);
		
		
		/**
		 * 
		 */
		void onStopSimulation();
		
		
		/**
		 * 
		 */
		void onPauseSimulation();
	}
	
	private class StopSimulationAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onStopSimulation();
			}
		}
	}
	
	private class PauseSimulationAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onPauseSimulation();
			}
		}
	}
	
	private class ScenarioObserver implements IInstanceableObserver
	{
		
		@Override
		public void onNewInstance(final Object object)
		{
			ASimulationScenario scenario = (ASimulationScenario) object;
			float speedFactor = 1;
			try
			{
				speedFactor = Float.parseFloat(txtSpeedFactor.getText());
			} catch (NumberFormatException err)
			{
				log.error("Could not parse speedFactor!");
			}
			for (ISimulationPanelObserver o : observers)
			{
				o.onRunSimulation(scenario, speedFactor);
			}
		}
	}
}
