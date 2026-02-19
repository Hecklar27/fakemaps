package hecklar.fakemaps.mixin;

import hecklar.fakemaps.FakeMaps;
import hecklar.fakemaps.MapQuadBatcher;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	private <E extends Entity> void skipItemFrameRendering(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!(entity instanceof ItemFrameEntity itemFrame)) {
			return;
		}

		// Item frames toggle — hide all item frames entirely
		if (!FakeMaps.itemFramesEnabled) {
			ci.cancel();
			return;
		}

		// Red square mode — skip the entire dispatcher for map item frames
		if (!FakeMaps.renderingEnabled) {
			return; // Let vanilla handle it
		}

		ItemStack heldStack = itemFrame.getHeldItemStack();
		if (heldStack.isEmpty() || itemFrame.getMapId(heldStack) == null) {
			return; // Not a map — let vanilla render the empty/non-map frame
		}

		// This is a map item frame in red square mode.
		// Cancel the entire EntityRenderDispatcher.render() — no updateRenderState,
		// no shadow, no renderer dispatch. Compute the transform ourselves.

		Direction facing = itemFrame.getHorizontalFacing();
		int rotation = itemFrame.getRotation();
		boolean glow = itemFrame.getType() == EntityType.GLOW_ITEM_FRAME;

		matrices.push();

		// Position the entity (dispatcher normally does this)
		matrices.translate(x, y, z);

		// Undo position offset (vanilla negates getPositionOffset)
		// getPositionOffset returns Vec3d(facing.x * 0.3, -0.25, facing.z * 0.3)
		matrices.translate(
				-(facing.getOffsetX() * 0.3),
				0.25,
				-(facing.getOffsetZ() * 0.3)
		);

		// Translate toward the wall surface
		matrices.translate(
				facing.getOffsetX() * 0.46875,
				facing.getOffsetY() * 0.46875,
				facing.getOffsetZ() * 0.46875
		);

		// Rotation based on facing direction
		float rotX, rotY;
		if (facing.getAxis().isHorizontal()) {
			rotX = 0;
			rotY = 180 - facing.getPositiveHorizontalDegrees();
		} else {
			rotX = -90 * facing.getDirection().offset();
			rotY = 180;
		}
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotX));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));

		// Map surface offset
		matrices.translate(0, 0, 0.4375f);

		// Map-specific transforms
		int rotSteps = (rotation % 4) * 2;
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotSteps * 360.0f / 8.0f));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
		matrices.scale(0.0078125f, 0.0078125f, 0.0078125f);
		matrices.translate(-64, -64, 0);
		matrices.translate(0, 0, -1);

		// Collect the final matrix for batched rendering
		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
		int mapLight = glow ? 15728850 : light;
		MapQuadBatcher.collect(positionMatrix, mapLight);

		matrices.pop();

		ci.cancel();
	}
}
