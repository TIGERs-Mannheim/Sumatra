/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.gamelog;

import edu.tigers.autoref.presenter.GameLogPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author "Lukas Magel"
 */
public class GameLogView extends ASumatraView
{
	
	/**
	 * 
	 */
	public GameLogView()
	{
		super(ESumatraViewType.AUTOREFEREE_GAME_LOG);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new GameLogPresenter();
	}
	
}
