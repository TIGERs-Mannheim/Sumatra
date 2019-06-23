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
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.SimulationHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario.ASimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SumatraBot;
import edu.tigers.sumatra.sim.SumatraCam;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPresenter extends ASumatraViewPresenter implements ISimulationPanelObserver
{
	@SuppressWarnings("unused")
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
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
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
	public void onRunSimulation(final ASimulationScenario scenario)
	{
		SimulationHelper.setupScenario(scenario);
	}
	
	
	private SumatraCam getSumatraCam()
	{
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			if (cam instanceof SumatraCam)
			{
				SumatraCam sCam = (SumatraCam) cam;
				return sCam;
			}
		} catch (ModuleNotFoundException e)
		{
		}
		return null;
	}
	
	
	@Override
	public void onPauseSimulation()
	{
		SumatraCam cam = getSumatraCam();
		cam.pause();
	}
	
	
	@Override
	public void onResumeSimulation()
	{
		SumatraCam cam = getSumatraCam();
		cam.play();
	}
	
	
	@Override
	public void onChangeSpeed(final double speed)
	{
		SumatraCam cam = getSumatraCam();
		cam.setSimSpeed(speed);
	}
	
	
	@Override
	public void onStep(final int i)
	{
		SumatraCam cam = getSumatraCam();
		cam.step(i);
	}
	
	
	@Override
	public void onReset()
	{
		SimulationHelper.resetSimulation();
	}
	
	
	@Override
	public void onLoadSnapshot(final String path)
	{
		if (getSumatraCam() != null)
		{
			try
			{
				Snapshot snapshot = Snapshot.load(path);
				SumatraCam cam = getSumatraCam();
				
				cam.getBall().setPos(new Vector3(snapshot.getBall().getPos(), 0));
				cam.getBall().setVel(new Vector3(snapshot.getBall().getVel(), 0));
				
				for (SumatraBot sBot : cam.getBots())
				{
					SnapObject oBot = snapshot.getBots().get(sBot.getBotId());
					if (oBot != null)
					{
						sBot.setPos(new Vector3(oBot.getPos(), 0));
						sBot.setVel(new Vector3(oBot.getVel(), 0));
					}
				}
				
			} catch (ParseException | IOException e)
			{
				log.error("", e);
			}
		}
	}
}
