package org.techjobs.techforall.dto

data class CursoMoodleDto(
    val id: Long,
    val nome: String,
    val categoria: String,
    val totalAtividades: Int,
    val totalAtividadesDoAluno: Int
)