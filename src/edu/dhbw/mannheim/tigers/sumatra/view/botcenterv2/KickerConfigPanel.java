/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerKickerConfig;


/**
 * Configure kicker/barrier parameters.
 * 
 * @author AndreR
 */
public class KickerConfigPanel extends JPanel
{
	/** */
	public interface IKickerConfigPanelObserver
	{
		/**
		 * @param cfg
		 */
		void onSave(TigerKickerConfig cfg);
		
		
		/** */
		void onQuery();
		
		
		/**
		 * @param cfg
		 */
		void onApplyToAll(TigerKickerConfig cfg);
	}
	
	/**  */
	private static final long								serialVersionUID	= 9192911965576762258L;
	
	private final JTextField								maxCapLevel			= new JTextField();
	private final JTextField								kickerCooldown		= new JTextField();
	private final JTextField								irThreshold			= new JTextField();
	private final JTextField								irFilterLimit		= new JTextField();
	
	private final List<IKickerConfigPanelObserver>	observers			= new CopyOnWriteArrayList<IKickerConfigPanelObserver>();
	
	
	/** */
	public KickerConfigPanel()
	{
		setLayout(new MigLayout("wrap 2", "[100,fill]10[100,fill]"));
		
		final JButton save = new JButton("Save");
		save.addActionListener(new Save());
		final JButton query = new JButton("Query");
		query.addActionListener(new Query());
		final JButton applyToAll = new JButton("Apply To All");
		applyToAll.addActionListener(new ApplyToAll());
		
		add(new JLabel("Max Cap Level [V]:"));
		add(maxCapLevel);
		maxCapLevel.setToolTipText("Range: 0V - " + TigerKickerConfig.MAX_LEVEL + "V");
		
		add(new JLabel("Kicker Cooldown [s]:"));
		add(kickerCooldown);
		kickerCooldown.setToolTipText("Range: 0.001s - 65.535s");
		
		add(new JLabel("IR Threshold [V]:"));
		add(irThreshold);
		irThreshold.setToolTipText("Range: 0.001V - 3.300V");
		
		add(new JLabel("IR Filter Limit [-]:"));
		add(irFilterLimit);
		irFilterLimit.setToolTipText("IR Threshold undershot counter limit until barrier interrupted triggers");
		
		add(query);
		add(save);
		add(applyToAll, "spanx");
		
		setBorder(BorderFactory.createTitledBorder("Kicker Configuration"));
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IKickerConfigPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IKickerConfigPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @param cfg
	 */
	public void setConfig(final TigerKickerConfig cfg)
	{
		SwingUtilities.invokeLater(() -> {
			maxCapLevel.setText("" + cfg.getMaxCapLevel());
			kickerCooldown.setText(String.format(Locale.ENGLISH, "%.3f", cfg.getKickerCooldown()));
			irThreshold.setText(String.format(Locale.ENGLISH, "%.3f", cfg.getIrThreshold()));
			irFilterLimit.setText("" + cfg.getIrFilterLimit());
		});
	}
	
	
	private void notifySave(final TigerKickerConfig cfg)
	{
		synchronized (observers)
		{
			for (IKickerConfigPanelObserver observer : observers)
			{
				observer.onSave(cfg);
			}
		}
	}
	
	
	private void notifyQuery()
	{
		synchronized (observers)
		{
			for (IKickerConfigPanelObserver observer : observers)
			{
				observer.onQuery();
			}
		}
	}
	
	
	private void notifyApplyToAll(final TigerKickerConfig cfg)
	{
		synchronized (observers)
		{
			for (IKickerConfigPanelObserver observer : observers)
			{
				observer.onApplyToAll(cfg);
			}
		}
	}
	
	
	private TigerKickerConfig parseInput()
	{
		TigerKickerConfig cfg = new TigerKickerConfig();
		
		try
		{
			cfg.setMaxCapLevel(Integer.parseInt(maxCapLevel.getText()));
			cfg.setKickerCooldown(Float.parseFloat(kickerCooldown.getText()));
			cfg.setIrThreshold(Float.parseFloat(irThreshold.getText()));
			cfg.setIrFilterLimit(Integer.parseInt(irFilterLimit.getText()));
		} catch (final NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(null, "Invalid input value", "Invalid Input", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		return cfg;
	}
	
	
	private class Save implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			TigerKickerConfig cfg = parseInput();
			if (cfg == null)
			{
				return;
			}
			
			notifySave(cfg);
		}
	}
	
	private class Query implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyQuery();
		}
	}
	
	private class ApplyToAll implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (JOptionPane.showConfirmDialog(null, "Really apply these values to all active bots?", "Confirm Action",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			{
				return;
			}
			
			TigerKickerConfig cfg = parseInput();
			if (cfg == null)
			{
				return;
			}
			
			notifyApplyToAll(cfg);
		}
	}
}
