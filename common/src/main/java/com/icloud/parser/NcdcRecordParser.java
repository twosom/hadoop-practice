package com.icloud.parser;

import org.apache.hadoop.io.Text;

public class NcdcRecordParser implements Parser<Text, NcdcRecordParser.NcdcRecord> {

    private static final int MISSING_TEMPERATURE = 9999;

    @Override
    public NcdcRecord parse(Text record) {
        final String recordString = record.toString();
        final String stationId = recordString.substring(4, 10) + "-" + recordString.substring(10, 15);
        final String year = recordString.substring(15, 19);
        boolean airTemperatureMalformed = false;
        int airTemperature = 0;
        switch (recordString.charAt(87)) {
            case '+':
                airTemperature = Integer.parseInt(recordString.substring(88, 92));
                break;
            case '-':
                airTemperature = Integer.parseInt(recordString.substring(87, 92));
                break;
            default:
                airTemperatureMalformed = true;
        }

        final String quality = recordString.substring(92, 93);
        return new NcdcRecord(stationId, year, airTemperature, quality, airTemperatureMalformed);
    }

    public static class NcdcRecord {

        private final String stationId;
        private final String year;
        private final int airTemperature;
        private final String quality;
        private final boolean airTemperatureMalformed;

        public NcdcRecord(String stationId, String year, int airTemperature, String quality, boolean airTemperatureMalformed) {
            this.stationId = stationId;
            this.year = year;
            this.airTemperature = airTemperature;
            this.quality = quality;
            this.airTemperatureMalformed = airTemperatureMalformed;
        }

        public String getYear() {
            return this.year;
        }

        public int getYearInt() {
            return Integer.parseInt(this.year);
        }

        public int getAirTemperature() {
            return this.airTemperature;
        }

        public String getQuality() {
            return this.quality;
        }

        public boolean isValidTemperature() {
            return !airTemperatureMalformed && airTemperature != MISSING_TEMPERATURE
                   && quality.matches("[01459]");
        }

        public boolean isMissingTemperature() {
            return airTemperature == MISSING_TEMPERATURE;
        }

        public String getStationId() {
            return this.stationId;
        }

        public boolean isMalformedTemperature() {
            return airTemperatureMalformed;
        }
    }

}
