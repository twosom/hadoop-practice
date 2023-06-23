package com.icloud.parser;

import org.apache.hadoop.io.Text;

import static com.icloud.parser.NcdcStationMetadataParser.StationMetadata;

public class NcdcStationMetadataParser implements Parser<Text, StationMetadata> {

    public static class StationMetadata {
        private String stationId;
        private String stationName;

        public StationMetadata(String stationId, String stationName) {
            this.stationId = stationId;
            this.stationName = stationName;
        }

        public String getStationId() {
            return stationId;
        }

        public String getStationName() {
            return stationName;
        }
    }

    @Override
    public StationMetadata parse(Text text) {
        return parse(text.toString());
    }

    public StationMetadata parse(String record) {
        if (record.length() < 42) return null;

        String usaf = record.substring(0, 6);
        String wban = record.substring(7, 12);

        String stationId = usaf + "-" + wban;
        String stationName = record.substring(13, 42);
        try {
            Integer.parseInt(usaf);
            return new StationMetadata(stationId, stationName);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
