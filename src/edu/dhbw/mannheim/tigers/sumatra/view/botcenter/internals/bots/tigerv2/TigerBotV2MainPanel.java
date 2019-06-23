/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetStateIndicator;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ITigerBotMainPanelObserver;


/**
 * Main panel for the tiger bot.
 * 
 * @author AndreR
 * 
 */
public class TigerBotV2MainPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= 8270294413292980459L;
	
	private final JTextField								id						= new JTextField();
	private final JTextField								name					= new JTextField();
	private final NetStateIndicator						status				= new NetStateIndicator();
	private final JTextField								rxData				= new JTextField();
	private final JTextField								txData				= new JTextField();
	private final JTextField								rxPackets			= new JTextField();
	private final JTextField								txPackets			= new JTextField();
	private final JCheckBox									logMovement			= new JCheckBox();
	private final JCheckBox									logKicker			= new JCheckBox();
	private final JButton									connect				= new JButton("Connect");
	private final JFileChooser								fileChooser			= new JFileChooser();
	private final JRadioButton								procMain				= new JRadioButton("Main");
	private final JRadioButton								procMedia			= new JRadioButton("Media");
	
	private final List<ITigerBotMainPanelObserver>	observers			= new ArrayList<ITigerBotMainPanelObserver>();
	
	
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
		final JButton saveLogs = new JButton("Apply");
		
		saveGeneral.addActionListener(new SaveGeneral());
		connect.addActionListener(new ConnectionChange());
		saveLogs.addActionListener(new SaveLogs());
		
		final JPanel general = new JPanel(new MigLayout("fill", "[50]10[200,fill]"));
		final JPanel network = new JPanel(new MigLayout("fill, wrap 3", "[50][100,fill]10[100,fill]", "[]20[][]"));
		final JPanel logs = new JPanel(new MigLayout("fill", "[100]10[150,fill]"));
		final JPanel firmware = new JPanel(new MigLayout("fill", "[100]10[50,fill]10[50,fill]"));
		
		general.setBorder(BorderFactory.createTitledBorder("General"));
		network.setBorder(BorderFactory.createTitledBorder("Network"));
		logs.setBorder(BorderFactory.createTitledBorder("Logs"));
		firmware.setBorder(BorderFactory.createTitledBorder("Firmware"));
		
		general.add(new JLabel("ID: "));
		general.add(id, "wrap");
		general.add(new JLabel("Name:"));
		general.add(name, "wrap");
		general.add(saveGeneral);
		
		network.add(new JLabel("Status:"));
		network.add(status);
		network.add(connect);
		network.add(new JLabel("Stats"));
		network.add(new JLabel("Packets"));
		network.add(new JLabel("Bytes"));
		network.add(new JLabel("RX"));
		network.add(rxPackets);
		network.add(rxData);
		network.add(new JLabel("TX"));
		network.add(txPackets);
		network.add(txData);
		
		logs.add(new JLabel("Movement:"));
		logs.add(logMovement, "wrap");
		logs.add(new JLabel("Kicker:"));
		logs.add(logKicker, "wrap");
		logs.add(saveLogs, "span 2");
		
		JButton updateFirmwareButton = new JButton("Update firmware");
		updateFirmwareButton.addActionListener(new ChooseBinFile());
		
		firmware.add(new JLabel("Processor:"));
		firmware.add(procMain);
		firmware.add(procMedia, "wrap");
		firmware.add(updateFirmwareButton, "span 3");
		
		add(general, "wrap");
		add(network, "wrap");
		add(logs, "wrap");
		add(firmware, "wrap");
		add(Box.createGlue(), "push");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(ITigerBotMainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(ITigerBotMainPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * 
	 * @param idVal
	 */
	public void setId(final BotID idVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				id.setText(String.valueOf(idVal.getNumber()));
			}
		});
	}
	
	
	/**
	 * 
	 * @return
	 * @throws NumberFormatException
	 */
	public int getId()
	{
		return Integer.parseInt(id.getText());
	}
	
	
	/**
	 * 
	 * @param nameVal
	 */
	public void setBotName(final String nameVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				name.setText(nameVal);
			}
		});
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getBotName()
	{
		return name.getText();
	}
	
	
	/**
	 * 
	 * @param state
	 */
	public void setConnectionState(final ENetworkState state)
	{
		status.setConnectionState(state);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				switch (state)
				{
					case OFFLINE:
						connect.setText("Connect");
						break;
					case CONNECTING:
						connect.setText("Disconnect");
						break;
					case ONLINE:
						connect.setText("Disconnect");
						break;
				}
			}
		});
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogMovement()
	{
		return logMovement.isSelected();
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogMovement(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				logMovement.setSelected(enable);
			}
		});
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogKicker()
	{
		return logKicker.isSelected();
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogKicker(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				logKicker.setSelected(enable);
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
	
	
	protected void notifySaveGeneral()
	{
		synchronized (observers)
		{
			for (final ITigerBotMainPanelObserver o : observers)
			{
				o.onSaveGeneral();
			}
		}
	}
	
	
	protected void notifyConnectionChange()
	{
		synchronized (observers)
		{
			for (final ITigerBotMainPanelObserver o : observers)
			{
				o.onConnectionChange();
			}
		}
	}
	
	
	private void notifySaveLogs()
	{
		synchronized (observers)
		{
			for (final ITigerBotMainPanelObserver observer : observers)
			{
				observer.onSaveLogs();
			}
		}
	}
	
	
	private void notifyUpdateFirmware(String filepath, boolean targetMain)
	{
		synchronized (observers)
		{
			for (ITigerBotMainPanelObserver observer : observers)
			{
				observer.onUpdateFirmware(filepath, targetMain);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	protected class SaveGeneral implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			notifySaveGeneral();
		}
	}
	
	protected class ConnectionChange implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyConnectionChange();
		}
	}
	
	protected class SaveLogs implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifySaveLogs();
		}
	}
	
	protected class ChooseBinFile implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int retVal = fileChooser.showOpenDialog(TigerBotV2MainPanel.this);
			
			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				notifyUpdateFirmware(fileChooser.getSelectedFile().getAbsolutePath(), procMain.isSelected());
			}
		}
	}
}
