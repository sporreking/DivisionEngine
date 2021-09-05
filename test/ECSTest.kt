import ecs.Component
import ecs.ECSystem
import ecs.Entity
import ecs.Scene
import org.junit.Test


class ECSTest {

    private data class TestComponent(var x: Double) : Component()

    private class TestSystem : ECSystem() {
        override fun update(scene: Scene) = scene.getComponents<TestComponent>().values.forEach { c -> c.x += 1.0 }
        override fun render(scene: Scene) {}
    }

    @Test
    fun test() {
        val c1 = TestComponent(.1)
        val c2 = TestComponent(.2)

        val e1 = Entity(c1)

        println("Entity ID: ${e1.id}")
        println("Component ID: ${e1.get<TestComponent>()!!.id}")

        println("Component Parent ID: ${e1.get<TestComponent>()!!.parent?.id}")

        val s = Scene(
            TestSystem()
        ).apply {
            add(e1)
        }

        println("Scene: ${s.hashCode()}")
        println("Scene of Entity: ${e1.scene?.hashCode()}")

        e1.add(c2)

        println("New Component ID: ${c2.id}")
        println("New Component Parent: ${c2.parent?.id}")

        println("Components on entity:")
        e1.forEach(::println)

        s.update()

        println("Components on entity after scene update:")
        e1.getAll<TestComponent>().forEach(::println)

        e1.remove(c2)

        println("Components on entity after removal of c2:")
        e1.forEach(::println)

        s.update()

        println("(c1, c2) after another scene update: ($c1, $c2)")
    }
}