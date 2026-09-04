package com.zaryxstudios.okaso.common.entity;

import com.zaryxstudios.okaso.common.event.OkasoEvent;

public class NPCInteractionEvent extends OkasoEvent {
    private final NPCHandle npc;
    private final String playerName;
    private final NPCInteractionType interactionType;

    public NPCInteractionEvent(NPCHandle npc, String playerName, NPCInteractionType interactionType) {
        this.npc = npc;
        this.playerName = playerName;
        this.interactionType = interactionType;
    }

    public NPCHandle getNPC() {
        return npc;
    }
    public String getPlayerName() {
        return playerName;
    }
    public NPCInteractionType getInteractionType() {
        return interactionType;
    }

    @Override
    public String toString() {
        return "NPCInteractionEvent{npc=" + npc.getUniqueId() + ", player=" + playerName + ", type=" + interactionType + "}";
    }
}