/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.GameMemory;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventTypeDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.states.APassingDetectionState;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.states.EPassingDetectionState;
import edu.tigers.sumatra.loganalysis.microtypes.KickDetection;
import edu.tigers.sumatra.loganalysis.microtypes.LineDetection;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static edu.tigers.sumatra.loganalysis.GameMemory.GameLogObject.BALL;


public class ShotDetection implements IEventTypeDetection<IShotEventType>
{
	private static final Logger log = Logger.getLogger(ShotDetection.class.getName());
	
	private List<IDrawableShape> passingHistoryDraw = new LinkedList<>();
	
	private double toleranceBotToBallKick = 180d;
	
	
	private ShotBuilder shotBuilder = new ShotBuilder();
	
	private LineDetection lineDetection = new LineDetection();
	private KickDetection kickDetection = new KickDetection();
	
	// Memory last Values
	private GameMemory memory;
	
	
	// State param
	private WorldFrameWrapper currentWorldFrame;
	private ITrackedBot nextBotToBall;

	private StateMachine<APassingDetectionState> detectionStateMachine;
	
	private IShotEventType detectedShot = null;

	
	
	public ShotDetection()
	{
		try
		{
			detectionStateMachine = new StateMachine<>();
			
			for (EPassingDetectionState enumState : EPassingDetectionState.values())
			{
				APassingDetectionState state = (APassingDetectionState) enumState.getInstanceableClass()
						.newDefaultInstance();
				
				if (enumState == EPassingDetectionState.NO_PASS)
				{
					detectionStateMachine.setInitialState(state);
				}
				
				detectionStateMachine.addTransition(null, enumState, state);
			}
			
		} catch (InstanceableClass.NotCreateableException e)
		{
			log.error("Failed creating StateMachine - State could not be created: " + e.getMessage(), e);
		}


	}
	
	
	@Override
	public void nextFrameForDetection(TypeDetectionFrame typeDetectionFrame)
	{
		memory = typeDetectionFrame.getMemory();
		currentWorldFrame = typeDetectionFrame.getWorldFrameWrapper();
		ShapeMap shapes = typeDetectionFrame.getShapeMap();
		
		nextBotToBall = typeDetectionFrame.getNextBotToBall();

		kickDetection.nextFrameForDetection(typeDetectionFrame);
		lineDetection.nextFrameForDetection(typeDetectionFrame);
		
		if (currentWorldFrame.getGameState().isStop())
		{
			// Reset Passing
			shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.GAME_STATE_STOP);
			setState(EPassingDetectionState.NO_PASS);
		} else
		{
			PassTypeDetectionFrame passTypeDetectionFrame = new PassTypeDetectionFrame(typeDetectionFrame,
					shotBuilder, kickDetection, lineDetection, this);
			detectionStateMachine.getCurrentState().callNextFrameForDetection(passTypeDetectionFrame);
		}
		
		// Draw last passes in the visualizer
		for (IDrawableShape passing : passingHistoryDraw)
		{
			shapes.get(ELogAnalysisShapesLayer.PASSING).add(passing);
		}

		Map<EPassingDetectionState, Color> passStateColorMap = new EnumMap<>(EPassingDetectionState.class);
		passStateColorMap.put(EPassingDetectionState.NO_PASS, Color.MAGENTA);
		passStateColorMap.put(EPassingDetectionState.PASS, Color.BLACK);
		passStateColorMap.put(EPassingDetectionState.CHIP, Color.BLUE);
		passStateColorMap.put(EPassingDetectionState.WAITING_CHIP, Color.CYAN);
		passStateColorMap.put(EPassingDetectionState.PASS_DETECTION, Color.ORANGE);

		Color colorBall = passStateColorMap.getOrDefault(detectionStateMachine.getCurrentState().getId(), Color.MAGENTA);

		IVector2 ballPos = currentWorldFrame.getSimpleWorldFrame().getBall().getPos();
		shapes.get(ELogAnalysisShapesLayer.PASSING).add(new DrawableCircle(Circle.createCircle(ballPos, Geometry.getBallRadius()), colorBall));
	}

	@Override
	public void resetDetection()
	{
		passingHistoryDraw.clear();

		shotBuilder.clear();
		lineDetection = new LineDetection();
		kickDetection = new KickDetection();


		// State param
		currentWorldFrame = null;
		nextBotToBall = null;

		detectionStateMachine.restart();
		detectedShot = null;
	}
	
	
	@Override
	public IShotEventType getDetectedEventType()
	{
		return detectedShot;
	}


	public void setState(EPassingDetectionState newStateEnum)
	{
		
		// if current detection state is null (first call)
		if (detectionStateMachine.getCurrentState() == null)
		{
			detectionStateMachine.triggerEvent(newStateEnum);
			detectionStateMachine.update();
			return;
		}
		
		
		EPassingDetectionState newStateID = newStateEnum;
		EPassingDetectionState curStateID = detectionStateMachine.getCurrentState().getId();
		
		if (newStateID != curStateID)
		{
			onDetectionStateChanged(curStateID, newStateID);
		}
	}
	
	
	private void onDetectionStateChanged(EPassingDetectionState oldStateID, EPassingDetectionState newStateID)
	{
		boolean leaveDetectedPass = oldStateID.isPassActive() && !newStateID.isPassActive();
		
		if (leaveDetectedPass)
		{
			ITrackedBall passEndBall = (ITrackedBall)memory.get(BALL, 1);
			ITrackedBot receiver = null;
			
			boolean ballCloseToNextBot = nextBotToBall.getPos().distanceTo(passEndBall.getPos()) < toleranceBotToBallKick
					+ Geometry.getBallRadius();

			boolean nextBotSameTeamShooter = false;

			if(shotBuilder.getPasserBot() != null)
			{
				nextBotSameTeamShooter = nextBotToBall.getTeamColor() == shotBuilder.getPasserBot().getTeamColor();
			}


			boolean nextBotIsReceiver = ballCloseToNextBot && nextBotSameTeamShooter;

			if (nextBotIsReceiver)
			{
				receiver = nextBotToBall;
			}

			try {

				//decide whether the shot is a goal shot or a pass and create its shot event type
				detectedShot = shotBuilder.createShotEventType(currentWorldFrame.getTimestamp(), receiver, passEndBall.getPos());

				passingHistoryDraw.addAll(detectedShot.getDrawableShotShape());

				shotBuilder.clear();

			} catch (ShotBuilder.WrongBuilderStateException e) {
				log.error("Shot builder updates methods have not been called in right order", e);
			}
		}
		
		detectionStateMachine.triggerEvent(newStateID);
		detectionStateMachine.update();
		
		log.info("PassingDetectionState: " + oldStateID + " > \t" + newStateID);
	}
}

