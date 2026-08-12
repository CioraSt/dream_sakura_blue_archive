package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Single source of truth for wiki HALO registration and academy ordering. */
public final class HaloCatalog {
    private static final String RESOURCE = "/data/dream_sakura_blue_archive/halo_catalog.tsv";
    private static final List<Entry> ENTRIES = load();
    private static final Map<String, Entry> BY_ID;

    static {
        Map<String, Entry> byId = new LinkedHashMap<>();
        for (Entry entry : ENTRIES) {
            if (byId.put(entry.id(), entry) != null) {
                throw new IllegalStateException("Duplicate HALO catalog id: " + entry.id());
            }
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    private HaloCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Entry get(String itemId) {
        return BY_ID.get(itemId);
    }

    public static String schoolOf(String itemId) {
        Entry entry = get(itemId);
        return entry == null ? null : entry.school();
    }

    public static boolean isGenerated(String itemId) {
        Entry entry = get(itemId);
        return entry != null && !entry.existing();
    }

    private static List<Entry> load() {
        InputStream stream = HaloCatalog.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing HALO catalog resource: " + RESOURCE);
        }
        List<Entry> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) continue;
                String[] fields = line.split("\\t", -1);
                if (fields.length < 9) {
                    throw new IllegalStateException("Invalid HALO catalog row: " + line);
                }
                result.add(new Entry(
                        fields[0], fields[1], fields[2],
                        Integer.parseInt(fields[3]), Integer.parseInt(fields[4]),
                        Boolean.parseBoolean(fields[5]), fields[6],
                        Integer.parseInt(fields[7], 16), Integer.parseInt(fields[8], 16)
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read HALO catalog", exception);
        }
        dream_sakura_blue_archive.LOGGER.info("Loaded {} HALO catalog entries", result.size());
        return List.copyOf(result);
    }

    public record Entry(
            String id,
            String name,
            String school,
            int portraitWidth,
            int portraitHeight,
            boolean existing,
            String portraitSource,
            int primaryColor,
            int secondaryColor
    ) {
        public String portraitId() {
            return id.endsWith("_halo") ? id.substring(0, id.length() - 5) : id;
        }

        public int backgroundStart() {
            return argb(0xD8, scale(primaryColor, 0.16F));
        }

        public int backgroundEnd() {
            return argb(0xD8, scale(secondaryColor, 0.28F));
        }

        public int borderStart() {
            return argb(0xFF, primaryColor);
        }

        public int borderEnd() {
            return argb(0xFF, secondaryColor);
        }

        private static int scale(int color, float factor) {
            int red = Math.round(((color >> 16) & 0xFF) * factor);
            int green = Math.round(((color >> 8) & 0xFF) * factor);
            int blue = Math.round((color & 0xFF) * factor);
            return (red << 16) | (green << 8) | blue;
        }

        private static int argb(int alpha, int color) {
            return (alpha << 24) | (color & 0xFFFFFF);
        }

    }
}
