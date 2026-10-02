package info.plateaukao.einkbro.view

import android.graphics.DashPathEffect

/** One effect per current density; regular frames do not allocate an effect or interval array. */
internal class EdgeDashEffectCache {
    private var density = Float.NaN
    private var effect: DashPathEffect? = null

    fun atDensity(newDensity: Float): DashPathEffect {
        val current = effect
        if (current != null && density == newDensity) return current
        val replacement =
            DashPathEffect(
                floatArrayOf(DASH_DP * newDensity, GAP_DP * newDensity),
                0f,
            )
        density = newDensity
        effect = replacement
        return replacement
    }

    private companion object {
        const val DASH_DP = 5f
        const val GAP_DP = 4f
    }
}
