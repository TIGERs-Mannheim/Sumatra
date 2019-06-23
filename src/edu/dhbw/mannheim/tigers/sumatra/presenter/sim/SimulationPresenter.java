/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.sim;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.Simulation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.ASimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPresenter implements ISumatraViewPresenter, ISimulationPanelObserver
{
	private static final Logger	log		= Logger.getLogger(SimulationPresenter.class.getName());
	private final SimulationPanel	simPanel	= new SimulationPanel();
	
	
	/**
	 * 
	 */
	public SimulationPresenter()
	{
		simPanel.addObserver(this);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				if (!Simulation.isSimulationRunning())
				{
					simPanel.setActive(false);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				simPanel.setActive(true);
				break;
			default:
				break;
		
		}
	}
	
	
	@Override
	public Component getComponent()
	{
		return simPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return simPanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	@Override
	public void onRunSimulation(final ASimulationScenario scenario, final float speedFactor)
	{
		if (speedFactor != -1)
		{
			scenario.getParams().setSpeedFactor(speedFactor);
		}
		Simulation.runSimulation(scenario);
	}
	
	
	@Override
	public void onStopSimulation()
	{
		Simulation.stopSimulation();
	}
	
	
	@Override
	public void onPauseSimulation()
	{
		Simulation sim;
		try
		{
			sim = (Simulation) SumatraModel.getInstance().getModule(Simulation.MODULE_ID);
			sim.togglePause();
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find Simulation module!", err);
			return;
		}
	}
}
