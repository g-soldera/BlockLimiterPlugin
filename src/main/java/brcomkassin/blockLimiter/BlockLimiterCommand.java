package brcomkassin.blockLimiter;

import brcomkassin.BlockLimiterPlugin;
import brcomkassin.utils.Message;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockLimiterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (args.length < 1) {
            Message.Chat.send(player, "&4Uso correto: /limitar <valor>");
            return false;
        }

        if (args[0].equalsIgnoreCase("open")) {
            BlockLimiter.openInventory(player);
            return true;
        }

        if (itemInHand.getType() == Material.AIR) {
            Message.Chat.send(player,
                    "&4Você precisa está segurando um item na mão para usar o comando!");
            return false;
        }

        try {
            int limit = Integer.parseInt(args[0]);
            BlockLimiter.addLimitedBlock(player, itemInHand, limit);
        } catch (IllegalArgumentException e) {
            Message.Chat.send(player, "&4Você deve passar um valor como parâmetro!");
            Message.Chat.send(player, "&4Uso correto: /limitar <valor>");
            return true;
        }

        return false;
    }
}
