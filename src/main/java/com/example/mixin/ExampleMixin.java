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

    // Шаг 1: Перехват нажатия клавиши Left Shift
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        KeyHandler.handleRawKey(key, action);
    }

    // Шаг 2: Обработчик двойного клика Shift с проверкой на нахождение в мире
    public static class KeyHandler {
        private static long lastShiftTime = 0;
        private static final long DOUBLE_CLICK_DELAY = 300; // Окно в мс для двойного нажатия

        public static void handleRawKey(int key, int action) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // ПРОВЕРКА: Если игрок НЕ в мире (в главном меню, загрузке и т.д.) — ничего не делаем
            if (client.world == null || client.player == null) return;
            
            // Отлавливаем только момент нажатия (GLFW_PRESS) Левого Shift
            if (key == GLFW.GLFW_KEY_LEFT_SHIFT && action == GLFW.GLFW_PRESS) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastShiftTime < DOUBLE_CLICK_DELAY) {
                    // Если меню уже открыто — закрываем его
                    if (client.currentScreen instanceof ModGuiScreen) {
                        client.execute(() -> client.setScreen(null));
                    } else if (client.currentScreen == null) {
                        // Если экранов нет — открываем наше визуальное меню
                        client.execute(() -> client.setScreen(new ModGuiScreen()));
                    }
                    lastShiftTime = 0; // Сбрасываем таймер для предотвращения тройного клика
                } else {
                    lastShiftTime = currentTime; // Фиксируем первое нажатие
                }
            }
        }
    }

    // Шаг 3: Класс самого визуального меню (GUI Screen)
    public static class ModGuiScreen extends Screen {
        public ModGuiScreen() {
            // Название меню
            super(Text.literal("werst web by gemini"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Рисуем стандартный полупрозрачный фон Fabric/Minecraft
            this.renderBackground(context, mouseX, mouseY, delta);
            
            // Выводим заголовок по центру экрана
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
            
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPauseGame() {
            return false; // Игра не встает на паузу
        }
    }
}
