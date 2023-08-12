package com.diskree.achievetodo;

import com.diskree.achievetodo.advancements.hints.*;
import com.diskree.achievetodo.server.AchieveToDoServer;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

public class AchieveToDo implements ModInitializer {

    public static final String ID = "achievetodo";

    public static final String BACAP_DATA_PACK = "file/bacap.zip";
    public static final String BACAP_HARDCORE_DATA_PACK = "file/bacap_hardcore.zip";
    public static final String BACAP_TERRALITH_DATA_PACK = "file/bacap_terralith.zip";
    public static final String BACAP_AMPLIFIED_NETHER_DATA_PACK = "file/bacap_amplified_nether.zip";
    public static final String BACAP_NULLSCAPE_DATA_PACK = "file/bacap_nullscape.zip";

    public static final String TERRALITH_DATA_PACK = "file/terralith.zip";
    public static final String AMPLIFIED_NETHER_DATA_PACK = "file/amplified_nether.zip";
    public static final String NULLSCAPE_DATA_PACK = "file/nullscape.zip";

    public static final String BACAP_OVERRIDE_DATA_PACK = AchieveToDo.ID + "/" + "bacap_override";
    public static final String BACAP_OVERRIDE_HARDCORE_DATA_PACK = AchieveToDo.ID + "/" + "bacap_override_hardcore";
    public static final String BACAP_REWARDS_ITEM_DATA_PACK_NAME = AchieveToDo.ID + "/" + "bacap_rewards_item";
    public static final String BACAP_REWARDS_EXPERIENCE_DATA_PACK_NAME = AchieveToDo.ID + "/" + "bacap_rewards_experience";
    public static final String BACAP_REWARDS_TROPHY_DATA_PACK_NAME = AchieveToDo.ID + "/" + "bacap_rewards_trophy";
    public static final String BACAP_LANGUAGE_PACK = AchieveToDo.ID + "/" + "bacap_lp";


    public static final Identifier ANCIENT_CITY_PORTAL_BLOCK_ID = new Identifier(ID, "ancient_city_portal");
    public static final AncientCityPortalBlock ANCIENT_CITY_PORTAL_BLOCK = new AncientCityPortalBlock(AbstractBlock.Settings.create()
            .noCollision()
            .ticksRandomly()
            .instrument(NoteBlockInstrument.BASEDRUM)
            .strength(-1.0f, 3600000.0f)
            .dropsNothing()
            .sounds(BlockSoundGroup.GLASS)
            .luminance((blockState) -> 11)
            .pistonBehavior(PistonBehavior.BLOCK));

    public static final Identifier REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID = new Identifier(ID, "reinforced_deepslate_charged");
    public static final Block REINFORCED_DEEPSLATE_CHARGED_BLOCK = new Block(QuiltBlockSettings.create());

    public static final Identifier REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID = new Identifier(ID, "reinforced_deepslate_broken");
    public static final Block REINFORCED_DEEPSLATE_BROKEN_BLOCK = new Block(QuiltBlockSettings.create());

    public static final Identifier LOCKED_ACTION_ITEM_ID = new Identifier(ID, "locked_action");
    public static final Item LOCKED_ACTION_ITEM = new Item(new QuiltItemSettings());

    public static final Identifier ANCIENT_CITY_PORTAL_HINT_ITEM_ID = new Identifier(ID, "ancient_city_portal_hint");
    public static final Item ANCIENT_CITY_PORTAL_HINT_ITEM = new Item(new QuiltItemSettings().maxDamage(1000));

    public static final Identifier ANCIENT_CITY_PORTAL_PARTICLES_ID = new Identifier(ID, "ancient_city_portal_particles");
    public static final DefaultParticleType ANCIENT_CITY_PORTAL_PARTICLES = FabricParticleTypes.simple();

