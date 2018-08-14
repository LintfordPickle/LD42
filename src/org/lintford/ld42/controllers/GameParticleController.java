package org.lintford.ld42.controllers;

import org.lintford.ld42.data.GameParticleSystems;
import org.lintford.ld42.data.ParticleBulletCollisionModifier;
import org.lintford.ld42.data.World;
import org.lintford.ld42.data.level.RoadSection;
import org.lintford.ld42.screens.GameWorldScreen;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.particles.ParticleSystem;
import net.lintford.library.core.graphics.particles.initialisers.ParticleRandomRotationInitialiser;
import net.lintford.library.core.graphics.particles.initialisers.ParticleRandomSizeInitialiser;
import net.lintford.library.core.graphics.particles.initialisers.ParticleSourceRegionInitialiser;
import net.lintford.library.core.graphics.particles.initialisers.ParticleTurnToFaceInitialiser;
import net.lintford.library.core.graphics.particles.modifiers.ParticleLifetimeAlphaFadeInOutModifier;
import net.lintford.library.core.graphics.particles.modifiers.ParticleLifetimeAlphaFadeOutModifier;
import net.lintford.library.core.graphics.particles.modifiers.ParticleLifetimeModifier;
import net.lintford.library.core.graphics.particles.modifiers.ParticlePhysicsModifier;
import net.lintford.library.core.maths.RandomNumbers;

