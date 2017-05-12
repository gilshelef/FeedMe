package com.gilshelef.feedme.nonprofit.data.types;

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

    public void build() {
        setName();
        setHebrewName();
        setDefaultThumbnail();
        setColor();
    }

}
