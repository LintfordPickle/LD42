package org.lintford.ld42.data;

import java.util.List;

import org.lintford.ld42.controllers.GameParticleController;
import org.lintford.ld42.data.cars.BaseCar;
import org.lintford.ld42.data.mobs.BaseMob;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.particles.Particle;
import net.lintford.library.core.graphics.particles.modifiers.IParticleModifier;
import net.lintford.library.core.maths.RandomNumbers;

/** Particles collide with ground */
public class ParticleBulletCollisionModifier implements IParticleModifier {

	// --------------------------------------
	// Variables
	// --------------------------------------

	World mWorld;
	GameParticleController mGameParticleController;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ParticleBulletCollisionModifier(GameParticleController pParticleController, World pWorld) {
		mWorld = pWorld;
		mGameParticleController = pParticleController;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(Particle pParticle) {

	}

	@Override
	public void update(LintfordCore pCore) {

	}

	@Override
	public void updateParticle(LintfordCore pCore, Particle pParticle) {
		List<BaseCar> lCars = mWorld.carManager().cars();
		List<BaseMob> lMobs = mWorld.mobManager().mobs();

		final float lPartX = pParticle.x;
		final float lPartY = pParticle.y;
		final float lPartRad = pParticle.radius;

		final int lCarCount = lCars.size();
		for (int i = 0; i < lCarCount; i++) {
			BaseCar lCar = lCars.get(i);

			// TODO: Quick distance check

			float lDist = (float) Math.sqrt((lCar.x - lPartX) * (lCar.x - lPartX) + (lCar.y - lPartY) * (lCar.y - lPartY));
			if (lDist < lCar.height + lPartRad) {
				pParticle.reset();
				lCar.dealDamage(RandomNumbers.random(4, 20));

			}

		}

		final int lMobCount = lMobs.size();
		for (int i = 0; i < lMobCount; i++) {
			BaseMob lMob = lMobs.get(i);

			// TODO: Quick distance check

			float lDist = (float) Math.sqrt((lMob.x - lPartX) * (lMob.x - lPartX) + (lMob.y - lPartY) * (lMob.y - lPartY));
			if (lDist < 16f + lPartRad) {
				pParticle.reset();
				lMob.isAlive = false;

			}

		}

	}

}
