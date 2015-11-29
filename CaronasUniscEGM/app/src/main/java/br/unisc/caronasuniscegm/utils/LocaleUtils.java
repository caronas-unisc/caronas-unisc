package br.unisc.caronasuniscegm.utils;

import android.content.Context;

import br.unisc.caronasuniscegm.R;

public class LocaleUtils {

    public static String periodToLocalizedString(Context context, String period) {
        switch (period) {
            case "morning":
                return context.getString(R.string.field_morning);

            case "afternoon":
                return context.getString(R.string.field_afternoon);

            case "night":
                return context.getString(R.string.field_night);
        }

        return "";
    }

}
