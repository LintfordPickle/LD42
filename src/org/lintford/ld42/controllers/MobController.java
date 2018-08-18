package org.lintford.ld42.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.cars.BaseCar;
import org.lintford.ld42.data.cars.CarManager;
import org.lintford.ld42.data.level.Building;
import org.lintford.ld42.data.level.PickUpAreas;
import org.lintford.ld42.data.level.RoadSection;
import org.lintford.ld42.data.mobs.BaseMob;
import org.lintford.ld42.data.mobs.MobManager;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.collisions.SAT;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;

public class MobController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "MobController";

	private float MOB_SPAWN_TIMER = 350;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;

	private LevelController mLevelController;

	private CarManager mCarManager;
	private MobManager mMobManager;

	private List<BaseMob> mUpdateMobList;
	private float spawnTimer;

	public Vector2f collisionVel = new Vector2f();
	public Vector2f collisionNor = new Vector2f();
	public Vector2f reflectionVelNor = new Vector2f();

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

	public MobController(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;

		mCarManager = mWorld.carManager();
		mMobManager = mWorld.mobManager();

		mUpdateMobList = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, mGroupID);

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		updateMobSpawns(pCore);

		updateMobs(pCore);

		// Collisions

		checkAllBuildingCollisions(pCore);

		checkAllCarCollisions(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void updateMobSpawns(LintfordCore pCore) {
		spawnTimer += pCore.time().elapseGameTimeMilli();

		if (spawnTimer > MOB_SPAWN_TIMER) {
			if (mMobManager.mobs().size() < MobManager.POOL_SIZE) {
				int lNumSpawns = RandomNumbers.random(0, 8);
				for (int i = 0; i < lNumSpawns; i++) {
					spawnMob();

				}

			}

			spawnTimer = 0;
		}
	}

	private void spawnMob() {
		List<RoadSection> lActiveRoadSections = mLevelController.activeRoadSections();
		final int lNumActiveRoadSections = lActiveRoadSections.size();

		if (lNumActiveRoadSections == 0)
			return;

		RoadSection lRoad = lActiveRoadSections.get(RandomNumbers.random(0, lNumActiveRoadSections));
		lActiveRoadSections.get(RandomNumbers.random(0, lNumActiveRoadSections));

		PickUpAreas lRandArea = lRoad.getRandomMobSpawner();
		if (lRandArea == null)
			return;

		BaseMob lNewMob = mMobManager.getMobFromPool();
		if (lNewMob == null) // pool empty
			return;

		lNewMob.isAlive = true;
		lNewMob.parentRoadSection = lRoad;
		lNewMob.setPosition(lRandArea.x, lRandArea.y);
		mMobManager.addMob(lNewMob);

	}

	public BaseMob getRandomMobOnMap() {
		RoadSection lRoad = mWorld.level().getRandomRoadSectionOnMap();

		BaseMob lNewMob = mMobManager.getMobFromPool();
		if (lNewMob == null) // pool empty
			return null;

		PickUpAreas lRandArea = lRoad.getRandomMobSpawner();

		lNewMob.isAlive = true;
		lNewMob.parentRoadSection = lRoad;
		lNewMob.setPosition(lRandArea.x, lRandArea.y);
		mMobManager.addMob(lNewMob);

		return lNewMob;
	}

	private void updateMobs(LintfordCore pCore) {
		mUpdateMobList.clear();
		final int lMobCount = mMobManager.mobs().size();
		for (int i = 0; i < lMobCount; i++) {
			mUpdateMobList.add(mMobManager.mobs().get(i));

		}

		final int lUpdateMobCount = mUpdateMobList.size();
		for (int i = 0; i < lUpdateMobCount; i++) {
			BaseMob lMob = mUpdateMobList.get(i);

			// don't despawn quest givers across the map
			if ((!lMob.isQuestGiver) && (lMob.parentRoadSection == null || !lMob.parentRoadSection.isActive)) {
				lMob.parentRoadSection = null;

				mMobManager.mobs().remove(lMob);
				mMobManager.addMobToPool(lMob);

				continue;
			}

			if (!lMob.isQuestGiver && lMob.isAlive) {
				// Update the mob
				lMob.dx += 1.0f * RandomNumbers.random(1f, 2f) * RandomNumbers.randomSign();
				lMob.dy += 1.0f * RandomNumbers.random(1f, 2f) * RandomNumbers.randomSign();

			}

			lMob.update(pCore);

		}

	}

	private void checkAllBuildingCollisions(LintfordCore pCore) {
		final int lBuildingCount = mWorld.level().building().size();
		for (int i = 0; i < lBuildingCount; i++) {
			Building lBuilding = mWorld.level().building().get(i);

			// Inner check - Mobs / Building
			final int lMobCount = mMobManager.mobs().size();
			for (int j = 0; j < lMobCount; j++) {
				BaseMob lMob = mMobManager.mobs().get(j);
				checkMobBuildingCollision(pCore, lMob, lBuilding);

			}

			// Inner check - Car / Building
			final int lCarCount = mCarManager.cars().size();
			for (int j = 0; j < lCarCount; j++) {
				BaseCar lCar = mCarManager.cars().get(j);
				checkCarBuildingCollision(pCore, lCar, lBuilding);

			}

			// Check player collisions with buildings
			checkCarBuildingCollision(pCore, mCarManager.playerCar(), lBuilding);

		}

	}

	private void checkMobBuildingCollision(LintfordCore pCore, BaseMob pMob, Building pBuilding) {
		// TODO: Stop mobs from waling through buildings (or not)

	}

	private void checkCarBuildingCollision(LintfordCore pCore, BaseCar pCar, Building pBuilding) {

		if (SAT.intersects(pCar.bounds(), pBuilding)) {
			collisionVel.x = (float) Math.cos(pCar.rotation) * pCar.speed;
			collisionVel.y = (float) Math.sin(pCar.rotation) * pCar.speed;

			float lColLenX = pBuilding.centerX - pCar.x;
			float lColLenY = pBuilding.centerY - pCar.y;

			if (Math.abs(lColLenX) > Math.abs(lColLenY)) {
				collisionNor.x = pBuilding.centerX < pCar.x ? 01f : -1f;
				collisionNor.y = 0f;
			} else {
				collisionNor.x = 0f;
				collisionNor.y = pBuilding.centerY < pCar.y ? 1f : -1f;
			}

			float nx = collisionNor.x;
			float ny = collisionNor.y;

			collisionVel.nor();
			reflectionVelNor = collisionVel.reflected(collisionNor);

			// Steer away from the wall // check: when going forwards?
			{
				float lPosX = pCar.x;
				float lPosY = pCar.y;
				float lFaceThisX = lPosX + reflectionVelNor.x;
				float lFaceThisY = lPosY + reflectionVelNor.y;

				pCar.steerAngle = turnToFace(lPosX, lPosY, lFaceThisX, lFaceThisY, pCar.rotation, 0.25f);

			}

			// Push the car away from the wall
			{
				float ang = (float) Math.atan2(ny, nx);
				pCar.x += (float) Math.cos(ang) * 4f;
				pCar.y += (float) Math.sin(ang) * 4f;

			}

			// Fresnel effect to work out the amount of speed to reduce and damage to deal
			float fresnelTerm = (Vector2f.dot(nx, ny, collisionVel.x, collisionVel.y));
			pCar.speed = pCar.speed * (1f - Math.abs(fresnelTerm * 0.6f));
			int lDamAmt = (int) Math.abs(fresnelTerm * pCar.speed / 100f);
			pCar.dealDamage(lDamAmt);

		}

	}

	private void checkAllCarCollisions(LintfordCore pCore) {
		final int lCarCount = mCarManager.cars().size();
		for (int i = 0; i < lCarCount; i++) {
			BaseCar lCar0 = mCarManager.cars().get(i);

			// Check Car / Car collisions
			for (int j = i + 1; j < lCarCount; j++) {
				BaseCar lCar1 = mCarManager.cars().get(j);
				checkCarCarCollision(pCore, lCar0, lCar1);

			}

			// Check for car/person colls
			final int lMobCount = mMobManager.mobs().size();
			for (int j = 0; j < lMobCount; j++) {
				BaseMob lMob = mMobManager.mobs().get(j);

				if (lMob.isAlive)
					checkCarMobCollision(pCore, mCarManager.playerCar(), lMob);

			}

			// Player car/car collision
			if (mCarManager.playerCar() != lCar0)
				checkCarCarCollision(pCore, lCar0, mCarManager.playerCar());

		}

	}

	private void checkCarMobCollision(LintfordCore pCore, BaseCar pCar, BaseMob pMob) {
		if (pCar.intersects(pMob)) {
			pMob.isAlive = false;

			float lDist = (float) Math.sqrt((pCar.x - pMob.x) * (pCar.x - pMob.x) + (pCar.y - pMob.y) * (pCar.y - pMob.y));
			if (lDist < pCar.height + pMob.radius) {
				float lAngle = (float) Math.atan2(pCar.y - pMob.y, pCar.x - pMob.x);
				float lForce = 100.2f;
				float lAmt = (pCar.height + pMob.radius - lDist) / (pCar.height + pMob.radius);

				// pCar.dx += Math.cos(lAngle) * lForce * lAmt;
				// pCar.dy += Math.sin(lAngle) * lForce * lAmt;

				pMob.dx = -(float) Math.cos(lAngle) * lForce * lAmt;
				pMob.dy = -(float) Math.sin(lAngle) * lForce * lAmt;

				pCar.speed *= 0.96f;

			}

		}
	}

	private void checkCarCarCollision(LintfordCore pCore, BaseCar pCar0, BaseCar pCar1) {
		if (SAT.intersects(pCar0.bounds(), pCar1.bounds())) {
			collisionVel.x = (float) Math.cos(pCar0.rotation) * pCar0.speed;
			collisionVel.y = (float) Math.sin(pCar0.rotation) * pCar0.speed;

			float lColLenX = pCar1.x - pCar0.x;
			float lColLenY = pCar1.y - pCar0.y;

			if (Math.abs(lColLenX) > Math.abs(lColLenY)) {
				collisionNor.x = pCar1.x < pCar0.x ? 01f : -1f;
				collisionNor.y = 0f;
			} else {
				collisionNor.x = 0f;
				collisionNor.y = pCar1.y < pCar0.y ? 1f : -1f;
			}

			collisionVel.nor();
			reflectionVelNor = collisionVel.reflected(collisionNor);

			// Steer away from the wall // check: when going forwards?
			{
				float lPosX = pCar0.x;
				float lPosY = pCar0.y;
				float lFaceThisX = lPosX + reflectionVelNor.x;
				float lFaceThisY = lPosY + reflectionVelNor.y;

				pCar0.rotation = turnToFace(lPosX, lPosY, lFaceThisX, lFaceThisY, pCar0.rotation, 0.25f);
				pCar0.steerAngle = turnToFace(lPosX, lPosY, lFaceThisX, lFaceThisY, pCar0.rotation, 0.25f);

			}

			// TOOD: There is a faster distance check to be done first (no sqrt) ...

			float lDist = (float) Math.sqrt((pCar0.x - pCar1.x) * (pCar0.x - pCar1.x) + (pCar0.y - pCar1.y) * (pCar0.y - pCar1.y));
			if (lDist < pCar0.height + pCar1.height) {
				float lAngle = (float) Math.atan2(pCar0.y - pCar1.y, pCar0.x - pCar1.x);
				float lForce = 0.2f;
				float lAmt = (pCar0.height + pCar1.height - lDist) / (pCar0.height + pCar1.height);

				pCar0.dx += Math.cos(lAngle) * lForce * lAmt;
				pCar0.dy += Math.sin(lAngle) * lForce * lAmt;

				pCar1.dx -= Math.cos(lAngle) * lForce * lAmt;
				pCar1.dy -= Math.sin(lAngle) * lForce * lAmt;

				pCar0.speed *= 0.8f;
				pCar1.speed *= 0.8f;

			}
			
			// Fresnel effect to work out the amount of speed to reduce and damage to deal
			// FIXME: This is not the correct vector (velocity ??)
			float fresnelTerm = (Vector2f.dot(collisionVel.x, collisionVel.y, collisionVel.x, collisionVel.y));
			float lDamAmt0 = Math.abs(fresnelTerm * pCar0.speed / 100f);
			float lDamAmt1 = Math.abs(fresnelTerm * pCar1.speed / 100f);
			pCar0.speed = pCar0.speed * (1f - Math.abs(fresnelTerm * 0.6f));
			pCar1.speed = pCar1.speed * (1f - Math.abs(fresnelTerm * 0.6f));
			pCar0.dealDamage((int) lDamAmt0);
			pCar1.dealDamage((int) lDamAmt1);

		}

	}

	public static float turnToFace(float pPosX, float pPosY, float pFaceThisX, float pFaceThisY, float pCurrentAngle, float pTurnSpeed) {
		float x = pFaceThisX - pPosX;
		float y = pFaceThisY - pPosY;

		float desiredAngle = (float) Math.atan2(y, x);
		float difference = wrapAngle(desiredAngle - pCurrentAngle);

		// clamp
		difference = clamp(difference, -pTurnSpeed, pTurnSpeed);

		return wrapAngle(difference);

	}

	public static float wrapAngle(float radians) {
		while (radians < -Math.PI) {
			radians += Math.PI * 2;
		}
		while (radians > Math.PI) {
			radians -= Math.PI * 2;
		}
		return radians;
	}

	static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

	public BaseMob getRandomActiveMob() {
		if (mWorld == null || mMobManager.mobs() == null || mMobManager.mobs().size() == 0)
			return null;

		return mMobManager.mobs().get(RandomNumbers.random(0, mMobManager.mobs().size()));

	}

}
