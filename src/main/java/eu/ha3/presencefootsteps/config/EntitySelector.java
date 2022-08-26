package eu.ha3.presencefootsteps.config;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

public enum EntitySelector implements Predicate<Entity> {
    ALL {
        @Override
        public boolean test(Entity e) {
            return true;
        }
    },
    PLAYERS_AND_HOSTILES {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity || e instanceof Monster;
        }
    },
    PLAYERS_ONLY {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity;
        }
    };

    public static final EntitySelector[] VALUES = values();
}
