package kireiko.dev.anticheat.api;

public interface PacketCheckHandler extends Cloneable {
    void event(Object event);
}
