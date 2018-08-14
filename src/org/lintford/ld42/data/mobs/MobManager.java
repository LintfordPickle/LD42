package org.lintford.ld42.data.mobs;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.data.BaseData;

public class MobManager extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 4611666884886213537L;

	public static final int POOL_SIZE = 128;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<BaseMob> mMobPool;
	private List<BaseMob> mMobs;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<BaseMob> mobs() {
		return mMobs;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MobManager() {
		mMobPool = new ArrayList<>();
		mMobs = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		for (int i = 0; i < POOL_SIZE; i++) {
			mMobPool.add(new BaseMob());

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addMob(BaseMob pNewMob) {
		if (mMobPool.contains(pNewMob)) {
			mMobPool.remove(pNewMob);

		}

		if (!mMobs.contains(pNewMob)) {
			mMobs.add(pNewMob);

		}

	}

	public void removeMob(BaseMob pNewMob) {
		if (!mMobPool.contains(pNewMob)) {
			mMobPool.add(pNewMob);

		}

		if (mMobs.contains(pNewMob)) {
			mMobs.remove(pNewMob);

		}

	}

	public BaseMob getMobFromPool() {
		if (mMobPool.size() == 0)
			return null;

		return mMobPool.remove(0);

	}

	public void addMobToPool(BaseMob pMob) {
		if (!mMobPool.contains(pMob))
			mMobPool.add(pMob);

	}

}
