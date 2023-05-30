package eu.ha3.presencefootsteps.util;

import java.util.Random;

import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;

public record Range (float min, float max) {
    public static Range exactly(float value) {
        return new Range(value, value);
    }

    public Range read(String name, JsonObject json, AcousticsJsonParser context) {
        float min = this.min;
        float max = this.max;
        if (json.has(name + "_min")) {
            min = context.getPercentage(json, name + "_min");
        }

        if (json.has(name + "_max")) {
            max = context.getPercentage(json, name + "_max");
        }

        if (json.has(name)) {
            min = max = context.getPercentage(json, name);
        }

        return new Range(min, max);
    }

    public float random(Random rand) {
        return MathUtil.randAB(rand, min, max);
    }

    public float on(float value) {
        return MathUtil.between(min, max, value);
    }
}
