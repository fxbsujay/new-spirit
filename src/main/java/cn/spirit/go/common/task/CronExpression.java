package cn.spirit.go.common.task;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.BitSet;

public class CronExpression {

    private static final int CRON_PARTS_6 = 6;
    private static final int CRON_PARTS_5 = 5;

    private final CronField seconds;
    private final CronField minutes;
    private final CronField hours;
    private final CronField dayOfMonth;
    private final CronField month;
    private final CronField dayOfWeek;
    private final boolean hasSeconds;
    private final String expression;

    public CronExpression(String expression) {
        this.expression = expression.trim();
        String[] parts = this.expression.split("\\s+");
        if (parts.length != CRON_PARTS_5 && parts.length != CRON_PARTS_6) {
            throw new IllegalArgumentException(
                "Cron expression must have 5 or 6 fields, got " + parts.length + ": " + expression);
        }

        int idx = 0;
        if (parts.length == CRON_PARTS_6) {
            hasSeconds = true;
            seconds = parseField(parts[idx++], 0, 59);
        } else {
            hasSeconds = false;
            seconds = parseField("0", 0, 59);
        }
        minutes = parseField(parts[idx++], 0, 59);
        hours = parseField(parts[idx++], 0, 23);
        dayOfMonth = parseField(parts[idx++], 1, 31);
        month = parseField(parts[idx++], 1, 12);
        dayOfWeek = parseField(parts[idx], 0, 7);
    }

    public boolean matches(LocalDateTime time) {
        return seconds.matches(time.getSecond())
            && minutes.matches(time.getMinute())
            && hours.matches(time.getHour())
            && month.matches(time.getMonthValue())
            && matchDay(time);
    }

    private boolean matchDay(LocalDateTime time) {
        boolean domRestricted = !dayOfMonth.isAll();
        boolean dowRestricted = !dayOfWeek.isAll();
        if (domRestricted && dowRestricted) {
            return dayOfMonth.matches(time.getDayOfMonth())
                || dayOfWeek.matches(time.getDayOfWeek().getValue() % 7);
        }
        if (domRestricted) {
            return dayOfMonth.matches(time.getDayOfMonth());
        }
        if (dowRestricted) {
            return dayOfWeek.matches(time.getDayOfWeek().getValue() % 7);
        }
        return true;
    }

