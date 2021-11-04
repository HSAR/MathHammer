package io.hsar.mathhammer.model

data class AttackGroup(val modelName: String, val pointsCost: Int, val attackProfiles: Set<AttackProfile>)