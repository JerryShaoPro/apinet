package com.jerryshao.apinet.settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * Settings loader that loads (parses) the settings in a yaml format by flattening them
 * into a map.
 *
 * @author kimchy, chuter
 */
public class YamlSettingsLoader {

    public static Map<String, String> load(String source) throws IOException {
        // replace tabs with whitespace (yaml does not accept tabs, but many users might use it still...)
        source = source.replace("\t", "  ");
        Yaml yaml = new Yaml();
        Map<Object, Object> yamlMap = (Map<Object, Object>) yaml.load(source);
        StringBuilder sb = new StringBuilder();
        Map<String, String> settings = new HashMap<String, String>();
        if (yamlMap == null) {
            return settings;
        }
        List<String> path = new ArrayList<String>();
        serializeMap(settings, sb, path, yamlMap);
        return settings;
    }

    private static void serializeMap(Map<String, String> settings, StringBuilder sb, List<String> path, Map<Object, Object> yamlMap) {
        for (Map.Entry<Object, Object> entry : yamlMap.entrySet()) {
        	String key = (entry.getKey() instanceof String) ? (String)entry.getKey() : String.valueOf(entry.getKey());
            if (entry.getValue() instanceof Map) {
            	path.add(key);
                serializeMap(settings, sb, path, (Map<Object, Object>) entry.getValue());
                path.remove(path.size() - 1);
            } else if (entry.getValue() instanceof List) {
                path.add(key);
                serializeList(settings, sb, path, (List) entry.getValue());
                path.remove(path.size() - 1);
            } else {
                serializeValue(settings, sb, path, key, entry.getValue());
            }
        }
    }

    private static void serializeList(Map<String, String> settings, StringBuilder sb, List<String> path, List yamlList) {
        int counter = 0;
        for (Object listEle : yamlList) {
            if (listEle instanceof Map) {
                path.add(Integer.toString(counter));
                serializeMap(settings, sb, path, (Map<Object, Object>) listEle);
                path.remove(path.size() - 1);
            } else if (listEle instanceof List) {
                path.add(Integer.toString(counter));
                serializeList(settings, sb, path, (List) listEle);
                path.remove(path.size() - 1);
            } else {
                serializeValue(settings, sb, path, Integer.toString(counter), listEle);
            }
            counter++;
        }
    }

    private static void serializeValue(Map<String, String> settings, StringBuilder sb, List<String> path, String name, Object value) {
        if (value == null) {
            return;
        }
        sb.setLength(0);
        for (String pathEle : path) {
            sb.append(pathEle).append('.');
        }
        sb.append(name);
        settings.put(sb.toString(), value.toString());
    }
}
