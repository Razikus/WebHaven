package tech.razikus.headlesshaven;

import java.util.Objects;

public class AstronomyProcessed {
    // Raw values
    private final double dayTime;
    private final double moonPhase;
    private final double yearTime;
    private final double seasonProgress;
    private final double seasonDay;
    private final double yearsElapsed;
    private final double yearMonth;
    private final double monthDay;

    // Time calculations
    private final int dayTimePercentage;
    private final int dayTimeHours;
    private final int dayTimeMinutes;

    // Calendar values
    private final int currentDay;
    private final int currentMonth;
    private final int currentYear;

    // Formatted calendar values
    private final String dayOrdinal;
    private final String monthOrdinal;
    private final String yearOrdinal;

    // Time of day info
    private final boolean isNight;
    private final String timeOfDay;

    // Moon information
    private final int moonPhasePercentage;
    private final int moonPhaseIndex;
    private final String moonPhaseName;
    private final double moonPhaseAngle;
    private final String moonColor;

    // Season information
    private final int seasonIndex;
    private final String seasonName;
    private final int seasonProgressPercentage;

    // Formatted strings
    private final String formattedDate;
    private final String tooltip;

    public AstronomyProcessed(
            double dayTime, double moonPhase, double yearTime, double seasonProgress,
            double seasonDay, double yearsElapsed, double yearMonth, double monthDay,
            int dayTimePercentage, int dayTimeHours, int dayTimeMinutes,
            int currentDay, int currentMonth, int currentYear,
            String dayOrdinal, String monthOrdinal, String yearOrdinal,
            boolean isNight, String timeOfDay,
            int moonPhasePercentage, int moonPhaseIndex, String moonPhaseName,
            double moonPhaseAngle, String moonColor,
            int seasonIndex, String seasonName, int seasonProgressPercentage,
            String formattedDate, String tooltip) {

        this.dayTime = dayTime;
        this.moonPhase = moonPhase;
        this.yearTime = yearTime;
        this.seasonProgress = seasonProgress;
        this.seasonDay = seasonDay;
        this.yearsElapsed = yearsElapsed;
        this.yearMonth = yearMonth;
        this.monthDay = monthDay;

        this.dayTimePercentage = dayTimePercentage;
        this.dayTimeHours = dayTimeHours;
        this.dayTimeMinutes = dayTimeMinutes;

        this.currentDay = currentDay;
        this.currentMonth = currentMonth;
        this.currentYear = currentYear;

        this.dayOrdinal = dayOrdinal;
        this.monthOrdinal = monthOrdinal;
        this.yearOrdinal = yearOrdinal;

        this.isNight = isNight;
        this.timeOfDay = timeOfDay;

        this.moonPhasePercentage = moonPhasePercentage;
        this.moonPhaseIndex = moonPhaseIndex;
        this.moonPhaseName = moonPhaseName;
        this.moonPhaseAngle = moonPhaseAngle;
        this.moonColor = moonColor;

        this.seasonIndex = seasonIndex;
        this.seasonName = seasonName;
        this.seasonProgressPercentage = seasonProgressPercentage;

        this.formattedDate = formattedDate;
        this.tooltip = tooltip;
    }

    // Getters
    public double getDayTime() { return dayTime; }
    public double getMoonPhase() { return moonPhase; }
    public double getYearTime() { return yearTime; }
    public double getSeasonProgress() { return seasonProgress; }
    public double getSeasonDay() { return seasonDay; }
    public double getYearsElapsed() { return yearsElapsed; }
    public double getYearMonth() { return yearMonth; }
    public double getMonthDay() { return monthDay; }

    public int getDayTimePercentage() { return dayTimePercentage; }
    public int getDayTimeHours() { return dayTimeHours; }
    public int getDayTimeMinutes() { return dayTimeMinutes; }

    public int getCurrentDay() { return currentDay; }
    public int getCurrentMonth() { return currentMonth; }
    public int getCurrentYear() { return currentYear; }

    public String getDayOrdinal() { return dayOrdinal; }
    public String getMonthOrdinal() { return monthOrdinal; }
    public String getYearOrdinal() { return yearOrdinal; }

    public boolean isNight() { return isNight; }
    public String getTimeOfDay() { return timeOfDay; }

