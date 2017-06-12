package com.gilshelef.feedme.nonprofit.data.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilshe on 3/10/17.
 */

public abstract class BaseType implements Type {

    protected String name;
    protected String hebrewName;
    protected int defaultThumbnail;
    protected float color;

    abstract void setColor();

    abstract void setDefaultThumbnail();

    abstract void setHebrewName();

    abstract void setName();

    @Override
    public String hebrew() {
        return hebrewName;
    }

    @Override
    public String english() {
        return name;
    }

    @Override
    public int defaultThumbnail() {
        return defaultThumbnail;
    }

    @Override
    public float color() {
        return color;
    }

    @Override
    public String toString(){
        return hebrew();
    }


    @Override
    public boolean equals(Object o) {
        return this == o || onEqual(o);
    }


    @Override
    public int hashCode() {
        return Integer.valueOf(name);
    }

    protected abstract boolean onEqual(Object o);

    @Override
    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put(Type.K_COLOR, color);
        result.put(Type.K_THUMBNAIL, defaultThumbnail);
        result.put(Type.K_HEBREW, hebrewName);
        result.put(Type.K_NAME, name);
        return result;
    }

    public void build() {
        setName();
        setHebrewName();
        setDefaultThumbnail();
        setColor();
    }

}
