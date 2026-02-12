package tech.razikus.headlesshaven;

import haven.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;

public class DebugRender {
    private final Resource resource;
    private final String debugDir;
    private Collection<Resource.Layer> layers;

    @SuppressWarnings("unchecked")
    public DebugRender(Resource resource, String debugOutputDirectory) {
        this.resource = resource;
        this.debugDir = debugOutputDirectory;
        try {
            Field layersField = Resource.class.getDeclaredField("layers");
            layersField.setAccessible(true);
            this.layers = (Collection<Resource.Layer>) layersField.get(resource);
        } catch (Exception e) {
            System.err.println("Could not access layers field: " + e.getMessage());
            this.layers = new ArrayList<>();
        }
        createDebugDirectory();
    }

    private void createDebugDirectory() {
        try {
            Files.createDirectories(Paths.get(debugDir));
        } catch (Exception e) {
            System.err.println("Could not create debug directory: " + e.getMessage());
        }
    }

    public void dumpResourceInfo() {
        try {
            String filename = String.format("%s/%s_info.txt", debugDir, resource.name.replace('/', '_'));
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Resource Debug Information");
                writer.println("========================");
                writer.println("Name: " + resource.name);
                writer.println("Version: " + resource.ver);
                writer.println("Source: " + resource.source);
                writer.println("\nLayers Information:");
                writer.println("------------------");

                // Dump layer information
                for (Resource.Layer layer : layers) {
                    writer.println("\nLayer Type: " + layer.getClass().getSimpleName());

                    if (layer instanceof Resource.Image) {
                        Resource.Image img = (Resource.Image) layer;
                        writer.println("Image ID: " + img.id);
                        writer.println("Z-index: " + img.z);
                        writer.println("Sub Z-index: " + img.subz);
                        writer.println("Size: " + img.sz);
                        writer.println("Offset: " + img.o);
                        saveImage(img);
                    } else if (layer instanceof Resource.Tooltip) {
                        Resource.Tooltip tooltip = (Resource.Tooltip) layer;
                        writer.println("Tooltip text: " + tooltip.t);
                    } else if (layer instanceof Resource.Pagina) {
                        Resource.Pagina pagina = (Resource.Pagina) layer;
                        writer.println("Pagina text: " + pagina.text);
                    } else if (layer instanceof Resource.AButton) {
                        Resource.AButton btn = (Resource.AButton) layer;
                        writer.println("Button name: " + btn.name);
                        writer.println("Hotkey: " + btn.hk);
                        writer.println("Action data: " + Arrays.toString(btn.ad));
                    } else if (layer instanceof Resource.Audio) {
                        Resource.Audio audio = (Resource.Audio) layer;
                        writer.println("Audio ID: " + audio.id);
                        writer.println("Base volume: " + audio.bvol);
                        saveAudio(audio);
                    } else if (layer instanceof Resource.Code) {
                        Resource.Code code = (Resource.Code) layer;
                        writer.println("Code name: " + code.name);
                        saveCode(code);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error dumping resource info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveImage(Resource.Image img) {
        try {
            String filename = String.format("%s/%s_image_%d.png", debugDir, resource.name.replace('/', '_'), img.id);
            BufferedImage image = img.img;
            ImageIO.write(image, "PNG", new File(filename));
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    private void saveAudio(Resource.Audio audio) {
        try {
            String filename = String.format("%s/%s_audio_%s.ogg", debugDir, resource.name.replace('/', '_'), audio.id);
            Files.write(Paths.get(filename), audio.coded);
        } catch (Exception e) {
            System.err.println("Error saving audio: " + e.getMessage());
        }
    }

    private void saveCode(Resource.Code code) {
        try {
            String filename = String.format("%s/%s_code_%s.java", debugDir, resource.name.replace('/', '_'), code.name);
            Files.write(Paths.get(filename), code.data);
        } catch (Exception e) {
            System.err.println("Error saving code: " + e.getMessage());
        }
    }

    public static void dumpAllResources(Collection<Resource> resources, String debugDir) {
        for (Resource res : resources) {
            new DebugRender(res, debugDir).dumpResourceInfo();
        }
    }
}