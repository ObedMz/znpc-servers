package ak.znetwork.znpcservers.npc.packets.list;

import ak.znetwork.znpcservers.npc.ZNPC;
import ak.znetwork.znpcservers.npc.enums.ItemSlot;
import ak.znetwork.znpcservers.npc.packets.PacketsImpl;
import ak.znetwork.znpcservers.types.ClassTypes;
import ak.znetwork.znpcservers.utility.ReflectionUtils;
import ak.znetwork.znpcservers.utility.Utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * <p>Copyright (c) ZNetwork, 2020.</p>
 *
 * v1.8 - 1.16
 *
 * @author ZNetwork
 * @since 07/02/2020
 */
public class PacketsV8 extends PacketsImpl.Default {

    private static final boolean V9 = Utils.versionNewer(9);

    private static final String TEAM_MODE = V9 ? "i" : "h";

    public PacketsV8(ZNPC znpc) {
        super(znpc);
    }

    @Override
    public void deleteScoreboard() throws ReflectiveOperationException {
        scoreboardDelete = ClassTypes.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.newInstance();
        ReflectionUtils.setValue(scoreboardDelete, "a", getNPC().getGameProfile().getName());
        ReflectionUtils.setValue(scoreboardDelete, TEAM_MODE, 1);
    }

    @Override
    public void spawnScoreboard() throws ReflectiveOperationException {
        scoreboardSpawn = ClassTypes.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.newInstance();
        ReflectionUtils.setValue(scoreboardSpawn, "a", getNPC().getGameProfile().getName());
        ReflectionUtils.setValue(scoreboardSpawn, "e", "never");
        ReflectionUtils.setValue(scoreboardSpawn, TEAM_MODE, 0);
        ReflectionUtils.setValue(scoreboardSpawn, V9 ? "h" : "g", Collections.singletonList(getNPC().getGameProfile().getName()));
        if (getNPC().getNpcPojo().isHasGlow() && V9) {
            updateGlowPacket(scoreboardSpawn);
        }
    }

    @Override
    public void spawnPlayerPacket(Object nmsWorld) throws ReflectiveOperationException {
        playerSpawnPacket = ClassTypes.PLAYER_CONSTRUCTOR_OLD.newInstance(ClassTypes.GET_SERVER_METHOD.invoke(Bukkit.getServer()), nmsWorld, getNPC().getGameProfile(), (Utils.versionNewer(14) ? ClassTypes.PLAYER_INTERACT_MANAGER_NEW_CONSTRUCTOR : ClassTypes.PLAYER_INTERACT_MANAGER_OLD_CONSTRUCTOR).newInstance(nmsWorld));
    }

    @Override
    public Object getClickType(Object interactPacket) throws ReflectiveOperationException {
        return ReflectionUtils.getValue(interactPacket, "action");
    }

    @Override
    public void getEquipPacket(ItemSlot itemSlot, ItemStack itemStack) throws ReflectiveOperationException {
        equipPackets.put(itemSlot.name(), ClassTypes.PACKET_PLAY_OUT_ENTITY_EQUIPMENT_CONSTRUCTOR_OLD.newInstance(getNPC().getEntityID(), itemSlot.getSlotOld(), ClassTypes.AS_NMS_COPY_METHOD.invoke(ClassTypes.CRAFT_ITEM_STACK_CLASS, itemStack)));
    }
}
