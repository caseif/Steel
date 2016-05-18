package net.caseif.flint.steel.util.agent;

import static com.google.common.base.Preconditions.checkArgument;

import net.caseif.flint.common.util.agent.chat.IChatAgent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Platform implementation of {@link IChatAgent}.
 */
public class ChatAgent implements IChatAgent {

    @Override
    public void processAndSend(UUID recipient, String message) {
        // we can basically just send the message with the legacy codes
        getPlayer(recipient).sendMessage(message);
    }

    @Override
    public void processAndSend(UUID recipient, String... message) {
        getPlayer(recipient).sendMessage(message);
    }

    private Player getPlayer(UUID uuid) {
        Player pl = Bukkit.getPlayer(uuid);
        checkArgument(pl != null, "Cannot find player with given UUID");
        return pl;
    }

}
