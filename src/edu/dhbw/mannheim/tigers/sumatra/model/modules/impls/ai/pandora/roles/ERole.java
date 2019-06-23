/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveBallToRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.OffensiveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PenaltyShooterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.SimpleShooterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.PenaltyKeeperRoleV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.ChipKickTrainerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.ChipKickTrainerV2Role;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.DestChangedTestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.SkillTestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.StarCalibrateRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.StraightKickTrainerRole;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * Enumeration that represents the different {@link ARole} When added a new role do not forget to adjust role factory in
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis}.
 * 
 * @author Gero, ChristianK
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ERole implements IInstanceableEnum
{
	// main
	/**  */
	OFFENSIVE(new InstanceableClass(OffensiveRole.class)),
	/**  */
	SUPPORT(new InstanceableClass(SupportRole.class)),
	/** */
	DEFENDER(new InstanceableClass(DefenderRole.class)),
	/** */
	KEEPER(new InstanceableClass(KeeperRole.class)),
	
	// movement
	/** */
	MOVE(new InstanceableClass(MoveRole.class, new InstanceableParameter(EMoveBehavior.class, "moveBehavior", "NORMAL"))),
	/**  */
	MOVE_BALL_TO(new InstanceableClass(MoveBallToRole.class, new InstanceableParameter(IVector2.class, "ballTarget",
			"0,0"))),
	
	// to be removed
	/** */
	MAN_TO_MAN_MARKER(new InstanceableClass(ManToManMarkerRole.class, new InstanceableParameter(DynamicPosition.class,
			"markerPos", "0,0"))),
	
	// Standards
	/** */
	PENALTY_KEEPER_V2(new InstanceableClass(PenaltyKeeperRoleV2.class)),
	
	// test
	/** */
	MOVE_TO_TEST(new InstanceableClass(MoveRole.class, new InstanceableParameter(IVector2.class, "destination", "0,0"),
			new InstanceableParameter(Float.TYPE, "orientation", "0.0"), new InstanceableParameter(Float.TYPE, "speed",
					"3.0"))),
	/**  */
	SKILL_TEST(new InstanceableClass(SkillTestRole.class)),
	/**  */
	STAR_CALIBRATE(new InstanceableClass(StarCalibrateRole.class)),
	/**  */
	DEST_CHANGED(new InstanceableClass(DestChangedTestRole.class, new InstanceableParameter(IVector2.class, "diffDest",
			"0,1000"), new InstanceableParameter(IVector2.class, "diffAngle", "1000,0"), new InstanceableParameter(
			Integer.TYPE, "freq", "30"))),
	/**  */
	SIMPLE_SHOOTER(new InstanceableClass(SimpleShooterRole.class)),
	/**  */
	PENALTY_SHOOTER(new InstanceableClass(PenaltyShooterRole.class)),
	
	/**  */
	EPIC_PENALTY_SHOOTER(new InstanceableClass(EpicPenaltyShooterRole.class)),
	
	/**  */
	CHIP_KICK_TRAINER(new InstanceableClass(ChipKickTrainerRole.class, new InstanceableParameter(Integer.TYPE, "durLow",
			"2000"), new InstanceableParameter(Integer.TYPE, "durHigh", "6000"), new InstanceableParameter(Integer.TYPE,
			"dribbleLow", "5000"), new InstanceableParameter(Integer.TYPE, "dribbleHigh", "15000"))),
	/**  */
	CHIP_KICK_TRAINER_V2(new InstanceableClass(ChipKickTrainerV2Role.class, new InstanceableParameter(Integer.TYPE,
			"durLow",
			"2000"), new InstanceableParameter(Integer.TYPE, "durHigh", "6000"), new InstanceableParameter(Integer.TYPE,
			"dribbleLow", "5000"), new InstanceableParameter(Integer.TYPE, "dribbleHigh", "15000"))),
	
	/**  */
	STRAIGHT_KICK_TRAINER(new InstanceableClass(StraightKickTrainerRole.class)), ;
	
	private final InstanceableClass	clazz;
	
	
	/**
	 */
	private ERole(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
