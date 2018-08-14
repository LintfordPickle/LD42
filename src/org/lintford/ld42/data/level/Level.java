package org.lintford.ld42.data.level;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.data.BaseData;

public class Level extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -3885455938434645657L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private Vector2f mPlayerSpawnPosition;

	private List<Building> mBuildings;
	private List<RoadSection> mRoads;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public Vector2f playerSpawnPosition() {
		return mPlayerSpawnPosition;
	}

	public void setPlayerSpawnPosition(float pPosX, float pPosY) {
		mPlayerSpawnPosition.x = pPosX;
		mPlayerSpawnPosition.y = pPosY;

	}

	public List<Building> building() {
		return mBuildings;
	}

	public List<RoadSection> roads() {
		return mRoads;
	}

	public RoadSection getRandomRoadSectionOnMap() {
		if (mRoads == null || mRoads.size() == 0)
			return null;

		return mRoads.get(RandomNumbers.random(0, mRoads.size()));
	}

	public RoadSection getRoadSectionByIndex(int pTileIndex) {
		final int lSectionCount = mRoads.size();
		for (int i = 0; i < lSectionCount; i++) {
			if (mRoads.get(i).tileIndex == pTileIndex)
				return mRoads.get(i);

		}

		return null;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Level() {
		mRoads = new ArrayList<>();
		mBuildings = new ArrayList<>();

		mPlayerSpawnPosition = new Vector2f();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addBuilding(Building pNewBuilding) {
		if (!mBuildings.contains(pNewBuilding)) {
			mBuildings.add(pNewBuilding);

		}

	}

	public void removeBuilding(Building pNewBuilding) {
		if (mBuildings.contains(pNewBuilding)) {
			mBuildings.remove(pNewBuilding);

		}

	}

	public void addRoadSection(RoadSection pNewRoadSection) {
		if (!mRoads.contains(pNewRoadSection)) {
			mRoads.add(pNewRoadSection);

		}

	}

	public void removeRoadSection(RoadSection pNewRoadSection) {
		if (mRoads.contains(pNewRoadSection)) {
			mRoads.remove(pNewRoadSection);

		}

	}

}
