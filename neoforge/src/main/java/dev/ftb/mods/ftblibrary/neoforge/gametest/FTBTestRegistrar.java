package dev.ftb.mods.ftblibrary.neoforge.gametest;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FTBTestRegistrar {
    private static final Identifier STRUCTURE = FTBLibrary.id("empty_5x5x7");

    private static final Map<Identifier, Consumer<GameTestHelper>> TEST_BODIES = new ConcurrentHashMap<>();

    private final RegisterGameTestsEvent event;
    private final Holder<TestEnvironmentDefinition<?>> environment;
    private final String modId;

    public FTBTestRegistrar(RegisterGameTestsEvent event, String modId) {
        this.event = event;
        this.environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(modId, "default"), new TestEnvironmentDefinition.AllOf(List.of()));
        this.modId = modId;
    }

    public void add(String name, int maxTicks, Consumer<GameTestHelper> body) {
        add(name, maxTicks, 0, body);
    }

    public void add(String name, int maxTicks, int setupTicks, Consumer<GameTestHelper> body) {
        TestData<Holder<TestEnvironmentDefinition<?>>> info = new TestData<>(
                environment,
                STRUCTURE,
                maxTicks,
                setupTicks,
                true,
                Rotation.NONE
        );
        try {
            Identifier id = Identifier.fromNamespaceAndPath(modId, name);
            event.registerTest(id, new FTBGameTest(info, id));
            TEST_BODIES.put(id, body);
        } catch (Throwable t) {
            FTBLibrary.LOGGER.error("Failed to register game test {}: {}", modId, name, t);
        }
    }

    public static Consumer<GameTestHelper> getGameTest(Identifier testId) {
        return TEST_BODIES.get(testId);
    }
}
