package org.lintford.ld42.renderers;

import java.util.List;

import org.lintford.ld42.controllers.LevelController;
import org.lintford.ld42.data.World;
import org.lintford.ld42.data.level.Building;
import org.lintford.ld42.data.level.LevelNode;
import org.lintford.ld42.data.level.RoadSection;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class WorldRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "WorldRenderer";

	private static final boolean RENDER_GRAPH_NODES = false;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private LevelController mLevelController;

	private TextureBatch mRoadsTextureBatch;
	private TextureBatch mBuildingsTextureBatch;

	private Texture mRoadTexture;
	private Texture mBuildingTexture;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public int ZDepth() {
		return 1;

	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public WorldRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mRoadsTextureBatch = new TextureBatch();
		mBuildingsTextureBatch = new TextureBatch();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mRoadsTextureBatch.loadGLContent(pResourceManager);
		mBuildingsTextureBatch.loadGLContent(pResourceManager);

		mRoadTexture = TextureManager.textureManager().loadTexture("RoadTexture", "res//textures//textureRoads.png");
		mBuildingTexture = TextureManager.textureManager().loadTexture("BuildingTexture", "res//textures//textureBuildings.png");

		mLevelController = (LevelController) mRendererManager.core().controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, mEntityID);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mRoadsTextureBatch.unloadGLContent();
		mBuildingsTextureBatch.unloadGLContent();

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {
		renderRoads(pCore);
		renderBuildings(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void renderRoads(LintfordCore pCore) {
		// if roads dirty
		boolean lRoadsDirty = true;
		if (lRoadsDirty) {
			List<RoadSection> lRoads = mLevelController.activeRoadSections();
			mRoadsTextureBatch.begin(pCore.gameCamera());

			final int lNumRoads = lRoads.size();
			for (int i = 0; i < lNumRoads; i++) {
				RoadSection lRoadSection = lRoads.get(i);

				float lSrcX = (lRoadSection.textureIndex % 4) * 128;
				float lSrcY = (lRoadSection.textureIndex / 4) * 128;

				mRoadsTextureBatch.draw(mRoadTexture, lSrcX, lSrcY, 128, 128, lRoadSection, -0.8f, 1, 1, 1, 1);
			}

			mRoadsTextureBatch.end();

		}

		mRoadsTextureBatch.redraw();

		if (RENDER_GRAPH_NODES) {
			List<RoadSection> lRoads = mLevelController.activeRoadSections();
			Debug.debugManager().drawers().startLineRenderer(pCore.gameCamera());

			final int lNumRoads = lRoads.size();
			for (int i = 0; i < lNumRoads; i++) {
				RoadSection lRoadSection = lRoads.get(i);

				final int lNumPaths = lRoadSection.levelNode.size();
				for (int j = 0; j < lNumPaths; j++) {
					LevelNode lNode = lRoadSection.levelNode.get(j);
					float lSX = lNode.x;
					float lSY = lNode.y;

					final int neighbourCount = lNode.connections().size();
					for (int k = 0; k < neighbourCount; k++) {
						LevelNode lDNode = lNode.connections().get(k);
						float lDX = lDNode.x;
						float lDY = lDNode.y;

						Debug.debugManager().drawers().drawLine(lSX, lSY, lDX, lDY);

					}

				}

			}

			Debug.debugManager().drawers().endLineRenderer();

		}

	}

	private void renderBuildings(LintfordCore pCore) {
		boolean lBuildingsDirty = true;
		if (lBuildingsDirty) {
			List<Building> lBuildings = mLevelController.activeBuildings();
			mBuildingsTextureBatch.begin(pCore.gameCamera());

			final int lNumBuildings = lBuildings.size();
			for (int i = 0; i < lNumBuildings; i++) {
				Building lBuilding = lBuildings.get(i);

				float lSrcX = (lBuilding.textureIndex % 4) * 128;
				float lSrcY = (lBuilding.textureIndex / 4) * 128;

				mBuildingsTextureBatch.draw(mBuildingTexture, lSrcX, lSrcY, 128, 128, lBuilding, -0.8f, 1, 1, 1, 1);

			}

			mBuildingsTextureBatch.end();
		}

		mBuildingsTextureBatch.redraw();

	}

	@Override
	public void initialise(LintfordCore pCore) {
		// TODO Auto-generated method stub

	}

}
