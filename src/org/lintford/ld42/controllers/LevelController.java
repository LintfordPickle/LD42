package org.lintford.ld42.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.level.Building;
import org.lintford.ld42.data.level.RoadSection;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.maths.RandomNumbers;

public class LevelController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "LevelController";

	// --------------------------------------
	// Variables
	// --------------------------------------

	World mWorld;

	List<RoadSection> mUpdateActiveRoadSections;
	List<RoadSection> mActiveRoadSections;

	List<Building> mUpdateActiveBuildings;
	List<Building> mActiveBuildings;

	AARectangle mVisibleBounds;
	AARectangle mSimulationBounds;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<RoadSection> activeRoadSections() {
		return mActiveRoadSections;
	}

	public List<Building> activeBuildings() {
		return mActiveBuildings;
	}

	@Override
	public boolean isInitialised() {
		return true;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public LevelController(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;

		mUpdateActiveRoadSections = new ArrayList<>();
		mActiveRoadSections = new ArrayList<>();

		mUpdateActiveBuildings = new ArrayList<>();
		mActiveBuildings = new ArrayList<>();

		mVisibleBounds = new AARectangle();
		mSimulationBounds = new AARectangle();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mVisibleBounds.set(pCore.gameCamera().boundingRectangle());
		mSimulationBounds.set(pCore.gameCamera().boundingRectangle());
		mSimulationBounds.expand(512f);

		// ROADS
		mUpdateActiveRoadSections.clear();
		int lRoadSectionCount = mWorld.level().roads().size();
		for (int i = 0; i < lRoadSectionCount; i++) {
			RoadSection lRoad = mWorld.level().roads().get(i);

			// check to see if within camera simulation bounds, and add to update list if so
			if (mSimulationBounds.intersects(lRoad.left(), lRoad.top(), lRoad.right(), lRoad.bottom())) {
				lRoad.isActive = true;

				if (mVisibleBounds.intersects(lRoad.left(), lRoad.top(), lRoad.right(), lRoad.bottom())) {
					lRoad.isVisible = true;

				} else {
					lRoad.isVisible = false;

				}

				mUpdateActiveRoadSections.add(lRoad);

			} else {
				lRoad.isActive = false;
				lRoad.isVisible = false;

			}

		}

		mActiveRoadSections.clear();
		// update the active sections (and remove the inactive ones)
		lRoadSectionCount = mUpdateActiveRoadSections.size();
		for (int i = 0; i < lRoadSectionCount; i++) {
			RoadSection lRoadSection = mUpdateActiveRoadSections.get(i);

			mActiveRoadSections.add(lRoadSection);

		}

		// BUILDINGS
		mUpdateActiveBuildings.clear();
		int lBuildingsCount = mWorld.level().building().size();
		for (int i = 0; i < lBuildingsCount; i++) {
			Building lBuilding = mWorld.level().building().get(i);

			// check to see if within camera simulation bounds, and add to update list if so
			if (mSimulationBounds.intersects(lBuilding.left(), lBuilding.top(), lBuilding.right(), lBuilding.bottom())) {
				mUpdateActiveBuildings.add(lBuilding);

			}

		}

		mActiveBuildings.clear();
		// update the active sections (and remove the inactive ones)
		lBuildingsCount = mUpdateActiveBuildings.size();
		for (int i = 0; i < lBuildingsCount; i++) {
			Building lBuilding = mUpdateActiveBuildings.get(i);

			mActiveBuildings.add(lBuilding);

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public RoadSection getRandomRoadSection() {
		if (mActiveRoadSections == null || mActiveRoadSections.size() == 0)
			return null;

		return mActiveRoadSections.get(RandomNumbers.random(0, mActiveRoadSections.size()));

	}

}
