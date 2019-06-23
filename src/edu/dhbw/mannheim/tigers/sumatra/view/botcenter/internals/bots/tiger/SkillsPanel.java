/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickMode;


/**
 * Control various skills from this panel.
 * 
 * @author AndreR, DanielW
 * 
 */
public class SkillsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID	= 5399293600256113771L;
	
	private JTextField								moveToXY_x			= null;
	private JTextField								moveToXY_y			= null;
	
	private JTextField								rotateAndMoveToXY_x			= null;
	private JTextField								rotateAndMoveToXY_y			= null;
	private JTextField								rotateAndMoveToXY_angle		= null;
	
	private JTextField								straightMoveTime	= null;
	
	private JTextField								rotateAngle			= null;
	
	private JTextField								kickLength			= null;
	private JComboBox									kickMode				= null;
	
	private JTextField								look_x				= null;
	private JTextField								look_y				= null;
	
	private JTextField								dribbleRPM			= null;
	
	private final List<ISkillsPanelObserver>	observers			= new ArrayList<ISkillsPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public SkillsPanel()
	{
		setLayout(new MigLayout("fill"));
		
		// MOVE TO XY
		JPanel moveToXYPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		
		moveToXY_x = new JTextField();
		moveToXY_y = new JTextField();
		
		JButton moveToXY = new JButton("Move to XY");
		moveToXY.addActionListener(new MoveToXY());
		
		moveToXYPanel.add(new JLabel("X:"));
		moveToXYPanel.add(moveToXY_x);
		moveToXYPanel.add(new JLabel("Y:"));
		moveToXYPanel.add(moveToXY_y);
		moveToXYPanel.add(moveToXY);
		
		moveToXYPanel.setBorder(BorderFactory.createTitledBorder("MoveToXY"));	
		
		// STRAIGHT MOVE
		JPanel straightMovePanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		straightMoveTime = new JTextField();
		
		JButton straightMove = new JButton("Straight Move");
		straightMove.addActionListener(new StraightMove());
		
		straightMovePanel.add(new JLabel("Time [ms]:"));
		straightMovePanel.add(straightMoveTime);
		straightMovePanel.add(straightMove);
		
		straightMovePanel.setBorder(BorderFactory.createTitledBorder("StraightMove"));
		
		// ROTATE
		JPanel rotatePanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		rotateAngle = new JTextField();
		
		JButton rotate = new JButton("rotate");
		rotate.addActionListener(new Rotate());
		
		rotatePanel.add(new JLabel("target angle (rad): "));
		rotatePanel.add(rotateAngle);
		rotatePanel.add(rotate);
		
		rotatePanel.setBorder(BorderFactory.createTitledBorder("Rotate"));
		

		// KICK
		JPanel kickPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		kickLength = new JTextField();
		kickLength.setToolTipText("no input > full shoot velocity");
		
		String[] modes = { "FORCE", "ARM", "DISARM" };
		kickMode = new JComboBox(modes);
		kickMode.setSelectedIndex(2);
		
		JButton kick = new JButton("Kick");
		kick.addActionListener(new Kick());
		JButton chip = new JButton("Chip");
		chip.addActionListener(new Chip());
		
		kickPanel.add(new JLabel("Kick Length: "));
		kickPanel.add(kickLength);
		kickPanel.add(kickMode);
		kickPanel.add(kick);
		kickPanel.add(chip);
		
		kickPanel.setBorder(BorderFactory.createTitledBorder("Kick"));
		

		// LOOKING
		JPanel aimPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		look_x = new JTextField();
		look_y = new JTextField();
		
		JButton lookAt = new JButton("look xy");
		lookAt.addActionListener(new LookAt());
		

		aimPanel.add(new JLabel("x: "));
		aimPanel.add(look_x);
		aimPanel.add(new JLabel("y: "));
		aimPanel.add(look_y);
		aimPanel.add(lookAt);
		
		aimPanel.setBorder(BorderFactory.createTitledBorder("LookAt"));
		
		// DRIBBLE
		JPanel dribblePanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		dribbleRPM = new JTextField();
		
		JButton dribble = new JButton("Dribble");
		dribble.addActionListener(new Dribble());
		
		dribblePanel.add(new JLabel("rpm: "));
		dribblePanel.add(dribbleRPM);
		dribblePanel.add(dribble);
		
		dribblePanel.setBorder(BorderFactory.createTitledBorder("Dribble"));
		
		// RotateAndMoveToXY
		JPanel rotateAndMoveToXYPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		
		rotateAndMoveToXY_x = new JTextField();
		rotateAndMoveToXY_y = new JTextField();
		rotateAndMoveToXY_angle = new JTextField();
		
		JButton rotateAndMoveToXY = new JButton("Move to XY and rotate");
		rotateAndMoveToXY.addActionListener(new RotateAndMoveToXY());
		
		rotateAndMoveToXYPanel.add(new JLabel("X:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToXY_x);
		rotateAndMoveToXYPanel.add(new JLabel("Y:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToXY_y);
		rotateAndMoveToXYPanel.add(new JLabel("Angle:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToXY_angle);

		rotateAndMoveToXYPanel.add(rotateAndMoveToXY);
		
		rotateAndMoveToXYPanel.setBorder(BorderFactory.createTitledBorder("RotateWhileMoving"));

		add(moveToXYPanel, "wrap");
		add(straightMovePanel, "wrap");
		add(rotatePanel, "wrap");
		add(kickPanel, "wrap");
		add(aimPanel, "wrap");
		add(dribblePanel, "wrap");
		add(rotateAndMoveToXYPanel, "wrap");
		add(Box.createGlue(), "push");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(ISkillsPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(ISkillsPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	private void notifyMoveToXY(float x, float y)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onMoveToXY(x, y);
			}
		}
	}
	
	private void notifyRotateAndMoveToXY(float x, float y, float angle)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onRotateAndMoveToXY(x, y, angle);
			}
		}
	}

	private void notifyStraightMove(int time)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onStraightMove(time);
			}
		}
	}
	

	private void notifyRotate(float targetAngle)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onRotate(targetAngle);
			}
		}
	}
	

	private void notifyKick(float kicklength, EKickMode mode, EKickDevice device)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onKick(kicklength, mode, device);
			}
		}
	}
	

	private void notifyLookAt(Vector2 lookAtTarget)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onLookAt(lookAtTarget);
			}
		}
	}
	

	private void notifyDribble(int rpm)
	{
		synchronized (observers)
		{
			for (ISkillsPanelObserver observer : observers)
			{
				observer.onDribble(rpm);
			}
		}
	}	
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class MoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			float x;
			float y;
			
			try
			{
				x = Float.parseFloat(moveToXY_x.getText());
				y = Float.parseFloat(moveToXY_y.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyMoveToXY(x, y);
		}
	}
	
	private class RotateAndMoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			float x;
			float y;
			float angle;
			try
			{
				x = Float.parseFloat(rotateAndMoveToXY_x.getText());
				y = Float.parseFloat(rotateAndMoveToXY_y.getText());
				angle = Float.parseFloat(rotateAndMoveToXY_angle.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyRotateAndMoveToXY(x, y, angle);
		}
	}
	
	private class StraightMove implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int time;
			
			try
			{
				time = Integer.parseInt(straightMoveTime.getText());
			} catch (NumberFormatException e)
			{
				return;
			}
			
			notifyStraightMove(time);
		}
	}
		
	private class Rotate implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			float angle = 0;
			
			try
			{
				angle = Float.valueOf((rotateAngle.getText()));
			} catch (NumberFormatException err)
			{
				return;
			}
			
			notifyRotate(angle);
		}
	}
	
	private class Kick implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float length;
			try
			{
				length = Float.valueOf(kickLength.getText());
			} catch (NumberFormatException err)
			{
				length = 0;
			}
			EKickMode mode;
			switch (kickMode.getSelectedIndex())
			{
				case 0:
					mode = EKickMode.FORCE;
					break;
				case 1:
					mode = EKickMode.ARM;
					break;
				default:
					mode = EKickMode.DISARM;
					break;
			}
			notifyKick(length, mode, EKickDevice.STRAIGHT);
		}
		
	}
	
	private class Chip implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float length;
			try
			{
				length = Float.valueOf(kickLength.getText());
			} catch (NumberFormatException err)
			{
				length = 0;
			}
			EKickMode mode;
			switch (kickMode.getSelectedIndex())
			{
				case 0:
					mode = EKickMode.FORCE;
					break;
				case 1:
					mode = EKickMode.ARM;
					break;
				default:
					mode = EKickMode.DISARM;
					break;
			}
			notifyKick(length, mode, EKickDevice.CHIP);
			
		}
		
	}
	
	private class LookAt implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			float x;
			float y;
			try
			{
				x = Float.valueOf(look_x.getText());
				y = Float.valueOf(look_y.getText());
			} catch (NumberFormatException err)
			{
				return;
			}
			notifyLookAt(new Vector2(x, y));
			
		}
		
	}
	
	
	private class Dribble implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int rpm;
			try
			{
				rpm = Integer.valueOf(dribbleRPM.getText());
				dribbleRPM.setBackground(Color.WHITE);
			} catch (NumberFormatException err)
			{
				dribbleRPM.setBackground(Color.RED);
				return;
			}
			notifyDribble(rpm);
		}
		
	}
	
}
