package dev.ftb.mods.ftblibrary.util.fabric;

import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;
import net.fabricmc.fabric.api.event.Event;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FabricEventHelper {
    public static <T> void registerFabricEventPoster(Class<T> cls, Event<? extends Consumer<T>> event) {
        NativeEventPosting.INSTANCE.registerEvent(cls, data -> event.invoker().accept(data));
    }

    public static <T> void registerFabricEventPosterPredicate(TypedEvent<T, Boolean> type, Event<? extends Predicate<T>> event) {
        NativeEventPosting.INSTANCE.registerEventWithResult(type, data -> event.invoker().test(data));
    }

    public static <T,R> void registerFabricEventPosterFunction(TypedEvent<T, R> type, Event<? extends Function<T,R>> event) {
        NativeEventPosting.INSTANCE.registerEventWithResult(type, data -> event.invoker().apply(data));
    }

    // TODO more general support for events with methods other than accept/test/apply
}
