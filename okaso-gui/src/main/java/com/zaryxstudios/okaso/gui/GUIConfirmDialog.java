package com.zaryxstudios.okaso.gui;

import com.zaryxstudios.okaso.common.compat.BukkitCompat;
import com.zaryxstudios.okaso.common.gui.GUIItem;
import com.zaryxstudios.okaso.common.text.TextColorizer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GUIConfirmDialog {

    private final Plugin plugin;
    private String title;
    private String message;
    private GUIItem confirmItem;
    private GUIItem cancelItem;
    private String confirmName;
    private String cancelName;
    private ItemStack confirmStack;
    private ItemStack cancelStack;
    private Consumer<Player> onConfirm;
    private Consumer<Player> onCancel;
    private int confirmSlot;
    private int cancelSlot;
    private int messageSlot;
    private GUIItem messageItem;

    public GUIConfirmDialog(Plugin plugin) {
        this.plugin = plugin;
        this.title = GUIMessages.get(GUIMessages.DIALOG_TITLE);
        this.message = GUIMessages.get(GUIMessages.DIALOG_MESSAGE);
        this.confirmStack = BukkitCompat.item("LIME_WOOL");
        this.cancelStack = BukkitCompat.item("RED_WOOL");
        this.confirmName = GUIMessages.get(GUIMessages.BUTTON_CONFIRM);
        this.cancelName = GUIMessages.get(GUIMessages.BUTTON_CANCEL);
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
        this.confirmStack = new ItemStack(material);
        return this;
    }

    public GUIConfirmDialog cancelMaterial(Material material) {
        this.cancelStack = new ItemStack(material);
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
        gui.fillBorder(OkasoBukkitGUIItem.of(BukkitCompat.item("GRAY_STAINED_GLASS_PANE")));
        if (messageItem != null) {
            gui.setItem(messageSlot, messageItem);
        } else if (message != null) {
            gui.setItem(messageSlot, OkasoBukkitGUIItem.builder(Material.PAPER)
                .name(message)
                .build());
        }
        GUIItem confirm = confirmItem;
        if (confirm == null) {
            confirm = OkasoBukkitGUIItem.builder(confirmStack)
                .name(confirmName)
                .build();
        }
        GUIItem confirmFinal = confirm;
        gui.setItem(confirmSlot, OkasoBukkitGUIItem.of(
            confirmFinal.getItemStack() instanceof ItemStack
                ? (ItemStack) confirmFinal.getItemStack()
                : confirmStack.clone(),
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
            cancel = OkasoBukkitGUIItem.builder(cancelStack)
                .name(cancelName)
                .build();
        }
        GUIItem cancelFinal = cancel;
        gui.setItem(cancelSlot, OkasoBukkitGUIItem.of(
            cancelFinal.getItemStack() instanceof ItemStack
                ? (ItemStack) cancelFinal.getItemStack()
                : cancelStack.clone(),
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
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("item", itemName);
        GUIConfirmDialog dialog = new GUIConfirmDialog(plugin)
            .title(GUIMessages.get(GUIMessages.DIALOG_DELETE_TITLE, placeholders, itemName))
            .message(GUIMessages.get(GUIMessages.DIALOG_DELETE_MESSAGE, placeholders, itemName))
            .confirmName(GUIMessages.get(GUIMessages.DIALOG_DELETE_CONFIRM))
            .onConfirm(onDelete);
        dialog.confirmStack = BukkitCompat.item("RED_WOOL");
        return dialog;
    }
}
