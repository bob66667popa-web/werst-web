package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class ExampleMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        KeyHandler.handleRawKey(key, action);
    }

    public static class KeyHandler {
        private static long lastShiftTime = 0;
        private static final long DOUBLE_CLICK_DELAY = 300;

        public static void handleRawKey(int key, int action) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;
            
            if (key == GLFW.GLFW_KEY_LEFT_SHIFT && action == GLFW.GLFW_PRESS) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastShiftTime < DOUBLE_CLICK_DELAY) {
                    if (client.currentScreen instanceof ModGuiScreen) {
                        client.execute(() -> client.setScreen(null));
                    } else if (client.currentScreen == null) {
                        client.execute(() -> client.setScreen(new ModGuiScreen()));
                    }
                    lastShiftTime = 0;
                } else {
                    lastShiftTime = currentTime;
                }
            }
        }
    }

    public static class ModGuiScreen extends Screen {
        public ModGuiScreen() {
            super(Text.literal("werst web by gemini"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPauseGame() {
            return false;
        }
    }
}

