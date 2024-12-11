package tech.razikus.headlesshaven;

import haven.Coord;
import haven.Coord2d;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.awt.Color;

import static haven.MCache.tilesz;

public class MapRenderer {
    private final SimpleMapCache mapCache;
    private final Map<Integer, Color> tileColors = new HashMap<>();
    private static final int TILE_SIZE = 10; // pixels per tile

    public MapRenderer(SimpleMapCache mapCache) {
        this.mapCache = mapCache;
        initializeDefaultColors();
    }

    private void initializeDefaultColors() {
        // Create a range of distinct colors using golden ratio
        float goldenRatio = 0.618033988749895f;
        float hue = 0;

        for (int i = 0; i < 256; i++) {
            hue = (hue + goldenRatio) % 1.0f;

            float saturation, brightness;

            switch (i % 4) {
                case 0: // Vibrant colors
                    saturation = 0.9f;
                    brightness = 0.9f;
                    break;
                case 1: // Softer colors
                    saturation = 0.7f;
                    brightness = 0.95f;
                    break;
                case 2: // Deep colors
                    saturation = 0.85f;
                    brightness = 0.8f;
                    break;
                default: // Lighter colors
                    saturation = 0.6f;
                    brightness = 1.0f;
                    break;
            }

            Color color = Color.getHSBColor(hue, saturation, brightness);

            if (isColorTooDark(color) || isColorTooLight(color)) {
                color = Color.getHSBColor(hue, saturation, 0.75f);
            }

            tileColors.put(i, color);
        }
    }

    private boolean isColorTooDark(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0 < 30;
    }

    private boolean isColorTooLight(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0 > 225;
    }

    public void renderAll(String filename, PseudoObject player) throws IOException {

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Coord coord : mapCache.getGrids().keySet()) {
            minX = Math.min(minX, coord.x);
            minY = Math.min(minY, coord.y);
            maxX = Math.max(maxX, coord.x);
            maxY = Math.max(maxY, coord.y);
        }

        // Add padding
        minX -= 1;
        minY -= 1;
        maxX += 1;
        maxY += 1;

