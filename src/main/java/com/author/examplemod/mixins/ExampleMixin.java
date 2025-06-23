package com.author.examplemod.mixins;

import com.author.examplemod.ExampleMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin extends Screen {
    protected ExampleMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo info) {
        addRenderableWidget(Button.builder(Component.literal("ExampleMod Test"), button -> {
                    ExampleMod.LOGGER.info("This line is printed by an example mod mixin!");
                })
                //? if fabric
                .pos(0, 12)
                .build());
    }
}
