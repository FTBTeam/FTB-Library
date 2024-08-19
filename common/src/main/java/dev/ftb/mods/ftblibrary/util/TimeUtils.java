package dev.ftb.mods.ftblibrary.util;

public class TimeUtils {
    public static String getTimeString(long millis) {
        var neg = false;
        if (millis < 0L) {
            neg = true;
            millis = -millis;
        }

        var sb = new StringBuilder();

        if (millis < 1000L) {
            if (neg) {
                sb.append('-');
            }

            sb.append(millis);
            sb.append('m');
            sb.append('s');
            return sb.toString();
        }

        var secs = millis / 1000L;

        if (neg) {
            sb.append('-');
        }

        var h = (secs / 3600L) % 24;
        var m = (secs / 60L) % 60L;
        var s = secs % 60L;

        if (secs >= 86400L) {
            sb.append(secs / 86400L);
            sb.append('d');
            sb.append(' ');
        }

        if (h > 0 || secs >= 86400L) {
            if (h < 10) {
                sb.append('0');
            }
            sb.append(h);
            //sb.append("h ");
            sb.append(':');
        }

        if (m < 10) {
            sb.append('0');
        }
        sb.append(m);
        //sb.append("m ");
        sb.append(':');
        if (s < 10) {
            sb.append('0');
        }
        sb.append(s);
        //sb.append('s');

        return sb.toString();
    }

    public static String prettyTimeString(long seconds) {
        if (seconds <= 0L) {
            return "0 seconds";
        }

        var builder = new StringBuilder();
        prettyTimeString(builder, seconds, true);
        return builder.toString();
    }

    private static void prettyTimeString(StringBuilder builder, long seconds, boolean addAnother) {
        if (seconds <= 0L) {
            return;
        } else if (!addAnother) {
            builder.append(" and ");
        }

        if (seconds < 60L) {
            builder.append(seconds);
            builder.append(seconds == 1L ? " second" : " seconds");
        } else if (seconds < 3600L) {
            builder.append(seconds / 60L);
            builder.append(seconds / 60L == 1L ? " minute" : " minutes");

            if (addAnother) {
                prettyTimeString(builder, seconds % 60L, false);
            }
        } else if (seconds < 86400L) {
            builder.append(seconds / 3600L);
            builder.append(seconds / 3600L == 1L ? " hour" : " hours");

            if (addAnother) {
                prettyTimeString(builder, seconds % 3600L, false);
            }
        } else {
            builder.append(seconds / 86400L);
            builder.append(seconds / 86400L == 1L ? " day" : " days");

            if (addAnother) {
                prettyTimeString(builder, seconds % 86400L, false);
            }
        }
    }
}
