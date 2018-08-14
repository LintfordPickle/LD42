package org.lintford.ld42.controllers;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.level.Building;
import org.lintford.ld42.data.level.Level;
import org.lintford.ld42.data.level.LevelNode;
import org.lintford.ld42.data.level.PickUpAreas;
import org.lintford.ld42.data.level.RoadSection;
import org.lintford.ld42.screens.GameWorldScreen;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;

public class IOWorldLoader extends BaseController {

	public class LevelPixelData {
		public int width;
		public int height;
		public int[] pixels;
	}

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "IOWorldLoader";

	public static final int TOP = 0b00000001;
	public static final int BOTTOM = 0b00000010;
	public static final int LEFT = 0b00000100;
	public static final int RIGHT = 0b00001000;

	final int EMPTY = 0xff000000;
	final int BUILDING = 0xffff0000;
	final int ROAD = 0xffffffff;

	public static final float MUL_FACTOR = GameWorldScreen.GRID_SIZE / 128f;

	public int pickupAreaIndex = 0;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private boolean mWorldLoaded;
	private boolean mPlayerSpawnPlaced;
	LevelPixelData mLevelPixelData;
	World mWorld;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean isLevelLoaded() {
		return mWorldLoaded;
	}

	@Override
	public boolean isInitialised() {
		return true;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public IOWorldLoader(ControllerManager pControllerManager, World pWorld, int pGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pGroupID);

		mWorld = pWorld;
		mPlayerSpawnPlaced = false;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {

	}

	@Override
	public void unload() {

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void loadWorld(String pFilename) {
		mLevelPixelData = getImagePixelData(pFilename);

		populateWorld();

		// After all road sections have been created, build a node graph
		buildRoadGraph();

		mWorldLoaded = true;

	}

	public void unloadWorld() {
		mLevelPixelData = null;
		mWorld = null;
		mWorldLoaded = false;

	}

	private LevelPixelData getImagePixelData(String pFilename) {
		LevelPixelData lReturn = new LevelPixelData();

		BufferedImage lImage = null;

		try {
			File lTextureFile = new File(pFilename);

			lImage = ImageIO.read(lTextureFile);

			lReturn.width = lImage.getWidth();
			lReturn.height = lImage.getHeight();
			lReturn.pixels = new int[lReturn.width * lReturn.height];
			lImage.getRGB(0, 0, lReturn.width, lReturn.height, lReturn.pixels, 0, lReturn.width);

		} catch (Exception e) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "Failed to load level from file: " + pFilename);
			Debug.debugManager().logger().printException(getClass().getSimpleName(), e);

			lReturn = null;

		}

		return lReturn;
	}

