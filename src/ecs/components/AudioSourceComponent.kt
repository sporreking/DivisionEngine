package ecs.components

import ecs.Component
import resources.AudioSource

data class AudioSourceComponent(
    val source: AudioSource = AudioSource()
) : Component()