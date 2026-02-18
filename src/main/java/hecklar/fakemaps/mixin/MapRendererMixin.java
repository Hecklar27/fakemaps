package hecklar.fakemaps.mixin;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import hecklar.fakemaps.FakeMaps;

@Mixin(MapRenderer.class)
public class MapRendererMixin {
	@Unique
	private static final Identifier WHITE_TEXTURE = Identifier.of("minecraft", "textures/misc/white.png");

	@Unique
	private static final float BORDER_SIZE = 4.0f;

	@Inject(at = @At("HEAD"), method = "draw", cancellable = true)
	private void replaceMapRendering(MapRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean bl, int light, CallbackInfo ci) {
		// Red squares: draw them and cancel vanilla
		if (FakeMaps.renderingEnabled) {
			Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(WHITE_TEXTURE));
			float z = -0.01f;
			float b = BORDER_SIZE;

			drawQuad(vertexConsumer, positionMatrix, 0, 0, 128, b, 0, 0, 0, 255, light, z);       // top
			drawQuad(vertexConsumer, positionMatrix, 0, 128 - b, 128, 128, 0, 0, 0, 255, light, z); // bottom
			drawQuad(vertexConsumer, positionMatrix, 0, b, b, 128 - b, 0, 0, 0, 255, light, z);     // left
			drawQuad(vertexConsumer, positionMatrix, 128 - b, b, 128, 128 - b, 0, 0, 0, 255, light, z); // right
			drawQuad(vertexConsumer, positionMatrix, b, b, 128 - b, 128 - b, 255, 0, 0, 255, light, z);

			ci.cancel();
			return;
		}

		// No red squares — check if vanilla maps should render
		if (!FakeMaps.vanillaMapsEnabled) {
			ci.cancel(); // Vanilla maps off, just show empty item frames
		}
	}

	@Unique
	private static void drawQuad(VertexConsumer consumer, Matrix4f matrix,
								 float x0, float y0, float x1, float y1,
								 int r, int g, int b, int a, int light, float z) {
		consumer.vertex(matrix, x0, y1, z).color(r, g, b, a).texture(0, 1).light(light);
		consumer.vertex(matrix, x1, y1, z).color(r, g, b, a).texture(1, 1).light(light);
		consumer.vertex(matrix, x1, y0, z).color(r, g, b, a).texture(1, 0).light(light);
		consumer.vertex(matrix, x0, y0, z).color(r, g, b, a).texture(0, 0).light(light);
	}
}
