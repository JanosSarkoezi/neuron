package com.example.sandbox.functional;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
