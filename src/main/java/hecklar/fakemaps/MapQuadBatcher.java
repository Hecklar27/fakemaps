package hecklar.fakemaps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class MapQuadBatcher {
	private static final Identifier WHITE_TEXTURE = Identifier.of("minecraft", "textures/misc/white.png");

	// Map content Z (slightly in front of surface)
	private static final float MAP_Z = -0.01f;
	// Frame Z (behind the map surface)
	private static final float FRAME_Z = 1.0f;

	// Black border around the map content
	private static final float BORDER_SIZE = 4.0f;

	// Frame extends beyond the 0-128 map area by this many units on each side
	// 1/16 block = 8 map units (128 units per block)
	private static final float FRAME_EXTEND = 8.0f;

	// Oak wood brown color (RGB ~141, 110, 52)
	private static final int FRAME_R = 141;
	private static final int FRAME_G = 110;
	private static final int FRAME_B = 52;

	private static final int INITIAL_CAPACITY = 1024;

	private static Matrix4f[] matrixPool = new Matrix4f[INITIAL_CAPACITY];
	private static int[] lightValues = new int[INITIAL_CAPACITY];
	private static int count = 0;

	static {
		for (int i = 0; i < INITIAL_CAPACITY; i++) {
			matrixPool[i] = new Matrix4f();
		}
	}

	public static void collect(Matrix4f matrix, int light) {
		if (count >= matrixPool.length) {
			grow();
		}
		matrixPool[count].set(matrix);
		lightValues[count] = light;
		count++;
	}

	private static void grow() {
		int newSize = matrixPool.length * 2;
		Matrix4f[] newPool = new Matrix4f[newSize];
		System.arraycopy(matrixPool, 0, newPool, 0, matrixPool.length);
		for (int i = matrixPool.length; i < newSize; i++) {
			newPool[i] = new Matrix4f();
		}
		matrixPool = newPool;

		int[] newLights = new int[newSize];
		System.arraycopy(lightValues, 0, newLights, 0, lightValues.length);
		lightValues = newLights;
	}

	public static void flush(VertexConsumerProvider vertexConsumers) {
		if (count == 0) return;

		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getText(WHITE_TEXTURE));
		float b = BORDER_SIZE;
		float fe = FRAME_EXTEND;

		for (int i = 0; i < count; i++) {
			Matrix4f m = matrixPool[i];
			int light = lightValues[i];

			// Fake wooden frame border (behind the map content)
			// 4 edge strips extending beyond the 0-128 map area
			drawQuad(consumer, m, -fe, -fe, 128 + fe, 0, FRAME_R, FRAME_G, FRAME_B, 255, light, FRAME_Z);       // top
			drawQuad(consumer, m, -fe, 128, 128 + fe, 128 + fe, FRAME_R, FRAME_G, FRAME_B, 255, light, FRAME_Z); // bottom
			drawQuad(consumer, m, -fe, 0, 0, 128, FRAME_R, FRAME_G, FRAME_B, 255, light, FRAME_Z);               // left
			drawQuad(consumer, m, 128, 0, 128 + fe, 128, FRAME_R, FRAME_G, FRAME_B, 255, light, FRAME_Z);         // right

			// Black border inside the map area
			drawQuad(consumer, m, 0, 0, 128, b, 0, 0, 0, 255, light, MAP_Z);
			drawQuad(consumer, m, 0, 128 - b, 128, 128, 0, 0, 0, 255, light, MAP_Z);
			drawQuad(consumer, m, 0, b, b, 128 - b, 0, 0, 0, 255, light, MAP_Z);
			drawQuad(consumer, m, 128 - b, b, 128, 128 - b, 0, 0, 0, 255, light, MAP_Z);

			// Red fill
			drawQuad(consumer, m, b, b, 128 - b, 128 - b, 255, 0, 0, 255, light, MAP_Z);
		}

		count = 0;
	}

	public static void clear() {
		count = 0;
	}

	public static int getCount() {
		return count;
	}

	private static void drawQuad(VertexConsumer consumer, Matrix4f matrix,
								 float x0, float y0, float x1, float y1,
								 int r, int g, int b, int a, int light, float z) {
		consumer.vertex(matrix, x0, y1, z).color(r, g, b, a).texture(0, 1).light(light);
		consumer.vertex(matrix, x1, y1, z).color(r, g, b, a).texture(1, 1).light(light);
		consumer.vertex(matrix, x1, y0, z).color(r, g, b, a).texture(1, 0).light(light);
		consumer.vertex(matrix, x0, y0, z).color(r, g, b, a).texture(0, 0).light(light);
	}
}
