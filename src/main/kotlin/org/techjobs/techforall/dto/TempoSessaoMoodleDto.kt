package org.techjobs.techforall.dto

data class TempoSessaoMoodleDto(
    val alunoId: Long,
    val alunoEmail: String,
    val diaSessao: String,
    val qtdTempoSessao: Double
)