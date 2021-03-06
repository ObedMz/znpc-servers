package ak.znetwork.znpcservers.commands.list;

import ak.znetwork.znpcservers.ServersNPC;
import ak.znetwork.znpcservers.commands.ZNCommand;
import ak.znetwork.znpcservers.commands.exception.CommandExecuteException;
import ak.znetwork.znpcservers.configuration.enums.ZNConfigValue;
import ak.znetwork.znpcservers.configuration.enums.type.ZNConfigType;
import ak.znetwork.znpcservers.manager.ConfigManager;
import ak.znetwork.znpcservers.npc.ZNPC;
import ak.znetwork.znpcservers.npc.enums.ItemSlot;
import ak.znetwork.znpcservers.npc.enums.ToggleFunction;
import ak.znetwork.znpcservers.npc.enums.TypeZNPC;
import ak.znetwork.znpcservers.npc.model.ZNPCAction;
import ak.znetwork.znpcservers.types.ConfigTypes;
import ak.znetwork.znpcservers.user.ZNPCUser;
import ak.znetwork.znpcservers.npc.skin.ZNPCSkin;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import org.bukkit.ChatColor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static ak.znetwork.znpcservers.npc.path.ZNPCPathImpl.AbstractTypeWriter.*;

/**
 * <p>Copyright (c) ZNetwork, 2020.</p>
 *
 * @author ZNetwork
 * @since 07/02/2020
 */
public class DefaultCommand extends ZNCommand {

    /**
     * A string whitespace.
     */
    private static final String WHITESPACE = " ";

    private static final Splitter SPACE_SPLITTER = Splitter.on(WHITESPACE);
    private static final Joiner SPACE_JOINER = Joiner.on(WHITESPACE);

    /**
     * Creates a new command.
     */
    public DefaultCommand(String command) {
        super(command);
    }

    @ZNCommandSub(aliases = {}, name = "", permission = "")
    public void defaultCommand(ZNCommandSender sender, Map<String, String> args) {
        sender.sendMessage("&6&m------------------------------------------");
        sender.sendMessage("&a&lZNPCS ZNETWORK &8(&6https://www.spigotmc.org/resources/znpcs-1-8-1-16-bungeecord-serversnpcs-open-source.80940/&8)");
        getCommands().forEach(sender::sendMessage);
        sender.sendMessage("&6&m------------------------------------------");
    }

    @ZNCommandSub(
            aliases = {"-id", "-type", "-name"},
            name = "create",
            permission = "znpcs.cmd.create",
            help = {
                    " &f&l* &e/znpcs create -id 1 -type PLAYER -name Qentin"
            }
    )
    public void createNPC(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 3) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        boolean foundNPC = ConfigTypes.NPC_LIST.stream().anyMatch(npc -> npc.getId() == id);

