package me.m0dii.extraenchants.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class EnchantInfoGUI implements InventoryHolder {
    private final Inventory inventory;

    private final Enchantment enchant;

    public EnchantInfoGUI(Enchantment enchant) {
        this.inventory = Bukkit.createInventory(this, 9 * 6, Utils.format("&8&lEnchant Information"));
        this.enchant = enchant;

        init();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void open(Player p) {
        p.openInventory(this.inventory);
    }

    public void init() {
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);

        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);

        ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemStack hoe = new ItemStack(Material.NETHERITE_HOE);

        this.inventory.setItem(10, helmet);
        this.inventory.setItem(11, canEnchant(helmet));
        this.inventory.setItem(19, chestplate);
        this.inventory.setItem(20, canEnchant(chestplate));
        this.inventory.setItem(28, leggings);
        this.inventory.setItem(29, canEnchant(leggings));
        this.inventory.setItem(37, boots);
        this.inventory.setItem(38, canEnchant(boots));

        this.inventory.setItem(22, sword);
        this.inventory.setItem(31, canEnchant(sword));

        this.inventory.setItem(16, pickaxe);
        this.inventory.setItem(15, canEnchant(pickaxe));
        this.inventory.setItem(25, axe);
        this.inventory.setItem(24, canEnchant(axe));
        this.inventory.setItem(34, shovel);
        this.inventory.setItem(33, canEnchant(shovel));
        this.inventory.setItem(43, hoe);
        this.inventory.setItem(42, canEnchant(hoe));

        ItemStack backButton = new ItemStack(Material.PAPER);
        ItemMeta meta = backButton.getItemMeta();

        meta.setDisplayName(Utils.format("&c&lBack"));

        backButton.setItemMeta(meta);

        this.inventory.setItem(49, backButton);

        for (int i = 0; i < this.inventory.getSize(); i++) {
            if (this.inventory.getItem(i) == null) {
                ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta glassMeta = glass.getItemMeta();

                glassMeta.setDisplayName(" ");

                glass.setItemMeta(glassMeta);

                this.inventory.setItem(i, glass);
            }
        }
    }

    private ItemStack canEnchant(ItemStack item) {
        ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName(Utils.format("&a&lCan Enchant"));

        if (!enchant.canEnchantItem(item)) {
            itemStack.setType(Material.RED_STAINED_GLASS_PANE);

            meta.setDisplayName(Utils.format("&c&lCan't Enchant"));
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
