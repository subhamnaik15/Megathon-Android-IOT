package com.megathon.smarthome.ui.home.view.cardSlider

import android.graphics.PointF
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

import java.security.InvalidParameterException

/**
 * Extended [LinearSnapHelper] that works **only** with [CardSliderLayoutManager].
 */
class CardSnapHelper : LinearSnapHelper() {

    private var recyclerView: RecyclerView? = null

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)

        if (recyclerView != null && recyclerView.layoutManager !is CardSliderLayoutManager) {
            throw InvalidParameterException("LayoutManager must be instance of CardSliderLayoutManager")
        }

        this.recyclerView = recyclerView
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager?,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val lm = layoutManager as CardSliderLayoutManager?

        val itemCount = lm!!.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val vectorProvider = layoutManager as RecyclerView.SmoothScroller.ScrollVectorProvider?

        val vectorForEnd =
            vectorProvider!!.computeScrollVectorForPosition(itemCount - 1) ?: return RecyclerView.NO_POSITION

        val distance = calculateScrollDistance(velocityX, velocityY)[0]
        var deltaJump: Int

        if (distance > 0) {
            deltaJump = Math.floor((distance / lm.cardWidth).toDouble()).toInt()
        } else {
            deltaJump = Math.ceil((distance / lm.cardWidth).toDouble()).toInt()
        }

        val deltaSign = Integer.signum(deltaJump)
        deltaJump = deltaSign * Math.min(3, Math.abs(deltaJump))

        if (vectorForEnd.x < 0) {
            deltaJump = -deltaJump
        }

        if (deltaJump == 0) {
            return RecyclerView.NO_POSITION
        }

        val currentPosition = lm.activeCardPosition
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        var targetPos = currentPosition + deltaJump
        if (targetPos < 0 || targetPos >= itemCount) {
            targetPos = RecyclerView.NO_POSITION
        }

        return targetPos
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return (layoutManager as CardSliderLayoutManager).topView
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        val lm = layoutManager as CardSliderLayoutManager
        val viewLeft = lm.getDecoratedLeft(targetView)
        val activeCardLeft = lm.activeCardLeft
        val activeCardCenter = lm.activeCardLeft + lm.cardWidth / 2
        val activeCardRight = lm.activeCardLeft + lm.cardWidth

        val out = intArrayOf(0, 0)
        if (viewLeft < activeCardCenter) {
            val targetPos = lm.getPosition(targetView)
            val activeCardPos = lm.activeCardPosition
            if (targetPos != activeCardPos) {
                out[0] = -(activeCardPos - targetPos) * lm.cardWidth
            } else {
                out[0] = viewLeft - activeCardLeft
            }
        } else {
            out[0] = viewLeft - activeCardRight + 1
        }

        if (out[0] != 0) {
            recyclerView!!.smoothScrollBy(out[0], 0, AccelerateInterpolator())
        }

        return intArrayOf(0, 0)
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return (layoutManager as CardSliderLayoutManager).getSmoothScroller(recyclerView)
    }

}
