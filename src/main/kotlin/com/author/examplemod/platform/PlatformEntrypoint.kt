package com.author.examplemod.platform

import com.author.examplemod.ExampleMod
/*? if fabric {*/
import net.fabricmc.api.ClientModInitializer

class PlatformEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        ExampleMod.init()
    }
}
/*?} elif neoforge {*/
/*import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod

@Mod(ExampleMod.MOD_ID, dist = [Dist.CLIENT])
class PlatformEntrypoint {
    init {
        ExampleMod.init()
    }
}
*//*?} elif forge {*/
/*import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLLoader

@Mod(ExampleMod.MOD_ID)
class PlatformEntrypoint {
    init {
        if (FMLLoader.getDist().isClient) ExampleMod.init()
    }
}
*//*?}*/

