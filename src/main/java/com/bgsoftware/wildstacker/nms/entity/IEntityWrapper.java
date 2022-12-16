package com.bgsoftware.wildstacker.nms.entity;

public interface IEntityWrapper {

    void setHealth(float health, boolean preventUpdate);

    void setRemoved(boolean removed);

    void setDead(boolean dead);

}
