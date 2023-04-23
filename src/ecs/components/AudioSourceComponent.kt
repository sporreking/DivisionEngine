package ecs.components

import ecs.Component
import resources.AudioSource

/** Represents an audio source for emitting sounds. */
data class AudioSourceComponent(
    /** The audio source properties. */
    val source: AudioSource = AudioSource()
) : Component()