        if (foundNPC) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_FOUND);
            return;
        }

        String name = args.get("name").trim();

        if (name.length() < 3 || name.length() > 16) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NAME_LENGTH);
            return;
        }

        String type = args.get("type").toUpperCase();

        try {
            ServersNPC.createNPC(id,
                    TypeZNPC.valueOf(type),
                    sender.getPlayer().getLocation(),
                    name
            );

            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } catch (Exception e) {
            throw new CommandExecuteException("An error occurred while creating npc", e);
        }
    }

    @ZNCommandSub(
            aliases = {"-id"},
            name = "delete",
            permission = "znpcs.cmd.delete",
            help = {
                    " &f&l* &e/znpcs delete -id 1"
            }
    )
    public void deleteNPC(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 1) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        ServersNPC.deleteNPC(id);
        ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
    }

    @ZNCommandSub(
            aliases = {},
            name = "list",
            permission = "znpcs.cmd.list"
    )
    public void list(ZNCommandSender sender, Map<String, String> args) {
        if (ConfigTypes.NPC_LIST.isEmpty()) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NO_NPC_FOUND);
        } else {
            ConfigTypes.NPC_LIST.forEach(npc -> sender.sendMessage("&f&l * &a" + npc.getId() + " " + npc.getHologramLines().toString() + " &7(&e" + npc.getLocation().getWorld() + " " + npc.getLocation().getX() + " " + npc.getLocation().getY() + " " + npc.getLocation().getZ() + "&7)"));
        }
    }

    @ZNCommandSub(
            aliases = {"-id", "-skin"},
            name = "skin",
            permission = "znpcs.cmd.skin",
            help = {
                    " &f&l* &e/znpcs skin -id 1 -skin Notch"
            }
    )
    public void setSkin(ZNCommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        foundNPC.changeSkin(ZNPCSkin.forName(args.get("skin")));
        ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
    }

    @ZNCommandSub(
            aliases = {"-id", "-slot"},
            name = "equip",
            permission = "znpcs.cmd.equip",
            help = {
                    " &f&l* &e/znpcs equip -id 1 -slot [HAND,OFFHAND,HELMET,CHESTPLATE,LEGGINGS,BOOTS]",
                    "&8(You need to have the item in your hand.)"
            }
    )
    public void equip(ZNCommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        // Update new equip for npc viewers
        foundNPC.updateEquipment(ItemSlot.valueOf(args.get("slot").toUpperCase()), sender.getPlayer().getInventory().getItemInMainHand());
        ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
    }

    @ZNCommandSub(
            aliases = {"-id", "-lines"},
            name = "lines",
            permission = "znpcs.cmd.lines",
            help = {
                    " &f&l* &e/znpcs lines -id 1 -lines First Second Third-Space"
            }
    )
    public void changeLines(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        try {
            // Update lines
            foundNPC.getNpcPojo().setHologramLines(Lists.reverse(SPACE_SPLITTER.splitToList(args.get("lines"))));
            foundNPC.getHologram().createHologram();
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } catch (Exception exception) {
            throw new CommandExecuteException("An error occurred while changing hologram for npc " + foundNPC.getNpcPojo().getId(), exception);
        }
    }

    @ZNCommandSub(
            aliases = {"-id"},
            name = "move",
            permission = "znpcs.cmd.move",
            help = {
                    " &f&l* &e/znpcs move -id 1"
            }
    )
    public void move(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 1) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        try {
            foundNPC.setLocation(sender.getPlayer().getLocation());
            foundNPC.setLastMove(System.currentTimeMillis()); // Fix

            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } catch (Exception exception) {
            throw new CommandExecuteException("An error occurred while moving npc", exception);
        }
    }

    @ZNCommandSub(
            aliases = {"-id", "-type"},
            name = "type",
            permission = "znpcs.cmd.type",
            help = {
                    " &f&l* &e/znpcs type -id 1 -type ZOMBIE"
            }
    )
    public void changeType(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        TypeZNPC npcType = TypeZNPC.valueOf(args.get("type").toUpperCase());

        if (npcType != TypeZNPC.PLAYER && npcType.getConstructor() == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.UNSUPPORTED_ENTITY);
            return;
        }

        try {
            foundNPC.changeType(npcType);
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } catch (Exception e) {
            throw new CommandExecuteException("An error occurred while changing npc type", e);
        }
    }

    @ZNCommandSub(
            aliases = {"-id", "-add", "-remove", "-cooldown", "-list"},
            name = "action",
            permission = "znpcs.cmd.action",
            help = {
                    " &f&l* &e/znpcs action -id 1 -add SERVER skywars",
                    " &f&l* &e/znpcs action -id 1 -add CMD spawn"
            }
    )
    public void action(ZNCommandSender sender, Map<String, String> args) {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        final List<ZNPCAction> actions = foundNPC.getNpcPojo().getClickActions();
        if (args.containsKey("add")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("add"));

            if (split.size() < 2) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE_ACTION_ADD);
                return;
            }

            actions.add(new ZNPCAction(split.get(0).toUpperCase(), SPACE_JOINER.join(Iterables.skip(split, 1))));
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } else if (args.containsKey("remove")) {
            Integer actionId = Ints.tryParse(args.get("remove"));

            if (actionId == null) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            } else {
                if (actionId >= actions.size()) {
                    ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NO_ACTION_FOUND);
                    return;
                }
                actions.remove(actions.get(actionId));
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
            }
        } else if (args.containsKey("cooldown")) {
            List<String> split = SPACE_SPLITTER.splitToList(args.get("cooldown"));

            if (split.size() < 2) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE_ACTION_DELAY);
                return;
            }

            Integer actionId = Ints.tryParse(split.get(0));
            Integer actionDelay = Ints.tryParse(split.get(1));

            if (actionId == null || actionDelay == null) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            } else {
                if (actionId >= actions.size()) {
                    ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NO_ACTION_FOUND);
                    return;
                }
                actions.get(actionId).setDelay(actionDelay);
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
            }
        } else if (actions.isEmpty()) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NO_ACTION_FOUND);
        } else {
            actions.forEach(s -> sender.sendMessage("&8(&a" + actions.indexOf(s) + "&8) &6" + s));
        }
    }

    @ZNCommandSub(
            aliases = {"-id", "-type", "-value"},
            name = "toggle",
            permission = "znpcs.cmd.toggle",
            help = {
                    " &f&l* &e/znpcs toggle -id 1 -type look"
            }
    )
    public void toggle(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        ToggleFunction npcToggle = ToggleFunction.valueOf(args.get("type").toUpperCase());

        try {
            npcToggle.toggle(foundNPC, args.get("value"));
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } catch (Exception exception) {
            throw new CommandExecuteException("An error occurred while changing toggle command", exception);
        }
    }

    @ZNCommandSub(
            aliases = {"-id", "-customize"},
            name = "customize",
            permission = "znpcs.cmd.customize"
    )
    public void customize(ZNCommandSender sender, Map<String, String> args) throws Exception {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        TypeZNPC npcType = foundNPC.getNpcPojo().getNpcType();

        List<String> customizeOptions = SPACE_SPLITTER.splitToList(args.get("customize"));
        String methodName = customizeOptions.get(0);

        if (npcType.getCustomizationProcessor().contains(methodName)) {
            Method method = npcType.getCustomizationProcessor().getMethods().get(methodName);
            Iterable<String> split = Iterables.skip(customizeOptions, 1);
            if (Iterables.size(split) < method.getParameterTypes().length) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.TOO_FEW_ARGUMENTS);
                return;
            }

            try {
                String[] values = Iterables.toArray(split, String.class);

                npcType.updateCustomization(foundNPC, methodName, values);
                foundNPC.getNpcPojo().getCustomizationMap().put(methodName, values);

                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new CommandExecuteException("An error occurred while customizing npc", exception);
            }
        } else {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.METHOD_NOT_FOUND);
            for (Map.Entry<String, Method> method : npcType.getCustomizationProcessor().getMethods().entrySet()) {
                sender.sendMessage(ChatColor.YELLOW + method.getKey() + " " + SPACE_JOINER.join(method.getValue().getParameterTypes()));
            }
        }
    }

    @ZNCommandSub(
            aliases = {"-set", "-create", "-exit", "-id", "-path", "-list"},
            name = "path",
            permission = "znpcs.cmd.path"
    )
    public void path(ZNCommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        ZNPCUser znpcUser = ZNPCUser.registerOrGet(sender.getPlayer());
        if (znpcUser == null) {
            return;
        }

        if (args.containsKey("set")) {
            if (args.size() < 2) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE_PATH_SET);
                return;
            }

            Integer id = Ints.tryParse(args.get("set"));

            if (id == null) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
                return;
            }

            ZNPC foundNPC = ZNPC.find(id);

            if (foundNPC == null) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
                return;
            }

            foundNPC.setPath(AbstractTypeWriter.find(args.get("path")));
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
        } else if (args.containsKey("create")) {
            String pathName = args.get("create");

            if (pathName.length() < 3 || pathName.length() > 16) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NAME_LENGTH);
                return;
            }

            if (AbstractTypeWriter.find(pathName) != null) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.PATH_FOUND);
                return;
            }

            if (znpcUser.isHasPath()) {
                sender.getPlayer().sendMessage(ChatColor.RED + "You already have a path creator active, to remove it use /znpcs path -exit.");
                return;
            }

            AbstractTypeWriter.forCreation(pathName, znpcUser, TypeWriter.MOVEMENT);
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.START_PATH);
        } else if (args.containsKey("exit")) {
            znpcUser.setHasPath(false);
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.EXIT_PATH);;
        } else if (args.containsKey("list")) {
            if (AbstractTypeWriter.getPaths().isEmpty()) {
                ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NO_PATH_FOUND);
            } else {
                AbstractTypeWriter.getPaths().forEach(path -> sender.getPlayer().sendMessage(ChatColor.GREEN + path.getName()));
            }
        }
    }

    @ZNCommandSub(
            aliases = {"-id"},
            name = "teleport",
            permission = "znpcs.cmd.teleport"
    )
    public void teleport(ZNCommandSender sender, Map<String, String> args) {
        if (args.size() < 1) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        // Teleport player to npc location
        sender.getPlayer().teleport(foundNPC.getLocation());
    }

    @ZNCommandSub(
            aliases = {"-id", "-height"},
            name = "height",
            permission = "znpcs.cmd.height"
    )
    public void changeHologramHeight(ZNCommandSender sender, Map<String, String> args) throws CommandExecuteException {
        if (args.size() < 2) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INCORRECT_USAGE);
            return;
        }

        Integer id = Ints.tryParse(args.get("id"));

        if (id == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        ZNPC foundNPC = ZNPC.find(id);

        if (foundNPC == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.NPC_NOT_FOUND);
            return;
        }

        Double givenHeight = Doubles.tryParse(args.get("height"));

        if (givenHeight == null) {
            ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.INVALID_NUMBER);
            return;
        }

        foundNPC.getNpcPojo().setHologramHeight(givenHeight);
        foundNPC.getHologram().createHologram(); // Update hologram

        ConfigManager.getByType(ZNConfigType.MESSAGES).sendMessage(sender.getCommandSender(), ZNConfigValue.SUCCESS);
    }
}
