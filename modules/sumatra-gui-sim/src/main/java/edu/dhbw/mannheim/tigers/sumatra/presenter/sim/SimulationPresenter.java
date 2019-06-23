/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.presenter.sim;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPresenter extends ASumatraViewPresenter implements ISimulationPanelObserver, IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(SimulationPresenter.class.getName());
	private final SimulationPanel	simPanel			= new SimulationPanel();
	private WorldFrameWrapper		lastWorldFrame	= null;
	private SumatraSimulator		simulator;
	
	
	private void activate()
	{
		simPanel.reset();
		
		try
		{
			AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
					AWorldPredictor.MODULE_ID);
			worldPredictor.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Worldpredictor not found for adding IWorldPredictorObserver", err);
		}
		
		try
		{
			AVisionFilter vf = (AVisionFilter) SumatraModel.getInstance().getModule(AVisionFilter.MODULE_ID);
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
			AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
					AWorldPredictor.MODULE_ID);
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
		if (lastWorldFrame == null)
		{
			return;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		SimpleWorldFrame worldFrame = lastWorldFrame.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String defaultFilename = "data/snapshots/" + sdf.format(new Date()) + ".snap";
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("data/snapshots"));
		fileChooser.setSelectedFile(new File(defaultFilename));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("snapshot files", "snap");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(simPanel) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			// save to file
			try
			{
				snapshot.save(file.getAbsolutePath());
			} catch (IOException e)
			{
				log.error("Could not save snapshot file", e);
			}
		}
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
		if (lastWorldFrame == null)
		{
			return;
		}
		
		SimpleWorldFrame worldFrame = lastWorldFrame.getSimpleWorldFrame();
		Snapshot snapshot = createSnapshot(worldFrame);
		String snapJson = snapshot.toJSON().toJSONString();
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(snapJson);
		clipboard.setContents(stringSelection, null);
	}
	
	
	@Override
	public void onSyncWithAi(final boolean sync)
	{
		SimulationHelper.setProcessAllWorldFrames(sync);
	}
	
	
	private static Snapshot createSnapshot(final SimpleWorldFrame worldFrame)
	{
		Map<BotID, SnapObject> snapBots = new HashMap<>();
		for (Map.Entry<BotID, ITrackedBot> entry : worldFrame.getBots())
		{
			ITrackedBot bot = entry.getValue();
			snapBots.put(entry.getKey(),
					new SnapObject(Vector3.from2d(bot.getPos(), bot.getOrientation()),
							Vector3.from2d(bot.getVel(), bot.getAngularVel())));
		}
		
		ITrackedBall ball = worldFrame.getBall();
		SnapObject snapBall = new SnapObject(ball.getPos3(), ball.getVel3());
		
		return new Snapshot(snapBots, snapBall);
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
		lastWorldFrame = wFrameWrapper;
		simPanel.updateTime(wFrameWrapper.getSimpleWorldFrame().getTimestamp());
	}
}
