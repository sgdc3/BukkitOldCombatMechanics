package gvlfm78.plugin.oldcombatmechanics.utils.reflection.type;

public enum PacketType {

    PlayOut("PlayOut"), PlayIn("PlayIn");

    public String prefix;

    PacketType(String prefix) {
        this.prefix = prefix;
    }
}