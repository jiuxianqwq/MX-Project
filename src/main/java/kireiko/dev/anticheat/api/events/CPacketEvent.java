package kireiko.dev.anticheat.api.events;

import com.comphenix.protocol.events.PacketEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CPacketEvent {
    private PacketEvent packetEvent;
}
