package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class GUIConfirmDialog {

    private final Plugin plugin;
    private String title;
    private String message;
    private GUIItem confirmItem;
    private GUIItem cancelItem;
    private String confirmName;
    private String cancelName;
    private Material confirmMaterial;
    private Material cancelMaterial;
    private Consumer<Player> onConfirm;
    private Consumer<Player> onCancel;
    private int confirmSlot;
    private int cancelSlot;
    private int messageSlot;
    private GUIItem messageItem;

    public GUIConfirmDialog(Plugin plugin) {
        this.plugin = plugin;
        this.title = "&6Confirmar";
        this.message = "¿Estás seguro?";
        this.confirmMaterial = Material.LIME_WOOL;
        this.cancelMaterial = Material.RED_WOOL;
        this.confirmName = "&a&lConfirmar";
        this.cancelName = "&c&lCancelar";
        this.confirmSlot = 11;
        this.cancelSlot = 15;
        this.messageSlot = 4;
    }

    public GUIConfirmDialog title(String title) {
        this.title = title;
        return this;
    }

    public GUIConfirmDialog message(String message) {
        this.message = message;
        return this;
    }

    public GUIConfirmDialog messageItem(GUIItem item) {
        this.messageItem = item;
        return this;
    }

    public GUIConfirmDialog messageSlot(int slot) {
        this.messageSlot = slot;
        return this;
    }

    public GUIConfirmDialog confirmItem(GUIItem item) {
        this.confirmItem = item;
        return this;
    }

    public GUIConfirmDialog cancelItem(GUIItem item) {
        this.cancelItem = item;
        return this;
    }

    public GUIConfirmDialog confirmName(String name) {
        this.confirmName = name;
        return this;
    }

    public GUIConfirmDialog cancelName(String name) {
        this.cancelName = name;
        return this;
    }

    public GUIConfirmDialog confirmMaterial(Material material) {
        this.confirmMaterial = material;
        return this;
    }

    public GUIConfirmDialog cancelMaterial(Material material) {
        this.cancelMaterial = material;
        return this;
    }

    public GUIConfirmDialog confirmSlot(int slot) {
        this.confirmSlot = slot;
        return this;
    }

    public GUIConfirmDialog cancelSlot(int slot) {
        this.cancelSlot = slot;
        return this;
    }

    public GUIConfirmDialog onConfirm(Consumer<Player> handler) {
        this.onConfirm = handler;
        return this;
    }

    public GUIConfirmDialog onCancel(Consumer<Player> handler) {
        this.onCancel = handler;
        return this;
    }

    public void open(Player player) {
        OkasoBukkitGUI gui = new OkasoBukkitGUI(plugin, TextColorizer.translate(title), 27);
        gui.fillBorder(OkasoBukkitGUIItem.of(Material.GRAY_STAINED_GLASS_PANE));
        if (messageItem != null) {
            gui.setItem(messageSlot, messageItem);
        } else if (message != null) {
            gui.setItem(messageSlot, OkasoBukkitGUIItem.builder(Material.PAPER)
                .name(message)
                .build());
        }
        GUIItem confirm = confirmItem;
        if (confirm == null) {
            confirm = OkasoBukkitGUIItem.builder(confirmMaterial)
                .name(confirmName)
                .build();
        }
        GUIItem confirmFinal = confirm;
        gui.setItem(confirmSlot, OkasoBukkitGUIItem.of(
            confirmFinal.getItemStack() instanceof org.bukkit.inventory.ItemStack
                ? (org.bukkit.inventory.ItemStack) confirmFinal.getItemStack()
                : new org.bukkit.inventory.ItemStack(confirmMaterial),
            event -> {
                if (onConfirm != null) {
                    Object clicked = event.getWhoClicked();
                    if (clicked instanceof Player) {
                        onConfirm.accept((Player) clicked);
                    }
                }
                gui.closeAll();
            }));
        GUIItem cancel = cancelItem;
        if (cancel == null) {
            cancel = OkasoBukkitGUIItem.builder(cancelMaterial)
                .name(cancelName)
                .build();
        }
        GUIItem cancelFinal = cancel;
        gui.setItem(cancelSlot, OkasoBukkitGUIItem.of(
            cancelFinal.getItemStack() instanceof org.bukkit.inventory.ItemStack
                ? (org.bukkit.inventory.ItemStack) cancelFinal.getItemStack()
                : new org.bukkit.inventory.ItemStack(cancelMaterial),
            event -> {
                if (onCancel != null) {
                    Object clicked = event.getWhoClicked();
                    if (clicked instanceof Player) {
                        onCancel.accept((Player) clicked);
                    }
                }
                gui.closeAll();
            }));
        gui.open(player);
    }

    public static GUIConfirmDialog of(Plugin plugin) {
        return new GUIConfirmDialog(plugin);
    }

    public static GUIConfirmDialog yesNo(Plugin plugin, String question,
                                         Consumer<Player> onYes, Consumer<Player> onNo) {
        return new GUIConfirmDialog(plugin)
            .message(question)
            .onConfirm(onYes)
            .onCancel(onNo);
    }

    public static GUIConfirmDialog delete(Plugin plugin, String itemName,
                                          Consumer<Player> onDelete) {
        return new GUIConfirmDialog(plugin)
            .title("&cEliminar " + itemName)
            .message("¿Eliminar " + itemName + "?")
            .confirmMaterial(Material.RED_WOOL)
            .confirmName("&c&lEliminar")
            .onConfirm(onDelete);
    }
}
