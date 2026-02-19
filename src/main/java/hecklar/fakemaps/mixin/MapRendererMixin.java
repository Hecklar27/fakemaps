package hecklar.fakemaps.mixin;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import hecklar.fakemaps.FakeMaps;
import hecklar.fakemaps.MapQuadBatcher;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

	@Inject(at = @At("HEAD"), method = "draw", cancellable = true)
	private void replaceMapRendering(MapRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean bl, int light, CallbackInfo ci) {
		// Red squares: collect transform for batched rendering
		if (FakeMaps.renderingEnabled) {
			Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
			MapQuadBatcher.collect(positionMatrix, light);
			ci.cancel();
			return;
		}

		// No red squares — check if vanilla maps should render
		if (!FakeMaps.vanillaMapsEnabled) {
			ci.cancel(); // Vanilla maps off, just show empty item frames
		}
	}
}
