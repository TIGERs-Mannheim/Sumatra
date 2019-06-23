/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.sim;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.snapshot.SnapshotController;
import edu.tigers.sumatra.view.sim.SimulationPanel;
import edu.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPresenter extends ASumatraViewPresenter implements ISimulationPanelObserver, IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimulationPresenter.class.getName());
	private final SimulationPanel simPanel = new SimulationPanel();
	private final SnapshotController snapshotController = new SnapshotController(simPanel);
	private SumatraSimulator simulator;
	
	
	private void activate()
	{
		simPanel.reset();
		
		try
		{
			AWorldPredictor worldPredictor = SumatraModel.getInstance().getModule(
					AWorldPredictor.class);
			worldPredictor.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
		}
		
		try
		{
			AVisionFilter vf = SumatraModel.getInstance().getModule(AVisionFilter.class);
			if (vf instanceof SumatraSimulator)
			{
				simulator = (SumatraSimulator) vf;
				simPanel.addObserver(this);
			}
		} catch (ModuleNotFoundException e)
		{
			log.error("cam module not found.", e);
		}
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				activate();
				
				break;
			case RESOLVED:
				deactivate();
				
				break;
			case NOT_LOADED:
			default:
				break;
			
		}
	}
	
	
	private void deactivate()
	{
		simPanel.removeObserver(this);
		simPanel.reset();
		
		try
		{
			AWorldPredictor worldPredictor = SumatraModel.getInstance().getModule(
					AWorldPredictor.class);
			worldPredictor.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
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
	public void onRunSimulation(final SimulationParameters params)
	{
		try
		{
			SimulationHelper.loadSimulation(params);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not setup scenario.", e);
		}
	}
	
	
	@Override
	public void onPauseSimulation()
	{
		simulator.pause();
	}
	
	
	@Override
	public void onResumeSimulation()
	{
		simulator.play();
	}
	
	
	@Override
	public void onChangeSpeed(final double speed)
	{
		simulator.setSimSpeed(speed);
	}
	
	
	@Override
	public void onStep(final int i)
	{
		simulator.step();
	}
	
	
	@Override
	public void onStepBwd(final int i)
	{
		simulator.stepBack();
	}
	
	
	@Override
	public void onReset()
	{
		SimulationHelper.resetSimulation();
	}
	
	
	@Override
	public void onSaveSnapshot()
	{
		snapshotController.onSnapshot();
	}
	
	
	@Override
	public void onPasteSnapshot()
	{
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) &&
				contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText)
		{
			try
			{
				String snapJson = (String) contents.getTransferData(DataFlavor.stringFlavor);
				Snapshot snapshot = Snapshot.fromJSONString(snapJson);
				SimulationParameters params = new SimulationParameters(snapshot);
				SimulationHelper.loadSimulation(params);
			} catch (ParseException e)
			{
				log.error("Could not parse snap file.", e);
			} catch (UnsupportedFlavorException | IOException | ModuleNotFoundException ex)
			{
				log.error("Could not get content from clipboard.", ex);
			}
		}
	}
	
	
	@Override
	public void onCopySnapshot()
	{
		snapshotController.onCopySnapshot();
	}
	
	
	@Override
	public void onSyncWithAi(final boolean sync)
	{
		SimulationHelper.setProcessAllWorldFrames(sync);
	}
	
	
	@Override
	public void onLoadSnapshot(final String path)
	{
		try
		{
			Snapshot snapshot = Snapshot.loadFromFile(path);
			SimulationParameters params = new SimulationParameters(snapshot);
			SimulationHelper.loadSimulation(params);
		} catch (IOException e)
		{
			log.error("Could not load snapshot", e);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find module.", e);
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		snapshotController.updateWorldFrame(wFrameWrapper);
		simPanel.updateTime(wFrameWrapper.getSimpleWorldFrame().getTimestamp());
	}
}
