package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.files.SoundWrapper;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WildMenu implements InventoryHolder {

    protected static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    protected static final Map<String, MenuData> dataMap = new ConcurrentHashMap<>();

    private final String identifier;

    protected Inventory inventory;

    protected WildMenu(String identifier){
        this.identifier = identifier;
    }

    public final void onButtonClick(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();

        e.setCancelled(true);

        if(e.getCurrentItem() != null) {
            SoundWrapper sound = getSound(e.getRawSlot());
            if (sound != null)
                sound.playSound(player);

            List<String> commands = getCommands(e.getRawSlot());
            if (commands != null) {
                commands.forEach(command -> {
                    boolean playerExecute = false;
                    if(command.startsWith("PLAYER:")) {
                        command = command.replaceFirst("PLAYER:", "");
                        playerExecute = true;
                    }

                    Bukkit.dispatchCommand(playerExecute ? player : Bukkit.getConsoleSender(), command);
                });
            }

            Pair<String, SoundWrapper> permission = getPermission(e.getRawSlot());
            if(permission != null && !player.hasPermission(permission.getKey())){
                if(permission.getValue() != null)
                    permission.getValue().playSound(player);

                return;
            }
        }

        onPlayerClick(e);
    }

    public abstract void onPlayerClick(InventoryClickEvent e);

    public abstract void onMenuClose(InventoryCloseEvent e);

    public void onInventoryBuild(){

    }

    @Override
    public Inventory getInventory() {
        return buildInventory();
    }

    public void openMenu(Player player){
        if(inventory == null) {
            if (Bukkit.isPrimaryThread()) {
                Executor.async(() -> openMenu(player));
                return;
            }

            try {
                inventory = getInventory();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        Executor.sync(() -> {
            if(!player.isOnline())
                return;

            if(Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
                return;

            onInventoryBuild();

            player.openInventory(inventory);
        });
    }

    protected Inventory buildInventory(){
        MenuData menuData = getData();

        Inventory inventory = Bukkit.createInventory(this, menuData.rowsSize * 9, menuData.title);

        for(Map.Entry<Integer, ItemBuilder> itemStackEntry : menuData.fillItems.entrySet()) {
            ItemBuilder itemBuilder = itemStackEntry.getValue().copy();
            if(itemStackEntry.getKey() >= 0)
                inventory.setItem(itemStackEntry.getKey(), itemBuilder.build());
        }

        return inventory;
    }

    public void resetData(){
        dataMap.put(identifier, new MenuData());
    }

    public void setTitle(String title){
        getData().title = title;
    }

    public void setRowsSize(int rowsSize){
        getData().rowsSize = rowsSize;
    }

    public void addSound(int slot, SoundWrapper sound) {
        if(sound != null)
            getData().sounds.put(slot, sound);
    }

    public void addCommands(int slot, List<String> commands) {
        if(commands != null && !commands.isEmpty())
            getData().commands.put(slot, commands);
    }

    public void addPermission(int slot, String permission, SoundWrapper noAccessSound) {
        if(permission != null && !permission.isEmpty())
            getData().permissions.put(slot, new Pair<>(permission, noAccessSound));
    }

    public void addFillItem(int slot, ItemBuilder itemBuilder){
        if(itemBuilder != null)
            getData().fillItems.put(slot, itemBuilder);
    }

    private SoundWrapper getSound(int slot){
        return getData().sounds.get(slot);
    }

    private List<String> getCommands(int slot){
        return getData().commands.get(slot);
    }

    private Pair<String, SoundWrapper> getPermission(int slot){
        return getData().permissions.get(slot);
    }

    protected MenuData getData(){
        if(!dataMap.containsKey(identifier)){
            dataMap.put(identifier, new MenuData());
        }

        return dataMap.get(identifier);
    }

    protected static final class MenuData{

        public final Map<Integer, SoundWrapper> sounds = new HashMap<>();
        public final Map<Integer, List<String>> commands = new HashMap<>();
        public final Map<Integer, Pair<String, SoundWrapper>> permissions = new HashMap<>();
        public final Map<Integer, ItemBuilder> fillItems = new HashMap<>();
        public String title = "";
        public int rowsSize = 6;

    }

}
