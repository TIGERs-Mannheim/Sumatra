/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import javax.swing.*;

import edu.tigers.autoref.view.main.IActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.components.BasePanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Lukas Magel
 */
public class ActiveEnginePanel extends BasePanel<IActiveEnginePanelObserver> implements IActiveEnginePanel
{
	/**  */
	private static final long	serialVersionUID	= -8855537755362886421L;
	
	private JLabel					followUpLabel;
	private JLabel					teamInFavorLabel;
	private JLabel					positionLabel;
	private JButton				proceedButton;
	private JButton				resetButton;
	
	
	/**
	 * Create new instance
	 */
	public ActiveEnginePanel()
	{
		setLayout(new MigLayout("fill", "[50%][50%]", ""));
		setBorder(BorderFactory.createTitledBorder("Engine"));
		
		followUpLabel = new JLabel("");
		teamInFavorLabel = new JLabel("");
		positionLabel = new JLabel("");
		
		proceedButton = new JButton("Proceed");
		proceedButton.addActionListener(e -> informObserver(IActiveEnginePanelObserver::onProceedButtonPressed));
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> informObserver(IActiveEnginePanelObserver::onResetButtonPressed));
		
		add(followUpLabel, "span, wrap");
		add(teamInFavorLabel, "span, wrap");
		add(positionLabel, "span, wrap");
		add(proceedButton, "grow, split 1");
		add(resetButton, "grow");
	}
	
	
	@Override
	public void setNextAction(final FollowUpAction action)
	{
		String actionStr = "";
		String teamStr = "";
		String posStr = "";
		if (action != null)
		{
			actionStr = action.getActionType().toString();
			teamStr = action.getTeamInFavor().toString();
			posStr = action.getNewBallPosition().map(pos -> String.format("%.3f | %.3f", pos.x(), pos.y())).orElse("");
		}
		
		followUpLabel.setText("Next action: " + actionStr);
		teamInFavorLabel.setText("Team: " + teamStr);
		positionLabel.setText("Position: " + posStr);
	}
	
	
	@Override
	public void setProceedButtonEnabled(final boolean enabled)
	{
		proceedButton.setEnabled(enabled);
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		if (!enabled)
		{
			setNextAction(null);
		}
		proceedButton.setEnabled(false);
		resetButton.setEnabled(enabled);
	}
}
