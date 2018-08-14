package org.lintford.ld42.data.level;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.maths.RandomNumbers;

public class LevelNode {

	// --------------------------------------
	// Variables
	// --------------------------------------

	public final float x;
	public final float y;

	private final RoadSection parentRoadSection;
	private List<LevelNode> mConnections;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public RoadSection parentRoadSection() {
		return parentRoadSection;
	}

	public List<LevelNode> connections() {
		return mConnections;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public LevelNode(RoadSection pParentRoad, float pX, float pY) {
		mConnections = new ArrayList<>();
		parentRoadSection = pParentRoad;
		x = pX;
		y = pY;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public LevelNode getRandomConnection() {
		if (mConnections.size() == 0)
			return null;

		return mConnections.get(RandomNumbers.random(0, mConnections.size()));
	}

	public LevelNode getRandomConnection(float pDirX, float pDirY) {
		if (mConnections.size() == 0)
			return null;

		// Get the connection with the least turning angle
		int lFound = 0;
		float lAngle = Float.MAX_VALUE;

		final int NUMCON = mConnections.size();
		for (int i = 0; i < NUMCON; i++) {
			LevelNode lCheckNode = mConnections.get(i);
			float lHeadingX = lCheckNode.x - pDirX;
			float lHeadingY = lCheckNode.y - pDirY;
			float angle = (float) Math.atan2(lHeadingX, lHeadingY);

			if (angle < lAngle) {
				lAngle = angle;
				lFound = i;
			}
		}

		return mConnections.get(lFound);
	}

	public void addConnection(LevelNode pOtherNode) {
		if (!mConnections.contains(pOtherNode)) {
			mConnections.add(pOtherNode);

		}

	}

}
