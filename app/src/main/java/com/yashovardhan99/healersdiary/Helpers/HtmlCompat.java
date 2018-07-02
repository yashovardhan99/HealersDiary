package com.yashovardhan99.healersdiary.Helpers;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * Created by Yashovardhan99 on 02-07-2018 as a part of HealersDiary.
 */

public class HtmlCompat {

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(source);
    }
}
