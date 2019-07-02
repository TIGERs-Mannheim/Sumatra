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
import java.util.Optional;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.ISimulatorObserver;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.snapshot.SnapshotController;
import edu.tigers.sumatra.util.GUIUtilities;
import edu.tigers.sumatra.view.sim.SimulationBotMgrPanel;
import edu.tigers.sumatra.view.sim.SimulationPanel;
import edu.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter for the simulator view.
 */
public class SimulationPresenter extends ASumatraViewPresenter
		implements ISimulationPanelObserver, IWorldFrameObserver, SimulationBotMgrPanel.ISimulationBotMgrObserver,
		ISimulatorObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimulationPresenter.class.getName());
	private final SimulationPanel simPanel = new SimulationPanel();
	private final SnapshotController snapshotController = new SnapshotController(simPanel);
	private long lastTimestamp = 0;
	private long realLastTimestamp = System.nanoTime();
	private final ExponentialMovingAverageFilter timeFilter = new ExponentialMovingAverageFilter(0.95);
	
	
	public SimulationPresenter()
	{
		GUIUtilities.setEnabledRecursive(simPanel, false);
	}
	
	private void activate()
	{
		simPanel.reset();
		
		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(o -> o.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.addSimulatorObserver(this));
		
		simPanel.addObserver(this);
		GUIUtilities.setEnabledRecursive(simPanel, true);
		simPanel.getBotMgrPanel().addObserver(this);
		
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(
				simulator -> simPanel.getBotMgrPanel().getAutoBotCount().setSelected(simulator.getManageBotCount()));
		
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
		simPanel.getBotMgrPanel().removeObserver(this);
		GUIUtilities.setEnabledRecursive(simPanel, false);
		simPanel.reset();
		
		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(o -> o.removeObserver(this));
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.removeSimulatorObserver(this));
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
	public void onPauseSimulation()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::pause);
	}
	
	
	@Override
	public void onResumeSimulation()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::play);
	}
	
	
	@Override
	public void onChangeSpeed(final double speed)
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(sim -> sim.setSimSpeed(speed));
	}
	
	
	@Override
	public void onStep(final int i)
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::step);
	}
	
	
	@Override
	public void onStepBwd(final int i)
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::stepBack);
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
		updateTimes(wFrameWrapper);
	}
	
	
	private void updateTimes(final WorldFrameWrapper wFrameWrapper)
	{
		long now = System.nanoTime();
		long frameDiff = wFrameWrapper.getTimestamp() - lastTimestamp;
		long realDiff = now - realLastTimestamp;
		double relTime = (double) frameDiff / realDiff;
		timeFilter.update(relTime);
		
		simPanel.updateRelativeTime(timeFilter.getState());
		simPanel.updateTime(wFrameWrapper.getSimpleWorldFrame().getTimestamp());
		
		realLastTimestamp = now;
		lastTimestamp = wFrameWrapper.getTimestamp();
	}
	
	
	@Override
	public void onAddBot(final BotID botID)
	{
		final Optional<SumatraSimulator> sim = SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class);
		if (!sim.isPresent())
		{
			simPanel.getBotMgrPanel().setBotAvailable(botID, false);
			return;
		}
		final int preFactor = (botID.getTeamColor() == ETeamColor.BLUE) ? 1 : -1;
		final IVector2 positionOnField = Vector2.fromXY(-1 * preFactor * botID.getNumber() * Geometry.getBotRadius() * 4,
				preFactor * (Geometry.getFieldWidth() * 0.5 + Geometry.getBotRadius() * 2));
		final double orientation = -1 * preFactor * AngleMath.PI_HALF;
		
		sim.ifPresent(
				s -> s.registerBot(botID, Pose.from(positionOnField, orientation), Vector3.fromXYZ(0, 0, 0)));
	}
	
	
	@Override
	public void onRemoveBot(final BotID botID)
	{
		final Optional<SumatraSimulator> sim = SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class);
		if (!sim.isPresent())
		{
			simPanel.getBotMgrPanel().setBotAvailable(botID, true);
			return;
		}
		sim.ifPresent(s -> s.unregisterBot(botID));
	}
	
	
	@Override
	public void onSetAutoBotCount(final boolean active)
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.setManageBotCount(active));
	}
	
	
	@Override
	public void onBotAdded(final BotID botID)
	{
		simPanel.getBotMgrPanel().setBotAvailable(botID, true);
	}
	
	
	@Override
	public void onBotRemove(final BotID botID)
	{
		simPanel.getBotMgrPanel().setBotAvailable(botID, false);
	}
}
