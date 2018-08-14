package org.lintford.ld42.data;

import org.lintford.ld42.data.cars.CarManager;
import org.lintford.ld42.data.level.Level;
import org.lintford.ld42.data.mission.MissionManager;
import org.lintford.ld42.data.mobs.MobManager;

import net.lintford.library.data.BaseData;

public class World extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 9205285203893326732L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private GameState mGameState;
	private GameParticleSystems mGameParticles;
	private Level mLevel;

	private CarManager mCarManager;
	private MobManager mMobManager;
	private MissionManager mMissionManager;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public MissionManager missionManager() {
		return mMissionManager;
	}

	public CarManager carManager() {
		return mCarManager;
	}

	public MobManager mobManager() {
		return mMobManager;
	}

	public GameParticleSystems gameParticles() {
		return mGameParticles;
	}

	public Level level() {
		return mLevel;
	}

	public GameState gameState() {
		return mGameState;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public World() {
		mGameState = new GameState();
		mGameParticles = new GameParticleSystems();
		mLevel = new Level();

		mCarManager = new CarManager();
		mMobManager = new MobManager();
		mMissionManager = new MissionManager();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		mCarManager.initialize();
		mMobManager.initialize();
		mMissionManager.initialize();

		mGameParticles.initialize();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

}
