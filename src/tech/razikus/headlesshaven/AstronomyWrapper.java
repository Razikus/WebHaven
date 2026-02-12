package tech.razikus.headlesshaven;

import haven.Astronomy;

import java.awt.*;

public class AstronomyWrapper extends Astronomy {
    public AstronomyWrapper(double dt, double mp, double yt, boolean night, Color mc, int is, double sp, double sd, double years, double ym, double md) {
        super(dt, mp, yt, night, mc, is, sp, sd, years, ym, md);
    }

    private static final String[] MOON_PHASES = {
            "New Moon",
            "Waxing Crescent",
            "First Quarter",
            "Waxing Gibbous",
            "Full Moon",
            "Waning Gibbous",
            "Last Quarter",
            "Waning Crescent"
    };


    public AstronomyProcessed process() {
        return new AstronomyProcessed(
                dt, mp, yt, sp, sd, years, ym, md,
                (int) Math.round(dt * 100),
                (int) Math.round(dt * 24),
                (int) Math.round(dt * 24 * 60),
                (int) Math.floor(md) + 1,
                (int) Math.floor(ym) + 1,
                (int) Math.floor(years) + 1,
                ord((int) Math.floor(md) + 1),
                ord((int) Math.floor(ym) + 1),
                ord((int) Math.floor(years) + 1),
                night,
                getTimeOfDay(),
                (int) Math.round(mp * 100),
                getMoonPhaseIndex(),
                MOON_PHASES[getMoonPhaseIndex()],
                mp * 360,
                mc != null ? String.format("#%02x%02x%02x", mc.getRed(), mc.getGreen(), mc.getBlue()) : null,
                is,
                getSeasonName(),
                (int) Math.round(sp * 100),
                String.format("%s day of the %s month of the %s year",
                        ord((int) Math.floor(md) + 1),
                        ord((int) Math.floor(ym) + 1),
                        ord((int) Math.floor(years) + 1)),
                getTooltip()
        );
    }

        private String getTimeOfDay() {
            double hours = dt * 24;
            if (hours < 6) return "Night";
            if (hours < 12) return "Morning";
            if (hours < 18) return "Afternoon";
            return "Evening";
        }

        private int getMoonPhaseIndex() {
            // Convert moon phase (0-1) to index (0-7)
            double adjustedPhase = (mp + 0.0625) % 1.0;
            return (int)(adjustedPhase * 8) % 8;
        }

        private String getMoonPhaseName() {
            return MOON_PHASES[getMoonPhaseIndex()];
        }

        private String getSeasonName() {
            switch (is) {
                case 0: return "Spring";
                case 1: return "Summer";
                case 2: return "Autumn";
                case 3: return "Winter";
                default: return "Unknown";
            }
        }



        private static String ord(int i) {
            if(((i % 100) / 10) != 1) {
                if((i % 10) == 1)
                    return(i + "st");
                else if((i % 10) == 2)
                    return(i + "nd");
                else if((i % 10) == 3)
                    return(i + "rd");
            }
            return(i + "th");
        }
        public  String getTooltip() {
            int day = (int)Math.floor(this.md) + 1;
            int month = (int)Math.floor(this.ym) + 1;
            int year = (int)Math.floor(this.years) + 1;

            return String.format("%s day of the %s month of the %s year",
                    ord(day),
                    ord(month),
                    ord(year));
        }

}
