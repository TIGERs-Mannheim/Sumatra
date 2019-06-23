/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): BernhardP, AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.AICenterPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.BotCenterPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.BotCenterPresenterNoGui;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.main.MainPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.referee.RefereePresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.timer.TimerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.VisualizerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.wpcenter.WPCenterPresenter;


/**
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 * @author bernhard
 */
public class Sumatra
{
	/**
	 * Creates the model of the application and redirects to a presenter.
	 * @param args
	 */
	public static void main(String[] args)
	{
		// create model
		SumatraModel.getInstance();
		
		// create the main presenter
		MainPresenter mainPresenter = new MainPresenter(args);
		
		if (mainPresenter.hasGUI())
		{
			// create your custom presenters here
			LogPresenter logPresenter = new LogPresenter(); // from this point on logging is visible in the logPanel
			BotCenterPresenter botCenter = new BotCenterPresenter();
			VisualizerPresenter visualizer = new VisualizerPresenter();
			
			AICenterPresenter aiCenter = new AICenterPresenter();
			mainPresenter.addAIPresenter(aiCenter);
			
			TimerPresenter timer = new TimerPresenter();
			WPCenterPresenter wpCenter = new WPCenterPresenter();
			RefereePresenter referee = new RefereePresenter();
			
			// add all panels from your presenters to the main view
			mainPresenter.addView(logPresenter.getLogPanel());
			mainPresenter.addView(botCenter);
			mainPresenter.addView(visualizer.getView());
			mainPresenter.addView(aiCenter);
			mainPresenter.addView(timer.getView());
			mainPresenter.addView(wpCenter);
			mainPresenter.addView(referee);
			
		} else
		{
			BotCenterPresenterNoGui botCenter = new BotCenterPresenterNoGui();
			
			mainPresenter.addView(botCenter);
		}
		

		// start main presenter and delegate application control
		mainPresenter.start();
	}
}
