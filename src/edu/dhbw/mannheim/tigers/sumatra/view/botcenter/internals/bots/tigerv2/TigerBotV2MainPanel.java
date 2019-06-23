/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.PingStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetStateIndicator;


/**
 * Main panel for the tiger bot.
 * 
 * @author AndreR
 */
public class TigerBotV2MainPanel extends JPanel
{
	/**
	 * Tiger bot main panel observer.
	 * 
	 * @author AndreR
	 */
	public interface ITigerBotV2MainPanelObserver
	{
		/**
		 *
		 */
		void onSaveGeneral();
		
		
		/**
		 * @param id
		 * @param color
		 */
		void onChangeId(int id, ETeamColor color);
		
		
		/**
		 *
		 */
		void onSaveLogs();
		
		
		/**
		 * @param numPings
		 * @param payloadSize
		 */
		void onStartPing(int numPings, int payloadSize);
		
		
		/**
		 *
		 */
		void onStopPing();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long									serialVersionUID	= 8270294413292980459L;
	
	private final JTextField									id						= new JTextField();
	private final JTextField									name					= new JTextField();
	private final JComboBox<Integer>							baseStation			= new JComboBox<Integer>(new Integer[] { 0, 1 });
	private final NetStateIndicator							status				= new NetStateIndicator();
	private final JTextField									rxData				= new JTextField();
	private final JTextField									txData				= new JTextField();
	private final JTextField									rxPackets			= new JTextField();
	private final JTextField									txPackets			= new JTextField();
	private final JTextField									minDelay				= new JTextField();
	private final JTextField									avgDelay				= new JTextField();
	private final JTextField									maxDelay				= new JTextField();
	private final JTextField									lostPings			= new JTextField();
	private final JTextField									numPings				= new JTextField("10");
	private final JTextField									pingPayload			= new JTextField("0");
	private final JButton										startStopPing		= new JButton("Start");
	private final JCheckBox										logMovement			= new JCheckBox();
	private final JCheckBox										logExtMovement		= new JCheckBox();
	private final JCheckBox										logKicker			= new JCheckBox();
	private final JCheckBox										logPower				= new JCheckBox();
	private final JRadioButton									procMain				= new JRadioButton("Main");
	private final JRadioButton									procMedia			= new JRadioButton("Media");
	private final JComboBox<ETeamColor>						color					= new JComboBox<ETeamColor>(new ETeamColor[] {
																								ETeamColor.YELLOW, ETeamColor.BLUE });
	
	private final JTextField									changeId				= new JTextField(3);
	private final JComboBox<ETeamColor>						changeColor			= new JComboBox<ETeamColor>(new ETeamColor[] {
																								ETeamColor.YELLOW, ETeamColor.BLUE });
	