	private void populateWorld() {
		Level lLevel = mWorld.level();

		// Iterate over the map and create road segments and buildings
		int[] pixels = mLevelPixelData.pixels;

		final int width = mLevelPixelData.width;
		final int height = mLevelPixelData.height;

		final int lNumCells = pixels.length;
		for (int i = 0; i < lNumCells; i++) {
			int lPixelData = pixels[i];
			int lSurroundFlags = 0b00000000;

			if (lPixelData == EMPTY)
				continue;

			final int lLocalX = (i % width);
			final int lLocalY = (i / width);

			// Need to find out which blocks are the same as this block
			if (lLocalY > 0 && pixels[i - width] == lPixelData) {
				lSurroundFlags |= TOP;
			}
			if (lLocalX > 0 && pixels[i - 1] == lPixelData) {
				lSurroundFlags |= LEFT;
			}
			if (lLocalX < width - 1 && pixels[i + 1] == lPixelData) {
				lSurroundFlags |= RIGHT;
			}
			if (lLocalY < height - 1 && pixels[i + width] == lPixelData) {
				lSurroundFlags |= BOTTOM;
			}

			switch (lPixelData) {
			case ROAD:
				if (!mPlayerSpawnPlaced) {
					lLevel.setPlayerSpawnPosition((lLocalX * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2, (lLocalY * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2);
					mPlayerSpawnPlaced = true;
				}

				createRoadSegment(lLevel, lLocalX, lLocalY, lSurroundFlags, i);
				break;
			case BUILDING:
				createBuilding(lLevel, lLocalX, lLocalY, lSurroundFlags);
				break;
			default:

				continue;
			}

		}

	}

	private void buildRoadGraph() {
		Level lLevel = mWorld.level();

		// Graph node travesal still based on raw map image as we need to know neighbours
		int[] pixels = mLevelPixelData.pixels;

		final int width = mLevelPixelData.width;
		final int height = mLevelPixelData.height;

		final int lNumCells = pixels.length;
		for (int i = 0; i < lNumCells; i++) {
			int lPixelData = pixels[i];

			final int lLocalX = (i % width);
			final int lLocalY = (i / width);

			if (lPixelData != ROAD)
				continue;

			RoadSection lOurRoadSection = lLevel.getRoadSectionByIndex(i);
			if (lOurRoadSection == null)
				continue;

			// For now, skip non-road tiles (later could add garages and stuff).
			// --->

			// Need to find out which blocks are the same as this block
			// TOP
			if (lLocalY > 0 && pixels[i - width] == lPixelData) {
				RoadSection lTopSection = lLevel.getRoadSectionByIndex(i - width);
				if (lTopSection != null) {
					joinGraphNodes(lOurRoadSection, lTopSection, true, false, false, false);

				}
			}

			// LEFT
			if (lLocalX > 0 && pixels[i - 1] == lPixelData) {
				RoadSection lLeftSection = lLevel.getRoadSectionByIndex(i - 1);
				if (lLeftSection != null) {
					joinGraphNodes(lOurRoadSection, lLeftSection, false, false, true, false);

				}
			}

			// RIGHT
			if (lLocalX < width - 1 && pixels[i + 1] == lPixelData) {
				RoadSection lRightSection = lLevel.getRoadSectionByIndex(i + 1);
				if (lRightSection != null) {
					joinGraphNodes(lOurRoadSection, lRightSection, false, false, false, true);

				}
			}

			// BOTTOM
			if (lLocalY < height - 1 && pixels[i + width] == lPixelData) {
				RoadSection lBottomSection = lLevel.getRoadSectionByIndex(i + width);
				if (lBottomSection != null) {
					joinGraphNodes(lOurRoadSection, lBottomSection, false, true, false, false);

				}
			}

		}

	}

	private void createBuilding(Level pLevel, float pLocalX, float pLocalY, int pSurroundFlags) {
		Building lNewBuilding = new Building();

		switch (pSurroundFlags) {
		case LEFT:
			lNewBuilding.textureIndex = 14;
			break;

		case TOP:
			lNewBuilding.textureIndex = 11;
			break;

		case RIGHT:
			lNewBuilding.textureIndex = 12;
			break;

		case BOTTOM:
			lNewBuilding.textureIndex = 3;
			break;

		case TOP | BOTTOM:
			lNewBuilding.textureIndex = 7;
			break;

		case LEFT | RIGHT:
			lNewBuilding.textureIndex = 13;
			break;

		case RIGHT | BOTTOM:
			lNewBuilding.textureIndex = 0;
			break;

		case LEFT | BOTTOM:
			lNewBuilding.textureIndex = 2;
			break;

		case RIGHT | TOP:
			lNewBuilding.textureIndex = 8;
			break;

		case LEFT | TOP:
			lNewBuilding.textureIndex = 10;
			break;

		case RIGHT | LEFT | TOP | BOTTOM:
			lNewBuilding.textureIndex = 5;
			break;

		case RIGHT | LEFT | BOTTOM:
			lNewBuilding.textureIndex = 1;
			break;

		case LEFT | TOP | BOTTOM:
			lNewBuilding.textureIndex = 6;
			break;

		case RIGHT | TOP | BOTTOM:
			lNewBuilding.textureIndex = 4;
			break;

		case RIGHT | LEFT | TOP:
			lNewBuilding.textureIndex = 9;
			break;

		default:
			lNewBuilding.textureIndex = 15;
			break;
		}

		lNewBuilding.set((pLocalX * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2, (pLocalY * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2, GameWorldScreen.GRID_SIZE,
				GameWorldScreen.GRID_SIZE);

		pLevel.addBuilding(lNewBuilding);

	}

	private void createRoadSegment(Level pLevel, float pLocalX, float pLocalY, int pSurroundFlags, int pTileIndex) {
		RoadSection lNewRoadSection = new RoadSection(pTileIndex);
		lNewRoadSection.set((pLocalX * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2, (pLocalY * GameWorldScreen.GRID_SIZE) + GameWorldScreen.GRID_SIZE / 2, GameWorldScreen.GRID_SIZE,
				GameWorldScreen.GRID_SIZE);

		switch (pSurroundFlags) {
		case TOP: {
			lNewRoadSection.textureIndex = 0;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR));

			break;
		}

		case LEFT: {
			lNewRoadSection.textureIndex = 1;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case BOTTOM: {
			lNewRoadSection.textureIndex = 2;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));

			break;
		}

		case RIGHT: {
			lNewRoadSection.textureIndex = 3;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR));
			break;
		}

		case RIGHT | LEFT: {
			lNewRoadSection.textureIndex = 4;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case TOP | BOTTOM: {
			lNewRoadSection.textureIndex = 5;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode2.addConnection(lNode3);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR));

			break;
		}

		case RIGHT | BOTTOM: {
			lNewRoadSection.textureIndex = 6;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);

			lNode3.addConnection(lNode4);
			lNode4.addConnection(lNode5);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case LEFT | BOTTOM: {
			lNewRoadSection.textureIndex = 7;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);

			lNode3.addConnection(lNode4);
			lNode4.addConnection(lNode5);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));

			break;
		}

		case TOP | RIGHT: {
			lNewRoadSection.textureIndex = 8;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);

			lNode3.addConnection(lNode4);
			lNode4.addConnection(lNode5);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case TOP | LEFT: {
			lNewRoadSection.textureIndex = 9;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);

			lNode3.addConnection(lNode4);
			lNode4.addConnection(lNode5);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR));

			break;
		}

		case RIGHT | BOTTOM | TOP | LEFT: {
			lNewRoadSection.textureIndex = 10;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode6 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode7 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode8 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode9 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode10 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode11 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);

			lNode0.addConnection(lNode1);
			lNode1.addConnection(lNode2);
			lNode1.addConnection(lNode5);

			lNode3.addConnection(lNode4);
			lNode4.addConnection(lNode5);
			lNode4.addConnection(lNode8);

			lNode6.addConnection(lNode7);
			lNode7.addConnection(lNode8);
			lNode7.addConnection(lNode11);

			lNode9.addConnection(lNode10);
			lNode10.addConnection(lNode11);
			lNode10.addConnection(lNode2);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);
			lNewRoadSection.levelNode.add(lNode6);
			lNewRoadSection.levelNode.add(lNode7);
			lNewRoadSection.levelNode.add(lNode8);
			lNewRoadSection.levelNode.add(lNode9);
			lNewRoadSection.levelNode.add(lNode10);
			lNewRoadSection.levelNode.add(lNode11);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));
			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case LEFT | TOP | RIGHT: {
			lNewRoadSection.textureIndex = 12;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode6 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode7 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode8 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode9 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);

			lNode0.addConnection(lNode2);
			lNode1.addConnection(lNode3);
			lNode2.addConnection(lNode3);
			lNode2.addConnection(lNode6);

			lNode4.addConnection(lNode5);
			lNode5.addConnection(lNode6);
			lNode5.addConnection(lNode9);

			lNode7.addConnection(lNode8);
			lNode7.addConnection(lNode1);
			lNode8.addConnection(lNode9);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);
			lNewRoadSection.levelNode.add(lNode6);
			lNewRoadSection.levelNode.add(lNode7);
			lNewRoadSection.levelNode.add(lNode8);
			lNewRoadSection.levelNode.add(lNode9);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		case TOP | RIGHT | BOTTOM: {
			lNewRoadSection.textureIndex = 13;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode6 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode7 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode8 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode9 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);

			lNode0.addConnection(lNode2);
			lNode1.addConnection(lNode3);
			lNode2.addConnection(lNode3);
			lNode2.addConnection(lNode6);

			lNode4.addConnection(lNode5);
			lNode5.addConnection(lNode6);
			lNode5.addConnection(lNode9);

			lNode7.addConnection(lNode8);
			lNode7.addConnection(lNode1);
			lNode8.addConnection(lNode9);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);
			lNewRoadSection.levelNode.add(lNode6);
			lNewRoadSection.levelNode.add(lNode7);
			lNewRoadSection.levelNode.add(lNode8);
			lNewRoadSection.levelNode.add(lNode9);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR));

			break;
		}

		case RIGHT | BOTTOM | LEFT: {
			lNewRoadSection.textureIndex = 14;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode6 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode7 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode8 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode9 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);

			lNode0.addConnection(lNode2);
			lNode1.addConnection(lNode3);
			lNode2.addConnection(lNode3);
			lNode2.addConnection(lNode6);

			lNode4.addConnection(lNode5);
			lNode5.addConnection(lNode6);
			lNode5.addConnection(lNode9);

			lNode7.addConnection(lNode8);
			lNode7.addConnection(lNode1);
			lNode8.addConnection(lNode9);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);
			lNewRoadSection.levelNode.add(lNode6);
			lNewRoadSection.levelNode.add(lNode7);
			lNewRoadSection.levelNode.add(lNode8);
			lNewRoadSection.levelNode.add(lNode9);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR));

			break;
		}

		case LEFT | BOTTOM | TOP: {
			lNewRoadSection.textureIndex = 15;
			LevelNode lNode0 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);
			LevelNode lNode1 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode2 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode3 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 80 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode4 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 16 * MUL_FACTOR);
			LevelNode lNode5 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode6 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 48 * MUL_FACTOR);
			LevelNode lNode7 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 16 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode8 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 80 * MUL_FACTOR);
			LevelNode lNode9 = new LevelNode(lNewRoadSection, lNewRoadSection.left() + 48 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR);

			lNode0.addConnection(lNode2);
			lNode1.addConnection(lNode3);
			lNode2.addConnection(lNode3);
			lNode2.addConnection(lNode6);

			lNode4.addConnection(lNode5);
			lNode5.addConnection(lNode6);
			lNode5.addConnection(lNode9);

			lNode7.addConnection(lNode8);
			lNode7.addConnection(lNode1);
			lNode8.addConnection(lNode9);

			lNewRoadSection.levelNode.add(lNode0);
			lNewRoadSection.levelNode.add(lNode1);
			lNewRoadSection.levelNode.add(lNode2);
			lNewRoadSection.levelNode.add(lNode3);
			lNewRoadSection.levelNode.add(lNode4);
			lNewRoadSection.levelNode.add(lNode5);
			lNewRoadSection.levelNode.add(lNode6);
			lNewRoadSection.levelNode.add(lNode7);
			lNewRoadSection.levelNode.add(lNode8);
			lNewRoadSection.levelNode.add(lNode9);

			lNewRoadSection.pickUpAreas.add(new PickUpAreas(pickupAreaIndex, lNewRoadSection.left() + 112 * MUL_FACTOR, lNewRoadSection.top() + 112 * MUL_FACTOR));

			break;
		}

		default:
			lNewRoadSection.textureIndex = 11;
		}

		pLevel.addRoadSection(lNewRoadSection);

	}

	private void joinGraphNodes(RoadSection pSection0, RoadSection pSection1, boolean pTop, boolean pBottom, boolean pLeft, boolean pRight) {
		switch (pSection0.textureIndex) {
		case 0:
			switch (pSection1.textureIndex) {
			case 2:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 9:
				break;
			case 10:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 13:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			case 14:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			case 15:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 1:
			switch (pSection1.textureIndex) {
			case 3:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 12:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			case 13:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			case 14:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 2:
			switch (pSection1.textureIndex) {
			case 0:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 5:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 8:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(5));
				break;
			case 9:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				break;
			case 10:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(6));
				break;
			case 12:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			case 13:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(9));
				break;
			case 15:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			}
			break;
		case 3:
			switch (pSection1.textureIndex) {
			case 1:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 8:
				break;
			case 9:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 10:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(9));
				break;
			case 12:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 14:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			case 15:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			}
			break;
		case 4:
			switch (pSection1.textureIndex) {
			case 1:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pRight)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				break;
			case 8:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 9:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				break;
			case 10:
				if (pRight)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(9));
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 12:
				if (pRight)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			case 13:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(7));
				break;
			}
			break;
		case 5:
			switch (pSection1.textureIndex) {
			case 0:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				if (pBottom)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pBottom)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(6));
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 12:
				pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(7));
				break;
			case 13:
				if (pBottom)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				break;
			case 14:
				pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				break;
			case 15:
				if (pBottom)
					pSection0.levelNode.get(1).addConnection(pSection1.levelNode.get(4));
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 6:
			switch (pSection1.textureIndex) {
			case 0:
				pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 1:
				pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 8:
				pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(9));
				if (pBottom)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(6));
				break;
			case 12:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				break;
			case 13:
				pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 14:
				pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(4));
				break;
			case 15:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(7));
				if (pBottom)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(4));
				break;
			}
			break;
		case 7:
			switch (pSection1.textureIndex) {
			case 0:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(2));
				break;
			case 5:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 6:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(6));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 12:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(7));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(4));
				break;
			case 13:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				break;
			case 14:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pBottom)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				break;
			}
			break;
		case 8:
			switch (pSection1.textureIndex) {
			case 1:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 9:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 10:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(9));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 12:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 13:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(4));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(4));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				break;
			case 15:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(7));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 9:
			switch (pSection1.textureIndex) {
			case 2:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(2));
				break;
			case 5:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 12:
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(4));
				break;
			case 13:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				break;
			case 14:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(7));
				if (pLeft)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pTop)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 10:
			switch (pSection1.textureIndex) {
			case 0:
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(0));
				break;
			case 1:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(2));
				break;
			case 5:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(2));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(0));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(6));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(3));
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(9));
				break;
			case 12:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(4));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(7));
				break;
			case 13:
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(7));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(0));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(4));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(7));
				if (pLeft)
					pSection0.levelNode.get(8).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pRight)
					pSection0.levelNode.get(2).addConnection(pSection1.levelNode.get(7));
				if (pTop)
					pSection0.levelNode.get(5).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(11).addConnection(pSection1.levelNode.get(4));
				break;
			}
			break;
		case 12:
			switch (pSection1.textureIndex) {
			case 1:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(2));
				break;
			case 5:
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(3));
				break;
			case 9:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 10:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(9));
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(3));
				break;
			case 12:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(4));
				break;
			case 13:
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(7));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(7));
				if (pLeft)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pRight)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				if (pTop)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 13:
			switch (pSection1.textureIndex) {
			case 0:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 1:
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(2));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 7:
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(6));
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(9));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 12:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 13:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(4));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(4));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(7));
				break;
			case 15:
				if (pBottom)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				if (pRight)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(7));
				if (pTop)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			}
			break;
		case 14:
			switch (pSection1.textureIndex) {
			case 0:
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 1:
				pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 5:
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 6:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 10:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(6));
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(9));
				break;
			case 12:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(7));
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 13:
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 14:
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pBottom)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(4));
				if (pRight)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(7));
				break;
			}
			break;
		case 15:
			switch (pSection1.textureIndex) {
			case 0:
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 2:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				break;
			case 3:
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 4:
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(2));
				break;
			case 5:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(2));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 6:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				break;
			case 7:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(3));
				break;
			case 8:
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 9:
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(3));
				break;
			case 10:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(3));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(6));
				break;
			case 12:
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(4));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(7));
				break;
			case 13:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(4));
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(7));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(0));
				break;
			case 14:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(7));
				if (pLeft)
					pSection0.levelNode.get(6).addConnection(pSection1.levelNode.get(0));
				break;
			case 15:
				if (pTop)
					pSection0.levelNode.get(3).addConnection(pSection1.levelNode.get(0));
				if (pBottom)
					pSection0.levelNode.get(9).addConnection(pSection1.levelNode.get(4));
				break;
			}
			break;

		}

	}

}
