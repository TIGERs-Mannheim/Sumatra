/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill;

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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.PIDFacade;


/**
 * TODO osteinbrecher, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class PIDSkillPanel extends JPanel
{
	
	/**
	 * Interface for {@link PIDSkillPanel}
	 */
	public interface IPIDSkillPanelObserver
	{
		public void onNewPIDFacade(PIDFacade pidFacade);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID		= -1643469526102813517L;
	
	private final List<IPIDSkillPanelObserver>	observers				= new ArrayList<IPIDSkillPanelObserver>();
	
	private final JTextField	wKp;
	private final JTextField	wKi;
	private final JTextField	wKd;
	private final JTextField	wMaxOut;
	private final JTextField	wSlewRate;
	private final JTextField	lKp;
	private final JTextField	lKi;
	private final JTextField	lKd;
	private final JTextField	lMaxOut;
	private final JTextField	lSlewRate;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PIDSkillPanel()
	{
		setLayout(new MigLayout("fill, wrap 6", "[80]20[50,fill]10[50,fill]10[50,fill]10[50,fill]10[50,fill]"));
		setBorder(BorderFactory.createTitledBorder("PIDs"));
		
		wKp = new JTextField();
		wKi = new JTextField();
		wKd = new JTextField();
		wMaxOut = new JTextField();
		wSlewRate = new JTextField();
		lKp = new JTextField();
		lKi = new JTextField();
		lKd = new JTextField();
		lMaxOut = new JTextField();
		lSlewRate = new JTextField();
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new PIDSaveAction());
		
		add(new JLabel("Kp"), "skip");
		add(new JLabel("Ki"));
		add(new JLabel("Kd"));
		add(new JLabel("maxOut"));
		add(new JLabel("slewRate"));
		
		add(new JLabel("Velocity"));
		add(lKp);
		add(lKi);
		add(lKd);
		add(lMaxOut);
		add(lSlewRate);
		
		add(new JLabel("Orientation"));
		add(wKp);
		add(wKi);
		add(wKd);
		add(wMaxOut);
		add(wSlewRate);
		
		add(saveButton, "span 6");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void addObserver(IPIDSkillPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IPIDSkillPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	public PIDFacade getPIDFacade()
	{
		PIDFacade pidFacade = new PIDFacade();
		
		try
		{
			pidFacade.velocity.p = Float.parseFloat(lKp.getText());
			pidFacade.velocity.i = Float.parseFloat(lKi.getText());
			pidFacade.velocity.d = Float.parseFloat(lKd.getText());
			pidFacade.velocity.maxOutput = Float.parseFloat(lMaxOut.getText());
			pidFacade.velocity.slewRate = Float.parseFloat(lSlewRate.getText());
			pidFacade.orientation.p = Float.parseFloat(wKp.getText());
			pidFacade.orientation.i = Float.parseFloat(wKi.getText());
			pidFacade.orientation.d = Float.parseFloat(wKd.getText());
			pidFacade.orientation.maxOutput = Float.parseFloat(wMaxOut.getText());
			pidFacade.orientation.slewRate = Float.parseFloat(wSlewRate.getText());
		}

		catch (NumberFormatException ex)
		{
		}

		return pidFacade;
	}
	
	public void setPIDFacade(final PIDFacade fac)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				wKp.setText("" + fac.orientation.p);
				wKi.setText("" + fac.orientation.i);
				wKd.setText("" + fac.orientation.d);
				wMaxOut.setText("" + fac.orientation.maxOutput);
				wSlewRate.setText("" + fac.orientation.slewRate);

				lKp.setText("" + fac.velocity.p);
				lKi.setText("" + fac.velocity.i);
				lKd.setText("" + fac.velocity.d);
				lMaxOut.setText("" + fac.velocity.maxOutput);
				lSlewRate.setText("" + fac.velocity.slewRate);

			}
		});
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void notifyPIDFacade(PIDFacade fac)
	{
		synchronized (observers)
		{
			for (IPIDSkillPanelObserver observer : observers)
			{
				observer.onNewPIDFacade(fac);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public class PIDSaveAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			notifyPIDFacade(getPIDFacade());
		}
		
	}
	
}
