package resources

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Float4
import org.lwjgl.opengl.GL30.*

/** Represents data for an attribute. See [validTypes]. */
class AttributeData<T : Any>(
    /** The vertex attribute location of this particular data. See "`ATTLOC_`" constants within [ShaderProgram]. */
    val attributeLocation: Int,

    /** A list with data. Each element must be of same type. Make sure that the type is valid. See [validTypes]. */
    private val data: List<T>,

    /** Must be set to true if the [data] should be mutable. */
    val mutable: Boolean = false
) {

    companion object {
        /** List of valid data types. */
        val validTypes = listOf(
            Float, Float2::class, Float3::class, Float4::class
        )
    }

    init {
        // Sanity check
        require(data.isEmpty() || data.distinctBy { v -> v::class }.count() == 1) { "All elements in data must be of same type."}
        require(data.all { v -> validTypes.contains(v::class) }) { "Invalid data type." }
    }


    /**
     * The number of elements in each vector. Determined by the type of data that was provided upon construction.
     * Note that if there is no data, this value is equal to -1.
     */
    val vectorLength = when (data.firstOrNull()?.let { it::class }) {
        Float::class -> 1
        Float2::class -> 2
        Float3::class -> 3
        Float4::class -> 4
        else -> -1
    }

    /** The number of vectors (or scalars if data type was `Float`) used to represent this data. */
    val numVectors get() = data.size

    /** The float representation of the contained data. */
    val floats get() = buildList {
        data.forEach { v ->
            when (v::class) {
                Float::class -> add(v as Float)
                Float2::class -> with(v as Float2) { addAll(listOf(x, y)) }
                Float3::class -> with(v as Float3) { addAll(listOf(x, y, z)) }
                Float4::class -> with(v as Float4) { addAll(listOf(x, y, z, w)) }
            }
        }
    }.toFloatArray()
}

/** Represents some vertex data on the GPU. */
class VertexBufferObject(
    /** The data to use for this VBO. Sent to the GPU upon construction. */
    data: AttributeData<*>
) {

    /** The OpenGL handle of this vertex buffer object. */
    val handle = glGenBuffers()

    init {
        // Bind and send data
        glBindBuffer(GL_ARRAY_BUFFER, handle)
        if (data.numVectors > 0)
            glBufferData(GL_ARRAY_BUFFER, data.floats, if (data.mutable) GL_DYNAMIC_DRAW else GL_STATIC_DRAW)
    }

    /** The number of relevant floats contained by this VBO. Note that more memory may be allocated (see [capacity]). */
    var size: Int = data.numVectors * data.vectorLength
        private set

    /** The maximum number of floats that may be contained within current GPU memory allocation. */
    var capacity: Int = data.numVectors * data.vectorLength
        private set

    /** The number of elements contained by each data vector. */
    var vectorLength = data.vectorLength
        private set

    /** The number of relevant vertices contained by this VBO. */
    val numVertices get() = size / vectorLength

    /** True if the data is mutable. */
    val mutable: Boolean = data.mutable

    /**
     * Replaces the data contained by this VBO with new [data]. Note that the [vectorLength] of the new data must be the
     * same as the [vectorLength][VertexBufferObject.vectorLength] of the old data, and that the VBO must be [mutable].
     */
    fun replace(data: FloatArray, vectorLength: Int) {
        check(mutable) { "Cannot replace non-mutable data!" }

        if (this.vectorLength == -1) this.vectorLength = vectorLength
        else require(vectorLength == this.vectorLength) {
            "The replacing data must be of same type as the old data!"
        }

        bind()
        if (data.size <= capacity) glBufferSubData(GL_ARRAY_BUFFER, 0, data)
        else { glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW); capacity = data.size }
        size = data.size
    }

    /** Binds this vertex buffer object. */
    fun bind() = glBindBuffer(GL_ARRAY_BUFFER, handle)
}

/** Represents some index data on the GPU. */
class IndexBufferObject(
    /** The indices to use. Sent to the GPU upon construction. */
    data: List<Int>,

    /** Must be set to true if the [data] should be mutable. */
    val mutable: Boolean = false
) {

    /** The OpenGL handle of this index buffer object. */
    val handle = glGenBuffers()

    init {
        // Bind and send data
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle)
        if (data.isNotEmpty())
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, data.toIntArray(), if (mutable) GL_DYNAMIC_DRAW else GL_STATIC_DRAW)
    }

    /**
     * The number of indices that are currently contained by this IBO.
     * Note that more memory may be allocated (see [capacity]).
     */
    var numIndices = data.size
        private set

    /** The maximum number of indices that may be contained within current GPU memory allocation. */
    var capacity = data.size

    /** Replaces the indices contained by this VBO with the new [data]. Note that the IBO must be [mutable]. */
    fun replace(data: IntArray) {
        check(mutable) { "Cannot replace non-mutable indices!" }

        bind()
        if (data.size <= capacity) glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, data)
        else { glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW); capacity = data.size }
        numIndices = data.size
    }

    /** Binds this index buffer object. */
    fun bind() = glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle)
}

