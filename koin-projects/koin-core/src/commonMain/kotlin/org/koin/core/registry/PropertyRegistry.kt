/*
 * Copyright 2017-2018 the original author or authors.
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
package org.koin.core.registry

import org.koin.core.KoinApplication.Companion.logger
import org.koin.core.error.NoPropertyFileFoundException
import org.koin.core.logger.Level
import org.koin.core.mp.KoinMPProperties
import org.koin.core.mp.KoinMultiPlatform
import org.koin.core.mp.getSystemEnvironmentProperties
import org.koin.core.mp.toMap
import org.koin.ext.isFloat
import org.koin.ext.isInt
import org.koin.ext.quoted

/**
 * Property Registry
 * Save/find all Koin properties
 *
 * @author Arnaud Giuliani
 */
@Suppress("UNCHECKED_CAST")
class PropertyRegistry {

    private val values: MutableMap<String, Any> = KoinMultiPlatform.emptyMutableMap()

    /**
     * saveProperty all properties to registry
     * @param properties
     */
    fun saveProperties(properties: Map<String, Any>) {
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("load ${properties.size} properties")
        }

        values.putAll(properties)
    }

    /**
     *Save properties values into PropertyRegister
     */
    fun saveProperties(properties: KoinMPProperties) {
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("load ${properties.size} properties")
        }

        val propertiesMapValues = properties.toMap()
        propertiesMapValues.forEach { (k: String, v: String) ->
            when {
                v.isInt() -> saveProperty(k, v.toInt())
                v.isFloat() -> saveProperty(k, v.toFloat())
                else -> saveProperty(k, v.quoted())
            }
        }
    }

    /**
     * save a property (key,value)
     */
    internal fun <T : Any> saveProperty(key: String, value: T) {
        values[key] = value
    }

    /**
     * Get a property
     * @param key
     */
    fun <T> getProperty(key: String): T? {
        return values[key] as? T?
    }

    /**
     * Load properties from Property file
     * @param fileName
     */
    fun loadPropertiesFromFile(fileName: String) {
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("load properties from $fileName")
        }
        val content = KoinMultiPlatform.loadResourceString(fileName)
        if (content != null) {
            if (logger.isAt(Level.INFO)) {
                logger.info("loaded properties from file:'$fileName'")
            }
            val properties = KoinMultiPlatform.parseProperties(content)
            saveProperties(properties)
        } else {
            throw NoPropertyFileFoundException("No properties found for file '$fileName'")
        }
    }

    /**
     * Load properties from environment
     */
    fun loadEnvironmentProperties() {
        if (logger.isAt(Level.DEBUG)) {
            logger.debug("load properties from environment")
        }
        saveProperties(KoinMultiPlatform.getSystemProperties())
        saveProperties(getSystemEnvironmentProperties())
    }

    fun close() {
        values.clear()
    }
}
