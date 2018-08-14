package org.lintford.ld42.data.mobs;

import org.lintford.ld42.data.level.RoadSection;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.data.entities.CircleEntity;

public class BaseMob extends CircleEntity {

	private static final long serialVersionUID = -6989807923915894765L;

	public RoadSection parentRoadSection;
	
	public boolean isQuestGiver;
	public boolean isAlive;

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		x += dx * pCore.time().elapseGameTimeMilli() / 1000f;
		y += dy * pCore.time().elapseGameTimeMilli() / 1000f;

		dx *= 0.976f;
		dy *= 0.976f;

	}

}
