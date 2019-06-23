/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;

/**
 * Main panel for the tiger bot.
 * 
 * @author AndreR
 * 
 */
public class TigerBotMainPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 8270294413292980459L;

	private JTextField id = new JTextField();
	private JTextField name = new JTextField();
	private JTextField ip = new JTextField();
	private JTextField port = new JTextField();
	private JTextField status = new JTextField();
	private JTextField mac = new JTextField();
	private JTextField serverPort = new JTextField();
	private JTextField cpuId = new JTextField();
	private JCheckBox useUpdateAll = new JCheckBox();
	private JCheckBox logMovement = new JCheckBox();
	private JCheckBox logKicker = new JCheckBox();
	private JCheckBox logAccel = new JCheckBox();
	private JCheckBox logIr = new JCheckBox();
	private JButton connect = new JButton("Connect");
	
	private final List<ITigerBotMainPanelObserver> observers = new ArrayList<ITigerBotMainPanelObserver>();

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerBotMainPanel()
	{
		setLayout(new MigLayout("fill"));
		
		status.setEditable(false);
		
		JButton saveGeneral = new JButton("Save");
		JButton saveLogs = new JButton("Apply");

		saveGeneral.addActionListener(new SaveGeneral());
		connect.addActionListener(new ConnectionChange());
		saveLogs.addActionListener(new SaveLogs());
		
		JPanel general = new JPanel(new MigLayout("fill", "[50]10[200,fill]"));
		JPanel network = new JPanel(new MigLayout("fill", "[50][100,fill]10[100,fill]"));
		JPanel logs = new JPanel(new MigLayout("fill", "[100]10[150,fill]"));
		
		general.setBorder(BorderFactory.createTitledBorder("General"));
		network.setBorder(BorderFactory.createTitledBorder("Network"));
		logs.setBorder(BorderFactory.createTitledBorder("Logs"));
		
		general.add(new JLabel("ID: "));
		general.add(id, "wrap");
		general.add(new JLabel("Name:"));
		general.add(name, "wrap");
		general.add(new JLabel("CPU ID:"));
		general.add(cpuId, "wrap");
		general.add(new JLabel("MAC:"));
		general.add(mac, "wrap");
		general.add(new JLabel("IP:"));
		general.add(ip, "wrap");
		general.add(new JLabel("Port:"));
		general.add(port, "wrap");
		general.add(new JLabel("Server Port:"));
		general.add(serverPort, "wrap");
		general.add(new JLabel("Use UpdateAll:"));
		general.add(useUpdateAll, "wrap");
		general.add(saveGeneral);
		
		network.add(new JLabel("Status:"));
		network.add(status);
		network.add(connect);
		
		logs.add(new JLabel("Movement:"));
		logs.add(logMovement, "wrap");
		logs.add(new JLabel("Kicker:"));
		logs.add(logKicker, "wrap");
		logs.add(new JLabel("Acceleration:"));
		logs.add(logAccel, "wrap");
		logs.add(new JLabel("IR:"));
		logs.add(logIr, "wrap");
		logs.add(saveLogs, "span 2");
		
		add(general, "wrap");
		add(network, "wrap");
		add(logs, "wrap");
		add(Box.createGlue(), "push");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ITigerBotMainPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
		}
	}
	
	
	public void removeObserver(ITigerBotMainPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
		}
	}
	
	public void setId(final int idVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				id.setText(String.valueOf(idVal));
			}
		});
	}
	
	public int getId() throws NumberFormatException
	{
		return Integer.parseInt(id.getText());
	}
	
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
	
	public String getBotName()
	{
		return name.getText();
	}
	
	public void setIp(final String ipVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				ip.setText(ipVal);
			}
		});
	}
	
	public String getIp()
	{
		return ip.getText();
	}
	
	public void setPort(final int portVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				port.setText(String.valueOf(portVal));
			}
		});
	}
	
	public int getPort() throws NumberFormatException
	{
 		return Integer.parseInt(port.getText());
	}
		
	public void setConnectionState(final ENetworkState state)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				switch(state)
				{
					case OFFLINE:
						status.setText("Offline");
						connect.setText("Connect");
						status.setBackground(new Color(255, 128, 128));
					break;
					case CONNECTING:
						status.setText("Connecting");
						connect.setText("Disconnect");
						status.setBackground(Color.CYAN);
					break;
					case ONLINE:
						status.setText("Online");
						connect.setText("Disconnect");
						status.setBackground(Color.GREEN);
					break;
				}
			}
		});
	}
	
	public void setCpuId(final String id)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				cpuId.setText(id);
			}
		});
	}
	
	public String getCpuId()
	{
		return cpuId.getText();
	}
	
	public void setMac(final String newMac)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				mac.setText(newMac);
			}
		});
	}
	
	public String getMac()
	{
		return mac.getText();
	}
	
	public void setServerPort(final int portVal)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				serverPort.setText(String.valueOf(portVal));
			}
		});
	}
	
	public int getServerPort() throws NumberFormatException
	{
 		return Integer.parseInt(serverPort.getText());
	}
	
	public void setUseUpdateAll(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				useUpdateAll.setSelected(enable);
			}
		});
	}
	
	public boolean getUseUpdateAll()
	{
		return useUpdateAll.isSelected();
	}
	
	public boolean getLogMovement()
	{
		return logMovement.isSelected();
	}
	
	public void setLogMovement(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				logMovement.setSelected(enable);
			}
		});
	}
	
	public boolean getLogKicker()
	{
		return logKicker.isSelected();
	}

	public void setLogKicker(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				logKicker.setSelected(enable);
			}
		});
	}

	public boolean getLogAccel()
	{
		return logAccel.isSelected();
	}
	
	public void setLogAccel(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				logAccel.setSelected(enable);
			}
		});
	}
	
	public boolean getLogIr()
	{
		return logIr.isSelected();
	}
	
	public void setLogIr(final boolean enable)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				logIr.setSelected(enable);
			}
		});
	}

	protected void notifySaveGeneral()
	{
		synchronized(observers)
		{
			for(ITigerBotMainPanelObserver o : observers)
			{
				o.onSaveGeneral();
			}
		}
	}

	protected void notifyConnectionChange()
	{
		synchronized(observers)
		{
			for(ITigerBotMainPanelObserver o : observers)
			{
				o.onConnectionChange();
			}
		}
	}
	
	private void notifySaveLogs()
	{
		synchronized(observers)
		{
			for (ITigerBotMainPanelObserver observer : observers)
			{
				observer.onSaveLogs();
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
}
