package eu.ha3.presencefootsteps.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundSystem.class)
abstract class MSoundSystem {
    @ModifyConstant(method = "getAdjustedVolume", constant = @Constant(floatValue = 1F))
    private float onGetAdjustedVolume(float constant, SoundInstance sound) {
        if (sound instanceof ImmediateSoundPlayer.UncappedSoundInstance t) {
            return t.getMaxVolume();
        }
        return constant;
    }
}
