/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 25, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoref.util.AutoRefImageRegistry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.components.JImagePanel;
import edu.tigers.sumatra.components.ResizingLabel;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author "Lukas Magel"
 */
public class GameEventPanel extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= 3329621495568274433L;
	
	private JImagePanel			iconPanel			= new JImagePanel();
	private ResizingLabel		causeLabel			= new ResizingLabel(true);
	private JLabel					teamPrefixLabel	= new JLabel();
	private JTeamLabel			teamLabel			= new JTeamLabel();
	
	
	/**
	 * @param headerFont
	 * @param font
	 */
	public GameEventPanel(final Font headerFont, final Font font)
	{
		setupGUI(headerFont, font);
	}
	
	
	/**
	 * @param font
	 */
	private void setupGUI(final Font headerFont, final Font font)
	{
		causeLabel.setTargetFont(headerFont);
		causeLabel.setText("");
		
		teamPrefixLabel.setFont(font);
		/*
		 * The teamPrefixLabel is assigned a default text to force it to calculate its preferred height. This height
		 * information is used to scale the icon accordingly.
		 */
		teamPrefixLabel.setText("Init");
		
		teamLabel.setFont(font);
		teamLabel.setText("");
		
		setLayout(new MigLayout("align left", "[shrinkprio 100]10[shrinkprio 200]"));
		add(iconPanel, "spany 2, aligny top");
		add(causeLabel, "wrap");
		add(teamPrefixLabel, "skip, span, split 2");
		add(teamLabel);
		
		int iconHeight = (int) (teamPrefixLabel.getPreferredSize().height * 2.2f);
		iconPanel.setPreferredSize(new Dimension(iconHeight, iconHeight));
	}
	
	
	/**
	 * Clear the text and image
	 */
	public void clear()
	{
		causeLabel.setText(null);
		teamLabel.setVisible(false);
		teamPrefixLabel.setText(null);
		iconPanel.setImage(null);
	}
	
	
	/**
	 * @param event
	 */
	public void setEvent(final IGameEvent event)
	{
		teamLabel.setVisible(true);
		iconPanel.setImage(AutoRefImageRegistry.getEventIcon(event.getType()));
		causeLabel.setText(getEventText(event.getType()));
		teamPrefixLabel.setText(getTeamPrefixText(event.getType()));
		Optional<BotID> respBot = event.getResponsibleBot();
		if (respBot.isPresent())
		{
			teamLabel.setRobot(respBot.get());
		} else
		{
			teamLabel.setTeam(event.getResponsibleTeam());
		}
	}
	
	
	private String getEventText(final EGameEvent event)
	{
		switch (event)
		{
			case ATTACKER_IN_DEFENSE_AREA:
				return "Attacker in Defense Area";
			case ATTACKER_TOUCH_KEEPER:
				return "Attacker Touched Keeper";
			case ATTACKER_TO_DEFENCE_AREA:
				return "Attacker too close to Defense Area";
			case BALL_DRIBBLING:
				return "Dribbling";
			case BALL_HOLDING:
				return "Ball Holding";
			case BALL_LEFT_FIELD:
				return "Ball left the field";
			case BALL_SPEEDING:
				return "Ball Speeding";
			case BOT_COLLISION:
				return "Robot Collision";
			case BOT_COUNT:
				return "Robot Count";
			case BOT_STOP_SPEED:
				return "Robot Stop Speed";
			case DEFENDER_TO_KICK_POINT_DISTANCE:
				return "Robot to Ball Distance";
			case DOUBLE_TOUCH:
				return "Double Touch";
			case GOAL:
				return "Goal";
			case ICING:
				return "Icing";
			case INDIRECT_GOAL:
				return "Indirect Goal";
			case KICK_TIMEOUT:
				return "Kick Timeout";
			case MULTIPLE_DEFENDER:
				return "Multiple Defender";
			case MULTIPLE_DEFENDER_PARTIALLY:
				return "Multiple Defender Partially";
			default:
				return "";
		}
	}
	
	
	private String getTeamPrefixText(final EGameEvent event)
	{
		switch (event)
		{
			case ATTACKER_IN_DEFENSE_AREA:
			case ATTACKER_TOUCH_KEEPER:
			case ATTACKER_TO_DEFENCE_AREA:
			case BALL_DRIBBLING:
			case BALL_HOLDING:
			case BOT_COLLISION:
			case BOT_STOP_SPEED:
			case DEFENDER_TO_KICK_POINT_DISTANCE:
			case DOUBLE_TOUCH:
			case ICING:
			case MULTIPLE_DEFENDER:
			case MULTIPLE_DEFENDER_PARTIALLY:
				return "Committed by ";
			case BALL_LEFT_FIELD:
				return "Last contact by ";
			case BALL_SPEEDING:
				return "Shot by ";
			case BOT_COUNT:
				return "Exceeded by ";
			case GOAL:
			case INDIRECT_GOAL:
				return "Shot by ";
			case KICK_TIMEOUT:
				return "By team ";
			default:
				return "";
		}
	}
}
