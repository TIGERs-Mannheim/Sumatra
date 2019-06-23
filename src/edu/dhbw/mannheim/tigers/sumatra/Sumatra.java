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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.AICenterPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.BotCenterPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.BotCenterPresenterNoGui;
import edu.dhbw.mannheim.tigers.sumatra.presenter.config.ConfigEditorPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.main.MainPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.RCMPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.RCMPresenterMqtt;
import edu.dhbw.mannheim.tigers.sumatra.presenter.referee.RefereePresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.statistics.PlayFinderStatisticsPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.timer.TimerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.VisualizerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.util.JULLoggingBridge;
import edu.dhbw.mannheim.tigers.sumatra.util.NativesLoader;
import edu.dhbw.mannheim.tigers.sumatra.util.NativesLoader.LoaderException;
import edu.dhbw.mannheim.tigers.sumatra.util.OsDetector;


/**
 * 
 * <pre>
 *          __  _-==-=_,-.
 *         /--`' \_@-@.--<
 *         `--'\ \   <___/.                 The wonderful thing about Tiggers,
 *             \ \\   " /                   is Tiggers are wonderful things.
 *               >=\\_/`<                   Their tops are made out of rubber,
 *   ____       /= |  \_/                   their bottoms are made out of springs.
 * _'    `\   _/=== \__/                    They're bouncy, trouncy, flouncy, pouncy,
 * `___/ //\./=/~\====\                     Fun, fun, fun, fun, fun.
 *     \   // /   | ===:                    But the most wonderful thing about Tiggers is,
 *      |  ._/_,__|_ ==:        __          I'm the only one.
 *       \/    \\ \\`--|       / \\
 *        |    _     \\:      /==:-\
 *        `.__' `-____/       |--|==:
 *           \    \ ===\      :==:`-'
 *           _>    \ ===\    /==/
 *          /==\   |  ===\__/--/
 *         <=== \  /  ====\ \\/
 *         _`--  \/  === \/--'
 *        |       \ ==== |
 *         -`------/`--' /
 *                 \___-'
 * </pre>
 * 
 * 
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 * @author bernhard
 */
public final class Sumatra
{
	private static final Logger	log;
	
	
	/**  */
	public static final String		MATCH_CHECKLIST	= "-log4j file log disabled?\n" + "-appropriate log level set?\n"
																			+ "-correct knowledge base selected and loaded?\n";
	
	
	private Sumatra()
	{
		
	}
	
	static
	{
		// Setup log4j system
		// -Dlog4j.configuration="edu/dhbw/mannheim/tigers/sumatra/log4j.properties"
		System.setProperty("log4j.configuration", "edu/dhbw/mannheim/tigers/sumatra/log4j.properties");
		PropertyConfigurator.configure(System.getProperties());
		// Connect java.util.logging (for jinput)
		JULLoggingBridge.install();
		
		log = Logger.getLogger(Sumatra.class.getName());
		log.info("Logger initialized and starting Sumatra");
		// Load native libraries
		try
		{
			// load libs for input control, needed by RCM-Module
			// for linux, this does not work, so instead, library path is set in build path config from eclipse
			if (OsDetector.isWindows())
			{
				final String curDir = System.getProperty("user.dir");
				final NativesLoader loader = new NativesLoader(curDir + "/lib/native/", true);
				loader.loadLibrary("jinput-dx8");
				loader.loadLibrary("jinput-raw");
			}
		} catch (final LoaderException err)
		{
			log.error("Could not load native libaries", err);
		}
	}
	
	
	/**
	 * Creates the model of the application and redirects to a presenter.
	 * @param args
	 */
	public static void main(String[] args)
	{
		log.trace("main started");
		// create model
		SumatraModel.getInstance();
		log.trace("SumatraModel loaded");
		
		// create the main presenter
		final MainPresenter mainPresenter = new MainPresenter(args);
		
		if (mainPresenter.hasGUI())
		{
			log.trace("Creating presenters");
			// create your custom presenters here
			// from this point on logging is visible in the logPanel
			final LogPresenter logPresenter = new LogPresenter();
			final BotCenterPresenter botCenter = new BotCenterPresenter();
			final VisualizerPresenter visualizer = new VisualizerPresenter();
			
			final AICenterPresenter aiCenter = new AICenterPresenter();
			mainPresenter.addAIPresenter(aiCenter);
			
			final TimerPresenter timer = new TimerPresenter();
			
			// WPCenterPresenter performs many updates. comment out, if Sumatra is slow
			// WPCenterPresenter wpCenter = new WPCenterPresenter();
			final RefereePresenter referee = new RefereePresenter();
			
			final RCMPresenter rcm = RCMPresenter.getInstance();
			new RCMPresenterMqtt();
			
			final ConfigEditorPresenter configEditor = new ConfigEditorPresenter();
			
			final PlayFinderStatisticsPresenter statistics = new PlayFinderStatisticsPresenter();
			
			log.trace("Finished creating presenters");
			
			// add all panels from your presenters to the main view
			mainPresenter.addView(logPresenter.getView());
			mainPresenter.addView(botCenter.getView());
			mainPresenter.addView(visualizer.getView());
			mainPresenter.addView(aiCenter.getView());
			mainPresenter.addView(timer.getView());
			// mainPresenter.addView(wpCenter.getView());
			mainPresenter.addView(referee.getView());
			mainPresenter.addView(rcm.getView());
			mainPresenter.addView(configEditor.getView());
			mainPresenter.addView(statistics.getView());
			
			log.trace("Finished adding views");
		} else
		{
			final BotCenterPresenterNoGui botCenter = new BotCenterPresenterNoGui();
			
			mainPresenter.addView(botCenter);
		}
		
		
		// start main presenter and delegate application control
		mainPresenter.start();
	}
	
	
	/**
	 * the static part of this class should have been executed now
	 */
	public static void touch()
	{
	}
}
