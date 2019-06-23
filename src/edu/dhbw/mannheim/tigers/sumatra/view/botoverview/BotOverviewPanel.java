/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botoverview;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXTable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotAiInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botoverview.BotOverviewTableModel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


/**
 * Main Panel for Bot Overview
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewPanel extends JPanel implements ISumatraView
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long				serialVersionUID	= -8536401073164037476L;
	private final BotOverviewTableModel	model;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public BotOverviewPanel()
	{
		setLayout(new MigLayout("fill, inset 0"));
		model = new BotOverviewTableModel();
		JXTable table = new JXTable(model);
		table.setColumnControlVisible(true);
		table.updateUI();
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, "top, grow");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param lastAIInfoframe
	 */
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				IBotIDMap<TrackedTigerBot> bots = lastAIInfoframe.getWorldFrame().getTigerBotsAvailable();
				
				// remove vanished bots
				for (BotID botId : model.getBots())
				{
					if ((botId.getTeamColor() == lastAIInfoframe.getTeamColor()) && !bots.containsKey(botId))
					{
						model.removeBot(botId);
					}
				}
				
				// add ai info for all known bots
				for (TrackedTigerBot bot : bots.values())
				{
					BotID botId = bot.getId();
					BotAiInformation aiInfo = lastAIInfoframe.getTacticalField().getBotAiInformation().get(botId);
					if (aiInfo != null)
					{
						BotOverviewColumn column = new BotOverviewColumn(aiInfo);
						model.putBot(botId, column);
					} else
					{
						model.removeBot(botId);
					}
				}
			}
		});
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final List<JMenu> menus = new ArrayList<JMenu>();
		return menus;
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
