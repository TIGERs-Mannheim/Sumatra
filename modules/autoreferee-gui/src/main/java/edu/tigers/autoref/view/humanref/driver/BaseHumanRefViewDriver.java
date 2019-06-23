/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.humanref.driver;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.autoref.view.humanref.BaseHumanRefPanel;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;
import edu.tigers.sumatra.visualizer.view.field.IFieldPanel;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * @author "Lukas Magel"
 */
public class BaseHumanRefViewDriver implements IHumanRefViewDriver
{
	private static final List<IShapeLayer>	VALID_LAYERS;
	
	@Configurable
	private static boolean						fieldFlipped	= false;
	
	private final BaseHumanRefPanel			panel;
	
	private WorldFrameWrapper					wFrame;
	private IAutoRefFrame						refFrame;
	
	static
	{
		List<IShapeLayer> validLayers = Arrays.asList(
				EWpShapesLayer.BALL, EWpShapesLayer.BOTS,
				EWpShapesLayer.FIELD_BORDERS, EAutoRefShapesLayer.ENGINE,
				EWpShapesLayer.BALL_BUFFER);
		VALID_LAYERS = Collections.unmodifiableList(validLayers);
		
		ConfigRegistration.registerClass("autoreferee", BaseHumanRefViewDriver.class);
	}
	
	
	/**
	 * @param panel
	 */
	public BaseHumanRefViewDriver(final BaseHumanRefPanel panel)
	{
		this.panel = panel;
		
		ConfigRegistration.registerConfigurableCallback("autoreferee", new IConfigObserver()
		{
			@Override
			public void afterApply(final IConfigClient configClient)
			{
				IFieldPanel fieldPanel = panel.getFieldPanel();
				EFieldTurn turn = fieldPanel.getFieldTurn();
				if ((((turn == EFieldTurn.NORMAL) || (turn == EFieldTurn.T90)) && fieldFlipped)
						|| (((turn == EFieldTurn.T180) || (turn == EFieldTurn.T270)) && !fieldFlipped))
				{
					panel.turnField();
				}
			}
		});
	}
	
	
	@Override
	public void setNewWorldFrame(final WorldFrameWrapper frame)
	{
		wFrame = frame;
		updateUIWithFrame(frame);
	}
	
	
	@Override
	public void setNewRefFrame(final IAutoRefFrame frame)
	{
		refFrame = frame;
	}
	
	
	@Override
	public void setNewGameLogEntry(final GameLogEntry entry)
	{
	}
	
	
	@Override
	public void paintField()
	{
		updateWorldFrameShapes();
		panel.getFieldPanel().paintOffline();
	}
	
	
	private void updateUIWithFrame(final WorldFrameWrapper frame)
	{
		RefereeMsg refMsg = frame.getRefereeMsg();
		GameState state = frame.getGameState();
		
		long microsLeft;
		if (state.getState() == EGameState.TIMEOUT)
		{
			TeamInfo teamInfo = refMsg.getTeamInfo(state.getForTeam());
			microsLeft = teamInfo.getTimeoutTime();
		} else
		{
			microsLeft = refMsg.getStageTimeLeft();
		}
		Duration timeLeft = Duration.ofMillis(TimeUnit.MICROSECONDS.toMillis(microsLeft));
		
		panel.setTimeLeft(timeLeft);
		panel.setStage(refMsg.getStage());
		panel.setGoals(refMsg.getGoals());
		panel.setTeamNames(refMsg.getTeamNames());
		panel.setState(state);
	}
	
	
	/**
	 * 
	 */
	private void updateWorldFrameShapes()
	{
		WorldFrameWrapper lastFrame = wFrame;
		IAutoRefFrame lastRefFrame = refFrame;
		
		IFieldPanel fieldPanel = panel.getFieldPanel();
		if (lastFrame != null)
		{
			fieldPanel.setShapeMap(EShapeLayerSource.WP, filterShapes(lastFrame.getShapeMap()), false);
		} else
		{
			fieldPanel.clearField(EShapeLayerSource.WP);
			fieldPanel.clearField(EShapeLayerSource.AUTOREFEREE);
		}
		
		if (lastRefFrame != null)
		{
			fieldPanel.setShapeMap(EShapeLayerSource.AUTOREFEREE, lastRefFrame.getShapes(), false);
		} else
		{
			fieldPanel.clearField(EShapeLayerSource.AUTOREFEREE);
		}
	}
	
	
	private ShapeMap filterShapes(final ShapeMap shapes)
	{
		ShapeMap filteredShapes = new ShapeMap();
		shapes.getAllShapeLayers().stream()
				.filter(VALID_LAYERS::contains)
				.forEach(layer -> filteredShapes.get(layer).addAll(shapes.get(layer)));
		
		return filteredShapes;
	}
	
	
	@Override
	public void start()
	{
	}
	
	
	@Override
	public void stop()
	{
	}
}
