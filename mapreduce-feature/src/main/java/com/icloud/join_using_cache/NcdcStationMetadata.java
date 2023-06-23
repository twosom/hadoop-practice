package com.icloud.join_using_cache;

import com.icloud.parser.NcdcStationMetadataParser;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NcdcStationMetadata {
    private Map<String, String> stationIdToName = new HashMap<>();
    NcdcStationMetadataParser parser = new NcdcStationMetadataParser();

    public void initialize(final InputStream inputStream) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(inputStream));
            in.lines().map(parser::parse)
                    .filter(Objects::nonNull)
                    .forEach(record -> stationIdToName.put(record.getStationId(), record.getStationName()));
        } finally {
            IOUtils.closeStream(in);
        }
    }

    public String getStationName(String stationId) {
        String stationName = stationIdToName.get(stationId);
        if (stationName == null || stationName.trim().length() == 0) {
            return stationId; // no match: fall back to ID
        }
        return stationName;
    }

    public void initialize(File file) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            in.lines()
                    .map(parser::parse)
                    .filter(Objects::nonNull)
                    .forEach(record -> stationIdToName.put(record.getStationId(), record.getStationName()));
        } finally {
            IOUtils.closeStream(in);
        }
    }
}
