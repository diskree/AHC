package com.diskree.achievetodo.injection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;

public interface UsableItem {
    boolean achieveToDo$canUse(PlayerEntity player, BlockHitResult blockHitResult);
}
