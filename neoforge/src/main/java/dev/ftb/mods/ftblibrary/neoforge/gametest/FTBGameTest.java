package dev.ftb.mods.ftblibrary.neoforge.gametest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class FTBGameTest extends GameTestInstance {
    public static final MapCodec<FTBGameTest> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TestData.CODEC.forGetter(test -> test.info()),
            Identifier.CODEC.fieldOf("test_id").forGetter(FTBGameTest::testId)
    ).apply(builder, FTBGameTest::new));

    private final Identifier testId;

    public FTBGameTest(TestData<Holder<TestEnvironmentDefinition<?>>> info, Identifier testId) {
        super(info);
        this.testId = testId;
    }

    @Override
    public void run(GameTestHelper helper) {
        Consumer<GameTestHelper> consumer = FTBTestRegistrar.getGameTest(testId);
        if (consumer == null) {
            throw new GameTestAssertException(Component.literal("unknown test " + testId), 0);
        }
        consumer.accept(helper);
    }

    public Identifier testId() {
        return testId;
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("FTB Gametest: " + testId);
    }
}
