package com.github.manuel3;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VeinMiningSystem  extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final int oddNumberDiameter = 5;
    private final int initialOffset;

    protected VeinMiningSystem() {
        super(BreakBlockEvent.class);
        initialOffset = (oddNumberDiameter-1)/2;
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        ItemStack item = event.getItemInHand();
        String id = event.getBlockType().getId();
        if (id.equals("Empty")) return;
        if (!id.startsWith("Ore_")) return;
        Vector3i brokenPosition = event.getTargetBlock();

        World world = player.getWorld();
        if (world == null) return;
        Vector3i startPosition = new Vector3i(brokenPosition.getX()-initialOffset, brokenPosition.getY()-initialOffset, brokenPosition.getZ()-initialOffset);

        for (int x = 0; x < oddNumberDiameter; x++) {
            for (int y = 0; y < oddNumberDiameter; y++) {
                for (int z = 0; z < oddNumberDiameter; z++) {
                    BlockType type = world.getBlockType(startPosition.getX()+x, startPosition.getY()+y, startPosition.getZ()+z);
                    if (type == null) continue;
                    if (type.getId().equals(id)) {
                        Vector3i pos = new Vector3i(startPosition.getX()+x, startPosition.getY()+y, startPosition.getZ()+z);
                        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(pos.getX(), pos.getZ()));
                        if (chunkRef == null) continue;
                        BlockHarvestUtils.performBlockDamage(pos, item, null, Float.MAX_VALUE, 0, chunkRef, commandBuffer, world.getChunkStore().getStore());
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
