package com.ihd2.dyn4j

import org.dyn4j.collision.Filter

/**
 * Forbids a model from colliding with itself
 */
class NoSelfCollisionFilter(val modelId: Int): Filter {
    override fun isAllowed(filter: Filter?): Boolean {
        if (filter == Filter.DEFAULT_FILTER) return true

        return if (filter is NoSelfCollisionFilter) {
            modelId != filter.modelId
        } else false
    }
}