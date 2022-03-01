package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import me.lucko.helper.time.Time;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NukeEnchant extends UltraPrisonEnchantment {

	private double chance;
	private boolean countBlocksBroken;

	public NukeEnchant(UltraPrisonEnchants instance) {
		super(instance, 21);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {

			long startTime = Time.nowMillis();
			Block b = e.getBlock();
			IWrappedRegion region = RegionUtils.getMineRegionWithHighestPriority(b.getLocation());
			if (region != null) {
				Player p = e.getPlayer();
				ICuboidSelection selection = (ICuboidSelection) region.getSelection();

				List<Block> blocksAffected = new ArrayList<>();

				double totalDeposit = 0;
				int blockCount = 0;
				int fortuneLevel = plugin.getApi().getEnchantLevel(p.getItemInHand(), 3);
				int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;

				boolean autoSellEnabledPlayer = this.plugin.isAutoSellModule() && plugin.getCore().getAutoSell().hasAutoSellEnabled(p);

				for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
					for (int z = selection.getMinimumPoint().getBlockZ(); z <= selection.getMaximumPoint().getBlockZ(); z++) {
						for (int y = selection.getMinimumPoint().getBlockY(); y <= selection.getMaximumPoint().getBlockY(); y++) {
							Block b1 = b.getWorld().getBlockAt(x, y, z);
							if (b1.getType() == Material.AIR) {
								continue;
							}
							blockCount++;
							blocksAffected.add(b1);
							if (autoSellEnabledPlayer) {
								totalDeposit += ((plugin.getCore().getAutoSell().getPriceForBrokenBlock(region.getId(), b1) + 0.0) * amplifier);
							} else {
								if (plugin.getCore().isUltraBackpacksEnabled()) {
									continue;
								}
								p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
							}
						}
					}
				}

				this.plugin.getCore().debug("NukeEnchant::onBlockBreak::LoopingBlocks >> Took " + (System.currentTimeMillis() - startTime) + " ms.", this.plugin);

				if (plugin.getCore().getJetsPrisonMinesAPI() != null) {
					plugin.getCore().getJetsPrisonMinesAPI().blockBreak(blocksAffected);
				}

				if (this.plugin.isMinesModule()) {
					Mine mine = plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation());

					if (mine != null) {
						mine.handleBlockBreak(blocksAffected);
					}
				}

				boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

				double total = this.plugin.isMultipliersModule() ? plugin.getCore().getMultipliers().getApi().getTotalToDeposit(p, totalDeposit, MultiplierType.SELL) : totalDeposit;
				total = luckyBooster ? total * 2 : total;

				plugin.getCore().getEconomy().depositPlayer(p, total);

				if (this.plugin.isAutoSellModule()) {
					plugin.getCore().getAutoSell().addToCurrentEarnings(p, total);
				}

				if (this.countBlocksBroken) {
					plugin.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
				}
				plugin.getCore().getTokens().handleBlockBreak(p, blocksAffected, countBlocksBroken);

				if (plugin.getCore().isUltraBackpacksEnabled()) {
					UltraBackpacksAPI.handleBlocksBroken(p, blocksAffected);
				}

				for (Block b1 : blocksAffected) {
					this.plugin.getCore().getNmsProvider().setBlockInNativeDataPalette(b1.getWorld(), b1.getX(), b1.getY(), b1.getZ(), 0, (byte) 0, true);
				}

			}
			long timeEnd = Time.nowMillis();
			this.plugin.getCore().debug("NukeEnchant::onBlockBreak >> Took " + (timeEnd - startTime) + " ms.", this.plugin);
		}

	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.countBlocksBroken = plugin.getConfig().get().getBoolean("enchants." + id + ".Count-Blocks-Broken");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}