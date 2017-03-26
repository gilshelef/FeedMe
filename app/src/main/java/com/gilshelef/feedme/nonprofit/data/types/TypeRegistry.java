package com.gilshelef.feedme.nonprofit.data.types;

/**
 * Created by gilshe on 3/10/17.
 */

class TypeRegistry {


    static void registerAll(TypeManager typeManager) {
        Type vegetable = new Vegetables();
        vegetable.build();
        typeManager.register("ירקות",vegetable);

        Type clothes = new Clothes();
        clothes.build();
        typeManager.register("בגדים", clothes);

        Type pastry = new Pasty();
        pastry.build();
        typeManager.register("מאפים", pastry);

        Type other = new Other();
        other.build();
        typeManager.register(TypeManager.OTHER_DONATION, other);
    }
}
