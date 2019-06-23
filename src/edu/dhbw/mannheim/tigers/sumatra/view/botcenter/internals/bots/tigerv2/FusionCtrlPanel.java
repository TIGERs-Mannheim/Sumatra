/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.04.2013
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/**
 * Configure sensor fusion and control parameters.
 * 
 * @author AndreR
 * 
 */
public class FusionCtrlPanel extends JPanel
{
	/** */
	public interface IFusionCtrlPanelObserver
	{
		/**
		 * State uncertainties updated.
		 * 
		 * @param unc
		 */
		void onNewStateUncertainties(StateUncertainties unc);
		
		
		/**
		 * Sensor uncertainties updated.
		 * 
		 * @param unc
		 */
		void onNewSensorUncertainties(SensorUncertainties unc);
		
		
		/**
		 * 
		 * @param pos
		 * @param vel
		 * @param acc
		 */
		void onNewControllerParams(PIDParametersXYW pos, PIDParametersXYW vel, PIDParametersXYW acc);
		
		
		/** */
		void onCopyCtrlValuesToAll();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID	= 595449253382090964L;
	
	private JTextField[]									stateXY				= new JTextField[3];
	private JTextField[]									stateW				= new JTextField[3];
	private JTextField[]									sensorVision		= new JTextField[2];
	private JTextField[]									sensorEnc			= new JTextField[2];
	private JTextField[]									sensorAccGyro		= new JTextField[2];
	private JTextField[]									sensorMotor			= new JTextField[2];
	
