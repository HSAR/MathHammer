package io.hsar.mathhammer.fortyk.model

data class AttackGroup(val modelName: String, val pointsCost: Int, val attackProfiles: Set<AttackProfile>)