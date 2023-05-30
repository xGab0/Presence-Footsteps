package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.Period;
import eu.ha3.presencefootsteps.util.Range;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

record DelayedAcoustic(
        String soundName,
        Range volume,
        Range pitch,
        Period delay
) implements Acoustic {

    public static @NotNull DelayedAcoustic of(String name, AcousticsJsonParser context) {
        Range volume = new Range(1);
        Range pitch = new Range(1);
        Period delay = new Period(0);

        volume.copy(context.getVolumeRange());
        pitch.copy(context.getPitchRange());

        return new DelayedAcoustic(
                context.getSoundName(name),
                volume, pitch, delay
        );
    }

    public static DelayedAcoustic fromJson(JsonObject json, AcousticsJsonParser context) {
        DelayedAcoustic acoustic = DelayedAcoustic.of(json.get("name").getAsString(), context);
        Period delay = new Period(0);

        acoustic.volume.read("vol", json, context);
        acoustic.pitch.read("pitch", json, context);

        if (json.has("delay")) {
            delay.set(json.get("delay").getAsLong());
        } else {
            delay.set(json.get("delay_min").getAsLong(), json.get("delay_max").getAsLong());
        }

        return new DelayedAcoustic(
                acoustic.soundName,
                acoustic.volume,
                acoustic.pitch,
                delay
        );
    }

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        VaryingAcoustic.playSound(soundName, volume, pitch, delay, player, location, inputOptions);
    }
}