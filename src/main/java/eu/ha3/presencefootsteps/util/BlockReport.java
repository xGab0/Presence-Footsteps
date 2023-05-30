package eu.ha3.presencefootsteps.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import com.google.gson.stream.JsonWriter;
import com.minelittlepony.common.util.GamePaths;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.world.Lookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BlockReport {
    private final Path loc;

    public BlockReport(String baseName) {
        loc = getUniqueFileName(GamePaths.getGameDirectory().resolve("presencefootsteps"), baseName, ".json");
    }

    public CompletableFuture<?> execute(@Nullable Predicate<BlockState> filter) {
        return CompletableFuture.runAsync(() -> {
            try {
                writeReport(filter);
                printResults();
            } catch (Exception e) {
                addMessage(Text.translatable("pf.report.error", e.getMessage()).styled(s -> s.withColor(Formatting.RED)));
            }
        });
    }

    private void writeReport(@Nullable Predicate<BlockState> filter) throws IOException {
        Files.createDirectories(loc.getParent());

        try (var writer = JsonObjectWriter.of(new JsonWriter(Files.newBufferedWriter(loc)))) {
            writer.object(() -> {
                final Map<String, BlockSoundGroup> groups = new Object2ObjectOpenHashMap<>();
                writer.object("blocks", () -> {
                    writer.each(Registries.BLOCK, block -> {
                        BlockState state = block.getDefaultState();

                        var group = block.getDefaultState().getSoundGroup();
                        if (group != null && group.getStepSound() != null) {
                            String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", group.volume, group.pitch);
                            groups.put(group.getStepSound().getId().toString() + "@" + substrate, group);
                        }

                        if (filter == null || filter.test(state)) {
                            writer.object(Registries.BLOCK.getId(block).toString(), () -> {
                                writer.field("class", getClassData(state));
                                writer.field("tags", getTagData(state));
                                writer.field("sound", getSoundData(group));
                                writer.field("association", PresenceFootsteps.getInstance().getEngine().getIsolator().getBlockMap().getAssociation(state, Lookup.EMPTY_SUBSTRATE));
                            });
                        }
                    });
                });
                writer.array("unmapped_entities", () -> {
                    writer.each(Registries.ENTITY_TYPE, type -> {
                        Identifier id = Registries.ENTITY_TYPE.getId(type);
                        if (!PresenceFootsteps.getInstance().getEngine().getIsolator().getLocomotionMap().contains(id)) {
                            if (type.create(MinecraftClient.getInstance().world) instanceof LivingEntity) {
                                writer.writer().value(id.toString());
                            }
                        }
                    });
                });
                writer.object("primitives", () -> {
                    writer.each(groups.values(), group -> {
                        String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", group.volume, group.pitch);
                        writer.field(group.getStepSound().getId().toString() + "@" + substrate, PresenceFootsteps.getInstance().getEngine().getIsolator().getPrimitiveMap().getAssociation(group, substrate));
                    });
                });
            });
        }
    }

    private String getSoundData(@Nullable BlockSoundGroup group) {
        if (group == null) {
            return "NULL";
        }
        if (group.getStepSound() == null) {
            return "NO_SOUND";
        }
        return group.getStepSound().getId().getPath();
    }

    private String getClassData(BlockState state) {
        @Nullable
        String canonicalName = state.getBlock().getClass().getCanonicalName();
        if (canonicalName == null) {
            return "<anonymous>";
        }
        return FabricLoader.getInstance().getMappingResolver().unmapClassName("named", canonicalName);
    }

    private String getTagData(BlockState state) {
        return Registries.BLOCK.streamTags().filter(state::isIn).map(TagKey::id).map(Identifier::toString).collect(Collectors.joining(","));
    }

    private void printResults() {
        addMessage(Text.translatable("pf.report.save")
                .append(Text.literal(loc.getFileName().toString()).styled(s -> s
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, loc.toString()))
                    .withFormatting(Formatting.UNDERLINE)))
                .styled(s -> s
                    .withColor(Formatting.GREEN)));
    }

    public static void addMessage(Text text) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
    }

    static Path getUniqueFileName(Path directory, String baseName, String ext) {
        Path loc = null;

        int counter = 0;
        while (loc == null || Files.exists(loc)) {
            loc = directory.resolve(baseName + (counter == 0 ? "" : "_" + counter) + ext);
            counter++;
        }

        return loc;
    }
}
