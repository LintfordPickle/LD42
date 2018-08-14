package org.lintford.ld42.data.level;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.maths.RandomNumbers;

public class RoadSection extends Rectangle {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -3347810612441241581L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public final int tileIndex;
	public boolean isActive;
	public boolean isVisible;

	public int textureIndex;
	public List<LevelNode> levelNode;
	public List<PickUpAreas> pickUpAreas;
	
	// --------------------------------------
	// Constructors
	// --------------------------------------

 	public RoadSection(int pTileIndex) {
		tileIndex = pTileIndex;
		levelNode = new ArrayList<>();
		pickUpAreas = new ArrayList<>();

	}

	public LevelNode getRandomStartNode() {
		if (levelNode == null || levelNode.size() == 0)
			return null;
		if (levelNode.size() == 1)
			return levelNode.get(0);

		return levelNode.get(RandomNumbers.random(0, levelNode.size()));

	}

	public PickUpAreas getRandomMobSpawner() {
		if (pickUpAreas == null || pickUpAreas.size() == 0)
			return null;

		return pickUpAreas.get(RandomNumbers.random(0, pickUpAreas.size()));

	}

}
