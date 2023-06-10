package com.icloud.parser;

import org.apache.hadoop.io.Text;

public class NcdcRecordParser implements Parser<Text, NcdcRecordParser.NcdcRecord> {

    private static final int MISSING_TEMPERATURE = 9999;


    @Override
    public NcdcRecord parse(String record) {
        final String year = record.substring(15, 19);
        final String airTemperatureString = (record.charAt(87) == '+') ?
                record.substring(88, 92) :
                record.substring(87, 92);
        final int airTemperature = Integer.parseInt(airTemperatureString);
        final String quality = record.substring(92, 93);
        return new NcdcRecord(year, airTemperature, quality);
    }

    @Override
    public NcdcRecord parse(Text record) {
        return parse(record.toString());
    }

    public static class NcdcRecord {

        private final String year;
        private final int airTemperature;
        private final String quality;

        public NcdcRecord(String year, int airTemperature, String quality) {
            this.year = year;
            this.airTemperature = airTemperature;
            this.quality = quality;
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
            return this.airTemperature != MISSING_TEMPERATURE && quality.matches("[01459]");
        }
    }

}
