package org.lintford.ld42.data.mission;

import org.lintford.ld42.data.level.PickUpAreas;
import org.lintford.ld42.data.mobs.BaseMob;

import net.lintford.library.data.BaseData;

public class Mission extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 7382937295226510288L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public transient BaseMob questGiver;
	public float destX;
	public float destY;
	public boolean isReadyForPickup;
	public int fee;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Mission() {
		reset();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void init(BaseMob pQuestGiver, PickUpAreas lDropOffPoint, int pFee) {
		questGiver = pQuestGiver;
		destX = lDropOffPoint.x;
		destY = lDropOffPoint.y;
		fee = pFee;
		isReadyForPickup = true;
	}

	public void reset() {
		destX = 0;
		destY = 0;
		questGiver = null;
		fee = 0;
		isReadyForPickup = false;
	}

}
