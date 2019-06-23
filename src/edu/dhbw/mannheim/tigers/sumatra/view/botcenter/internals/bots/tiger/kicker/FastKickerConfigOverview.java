/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.07.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Overview for fast kicker configuration.
 * 
 * @author Oliver Steinbrecher
 */
public class FastKickerConfigOverview extends JPanel
{
	/**
	 */
	public interface IFastKickerConfigObserver
	{
		/**
		 * @param botId
		 */
		void onSetAutoChg(BotID botId);
		
		
		/**
		 * @param chg
		 */
		void onSetChgAll(int chg);
		
		
		/**
		 */
		void onDischargeAll();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long								serialVersionUID	= -43699869205865027L;
	
	private final Map<BotID, JPanel>						botPanels			= new TreeMap<BotID, JPanel>(BotID.getComparator());
	
	private JButton											setChg				= null;
	private JButton											dischargeAll		= null;
	
	private JTextField										maxCap				= null;
	
	private final List<IFastKickerConfigObserver>	observers			= new ArrayList<IFastKickerConfigObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public FastKickerConfigOverview()
	{
		setLayout(new MigLayout("fill", "", ""));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param panel
	 */
	public void addBotPanel(final BotID botId, final JPanel panel)
	{
		botPanels.put(botId, panel);
		
		updatePanels();
	}
	
	
	/**
	 * @param panel
	 */
	public void removeBotPanel(final JPanel panel)
	{
		for (Map.Entry<BotID, JPanel> entry : botPanels.entrySet())
		{
			if (entry.getValue().equals(panel))
			{
				botPanels.remove(entry.getKey());
				break;
			}
		}
		
		updatePanels();
	}
	
	
	/**
	 */
	public void removeAllBotPanels()
	{
		botPanels.clear();
		
		updatePanels();
	}
	
	
	/**
	 * @return
	 */
	public int getMaxChg()
	{
		if (maxCap != null)
		{
			final int max = Integer.parseInt(maxCap.getText());
			return max < 0 ? 0 : max;
		}
		return 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void updatePanels()
	{
		final JPanel panel = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				removeAll();
				
				setChg = new JButton("set");
				setChg.addActionListener(new SetChgAll());
				dischargeAll = new JButton("discharge all");
				dischargeAll.addActionListener(new DischargeAll());
				
				maxCap = new JTextField("150");
				
				final JPanel maxPanel = new JPanel(new MigLayout("fill", "[100!, fill]"));
				maxPanel.add(new JLabel("maxCap"));
				maxPanel.add(maxCap);
				
				add(maxPanel);
				add(setChg);
				add(dischargeAll, "wrap, push");
				
				for (final Entry<BotID, JPanel> pair : botPanels.entrySet())
				{
					add(pair.getValue(), "wrap, gapbottom 0");
				}
				
				add(Box.createGlue(), "push");
				
				SwingUtilities.updateComponentTreeUI(panel);
			}
		});
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IFastKickerConfigObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IFastKickerConfigObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyAutoLoad()
	{
		synchronized (observers)
		{
			for (final IFastKickerConfigObserver observer : observers)
			{
				for (final Entry<BotID, JPanel> pair : botPanels.entrySet())
				{
					try
					{
						final KickerConfig kickerPanel = (KickerConfig) pair.getValue();
						
						if (kickerPanel.isAutoloadEnabled())
						{
							observer.onSetAutoChg(pair.getKey());
						}
					} catch (final ClassCastException e)
					{
						// should not appear
					}
				}
			}
		}
	}
	
	
	private void notifySetChgAll(final int chg)
	{
		synchronized (observers)
		{
			for (final IFastKickerConfigObserver observer : observers)
			{
				observer.onSetChgAll(chg);
			}
		}
	}
	
	
	private void notifyDischargeAll()
	{
		synchronized (observers)
		{
			for (final IFastKickerConfigObserver observer : observers)
			{
				observer.onDischargeAll();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class SetChgAll implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent evt)
		{
			try
			{
				notifySetChgAll(getMaxChg());
				notifyAutoLoad();
			} catch (final NumberFormatException e)
			{
				
			}
		}
	}
	
	private class DischargeAll implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent evt)
		{
			notifyDischargeAll();
		}
	}
}
