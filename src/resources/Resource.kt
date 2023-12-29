package resources

// TODO: Include name in abstraction?
abstract class Resource<L>(name: String? = null) {

    var name = name
        private set

    abstract fun load(loadInstruction: L)
}