package gvlfm78.plugin.oldcombatmechanics.utils.reflection;

import gvlfm78.plugin.oldcombatmechanics.utils.reflection.type.PacketType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.UUID;

/**
 * From <a href="https://www.spigotmc.org/resources/1-9-anti-collision.28770/">1.9 anti-collision plugin by Mentrixx</a>
 * Modified by gvlfm78 to work with Rayzr's Reflector utility
 * <p>
 * I've tried to contact the author but didn't get an answer after months,
 * and as the plugin has no license decided to take some of the code
 */
public class TeamPacketUtils {
    private static Class<?> packetTeamClass;
    private static Field nameField;
    private static Field modeField;
    private static Field collisionRuleField;
    private static Field playersField;

    static {
        try {
            packetTeamClass = Reflector.Packets.getPacket(PacketType.PlayOut, "ScoreboardTeam");
            nameField = Reflector.getInaccessibleField(packetTeamClass, "a");
            modeField = Reflector.getInaccessibleField(packetTeamClass, "i");
            collisionRuleField = Reflector.getInaccessibleField(packetTeamClass, "f");
            playersField = Reflector.getInaccessibleField(packetTeamClass, "h");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String sendNewTeamPacket(Player player) {
        String teamName = UUID.randomUUID().toString().substring(0, 15);
        try {
            Object packetTeamObject = packetTeamClass.newInstance();

            nameField.set(packetTeamObject, teamName);
            modeField.set(packetTeamObject, 0);
            playersField.set(packetTeamObject, Collections.singletonList(player.getName()));
            changePacketCollisionType(packetTeamObject);

            Reflector.Packets.sendPacket(player, packetTeamObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teamName;
    }

    public static String sendRemoveTeamPacket(Player player, String teamName) {
        try {
            Object packetTeamObject = packetTeamClass.newInstance();

            nameField.set(packetTeamObject, teamName);
            modeField.set(packetTeamObject, 1);

            Reflector.Packets.sendPacket(player, packetTeamObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teamName;
    }

    public static void changePacketCollisionType(Object packetTeamObject) throws Exception {
        collisionRuleField.set(packetTeamObject, "never");
    }
}