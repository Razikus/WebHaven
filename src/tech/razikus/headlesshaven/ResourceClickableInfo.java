package tech.razikus.headlesshaven;

import haven.Coord;
import haven.Coord2d;

import java.util.ArrayList;
import java.util.List;

public class ResourceClickableInfo {
    private final List<ObstaclePolygon> obstacles = new ArrayList<>();
    private Coord cc = null;  // click center
    private Coord[][] clickPoints = null;
    private final String resourceName;

    public ResourceClickableInfo(String resourceName) {
        this.resourceName = resourceName;
    }


    public void setClickCenter(Coord cc) {
        this.cc = cc;
    }


    public void setClickPoints(Coord[][] points) {
        this.clickPoints = points;
    }

    public Coord[] getClickPoints(int epid) {
        if (epid < 0 || epid >= 8) return new Coord[0];
        return clickPoints[epid] != null ? clickPoints[epid] : new Coord[0];
    }

    public Coord getClickCenter() {
        return cc;
    }

    public static class ObstaclePolygon {
        public final List<Coord2d> points;

        public ObstaclePolygon(List<Coord2d> points) {
            this.points = points;
        }

        @Override
        public String toString() {
            return "ObstaclePolygon{" +
                    "points=" + points +
                    '}';
        }
    }

    public void addObstacle(List<Coord2d> points) {
        obstacles.add(new ObstaclePolygon(points));
    }

    public String prettyClickPoints() {
        if(this.clickPoints == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clickPoints.length; i++) {
            sb.append("epid: ").append(i).append(" ");
            if (clickPoints[i] != null) {
                for (Coord c : clickPoints[i]) {
                    sb.append(c.x).append(" ").append(c.y);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ResourceClickableInfo{" +
                "obstacles=" + obstacles +
                ", cc=" + cc +
                ", clickPoints=" + prettyClickPoints() +
                ", resourceName='" + resourceName + '\'' +
                '}';
    }
}