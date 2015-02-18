package org.minezy.android.utils;

public interface Parametrized<P, R> {
    public R perform(P param) throws Exception;
}
