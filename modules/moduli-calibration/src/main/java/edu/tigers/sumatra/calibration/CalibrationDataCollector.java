/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ISkillSystemObserver;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CalibrationDataCollector implements ISkillSystemObserver, IWorldFrameObserver
{
	private final List<ICalibrationDataObserver> observers = new CopyOnWriteArrayList<>();
	private boolean botActive;
	private BotID closestBotToBall;
	private KickParams lastKickParams;
	private IVector2 lastRawBallPos;


	public void addObserver(ICalibrationDataObserver o)
	{
		observers.add(o);
	}


	public void removeObserver(ICalibrationDataObserver o)
	{
		observers.remove(o);
	}


	@Override
	public void onNewWorldFrame(WorldFrameWrapper wFrameWrapper)
	{
		var ballPos = wFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		closestBotToBall = wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.min(Comparator.comparing(bot -> bot.getPos().distanceTo(ballPos))).map(
						ITrackedBot::getBotId).orElse(BotID.noBot());
		var bot = wFrameWrapper.getSimpleWorldFrame().getBot(closestBotToBall);
		if (botActive && bot != null && lastKickParams != null)
		{
			var data = CalibrationData.builder()
					.timestamp(wFrameWrapper.getTimestamp())
					.ball(wFrameWrapper.getSimpleWorldFrame().getBall())
					.kickFitState(wFrameWrapper.getSimpleWorldFrame().getKickFitState().orElse(null))
					.bot(bot)
					.rawBallPos(lastRawBallPos)
					.kickParams(lastKickParams)
					.build();
			var sample = CalibrationDataSample.fromInput(data);
			observers.forEach(o -> o.onNewCalibrationData(sample));
		} else
		{
			observers.forEach(ICalibrationDataObserver::onNoData);
		}
	}


	@Override
	public void onNewCamDetectionFrame(ExtendedCamDetectionFrame frame)
	{
		lastRawBallPos = frame.getBall().getPos().getXYVector();
	}


	@Override
	public void onClearWorldFrame()
	{
		closestBotToBall = null;
		lastKickParams = null;
		botActive = false;
		observers.forEach(ICalibrationDataObserver::onNoData);
	}


	@Override
	public void onClearCamDetectionFrame()
	{
		lastRawBallPos = null;
	}


	@Override
	public void onCommandSent(ABot bot, long timestamp)
	{
		if (bot.getBotId() == closestBotToBall)
		{
			lastKickParams = KickParams.of(
					bot.getMatchCtrl().getSkill().getDevice(),
					bot.getMatchCtrl().getSkill().getKickSpeed()
			);
			botActive = bot.getMatchCtrl().getSkill().getType() != EBotSkill.MOTORS_OFF;
		}
	}
}
