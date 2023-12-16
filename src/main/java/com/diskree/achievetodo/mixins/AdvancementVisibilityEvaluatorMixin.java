package com.diskree.achievetodo.mixins;

import com.diskree.achievetodo.AchieveToDo;
import it.unimi.dsi.fastutil.Stack;
import net.minecraft.advancement.AdvancementDisplays;
import net.minecraft.advancement.PlacedAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(AdvancementDisplays.class)
public abstract class AdvancementVisibilityEvaluatorMixin {

    @Inject(method = "shouldDisplay(Lnet/minecraft/advancement/PlacedAdvancement;Lit/unimi/dsi/fastutil/Stack;Ljava/util/function/Predicate;Lnet/minecraft/advancement/AdvancementDisplays$ResultConsumer;)Z", at = @At("HEAD"), cancellable = true)
    private static void calculateDisplayInject(PlacedAdvancement advancement, Stack<AdvancementDisplays.Status> statuses, Predicate<PlacedAdvancement> donePredicate, AdvancementDisplays.ResultConsumer consumer, CallbackInfoReturnable<Boolean> cir) {
        if (AchieveToDo.getBlockedActionFromAdvancement(advancement) != null) {
            statuses.push(AdvancementDisplays.Status.SHOW);
            for (PlacedAdvancement child : advancement.getChildren()) {
                AdvancementDisplays.shouldDisplay(child, statuses, donePredicate, consumer);
            }
            consumer.accept(advancement, true);
            cir.setReturnValue(true);
        }
    }
}
