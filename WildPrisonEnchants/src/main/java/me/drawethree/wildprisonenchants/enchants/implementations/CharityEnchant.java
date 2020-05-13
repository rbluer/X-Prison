package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.utils.Players;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CharityEnchant extends WildPrisonEnchantment {

    private final double chance;
    private final long minAmount;
    private final long maxAmount;

    public CharityEnchant(WildPrisonEnchants instance) {
        super(instance, 11);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
        this.minAmount = instance.getConfig().getLong("enchants." + id + ".Min-Money");
        this.maxAmount = instance.getConfig().getLong("enchants." + id + ".Max-Money");
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
            long randAmount;

            for (Player p : Players.all()) {
                randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
                plugin.getEconomy().depositPlayer(p, randAmount);
                if (p.equals(e.getPlayer())) {
                    p.sendMessage(plugin.getMessage("charity_your").replace("%amount%", String.format("%,d",randAmount)));
                } else {
                    p.sendMessage(plugin.getMessage("charity_other").replace("%amount%", String.format("%,d",randAmount)).replace("%player%", e.getPlayer().getName()));
                }
            }
        }
    }
}