/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.07.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * Overview for fast kicker configuration.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class FastKickerConfigOverview extends JPanel
{
	public interface IFastKickerConfigObserver
	{
		
		public void onSetAutoChg(int botId);
		

		public void onSetChgAll(int chg);
		

		public void onDischargeAll();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long								serialVersionUID	= -43699869205865027L;
	
	// private ArrayList<JPanel> botPanels = new ArrayList<JPanel>();
	private final HashMap<Integer, JPanel>				botPanels			= new HashMap<Integer, JPanel>();
	
	private boolean											active				= false;
	
	private JButton											setChg				= null;
	private JButton											dischargeAll		= null;
	
	private JTextField										maxCap				= null;
	
	private final List<IFastKickerConfigObserver>	observers			= new ArrayList<IFastKickerConfigObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public FastKickerConfigOverview()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		setActive(false);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setActive(boolean active)
	{
		this.active = active;
		
		updatePanels();
	}
	

	public void addBotPanel(int botId, JPanel panel)
	{
		// botPanels.add(panel);
		botPanels.put(botId, panel);
		
		updatePanels();
	}
	

	public void removeBotPanel(JPanel panel)
	{
		botPanels.remove(panel);
		
		updatePanels();
	}
	

	public void removeAllBotPanels()
	{
		botPanels.clear();
		
		updatePanels();
	}
	

	public int getMaxChg()
	{
		if (maxCap != null)
		{
			int max = Integer.parseInt(maxCap.getText());
			return max < 0 ? 0 : max;
		} else
		{
			return 0;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void updatePanels()
	{
		final JPanel panel = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				removeAll();
				
				if (active)
				{
					for (Entry<Integer, JPanel> pair : botPanels.entrySet())
					{
						add(pair.getValue(), "wrap, gapbottom 0");
					}
					
					// for (JPanel panel : botPanels)
					// {
					// add(panel, "wrap, gapbottom 0");
					// }
					
					setChg = new JButton("set");
					setChg.addActionListener(new SetChgAll());
					dischargeAll = new JButton("discharge all");
					dischargeAll.addActionListener(new DischargeAll());
					
					maxCap = new JTextField();
					
					JPanel maxPanel = new JPanel(new MigLayout("fill", "[100!, fill]"));
					
					maxPanel.add(new JLabel("maxCap"));
					maxPanel.add(maxCap);
					
					add(maxPanel, "wrap");
					
					add(setChg);
					add(dischargeAll);
					

				} else
				{
					add(new JLabel("Botcenter unavailable - botmanager stopped"), "wrap");
					setChg = null;
					dischargeAll = null;
					maxCap = null;
				}
				
				add(Box.createGlue(), "push");
				
				SwingUtilities.updateComponentTreeUI(panel);
			}
		});
	}
	

	public void addObserver(IFastKickerConfigObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IFastKickerConfigObserver observer)
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
			for (IFastKickerConfigObserver observer : observers)
			{
				for (Entry<Integer, JPanel> pair : botPanels.entrySet())
				{
					try
					{
						KickerConfig kickerPanel = (KickerConfig) pair.getValue();
						
						if (kickerPanel.isAutoloadEnabled())
						{
							observer.onSetAutoChg(pair.getKey());
						}
					} catch (ClassCastException e)
					{
						// should not appear
					}
				}
			}
		}
	}
	

	private void notifySetChgAll(int chg)
	{
		synchronized (observers)
		{
			for (IFastKickerConfigObserver observer : observers)
			{
				observer.onSetChgAll(chg);
			}
		}
	}
	

	private void notifyDischargeAll()
	{
		synchronized (observers)
		{
			for (IFastKickerConfigObserver observer : observers)
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
		public void actionPerformed(ActionEvent evt)
		{
			try
			{
				notifySetChgAll(getMaxChg());
				notifyAutoLoad();
			} catch (NumberFormatException e)
			{
				
			}
		}
	}
	
	private class DischargeAll implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			notifyDischargeAll();
		}
	}
}
