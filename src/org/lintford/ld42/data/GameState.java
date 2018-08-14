package org.lintford.ld42.data;

import org.lintford.ld42.data.mission.Mission;

import net.lintford.library.data.BaseData;

public class GameState extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 3251734744843619797L;

	public static final float WANTED_TIME = 15000; // 15 seconds

	// --------------------------------------
	// Variables
	// --------------------------------------

	private int wantedLevel;
	private int mCredits;

	private float mWantedColldownTimer;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean isOnMission() {
		return currentMission != null;

	}

	public boolean isWanted() {
		return wantedLevel > 0;

	}

	public float wantedTimer() {
		return mWantedColldownTimer;
	}

	public void wantedTimer(float pNewTime) {
		if (pNewTime <= 0) {
			removeWantedLevel();
			return;

		} else if (pNewTime > WANTED_TIME) {
			pNewTime = WANTED_TIME;

		}

		mWantedColldownTimer = pNewTime;

	}

	public int credits() {
		return mCredits;
	}

	public void addCredits(int pCredits) {
		mCredits += pCredits;

		if (mCredits < 0)
			mCredits = 0;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Mission currentMission;

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setWantedLevel() {
		mWantedColldownTimer = WANTED_TIME;
	}

	public void removeWantedLevel() {
		mWantedColldownTimer = WANTED_TIME;
	}

}
