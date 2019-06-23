/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.10.2010
 * Author(s): Marcel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.wpcenter;

import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamDetnObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals.AnalysisCharts;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals.IAnalysisChartsObserver;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Presenter for the Timer-GUI
 * 
 * @author Marcel
 * 
 */
public class WPChartPresenter implements IModuliStateObserver, IWorldPredictorObserver, ICamDetnObserver,
		IAnalysisChartsObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int		BUFFER_SIZE					= 100;
	private final ATrackedObject	frames[]						= new ATrackedObject[BUFFER_SIZE];
	private final long				frameTimes[]				= new long[BUFFER_SIZE];
	private int							frameFirst					= 0;
	private int							frameLast					= 0;
	
	private static final int		FPS_INTERVALL				= 15;
	private int							fpsUpCounter				= FPS_INTERVALL;
	private int							fpsInCounter				= FPS_INTERVALL;
	private int							fpsOutCounter				= FPS_INTERVALL;
	private long						fpsUpTmp						= 0;
	private long						fpsInTmp						= 0;
	private long						fpsOutTmp					= 0;
	
	private static final Logger	log							= Logger.getLogger(WPChartPresenter.class.getName());
	
	private final SumatraModel		model							= SumatraModel.getInstance();
	
	private AnalysisCharts			chart							= null;
	
	
	private BotID						targetId						= new BotID(0);
	
	private AWorldPredictor			wp								= null;
	private ACam						cam							= null;
	
	
	private long						startUpdate					= System.nanoTime();
	private long						startNewWP					= System.nanoTime();
	// in milliseconds
	private static final long		VISUALIZATION_FREQUENCY	= 100;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public WPChartPresenter()
	{
		chart = new AnalysisCharts();
		chart.addObserver(this);
		ModuliStateAdapter.getInstance().addObserver(this);
		frameTimes[0] = 0;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					wp = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
					wp.addObserver(this);
					cam = (ACam) model.getModule(ACam.MODULE_ID);
					cam.addCamDetectionObserver(this);
					
				} catch (final ModuleNotFoundException err)
				{
					log.error("Timer Module not found!");
				}
				break;
			}
			
			default:
			{
				if (wp != null)
				{
					wp.removeObserver(this);
					wp = null;
				}
				
				if (cam != null)
				{
					cam.removeCamDetectionObserver(this);
					cam = null;
				}
				
				chart.clearChart();
				
				break;
			}
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public JPanel getChart()
	{
		return chart;
	}
	
	
	@Override
	public void onSetId(int id)
	{
		targetId = new BotID(id);
		frameFirst = 0;
		frameLast = 0;
		chart.clearChart();
	}
	
	
	@Override
	public void onShowA()
	{
		chart.setVisibleA(!chart.isVisibleA());
	}
	
	
	@Override
	public void onShowAbs()
	{
		chart.setVisibleAbs(!chart.isVisibleAbs());
		
	}
	
	
	@Override
	public void onShowX()
	{
		chart.setVisibleX(!chart.isVisibleX());
		
	}
	
	
	@Override
	public void onShowY()
	{
		chart.setVisibleY(!chart.isVisibleY());
		
	}
	
	
	@Override
	public void onShowBallVel()
	{
		chart.setVisibleBallVel(!chart.isVisibleBallVel());
	}
	
	
	private void handleUpdate(CamDetectionFrame df, int before, int after)
	{
		final long time = df.tCapture;
		final float inflBefore = ((float) (time - frameTimes[before])) / (frameTimes[after] - frameTimes[before]);
		final float inflAfter = 1 - inflBefore;
		
		if (targetId.getNumber() == 0)
		{
			if (df.balls.size() > 0)
			{
				if (++fpsUpCounter > FPS_INTERVALL)
				{
					chart.setFpsUp((FPS_INTERVALL * 1000000000.0f) / (df.tCapture - fpsUpTmp));
					fpsUpCounter = 1;
					fpsUpTmp = df.tCapture;
				}
				
				try
				{
					final CamBall ball = df.balls.get(0);
					final float xError = ((inflBefore * ((TrackedBall) frames[before]).getPos().x()) + (inflAfter * ((TrackedBall) frames[after])
							.getPos().x())) - ball.pos.x();
					final float yError = ((inflBefore * ((TrackedBall) frames[before]).getPos().y()) + (inflAfter * ((TrackedBall) frames[after])
							.getPos().y())) - ball.pos.y();
					final float absError = (float) Math.sqrt((xError * xError) + (yError * yError));
					chart.updateErrors(time, xError, yError, 0.0f, absError);
				} catch (final ClassCastException ex)
				{
					return;
				} catch (final Exception ex)
				{
					log.warn("Ball Array out of bound", ex);
					return;
				}
			}
			
		} else
		{
			CamRobot bot = null;
			for (final CamRobot tmpBot : df.robotsEnemies)
			{
				if (tmpBot.robotID == targetId.getNumber())
				{
					bot = tmpBot;
					break;
				}
			}
			for (final CamRobot tmpBot : df.robotsTigers)
			{
				if (tmpBot.robotID == targetId.getNumber())
				{
					bot = tmpBot;
					break;
				}
			}
			if (bot != null)
			{
				if (++fpsUpCounter > FPS_INTERVALL)
				{
					chart.setFpsUp((FPS_INTERVALL * 1000000000.0f) / (df.tCapture - fpsUpTmp));
					fpsUpCounter = 1;
					fpsUpTmp = df.tCapture;
				}
				
				TrackedBot bot1;
				TrackedBot bot2;
				
				try
				{
					bot1 = (TrackedBot) frames[before];
					bot2 = (TrackedBot) frames[after];
				} catch (final ClassCastException ex)
				{
					return;
				}
				
				final float xError = ((inflBefore * bot1.getPos().x()) + (inflAfter * bot2.getPos().x())) - bot.pos.x();
				final float yError = ((inflBefore * bot1.getPos().y()) + (inflAfter * bot2.getPos().y())) - bot.pos.y();
				float aError = 0;
				if (Math.abs(bot1.getAngle() - bot2.getAngle()) > Math.PI)
				{
					if (bot1.getAngle() > bot2.getAngle())
					{
						aError = (float) (((inflBefore * (bot1.getAngle() - (2 * Math.PI))) + (inflAfter * bot2.getAngle())) - bot.orientation);
					} else
					{
						aError = (float) (((inflBefore * (bot1.getAngle() + (2 * Math.PI))) + (inflAfter * bot2.getAngle())) - bot.orientation);
					}
				}
				if (aError > Math.PI)
				{
					aError -= 2 * Math.PI;
				} else if (aError < -Math.PI)
				{
					aError += 2 * Math.PI;
				}
				final float absError = (float) Math.sqrt((xError * xError) + (yError * yError));
				chart.updateErrors(time, xError, yError, aError, absError);
			}
		}
	}
	
	
	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		if ((System.nanoTime() - startNewWP) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			
			if (++fpsOutCounter > FPS_INTERVALL)
			{
				chart.setFpsOut((FPS_INTERVALL * 1000000000.0f) / (wf.time - fpsOutTmp));
				fpsOutCounter = 1;
				fpsOutTmp = wf.time;
			}
			
			
			if (wf.time < frameTimes[frameLast])
			{
				return;
			}
			
			if (targetId.getNumber() == 0)
			{
				
				if (wf.ball != null)
				{
					frames[frameLast] = wf.ball;
					frameTimes[frameLast] = wf.time;
					frameLast = (frameLast + 1) % BUFFER_SIZE;
					if (frameLast == frameFirst)
					{
						frameLast = ((frameLast - 1) + BUFFER_SIZE) % BUFFER_SIZE;
					}
					chart.updateWF(wf.time, wf.ball.getPos().x(), wf.ball.getPos().y(), 0.0f);
					chart.updateBallVel(wf.time, wf.ball.getVel().getLength2());
				}
			} else
			{
				TrackedBot bot;
				bot = wf.foeBots.getWithNull(targetId);
				if (bot == null)
				{
					bot = wf.tigerBotsVisible.get(targetId);
				}
				if (bot != null)
				{
					frames[frameLast] = bot;
					frameTimes[frameLast] = wf.time;
					frameLast = (frameLast + 1) % BUFFER_SIZE;
					if (frameLast == frameFirst)
					{
						frameLast = ((frameLast - 1) + BUFFER_SIZE) % BUFFER_SIZE;
					}
					chart.updateWF(wf.time, bot.getPos().x(), bot.getPos().y(), bot.getAngle());
					return;
				}
			}
			startNewWP = System.nanoTime();
		}
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		
	}
	
	
	@Override
	public void update(ICamDetnObservable observable, CamDetectionFrame event)
	{
		if ((System.nanoTime() - startUpdate) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			if (++fpsInCounter > FPS_INTERVALL)
			{
				chart.setFpsIn((FPS_INTERVALL * 1000000000.0f) / (event.tCapture - fpsInTmp));
				fpsInCounter = 1;
				fpsInTmp = event.tCapture;
			}
			
			
			int i = frameFirst;
			final long time = event.tCapture;
			while (true)
			{
				if (frameTimes[i] > time)
				{
					if (i != frameFirst)
					{
						handleUpdate(event, ((i - 1) + BUFFER_SIZE) % BUFFER_SIZE, i);
						
						if ((((i - 1) + BUFFER_SIZE) % BUFFER_SIZE) != frameFirst)
						{
							frameFirst = ((i - 2) + BUFFER_SIZE) % BUFFER_SIZE;
						}
					}
					break;
				}
				if (i == frameLast)
				{
					break;
				}
				i = (i + 1) % BUFFER_SIZE;
			}
			
			
			if (targetId.getNumber() == 0)
			{
				
				if (event.balls.size() > 0)
				{
					final CamBall ball = event.balls.get(0);
					chart.updateCF(event.tCapture, ball.pos.x(), ball.pos.y(), 0.0f);
				}
			} else
			{
				for (final CamRobot bot : event.robotsEnemies)
				{
					if (bot.robotID == targetId.getNumber())
					{
						chart.updateCF(event.tCapture, bot.pos.x(), bot.pos.y(), bot.orientation);
						return;
					}
				}
				for (final CamRobot bot : event.robotsTigers)
				{
					if (bot.robotID == targetId.getNumber())
					{
						chart.updateCF(event.tCapture, bot.pos.x(), bot.pos.y(), bot.orientation);
						return;
					}
				}
			}
			startUpdate = System.nanoTime();
		}
	}
	
	
}
