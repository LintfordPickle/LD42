package org.lintford.ld42.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lintford.ld42.data.GameState;
import org.lintford.ld42.data.World;
import org.lintford.ld42.data.cars.BaseCar;
import org.lintford.ld42.data.level.PickUpAreas;
import org.lintford.ld42.data.level.RoadSection;
import org.lintford.ld42.data.mission.Mission;
import org.lintford.ld42.data.mission.MissionManager;
import org.lintford.ld42.data.mobs.BaseMob;
import org.lintford.ld42.data.mobs.MobManager;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;

public class GameStateController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "GameStateController";

	public static final int NUM_MAX_AVAILABLE_MISSIONS = 7;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private LevelController mLevelController;
	private MobController mMobController;

	private World mWorld;
	private GameState mGameState;
	private MobManager mMobManager;
	private MissionManager mMissionManager;

	private List<Mission> mUpdateMissionList;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public boolean isInitialised() {
		return true;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameStateController(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;
		mGameState = mWorld.gameState();
		mMobManager = mWorld.mobManager();
		mMissionManager = mWorld.missionManager();

		mUpdateMissionList = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, groupID());
		mMobController = (MobController) pCore.controllerManager().getControllerByNameRequired(MobController.CONTROLLER_NAME, groupID());

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		checkMissionSpawn(pCore);

		checkMissionPickUp(pCore, mWorld.carManager().playerCar());

		checkMissionDropOff(pCore, mWorld.carManager().playerCar());

		// Update the wanted amount
		if (mGameState.isWanted()) {

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void checkMissionSpawn(LintfordCore pCore) {
		// make sure that are available missions with in the current active play area
		if (mMissionManager.availableMissions().size() < NUM_MAX_AVAILABLE_MISSIONS) {
			BaseMob lRandomMob = mMobController.getRandomMobOnMap();
			if (lRandomMob != null) {
				Mission lNewMission = mMissionManager.getMissionFromPool();

				if (lNewMission == null)
					return; // pool empty

				RoadSection lRandomRoad = mLevelController.getRandomRoadSection();
				if (lRandomRoad == null)
					return;

				PickUpAreas lDropOffPoint = lRandomRoad.getRandomMobSpawner();
				if (lDropOffPoint == null)
					return;

				float lPlayerX = mWorld.carManager().playerCar().x;
				float lPlayerY = mWorld.carManager().playerCar().y;
				float lDist = (float) Math.sqrt((lRandomMob.x - lPlayerX) * (lRandomMob.x - lPlayerX) + (lRandomMob.y - lPlayerY) * (lRandomMob.y - lPlayerY));

				float lFareFee = (lDist / 10f) * RandomNumbers.random(0.8f, 1.2f);

				lNewMission.init(lRandomMob, lDropOffPoint, (int) lFareFee);
				lRandomMob.isQuestGiver = true;

				mMissionManager.addMission(lNewMission);

			}

		}
	}

	private void checkMissionPickUp(LintfordCore pCore, BaseCar pPlayerCar) {
		if (mWorld.gameState().currentMission != null)
			return; // cannot pickup two missions at the same time

		mUpdateMissionList.clear();
		final int lMissionCount = mMissionManager.availableMissions().size();
		for (int i = 0; i < lMissionCount; i++) {
			mUpdateMissionList.add(mMissionManager.availableMissions().get(i));

		}

		final int lUpdateMissionCount = mUpdateMissionList.size();
		for (int i = 0; i < lUpdateMissionCount; i++) {
			Mission lMission = mUpdateMissionList.get(i);

			// If the quest giver has died, remove the mission
			if (lMission.questGiver == null || !lMission.questGiver.isAlive) {
				lMission.reset();
				mMissionManager.removeMission(lMission);
				mMissionManager.addMissionToPool(lMission);

				continue;

			}

			// Only check for new missions if we don't already have one
			if (!mWorld.gameState().isOnMission()) {
				float lDist = (float) Math.sqrt((pPlayerCar.x - lMission.questGiver.x) * (pPlayerCar.x - lMission.questGiver.x) + (pPlayerCar.y - lMission.questGiver.y) * (pPlayerCar.y - lMission.questGiver.y));
				if (lDist < pPlayerCar.height + 100f) {
					if (Math.abs(pPlayerCar.speed) < 30f) {
						// take the mission on
						mWorld.gameState().currentMission = lMission;

						mMissionManager.removeMission(lMission);
						mMobManager.removeMob(lMission.questGiver);

					}

				}

			}

		}

	}

	private void checkMissionDropOff(LintfordCore pCore, BaseCar pPlayerCar) {
		if (mWorld.gameState().currentMission == null)
			return; // cannot drop off

		Mission lMission = mWorld.gameState().currentMission;

		float lDist = (float) Math.sqrt((pPlayerCar.x - lMission.destX) * (pPlayerCar.x - lMission.destX) + (pPlayerCar.y - lMission.destY) * (pPlayerCar.y - lMission.destY));
		if (lDist < pPlayerCar.height + 100f) {
			if (Math.abs(pPlayerCar.speed) < 30f) {
				mWorld.gameState().addCredits(lMission.fee);

				BaseMob lMob = new BaseMob();
				lMob.x = lMission.destX;
				lMob.y = lMission.destY;

				// remove the quest give from the map
				mMobManager.mobs().add(lMob);

				lMission.reset();
				mMissionManager.addMissionToPool(lMission);
				mWorld.gameState().currentMission = null;

			}

		}

	}

}
