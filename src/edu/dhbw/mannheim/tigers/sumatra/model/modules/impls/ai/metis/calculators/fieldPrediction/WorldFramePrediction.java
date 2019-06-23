/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;


/**
 * a Metis calculator to do a prediction for all elements on the field where they are in the future
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Embeddable
public class WorldFramePrediction
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private IBotIDMap<FieldPredictionInformation>	tigers	= new BotIDMap<FieldPredictionInformation>();
	private IBotIDMap<FieldPredictionInformation>	foes		= new BotIDMap<FieldPredictionInformation>();
	private FieldPredictionInformation					ball;
	
	
	/**
	  * 
	  */
	public WorldFramePrediction()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the ball
	 */
	public FieldPredictionInformation getBall()
	{
		return ball;
	}
	
	
	/**
	 * @param ball the ball to set
	 */
	public void setBall(FieldPredictionInformation ball)
	{
		this.ball = ball;
	}
	
	
	/**
	 * get the FieldPredictionInformation for one tiger
	 * 
	 * @param tiger
	 * @return
	 */
	public FieldPredictionInformation getTiger(BotID tiger)
	{
		return tigers.get(tiger);
	}
	
	
	/**
	 * set the field information of one tiger bot
	 * 
	 * @param botID
	 * @param fp
	 */
	public void setTigers(BotID botID, FieldPredictionInformation fp)
	{
		tigers.put(botID, fp);
	}
	
	
	/**
	 * get the FieldPredictor for one foe
	 * 
	 * @param foe
	 * @return
	 */
	public FieldPredictionInformation getFoe(BotID foe)
	{
		return foes.get(foe);
	}
	
	
	/**
	 * set the field information of one foe bot
	 * 
	 * @param botID
	 * @param fp
	 */
	public void setFoes(BotID botID, FieldPredictionInformation fp)
	{
		foes.put(botID, fp);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the tigers
	 */
	public IBotIDMap<FieldPredictionInformation> getTigers()
	{
		return tigers;
	}
	
	
	/**
	 * @return the foes
	 */
	public IBotIDMap<FieldPredictionInformation> getFoes()
	{
		return foes;
	}
}
