package io.kimmking.rpcfx.api;

public interface RpcfxResolver {

    <T>T resole(Class<T> clazz);
}
