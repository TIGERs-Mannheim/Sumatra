/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.sim;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.sim.ISimulatorObserver;
import edu.tigers.sumatra.sim.SimulationHelper;
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
import lombok.extern.log4j.Log4j2;
import org.json.simple.parser.ParseException;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;


/**
 * Presenter for the simulator view.
 */
@Log4j2
public class SimulationPresenter extends ASumatraViewPresenter
		implements ISimulationPanelObserver, IWorldFrameObserver, SimulationBotMgrPanel.ISimulationBotMgrObserver,
		ISimulatorObserver
{
	private final SimulationPanel simPanel = new SimulationPanel();
	private final SnapshotController snapshotController = new SnapshotController(simPanel);
	private final FpsCounter realTimeFpsCounter = new FpsCounter();
	private final FpsCounter frameTimeFpsCounter = new FpsCounter();


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

		BotID.getAll().forEach(id -> {
			String key = SimulationPresenter.class.getCanonicalName() + ".bots." + id;
			boolean checked = Boolean.parseBoolean(SumatraModel.getInstance().getUserProperty(key, "false"));
			if (checked)
			{
				onAddBot(id);
			}
		});
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
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::resume);
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
	public void onReset()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(sim -> {
			boolean wasRunning = sim.isRunning();
			sim.pause();
			SumatraModel.getInstance().getModule(Referee.class).initGameController();
			// consume new referee message
			sim.stepBlocking();
			sim.reset();
			// do initial step to push new state
			sim.stepBlocking();
			if (wasRunning)
			{
				sim.resume();
			}
		});
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
				SimulationHelper.loadSimulation(snapshot);
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
			SimulationHelper.loadSimulation(snapshot);
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
		realTimeFpsCounter.newFrame(System.nanoTime());
		frameTimeFpsCounter.newFrame(wFrameWrapper.getTimestamp());
		double relTime = realTimeFpsCounter.getAvgFps() / frameTimeFpsCounter.getAvgFps();

		simPanel.updateRelativeTime(relTime);
		simPanel.updateTime(wFrameWrapper.getSimpleWorldFrame().getTimestamp());
	}


	@Override
	public void onAddBot(final BotID botID)
	{
		final Optional<SumatraSimulator> sim = SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class);
		if (sim.isEmpty())
		{
			simPanel.getBotMgrPanel().setBotAvailable(botID, false);
			return;
		}
		final int preFactor = (botID.getTeamColor() == ETeamColor.BLUE) ? -1 : 1;
		final IVector2 positionOnField = Vector2.fromXY(-1 * preFactor * botID.getNumber() * Geometry.getBotRadius() * 4,
				preFactor * (Geometry.getFieldWidth() * 0.5 + Geometry.getBotRadius() * 2));
		final double orientation = -1 * preFactor * AngleMath.PI_HALF;

		sim.ifPresent(
				s -> s.registerBot(botID, Pose.from(positionOnField, orientation), Vector3.fromXYZ(0, 0, 0)));

		String key = SimulationPresenter.class.getCanonicalName() + ".bots." + botID;
		SumatraModel.getInstance().setUserProperty(key, "true");
	}


	@Override
	public void onRemoveBot(final BotID botID)
	{
		final Optional<SumatraSimulator> sim = SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class);
		if (sim.isEmpty())
		{
			simPanel.getBotMgrPanel().setBotAvailable(botID, true);
			return;
		}
		sim.ifPresent(s -> s.unregisterBot(botID));

		String key = SimulationPresenter.class.getCanonicalName() + ".bots." + botID;
		SumatraModel.getInstance().setUserProperty(key, "false");
	}


	@Override
	public void onSetAutoBotCount(final boolean active)
	{
		String key = SumatraSimulator.class.getCanonicalName() + ".manageBotCount";
		SumatraModel.getInstance().setUserProperty(key, String.valueOf(active));
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
