package tech.razikus.headlesshaven;

import java.util.Objects;

public class BuddyState {
    private Integer id;
    private String name;
    private Integer R;
    private Integer G;
    private Integer B;

    public BuddyState(Integer id, String name, Integer r, Integer g, Integer b) {
        this.id = id;
        this.name = name;
        R = r;
        G = g;
        B = b;
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

    public Integer getR() {
        return R;
    }

    public void setR(Integer r) {
        R = r;
    }

    public Integer getG() {
        return G;
    }

    public void setG(Integer g) {
        G = g;
    }

    public Integer getB() {
        return B;
    }

    public void setB(Integer b) {
        B = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuddyState that = (BuddyState) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(R, that.R) && Objects.equals(G, that.G) && Objects.equals(B, that.B);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, R, G, B);
    }

    @Override
    public String toString() {
        return "BuddyState{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", R=" + R +
                ", G=" + G +
                ", B=" + B +
                '}';
    }
}
