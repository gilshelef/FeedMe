package com.gilshelef.feedme.util;

import com.gilshelef.feedme.nonprofit.adapters.RecycledBaseAdapter;

/**
 * Created by gilshe on 3/29/17.
 */

public interface OnUpdateCount extends RecycledBaseAdapter.OnActionEvent {
    void updateItemsCount();
}
