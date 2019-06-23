/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.OverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationV2Panel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader.FirmwareUpdatePanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * New v2015 botcenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCenterPanelV2 extends JPanel implements ISumatraView
{
	/**  */
	private static final long										serialVersionUID	= -7749317503520671162L;
	
	private final OverviewPanel									botOverview;
	private final BotConfigOverviewPanel						botOverviewPanel;
	private final Map<EBaseStation, BaseStationV2Panel>	baseStationPanels	= new EnumMap<>(EBaseStation.class);
	private final FirmwareUpdatePanel							firmwareUpdatePanel;
	private final BcFeaturesPanel									featurePanel;
	private final JTabbedPane										tabbedPane;
	
	
	/**
	 * 
	 */
	public BotCenterPanelV2()
	{
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		botOverview = new OverviewPanel();
		botOverviewPanel = new BotConfigOverviewPanel();
		firmwareUpdatePanel = new FirmwareUpdatePanel();
		featurePanel = new BcFeaturesPanel();
		
		tabbedPane.addTab("Bot Overview", setupScrollPane(botOverview));
		tabbedPane.addTab("Bot Details", botOverviewPanel);
		
		int i = 0;
		for (EBaseStation ebs : EBaseStation.values())
		{
			BaseStationV2Panel panel = new BaseStationV2Panel();
			baseStationPanels.put(ebs, panel);
			tabbedPane.addTab("BaseStation " + i, setupScrollPane(panel));
			i++;
		}
		
		tabbedPane.addTab("Update Firmware", setupScrollPane(firmwareUpdatePanel));
		tabbedPane.addTab("Features", setupScrollPane(featurePanel));
	}
	
	
	/**
	 */
	public void showPanel()
	{
		add(tabbedPane, BorderLayout.CENTER);
		repaint();
	}
	
	
	/**
	 */
	public void hidePanel()
	{
		remove(tabbedPane);
		repaint();
	}
	
	
	private Component setupScrollPane(final Component comp)
	{
		JScrollPane scrollPane = new JScrollPane(comp);
		scrollPane.setPreferredSize(new Dimension(0, 0));
		return scrollPane;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return Collections.emptyList();
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	/**
	 * @return the botOverviewPanel
	 */
	public BotConfigOverviewPanel getBotOverviewPanel()
	{
		return botOverviewPanel;
	}
	
	
	/**
	 * @return the baseStationPanel
	 */
	public Map<EBaseStation, BaseStationV2Panel> getBaseStationPanels()
	{
		return baseStationPanels;
	}
	
	
	/**
	 * @return the firmwareUpdatePanel
	 */
	public FirmwareUpdatePanel getFirmwareUpdatePanel()
	{
		return firmwareUpdatePanel;
	}
	
	
	/**
	 * @return the botSummary
	 */
	public final OverviewPanel getOverviewPanel()
	{
		return botOverview;
	}
	
	
	/**
	 * @return the featurePanel
	 */
	public final BcFeaturesPanel getFeaturePanel()
	{
		return featurePanel;
	}
}
