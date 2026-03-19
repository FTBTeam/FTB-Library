package dev.ftb.mods.ftblibrary.util.fabric;

import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import net.fabricmc.fabric.api.event.Event;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FabricEventHelper {
    public static <T> void registerFabricEventPoster(Class<T> cls, Event<? extends Consumer<T>> event) {
        EventPostingHandler.INSTANCE.registerEvent(cls, data -> event.invoker().accept(data));
    }

    public static <T> void registerFabricEventPosterPredicate(Class<T> cls, Event<? extends Predicate<T>> event) {
        EventPostingHandler.INSTANCE.registerEventWithResult(cls, data -> event.invoker().test(data));
    }

    public static <T,R> void registerFabricEventPosterFunction(Class<T> cls, Event<? extends Function<T,R>> event) {
        EventPostingHandler.INSTANCE.registerEventWithResult(cls, data -> event.invoker().apply(data));
    }

    // TODO more general support for events with methods other than accept/test/apply
}
