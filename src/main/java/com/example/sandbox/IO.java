package com.example.sandbox;

import java.util.function.Consumer;
import java.util.function.Function;

public class IO<A> {
    private final Effect<A> effect;
    private IO(Effect<A> effect) {
        this.effect = effect;
    }

    public A unsafeRun() {
        return effect.run();
    }

    public <B> IO<B> flatMap( Function< A, IO<B>> function) {
        return IO.apply(() -> function.apply(effect.run()).unsafeRun());
    }

    public static <T> IO<T> apply(Effect<T> effect) {
        return new IO<>(effect);
    }

    public <B> IO<B> map(Function<A, B> function){
        return this.flatMap(result -> IO.apply(() -> function.apply(result)));
    }

    public IO<Void> mapToVoid(Consumer<A> function) {
        return this.flatMap(result -> IO.apply(() -> {
            function.accept(result);
            //We can’t instantiate Void, hence we can return null only
            return null;
        }));
    }
}