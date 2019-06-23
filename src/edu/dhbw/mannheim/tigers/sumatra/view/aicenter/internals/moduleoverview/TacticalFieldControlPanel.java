/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 5, 2011
 * Author(s): NicolaiO
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * This panel can be used to get all information of the TacticalField.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author NicolaiO
 */
public class TacticalFieldControlPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID				= 2823710107966028875L;
	
	private BallPossession		lastBallPossession			= new BallPossession();
	private ETeam					lastClosestTeam				= null;
	
	private JTextField			ballPossession					= null;
	private JTextField			ballPossessionOpponentId	= null;
	private JTextField			ballPossessionTigersId		= null;
	private JTextField			closestTeamToBall				= null;
	private JTextField			botLastTouchedBall			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TacticalFieldControlPanel()
	{
		setLayout(new MigLayout("wrap"));
		setBorder(BorderFactory.createTitledBorder("Tactical Field Control Panel"));
		
		ballPossession = new JTextField();
		ballPossession.setEditable(false);
		ballPossession.setBackground(Color.WHITE);
		
		ballPossessionTigersId = new JTextField();
		ballPossessionTigersId.setEditable(false);
		ballPossessionTigersId.setBackground(Color.WHITE);
		
		ballPossessionOpponentId = new JTextField();
		ballPossessionOpponentId.setEditable(false);
		ballPossessionOpponentId.setBackground(Color.WHITE);
		
		closestTeamToBall = new JTextField();
		closestTeamToBall.setEditable(false);
		closestTeamToBall.setBackground(Color.WHITE);
		
		botLastTouchedBall = new JTextField();
		botLastTouchedBall.setEditable(false);
		botLastTouchedBall.setBackground(Color.WHITE);
		
		final JPanel ballPossessionPanel = new JPanel(new MigLayout("fill, inset 0",
				"[60,fill]10[60,fill]10[30,fill]10[30,fill]"));
		ballPossessionPanel.add(new JLabel("Ball possession:"));
		ballPossessionPanel.add(ballPossession);
		ballPossessionPanel.add(new JLabel("Tiger:"));
		ballPossessionPanel.add(ballPossessionTigersId);
		ballPossessionPanel.add(new JLabel("Opponent:"));
		ballPossessionPanel.add(ballPossessionOpponentId);
		
		final JPanel closestTeamBallPanel = new JPanel(new MigLayout("fill, inset 0", "[60,fill]10[100,fill]"));
		closestTeamBallPanel.add(new JLabel("Closest Team to Ball:"));
		closestTeamBallPanel.add(closestTeamToBall);
		
		final JPanel botLastTouchedBallPanel = new JPanel(new MigLayout("fill, inset 0", "[60,fill]10[250,fill]"));
		botLastTouchedBallPanel.add(new JLabel("Bot last touched ball:"));
		botLastTouchedBallPanel.add(botLastTouchedBall);
		
		
		add(ballPossessionPanel);
		add(closestTeamBallPanel);
		add(botLastTouchedBallPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public void clearView()
	{
		ballPossession.setText("");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param possession
	 */
	public void setBallPossession(final BallPossession possession)
	{
		if (!lastBallPossession.isEqual(possession))
		{
			ballPossession.setText(possession.getEBallPossession().toString());
			ballPossessionTigersId.setText((possession.getTigersId().isUninitializedID()) ? "" : Integer
					.toString(possession.getTigersId().getNumber()));
			ballPossessionOpponentId.setText((possession.getOpponentsId().isUninitializedID()) ? "" : Integer
					.toString(possession.getOpponentsId().getNumber()));
			lastBallPossession = possession;
		}
	}
	
	
	/**
	 * @param team
	 */
	public void setClosestTeamToBall(final ETeam team)
	{
		if (lastClosestTeam != team)
		{
			closestTeamToBall.setText(team.toString());
			lastClosestTeam = team;
		}
	}
	
	
	/**
	 * @param botLastTouchedBall the botLastTouchedBall to set
	 */
	public void setBotLastTouchedBall(final BotID botLastTouchedBall)
	{
		this.botLastTouchedBall.setText(botLastTouchedBall.toString());
	}
}