public class GameParticleController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "GameParticleController";

	private float MOB_SPAWN_TIMER = 350;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;
	private GameParticleSystems mGameParticleSystems;

	private LevelController mLevelController;

	private float mRubbishSpawnTimer;

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

	public GameParticleController(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;
		mGameParticleSystems = pWorld.gameParticles();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, groupID());

		createParticleSystems();

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		spawnRubbish(pCore);

		final int lParticleSystemCount = mGameParticleSystems.particleSystems().size();
		for (int i = 0; i < lParticleSystemCount; i++) {
			ParticleSystem lSystem = mGameParticleSystems.particleSystems().get(i);

			lSystem.update(pCore);

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void createParticleSystems() {
		ParticleSystem lSkidMarksParticleSystem = new ParticleSystem("SkidMarks");
		ParticleSystem lBloodParticleSystem = new ParticleSystem("Blood");
		ParticleSystem lDustParticleSystem = new ParticleSystem("Dust");
		ParticleSystem lSmokeParticleSystem = new ParticleSystem("Smoke");
		ParticleSystem lDamageSmokeParticleSystem = new ParticleSystem("DamageSmoke");
		ParticleSystem lDamageFireParticleSystem = new ParticleSystem("DamageFire");
		ParticleSystem lRubbishParticleSystem = new ParticleSystem("Rubbish", 128);
		ParticleSystem lBulletsParticleSystem = new ParticleSystem("Bullets");

		mGameParticleSystems.particleSystems().add(lSkidMarksParticleSystem);
		mGameParticleSystems.particleSystems().add(lBloodParticleSystem);
		mGameParticleSystems.particleSystems().add(lSmokeParticleSystem);
		mGameParticleSystems.particleSystems().add(lDamageSmokeParticleSystem);
		mGameParticleSystems.particleSystems().add(lDamageFireParticleSystem);
		mGameParticleSystems.particleSystems().add(lBulletsParticleSystem);
		mGameParticleSystems.particleSystems().add(lDustParticleSystem);
		mGameParticleSystems.particleSystems().add(lRubbishParticleSystem);

		// Build the particle systems
		lBulletsParticleSystem.addInitialiser(new ParticleTurnToFaceInitialiser());
		lBulletsParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(0, 0, 32, 32));

		lBulletsParticleSystem.addModifier(new ParticleLifetimeModifier());
		lBulletsParticleSystem.addModifier(new ParticlePhysicsModifier());
		lBulletsParticleSystem.addModifier(new ParticleBulletCollisionModifier(this, mWorld));

		// Rubbish
		lRubbishParticleSystem.addInitialiser(new ParticleTurnToFaceInitialiser());
		lRubbishParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(32, 0, 32, 32));
		lRubbishParticleSystem.addInitialiser(new ParticleRandomSizeInitialiser(0.4f, 1.0f));
		lRubbishParticleSystem.addInitialiser(new ParticleRandomRotationInitialiser(0, 360));

		lRubbishParticleSystem.addModifier(new ParticleLifetimeModifier());
		lRubbishParticleSystem.addModifier(new ParticlePhysicsModifier());
		lRubbishParticleSystem.addModifier(new ParticleLifetimeAlphaFadeInOutModifier());

		// SkidMarks
		lSkidMarksParticleSystem.addInitialiser(new ParticleTurnToFaceInitialiser());
		lSkidMarksParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(64, 0, 32, 32));

		lSkidMarksParticleSystem.addModifier(new ParticleLifetimeModifier());
		lSkidMarksParticleSystem.addModifier(new ParticleLifetimeAlphaFadeOutModifier());

		// Smoke
		lSmokeParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(0, 32, 32, 32));
		lSmokeParticleSystem.addInitialiser(new ParticleRandomSizeInitialiser(1f, 2.0f));
		lSmokeParticleSystem.addInitialiser(new ParticleRandomRotationInitialiser(0, 360));

		lSmokeParticleSystem.addModifier(new ParticleLifetimeModifier());
		lSmokeParticleSystem.addModifier(new ParticlePhysicsModifier());
		lSmokeParticleSystem.addModifier(new ParticleLifetimeAlphaFadeInOutModifier());

		// DamageSmoke
		lDamageSmokeParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(32, 32, 64, 64));
		lDamageSmokeParticleSystem.addInitialiser(new ParticleRandomSizeInitialiser(1f, 3.0f));
		lDamageSmokeParticleSystem.addInitialiser(new ParticleRandomRotationInitialiser(0, 360));

		lDamageSmokeParticleSystem.addModifier(new ParticleLifetimeModifier());
		lDamageSmokeParticleSystem.addModifier(new ParticlePhysicsModifier());
		lDamageSmokeParticleSystem.addModifier(new ParticleLifetimeAlphaFadeInOutModifier());

		// Damage Fire
		lDamageFireParticleSystem.addInitialiser(new ParticleSourceRegionInitialiser(96, 32, 64, 64));
		lDamageFireParticleSystem.addInitialiser(new ParticleRandomSizeInitialiser(1f, 3.0f));
		lDamageFireParticleSystem.addInitialiser(new ParticleRandomRotationInitialiser(0, 360));

		lDamageFireParticleSystem.addModifier(new ParticleLifetimeModifier());
		lDamageFireParticleSystem.addModifier(new ParticlePhysicsModifier());
		lDamageFireParticleSystem.addModifier(new ParticleLifetimeAlphaFadeInOutModifier());

	}

	public void fireBullets(float pSrcX, float pSrcY, float pVelX, float pVelY) {
		ParticleSystem lBulletParticlesSystem = mGameParticleSystems.particleSystem("Bullets");

		if (lBulletParticlesSystem == null)
			return;

		lBulletParticlesSystem.spawnParticle(pSrcX, pSrcY, pVelX, pVelY, 1500f);

	}

	public void addSkidMarks(float pSrcX, float pSrcY, float pVelX, float pVelY) {
		ParticleSystem lSkidMarksParticlesSystem = mGameParticleSystems.particleSystem("SkidMarks");

		if (lSkidMarksParticlesSystem == null)
			return;

		lSkidMarksParticlesSystem.spawnParticle(pSrcX, pSrcY, pVelX, pVelY, 15000f);

	}

	public void addSmoke(float pSrcX, float pSrcY, float pVelX, float pVelY) {
		ParticleSystem lSmokeParticlesSystem = mGameParticleSystems.particleSystem("Smoke");

		if (lSmokeParticlesSystem == null)
			return;

		lSmokeParticlesSystem.spawnParticle(pSrcX, pSrcY, pVelX, pVelY, 750f);

	}

	public void addDamageSmoke(float pSrcX, float pSrcY, float pVelX, float pVelY) {
		ParticleSystem lDamageSmokeParticlesSystem = mGameParticleSystems.particleSystem("DamageSmoke");

		if (lDamageSmokeParticlesSystem == null)
			return;

		lDamageSmokeParticlesSystem.spawnParticle(pSrcX, pSrcY, pVelX, pVelY, RandomNumbers.random(500f, 1500f));

	}

	public void addDamageFire(float pSrcX, float pSrcY, float pVelX, float pVelY) {
		ParticleSystem lDamageFireParticlesSystem = mGameParticleSystems.particleSystem("DamageFire");

		if (lDamageFireParticlesSystem == null)
			return;

		lDamageFireParticlesSystem.spawnParticle(pSrcX, pSrcY, pVelX, pVelY, RandomNumbers.random(500f, 1500f));

	}

	private void spawnRubbish(LintfordCore pCore) {
		mRubbishSpawnTimer += pCore.time().elapseGameTimeMilli();

		if (mRubbishSpawnTimer > 100f) {
			ParticleSystem lRubbishParticlesSystem = mGameParticleSystems.particleSystem("Rubbish");

			if (lRubbishParticlesSystem == null)
				return;

			RoadSection lRoad = mLevelController.getRandomRoadSection();

			float lPosX = lRoad.left() + RandomNumbers.random(0, GameWorldScreen.GRID_SIZE);
			float lPosY = lRoad.top() + RandomNumbers.random(0, GameWorldScreen.GRID_SIZE);
			float lAngle = (float) Math.toRadians(RandomNumbers.random(0, 45) - 22);

			float lVelX = (float) Math.cos(lAngle) * RandomNumbers.random(20, 70);
			float lVelY = (float) Math.sin(lAngle) * RandomNumbers.random(20, 70);

			lRubbishParticlesSystem.spawnParticle(lPosX, lPosY, lVelX, lVelY, 15000f);

			mRubbishSpawnTimer = 0;

		}

	}

}
