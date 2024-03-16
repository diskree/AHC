package com.diskree.achievetodo.mixins;

import com.diskree.achievetodo.injection.UsableItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class HoeItemMixin implements UsableItem {

    @Unique
    private boolean isCanUseChecking;

    @Override
    public boolean achieveToDo$canUse(PlayerEntity player, BlockHitResult blockHitResult) {
        isCanUseChecking = true;
        boolean canUse = useOnBlock(new ItemUsageContext(player.getWorld(), player, null, null, blockHitResult)) == null;
        isCanUseChecking = false;
        return canUse;
    }

    @Shadow
    public abstract ActionResult useOnBlock(ItemUsageContext context);

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemUsageContext;getPlayer()Lnet/minecraft/entity/player/PlayerEntity;", shift = At.Shift.BEFORE), cancellable = true)
    public void returnOnUse(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (isCanUseChecking) {
            cir.setReturnValue(null);
        }
    }
}
