/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 11, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;


/**
 * KnowledgeField is a container for a field constellation.
 * 
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public abstract class AKnowledgeField
{
	/** not final for ObjectDB */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private BotIDMapConst<TrackedTigerBot>	tigerBots;
	/** not final for ObjectDB */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private BotIDMapConst<TrackedBot>		foeBots;
	/** not final for ObjectDB */
	@Embedded
	private TrackedBall							ball;
	/** not final for ObjectDB */
	@Embedded
	private BallPossession						ballPossession;
	
	
	/**
	 * @param aiFrame
	 */
	public AKnowledgeField(AIInfoFrame aiFrame)
	{
		tigerBots = aiFrame.worldFrame.tigerBotsVisible;
		foeBots = aiFrame.worldFrame.foeBots;
		ball = aiFrame.worldFrame.ball;
		ballPossession = aiFrame.tacticalInfo.getBallPossession();
	}
	
	
	/**
	 * @param tigerBots
	 * @param foeBots
	 * @param ball
	 * @param ballPossession
	 */
	public AKnowledgeField(BotIDMapConst<TrackedTigerBot> tigerBots, BotIDMapConst<TrackedBot> foeBots,
			TrackedBall ball, BallPossession ballPossession)
	{
		this.tigerBots = tigerBots;
		this.foeBots = foeBots;
		this.ball = ball;
		this.ballPossession = ballPossession;
	}
	
	
	/**
	 * @param original
	 */
	public AKnowledgeField(AKnowledgeField original)
	{
		tigerBots = original.tigerBots;
		foeBots = original.foeBots;
		ball = original.getBall();
		ballPossession = original.getBallPossession();
	}
	
	
	/**
	 * Compares to IKnowledgeField objects (same implementation!) and returns a IComparisonResult, where you can get a
	 * similarity factor (Ähnlichkeitsfaktor?) of both fields.
	 * 
	 * @param knowledgeField
	 * @return
	 */
	public abstract IComparisonResult compare(AKnowledgeField knowledgeField);
	
	
	/**
	 * Initialize the concrete implementation of this AKnowledgeField.
	 * Use this if you have additional fields that were not stored in the database
	 */
	public abstract void initialize();
	
	
	/**
	 * @return
	 */
	public BotIDMapConst<TrackedTigerBot> getTigerBots()
	{
		return tigerBots;
	}
	
	
	/**
	 * @return
	 */
	public BotIDMapConst<TrackedBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	/**
	 * @return
	 */
	public TrackedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @return
	 */
	public BallPossession getBallPossession()
	{
		return ballPossession;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param tigerBots the tigerBots to set
	 */
	protected void setTigerBots(BotIDMapConst<TrackedTigerBot> tigerBots)
	{
		this.tigerBots = tigerBots;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param foeBots the foeBots to set
	 */
	protected void setFoeBots(BotIDMapConst<TrackedBot> foeBots)
	{
		this.foeBots = foeBots;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param ball the ball to set
	 */
	protected void setBall(TrackedBall ball)
	{
		this.ball = ball;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param ballPossession the ballPossession to set
	 */
	protected void setBallPossession(BallPossession ballPossession)
	{
		this.ballPossession = ballPossession;
	}
}