        // Add to renderToFile call
        renderToFile(filename, new Coord(minX, minY), new Coord(maxX, maxY), player);
    }

    public void renderToFile(String filename, Coord topLeft, Coord bottomRight, PseudoObject player) throws IOException {
        StringBuilder svg = new StringBuilder();

        // Calculate dimensions
        int width = (bottomRight.x - topLeft.x + 1) * SimpleMapCache.cmaps.x * TILE_SIZE;
        int height = (bottomRight.y - topLeft.y + 1) * SimpleMapCache.cmaps.y * TILE_SIZE;

        // Start SVG with viewBox for better scaling
        svg.append(String.format("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg width="%d" height="%d" viewBox="0 0 %d %d" xmlns="http://www.w3.org/2000/svg">
            <defs>
                <pattern id="grid" width="%d" height="%d" patternUnits="userSpaceOnUse">
                    <path d="M %d 0 L 0 0 0 %d" fill="none" stroke="#ccc" stroke-width="0.5"/>
                </pattern>
            </defs>
            <rect width="100%%" height="100%%" fill="#f0f0f0"/>
            <rect width="100%%" height="100%%" fill="url(#grid)"/>
            """, width, height, width, height, TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE));

        // Add title and description
        svg.append(String.format("""
            <title>Map Visualization (%d,%d) to (%d,%d)</title>
            <desc>Generated map visualization showing tiles and elevation</desc>
            """, topLeft.x, topLeft.y, bottomRight.x, bottomRight.y));

        // Draw each grid
        for (int gridY = topLeft.y; gridY <= bottomRight.y; gridY++) {
            for (int gridX = topLeft.x; gridX <= bottomRight.x; gridX++) {
                Coord gridCoord = new Coord(gridX, gridY);
                SimpleMapCache.GridData grid = mapCache.getGrids().get(gridCoord);

                if (grid != null) {
                    renderGrid(svg, grid, gridCoord, topLeft);
                }
            }
        }

        // Add grid coordinates
        renderGridCoordinates(svg, topLeft, bottomRight);

        // Add legend
        renderLegend(svg, width);

        // Add map statistics
        renderStatistics(svg, width, height);

        if(player != null) {
            renderPlayer(svg, player, topLeft);
        }

        // Close SVG
        svg.append("</svg>");

        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(svg.toString());
        }
    }

    public void renderPlayer(StringBuilder svg, PseudoObject player, Coord topLeft) {
        Coord2d playerPos = player.getCoordinate();
        int baseX = (int)((playerPos.x / (SimpleMapCache.cmaps.x * tilesz.x) - topLeft.x) * SimpleMapCache.cmaps.x * TILE_SIZE);
        int baseY = (int)((playerPos.y / (SimpleMapCache.cmaps.y * tilesz.y) - topLeft.y) * SimpleMapCache.cmaps.y * TILE_SIZE);

        svg.append(String.format("""
        <circle cx="%d" cy="%d" r="5" fill="red" stroke="white" stroke-width="2">
            <title>Player Position: (%.1f, %.1f)</title>
        </circle>
        """,
                baseX, baseY, playerPos.x, playerPos.y
        ));
    }

    private void renderGrid(StringBuilder svg, SimpleMapCache.GridData grid, Coord gridCoord, Coord topLeft) {
        int baseX = (gridCoord.x - topLeft.x) * SimpleMapCache.cmaps.x * TILE_SIZE;
        int baseY = (gridCoord.y - topLeft.y) * SimpleMapCache.cmaps.y * TILE_SIZE;

        // Group for this grid
        svg.append(String.format("""
            <g id="grid_%d_%d" transform="translate(%d,%d)">
            """, gridCoord.x, gridCoord.y, baseX, baseY));

        // Draw each tile in the grid
        for (int y = 0; y < SimpleMapCache.cmaps.y; y++) {
            for (int x = 0; x < SimpleMapCache.cmaps.x; x++) {
                int tileIndex = y * SimpleMapCache.cmaps.x + x;
                int tileId = grid.tiles[tileIndex];
                Color color = tileColors.get(tileId);

                if (color != null) {
                    int tileX = x * TILE_SIZE;
                    int tileY = y * TILE_SIZE;
                    float elevation = grid.z[tileIndex];

                    // Adjust brightness based on elevation
                    Color adjustedColor = adjustColorForElevation(color, elevation);

                    svg.append(String.format("""
                        <rect x="%d" y="%d" width="%d" height="%d" fill="%s">
                          <title>Grid: (%d,%d) Tile: (%d,%d) ID: %d Elevation: %.2f</title>
                        </rect>
                        """,
                            tileX, tileY, TILE_SIZE, TILE_SIZE,
                            colorToHex(adjustedColor),
                            gridCoord.x, gridCoord.y, x, y, tileId, elevation
                    ));
                }
            }
        }

        svg.append("</g>");
    }

    private void renderGridCoordinates(StringBuilder svg, Coord topLeft, Coord bottomRight) {
        svg.append("<g id=\"coordinates\" font-size=\"12\" fill=\"black\">");

        // Draw grid coordinates
        for (int gridY = topLeft.y; gridY <= bottomRight.y; gridY++) {
            for (int gridX = topLeft.x; gridX <= bottomRight.x; gridX++) {
                int x = (gridX - topLeft.x) * SimpleMapCache.cmaps.x * TILE_SIZE;
                int y = (gridY - topLeft.y) * SimpleMapCache.cmaps.y * TILE_SIZE;

                svg.append(String.format("""
                    <text x="%d" y="%d">(%d,%d)</text>
                    """,
                        x + 5, y + 15, gridX, gridY
                ));
            }
        }

        svg.append("</g>");
    }

    private void renderLegend(StringBuilder svg, int width) {
        int legendX = width - 160;
        int legendY = 10;
        Set<Integer> uniqueTiles = new HashSet<>();

        // Collect unique tile IDs
        mapCache.getGrids().values().forEach(grid -> {
            for (int tileId : grid.tiles) {
                uniqueTiles.add(tileId);
            }
        });

        // Draw legend background
        svg.append(String.format("""
            <g id="legend" transform="translate(%d,%d)">
            <rect width="150" height="%d" fill="white" stroke="black" rx="5"/>
            <text x="5" y="15" font-weight="bold">Tile Types</text>
            """, legendX, legendY, uniqueTiles.size() * 20 + 30));

        // Draw legend entries
        int y = 30;
        for (int tileId : uniqueTiles) {
            Color color = tileColors.get(tileId);
            if (color != null) {
                svg.append(String.format("""
                    <rect x="5" y="%d" width="15" height="15" fill="%s" stroke="black"/>
                    <text x="25" y="%d">Tile ID: %d</text>
                    """,
                        y, colorToHex(color),
                        y + 12, tileId
                ));
                y += 20;
            }
        }

        svg.append("</g>");
    }

    private void renderStatistics(StringBuilder svg, int width, int height) {
        // Calculate statistics
        int totalGrids = mapCache.getGrids().size();
        int totalTiles = totalGrids * SimpleMapCache.cmaps.x * SimpleMapCache.cmaps.y;
        Set<Integer> uniqueTileTypes = new HashSet<>();
        float minElevation = Float.MAX_VALUE;
        float maxElevation = Float.MIN_VALUE;

        for (SimpleMapCache.GridData grid : mapCache.getGrids().values()) {
            for (int tileId : grid.tiles) {
                uniqueTileTypes.add(tileId);
            }
            for (float elevation : grid.z) {
                minElevation = Math.min(minElevation, elevation);
                maxElevation = Math.max(maxElevation, elevation);
            }
        }

        // Render statistics box
        svg.append(String.format("""
            <g id="statistics" transform="translate(10,%d)">
            <rect width="200" height="100" fill="white" stroke="black" rx="5"/>
            <text x="10" y="20" font-weight="bold">Map Statistics</text>
            <text x="10" y="40">Total Grids: %d</text>
            <text x="10" y="60">Total Tiles: %d</text>
            <text x="10" y="80">Unique Tile Types: %d</text>
            <text x="10" y="100">Elevation Range: %.2f to %.2f</text>
            </g>
            """,
                height - 110,
                totalGrids,
                totalTiles,
                uniqueTileTypes.size(),
                minElevation,
                maxElevation
        ));
    }
    private Color adjustColorForElevation(Color baseColor, float elevation) {
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        // Scale elevation to a smaller range, e.g. -0.3 to +0.3
        float brightnessFactor = 1.0f + (elevation / 500.0f);
        float newBrightness = Math.max(0.2f, Math.min(0.95f, hsb[2] * brightnessFactor));
        return Color.getHSBColor(hsb[0], hsb[1], newBrightness);
    }


    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}