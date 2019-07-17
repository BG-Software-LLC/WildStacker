package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableTemp;
import com.bgsoftware.wildstacker.utils.Executor;
import net.aminecraftdev.custombosses.CustomBosses;
import net.aminecraftdev.custombosses.utils.IBossUtils;
import net.aminecraftdev.custombosses.utils.IDropTableUtils;
import net.aminecraftdev.custombosses.zcore.c.CompositeRoot;
import net.aminecraftdev.custombosses.zcore.i.ICompositeRoot;
import net.aminecraftdev.custombosses.zcore.i.INumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CustomBossesHook {

    private static IBossUtils bossUtils;
    private static IDropTableUtils dropTableUtils;
    private static INumberUtils numberUtils;
    private static CustomBosses plugin;

    static {
        plugin = CustomBosses.getInstance();
        try{
            Field compositeRootField = CustomBosses.class.getDeclaredField("_compositeRoot");
            compositeRootField.setAccessible(true);
            ICompositeRoot compositeRoot = (ICompositeRoot) compositeRootField.get(plugin);
            compositeRootField.setAccessible(false);
            bossUtils = compositeRoot.getBossUtils();
            dropTableUtils = compositeRoot.getDropTableUtils();

            Field numberUtilsField = CompositeRoot.class.getDeclaredField("_numberUtils");
            numberUtilsField.setAccessible(true);
            numberUtils = (INumberUtils) numberUtilsField.get(compositeRoot);
            numberUtilsField.setAccessible(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static LootTable getLootTable(){
        return new LootTableTemp(){

            @Override
            public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
                List<ItemStack> drops = new ArrayList<>();
                for(int i = 0; i < stackAmount; i++)
                    drops.addAll(getBossDrops(stackedEntity.getLivingEntity()));
                return drops;
            }

        };
    }

    private static List<ItemStack> getBossDrops(LivingEntity livingEntity){
        List<ItemStack> drops = new ArrayList<>();

        String bossType = bossUtils.getBossType(livingEntity.getUniqueId());
        String tableName = plugin.getBossesYML().getConfigurationSection("Bosses." + bossType)
                .getConfigurationSection("Drops").getString("DropTable.table");

        Map<Integer, Player> bossKillers = bossUtils.getMapOfBossKillers(livingEntity.getUniqueId());
        bossKillers.put(1, livingEntity.getKiller());

        if(plugin.getDropTableYML().contains(tableName)) {
            ConfigurationSection tableSection = plugin.getDropTableYML().getConfigurationSection(tableName);

            for (String key : tableSection.getKeys(false)) {
                if(numberUtils.isStringInteger(key)) {
                    int index = Integer.valueOf(key);
                    ConfigurationSection var8 = tableSection.getConfigurationSection(key);
                    if (bossKillers.containsKey(index)) {
                        Player killer = bossKillers.get(index);

                        boolean randomCommand = var8.getBoolean("RandomCommand");
                        boolean randomCustomDrop = var8.getBoolean("RandomCustomDrop");
                        double requiredPercent = var8.getDouble("RequiredPercent", 0.0D);
                        double playerDamage = bossUtils.getPlayerDamage(killer.getName(), livingEntity.getUniqueId());

                        List<ItemStack> bossDrops = randomCustomDrop ? dropTableUtils.getRandomBossCustomDrops(var8) : dropTableUtils.getBossCustomDrops(var8);
                        List<String> bossCommands = randomCommand ? dropTableUtils.getRandomBossCommands(var8, killer) : dropTableUtils.getBossCommands(var8, killer);

                        if(playerDamage >= requiredPercent) {
                            Executor.sync(() -> {
                                for (String bossCommand : bossCommands)
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), bossCommand);
                            });

                            drops.addAll(bossDrops);
                        }
                    }
                }
            }
        }

        return drops;
    }

}

