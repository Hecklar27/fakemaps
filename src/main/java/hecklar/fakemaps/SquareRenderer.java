package hecklar.fakemaps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class SquareRenderer {
    public static void render(MatrixStack Matrixstack, Camera camera, VertexConsumerProvider vertexConsumerProvider){
        if (MinecraftClient.getInstance().world == null) {
            return;
        }

    }
}
