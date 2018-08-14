package org.lintford.ld42.data.mission;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.data.BaseData;

public class MissionManager extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 4611666884886213537L;

	public static final int POOL_SIZE = 16;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<Mission> mMissionPool;
	private List<Mission> mMissions;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<Mission> availableMissions() {
		return mMissions;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MissionManager() {
		mMissionPool = new ArrayList<>();
		mMissions = new ArrayList<>();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		for (int i = 0; i < POOL_SIZE; i++) {
			mMissionPool.add(new Mission());

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addMission(Mission pMission) {
		if (mMissionPool.contains(pMission)) {
			mMissionPool.remove(pMission);

		}

		if (!mMissions.contains(pMission)) {
			mMissions.add(pMission);

		}

	}

	public void removeMission(Mission pMission) {
		if (mMissions.contains(pMission)) {
			mMissions.remove(pMission);

		}

	}

	public Mission getMissionFromPool() {
		if (mMissionPool.size() == 0)
			return null;

		return mMissionPool.remove(0);

	}

	public void addMissionToPool(Mission pMission) {
		if (mMissions.contains(pMission))
			mMissions.remove(pMission);

		if (!mMissionPool.contains(pMission))
			mMissionPool.add(pMission);

	}

}
