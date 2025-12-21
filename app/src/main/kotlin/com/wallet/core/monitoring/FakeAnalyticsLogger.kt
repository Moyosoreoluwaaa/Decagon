package com.wallet.core.monitoring


/**
 * Fake analytics logger for testing.
 * Captures events for verification.
 */
class FakeAnalyticsLogger : AnalyticsLogger {
    private val _events = mutableListOf<AnalyticsEvent>()
    val events: List<AnalyticsEvent> get() = _events.toList()

    private val _screenViews = mutableListOf<String>()
    val screenViews: List<String> get() = _screenViews.toList()

    override fun logScreenView(screenName: String, screenClass: String?) {
        _screenViews.add(screenName)
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        _events.add(AnalyticsEvent(eventName, params))
    }

    override fun logError(throwable: Throwable, context: String?, fatal: Boolean) {
        _events.add(AnalyticsEvent(
            name = OctaneEvents.ERROR_OCCURRED,
            params = mapOf(
                "message" to (throwable.message ?: "Unknown"),
                "context" to (context ?: "None"),
                "fatal" to fatal
            )
        ))
    }

    override fun setUserId(userId: String?) {
        // No-op for fake
    }

    override fun setUserProperty(propertyName: String, value: String) {
        // No-op for fake
    }

    override fun logPerformance(
        metricName: String,
        durationMillis: Long,
        attributes: Map<String, String>
    ) {
        _events.add(AnalyticsEvent(
            name = metricName,
            params = attributes + ("duration_ms" to durationMillis)
        ))
    }

    // Test helpers
    fun clear() {
        _events.clear()
        _screenViews.clear()
    }

    fun getEvent(eventName: String): AnalyticsEvent? {
        return _events.find { it.name == eventName }
    }

    fun hasEvent(eventName: String): Boolean {
        return _events.any { it.name == eventName }
    }

    fun getEventsOfType(eventName: String): List<AnalyticsEvent> {
        return _events.filter { it.name == eventName }
    }
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any>
)