/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP.EConnectionState;

import net.miginfocom.swing.MigLayout;

/**
 * CT bot configuration panel.
 * 
 * @author AndreR
 * 
 */
public class CtBotPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -5810043650577472190L;
	
	private JTextField id = new JTextField();
	private JTextField name = new JTextField();
	private JTextField ip = new JTextField();
	private JTextField port = new JTextField();
	private JTextField status = new JTextField();
	private JButton connect = new JButton("Connect");
	private JTextField delay = new JTextField();
	private JTextField kp[] = new JTextField[2];
	private JTextField ki[] = new JTextField[2];
	private JTextField kd[] = new JTextField[2];
	private JTextField calTime = new JTextField();
	
	private List<ICtBotPanelObserver> observers = new ArrayList<ICtBotPanelObserver>();

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CtBotPanel()
	{
		setLayout(new MigLayout("fill"));
		
		kp[0] = new JTextField();
		kp[1] = new JTextField();
		kd[0] = new JTextField();
		kd[1] = new JTextField();
		ki[0] = new JTextField();
		ki[1] = new JTextField();
		
		status.setEditable(false);
		
		JButton saveGeneral = new JButton("Save");
		JButton savePid = new JButton("Save");
		JButton calibrate = new JButton("Calibrate");
		
		saveGeneral.addActionListener(new SaveGeneral());
		savePid.addActionListener(new SavePid());
		connect.addActionListener(new ConnectionChange());
		calibrate.addActionListener(new Calibrate());
		
		JPanel general = new JPanel(new MigLayout("fill", "[]10[50,fill] 20[]10[100,fill] 20[]10[100,fill] 20[]10[50,fill] 20[100,fill]"));
		JPanel network = new JPanel(new MigLayout("fill", "[50][100,fill]10[100,fill]"));
		JPanel pid = new JPanel(new MigLayout("fill, wrap", "[50][100,fill]10[100,fill]"));
		JPanel cal = new JPanel(new MigLayout("fill", "[110][60,fill]10[80,fill]"));
		
		general.setBorder(BorderFactory.createTitledBorder("General"));
		network.setBorder(BorderFactory.createTitledBorder("Network"));
		pid.setBorder(BorderFactory.createTitledBorder("PID"));
		cal.setBorder(BorderFactory.createTitledBorder("Calibration"));
		
		general.add(new JLabel("ID: "));
		general.add(id);
		general.add(new JLabel("Name:"));
		general.add(name);
		general.add(new JLabel("IP:"));
		general.add(ip);
		general.add(new JLabel("Port:"));
		general.add(port);
		general.add(saveGeneral);
		
		network.add(new JLabel("Status:"));
		network.add(status);
		network.add(connect);
		
		pid.add(new JLabel("Delay:"));
		pid.add(delay, "gapbottom 10");
		pid.add(new JLabel("Left"), "skip 2");
		pid.add(new JLabel("Right"));
		pid.add(new JLabel("Kp:"));
		pid.add(kp[0]);
		pid.add(kp[1]);
		pid.add(new JLabel("Ki:"));
		pid.add(ki[0]);
		pid.add(ki[1]);
		pid.add(new JLabel("Kd:"));
		pid.add(kd[0]);
		pid.add(kd[1]);
		pid.add(savePid, "span 3, align right, gaptop 10");
		
		cal.add(new JLabel("Calibration Time [ms]: "));
		cal.add(calTime);
		cal.add(calibrate);
		
		add(general, "wrap");
		add(network, "wrap");
		add(pid, "wrap");
		add(cal, "wrap");
		add(Box.createGlue(), "push");
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ICtBotPanelObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
	
	public void removeObserver(ICtBotPanelObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}
	
	public void setId(int id)
	{
		this.id.setText(String.valueOf(id));
	}
	
	public int getId() throws NumberFormatException
	{
		return Integer.parseInt(id.getText());
	}
	
	public void setName(String name)
	{
		this.name.setText(name);
	}
	
	public String getName()
	{
		return name.getText();
	}
	
	public void setIp(String ip)
	{
		this.ip.setText(ip);
	}
	
	public String getIp()
	{
		return ip.getText();
	}
	
	public void setPort(int port)
	{
		this.port.setText(String.valueOf(port));
	}
	
	public int getPort() throws NumberFormatException
	{
 		return Integer.parseInt(port.getText());
	}
		
	public void setConnectionState(EConnectionState state)
	{
		switch(state)
		{
			case DISCONNECTED:
				status.setText("Disconnected");
				connect.setText("Connect");
				status.setBackground(new Color(255, 128, 128));
			break;
			case CONNECTED:
				status.setText("Connected");
				connect.setText("Disconnect");
				status.setBackground(Color.GREEN);
			break;
			case CONNECTING:
				status.setText("Connecting...");
				connect.setText("Disconnect");
				status.setBackground(Color.CYAN);
			break;
		}
	}
	
	public void setDelay(int delay)
	{
		this.delay.setText(String.valueOf(delay));
	}
	
	public int getDelay() throws NumberFormatException
	{
		return Integer.parseInt(delay.getText());
	}
	
	public void setKp(float[] kp)
	{
		this.kp[0].setText(String.valueOf(kp[0]));
		this.kp[1].setText(String.valueOf(kp[1]));
	}
	
	public float[] getKp() throws NumberFormatException
	{
		float[] kp = new float[2];
		
		kp[0] = Float.parseFloat(this.kp[0].getText());
		kp[1] = Float.parseFloat(this.kp[1].getText());
		
		return kp;
	}

	public void setKi(float[] ki)
	{
		this.ki[0].setText(String.valueOf(ki[0]));
		this.ki[1].setText(String.valueOf(ki[1]));
	}

	public float[] getKi() throws NumberFormatException
	{
		float[] ki = new float[2];
		
		ki[0] = Float.parseFloat(this.ki[0].getText());
		ki[1] = Float.parseFloat(this.ki[1].getText());
		
		return ki;
	}

	public void setKd(float[] kd)
	{
		this.kd[0].setText(String.valueOf(kd[0]));
		this.kd[1].setText(String.valueOf(kd[1]));
	}

	public float[] getKd() throws NumberFormatException
	{
		float[] kd = new float[2];
		
		kd[0] = Float.parseFloat(this.kd[0].getText());
		kd[1] = Float.parseFloat(this.kd[1].getText());
		
		return kd;
	}
	
	public int getCalibrationTime() throws NumberFormatException
	{
		return Integer.parseInt(this.calTime.getText());
	}

	protected void notifySaveGeneral()
	{
		synchronized(observers)
		{
			for(ICtBotPanelObserver o : observers)
			{
				o.onSaveGeneral();
			}
		}
	}

	protected void notifySavePid()
	{
		synchronized(observers)
		{
			for(ICtBotPanelObserver o : observers)
			{
				o.onSavePid();
			}
		}
	}

	protected void notifyConnectionChange()
	{
		synchronized(observers)
		{
			for(ICtBotPanelObserver o : observers)
			{
				o.onConnectionChange();
			}
		}
	}
	
	private void notifyCalibrate()
	{
		synchronized(observers)
		{
			for (ICtBotPanelObserver observer : observers)
			{
				observer.onCalibrate();
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
	
	protected class SavePid implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifySavePid();
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
	
	protected class Calibrate implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			notifyCalibrate();
		}
	}
}