    public int getMoonPhasePercentage() { return moonPhasePercentage; }
    public int getMoonPhaseIndex() { return moonPhaseIndex; }
    public String getMoonPhaseName() { return moonPhaseName; }
    public double getMoonPhaseAngle() { return moonPhaseAngle; }
    public String getMoonColor() { return moonColor; }

    public int getSeasonIndex() { return seasonIndex; }
    public String getSeasonName() { return seasonName; }
    public int getSeasonProgressPercentage() { return seasonProgressPercentage; }

    public String getFormattedDate() { return formattedDate; }
    public String getTooltip() { return tooltip; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AstronomyProcessed that = (AstronomyProcessed) o;
        return Double.compare(dayTime, that.dayTime) == 0 && Double.compare(moonPhase, that.moonPhase) == 0 && Double.compare(yearTime, that.yearTime) == 0 && Double.compare(seasonProgress, that.seasonProgress) == 0 && Double.compare(seasonDay, that.seasonDay) == 0 && Double.compare(yearsElapsed, that.yearsElapsed) == 0 && Double.compare(yearMonth, that.yearMonth) == 0 && Double.compare(monthDay, that.monthDay) == 0 && dayTimePercentage == that.dayTimePercentage && dayTimeHours == that.dayTimeHours && dayTimeMinutes == that.dayTimeMinutes && currentDay == that.currentDay && currentMonth == that.currentMonth && currentYear == that.currentYear && isNight == that.isNight && moonPhasePercentage == that.moonPhasePercentage && moonPhaseIndex == that.moonPhaseIndex && Double.compare(moonPhaseAngle, that.moonPhaseAngle) == 0 && seasonIndex == that.seasonIndex && seasonProgressPercentage == that.seasonProgressPercentage && Objects.equals(dayOrdinal, that.dayOrdinal) && Objects.equals(monthOrdinal, that.monthOrdinal) && Objects.equals(yearOrdinal, that.yearOrdinal) && Objects.equals(timeOfDay, that.timeOfDay) && Objects.equals(moonPhaseName, that.moonPhaseName) && Objects.equals(moonColor, that.moonColor) && Objects.equals(seasonName, that.seasonName) && Objects.equals(formattedDate, that.formattedDate) && Objects.equals(tooltip, that.tooltip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayTime, moonPhase, yearTime, seasonProgress, seasonDay, yearsElapsed, yearMonth, monthDay, dayTimePercentage, dayTimeHours, dayTimeMinutes, currentDay, currentMonth, currentYear, dayOrdinal, monthOrdinal, yearOrdinal, isNight, timeOfDay, moonPhasePercentage, moonPhaseIndex, moonPhaseName, moonPhaseAngle, moonColor, seasonIndex, seasonName, seasonProgressPercentage, formattedDate, tooltip);
    }

    @Override
    public String toString() {
        return "AstronomyProcessed{" +
                "dayTime=" + dayTime +
                ", moonPhase=" + moonPhase +
                ", yearTime=" + yearTime +
                ", seasonProgress=" + seasonProgress +
                ", seasonDay=" + seasonDay +
                ", yearsElapsed=" + yearsElapsed +
                ", yearMonth=" + yearMonth +
                ", monthDay=" + monthDay +
                ", dayTimePercentage=" + dayTimePercentage +
                ", dayTimeHours=" + dayTimeHours +
                ", dayTimeMinutes=" + dayTimeMinutes +
                ", currentDay=" + currentDay +
                ", currentMonth=" + currentMonth +
                ", currentYear=" + currentYear +
                ", dayOrdinal='" + dayOrdinal + '\'' +
                ", monthOrdinal='" + monthOrdinal + '\'' +
                ", yearOrdinal='" + yearOrdinal + '\'' +
                ", isNight=" + isNight +
                ", timeOfDay='" + timeOfDay + '\'' +
                ", moonPhasePercentage=" + moonPhasePercentage +
                ", moonPhaseIndex=" + moonPhaseIndex +
                ", moonPhaseName='" + moonPhaseName + '\'' +
                ", moonPhaseAngle=" + moonPhaseAngle +
                ", moonColor='" + moonColor + '\'' +
                ", seasonIndex=" + seasonIndex +
                ", seasonName='" + seasonName + '\'' +
                ", seasonProgressPercentage=" + seasonProgressPercentage +
                ", formattedDate='" + formattedDate + '\'' +
                ", tooltip='" + tooltip + '\'' +
                '}';
    }
}