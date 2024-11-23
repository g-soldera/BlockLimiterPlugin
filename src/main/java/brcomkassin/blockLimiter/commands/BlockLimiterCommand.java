package brcomkassin.blockLimiter.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import brcomkassin.blockLimiter.inventory.LimiterInventory;
import brcomkassin.blockLimiter.limiter.BlockLimiter;
import brcomkassin.config.ConfigManager;
import brcomkassin.utils.Message;
import lombok.SneakyThrows;

public class BlockLimiterCommand implements TabExecutor {

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            LimiterInventory.openInventory(player);
            return true;
        }

        if (!player.hasPermission("limiter.perm")) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.no-permission"));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "criar":
                    handleCreateGroup(player, args);
                    break;

                case "adicionar":
                    handleAddToGroup(player, args);
                    break;

                case "deletar":
                    handleDeleteGroup(player, args);
                    break;

                case "limite":
                    handleUpdateLimit(player, args);
                    break;

                case "remover":
                    handleRemoveFromGroup(player, args);
                    break;

                default:
                    showHelp(player);
                    break;
            }
        } catch (SQLException e) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.group-not-found", 
                "group", args[1]));
        }
        return true;
    }

    private void handleCreateGroup(Player player, String[] args) throws SQLException {
        if (args.length < 3) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.wrong-usage", 
                "usage", "/limites criar <nome_grupo> <limite>"));
            return;
        }
        
        String groupName = args[1];
        int limit;
        
        try {
            limit = Integer.parseInt(args[2]);
            if (limit <= 0) {
                Message.Chat.send(player, ConfigManager.getMessage("commands.errors.limit-must-be-positive"));
                return;
            }
        } catch (NumberFormatException e) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.invalid-limit"));
            return;
        }

        Material material = player.getInventory().getItemInMainHand().getType();
        if (material == Material.AIR) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.hold-item", 
                "action", "criar um grupo"));
            return;
        }

        BlockLimiter.addBlockGroup(groupName, limit, material);
        Message.Chat.send(player, ConfigManager.getMessage("commands.group-created"));
    }

    private void handleAddToGroup(Player player, String[] args) throws SQLException {
        if (args.length < 2) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.wrong-usage", 
                "usage", "/limites adicionar <nome_grupo>"));
            return;
        }

        Material material = player.getInventory().getItemInMainHand().getType();
        if (material == Material.AIR) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.hold-item", 
                "action", "adicionar ao grupo"));
            return;
        }

        String groupName = args[1];
        BlockLimiter.addMaterialToGroup(groupName, material);
        Message.Chat.send(player, ConfigManager.getMessage("commands.item-added"));
    }

    private void handleDeleteGroup(Player player, String[] args) throws SQLException {
        if (args.length < 2) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.wrong-usage", 
                "usage", "/limites deletar <nome_grupo>"));
            return;
        }

        String groupName = args[1];
        BlockLimiter.deleteGroup(groupName);
        Message.Chat.send(player, ConfigManager.getMessage("commands.group-deleted"));
    }

    private void handleUpdateLimit(Player player, String[] args) throws SQLException {
        if (args.length < 3) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.wrong-usage", 
                "usage", "/limites limite <nome_grupo> <novo_limite>"));
            return;
        }

        String groupName = args[1];
        int newLimit;
        
        try {
            newLimit = Integer.parseInt(args[2]);
            if (newLimit <= 0) {
                Message.Chat.send(player, ConfigManager.getMessage("commands.errors.limit-must-be-positive"));
                return;
            }
        } catch (NumberFormatException e) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.invalid-limit"));
            return;
        }

        try {
            BlockLimiter.updateGroupLimit(groupName, newLimit);
            Message.Chat.send(player, ConfigManager.getMessage("commands.group-limit-updated"));
        } catch (SQLException e) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.group-not-found", 
                "group", groupName));
        }
    }

    private void handleRemoveFromGroup(Player player, String[] args) throws SQLException {
        if (args.length < 2) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.wrong-usage", 
                "usage", "/limites remover <nome_grupo>"));
            return;
        }

        Material material = player.getInventory().getItemInMainHand().getType();
        if (material == Material.AIR) {
            Message.Chat.send(player, ConfigManager.getMessage("commands.errors.hold-item", 
                "action", "remover do grupo"));
            return;
        }

        String groupName = args[1];
        BlockLimiter.removeMaterialFromGroup(groupName, material);
        Message.Chat.send(player, ConfigManager.getMessage("commands.item-removed"));
    }

    private void showHelp(Player player) {
        Message.Chat.send(player,
            "&6=== Comandos do Sistema de Limites ===",
            "&e/limites criar <nome_grupo> <limite> &7- Cria um novo grupo",
            "&e/limites adicionar <nome_grupo> &7- Adiciona o item na mão ao grupo",
            "&e/limites deletar <nome_grupo> &7- Deleta um grupo",
            "&e/limites limite <nome_grupo> <novo_limite> &7- Altera o limite de um grupo",
            "&e/limites remover <nome_grupo> &7- Remove o item na mão do grupo"
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        if (!player.hasPermission("limiter.perm")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("criar");
            completions.add("adicionar");
            completions.add("deletar");
            completions.add("limite");
            completions.add("remover");
            return completions;
        }

        return List.of();
    }
}
