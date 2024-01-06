package com.diskree.achievetodo.datagen;

import com.diskree.achievetodo.action.BlockedActionType;
import com.diskree.achievetodo.action.IGeneratedAdvancement;
import com.diskree.achievetodo.advancements.AdvancementsTab;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AdvancementsGenerator extends FabricAdvancementProvider {

    public static final String BLOCKED_ACTION_DEMYSTIFIED_CRITERION_PREFIX = "demystified_";
    public static final String BLOCKED_ACTION_UNBLOCKED_CRITERION_NAME = "unblocked";

    protected AdvancementsGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateAdvancement(Consumer<AdvancementEntry> consumer) {
        for (AdvancementsTab root : AdvancementsTab.values()) {
            if (!root.isModded || root == AdvancementsTab.HINTS) {
                continue;
            }
            AdvancementEntry rootAdvancement = Advancement.Builder
                    .createUntelemetered()
                    .display(
                            root.getIcon(),
                            root.getTitle(),
                            root.getDescription(),
                            root.getBackgroundTextureId(),
                            AdvancementFrame.TASK,
                            false,
                            false,
                            false
                    )
                    .criterion("tick", TickCriterion.Conditions.createTick())
                    .build(consumer, root.getRootAdvancementPath());
            AdvancementEntry parentAdvancement = rootAdvancement;
            List<BlockedActionType> addedBlockedActions = new ArrayList<>();
            for (List<IGeneratedAdvancement> row : root.children) {
                for (IGeneratedAdvancement advancement : row) {
                    ItemStack icon = advancement.getIcon();
                    if (icon == null) {
                        throw new IllegalArgumentException("Icon not found for " + root.getAdvancementPath(advancement));
                    }
                    Text title = advancement.getTitle();
                    if (title == null) {
                        throw new IllegalArgumentException("Title not found for " + root.getAdvancementPath(advancement));
                    }
                    Text description = advancement.getDescription();
                    if (description == null) {
                        throw new IllegalArgumentException("Description not found for " + root.getAdvancementPath(advancement));
                    }
                    if (advancement instanceof BlockedActionType blockedAction) {
                        if (blockedAction.getBlockedMessage() == null) {
                            throw new IllegalArgumentException("Blocked message not found for " + root.getAdvancementPath(advancement));
                        }
                        addedBlockedActions.add(blockedAction);
                    }
                    Advancement.Builder builder = Advancement.Builder
                            .createUntelemetered()
                            .parent(parentAdvancement)
                            .display(
                                    icon,
                                    title,
                                    description,
                                    null,
                                    AdvancementFrame.TASK,
                                    true,
                                    true,
                                    false
                            );
                    builder = setupRequirements(builder, advancement);
                    parentAdvancement = builder.build(consumer, root.getAdvancementPath(advancement));
                }
                parentAdvancement = rootAdvancement;
            }
            List<BlockedActionType> totalBlockedActions = new ArrayList<>(Arrays.asList(BlockedActionType.values()));
            if (addedBlockedActions.size() < totalBlockedActions.size()) {
                totalBlockedActions.removeAll(addedBlockedActions);
                throw new IllegalArgumentException("Skipped blocked actions: " + totalBlockedActions);
            }
        }
    }

    private Advancement.Builder setupRequirements(Advancement.Builder builder, IGeneratedAdvancement advancement) {
        if (advancement instanceof BlockedActionType blockedAction) {
            builder.criterion(BLOCKED_ACTION_DEMYSTIFIED_CRITERION_PREFIX + blockedAction.getName(), Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()));
            builder.criterion(BLOCKED_ACTION_UNBLOCKED_CRITERION_NAME, Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()));
        }
        return builder;
    }
}
