package com.gilshelef.feedme.nonprofit.adapters;

/**
 * Created by gilshe on 3/21/17.
 */

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int adapterPosition, int adapterPosition1);
    void onItemDismiss(int adapterPosition);
}