	private JTextField[]									ctrlPosXY			= new JTextField[3];
	private JTextField[]									ctrlPosW				= new JTextField[3];
	private JTextField[]									ctrlVelXY			= new JTextField[3];
	private JTextField[]									ctrlVelW				= new JTextField[3];
	private JTextField[]									ctrlAccXY			= new JTextField[3];
	private JTextField[]									ctrlAccW				= new JTextField[3];
	
	
	private final List<IFusionCtrlPanelObserver>	observers			= new ArrayList<IFusionCtrlPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public FusionCtrlPanel()
	{
		setLayout(new MigLayout("wrap 1"));
		
		for (int i = 0; i < 2; i++)
		{
			sensorVision[i] = new JTextField();
			sensorEnc[i] = new JTextField();
			sensorAccGyro[i] = new JTextField();
			sensorMotor[i] = new JTextField();
		}
		
		for (int i = 0; i < 3; i++)
		{
			stateXY[i] = new JTextField();
			stateW[i] = new JTextField();
			ctrlPosXY[i] = new JTextField();
			ctrlPosW[i] = new JTextField();
			ctrlVelXY[i] = new JTextField();
			ctrlVelW[i] = new JTextField();
			ctrlAccXY[i] = new JTextField();
			ctrlAccW[i] = new JTextField();
		}
		
		JButton saveStateButton = new JButton("Save");
		saveStateButton.addActionListener(new SaveNewState());
		
		JButton copyToAll = new JButton("Copy values to all bots");
		copyToAll.addActionListener(new CopyToAll());
		
		final JPanel statePanel = new JPanel(new MigLayout("fill, wrap 3", "[80]10[50,fill]10[50,fill]"));
		statePanel.add(new JLabel(""));
		statePanel.add(new JLabel("XY"));
		statePanel.add(new JLabel("W"));
		statePanel.add(new JLabel("Position"));
		statePanel.add(stateXY[0]);
		statePanel.add(stateW[0]);
		statePanel.add(new JLabel("Velocity"));
		statePanel.add(stateXY[1]);
		statePanel.add(stateW[1]);
		statePanel.add(new JLabel("Acceleration"));
		statePanel.add(stateXY[2]);
		statePanel.add(stateW[2]);
		statePanel.add(saveStateButton, "span 4");
		statePanel.setBorder(BorderFactory.createTitledBorder("State Uncertainties"));
		
		JButton saveSensorButton = new JButton("Save");
		saveSensorButton.addActionListener(new SaveNewSensors());
		
		final JPanel sensorPanel = new JPanel(new MigLayout("fill, wrap 4", "[80]10[50,fill]20[80]10[50,fill]"));
		
		sensorPanel.add(new JLabel(""));
		sensorPanel.add(new JLabel("XY"));
		sensorPanel.add(new JLabel(""));
		sensorPanel.add(new JLabel("W"));
		
		sensorPanel.add(new JLabel("Vision"));
		sensorPanel.add(sensorVision[0]);
		sensorPanel.add(new JLabel("Vision"));
		sensorPanel.add(sensorVision[1]);
		
		sensorPanel.add(new JLabel("Encoder"));
		sensorPanel.add(sensorEnc[0]);
		sensorPanel.add(new JLabel("Encoder"));
		sensorPanel.add(sensorEnc[1]);
		
		sensorPanel.add(new JLabel("Accelerometer"));
		sensorPanel.add(sensorAccGyro[0]);
		sensorPanel.add(new JLabel("Gyroscope"));
		sensorPanel.add(sensorAccGyro[1]);
		
		sensorPanel.add(new JLabel("Motor"));
		sensorPanel.add(sensorMotor[0]);
		sensorPanel.add(new JLabel("Motor"));
		sensorPanel.add(sensorMotor[1]);
		
		sensorPanel.add(saveSensorButton, "span 4");
		sensorPanel.setBorder(BorderFactory.createTitledBorder("Sensor Uncertainties"));
		
		JButton savePIDButton = new JButton("Save");
		savePIDButton.addActionListener(new SaveControl());
		
		final JPanel ctrlPanel = new JPanel(new MigLayout("fill, wrap 7",
				"[50]10[30,fill]10[30,fill]20[30,fill]10[30,fill]20[30,fill]10[30,fill]"));
		
		ctrlPanel.add(new JLabel(""));
		ctrlPanel.add(new JLabel("Position"), "span 2");
		ctrlPanel.add(new JLabel("Velocity"), "span 2");
		ctrlPanel.add(new JLabel("Acceleration"), "span 2");
		
		ctrlPanel.add(new JLabel(""));
		ctrlPanel.add(new JLabel("XY"));
		ctrlPanel.add(new JLabel("W"));
		ctrlPanel.add(new JLabel("XY"));
		ctrlPanel.add(new JLabel("W"));
		ctrlPanel.add(new JLabel("XY"));
		ctrlPanel.add(new JLabel("W"));
		
		ctrlPanel.add(new JLabel("P"));
		ctrlPanel.add(ctrlPosXY[0]);
		ctrlPanel.add(ctrlPosW[0]);
		ctrlPanel.add(ctrlVelXY[0]);
		ctrlPanel.add(ctrlVelW[0]);
		ctrlPanel.add(ctrlAccXY[0]);
		ctrlPanel.add(ctrlAccW[0]);
		
		ctrlPanel.add(new JLabel("I"));
		ctrlPanel.add(ctrlPosXY[1]);
		ctrlPanel.add(ctrlPosW[1]);
		ctrlPanel.add(ctrlVelXY[1]);
		ctrlPanel.add(ctrlVelW[1]);
		ctrlPanel.add(ctrlAccXY[1]);
		ctrlPanel.add(ctrlAccW[1]);
		
		ctrlPanel.add(new JLabel("D"));
		ctrlPanel.add(ctrlPosXY[2]);
		ctrlPanel.add(ctrlPosW[2]);
		ctrlPanel.add(ctrlVelXY[2]);
		ctrlPanel.add(ctrlVelW[2]);
		ctrlPanel.add(ctrlAccXY[2]);
		ctrlPanel.add(ctrlAccW[2]);
		
		ctrlPanel.add(savePIDButton, "span 7");
		
		ctrlPanel.setBorder(BorderFactory.createTitledBorder("Controller"));
		
		add(statePanel);
		add(sensorPanel);
		add(ctrlPanel);
		add(copyToAll);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IFusionCtrlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IFusionCtrlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewStateUncertainties(StateUncertainties unc)
	{
		synchronized (observers)
		{
			for (IFusionCtrlPanelObserver observer : observers)
			{
				observer.onNewStateUncertainties(unc);
			}
		}
	}
	
	
	private void notifyNewSensorUncertainties(SensorUncertainties unc)
	{
		synchronized (observers)
		{
			for (IFusionCtrlPanelObserver observer : observers)
			{
				observer.onNewSensorUncertainties(unc);
			}
		}
	}
	
	
	private void notifyNewControllerParams(PIDParametersXYW pos, PIDParametersXYW vel, PIDParametersXYW acc)
	{
		synchronized (observers)
		{
			for (IFusionCtrlPanelObserver observer : observers)
			{
				observer.onNewControllerParams(pos, vel, acc);
			}
		}
	}
	
	
	private void notifyCopyCtrlValuesToAll()
	{
		synchronized (observers)
		{
			for (IFusionCtrlPanelObserver observer : observers)
			{
				observer.onCopyCtrlValuesToAll();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Update sensor fusion parameters.
	 * 
	 * @param params
	 */
	public void setSensorFusionParams(final SensorFusionParameters params)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				stateXY[0].setText(Float.toString(params.getEx().getPos().x()));
				stateXY[1].setText(Float.toString(params.getEx().getVel().x()));
				stateXY[2].setText(Float.toString(params.getEx().getAcc().x()));
				stateW[0].setText(Float.toString(params.getEx().getPos().z()));
				stateW[1].setText(Float.toString(params.getEx().getVel().z()));
				stateW[2].setText(Float.toString(params.getEx().getAcc().z()));
				sensorVision[0].setText(Float.toString(params.getEz().getVision().x()));
				sensorVision[1].setText(Float.toString(params.getEz().getVision().z()));
				sensorEnc[0].setText(Float.toString(params.getEz().getEncoder().x()));
				sensorEnc[1].setText(Float.toString(params.getEz().getEncoder().z()));
				sensorAccGyro[0].setText(Float.toString(params.getEz().getAccelerometer().x()));
				sensorAccGyro[1].setText(Float.toString(params.getEz().getGyroscope()));
				sensorMotor[0].setText(Float.toString(params.getEz().getMotor().x()));
				sensorMotor[1].setText(Float.toString(params.getEz().getMotor().z()));
			}
		});
	}
	
	
	/**
	 * Update controller params.
	 * 
	 * @param params
	 */
	public void setControllerParams(final ControllerParameters params)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				ctrlPosXY[0].setText(Float.toString(params.getPos().getX().getKp()));
				ctrlPosXY[1].setText(Float.toString(params.getPos().getX().getKi()));
				ctrlPosXY[2].setText(Float.toString(params.getPos().getX().getKd()));
				ctrlPosW[0].setText(Float.toString(params.getPos().getW().getKp()));
				ctrlPosW[1].setText(Float.toString(params.getPos().getW().getKi()));
				ctrlPosW[2].setText(Float.toString(params.getPos().getW().getKd()));
				