	private final List<ITigerBotV2MainPanelObserver>	observers			= new ArrayList<ITigerBotV2MainPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 *
	 */
	public TigerBotV2MainPanel()
	{
		setLayout(new MigLayout("fill"));
		
		ButtonGroup procs = new ButtonGroup();
		procs.add(procMain);
		procs.add(procMedia);
		procMain.setSelected(true);
		
		final JButton saveGeneral = new JButton("Save");
		final JButton saveChangeId = new JButton("Change");
		final JButton saveLogs = new JButton("Apply");
		
		saveGeneral.addActionListener(new SaveGeneral());
		saveChangeId.addActionListener(new ChangeId());
		saveLogs.addActionListener(new SaveLogs());
		startStopPing.addActionListener(new StartStopPing());
		
		final JPanel general = new JPanel(new MigLayout("fill", "[40]5[100,fill]10[50]5[100,fill]"));
		final JPanel change = new JPanel(new MigLayout("fill", ""));
		final JPanel network = new JPanel(new MigLayout("fill, wrap 3", "[50][100,fill]10[100,fill]", "[]20[][]"));
		final JPanel ping = new JPanel(new MigLayout("fill, wrap 2", "[50][100,fill]"));
		final JPanel logs = new JPanel(new MigLayout("fill", "[100]10[150,fill]"));
		
		general.setBorder(BorderFactory.createTitledBorder("General"));
		change.setBorder(BorderFactory.createTitledBorder("Change BotId"));
		network.setBorder(BorderFactory.createTitledBorder("Network"));
		ping.setBorder(BorderFactory.createTitledBorder("Ping"));
		logs.setBorder(BorderFactory.createTitledBorder("Logs"));
		
		general.add(new JLabel("ID: "));
		general.add(id);
		general.add(new JLabel("Name:"));
		general.add(name, "wrap");
		general.add(new JLabel("Color:"));
		general.add(color);
		general.add(new JLabel("BaseStation:"));
		general.add(baseStation, "wrap");
		general.add(saveGeneral, "span 4");
		
		change.add(changeId);
		change.add(changeColor);
		change.add(saveChangeId);
		
		network.add(new JLabel("Status:"));
		network.add(status, "spanx 2");
		network.add(new JLabel("Stats"));
		network.add(new JLabel("Packets"));
		network.add(new JLabel("Bytes"));
		network.add(new JLabel("RX"));
		network.add(rxPackets);
		network.add(rxData);
		network.add(new JLabel("TX"));
		network.add(txPackets);
		network.add(txData);
		
		ping.add(new JLabel("Num Pings:"));
		ping.add(numPings);
		ping.add(new JLabel("Payload:"));
		ping.add(pingPayload);
		ping.add(startStopPing, "span 2");
		ping.add(new JLabel("Min Delay:"));
		ping.add(minDelay);
		ping.add(new JLabel("Avg Delay:"));
		ping.add(avgDelay);
		ping.add(new JLabel("Max Delay:"));
		ping.add(maxDelay);
		ping.add(new JLabel("Lost Pings:"));
		ping.add(lostPings);
		
		logs.add(new JLabel("Movement:"));
		logs.add(logMovement, "wrap");
		logs.add(new JLabel("Extended Movement:"));
		logs.add(logExtMovement, "wrap");
		logs.add(new JLabel("Kicker:"));
		logs.add(logKicker, "wrap");
		logs.add(new JLabel("Power:"));
		logs.add(logPower, "wrap");
		logs.add(saveLogs, "span 2");
		
		add(general, "wrap");
		add(change, "wrap");
		add(network, "wrap");
		add(logs, "wrap");
		add(ping, "wrap");
		add(Box.createGlue(), "push");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final ITigerBotV2MainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ITigerBotV2MainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @param idVal
	 */
	public void setId(final BotID idVal)
	{
		id.setText(String.valueOf(idVal.getNumber()));
	}
	
	
	/**
	 * @return
	 * @throws NumberFormatException
	 */
	public int getId()
	{
		return Integer.parseInt(id.getText());
	}
	
	
	/**
	 * @param nameVal
	 */
	public void setBotName(final String nameVal)
	{
		name.setText(nameVal);
	}
	
	
	/**
	 * @return
	 */
	public String getBotName()
	{
		return name.getText();
	}
	
	
	/**
	 * @param nameVal
	 */
	public void setBaseStation(final int nameVal)
	{
		baseStation.setSelectedItem(nameVal);
	}
	
	
	/**
	 * @return
	 */
	public Integer getBaseStation()
	{
		return (Integer) baseStation.getSelectedItem();
	}
	
	
	/**
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		status.setConnectionState(state);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogMovement()
	{
		return logMovement.isSelected();
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogExtMovement()
	{
		return logExtMovement.isSelected();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogMovement(final boolean enable)
	{
		logMovement.setSelected(enable);
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogExtMovement(final boolean enable)
	{
		logExtMovement.setSelected(enable);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogKicker()
	{
		return logKicker.isSelected();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogKicker(final boolean enable)
	{
		logKicker.setSelected(enable);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogPower()
	{
		return logPower.isSelected();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogPower(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				logPower.setSelected(enable);
			}
		});
	}
	
	
	/**
	 * @param stat
	 */
	public void setTxStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				txPackets.setText(Integer.toString(stat.packets));
				txData.setText(Integer.toString(stat.payload));
			}
		});
	}
	
	
	/**
	 * @param stat
	 */
	public void setRxStat(final Statistics stat)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				rxPackets.setText(Integer.toString(stat.packets));
				rxData.setText(Integer.toString(stat.payload));
			}
		});
	}
	
	
	/**
	 * @param stats
	 */
	public void setPingStats(final PingStats stats)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				minDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.minDelay));
				avgDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.avgDelay));
				maxDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.maxDelay));
				lostPings.setText(Integer.toString(stats.lostPings));
			}
		});
	}
	
	
	protected void notifySaveGeneral()
	{
		synchronized (observers)
		{
			for (final ITigerBotV2MainPanelObserver o : observers)
			{
				o.onSaveGeneral();
			}
		}
	}
	
	
	protected void notifyChangeId()
	{
		synchronized (observers)
		{
			for (final ITigerBotV2MainPanelObserver o : observers)
			{
				int id;
				try
				{
					id = Integer.valueOf(changeId.getText());
				} catch (NumberFormatException err)
				{
					return;
				}
				ETeamColor color = (ETeamColor) changeColor.getSelectedItem();
				o.onChangeId(id, color);
			}
		}
	}
	
	
	private void notifySaveLogs()
	{
		synchronized (observers)
		{
			for (final ITigerBotV2MainPanelObserver observer : observers)
			{
				observer.onSaveLogs();
			}
		}
	}
	
	
	private void notifyStartPing(final int numPings, final int payloadSize)
	{
		synchronized (observers)
		{
			for (ITigerBotV2MainPanelObserver observer : observers)
			{
				observer.onStartPing(numPings, payloadSize);
			}
		}
	}
	
	
	private void notifyStopPing()
	{
		synchronized (observers)
		{
			for (ITigerBotV2MainPanelObserver observer : observers)
			{
				observer.onStopPing();
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public ETeamColor getColor()
	{
		return (ETeamColor) color.getSelectedItem();
	}
	
	
	/**
	 * @param c
	 */
	public void setColor(final ETeamColor c)
	{
		color.setSelectedItem(c);
	}
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class SaveGeneral implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			notifySaveGeneral();
		}
	}
	
	protected class ChangeId implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyChangeId();
		}
	}
	
	protected class SaveLogs implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifySaveLogs();
		}
	}
	
	protected class StartStopPing implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			if (startStopPing.getText().equals("Start"))
			{
				int num = 0;
				int payload = 0;
				
				try
				{
					num = Integer.valueOf(numPings.getText());
					payload = Integer.valueOf(pingPayload.getText());
				} catch (final NumberFormatException err)
				{
					return;
				}
				
				notifyStartPing(num, payload);
				
				startStopPing.setText("Stop");
			} else
			{
				notifyStopPing();
				
				startStopPing.setText("Start");
			}
		}
	}
}
