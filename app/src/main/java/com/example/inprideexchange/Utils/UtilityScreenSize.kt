package com.example.inprideexchange.Utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.*


class UtilityScreenSize {

    companion object {

        private val TABLET_SW_DP = 720

        @Composable
        private fun isTablet(): Boolean {
            val config = LocalConfiguration.current
            return config.smallestScreenWidthDp >= TABLET_SW_DP
        }

        // ✅ The new sets ups ..
        // ✅ The new sets ups ..
        // ✅ The new sets ups ..
        @Composable
        fun adaptiveFontSize(
            phoneSp: TextUnit = 16.sp,   // Normal phones (Infinix, Tecno, Samsung A)
            tabletSp: TextUnit = 20.sp   // Tablets only
        ): TextUnit {
            return if (isTablet()) tabletSp else phoneSp
        }


        @Composable
        fun adaptiveBorder(
            phoneDp: Dp = 1.dp,     // Normal phones (Infinix, Tecno, Samsung A)
            tabletDp: Dp = 1.5.dp   // Tablets only
        ): Dp {
            return if (isTablet()) tabletDp else phoneDp
        }


        @Composable
        fun adaptiveWidthFraction(
            phoneFraction: Float = 1f,    // Normal phones = full width
            tabletFraction: Float = 0.85f // Tablets = slightly narrower, centered UI
        ): Float {
            return if (isTablet()) tabletFraction else phoneFraction
        }


        @Composable
        fun adaptiveHeight(
            phoneDp: Dp = 48.dp,
            tabletDp: Dp = 64.dp
        ): Dp {
            return if (isTablet()) tabletDp else phoneDp
        }


        @Composable
        fun adaptiveWidth(
            phoneDp: Dp,             // Normal phones (Infinix, Tecno, Samsung A)
            tabletDp: Dp = phoneDp  // Optional tablet override
        ): Dp {
            return if (isTablet()) tabletDp else phoneDp
        }


        @Composable
        fun adaptiveMargin(
            phoneDp: Dp = 8.dp,      // Default margin for phones
            tabletDp: Dp = 16.dp    // Default margin for tablets
        ): Dp {
            return if (isTablet()) tabletDp else phoneDp
        }


        // ---------------------------------------------------------
        // 2️⃣ ADAPTIVE LINE HEIGHT
        // ---------------------------------------------------------
        @Composable
        fun adaptiveLineHeight(phoneSp: TextUnit, tabletSp: TextUnit): TextUnit {
            return if (isTablet()) tabletSp else phoneSp
        }

        // ---------------------------------------------------------
        // 3️⃣ ADAPTIVE LINE SPACING (extra spacing)
        // ---------------------------------------------------------
        @Composable
        fun adaptiveLineSpacing(phoneSp: TextUnit, tabletSp: TextUnit): TextUnit {
            return if (isTablet()) tabletSp else phoneSp
        }

        // ---------------------------------------------------------
        // 4️⃣ ADAPTIVE ICON SIZE (Dp)
        // ---------------------------------------------------------
        @Composable
        fun adaptiveIconSize(phoneDp: Dp, tabletDp: Dp): Dp {
            return if (isTablet()) tabletDp else phoneDp
        }

        // ---------------------------------------------------------
        // 5️⃣ ADAPTIVE SCALE FOR FLOAT VALUES (e.g. 0.8f → 1f)
        // ---------------------------------------------------------
        @Composable
        fun adaptiveScale(phone: Float, tablet: Float): Float {
            return if (isTablet()) tablet else phone
        }




        // ---------------------------------------------------------
        // 5️⃣ Fixed Adpatiive height
        // ---------------------------------------------------------

        val  adaptivePhoneHeight : Dp = 48.dp

    }
}


