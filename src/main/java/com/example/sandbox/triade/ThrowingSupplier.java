package com.example.sandbox.triade;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
