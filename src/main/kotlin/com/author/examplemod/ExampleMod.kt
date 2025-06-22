package com.author.examplemod

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExampleMod {
    const val MOD_ID = "example_mod"
    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("ExampleMod")

    fun init() {
        LOGGER.info("Initializing example mod")
    }
}