				ctrlVelXY[0].setText(Float.toString(params.getVel().getX().getKp()));
				ctrlVelXY[1].setText(Float.toString(params.getVel().getX().getKi()));
				ctrlVelXY[2].setText(Float.toString(params.getVel().getX().getKd()));
				ctrlVelW[0].setText(Float.toString(params.getVel().getW().getKp()));
				ctrlVelW[1].setText(Float.toString(params.getVel().getW().getKi()));
				ctrlVelW[2].setText(Float.toString(params.getVel().getW().getKd()));
				
				ctrlAccXY[0].setText(Float.toString(params.getAcc().getX().getKp()));
				ctrlAccXY[1].setText(Float.toString(params.getAcc().getX().getKi()));
				ctrlAccXY[2].setText(Float.toString(params.getAcc().getX().getKd()));
				ctrlAccW[0].setText(Float.toString(params.getAcc().getW().getKp()));
				ctrlAccW[1].setText(Float.toString(params.getAcc().getW().getKi()));
				ctrlAccW[2].setText(Float.toString(params.getAcc().getW().getKd()));
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class SaveNewState implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			StateUncertainties unc = new StateUncertainties();
			
			try
			{
				unc.setPos(new Vector3(Float.parseFloat(stateXY[0].getText()), Float.parseFloat(stateXY[0].getText()),
						Float.parseFloat(stateW[0].getText())));
				
				unc.setVel(new Vector3(Float.parseFloat(stateXY[1].getText()), Float.parseFloat(stateXY[1].getText()),
						Float.parseFloat(stateW[1].getText())));
				
				unc.setAcc(new Vector3(Float.parseFloat(stateXY[2].getText()), Float.parseFloat(stateXY[2].getText()),
						Float.parseFloat(stateW[2].getText())));
				
				notifyNewStateUncertainties(unc);
			} catch (NumberFormatException ex)
			{
			}
		}
	}
	
	private class SaveNewSensors implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SensorUncertainties unc = new SensorUncertainties();
			
			try
			{
				unc.setVision(new Vector3(Float.parseFloat(sensorVision[0].getText()), Float.parseFloat(sensorVision[0]
						.getText()), Float.parseFloat(sensorVision[1].getText())));
				
				unc.setEncoder(new Vector3(Float.parseFloat(sensorEnc[0].getText()), Float.parseFloat(sensorEnc[0]
						.getText()), Float.parseFloat(sensorEnc[1].getText())));
				
				unc.setAccelerometer(new Vector2(Float.parseFloat(sensorAccGyro[0].getText()), Float
						.parseFloat(sensorAccGyro[0].getText())));
				unc.setGyroscope(Float.parseFloat(sensorAccGyro[1].getText()));
				
				unc.setMotor(new Vector3(Float.parseFloat(sensorMotor[0].getText()), Float.parseFloat(sensorMotor[0]
						.getText()), Float.parseFloat(sensorMotor[1].getText())));
				
				notifyNewSensorUncertainties(unc);
			} catch (NumberFormatException ex)
			{
			}
		}
	}
	
	private class SaveControl implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float xy[] = new float[3]; // P, I, D
			float w[] = new float[3]; // P, I, D
			
			PIDParametersXYW pos = new PIDParametersXYW();
			PIDParametersXYW vel = new PIDParametersXYW();
			PIDParametersXYW acc = new PIDParametersXYW();
			
			try
			{
				for (int i = 0; i < 3; i++)
				{
					xy[i] = Float.parseFloat(ctrlPosXY[i].getText());
					w[i] = Float.parseFloat(ctrlPosW[i].getText());
				}
				
				pos.setX(new PIDParameters(xy[0], xy[1], xy[2]));
				pos.setY(new PIDParameters(xy[0], xy[1], xy[2]));
				pos.setW(new PIDParameters(w[0], w[1], w[2]));
			} catch (NumberFormatException ex)
			{
			}
			
			try
			{
				for (int i = 0; i < 3; i++)
				{
					xy[i] = Float.parseFloat(ctrlVelXY[i].getText());
					w[i] = Float.parseFloat(ctrlVelW[i].getText());
				}
				
				vel.setX(new PIDParameters(xy[0], xy[1], xy[2]));
				vel.setY(new PIDParameters(xy[0], xy[1], xy[2]));
				vel.setW(new PIDParameters(w[0], w[1], w[2]));
			} catch (NumberFormatException ex)
			{
			}
			
			try
			{
				for (int i = 0; i < 3; i++)
				{
					xy[i] = Float.parseFloat(ctrlAccXY[i].getText());
					w[i] = Float.parseFloat(ctrlAccW[i].getText());
				}
				
				acc.setX(new PIDParameters(xy[0], xy[1], xy[2]));
				acc.setY(new PIDParameters(xy[0], xy[1], xy[2]));
				acc.setW(new PIDParameters(w[0], w[1], w[2]));
			} catch (NumberFormatException ex)
			{
			}
			
			notifyNewControllerParams(pos, vel, acc);
		}
	}
	
	private class CopyToAll implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			notifyCopyCtrlValuesToAll();
		}
	}
}
