package com.icloud.parser;

import org.apache.hadoop.io.Text;

public class NcdcRecordParser implements Parser<Text, NcdcRecordParser.NcdcRecord> {

    private static final int MISSING_TEMPERATURE = 9999;

    @Override
    public NcdcRecord parse(Text record) {
        String record1 = record.toString();
        final String year = record1.substring(15, 19);
        boolean airTemperatureMalformed = false;
        int airTemperature = 0;
        switch (record1.charAt(87)) {
            case '+':
                airTemperature = Integer.parseInt(record1.substring(88, 92));
                break;
            case '-':
                airTemperature = Integer.parseInt(record1.substring(87, 92));
                break;
            default:
                airTemperatureMalformed = true;
        }

        final String quality = record1.substring(92, 93);
        return new NcdcRecord(year, airTemperature, quality, airTemperatureMalformed);
    }

    public static class NcdcRecord {

        private final String year;
        private final int airTemperature;
        private final String quality;
        private final boolean airTemperatureMalformed;

        public NcdcRecord(String year, int airTemperature, String quality, boolean airTemperatureMalformed) {
            this.year = year;
            this.airTemperature = airTemperature;
            this.quality = quality;
            this.airTemperatureMalformed = airTemperatureMalformed;
        }

        public String getYear() {
            return year;
        }

        public int getAirTemperature() {
            return airTemperature;
        }

        public String getQuality() {
            return quality;
        }

        public boolean isValidTemperature() {
            return !airTemperatureMalformed && airTemperature != MISSING_TEMPERATURE
                   && quality.matches("[01459]");
        }

        public boolean isMalformedTemperature() {
            return airTemperatureMalformed;
        }
    }

}
