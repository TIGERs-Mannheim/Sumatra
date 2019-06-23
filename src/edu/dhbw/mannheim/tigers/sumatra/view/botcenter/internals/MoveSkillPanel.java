/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill.AccelerationSkillPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill.AccelerationSkillPanel.IAccelerationSkillPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill.PIDSkillPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill.PIDSkillPanel.IPIDSkillPanelObserver;


/**
 * TODO osteinbrecher, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author osteinbrecher
 * 
 */
public class MoveSkillPanel extends JPanel
{
	public interface IMoveSkillPanelObserver extends IAccelerationSkillPanelObserver, IPIDSkillPanelObserver
	{
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long			serialVersionUID	= 909613778008821749L;
	
	private AccelerationSkillPanel	accPanel				= null;
	private PIDSkillPanel				pidPanel				= null;
	
	private JScrollPane					scrollPane			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public MoveSkillPanel()
	{
		this.setLayout(new BorderLayout());
		
		JPanel combinePanel = new JPanel();
		combinePanel.setLayout(new MigLayout("fill", "", ""));
		
		this.accPanel = new AccelerationSkillPanel();
		this.pidPanel = new PIDSkillPanel();
		
		combinePanel.add(accPanel, "wrap");
		combinePanel.add(pidPanel, "wrap");
		
		scrollPane = new JScrollPane(combinePanel);
		this.add(scrollPane, BorderLayout.CENTER);
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IMoveSkillPanelObserver observer)
	{
		accPanel.addObserver(observer);
		pidPanel.addObserver(observer);
	}
	
	
	public void removeObserver(IMoveSkillPanelObserver observer)
	{
		accPanel.removeObserver(observer);
		pidPanel.removeObserver(observer);
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the accPanel
	 */
	public AccelerationSkillPanel getAccPanel()
	{
		return accPanel;
	}
	

	/**
	 * @return the pidPanel
	 */
	public PIDSkillPanel getPidPanel()
	{
		return pidPanel;
	}
	
}
