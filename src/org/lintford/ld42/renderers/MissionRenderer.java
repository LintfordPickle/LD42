package org.lintford.ld42.renderers;

import java.util.List;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.mission.Mission;
import org.lintford.ld42.data.mission.MissionManager;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.Circle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class MissionRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "MissionRenderer";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;
	private MissionManager mMissionManager;
	private TextureBatch mTextureBatch;
	private Circle mMissionCircle;

	@Override
	public int ZDepth() {
		return 4;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MissionRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mTextureBatch = new TextureBatch();
		mWorld = pWorld;
		mMissionManager = mWorld.missionManager();

		mMissionCircle = new Circle();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mTextureBatch.loadGLContent(pResourceManager);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {
		List<Mission> lMissions = mMissionManager.availableMissions();

		mTextureBatch.begin(pCore.gameCamera());

		final int lMissionCount = lMissions.size();
		for (int i = 0; i < lMissionCount; i++) {
			Mission lMission = lMissions.get(i);

			mMissionCircle.set(lMission.questGiver.x, lMission.questGiver.y, 100f);
			mTextureBatch.draw(TextureManager.TEXTURE_WHITE, 0, 0, 64, 64, mMissionCircle, -0.2f, 0.87f, 0.43f, 0.2f, 0.2f);
		}

		if (mWorld.gameState().currentMission != null) {
			Mission lCurrentMission = mWorld.gameState().currentMission;

			mMissionCircle.set(lCurrentMission.destX, lCurrentMission.destY, 100f);
			mTextureBatch.draw(TextureManager.TEXTURE_WHITE, 0, 0, 64, 64, mMissionCircle, -0.2f, 0.27f, 0.43f, 0.97f, 0.2f);

		}

		mTextureBatch.end();

	}

}
