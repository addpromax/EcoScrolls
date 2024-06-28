package com.willfp.ecoscrolls.scrolls

import com.willfp.eco.core.data.newPersistentDataContainer
import com.willfp.eco.core.fast.FastItemStack
import com.willfp.eco.core.fast.fast
import com.willfp.ecoscrolls.plugin
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

private val SCROLL_KEY = plugin.createNamespacedKey("scroll")
private val SCROLLS_KEY = plugin.createNamespacedKey("scrolls")
private val SCROLL_ID_KEY = plugin.createNamespacedKey("scroll")
private val SCROLL_LEVEL_KEY = plugin.createNamespacedKey("level")

var ItemStack.scroll: Scroll?
    get() = this.fast().scroll
    set(value) {
        this.fast().scroll = value
    }

var FastItemStack.scroll: Scroll?
    get() = this.persistentDataContainer.scroll
    set(value) {
        this.persistentDataContainer.scroll = value
    }

var PersistentDataContainer.scroll: Scroll?
    get() {
        val id = this.get(SCROLL_KEY, PersistentDataType.STRING) ?: return null
        return Scrolls[id]
    }
    set(value) {
        if (value == null) {
            this.remove(SCROLL_KEY)
            return
        }

        this.set(SCROLL_KEY, PersistentDataType.STRING, value.id)
    }

var ItemStack.scrolls: Set<ScrollLevel>
    get() = this.fast().scrolls
    set(value) {
        this.fast().scrolls = value
    }

var FastItemStack.scrolls: Set<ScrollLevel>
    get() = this.persistentDataContainer.scrolls
    set(value) {
        this.persistentDataContainer.scrolls = value
    }

var PersistentDataContainer.scrolls: Set<ScrollLevel>
    get() {
        if (!this.has(SCROLLS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY)) {
            return emptySet()
        }

        val baseTag = this.get(SCROLLS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY) ?: return emptySet()

        val scrolls = mutableSetOf<ScrollLevel>()

        for (tag in baseTag) {
            val scroll = Scrolls[tag.get(SCROLL_ID_KEY, PersistentDataType.STRING)] ?: return emptySet()
            val level = tag.get(SCROLL_LEVEL_KEY, PersistentDataType.INTEGER) ?: return emptySet()

            scrolls += scroll.getLevel(level)
        }

        return scrolls
    }
    set(value) {
        if (value.isEmpty()) {
            this.remove(SCROLLS_KEY)
            return
        }

        val tags = mutableListOf<PersistentDataContainer>()

        for (scroll in value) {
            val scrollTag = newPersistentDataContainer()

            scrollTag.set(SCROLL_ID_KEY, PersistentDataType.STRING, scroll.scroll.id)
            scrollTag.set(SCROLL_LEVEL_KEY, PersistentDataType.INTEGER, scroll.level)

            tags += scrollTag
        }

        this.set(SCROLLS_KEY, PersistentDataType.TAG_CONTAINER_ARRAY, tags.toTypedArray())
    }

fun ItemStack.getScrollLevel(scroll: Scroll): ScrollLevel? {
    return this.scrolls.find { it.scroll == scroll }
}

fun FastItemStack.getScrollLevel(scroll: Scroll): ScrollLevel? {
    return this.scrolls.find { it.scroll == scroll }
}
