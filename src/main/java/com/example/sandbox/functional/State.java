package com.example.sandbox.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Repräsentiert eine State-Monade, die einen Zustand {@code S} transformiert
 * und zusätzlich einen Wert {@code A} produziert.</p>
 *
 * <p>Formal: {@code State<S,A>} ist eine Funktion {@code S -> (S,A)}.</p>
 *
 * @param <S> Typ des Zustands
 * @param <A> Typ des Werts
 */
public record State<S, A>(Function<S, State.Result<S, A>> run) {

    /**
     * Ergebnis einer State-Transformation: neuer Zustand + Wert.
     */
    public record Result<S, A>(S state, A value) {}

    // --- Factory-Methoden ---

    public static <S, A> State<S, A> of(Function<S, Result<S, A>> fn) {
        return new State<>(fn);
    }

    public static <S, A> State<S, A> pure(A value) {
        return new State<>(s -> new Result<>(s, value));
    }

    public static <S> State<S, S> get() {
        return new State<>(s -> new Result<>(s, s));
    }

    public static <S> State<S, Void> put(S newState) {
        return new State<>(s -> new Result<>(newState, null));
    }

    public static <S> State<S, Void> modify(Function<S, S> f) {
        return new State<>(s -> new Result<>(f.apply(s), null));
    }

    public static <S, A> State<S, A> inspect(Function<S, A> f) {
        return new State<>(s -> new Result<>(s, f.apply(s)));
    }

    public static <S, A> State<S, A> fromSupplier(Supplier<A> supplier) {
        return new State<>(s -> new Result<>(s, supplier.get()));
    }

    // --- Monadische API ---

    public <B> State<S, B> map(Function<A, B> mapper) {
        return new State<>(s -> {
            var r = run.apply(s);
            return new Result<>(r.state(), mapper.apply(r.value()));
        });
    }

    public <B> State<S, B> flatMap(Function<A, State<S, B>> binder) {
        return new State<>(s -> {
            var r1 = run.apply(s);
            return binder.apply(r1.value()).run.apply(r1.state());
        });
    }

    // --- Hilfsmethoden eval / exec ---

    public A eval(S s) {
        return run.apply(s).value();
    }

    public S exec(S s) {
        return run.apply(s).state();
    }

    // --- Utility: sequence ---

    public static <S, A> State<S, List<A>> sequence(List<State<S, A>> states) {
        return new State<>(s -> {
            var cur = s;
            var results = new ArrayList<A>();
            for (var st : states) {
                var r = st.run.apply(cur);
                cur = r.state();
                results.add(r.value());
            }
            return new Result<>(cur, results);
        });
    }
}
