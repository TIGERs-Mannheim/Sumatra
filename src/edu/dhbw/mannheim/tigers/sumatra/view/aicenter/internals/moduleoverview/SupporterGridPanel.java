/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 20, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosGPUCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosGPUCalc.EScoringTypes;


/**
 * Set weighting for supporters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SupporterGridPanel extends JPanel
{
	private static final Logger							log						= Logger.getLogger(SupporterGridPanel.class
																										.getName());
	private static final long								serialVersionUID		= -1981115288021600708L;
	private static final int								RESOLUTION				= 1000;
	
	private Map<EScoringTypes, JSlider>					weightingBars			= new EnumMap<>(EScoringTypes.class);
	private final JPanel										weightingBarsPanel	= new JPanel();
	private final JComboBox<String>						cmbFile					= new JComboBox<String>();
	private final List<ISupporterGridPanelObserver>	observers				= new CopyOnWriteArrayList<ISupporterGridPanelObserver>();
	
	
	/**
	 * 
	 */
	public SupporterGridPanel()
	{
		setLayout(new MigLayout("fill, wrap 1", ""));
		
		JPanel cfgPanel = new JPanel();
		add(cfgPanel);
		
		cmbFile.setEditable(true);
		loadCfgFiles();
		cfgPanel.add(cmbFile);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new SaveAction());
		cfgPanel.add(btnSave);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new LoadAction());
		cfgPanel.add(btnLoad);
		
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new QueryAction());
		cfgPanel.add(btnUpdate);
		
		weightingBarsPanel.setLayout(new MigLayout("fill, wrap 2", "[][max]"));
		add(weightingBarsPanel);
	}
	
	
	private void loadCfgFiles()
	{
		cmbFile.removeAllItems();
		try
		{
			Files.list(Paths.get("config", "support")).filter(p -> p.toFile().isFile())
					.forEach(p -> cmbFile.addItem(p.toFile().getName()));
		} catch (IOException err)
		{
			log.error("Could not read config files", err);
		}
		if (cmbFile.getItemCount() == 0)
		{
			cmbFile.addItem("default");
		}
		String cfgName = SumatraModel.getInstance().getUserProperty(
				RedirectPosGPUCalc.class.getCanonicalName() + ".config");
		if (cfgName != null)
		{
			cmbFile.setSelectedItem(cfgName);
		} else
		{
			cmbFile.setSelectedIndex(0);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ISupporterGridPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISupporterGridPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyWeightChanged(final EScoringTypes type, final float value)
	{
		for (ISupporterGridPanelObserver observer : observers)
		{
			observer.onWeightChanged(type, value);
		}
	}
	
	
	/**
	 * @param type
	 * @param value
	 */
	public void setWeighting(final EScoringTypes type, final float value)
	{
		if (!weightingBars.containsKey(type))
		{
			final JSlider sb = new JSlider(SwingConstants.HORIZONTAL, 0, RESOLUTION, (int) (value * RESOLUTION));
			sb.setPreferredSize(new Dimension(1000, 100));
			weightingBars.put(type, sb);
			weightingBarsPanel.add(new JLabel(type.name()));
			weightingBarsPanel.add(sb);
			sb.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(final ChangeEvent e)
				{
					notifyWeightChanged(type, sb.getValue() / (float) RESOLUTION);
				}
			});
		} else
		{
			JSlider sb = weightingBars.get(type);
			if (!SumatraMath.isEqual(sb.getValue() / (float) RESOLUTION, value, 1e-3f))
			{
				ChangeListener chls[] = sb.getChangeListeners();
				sb.removeChangeListener(chls[0]);
				sb.setValue((int) (value * RESOLUTION));
				sb.addChangeListener(chls[0]);
			}
		}
	}
	
	
	/**
	 */
	public interface ISupporterGridPanelObserver
	{
		/**
		 * @param type
		 * @param value
		 */
		void onWeightChanged(EScoringTypes type, float value);
		
		
		/**
		 */
		void onQueryWeights();
		
		
		/**
		 * @param name
		 */
		void onSaveWeights(String name);
		
		
		/**
		 * @param name
		 */
		void onLoadWeights(String name);
	}
	
	private class SaveAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			String name = cmbFile.getSelectedItem().toString();
			for (ISupporterGridPanelObserver o : observers)
			{
				o.onSaveWeights(name);
			}
			loadCfgFiles();
		}
	}
	
	private class QueryAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (ISupporterGridPanelObserver o : observers)
			{
				o.onQueryWeights();
			}
		}
	}
	
	private class LoadAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			String name = cmbFile.getSelectedItem().toString();
			for (ISupporterGridPanelObserver o : observers)
			{
				o.onLoadWeights(name);
			}
			for (ISupporterGridPanelObserver o : observers)
			{
				o.onQueryWeights();
			}
		}
	}
}
