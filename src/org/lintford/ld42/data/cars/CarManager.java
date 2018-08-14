package org.lintford.ld42.data.cars;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.data.BaseData;

public class CarManager extends BaseData {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 4611666884886213537L;

	public static final int POOL_SIZE = 24;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private List<BaseCar> mCarPool;
	private List<BaseCar> mCars;

	private BaseCar mPlayerCar;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public BaseCar playerCar() {
		return mPlayerCar;

	}

	public List<BaseCar> cars() {
		return mCars;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public CarManager() {
		mCarPool = new ArrayList<>();
		mCars = new ArrayList<>();

		mPlayerCar = new BaseCar();
		mPlayerCar.carType = 2;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		for (int i = 0; i < POOL_SIZE; i++) {
			mCarPool.add(new BaseCar());

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addCar(BaseCar pNewCar) {
		if (!mCars.contains(pNewCar)) {
			mCars.add(pNewCar);

		}

	}

	public void removeCar(BaseCar pNewCar) {
		if (mCars.contains(pNewCar)) {
			mCars.remove(pNewCar);

		}

	}

	public BaseCar getCarFromPool() {
		if (mCarPool.size() == 0)
			return null;

		return mCarPool.remove(0);

	}

	public void addCarToPool(BaseCar pCar) {
		if (!mCarPool.contains(pCar))
			mCarPool.add(pCar);

	}

}
