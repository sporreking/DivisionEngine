package io

import com.curiouscreature.kotlin.math.*
import ecs.Component
import ecs.Entity
import ecs.Scene
import ecs.Transform
import resources.AudioSource
import resources.Material
import resources.RegistryKit
import resources.Texture
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.cast
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf

data class StorageHandler<T>(
    val serialize: (T, Array<out RegistryKit>) -> String,
    val deserialize: (String, Array<out RegistryKit>) -> T?
)

class SceneSaver {

    companion object {

        private val storageHandlers = mutableMapOf<String, StorageHandler<*>>()
        private val storageHandlerTypeEncodings = mutableMapOf<KClass<*>, String>()
        private inline fun <reified T> storageHandler() = storageHandlerFromType(T::class) as StorageHandler<T & Any>
        private fun storageHandlerFromType(type: KClass<*>) = storageHandlers[storageHandlerTypeEncodings[type]]

        private val validTypeEncodingSymbols = buildString {
            for (i in 48..57) append(i.toChar())
            for (i in 65..90) append(i.toChar())
            for (i in 97..122) append(i.toChar())
        }

        private fun uintToTypeEncoding(i: UInt) = buildString {
            for (n in 0..(i.toInt().floorDiv(validTypeEncodingSymbols.length))) {
                append(validTypeEncodingSymbols[i.toInt() % validTypeEncodingSymbols.length])
            }
        }

        inline fun <reified T : Any> registerStorageHandler(storageHandler: StorageHandler<T>) =
            registerStorageHandler(T::class, storageHandler)

        fun registerStorageHandler(type: KClass<*>, storageHandler: StorageHandler<*>) {
            uintToTypeEncoding(storageHandlers.count().toUInt()).let { encoding ->
                storageHandlerTypeEncodings[type] = encoding
                storageHandlers[encoding] = storageHandler
            }
        }

        // Add basic type serializers
        init {
            // Integer types
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toByteOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toShortOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toIntOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toLongOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toUByteOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toUShortOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toUIntOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toULongOrNull() }))

            // Floating point types
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toFloatOrNull() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toDoubleOrNull() }))

            // String and boolean types
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text.toBoolean() }))
            registerStorageHandler(StorageHandler({ data, _ -> data.toString() }, { text, _ -> text[0] }))
            registerStorageHandler(StorageHandler({ data, _ -> data }, { text, _ -> text }))

            // Floating point vector types
            registerStorageHandler(StorageHandler( // Float2
                { data, _ -> "${data[0]},${data[1]}"},
                { text, _ -> text.split(',').let { f ->
                    Float2(f[0].toFloat(), f[1].toFloat())
                }}
            ))
            registerStorageHandler(StorageHandler( // Float3
                { data, _ -> "${data[0]},${data[1]},${data[2]}"},
                { text, _ -> text.split(',').let { f ->
                    Float3(f[0].toFloat(), f[1].toFloat(), f[2].toFloat())
                }}
            ))
            registerStorageHandler(StorageHandler( // Float4
                { data, _ -> "${data[0]},${data[1]},${data[2]},${data[3]}"},
                { text, _ -> text.split(',').let { f ->
                    Float4(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3].toFloat())
                }}
            ))

            // Boolean vector types
            registerStorageHandler(StorageHandler( // Float2
                { data, _ -> "${data[0]},${data[1]}"},
                { text, _ -> text.split(',').let { f ->
                    Bool2(f[0].toBoolean(), f[1].toBoolean())
                }}
            ))
            registerStorageHandler(StorageHandler( // Float3
                { data, _ -> "${data[0]},${data[1]},${data[2]}"},
                { text, _ -> text.split(',').let { f ->
                    Bool3(f[0].toBoolean(), f[1].toBoolean(), f[2].toBoolean())
                }}
            ))
            registerStorageHandler(StorageHandler( // Float4
                { data, _ -> "${data[0]},${data[1]},${data[2]},${data[3]}"},
                { text, _ -> text.split(',').let { f ->
                    Bool4(f[0].toBoolean(), f[1].toBoolean(), f[2].toBoolean(), f[3].toBoolean())
                }}
            ))

            // Matrix types
            registerStorageHandler(StorageHandler(
                { data, _ -> data.toFloatArray().joinToString(",") { f -> f.toString() } },
                { text, _ -> Mat2.of(*(text.split(',').map { f -> f.toFloat() }.toFloatArray()))}
            ))
            registerStorageHandler(StorageHandler(
                { data, _ -> data.toFloatArray().joinToString(",") { f -> f.toString() } },
                { text, _ -> Mat3.fromRowMajor(*(text.split(',').map { f -> f.toFloat() }.toFloatArray()))}
            ))
            registerStorageHandler(StorageHandler(
                { data, _ -> data.asGLArray().joinToString(",") { f -> f.toString() } },
                { text, _ -> Mat4.fromRowMajor(*(text.split(',').map { f -> f.toFloat() }.toFloatArray()))}
            ))

            // Quaternion
            registerStorageHandler(StorageHandler(
                { data, _ -> "${data.x},${data.y},${data.z},${data.w}" },
                { text, _ -> text.split(',').let { f ->
                    Quaternion(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3].toFloat())
                }}
            ))

            // TODO: REVIEW WHEN RESOURCE SYSTEM HAS BEEN UPDATED
            // Registry based resources
            registerStorageHandler(StorageHandler( // ShaderProgram
                { data, registryKits ->
                    registryKits.find { rk ->
                        rk.shaderProgram.resources.containsValue(data)
                    }?.shaderProgram?.resources?.let { r ->
                        r.keys.first { k -> r[k] == data }
                    } ?: ""
                },
                { text, registryKits ->
                    registryKits.find { rk ->
                        rk.shaderProgram.resources.containsKey(text)
                    }?.shaderProgram?.get(text)
                }
            ))
            registerStorageHandler(StorageHandler( // Texture
                { data, registryKits ->
                    registryKits.find { rk ->
                        rk.texture.resources.containsValue(data)
                    }?.texture?.resources?.let { r ->
                        r.keys.first { k -> r[k] == data }
                    } ?: ""
                },
                { text, registryKits ->
                    registryKits.find { rk ->
                        rk.texture.resources.containsKey(text)
                    }?.texture?.get(text)
                }
            ))
            registerStorageHandler(StorageHandler( // Mesh
                { data, registryKits ->
                    registryKits.find { rk ->
                        rk.mesh.resources.containsValue(data)
                    }?.mesh?.resources?.let { r ->
                        r.keys.first { k -> r[k] == data }
                    } ?: ""
                },
                { text, registryKits ->
                    registryKits.find { rk ->
                        rk.mesh.resources.containsKey(text)
                    }?.mesh?.get(text)
                }
            ))
            registerStorageHandler(StorageHandler( // Font
                { data, registryKits ->
                    registryKits.find { rk ->
                        rk.font.resources.containsValue(data)
                    }?.font?.resources?.let { r ->
                        r.keys.first { k -> r[k] == data }
                    } ?: ""
                },
                { text, registryKits ->
                    registryKits.find { rk ->
                        rk.font.resources.containsKey(text)
                    }?.font?.get(text)
                }
            ))
            registerStorageHandler(StorageHandler( // Audio
                { data, registryKits ->
                    registryKits.find { rk ->
                        rk.audioClip.resources.containsValue(data)
                    }?.audioClip?.resources?.let { r ->
                        r.keys.first { k -> r[k] == data }
                    } ?: ""
                },
                { text, registryKits ->
                    registryKits.find { rk ->
                        rk.audioClip.resources.containsKey(text)
                    }?.audioClip?.get(text)
                }
            ))
            registerStorageHandler(StorageHandler( // Material
                { data, rk -> with (data) {
                    "$kd;$ks;$alpha;${color.r};${color.g};${color.b};${color.a};${
                        texture?.let { t -> encodeText(storageHandler<Texture>().serialize(t, rk)) } ?: ""
                    }"
                }},
                { text, rk -> delimiterPattern.split(text).let { m -> Material(
                    m[0].toFloat(), m[1].toFloat(), m[2].toFloat(),
                    Float4(m[3].toFloat(), m[4].toFloat(), m[5].toFloat(), m[6].toFloat()),
                    if (m[7].isEmpty()) null else storageHandler<Texture>().deserialize(decodeText(m[7]), rk))
                }}
            ))

            // Audio Source
            registerStorageHandler(StorageHandler( // TODO: Implement audio source state serialization
                { _, _ -> ""},
                { _, _ -> AudioSource() }
            ))
        }

        private const val specialChars = ";:{}[]=|"

        private fun encodeText(text: String): String {
            var encodedText = text.replace("\\", "\\\\")
            specialChars.forEach { c ->
                encodedText = encodedText.replace("$c", "\\$c")
            }
            return encodedText
        }

        private fun decodeText(text: String): String {
            var decodedText = text
            specialChars.forEach { c ->
                decodedText = decodedText.replace("\\$c", "$c")
            }
            return decodedText.replace("\\\\", "\\")
        }

        private val delimiterPattern = "(?<!\\\\);".toRegex()
        private val equalsPattern = "(?<!\\\\)=".toRegex()
        private val pipePattern = "(?<!\\\\)[|]".toRegex()

        private data class ParenthesisExtractionResult(
            val extraction: String,
            val remaining: String
        )

        private fun extractFromParentheses(
            text: String, openingChar: Char, closingChar: Char
        ): ParenthesisExtractionResult {
            val openingPattern = ("(?<!\\\\)\\$openingChar").toRegex()
            val closingPattern = ("(?<!\\\\)\\$closingChar").toRegex()
            var offset = 0
            var extraction = ""
            do {
                offset = closingPattern.find(text, offset + 1)?.range?.endInclusive
                    ?: throw IllegalArgumentException("Text must contain closing '$closingChar' parenthesis")
                extraction = text.substring(1, offset)
            } while (openingPattern.findAll(extraction).count() != closingPattern.findAll(extraction).count())

            return ParenthesisExtractionResult(
                extraction,
                if (offset < text.length) text.substring(offset + 1) else ""
            )
        }

        fun <T> serialize(data: T, vararg registryKits: RegistryKit): String = data?.let { d ->
            when (d) {
                is List<*> -> "[${d.joinToString(";") { item -> serialize(item, *registryKits) }}]"
                is Set<*> -> "{${d.joinToString(";") { item -> serialize(item, *registryKits) }}}"
                is Map<*, *> -> "{${d.map { (k, v) ->
                    "${serialize(k, *registryKits)}=${serialize(v, *registryKits)}"
                }.joinToString(";")}}"
                else -> if (storageHandlerTypeEncodings[d::class] != null) "${ // Encode type
                    storageHandlerTypeEncodings[d::class]
                        ?: throw IllegalStateException("There is no storage handler for type: '${d::class}'")
                }:${ // Serialize data
                    (storageHandlerFromType(d::class) as StorageHandler<T & Any>?)
                        ?.serialize?.let { s -> encodeText(s(d, registryKits))}
                }" else "null" // TODO: REMOVE NULL ALLOWANCE
            }
        } ?: "null"

        // inline fun <reified T : Any> deserialize(text: String) = deserialize(T::class, text)

        private data class DeserializationResult(val item: Any?, val remaining: String)

        private fun deserializeFirstItem(
            text: String,
            storageHandlerTypeEncodingTranslation: Map<String, String>?,
            registryKits: Array<out RegistryKit>
        ): DeserializationResult {
            if (text.startsWith('[')) { // Deserialize list
                val extractionResult = extractFromParentheses(text, '[', ']')

                if (extractionResult.extraction == "") return DeserializationResult(
                    emptyList<Any?>().toMutableList(), extractionResult.remaining.removePrefix(";")
                )

                val list = buildList {
                    var extraction = extractionResult.extraction
                    do {
                        val deserializationResult = deserializeFirstItem(
                            extraction, storageHandlerTypeEncodingTranslation, registryKits
                        )
                        add(deserializationResult.item)
                        extraction = deserializationResult.remaining
                    } while (extraction.isNotEmpty())
                }.toMutableList()

                return DeserializationResult(list, extractionResult.remaining.removePrefix(";"))
            }
            else if (text.startsWith('{')) { // Deserialize map
                val extractionResult = extractFromParentheses(text, '{', '}')

                if (extractionResult.extraction == "") return DeserializationResult(
                    emptyMap<Any?, Any?>().toMutableMap(), extractionResult.remaining.removePrefix(";")
                )

                val map = buildMap {
                    var extraction = extractionResult.extraction
                    do {
                        val equalsOffset = equalsPattern.find(extraction)?.range?.start
                            ?: throw IllegalStateException("Map serialization does not contain any '=' symbols")
                        val key = deserializeFirstItem(
                            extraction.substring(0, equalsOffset), storageHandlerTypeEncodingTranslation, registryKits
                        ).item
                        val deserializationResult = deserializeFirstItem(
                            extraction.substring(equalsOffset + 1),
                            storageHandlerTypeEncodingTranslation,
                            registryKits
                        )
                        put(key, deserializationResult.item)
                        extraction = deserializationResult.remaining
                    } while (extraction.isNotEmpty())
                }.toMutableMap()

                return DeserializationResult(map, extractionResult.remaining.removePrefix(";"))
            }
            else if (text.startsWith("null")) return DeserializationResult( // Handle null
                null,
                text.substringAfter("null")
            )
            else { // Deserialize single item
                val typeEncoding = text.substringBefore(":").let { te ->
                    if (storageHandlerTypeEncodingTranslation == null) te
                    else storageHandlerTypeEncodingTranslation[te]
                }
                val storageHandler = storageHandlers[typeEncoding] // TODO: USE SAVED LOOKUP TABLE
                    ?: throw IllegalStateException("There is no storage handler for type encoding: '$typeEncoding'")
                val remaining = text.substringAfter(":")
                val delimiterOffset = delimiterPattern.find(remaining)?.range?.start ?: (remaining.length)
                val data = remaining.substring(0, delimiterOffset)
                return DeserializationResult(
                    storageHandler.deserialize(decodeText(data), registryKits),
                    remaining.substring(delimiterOffset).removePrefix(";")
                )
            }
        } // delimiterPattern.find(text)?.range?.start?.let { i -> text.substring(i) } ?: ""

        fun deserialize(
            text: String,
            storageHandlerTypeEncodingTranslation: Map<String, String>? = null,
            vararg registryKits: RegistryKit
        ) = deserializeFirstItem(text, storageHandlerTypeEncodingTranslation, registryKits).item

        private const val OUTPUT_DIRECTORY = "save"
        private fun getSaveFilePath(name: String) = "$OUTPUT_DIRECTORY/$name.sav"

        private const val END_MARKER = "-"

        fun save(
            name: String,
            components: Map<Class<Component>, MutableMap<Long, Component>>,
            entities: Map<Long, Entity>,
            vararg registryKits: RegistryKit // TODO: Review upon resource update
        ) {
            // Map of entity IDs to their components' IDs
            val componentIDs = mutableMapOf<Long, MutableList<Long>>()

            // Component type encodings mapped to types
            val componentTypeTable = mutableMapOf<String, String>()

            // Component output
            val componentOutput = buildString {
                components.entries.forEachIndexed { i, (type, comps) ->

                    // Do not save transform
                    if (type == Transform::class.java) return@forEachIndexed

                    // Register component type
                    val componentTypeEncoding = uintToTypeEncoding(i.toUInt())
                    componentTypeTable[componentTypeEncoding] = type.name

                    // Save components of current type
                    comps.forEach { (id, c) ->

                        // Add component ID to list
                        componentIDs.putIfAbsent(c.parent!!.id, mutableListOf(id))?.add(id)

                        // Add component to output
                        append("$id|$componentTypeEncoding|${
                            c::class.memberProperties.filter { property -> 
                                property.name != "id" && property.name != "parent" && property.name != "transform"
                                        && property.javaField != null
                            }.joinToString(separator = "|") { property ->
                                "${property.name}=${serialize(property.getter.call(c), *registryKits)}"
                            } // ${serialize(property.get(c))}
                        }\n")
                    }
                }
            }

            // Entity output
            val entityOutput = buildString {
                entities.forEach { (id, entity) ->
                    val transformInfo = entity.transform.run {
                        "$px,$py,$pz,$sx,$sy,$sz,${orientation.toFloatArray().joinToString(separator = ",")}"
                    }
                    val componentInfo = componentIDs[id]?.joinToString(separator = ",")
                    append("$id;$transformInfo;$componentInfo\n")
                }
            }

            // Type encodings
            val componentTypeEncodingOutput = componentTypeTable
                .map { (e, t) -> "$e=$t" }.joinToString("\n")
            val storageHandlerTypeEncodingOutput = storageHandlerTypeEncodings
                .map { (t, e) -> "$e=${t.qualifiedName}" }.joinToString("\n")

            // Save to disk
            File(OUTPUT_DIRECTORY).takeUnless { f -> f.isDirectory }?.mkdir()
            File(getSaveFilePath(name)).printWriter().use { out ->
                out.println("# Storage Handler Type Encodings")
                out.println(storageHandlerTypeEncodingOutput)
                out.println("$END_MARKER\n")

                out.println("# Component Type Encodings")
                out.println(componentTypeEncodingOutput)
                out.println("$END_MARKER\n")

                out.println("# Components")
                out.println(componentOutput)
                out.println("$END_MARKER\n")

                out.println("# Entities")
                out.println(entityOutput)
                out.println(END_MARKER)
            }
        }

        private enum class LoadPhase {
            LOAD_STORAGE_HANDLER_TYPE_ENCODINGS,
            LOAD_COMPONENT_TYPE_ENCODINGS,
            LOAD_COMPONENTS,
            LOAD_ENTITIES,
            LOAD_DONE
        }

        private fun classFromName(name: String) = when (name) {
            Byte::class.qualifiedName!! -> Byte::class
            Short::class.qualifiedName!! -> Short::class
            Int::class.qualifiedName!! -> Int::class
            Long::class.qualifiedName!! -> Long::class
            Float::class.qualifiedName!! -> Float::class
            Double::class.qualifiedName!! -> Double::class
            Boolean::class.qualifiedName!! -> Boolean::class
            Char::class.qualifiedName!! -> Char::class
            String::class.qualifiedName!! -> String::class
            else -> Class.forName(name).kotlin
        }

        private fun loadType(line: String, map: MutableMap<String, KClass<*>>) = line.split('=').let { items ->
            check(items.size == 2) { "Invalid line format: '$line'" }
            check("^[A-Za-z0-9]+$".toRegex().matches(items[0])) { "Invalid type encoding: '${items[0]}'" }
            check("^[A-Za-z0-9.\$_]+\$".toRegex().matches(items[1])) { "Invalid class name: '${items[1]}'" }
            map[items[0]] = classFromName(items[1])
        }

        private fun loadComponent(
            line: String,
            components: MutableMap<Long, Component>,
            componentTypeEncodings: Map<String, KClass<*>>,
            storageHandlerTypeEncodingTranslation: Map<String, String>,
            registryKits: Array<out RegistryKit>
        ) {
            val items = pipePattern.split(line)

            // Component ID
            val id = items[0].toLong()

            // Component type
            val type = componentTypeEncodings[items[1]]!!

            // Component constructor arguments
            val args = items.subList(2, items.size).associate { p ->
                equalsPattern.split(p).let { arr ->
                    arr[0] to deserialize(
                        arr[1],
                        storageHandlerTypeEncodingTranslation,
                        *registryKits
                    )
                }
            }

            components[id] = type.constructors.first().let { c ->
                c.callBy(buildMap {
                    c.parameters.forEach { kp ->
                        if (kp.name == null) return@forEach
                        check(kp.isOptional || args.containsKey(kp.name)) {
                            "No value for required parameter: '${kp.name}'"
                        }
                        put(kp, args[kp.name])
                    }
                })
            } as Component
        }

        private fun loadEntity(
            line: String,
            entities: MutableList<Entity>,
            components: Map<Long, Component>
        ) {
            val (_, encodedTransform, componentIDs) = delimiterPattern.split(line)

            entities.add(
                Entity( // Load components
                    *componentIDs.split(',').map { id -> components[id.toLong()]!! }.toTypedArray()
                ).also { e -> e.transform.run { // Load transform
                    encodedTransform.split(',').let { arr ->
                        px = arr[0].toFloat(); py = arr[1].toFloat(); pz = arr[2].toFloat()
                        sx = arr[3].toFloat(); sy = arr[4].toFloat(); sz = arr[5].toFloat()
                        orientation = Quaternion(arr[6].toFloat(), arr[7].toFloat(), arr[8].toFloat(), arr[9].toFloat())
                    }
                }}
            )
        }

        fun load(name: String, scene: Scene, vararg registryKits: RegistryKit) {

            if (!scene.empty) TODO("Implement support for non-empty scene updates")

            val storageHandlerTypeEncodings = mutableMapOf<String, KClass<*>>()
            var storageHandlerTypeEncodingTranslation: Map<String, String>? = null
            val componentTypeEncodings = mutableMapOf<String, KClass<*>>()
            val components = mutableMapOf<Long, Component>()
            val entities = mutableListOf<Entity>()

            var phase = LoadPhase.LOAD_STORAGE_HANDLER_TYPE_ENCODINGS

            File(getSaveFilePath(name)).also { f ->
                require(f.exists()) { "No saved scene with name '$name'" }
            }.forEachLine { line ->

                // Escape empty lines or comments
                if (line.startsWith('#') || line.isEmpty()) return@forEachLine

                // Change load phase if applicable
                if (line == END_MARKER) { phase = LoadPhase.values()[phase.ordinal + 1]; return@forEachLine }

                // Phase dependent line handling
                when (phase) {
                    LoadPhase.LOAD_STORAGE_HANDLER_TYPE_ENCODINGS -> loadType(line, storageHandlerTypeEncodings)
                    LoadPhase.LOAD_COMPONENT_TYPE_ENCODINGS -> loadType(line, componentTypeEncodings)
                    LoadPhase.LOAD_COMPONENTS -> {

                        if (storageHandlerTypeEncodingTranslation == null) {
                            storageHandlerTypeEncodingTranslation =
                                storageHandlerTypeEncodings.entries.associate { (typeEncoding, type) ->
                                    typeEncoding to SceneSaver.storageHandlerTypeEncodings[type]!!
                                }
                        }

                        loadComponent(
                            line, components, componentTypeEncodings, storageHandlerTypeEncodingTranslation!!, registryKits
                        )
                    }
                    LoadPhase.LOAD_ENTITIES -> loadEntity(line, entities, components)
                    LoadPhase.LOAD_DONE -> Unit
                }
            }

            // Add entities to scene
            entities.forEach(scene::add)
        }
    }
}