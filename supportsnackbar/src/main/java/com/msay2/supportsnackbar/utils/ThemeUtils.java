package com.msay2.supportsnackbar.utils;

/**
 * Author: Meclot Yoann and The Android Open Source Project
 * Created and Modified on: 02/08/2018
 * Github: https://github.com/MSay2
 */

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Copyright MSay2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Support of compatibility for the {@Theme.Material} theme
 *
 * by MSay2 (Meclot Yoann)
 */

/**
 * check if the {@Theme.Material} theme is available
 */

public class ThemeUtils
{
    private static final int[] MATERIAL_CHECK_ATTRS =
            {
                    android.R.attr.colorPrimary
            };

    public static void checkMaterialTheme(Context context)
    {
        TypedArray typed = context.obtainStyledAttributes(MATERIAL_CHECK_ATTRS);
        boolean failed = !typed.hasValue(0);

        typed.recycle();
        if (failed)
        {
            throw new IllegalArgumentException("You need to use a Theme.Material theme");
        }
    }

    public static boolean isMaterial(Context context)
    {
        TypedArray typed = context.obtainStyledAttributes(MATERIAL_CHECK_ATTRS);
        boolean failed = !typed.hasValue(0);

        typed.recycle();

        return failed;
    }
}