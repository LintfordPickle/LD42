package org.lintford.ld42.data;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.graphics.particles.ParticleSystem;
import net.lintford.library.data.BaseData;

public class GameParticleSystems extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -8713678038151277725L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<ParticleSystem> mParticleSystems;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<ParticleSystem> particleSystems() {
		return mParticleSystems;
	}

	public ParticleSystem particleSystem(String pName) {
		final int lParticleSystemCount = mParticleSystems.size();
		for (int i = 0; i < lParticleSystemCount; i++) {
			if (mParticleSystems.get(i).name().equals(pName)) {
				return mParticleSystems.get(i);

			}

		}

		return null;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameParticleSystems() {
		mParticleSystems = new ArrayList<>();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addParticleSystem(ParticleSystem pNewParticleSystem) {
		if (pNewParticleSystem == null)
			return;
		if (pNewParticleSystem.name() == null || pNewParticleSystem.name().isEmpty())
			return;

		if (!mParticleSystems.contains(pNewParticleSystem)) {
			mParticleSystems.add(pNewParticleSystem);

		}

	}

}
