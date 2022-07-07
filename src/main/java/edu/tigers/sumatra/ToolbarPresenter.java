/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.view.FpsPanel.EFpsType;
import edu.tigers.sumatra.view.toolbar.IToolbarObserver;
import edu.tigers.sumatra.view.toolbar.ToolBar;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;

import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


/**
 * Presenter class for some toolbar functionalities.
 */
@Log4j2
public class ToolbarPresenter implements IModuliStateObserver, IToolbarObserver,
		IRecordObserver, IVisualizationFrameObserver, IWorldFrameObserver,
		IRefereeObserver
{
	private final ToolBar toolbar;

	private final FpsCounter fpsCounterCam = new FpsCounter();
	private final FpsCounter fpsCounterWF = new FpsCounter();
	private final FpsCounter fpsCounterAiY = new FpsCounter();
	private final FpsCounter fpsCounterAiB = new FpsCounter();


	/**
	 * @param toolbar
	 */
	public ToolbarPresenter(final ToolBar toolbar)
	{
		this.toolbar = toolbar;

		ModuliStateAdapter.getInstance().addObserver(this);

		new Timer(1000, new HeapUpdater()).start();
	}


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				startup();
				break;
			case RESOLVED:
				shutdown();
				break;
			default:
				break;
		}
	}


	private void shutdown()
	{
		toolbar.getFpsPanel().clearFps();

		SumatraModel.getInstance().getModuleOpt(RecordManager.class).ifPresent(m -> m.removeObserver(this));
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(m -> m.removeVisObserver(this));
		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(m -> m.removeObserver(this));

		GlobalShortcuts.removeAllForComponent(toolbar.getJToolBar());
	}


	private void startup()
	{
		SumatraModel.getInstance().getModuleOpt(RecordManager.class).ifPresent(m -> m.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(m -> m.addVisObserver(this));
		SumatraModel.getInstance().getModuleOpt(AWorldPredictor.class).ifPresent(m -> m.addObserver(this));

		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(this::addShortcuts);
	}


	private void addShortcuts(AAgent agent)
	{
		GlobalShortcuts.add(
				"Emergency Mode",
				toolbar.getJToolBar(),
				() -> {
					agent.changeMode(EAIControlState.EMERGENCY_MODE);
					SumatraModel.getInstance().getModuleOpt(ASkillSystem.class).ifPresent(ASkillSystem::emergencyStop);
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
		);
		GlobalShortcuts.add(
				"Match Mode",
				toolbar.getJToolBar(),
				() -> agent.changeMode(EAIControlState.MATCH_MODE),
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)
		);
	}


	@Override
	public void onToggleRecord()
	{
		SumatraModel.getInstance().getModuleOpt(RecordManager.class).ifPresent(RecordManager::toggleRecording);
	}


	@Override
	public void onStartStopRecord(final boolean recording)
	{
		toolbar.setRecordingEnabled(recording);
	}


	private class HeapUpdater implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			double curHeap = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
			double totalMem = Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0;
			double maxHeap = Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0;
			toolbar.getHeapBar().setMaximum((int) (totalMem));
			toolbar.getHeapBar().setValue((int) (curHeap));
			toolbar.getHeapLabel().setText(String.format("Mem: %6.1f/%6.1f|%6.1fMB", curHeap, totalMem, maxHeap));
		}
	}


	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		if (fpsCounterCam.newFrame(System.nanoTime()))
		{
			toolbar.getFpsPanel().setFps(EFpsType.CAM, fpsCounterCam.getAvgFps());
		}
	}


	@Override
	public void onClearCamDetectionFrame()
	{
		fpsCounterCam.reset();
		toolbar.getFpsPanel().setFps(EFpsType.CAM, 0);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		if (fpsCounterWF.newFrame(System.nanoTime()))
		{
			toolbar.getFpsPanel().setFps(EFpsType.WP, fpsCounterWF.getAvgFps());
		}
	}


	@Override
	public void onClearWorldFrame()
	{
		fpsCounterWF.reset();
		toolbar.getFpsPanel().setFps(EFpsType.WP, 0);
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		switch (frame.getAiTeam())
		{
			case BLUE:
				if (fpsCounterAiB.newFrame(System.nanoTime()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_B, fpsCounterAiB.getAvgFps());
				}
				break;
			case YELLOW:
				if (fpsCounterAiY.newFrame(System.nanoTime()))
				{
					toolbar.getFpsPanel().setFps(EFpsType.AI_Y, fpsCounterAiY.getAvgFps());
				}
				break;
			default:
				break;
		}
	}


	@Override
	public void onClearVisualizationFrame(final EAiTeam team)
	{
		switch (team)
		{
			case BLUE:
				fpsCounterAiB.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_B, 0);
				break;
			case YELLOW:
				fpsCounterAiY.reset();
				toolbar.getFpsPanel().setFps(EFpsType.AI_Y, 0);
				break;
			default:
				// not supported
				break;
		}
	}
}
