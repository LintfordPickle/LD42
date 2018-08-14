package org.lintford.ld42.data.level;

public class PickUpAreas {

	public final int pickUpIndex;
	public float x;
	public float y;
	public float size; // also for drop offs

	public PickUpAreas(final int pPickupIndex, float pX, float pY) {
		pickUpIndex = pPickupIndex;
		x = pX;
		y = pY;

	}

	public void spawnMob() {

	}

}
