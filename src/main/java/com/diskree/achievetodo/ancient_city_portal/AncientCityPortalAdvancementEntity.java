package com.diskree.achievetodo.ancient_city_portal;

import com.diskree.achievetodo.AchieveToDoMod;
import com.diskree.achievetodo.JukeboxBlockEntityImpl;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;

public class AncientCityPortalAdvancementEntity extends DisplayEntity.ItemDisplayEntity {

    private static final int PORTAL_WIDTH = 21;
    private static final int PORTAL_HEIGHT = 7;

    @Nullable
    private BlockPos jukeboxPos;
    private final EntityGameEventHandler<JukeboxEventListener> jukeboxEventHandler;
    private boolean isHalfOfPortalActivationSkipped;

    public AncientCityPortalAdvancementEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.jukeboxEventHandler = new EntityGameEventHandler<>(new JukeboxEventListener(new EntityPositionSource(AncientCityPortalAdvancementEntity.this, 0), 20));
    }

    @Override
    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
        if (getWorld() instanceof ServerWorld serverWorld) {
            callback.accept(this.jukeboxEventHandler, serverWorld);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (jukeboxPos != null) {
            nbt.put("JukeboxPos", NbtHelper.fromBlockPos(jukeboxPos));
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("JukeboxPos", NbtElement.COMPOUND_TYPE)) {
            jukeboxPos = NbtHelper.toBlockPos(nbt.getCompound("JukeboxPos"));
        }
    }

    private void updateJukeboxPos(BlockPos jukeboxPos, boolean playing) {
        boolean isAnotherJukebox = this.jukeboxPos != null && !this.jukeboxPos.equals(jukeboxPos) && isPortalActivationInProgress();
        if (isAnotherJukebox || isPortalActivated()) {
            if (isAnotherJukebox || canStopJukeboxAfterPortalActivation(jukeboxPos)) {
                stopJukebox(jukeboxPos);
            }
            return;
        }
        if (playing && checkPlayer() && checkDisk(jukeboxPos) && checkPortalFrame()) {
            if (!isPortalActivationInProgress()) {
                this.jukeboxPos = jukeboxPos;
            }
            ArrayList<BlockPos> portalAirBlocks = getPortalAirBlocks();
            if (!isHalfOfPortalActivationSkipped && portalAirBlocks.size() == 60) {
                isHalfOfPortalActivationSkipped = true;
                return;
            }
            Collections.shuffle(portalAirBlocks);
            for (int i = 0; i < (portalAirBlocks.size() % 10 == 0 ? 4 : 3); i++) {
                getWorld().setBlockState(portalAirBlocks.get(0), AchieveToDoMod.ANCIENT_CITY_PORTAL_BLOCK.getDefaultState().with(AncientCityPortalBlock.AXIS, getHorizontalFacing().rotateYClockwise().getAxis()));
                ((AncientCityPortalBlock) getWorld().getBlockState(portalAirBlocks.get(0)).getBlock()).hideParticles();
                portalAirBlocks.remove(0);
                if (isPortalActivated()) {
                    for (BlockPos pos : getPortalBlocks(false)) {
                        if (isPortal(pos)) {
                            ((AncientCityPortalBlock) getWorld().getBlockState(pos).getBlock()).impulseParticles();
                        }
                    }
                    break;
                }
            }
        } else {
            stopJukebox(jukeboxPos);
            this.jukeboxPos = null;
            for (BlockPos pos : getPortalBlocks(false)) {
                if (isPortal(pos)) {
                    getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    private boolean checkPlayer() {
        PlayerEntity player = AchieveToDoMod.getPlayer();
        return player != null && player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    private boolean checkDisk(BlockPos jukeboxPos) {
        if (jukeboxPos == null) {
            return false;
        }
        World world = getWorld();
        if (world == null) {
            return false;
        }
        BlockEntity blockEntity = world.getBlockEntity(jukeboxPos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) {
            return false;
        }
        return jukebox.getStack() != null && Items.MUSIC_DISC_5.equals(jukebox.getStack().getItem());
    }

    private boolean checkPortalFrame() {
        for (BlockPos pos : getPortalBlocks(false)) {
            if (!isAir(pos) && !isPortal(pos)) {
                return false;
            }
        }
        for (BlockPos pos : getPortalBlocks(true)) {
            if (!isReinforcedDeepslate(pos)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPortalActivated() {
        for (BlockPos pos : getPortalBlocks(false)) {
            if (!isPortal(pos)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPortalActivationInProgress() {
        if (isPortalActivated()) {
            return false;
        }
        for (BlockPos pos : getPortalBlocks(false)) {
            if (isPortal(pos)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<BlockPos> getPortalAirBlocks() {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (BlockPos pos : getPortalBlocks(false)) {
            if (isAir(pos)) {
                blocks.add(pos);
            }
        }
        return blocks;
    }

    private boolean canStopJukeboxAfterPortalActivation(BlockPos jukeboxPos) {
        BlockEntity blockEntity = getWorld().getBlockEntity(jukeboxPos);
        if (blockEntity instanceof JukeboxBlockEntityImpl jukebox) {
            return jukebox.isDiskRelaxPartFinished() || jukebox.isDiskStartedJustNow();
        }
        return true;
    }

    private void stopJukebox(BlockPos jukeboxPos) {
        if (!checkDisk(jukeboxPos)) {
            return;
        }
        BlockEntity blockEntity = getWorld().getBlockEntity(jukeboxPos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            jukebox.dropRecord();
        }
    }

    private boolean isReinforcedDeepslate(BlockPos pos) {
        World world = getWorld();
        return world != null && world.getBlockState(pos).isOf(Blocks.REINFORCED_DEEPSLATE);
    }

    private boolean isAir(BlockPos pos) {
        World world = getWorld();
        return world != null && world.getBlockState(pos).isAir();
    }

    private boolean isPortal(BlockPos pos) {
        World world = getWorld();
        return world != null && world.getBlockState(pos).isOf(AchieveToDoMod.ANCIENT_CITY_PORTAL_BLOCK);
    }

    private BlockPos getPortalLeftTopCornerPos() {
        return getBlockPos().up(3).offset(getHorizontalFacing().rotateYCounterclockwise(), 11);
    }

    private ArrayList<BlockPos> getPortalBlocks(boolean perimeter) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = 0; x <= PORTAL_WIDTH; x++) {
            for (int y = 0; y <= PORTAL_HEIGHT; y++) {
                if (x == 0 && y == 0 || x == 0 && y == PORTAL_HEIGHT || x == PORTAL_WIDTH && y == 0 || x == PORTAL_WIDTH && y == PORTAL_HEIGHT) {
                    continue;
                }
                BlockPos pos = getPortalLeftTopCornerPos().offset(getHorizontalFacing().rotateYClockwise(), x).down(y);
                if (x == 0 || y == 0 || x == PORTAL_WIDTH || y == PORTAL_HEIGHT) {
                    if (perimeter) {
                        blocks.add(pos);
                    }
                } else {
                    if (!perimeter) {
                        blocks.add(pos);
                    }
                }
            }
        }
        return blocks;
    }

    class JukeboxEventListener implements GameEventListener {
        private final PositionSource positionSource;
        private final int range;

        public JukeboxEventListener(PositionSource positionSource, int range) {
            this.positionSource = positionSource;
            this.range = range;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getRange() {
            return this.range;
        }

        @Override
        public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            if (event == GameEvent.JUKEBOX_PLAY) {
                AncientCityPortalAdvancementEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), true);
                return true;
            }
            if (event == GameEvent.JUKEBOX_STOP_PLAY) {
                AncientCityPortalAdvancementEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), false);
                return true;
            }
            return false;
        }
    }
}
