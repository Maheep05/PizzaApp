package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.pizza5ps.app.ui.theme.Motion

/**
 * A price that rolls rather than swaps.
 *
 * Customising a pizza changes the total constantly, and a number that hard-cuts
 * gives the user no signal about *which way* it moved. Rolling each digit — up when
 * the price rises, down when it falls — turns the total into live feedback on the
 * choice just made, which is the whole point of a customisation screen.
 *
 * Only the digits that actually change animate; the rest stay put.
 */
@Composable
fun AnimatedRupees(
    amount: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    // Tracked across recompositions so we know the direction of travel.
    val previous = remember { intArrayOf(amount) }
    val rising = amount >= previous[0]
    previous[0] = amount

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "₹", style = style, color = color)
        amount.toString().forEachIndexed { index, digit ->
            key(index) {
                AnimatedContent(
                    targetState = digit,
                    transitionSpec = {
                        val direction = if (rising) 1 else -1
                        (slideInVertically(Motion.mediumTween()) { height -> direction * height } +
                            fadeIn(Motion.fastTween())) togetherWith
                            (slideOutVertically(Motion.mediumTween()) { height -> -direction * height } +
                                fadeOut(Motion.fastTween())) using
                            SizeTransform(clip = false)
                    },
                    label = "priceDigit"
                ) { value ->
                    Text(text = value.toString(), style = style, color = color)
                }
            }
        }
    }
}

/** The same treatment for plain counts (cart quantity, item totals). */
@Composable
fun AnimatedCount(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    AnimatedContent(
        targetState = count,
        modifier = modifier,
        transitionSpec = {
            val rising = targetState > initialState
            val direction = if (rising) 1 else -1
            (slideInVertically(Motion.mediumTween()) { height -> direction * height } +
                fadeIn(Motion.fastTween())) togetherWith
                (slideOutVertically(Motion.mediumTween()) { height -> -direction * height } +
                    fadeOut(Motion.fastTween())) using
                SizeTransform(clip = false)
        },
        label = "count"
    ) { value ->
        Text(text = value.toString(), style = style, color = color)
    }
}