    public LocalDateTime nextFireTime(LocalDateTime after) {
        LocalDateTime candidate = hasSeconds
            ? after.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1)
            : after.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);

        int maxIterations = 366 * 24 * 60 * 60;
        int iterations = 0;

        while (iterations < maxIterations) {
            iterations++;

            if (!seconds.matches(candidate.getSecond())) {
                int nextSec = seconds.nextSetBit(candidate.getSecond());
                if (nextSec < 0) {
                    candidate = candidate.plusMinutes(1).withSecond(seconds.nextSetBit(0));
                } else {
                    candidate = candidate.withSecond(nextSec);
                }
            }

            if (!minutes.matches(candidate.getMinute())) {
                int nextMin = minutes.nextSetBit(candidate.getMinute());
                if (nextMin < 0) {
                    candidate = candidate.plusHours(1).withMinute(minutes.nextSetBit(0)).withSecond(seconds.nextSetBit(0));
                } else {
                    candidate = candidate.withMinute(nextMin).withSecond(seconds.nextSetBit(0));
                }
            }

            if (!hours.matches(candidate.getHour())) {
                int nextHour = hours.nextSetBit(candidate.getHour());
                if (nextHour < 0) {
                    candidate = candidate.plusDays(1).withHour(hours.nextSetBit(0))
                        .withMinute(minutes.nextSetBit(0)).withSecond(seconds.nextSetBit(0));
                } else {
                    candidate = candidate.withHour(nextHour).withMinute(minutes.nextSetBit(0))
                        .withSecond(seconds.nextSetBit(0));
                }
            }

            if (!month.matches(candidate.getMonthValue())) {
                int nextMon = month.nextSetBit(candidate.getMonthValue());
                if (nextMon < 0) {
                    candidate = candidate.plusYears(1).withMonth(month.nextSetBit(0)).withDayOfMonth(1)
                        .withHour(hours.nextSetBit(0)).withMinute(minutes.nextSetBit(0))
                        .withSecond(seconds.nextSetBit(0));
                } else {
                    candidate = candidate.withMonth(nextMon).withDayOfMonth(1)
                        .withHour(hours.nextSetBit(0)).withMinute(minutes.nextSetBit(0))
                        .withSecond(seconds.nextSetBit(0));
                }
            }

            if (!matchDay(candidate)) {
                int year = candidate.getYear();
                int mon = candidate.getMonthValue();

                for (int attempt = 0; attempt < 366; attempt++) {
                    candidate = candidate.plusDays(1);
                    if (candidate.getMonthValue() != mon) {
                        mon = candidate.getMonthValue();
                        if (!month.matches(mon)) {
                            int nextMon = month.nextSetBit(mon);
                            if (nextMon < 0) {
                                candidate = candidate.plusYears(1).withMonth(month.nextSetBit(0)).withDayOfMonth(1);
                            } else {
                                candidate = candidate.withMonth(nextMon).withDayOfMonth(1);
                            }
                            mon = candidate.getMonthValue();
                        }
                    }
                    int lastDay = YearMonth.of(candidate.getYear(), candidate.getMonthValue()).lengthOfMonth();
                    if (candidate.getDayOfMonth() > lastDay) {
                        candidate = candidate.plusMonths(1).withDayOfMonth(1);
                        mon = candidate.getMonthValue();
                        if (!month.matches(mon)) {
                            continue;
                        }
                    }
                    if (matchDay(candidate)) {
                        break;
                    }
                }
                candidate = candidate.withHour(hours.nextSetBit(0))
                    .withMinute(minutes.nextSetBit(0)).withSecond(seconds.nextSetBit(0));
            }

            if (isMatch(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No matching time found within limit for: " + expression);
    }

    private boolean isMatch(LocalDateTime time) {
        return seconds.matches(time.getSecond())
            && minutes.matches(time.getMinute())
            && hours.matches(time.getHour())
            && month.matches(time.getMonthValue())
            && matchDay(time);
    }

    @Override
    public String toString() {
        return expression;
    }

    private static CronField parseField(String field, int min, int max) {
        BitSet bits = new BitSet(max + 1);
        boolean isAll = false;

        if (field.equals("*") || field.equals("?")) {
            isAll = true;
            bits.set(min, max + 1);
        } else {
            String[] parts = field.split(",");
            for (String part : parts) {
                parsePart(part, min, max, bits);
            }
            if (bits.cardinality() == (max - min + 1)) {
                isAll = true;
            }
        }

        return new CronField(bits, min, max, isAll);
    }

    private static void parsePart(String part, int min, int max, BitSet bits) {
        int slashIdx = part.indexOf('/');
        int step = 1;
        if (slashIdx > 0) {
            step = Integer.parseInt(part.substring(slashIdx + 1));
            part = part.substring(0, slashIdx);
        }

        if (part.equals("*")) {
            for (int i = min; i <= max; i += step) {
                bits.set(i);
            }
        } else {
            int dashIdx = part.indexOf('-');
            if (dashIdx > 0) {
                int start = Integer.parseInt(part.substring(0, dashIdx));
                int end = Integer.parseInt(part.substring(dashIdx + 1));
                for (int i = start; i <= end; i += step) {
                    bits.set(i);
                }
            } else {
                bits.set(Integer.parseInt(part));
            }
        }
    }

    static class CronField {
        final BitSet bits;
        final int min;
        final int max;
        final boolean all;

        CronField(BitSet bits, int min, int max, boolean all) {
            this.bits = bits;
            this.min = min;
            this.max = max;
            this.all = all;
        }

        boolean matches(int value) {
            if (value < min || value > max) {
                return false;
            }
            return all || bits.get(value);
        }

        boolean isAll() {
            return all;
        }

        int nextSetBit(int from) {
            return bits.nextSetBit(Math.max(from, min));
        }
    }
}
