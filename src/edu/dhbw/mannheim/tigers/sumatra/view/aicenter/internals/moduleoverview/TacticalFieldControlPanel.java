/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 5, 2011
 * Author(s): NicolaiO
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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
 * 
 */
public class TacticalFieldControlPanel extends JPanel implements IChangeGUIMode
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID						= 2823710107966028875L;
	
	private BallPossession		lastBallPossession					= new BallPossession();
	private ETeam					lastClosestTeam						= null;
	
	private JTextField			ballPossession							= null;
	private JTextField			ballPossessionOpponentId			= null;
	private JTextField			ballPossessionTigersId				= null;
	private JTextField			closestTeamToBall						= null;
	private JTextField			botLastTouchedBall					= null;
	
	/** we can shoot on the goal immediately */
	private JCheckBox				tigersScoringChance					= null;
	/** our opponents can shoot on the goal immediately */
	private JCheckBox				opponentScoringChance				= null;
	/** we can shoot on the goal after getting and/or aiming the ball */
	private JCheckBox				tigersApproximateScoringChance	= null;
	/** our opponents can shoot on the goal after getting and/or aiming the ball */
	private JCheckBox				opponentApproximateScoringChance	= null;
	
	
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
		
		tigersScoringChance = new JCheckBox();
		tigersScoringChance.setEnabled(false);
		
		opponentScoringChance = new JCheckBox();
		opponentScoringChance.setEnabled(false);
		
		tigersApproximateScoringChance = new JCheckBox();
		tigersApproximateScoringChance.setEnabled(false);
		
		opponentApproximateScoringChance = new JCheckBox();
		opponentApproximateScoringChance.setEnabled(false);
		
		final JPanel ballPossessionPanel = new JPanel(new MigLayout("fill", "[60,fill]10[60,fill]10[30,fill]10[30,fill]"));
		ballPossessionPanel.add(new JLabel("Ball possession:"));
		ballPossessionPanel.add(ballPossession);
		ballPossessionPanel.add(new JLabel("Tiger:"));
		ballPossessionPanel.add(ballPossessionTigersId);
		ballPossessionPanel.add(new JLabel("Opponent:"));
		ballPossessionPanel.add(ballPossessionOpponentId);
		
		final JPanel closestTeamBallPanel = new JPanel(new MigLayout("fill", "[60,fill]10[100,fill]"));
		closestTeamBallPanel.add(new JLabel("Closest Team to Ball:"));
		closestTeamBallPanel.add(closestTeamToBall);
		
		final JPanel botLastTouchedBallPanel = new JPanel(new MigLayout("fill", "[60,fill]10[250,fill]"));
		botLastTouchedBallPanel.add(new JLabel("Bot last touched ball:"));
		botLastTouchedBallPanel.add(botLastTouchedBall);
		
		final JPanel scoringChancePanel = new JPanel(new MigLayout("fill", "[100,fill][10,fill]10[fill][10,fill]"));
		scoringChancePanel.add(new JLabel("Scoring Chance Tigers: "));
		scoringChancePanel.add(tigersScoringChance);
		scoringChancePanel.add(new JLabel("Opponents: "));
		scoringChancePanel.add(opponentScoringChance);
		
		final JPanel approximateScoringChancePanel = new JPanel(new MigLayout("fill",
				"[100,fill][10,fill]10[fill][10,fill]"));
		approximateScoringChancePanel.add(new JLabel("Approx. Scoring Chance Tigers: "));
		approximateScoringChancePanel.add(tigersApproximateScoringChance);
		approximateScoringChancePanel.add(new JLabel("Opponents: "));
		approximateScoringChancePanel.add(opponentApproximateScoringChance);
		
		
		add(ballPossessionPanel);
		add(closestTeamBallPanel);
		add(botLastTouchedBallPanel);
		add(scoringChancePanel);
		add(approximateScoringChancePanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onStart()
	{
		
	}
	
	
	@Override
	public void onStop()
	{
		
	}
	
	
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
	public void setBallPossession(BallPossession possession)
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
	public void setClosestTeamToBall(ETeam team)
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
	public void setBotLastTouchedBall(BotID botLastTouchedBall)
	{
		this.botLastTouchedBall.setText(botLastTouchedBall.toString());
	}
	
	
	@Override
	public void setPlayTestMode()
	{
		
	}
	
	
	@Override
	public void setRoleTestMode()
	{
		
	}
	
	
	@Override
	public void setMatchMode()
	{
		
	}
	
	
	@Override
	public void setEmergencyMode()
	{
		
	}
	
	
	/**
	 * @param tigersScoringChance
	 */
	public void setTigersScoringChance(boolean tigersScoringChance)
	{
		this.tigersScoringChance.setSelected(tigersScoringChance);
	}
	
	
	/**
	 * @param opponentScoringChance
	 */
	public void setOpponentScoringChance(boolean opponentScoringChance)
	{
		this.opponentScoringChance.setSelected(opponentScoringChance);
	}
	
	
	/**
	 * @param tigersApproximateScoringChance
	 */
	public void setTigersApproximateScoringChance(boolean tigersApproximateScoringChance)
	{
		this.tigersApproximateScoringChance.setSelected(tigersApproximateScoringChance);
	}
	
	
	/**
	 * @param opponentApproximateScoringChance
	 */
	public void setOpponentApproximateScoringChance(boolean opponentApproximateScoringChance)
	{
		this.opponentApproximateScoringChance.setSelected(opponentApproximateScoringChance);
	}
}
