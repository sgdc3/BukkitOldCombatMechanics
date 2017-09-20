package gvlfm78.plugin.oldcombatmechanics.utils.reflection;

import gvlfm78.plugin.oldcombatmechanics.utils.reflection.type.ClassType;
import gvlfm78.plugin.oldcombatmechanics.utils.reflection.type.HandleType;
import gvlfm78.plugin.oldcombatmechanics.utils.reflection.type.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflector {

    private static String VERSION;
    private static Class<?> CRAFT_PLAYER;
    private static Class<?> CRAFT_ENTITY;
    private static Class<?> CRAFT_SERVER;
    private static Class<?> CRAFT_WORLD;
    private static Class<?> CRAFT_ITEM_STACK;
    private static Method PLAYER_HANDLE;
    private static Method ENTITY_HANDLE;
    private static Method SERVER_HANDLE;
    private static Method WORLD_HANDLE;

    static {
        try {
            VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];

            CRAFT_PLAYER = getClass(ClassType.CRAFTBUKKIT, "entity.CraftPlayer");
            CRAFT_ENTITY = getClass(ClassType.CRAFTBUKKIT, "entity.CraftEntity");
            CRAFT_SERVER = getClass(ClassType.CRAFTBUKKIT, "CraftServer");
            CRAFT_WORLD = getClass(ClassType.CRAFTBUKKIT, "CraftWorld");
            CRAFT_ITEM_STACK = getClass(ClassType.CRAFTBUKKIT, "inventory.CraftItemStack");

            PLAYER_HANDLE = getMethod(CRAFT_PLAYER, "getHandle");
            ENTITY_HANDLE = getMethod(CRAFT_ENTITY, "getHandle");
            SERVER_HANDLE = getMethod(CRAFT_SERVER, "getHandle");
            WORLD_HANDLE = getMethod(CRAFT_WORLD, "getHandle");
        } catch (Exception e) {
            System.err.println("Failed to load Reflector");
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    public static Class<?> getClass(ClassType type, String name) {
        try {
            return Class.forName(type.getPackage() + "." + VERSION + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equalsIgnoreCase(name)) {
                if (method.getParameterTypes() == params) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) throws Exception {
        return clazz.getDeclaredField(fieldName);
    }

    public static Field getInaccessibleField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = getField(object.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    public static void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = getField(object.getClass(), fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    public static Constructor<?> getConstructor(Class<?> clazz, int numParams) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == numParams) {
                return constructor;
            }
        }
        return null;
    }

    public static Object getHandle(HandleType type, Object object) {
        try {
            switch (type) {
                case ENTITY:
                    return ENTITY_HANDLE.invoke(object);
                case PLAYER:
                    return PLAYER_HANDLE.invoke(object);
                case SERVER:
                    return SERVER_HANDLE.invoke(object);
                case WORLD:
                    return WORLD_HANDLE.invoke(object);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getNMSItem(ItemStack item) {
        try {
            return getFieldValue(item, "handle");
        } catch (Exception e) {
            return null;
        }
    }

    public static class Packets {
        public static Object getPlayerConnection(Player p) {
            try {
                return getFieldValue(p, "playerConnection");
            } catch (Exception e) {
                return null;
            }
        }

        public static Class<?> getPacket(PacketType type, String name) {
            return Reflector.getClass(ClassType.NMS, "Packet" + type.prefix + name);
        }

        public static void sendPacket(Player player, Object packet) {
            try {
                Object nmsPlayer = getHandle(HandleType.PLAYER, player);
                Field con_field = nmsPlayer.getClass().getField("playerConnection");
                Object con = con_field.get(nmsPlayer);
                Method packet_method = getMethod(con.getClass(), "sendPacket");
                packet_method.invoke(con, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}