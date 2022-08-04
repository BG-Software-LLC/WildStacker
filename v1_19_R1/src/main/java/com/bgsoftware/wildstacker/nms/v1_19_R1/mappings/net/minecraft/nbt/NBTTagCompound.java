package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.nbt;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.nbt.NBTBase;

public class NBTTagCompound extends MappedObject<net.minecraft.nbt.NBTTagCompound> {

    public static NBTTagCompound ofNullable(net.minecraft.nbt.NBTTagCompound handle) {
        return handle == null ? null : new NBTTagCompound(handle);
    }

    public NBTTagCompound() {
        super(new net.minecraft.nbt.NBTTagCompound());
    }

    public NBTTagCompound(net.minecraft.nbt.NBTTagCompound handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public boolean contains(String key) {
        return handle.e(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean contains(String key, int type) {
        return handle.b(key, type);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getCompound",
            type = Remap.Type.METHOD,
            remappedName = "p")
    public NBTTagCompound getCompound(String key) {
        return NBTTagCompound.ofNullable(handle.p(key));
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putString",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void putString(String key, String value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "put",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void put(String key, NBTBase nbtBase) {
        handle.a(key, nbtBase);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putFloat",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void putFloat(String key, float value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "remove",
            type = Remap.Type.METHOD,
            remappedName = "r")
    public void remove(String key) {
        handle.r(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getBoolean",
            type = Remap.Type.METHOD,
            remappedName = "q")
    public boolean getBoolean(String key) {
        return handle.q(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getInt",
            type = Remap.Type.METHOD,
            remappedName = "h")
    public int getInt(String key) {
        return handle.h(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getString",
            type = Remap.Type.METHOD,
            remappedName = "l")
    public String getString(String key) {
        return handle.l(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getDouble",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public double getDouble(String key) {
        return handle.k(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getShort",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public short getShort(String key) {
        return handle.g(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getByte",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public byte getByte(String key) {
        return handle.f(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getFloat",
            type = Remap.Type.METHOD,
            remappedName = "j")
    public float getFloat(String key) {
        return handle.j(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getLong",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public long getLong(String key) {
        return handle.i(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "size",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public int size() {
        return handle.e();
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "get",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public NBTBase get(String key) {
        return handle.c(key);
    }

}
