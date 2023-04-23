package util

/** Utilities for managing time. */
class TimeManager {

    private val secondsSinceEpoch get() = System.nanoTime().toDouble() / 1_000_000_000

    private val startTime by lazy { secondsSinceEpoch }

    /** The time passed since the first call to [update]. */
    var sinceStart = -1.0
        private set

    /** The time passed between the two previous calls to [update]. */
    var delta = -1.0
        private set

    /**
     * Updates the current time [sinceStart], and sets the [delta] to the time passed
     * since the previous call to this method.
     */
    fun update(): TimeManager {
        if (sinceStart < 0) sinceStart = startTime
        val old = sinceStart
        sinceStart = secondsSinceEpoch
        delta = sinceStart - old
        return this
    }
}