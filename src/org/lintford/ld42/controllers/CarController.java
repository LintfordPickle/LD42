package org.lintford.ld42.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.cars.BaseCar;
import org.lintford.ld42.data.cars.CarManager;
import org.lintford.ld42.data.level.LevelNode;
import org.lintford.ld42.data.level.RoadSection;
import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.RandomNumbers;

public class CarController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "CarController";

	static final float CAR_ACCELERATION = 15.0f;

	private float VEHICLE_SPAWN_TIMER = 350;
	private float VEHICLE_SHOOT_TIMER = 100;
	private float VEHICLE_SKIDMARK_TIMER = 50;
	private float VEHICLE_DUST_TIMER = 75;
	private float VEHICLE_SMOKE_TIMER = 150;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;
	private CarManager mCarManager;
	private LevelController mLevelController;
	private GameParticleController mGameParticleController;

	private List<BaseCar> mUpdateCarList;
	private float spawnTimer;

	private boolean mShootAltPosition;
	private float mShootTimer;

	private float mSkidMarkTimer;

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

	public CarController(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;
		mCarManager = mWorld.carManager();

		mUpdateCarList = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, mGroupID);
		mGameParticleController = (GameParticleController) pCore.controllerManager().getControllerByNameRequired(GameParticleController.CONTROLLER_NAME, mGroupID);

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (handlePlayerInput(pCore, mCarManager.playerCar()))
			return true;

		if (Debug.debugManager().debugManagerEnabled() && pCore.input().keyDownTimed(GLFW.GLFW_KEY_F8)) {
			BaseCar lPlayerCar = mCarManager.playerCar();

			lPlayerCar.x = mWorld.level().playerSpawnPosition().x;
			lPlayerCar.y = mWorld.level().playerSpawnPosition().y;
			lPlayerCar.rotation = 0;
			lPlayerCar.speed = 0;
			lPlayerCar.steerAngle = 0;

		}

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mShootTimer += pCore.time().elapseGameTimeMilli();
		mSkidMarkTimer += pCore.time().elapseGameTimeMilli();

		updateVehicleSpawns(pCore);

		updateCar(pCore, mCarManager.playerCar());

		mUpdateCarList.clear();
		final int lActiveCarCount = mCarManager.cars().size();
		for (int i = 0; i < lActiveCarCount; i++) {
			mUpdateCarList.add(mCarManager.cars().get(i));

		}

		final int lUpdateCarCount = mUpdateCarList.size();
		for (int i = 0; i < lUpdateCarCount; i++) {
			BaseCar lCar = mUpdateCarList.get(i);

			if (!lCar.currentRoadSection.isActive) {
				mCarManager.cars().remove(lCar);
				mCarManager.addCarToPool(lCar);

				continue;
			}

			updateCarAI(pCore, lCar);
			updateCar(pCore, lCar);

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void updateCar(LintfordCore pCore, BaseCar pCar) {

		// Check if the car has been destroyed
		if (pCar.health <= 0) {
			pCar.carType = 3;
			pCar.turnRank = 0;

		} else {
			pCar.dx += (float) Math.cos(pCar.rotation + pCar.steerAngle) * (pCar.speed / pCar.carSpeedMax) * .05f;
			pCar.dy += (float) Math.sin(pCar.rotation + pCar.steerAngle) * (pCar.speed / pCar.carSpeedMax) * .05f;

			pCar.x += pCar.dx * pCore.time().elapseGameTimeMilli();
			pCar.y += pCar.dy * pCore.time().elapseGameTimeMilli();

			pCar.dx *= 0.2f;
			pCar.dy *= 0.2f;

			// limit params
			pCar.speed = MathHelper.clamp(pCar.speed, -pCar.carSpeedMax * 0.5f, pCar.carSpeedMax);
			pCar.steerAngle = MathHelper.clamp(pCar.steerAngle, -pCar.carTurnAngleMax, pCar.carTurnAngleMax);

			// Try to make the handling less efficient at higher speeds
			if (Math.abs(pCar.speed) > 15) {
				float amt = (pCar.speed / pCar.carSpeedMax);
				pCar.dr += pCar.steerAngle * amt;

			}

			float supAmt = 0;

			// Project the wheel out along the heading direction, from the center of the vehicle
			pCar.mFrontWheels.x = pCar.x + pCar.wheelBase / 2 * (float) Math.cos(pCar.rotation);
			pCar.mFrontWheels.y = pCar.y + pCar.wheelBase / 2 * (float) Math.sin(pCar.rotation);

			pCar.mRearWheels.x = pCar.x - pCar.wheelBase / 2 * (float) Math.cos(pCar.rotation);
			pCar.mRearWheels.y = pCar.y - pCar.wheelBase / 2 * (float) Math.sin(pCar.rotation);

			if (pCar.speed > 1 && pCar.handBrakeOn) {
				supAmt = (float) Math.toRadians((1f / pCar.speed) * 200f);

			}

			// Move the wheels
			// Rear wheels move in carHeading
			pCar.mRearWheels.x += pCar.speed * pCore.time().elapseGameTimeSeconds() * (float) Math.cos(pCar.rotation - supAmt);
			pCar.mRearWheels.y += pCar.speed * pCore.time().elapseGameTimeSeconds() * (float) Math.sin(pCar.rotation - supAmt);

			// Front wheels move along carHeading + steering direction
			pCar.mFrontWheels.x += pCar.speed * pCore.time().elapseGameTimeSeconds() * (float) Math.cos(pCar.rotation + pCar.steerAngle);
			pCar.mFrontWheels.y += pCar.speed * pCore.time().elapseGameTimeSeconds() * (float) Math.sin(pCar.rotation + pCar.steerAngle);

			// Extrapolate the new car location (center of new wheel position)
			pCar.x = (pCar.mFrontWheels.x + pCar.mRearWheels.x) / 2;
			pCar.y = (pCar.mFrontWheels.y + pCar.mRearWheels.y) / 2;

			pCar.speed *= 0.98f;
			pCar.steerAngle *= 0.98f;
			pCar.rotation = (float) Math.atan2(pCar.mFrontWheels.y - pCar.mRearWheels.y, pCar.mFrontWheels.x - pCar.mRearWheels.x);// + (float)Math.toRadians(90);
			if (pCar.rotation < 0)
				pCar.rotation += 2 * Math.PI;

			if (pCar.handBrakeOn) {
				// TODO: This should spin the back out, not rotation on center!
				pCar.rotation += pCar.dr * 0.02f;

			}

			if (pCar.handBrakeOn)
				pCar.dr *= 0.91f;
			else
				pCar.dr *= 0.7f;

			if (Math.abs(pCar.steerAngle) > 0.2f && Math.abs(pCar.speed) > 300)
				pCar.turnRank = 2;
			else if (Math.abs(pCar.steerAngle) > 0.02f && Math.abs(pCar.speed) > 200)
				pCar.turnRank = 1;
			else
				pCar.turnRank = 0;
		}

		pCar.update(pCore);

		// ************************************
		// Particles
		// ************************************

		// Just a random chance for other vehicle to knock out a skid mark
		if (pCar.mSkidMarkTimer > VEHICLE_SKIDMARK_TIMER) {
			if (RandomNumbers.RANDOM.nextInt(1000) < 2f) {
				float lVelX = (float) Math.cos(pCar.rotation) * 10;
				float lVelY = (float) Math.sin(pCar.rotation) * 10;
				mGameParticleController.addSkidMarks(pCar.x, pCar.y, lVelX, lVelY);

				pCar.mSkidMarkTimer = 0;

			}

		}

		if (pCar.mDustTimer > VEHICLE_DUST_TIMER) {
			if (pCar.speed > 100f) {
				float lVelX = (float) Math.cos(pCar.rotation) * 10;
				float lVelY = (float) Math.sin(pCar.rotation) * 10;
				mGameParticleController.addSmoke(pCar.x, pCar.y, lVelX, lVelY);

				pCar.mDustTimer = 0;

			}

		}

		if (pCar.mSmokeTimer > VEHICLE_SMOKE_TIMER) {
			if (pCar.health < 20f) { // FIRE
				float lVelX = (float) Math.cos(Math.toRadians(RandomNumbers.random(0, 55f) - 25f)) * 55f;
				float lVelY = (float) Math.sin(Math.toRadians(RandomNumbers.random(0, 55f) - 25f)) * 55f;
				mGameParticleController.addDamageFire(pCar.x, pCar.y, lVelX, lVelY);

				pCar.mSmokeTimer = 0;

			} else if (pCar.health < 50f) { // SMOKE
				float lVelX = (float) Math.cos(Math.toRadians(RandomNumbers.random(0, 55f) - 25f)) * 55f;
				float lVelY = (float) Math.sin(Math.toRadians(RandomNumbers.random(0, 55f) - 25f)) * 55f;
				mGameParticleController.addDamageSmoke(pCar.x, pCar.y, lVelX, lVelY);

				pCar.mSmokeTimer = 0;

			}

		}

	}

	private void updateCarAI(LintfordCore pCore, BaseCar pCar) {
		if (pCar.targetNode == null) {
			pCar.targetNode = pCar.currentNode.getRandomConnection();
			return;
		}

		// Move the car towards the target node
		final float lHeadingVecX = (pCar.targetNode.x) - pCar.x;
		final float lHeadingVecY = (pCar.targetNode.y) - pCar.y;

		// Upon arrival, chose a new destination
		float dist = (float) Math.sqrt((lHeadingVecX * lHeadingVecX) + (lHeadingVecY * lHeadingVecY));

		if (dist > 32) {
			pCar.rotation = MobController.turnToFace(pCar.x, pCar.y, pCar.targetNode.x, pCar.targetNode.y, pCar.rotation, 0.25f);
			pCar.steerAngle = MobController.turnToFace(pCar.x, pCar.y, pCar.targetNode.x, pCar.targetNode.y, pCar.steerAngle, 5.25f);
			pCar.isSteering = true;

			pCar.speed += (CAR_ACCELERATION * 0.25f) * RandomNumbers.random(0.9f, 1.1f);

		} else {
			LevelNode lNewCurrent = pCar.targetNode;

			if (RandomNumbers.getRandomChance(60)) {
				pCar.targetNode = pCar.targetNode.getRandomConnection(lHeadingVecX, lHeadingVecY);
			} else
				pCar.targetNode = pCar.targetNode.getRandomConnection();

			pCar.currentRoadSection = lNewCurrent.parentRoadSection();
			pCar.currentNode = lNewCurrent;
		}

	}

	private boolean handlePlayerInput(LintfordCore pCore, BaseCar pCar) {
		BaseCar lPlayerCar = mCarManager.playerCar();

		if (pCore.input().keyDown(GLFW.GLFW_KEY_F)) {

			if (mShootTimer > VEHICLE_SHOOT_TIMER) {
				float lXOffsetAmt = mShootAltPosition ? -90 : +90;

				float lSideOffX = (float) Math.cos(mCarManager.playerCar().rotation + lXOffsetAmt) * -8;
				float lSideOffY = (float) Math.sin(mCarManager.playerCar().rotation + lXOffsetAmt) * -8;

				float lFrontOffX = (float) Math.cos(mCarManager.playerCar().rotation) * 32;
				float lFrontOffY = (float) Math.sin(mCarManager.playerCar().rotation) * 32;

				float lPosX = mCarManager.playerCar().x - lSideOffX + lFrontOffX;
				float lPosY = mCarManager.playerCar().y - lSideOffY + lFrontOffY;
				float lVelX = (float) Math.cos(mCarManager.playerCar().rotation) * 1250f;
				float lVelY = (float) Math.sin(mCarManager.playerCar().rotation) * 1250f;

				mGameParticleController.fireBullets(lPosX, lPosY, lVelX, lVelY);

				mShootAltPosition = !mShootAltPosition;
				mShootTimer = 0;
			}

		}

		boolean isSteering = false;
		if (pCore.input().keyDown(GLFW.GLFW_KEY_W)) {

			if (!lPlayerCar.handBrakeOn)
				lPlayerCar.speed += CAR_ACCELERATION;
			else
				lPlayerCar.speed += CAR_ACCELERATION * 0.5f;

			// add Smoke!
			float lPosX = mCarManager.playerCar().x;
			float lPosY = mCarManager.playerCar().y;
			float lVelX = (float) Math.cos(mCarManager.playerCar().rotation);
			float lVelY = (float) Math.sin(mCarManager.playerCar().rotation);

			mGameParticleController.addSmoke(lPosX, lPosY, lVelX, lVelY);

		}

		if (pCore.input().keyDown(GLFW.GLFW_KEY_S)) {
			lPlayerCar.speed -= CAR_ACCELERATION * 0.7f;
		}

		if (pCore.input().keyDown(GLFW.GLFW_KEY_A)) {
			lPlayerCar.steerAngle += -lPlayerCar.carTurnAngleInc;
			isSteering = true;
		}

		if (pCore.input().keyDown(GLFW.GLFW_KEY_D)) {
			lPlayerCar.steerAngle += lPlayerCar.carTurnAngleInc;
			isSteering = true;
		}

		if (!isSteering)
			lPlayerCar.steerAngle *= 0.94f;

		if (pCore.input().keyDown(GLFW.GLFW_KEY_SPACE)) {
			if (lPlayerCar.speed > 0)
				lPlayerCar.speed -= CAR_ACCELERATION * 0.7f;
			if (lPlayerCar.speed < 0)
				lPlayerCar.speed += CAR_ACCELERATION * 0.7f;

			lPlayerCar.handBrakeOn = true;

			if (lPlayerCar.speed > 30f && mSkidMarkTimer > VEHICLE_SKIDMARK_TIMER) {
				mSkidMarkTimer = 0;

				float lPosX = mCarManager.playerCar().x;
				float lPosY = mCarManager.playerCar().y;
				float lVelX = (float) Math.cos(mCarManager.playerCar().rotation) * 10;
				float lVelY = (float) Math.sin(mCarManager.playerCar().rotation) * 10;

				mGameParticleController.addSkidMarks(lPosX, lPosY, lVelX, lVelY);
			}

		} else {
			lPlayerCar.handBrakeOn = false;

		}

		return false;

	}

	private void updateVehicleSpawns(LintfordCore pCore) {
		spawnTimer += pCore.time().elapseGameTimeMilli();

		if (spawnTimer > VEHICLE_SPAWN_TIMER) {
			if (mCarManager.cars().size() < CarManager.POOL_SIZE) {
				int lNumSpawns = RandomNumbers.random(0, 4);
				for (int i = 0; i < lNumSpawns; i++) {
					spawnNPCCar();

				}

			}

			spawnTimer = 0;
		}

		if (mCarManager.cars().size() < 10) {
			int lNumSpawns = 10 - mCarManager.cars().size();
			for (int i = 0; i < lNumSpawns; i++) {
				spawnNPCCar();

			}
		}

	}

	private void spawnNPCCar() {
		List<RoadSection> lActiveRoadSections = mLevelController.activeRoadSections();
		final int lNumActiveRoadSections = lActiveRoadSections.size();

		if (lNumActiveRoadSections == 0)
			return;

		RoadSection lRoad = null;

		// Need to try and get a road instance which is both actie but offscreen
		int lTries = 3;
		while (lTries > 0) {
			lRoad = lActiveRoadSections.get(RandomNumbers.random(0, lNumActiveRoadSections));

			if (lRoad.isActive && !lRoad.isVisible)
				break; // we found one

			lTries--;
		}

		if (lRoad == null)
			return;

		BaseCar lNewCar = mCarManager.getCarFromPool();
		if (lNewCar == null) // pool empty
			return;

		// TODO: Extend all this
		lNewCar.carType = RandomNumbers.random(0, 3);

		if (lNewCar.carType == 0) {
			lNewCar.colorR = RandomNumbers.random(0f, 1f);
			lNewCar.colorG = RandomNumbers.random(0f, 1f);
			lNewCar.colorB = RandomNumbers.random(0f, 1f);

		} else {
			lNewCar.colorR = 1f;
			lNewCar.colorG = 1f;
			lNewCar.colorB = 1f;

		}

		lNewCar.currentRoadSection = lRoad;
		lNewCar.currentNode = lRoad.getRandomStartNode();
		lNewCar.targetNode = null;
		lNewCar.health = lNewCar.maxHealth;
		lNewCar.x = lNewCar.currentNode.x;
		lNewCar.y = lNewCar.currentNode.y;

		mCarManager.cars().add(lNewCar);
	}

}
