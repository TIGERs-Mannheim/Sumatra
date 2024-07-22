/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.sim;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
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
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.view.sim.SimulationBotMgrPanel;
import edu.tigers.sumatra.view.sim.SimulationPanel;
import edu.tigers.sumatra.view.sim.SimulationPanel.ISimulationPanelObserver;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.json.simple.parser.ParseException;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


/**
 * Presenter for the simulator view.
 */
@Log4j2
public class SimulationPresenter
		implements ISumatraViewPresenter, ISimulationPanelObserver, IWorldFrameObserver,
		SimulationBotMgrPanel.ISimulationBotMgrObserver,
		ISimulatorObserver
{
	@Configurable(defValue = "false", comment = "Save move destinations in snapshots")
	private static boolean saveMoveDestinations = false;

	static
	{
		ConfigRegistration.registerClass("user", SimulationPresenter.class);
	}

	@Getter
	private final SimulationPanel viewPanel = new SimulationPanel();
	private final SnapshotController snapshotController = new SnapshotController(viewPanel);
	private final FpsCounter realTimeFpsCounter = new FpsCounter();
	private final FpsCounter frameTimeFpsCounter = new FpsCounter();

	private final Path lastSnapshotFile = Paths.get("data/snapshots/last.json");
	private final String restoreLastSnapshotKey = SimulationPresenter.class.getCanonicalName() + ".restoreLastSnapshot";

	private static final int NO_SLOW_MOTION = Integer.MIN_VALUE;
	private static final int SLOW_MOTION_SPEED = -6;
	private int oldSpeed = NO_SLOW_MOTION;

	private boolean moduleStarted = false;


	public SimulationPresenter()
	{
		GUIUtilities.setEnabledRecursive(viewPanel, false);
	}


	@Override
	public void onStartModuli()
	{
		boolean latestSnapActive = Boolean.parseBoolean(
				SumatraModel.getInstance().getUserProperty(restoreLastSnapshotKey, "false"));
		viewPanel.getRestoreLastSnapshot().setSelected(latestSnapActive);
		if (latestSnapActive)
		{
			loadLastSnapshot();
		}
		ISumatraViewPresenter.super.onStartModuli();
		viewPanel.reset();

		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(o -> o.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.addSimulatorObserver(this));

		viewPanel.addObserver(this);
		GUIUtilities.setEnabledRecursive(viewPanel, true);
		viewPanel.getBotMgrPanel().addObserver(this);

		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(
				simulator -> viewPanel.getBotMgrPanel().getAutoBotCount().setSelected(simulator.getManageBotCount()));
		GlobalShortcuts.add(
				"Play / Pause", viewPanel, this::onToggleSimulation,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Slow motion", viewPanel, this::onToggleSlowMotion,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Step Back (single Frame)", viewPanel, this::onStepBwd,
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Step Forward (single Frame)", viewPanel, this::onStep,
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Save Snapshot to File", viewPanel, this::onSaveSnapshot,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Open Snapshot from File", viewPanel, this::onLoadSnapshot,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		GlobalShortcuts.add(
				"Copy Snapshot to clipboard", viewPanel, this::onCopySnapshot,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		GlobalShortcuts.add(
				"Paste Snapshot from Clipboard", viewPanel, this::onPasteSnapshot,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		moduleStarted = true;
	}


	@Override
	public void onStopModuli()
	{
		onSaveLastSnapshot();
		ISumatraViewPresenter.super.onStopModuli();
		viewPanel.removeObserver(this);
		viewPanel.getBotMgrPanel().removeObserver(this);
		GUIUtilities.setEnabledRecursive(viewPanel, false);
		viewPanel.reset();

		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(o -> o.removeObserver(this));
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.removeSimulatorObserver(this));
		moduleStarted = false;
	}


	private void loadLastSnapshot()
	{
		if (!Files.exists(lastSnapshotFile) || !SumatraModel.getInstance().isSimulation())
		{
			return;
		}
		try
		{
			Snapshot snap = Snapshot.loadFromFile(lastSnapshotFile);
			SimulationHelper.loadSimulation(snap);
		} catch (IOException e)
		{
			log.error("Could not load latest snapshot", e);
		}
	}


	@Override
	public void onToggleSimulation()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> {
			if (s.isRunning())
			{
				s.pause();
			} else
			{
				s.resume();
			}
			viewPanel.setRunning(s.isRunning());
		});
	}


	@Override
	public void onToggleSlowMotion()
	{
		if (oldSpeed == NO_SLOW_MOTION)
		{
			oldSpeed = viewPanel.getSliderSpeed().getValue();
			viewPanel.getSliderSpeed().setValue(SLOW_MOTION_SPEED);
			viewPanel.getSliderSpeed().setEnabled(false);
			viewPanel.getBtnSlowmotion().setSelected(true);
		} else
		{
			viewPanel.getSliderSpeed().setValue(oldSpeed);
			oldSpeed = NO_SLOW_MOTION;
			viewPanel.getSliderSpeed().setEnabled(true);
			viewPanel.getBtnSlowmotion().setSelected(false);
		}
	}


	@Override
	public void onChangeSpeed(final double speed)
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(sim -> sim.setSimSpeed(speed));
	}


	@Override
	public void onStep()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::step);
	}


	@Override
	public void onStepBwd()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(SumatraSimulator::stepBack);
	}


	@Override
	public void onReset()
	{
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(sim -> {
			boolean wasRunning = sim.isRunning();
			sim.pause();
			SumatraModel.getInstance().getModule(Referee.class).resetGameController();
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
		snapshotController.setSaveMoveDestinations(saveMoveDestinations);
		snapshotController.onSnapshot();
	}


	@Override
	public void onSaveLastSnapshot()
	{
		if (!moduleStarted)
		{
			return;
		}
		snapshotController.setSaveMoveDestinations(saveMoveDestinations);
		Snapshot snapshot = snapshotController.createSnapshot();

		try
		{
			snapshot.save(lastSnapshotFile);
		} catch (IOException e)
		{
			log.error("Could not save last snapshot to {}", lastSnapshotFile, e);
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
		snapshotController.setSaveMoveDestinations(saveMoveDestinations);
		snapshotController.onCopySnapshot();
	}


	@Override
	public void onLoadSnapshot()
	{
		File savedSnapshot = Paths.get("data/snapshots/snapshot.json").toFile();
		var lastSnapshotDir = savedSnapshot.getParentFile();
		if (lastSnapshotDir.mkdirs())
		{
			log.info("New directory created: {}", lastSnapshotDir);
		}

		var fcOpenSnapshot = new JFileChooser(lastSnapshotDir);
		fcOpenSnapshot.setSelectedFile(savedSnapshot);

		int returnVal = fcOpenSnapshot.showOpenDialog(viewPanel);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				String path = fcOpenSnapshot.getSelectedFile().getCanonicalPath();
				Snapshot snapshot = Snapshot.loadFromFile(Paths.get(path));
				SimulationHelper.loadSimulation(snapshot);
			} catch (IOException e)
			{
				log.error("Could not load snapshot", e);
			}
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

		viewPanel.updateRelativeTime(relTime);
		viewPanel.updateTime(wFrameWrapper.getSimpleWorldFrame().getTimestamp());
	}


	@Override
	public void onAddBot(final BotID botID)
	{
		final Optional<SumatraSimulator> sim = SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class);
		if (sim.isEmpty())
		{
			viewPanel.getBotMgrPanel().setBotAvailable(botID, false);
			return;
		}
		final int preFactor = (botID.getTeamColor() == ETeamColor.BLUE) ? -1 : 1;
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
		if (sim.isEmpty())
		{
			viewPanel.getBotMgrPanel().setBotAvailable(botID, true);
			return;
		}
		sim.ifPresent(s -> s.unregisterBot(botID));
	}


	@Override
	public void onSetAutoBotCount(final boolean active)
	{
		String key = SumatraSimulator.class.getCanonicalName() + ".manageBotCount";
		SumatraModel.getInstance().setUserProperty(key, String.valueOf(active));
		SumatraModel.getInstance().getModuleOpt(SumatraSimulator.class).ifPresent(s -> s.setManageBotCount(active));
	}


	@Override
	public void onSetRestoreLastSnapshot(final boolean active)
	{
		SumatraModel.getInstance().setUserProperty(restoreLastSnapshotKey, String.valueOf(active));
	}


	@Override
	public void onBotAdded(final BotID botID)
	{
		viewPanel.getBotMgrPanel().setBotAvailable(botID, true);
	}


	@Override
	public void onBotRemove(final BotID botID)
	{
		viewPanel.getBotMgrPanel().setBotAvailable(botID, false);
	}
}
