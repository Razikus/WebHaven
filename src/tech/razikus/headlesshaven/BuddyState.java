package tech.razikus.headlesshaven;

import java.util.Objects;

public class BuddyState {
    private Integer id;
    private String name;
    private Integer online; // 1 - online, 0 - offline, -1 - memorized/unkinned
    private Integer group;
    private Integer seen;


    public static final int[] GROUP_COLORS = new int[] {
            0xFFFFFF,
            0x00FF00,
            0xFF0000,
            0x0000FF,
            0x00FFFF,
            0xFFFF00,
            0xFF00FF,
            0xFF0080,
    };

    public BuddyState(Integer id, String name, Integer online, Integer group, Integer seen) {
        this.id = id;
        this.name = name;
        this.online = online;
        this.group = group;
        this.seen = seen;
    }

    public boolean isOnline() {
        return online != null && online == 1;
    }

    public boolean isKinned() {
        return online != null && online >= 0;
    }

    public boolean wasSeen() { // ability to do describe
        return seen != null && seen == 1;
    }


    public int getGroupColor() {
        if (group != null && group >= 0 && group < GROUP_COLORS.length) {
            return GROUP_COLORS[group];
        }
        return 0xFFFFFF;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public Integer getSeen() {
        return seen;
    }

    public void setSeen(Integer seen) {
        this.seen = seen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuddyState that = (BuddyState) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(online, that.online) && Objects.equals(group, that.group) && Objects.equals(seen, that.seen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, online, group, seen);
    }
}