    public static final Identifier JUKEBOX_PLAY_EVENT_ID = new Identifier(ID, "jukebox_play");
    public static final GameEvent JUKEBOX_PLAY = new GameEvent(JUKEBOX_PLAY_EVENT_ID.toString(), AncientCityPortalEntity.RITUAL_RADIUS);
    public static final EntityType<AncientCityPortalEntity> ANCIENT_CITY_PORTAL_ADVANCEMENT = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalEntity::new)
            .setDimensions(EntityDimensions.changing(0.0f, 0.0f))
            .maxChunkTrackingRange(10)
            .trackingTickInterval(1)
            .build();
    public static final Identifier JUKEBOX_STOP_PLAY_EVENT_ID = new Identifier(ID, "jukebox_stop_play");
    public static final GameEvent JUKEBOX_STOP_PLAY = new GameEvent(JUKEBOX_STOP_PLAY_EVENT_ID.toString(), AncientCityPortalEntity.RITUAL_RADIUS);
    public static final Identifier ANCIENT_CITY_PORTAL_TAB_ENTITY_ID = new Identifier(ID, "ancient_city_portal_tab_entity");
    public static final EntityType<AncientCityPortalTabEntity> ANCIENT_CITY_PORTAL_TAB = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalTabEntity::new)
            .setDimensions(EntityDimensions.changing(0.0f, 0.0f))
            .maxChunkTrackingRange(10)
            .trackingTickInterval(1)
            .build();
    public static final Identifier ANCIENT_CITY_PORTAL_ADVANCEMENT_ENTITY_ID = new Identifier(ID, "ancient_city_portal_advancement_entity");
    public static final Identifier ANCIENT_CITY_PORTAL_HINT_ENTITY_ID = new Identifier(ID, "ancient_city_portal_prompt_entity");
    public static final EntityType<AncientCityPortalPromptEntity> ANCIENT_CITY_PORTAL_HINT = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalPromptEntity::new)
            .setDimensions(EntityDimensions.changing(0.0f, 0.0f))
            .maxChunkTrackingRange(10)
            .trackingTickInterval(1)
            .build();

    public static final Identifier ANCIENT_CITY_PORTAL_EXPERIENCE_ORB_ENTITY_ID = new Identifier(ID, "ancient_city_portal_experience_orb");
    public static final EntityType<AncientCityPortalExperienceOrbEntity> ANCIENT_CITY_PORTAL_EXPERIENCE_ORB = QuiltEntityTypeBuilder.<AncientCityPortalExperienceOrbEntity>create(SpawnGroup.MISC, AncientCityPortalExperienceOrbEntity::new)
            .setDimensions(EntityDimensions.changing(0.5f, 0.5f))
            .maxChunkTrackingRange(6)
            .trackingTickInterval(20)
            .build();

    public static final SoundEvent MUSIC_DISC_5_ACTIVATOR = SoundEvent.createVariableRangeEvent(new Identifier(ID, "music_disc_5_activator"));
    public static final Identifier EVOKER_NO_TOTEM_OF_UNDYING_LOOT_TABLE_ID = new Identifier(ID, "entities/evoker_no_totem_of_undying");

    public static int getScore(PlayerEntity player) {
        if (player == null) {
            return 0;
        }
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return 0;
        }
        ScoreboardObjective scoreObjective = scoreboard.getObjective("bac_advancements");
        if (scoreObjective == null) {
            return 0;
        }
        ScoreboardPlayerScore playerScore = scoreboard.getPlayerScore(player.getEntityName(), scoreObjective);
        if (playerScore == null) {
            return 0;
        }
        return playerScore.getScore();
    }

    @Override
    public void onInitialize(ModContainer mod) {
        registerPacks();
        registerBlocks();
        registerItems();
        registerParticles();
        registerEvents();
        registerEntities();

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world != null && world.getRegistryKey() == World.OVERWORLD && pos != null) {
                if (pos.getY() >= 0 && AchieveToDoServer.isActionBlocked(player, BlockedAction.BREAK_BLOCKS_IN_POSITIVE_Y)) {
                    return ActionResult.FAIL;
                }
                if (pos.getY() < 0 && AchieveToDoServer.isActionBlocked(player, BlockedAction.BREAK_BLOCKS_IN_NEGATIVE_Y)) {
                    return ActionResult.FAIL;
                }
            }
            if (AchieveToDoServer.isToolBlocked(player, hand)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (AchieveToDoServer.isToolBlocked(player, hand)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = hand == Hand.MAIN_HAND ? player.getMainHandStack() : player.getOffHandStack();
            if (stack.isFood() && AchieveToDoServer.isFoodBlocked(player, stack.getItem().getFoodComponent())) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            if (stack.isOf(Items.SHIELD) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_SHIELD)) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            if (stack.isOf(Items.BOW) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_BOW)) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            if (stack.isOf(Items.FIREWORK_ROCKET) && player.isFallFlying() && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_FIREWORKS_WHILE_FLY)) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            if (stack.getItem() instanceof ArmorItem armorItem && AchieveToDoServer.isEquipmentBlocked(player, armorItem)) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockState block = world.getBlockState(hitResult.getBlockPos());
            if (block.isOf(Blocks.CRAFTING_TABLE) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_CRAFTING_TABLE)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.FURNACE) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_FURNACE)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.ANVIL) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_ANVIL)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.SMOKER) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_SMOKER)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.BLAST_FURNACE) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_BLAST_FURNACE)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.ENDER_CHEST) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_ENDER_CHEST)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.BREWING_STAND) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_BREWING_STAND)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.BEACON) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_BEACON)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.BLACK_SHULKER_BOX) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_SHULKER_BOX)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.SHULKER_BOX) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_SHULKER_BOX)) {
                return ActionResult.FAIL;
            }
            if (block.isOf(Blocks.ENCHANTING_TABLE) && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_ENCHANTING_TABLE)) {
                return ActionResult.FAIL;
            }
            if (AchieveToDoServer.isToolBlocked(player, hand)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof BoatEntity && AchieveToDoServer.isActionBlocked(player, BlockedAction.USING_BOAT)) {
                return ActionResult.FAIL;
            }
            if (entity instanceof VillagerEntity villagerEntity && AchieveToDoServer.isVillagerBlocked(player, villagerEntity.getVillagerData().getProfession())) {
                villagerEntity.sayNo();
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private void registerPacks() {
        QuiltLoader.getModContainer(AchieveToDo.ID).ifPresent((modContainer) -> {
            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_OVERRIDE_DATA_PACK.replace("/", ":")), modContainer, ResourcePackActivationType.NORMAL);
            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_OVERRIDE_HARDCORE_DATA_PACK.replace("/", ":")), modContainer, ResourcePackActivationType.NORMAL);
            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_REWARDS_ITEM_DATA_PACK_NAME.replace("/", ":")), modContainer, ResourcePackActivationType.NORMAL);
            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_REWARDS_EXPERIENCE_DATA_PACK_NAME.replace("/", ":")), modContainer, ResourcePackActivationType.NORMAL);
            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_REWARDS_TROPHY_DATA_PACK_NAME.replace("/", ":")), modContainer, ResourcePackActivationType.NORMAL);

            ResourceLoader.registerBuiltinResourcePack(new Identifier(BACAP_LANGUAGE_PACK.replace("/", ":")), modContainer, ResourcePackActivationType.DEFAULT_ENABLED, Text.of("BACAP Language Pack"));
        });
    }

    private void registerBlocks() {
        Registry.register(Registries.BLOCK, ANCIENT_CITY_PORTAL_BLOCK_ID, ANCIENT_CITY_PORTAL_BLOCK);
        Registry.register(Registries.BLOCK, REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID, REINFORCED_DEEPSLATE_CHARGED_BLOCK);
        Registry.register(Registries.BLOCK, REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID, REINFORCED_DEEPSLATE_BROKEN_BLOCK);
    }

    private void registerItems() {
        Registry.register(Registries.ITEM, LOCKED_ACTION_ITEM_ID, LOCKED_ACTION_ITEM);
        Registry.register(Registries.ITEM, ANCIENT_CITY_PORTAL_HINT_ITEM_ID, ANCIENT_CITY_PORTAL_HINT_ITEM);
        Registry.register(Registries.ITEM, REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID, new BlockItem(REINFORCED_DEEPSLATE_CHARGED_BLOCK, new QuiltItemSettings()));
        Registry.register(Registries.ITEM, REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID, new BlockItem(REINFORCED_DEEPSLATE_BROKEN_BLOCK, new QuiltItemSettings()));
    }

    private void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, ANCIENT_CITY_PORTAL_PARTICLES_ID, ANCIENT_CITY_PORTAL_PARTICLES);
    }

    private void registerEvents() {
        Registry.register(Registries.GAME_EVENT, JUKEBOX_PLAY_EVENT_ID, JUKEBOX_PLAY);
        Registry.register(Registries.GAME_EVENT, JUKEBOX_STOP_PLAY_EVENT_ID, JUKEBOX_STOP_PLAY);
    }

    private void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_TAB_ENTITY_ID, ANCIENT_CITY_PORTAL_TAB);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_ADVANCEMENT_ENTITY_ID, ANCIENT_CITY_PORTAL_ADVANCEMENT);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_HINT_ENTITY_ID, ANCIENT_CITY_PORTAL_HINT);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_EXPERIENCE_ORB_ENTITY_ID, ANCIENT_CITY_PORTAL_EXPERIENCE_ORB);
    }
}