/** Represents a mesh. */
class Mesh(
    /** The data to use for each desired attribute. */
    data: List<AttributeData<*>>,

    /** A list of indices to use for the index buffer object. */
    indices: List<Int>? = null,

    /** Must be set to true if the [indices] should be mutable. Does nothing if [indices] is null. */
    val mutableIndices: Boolean = false,

    /** The primitive mode. Defaults to GL_TRIANGLES. */
    val mode: Int = GL_TRIANGLES
) {

    companion object {
        /** The default quad mesh. */
        val QUAD = Mesh(listOf(
            AttributeData(ShaderProgram.ATTLOC_POSITION, listOf(
                Float3(-.5f, -.5f, .0f),
                Float3(-.5f, .5f, .0f),
                Float3(.5f, .5f, .0f),
                Float3(-.5f, -.5f, .0f),
                Float3(.5f, .5f, .0f),
                Float3(.5f, -.5f, .0f)
            )),
            AttributeData(ShaderProgram.ATTLOC_TEX_COORDS, listOf(
                Float2(.0f, .0f),
                Float2(.0f, 1.0f),
                Float2(1.0f, 1.0f),
                Float2(.0f, .0f),
                Float2(1.0f, 1.0f),
                Float2(1.0f, .0f)
            ))
        ))
    }

    /** The OpenGL handle of this mesh (vertex array object). */
    val handle = glGenVertexArrays()

    init {
        // Sanity check
        require(data.distinctBy { ad -> ad.attributeLocation }.count() == data.size)
            { "Cannot bind multiple VBOs to the same attribute location." }

        // Bind VAO
        glBindVertexArray(handle)
    }

    /**
     * The vertex buffer objects contained by this mesh, mapped by their attribute location. There is precisely one VBO
     * for each item in the data list provided at construction.
     */
    val vertexBufferObjects = data.associate { d ->
        val vbo = VertexBufferObject(d)

        glEnableVertexAttribArray(d.attributeLocation)

        if (d.numVectors > 0)
            glVertexAttribPointer(d.attributeLocation, d.vectorLength,
                GL_FLOAT, false, 0, 0)

        d.attributeLocation to vbo
    }

    /**
     * Replaces the attribute data of the VBO associated with the specified
     * [data.attributeLocation][AttributeData.attributeLocation] with new [data]. Note that the
     * [mutable][AttributeData.mutable] field is ignored for the new [data].
     */
    fun replaceAttributeData(data: AttributeData<*>) = vertexBufferObjects[data.attributeLocation]?.also { vbo ->
        require(data.numVectors > 0) { "Cannot replace with empty data!" }
        if (vbo.mutable) {
            bind()
            vbo.bind()
            glVertexAttribPointer(
                data.attributeLocation, data.vectorLength, GL_FLOAT, false, 0, 0
            )
        }
    }?.replace(
        data.floats, data.vectorLength
    ) ?: throw IllegalArgumentException("The mesh has no data for attribute location ${data.attributeLocation}!")

    fun replaceIndices(data: List<Int>) = indexBufferObject?.replace(data.toIntArray())
        ?: throw IllegalStateException("The mesh has no IBO!")

    /** The index buffer object to use for this mesh, or null if no IBO is desired. */
    val indexBufferObject = indices?.let { IndexBufferObject(it, mutableIndices) }

    /** The number of indices contained by this mesh. */
    val numIndices get() = indexBufferObject?.numIndices ?: 0

    /** The number of vertices contained by this mesh. Determined by the first VBO's number of vertices. */
    val numVertices get() = vertexBufferObjects.values.firstOrNull()?.numVertices ?: 0
    /*when (mode) {
        GL_TRIANGLES ->
        GL_TRIANGLE_FAN -> ((vertexBufferObjects.values.firstOrNull()?.numVertices ?: 2) - 2) * 3 + 1
        else -> 0
    }*/

    // Unbind VAO
    init { glBindVertexArray(0) }


    /** Binds this mesh (vertex array object). */
    fun bind() = glBindVertexArray(handle)
}