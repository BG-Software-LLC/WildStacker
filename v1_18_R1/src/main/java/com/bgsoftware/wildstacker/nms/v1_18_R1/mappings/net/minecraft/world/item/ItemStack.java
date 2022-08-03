package com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.world.item;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import net.minecraft.sounds.SoundEffect;

public class ItemStack extends MappedObject<net.minecraft.world.item.ItemStack> {

    public static final ItemStack AIR = new ItemStack(net.minecraft.world.item.ItemStack.b);

    public static ItemStack ofNullable(net.minecraft.world.item.ItemStack handle) {
        return handle == null ? null : new ItemStack(handle);
    }

    public ItemStack(net.minecraft.world.item.ItemStack handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "of",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static ItemStack of(net.minecraft.nbt.NBTTagCompound tagCompound) {
        return new ItemStack(net.minecraft.world.item.ItemStack.a(tagCompound));
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getItem",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public Item getItem() {
        return new Item(handle.c());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "isEmpty",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean isEmpty() {
        return handle.b();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "shrink",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public void shrink(int amount) {
        handle.g(amount);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getOrCreateTag",
            type = Remap.Type.METHOD,
            remappedName = "t")
    public NBTTagCompound getOrCreateTag() {
        return new NBTTagCompound(handle.t());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "setTag",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void setTag(net.minecraft.nbt.NBTTagCompound nbtTagCompound) {
        handle.c(nbtTagCompound);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "copy",
            type = Remap.Type.METHOD,
            remappedName = "m")
    public ItemStack copy() {
        return new ItemStack(handle.m());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "setCount",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public void setCount(int count) {
        handle.e(count);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getDamageValue",
            type = Remap.Type.METHOD,
            remappedName = "h")
    public int getDamageValue() {
        return handle.h();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "setDamageValue",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void setDamageValue(int damage) {
        handle.b(damage);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "save",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void save(net.minecraft.nbt.NBTTagCompound nbtTagCompound) {
        handle.b(nbtTagCompound);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "isDamageableItem",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public boolean isDamageableItem() {
        return handle.f();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getEquipSound",
            type = Remap.Type.METHOD,
            remappedName = "M")
    public SoundEffect getEquipSound() {
        return handle.M();
    }

}
