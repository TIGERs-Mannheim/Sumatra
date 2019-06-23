/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;

/**
 * Base class for all summaries in the OverviewPanel.
 * 
 * @author AndreR
 * 
 */
public class BaseSummary extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 7977184142933042070L;
	private JTextField id;
	private JTextField name;
	private JTextField ip;
	private JTextField port;
	private JTextField type;
	private JTextField status;
	private JPanel network;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BaseSummary(String typename)
	{
		setLayout(new MigLayout("fill", "[]10[]", "0[]0"));
		
		JPanel general = new JPanel(new MigLayout("fill", "[30]10[100,fill]20[30]10[50,fill]20[30]10[100,fill]"));
		network = new JPanel(new MigLayout("fill", "[30]10[100,fill]20[30]10[50,fill]20[30]10[100,fill]"));
		
		type = new JTextField(typename);
		type.setEditable(false);
		id = new JTextField();
		id.setEditable(false);
		name = new JTextField();
		name.setEditable(false);
		ip = new JTextField();
		ip.setEditable(false);
		port = new JTextField();
		port.setEditable(false);
		status = new JTextField();
		status.setEditable(false);

		general.add(new JLabel("Type:"));
		general.add(type);
		general.add(new JLabel("SSL ID:"));
		general.add(id);
		general.add(new JLabel("Name:"));
		general.add(name);
		network.add(new JLabel("IP:"));
		network.add(ip);
		network.add(new JLabel("Port:"));
		network.add(port);
		network.add(new JLabel("Status:"));
		network.add(status);
		
		add(general);
		add(network);

		general.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "General"));
		setNetworkState(ENetworkState.OFFLINE);
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setId(int id)
	{
		this.id.setText(String.valueOf(id));
	}
	
	public void setBotName(String name)
	{
		this.name.setText(name);
	}
	
	public void setIP(String ip)
	{
		this.ip.setText(ip);
	}
	
	public void setPort(int port)
	{
		this.port.setText(String.valueOf(port));
	}
	
	public void setNetworkState(ENetworkState state)
	{
		String text = "";
		Color borderColor = null;
		
		switch(state)
		{
			case OFFLINE:
				text = "Offline";
				borderColor = Color.RED;
			break;
			case ONLINE:
				text = "Online";
				borderColor = Color.GREEN;
			break;
		}
		
		status.setText(text);
		network.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borderColor), "Network"));
	}
}
