/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;

import java.util.Optional;


/**
 * If no other State is active the keeper should block the direct line between the ball and the goal center.
 * To prepare for a direct shoot the keeper turn around, cause of faster movement back- and forwards.
 *
 * @author ChrisC
 */
public class NormalBlockState extends AKeeperState
{
	private MoveToTrajSkill posSkill;
	
	
	/**
	 * @param parent the parent keeper role
	 */
	public NormalBlockState(KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		posSkill = new MoveToTrajSkill();
		posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		posSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
		posSkill.getMoveCon().setBotsObstacle(false);
		posSkill.getMoveCon().setBallObstacle(true);
		posSkill.getMoveCon().getMoveConstraints().setAccMax(KeeperRole.getKeeperAcc());
		posSkill.getMoveCon().setGoalPostObstacle(false);
		setNewSkill(posSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
        if (KeeperRole.isKeepersNormalMovementCircular()) {
            blockCircular();
        } else {
            blockOnLine();
        }
    }

    private void blockOnLine() {

        IVector2 newLeftPost = Geometry.getGoalOur().getLeftPost().addNew(Vector2.fromX(Geometry.getBotRadius()));
        IVector2 newRightPost = Geometry.getGoalOur().getRightPost().addNew(Vector2.fromX(Geometry.getBotRadius()));
        ILine newGoalLine = Line.fromPoints(newLeftPost, newRightPost);
        ILine ballGoalcenterLine = Line.fromPoints(getWFrame().getBall().getPos(), Geometry.getGoalOur().getCenter());

        Optional<IVector2> intersection = newGoalLine.intersectionWith(ballGoalcenterLine);

        IVector2 destination = Geometry.getGoalOur().getCenter();

        if (intersection.isPresent()) {

            destination = checkPosts(intersection.get());
        }
        posSkill.getMoveCon().updateDestination(destination);
        posSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
    }

    private IVector2 checkPosts(IVector2 destination) {
        IVector2 finalDestination = destination;
        boolean isBallBehindGoalline = getWFrame().getBall().getPos().x() < Geometry.getGoalOur().getCenter().x();
        boolean isDestinationLeftFromLeftPost = destination.y() > Geometry.getGoalOur().getLeftPost().y();
        boolean isDestinationRightFromRightPost = destination.y() < Geometry.getGoalOur().getRightPost().y();
        boolean isBallLeftFromGoal = getWFrame().getBall().getPos().y() > 0;

        if ((isDestinationLeftFromLeftPost && !isBallBehindGoalline) || (isBallBehindGoalline && isBallLeftFromGoal)) {
            finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getLeftPost().y());
        } else if (isDestinationRightFromRightPost || isBallBehindGoalline) {
            finalDestination = Vector2.fromXY(destination.x(), Geometry.getGoalOur().getRightPost().y());
        }
        return finalDestination;
    }


    private void blockCircular() {
        IVector2 ballPos = getWFrame().getBall().getTrajectory().getPosByTime(0.1);

        IVector2 bisector = TriangleMath.bisector(ballPos, Geometry.getGoalOur().getLeftPost(), Geometry
                .getGoalOur().getRightPost());

        IVector2 destination = bisector
                .addNew(ballPos.subtractNew(bisector).scaleToNew(KeeperRole.getDistToGoalCenter()));

        destination = setDestinationOutsideGoalPosts(destination);

        posSkill.getMoveCon().updateDestination(destination);
        posSkill.getMoveCon().updateTargetAngle(calcDefendingOrientation());


    }

    @Override
    public void doExitActions() {
		// Nothing to do here
	}
	
}
