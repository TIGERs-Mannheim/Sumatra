/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario.ASimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.scenario.ESimulationScenario;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationPanel extends JPanel implements ISumatraView
{
	/**  */
	private static final long							serialVersionUID	= 4936408016928626573L;
	@SuppressWarnings("unused")
	private static final Logger						log					= Logger.getLogger(SimulationPanel.class.getName());
	
	private final InstanceablePanel					instPanel;
	private final JToggleButton						btnPauseSim;
	private final JSlider								sliderSpeed;
	private final JLabel									labelSpeed;
	private final JButton								btnStep;
	private final JButton								btnReset;
	private final JButton								btnLoadSnapshot;
	private final JFileChooser							fcOpenSnapshot;
	
	private final List<ISimulationPanelObserver>	observers			= new CopyOnWriteArrayList<ISimulationPanelObserver>();
	
	
	/**
	 * 
	 */
	public SimulationPanel()
	{
		setLayout(new FlowLayout());
		
		instPanel = new InstanceablePanel(ESimulationScenario.values(), SumatraModel.getInstance().getUserSettings());
		instPanel.addObserver(new ScenarioObserver());
		instPanel.setShowCreate(true);
		add(instPanel);
		
		btnPauseSim = new JToggleButton("Pause");
		btnPauseSim.addActionListener(new PauseSimulationAction());
		add(btnPauseSim);
		
		btnStep = new JButton("Step");
		btnStep.addActionListener(new StepAction());
		add(btnStep);
		
		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ResetAction());
		add(btnReset);
		
		sliderSpeed = new JSlider(-100, 100, 0);
		sliderSpeed.setMajorTickSpacing(50);
		sliderSpeed.setMinorTickSpacing(10);
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setPaintTicks(true);
		sliderSpeed.addChangeListener(new SpeedListener());
		add(sliderSpeed);
		
		labelSpeed = new JLabel(String.format("%d", 0));
		labelSpeed.setPreferredSize(new Dimension(30, labelSpeed.getMaximumSize().height));
		add(labelSpeed);
		
		btnLoadSnapshot = new JButton("Load snapshot...");
		btnLoadSnapshot.addActionListener(new OpenSnapAction());
		add(btnLoadSnapshot);
		
		String path = null;
		try
		{
			path = Paths.get("").toFile().getCanonicalPath() + "/data/snapshots";
		} catch (IOException e)
		{
			log.error("", e);
		}
		
		fcOpenSnapshot = new JFileChooser(path);
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
		 */
		void onRunSimulation(ASimulationScenario scenario);
		
		
		/**
		 * 
		 */
		void onPauseSimulation();
		
		
		/**
		 * 
		 */
		void onResumeSimulation();
		
		
		/**
		 * @param speed
		 */
		void onChangeSpeed(double speed);
		
		
		/**
		 * @param i
		 */
		void onStep(int i);
		
		
		/**
		 * 
		 */
		void onReset();
		
		
		/**
		 * @param path
		 */
		void onLoadSnapshot(String path);
	}
	
	private class PauseSimulationAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (btnPauseSim.isSelected())
			{
				for (ISimulationPanelObserver o : observers)
				{
					o.onPauseSimulation();
				}
			} else
			{
				for (ISimulationPanelObserver o : observers)
				{
					o.onResumeSimulation();
				}
			}
		}
	}
	
	private class ScenarioObserver implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			ASimulationScenario scenario = (ASimulationScenario) object;
			for (ISimulationPanelObserver o : observers)
			{
				o.onRunSimulation(scenario);
			}
		}
	}
	
	private class SpeedListener implements ChangeListener
	{
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			double speed = sliderSpeed.getValue() / 10.0;
			
			labelSpeed.setText(String.format("%d", 10 * Math.round(speed)));
			
			if (speed < 0)
			{
				speed = 1 / -speed;
			} else if (speed == 0)
			{
				speed = 1;
			} else
			{
				speed += 1;
			}
			for (ISimulationPanelObserver o : observers)
			{
				o.onChangeSpeed(speed);
			}
		}
	}
	
	private class StepAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onStep(1);
			}
		}
	}
	
	private class ResetAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISimulationPanelObserver o : observers)
			{
				o.onReset();
			}
		}
	}
	
	private class OpenSnapAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int returnVal = fcOpenSnapshot.showOpenDialog(btnLoadSnapshot);
			
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fcOpenSnapshot.getSelectedFile();
				
				for (ISimulationPanelObserver o : observers)
				{
					try
					{
						o.onLoadSnapshot(file.getCanonicalPath());
					} catch (IOException e1)
					{
						log.error("", e1);
					}
				}
			}
		}
	}
}
