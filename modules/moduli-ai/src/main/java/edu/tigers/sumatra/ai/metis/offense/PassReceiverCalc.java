/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.drawable.animated.AnimationTimerSine;
import edu.tigers.sumatra.drawable.animated.AnimationTimerUp;
import edu.tigers.sumatra.drawable.animated.ColorAnimatorFixed;
import edu.tigers.sumatra.drawable.animated.NumberAnimatorMinMax;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Add a pass receiver if required
 */
@RequiredArgsConstructor
public class PassReceiverCalc extends ACalculator
{
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<Pass> keeperPass;

	@Getter
	private List<BotID> passReceiver;


	@Override
	public void doCalc()
	{
		var newPassReceivers = new ArrayList<BotID>();
		Optional.ofNullable(keeperPass.get()).ifPresent(pass -> newPassReceivers.add(pass.getReceiver()));
		var passList = offensiveActions.get().values().stream()
				.filter(this::isPassAction)
				.map(e -> e.getAction().getPass())
				.toList();

		passList.forEach(e -> getShapes(EAiShapesLayer.OFFENSE_PASSING).add(
				new AnimatedCrosshair(e.getKick().getTarget(),
						new NumberAnimatorMinMax(30, 150, new AnimationTimerSine(Math.PI)),
						new NumberAnimatorMinMax(0, Math.PI * 2.0, new AnimationTimerUp(2)),
						new ColorAnimatorFixed(Color.BLACK),
						new ColorAnimatorFixed(new Color(255, 255, 255, 0))
				)));
		passList.forEach(e -> getShapes(EAiShapesLayer.OFFENSE_PASSING).add(
				new AnimatedCrosshair(e.getKick().getTarget(),
						new NumberAnimatorMinMax(40, 210, new AnimationTimerSine(Math.PI)),
						new NumberAnimatorMinMax(0, Math.PI * 2.0, new AnimationTimerUp(2)),
						new ColorAnimatorFixed(Color.RED),
						new ColorAnimatorFixed(new Color(255, 255, 255, 0))
				)));

		passList
				.stream()
				.map(Pass::getReceiver)
				.filter(AObjectID::isBot)
				.forEach(newPassReceivers::add);

		passReceiver = Collections.unmodifiableList(newPassReceivers);
	}


	private boolean isPassAction(RatedOffensiveAction a)
	{
		return a.getAction().getPass() != null;
	}
}
