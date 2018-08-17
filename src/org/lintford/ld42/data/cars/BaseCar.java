package org.lintford.ld42.data.cars;

import org.lintford.ld42.data.level.LevelNode;
import org.lintford.ld42.data.level.RoadSection;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.data.entities.RectangleEntity;

public class BaseCar extends RectangleEntity {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -1357154438862304967L;

	public static final int WIDTH = 32;
	public static final int HEIGHT = 64;

	public static final int CAR_TYPE_PLAYER = 0;
	public static final int CAR_TYPE_POLICE = 1;
	public static final int CAR_TYPE_CIV00 = 2;
	public static final int CAR_TYPE_CIV01 = 3;
	
	public static final int TURNING_ANGLE_INCREMENTS_DEG = 1;
	public static final int MAX_TURNING_ANGLE_DEGS = 35; // def. 35

	// --------------------------------------
	// Variables
	// --------------------------------------

	public final int maxHealth = 100;
	public int health = 100;

	public RoadSection currentRoadSection;
	public LevelNode targetNode;
	public LevelNode currentNode;

	public Vector2f mFrontWheels;
	public Vector2f mRearWheels;

	// maxes from def
	public int spriteIndex;
	public int carType;
	public float carSpeedMax;
	public float carTurnAngleMax;
	public float wheelBase;
	public float colorR, colorG, colorB;

	// cur values
	public float speed;
	public float carTurnAngleInc;
	public float steerAngle;
	public boolean handBrakeOn;
	public boolean isSteering;
	public int turnRank;

	public float mDustTimer;
	public float mSmokeTimer;
	public float mSkidMarkTimer;

	// --------------------------------------
	// properties
	// --------------------------------------

	public int health() {
		return health;
	}

	public void dealDamage(int pAmt) {
		health -= pAmt;
		if (health < 0)
			health = 0;

	}

	public boolean turningLeft() {
		return steerAngle < 0;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public BaseCar() {
		super(WIDTH, HEIGHT);

		mFrontWheels = new Vector2f();
		mRearWheels = new Vector2f();

		wheelBase = HEIGHT / 2;
		carSpeedMax = 1000 * RandomNumbers.random(0.90f, 1.1f);
		carTurnAngleInc = (float) Math.toRadians(TURNING_ANGLE_INCREMENTS_DEG);
		carTurnAngleMax = (float) Math.toRadians(MAX_TURNING_ANGLE_DEGS);

		colorR = 1f;
		colorG = 1f;
		colorB = 1f;

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mDustTimer += pCore.time().elapseGameTimeMilli();
		mSmokeTimer += pCore.time().elapseGameTimeMilli();
		mSkidMarkTimer += pCore.time().elapseGameTimeMilli();

	}

}
