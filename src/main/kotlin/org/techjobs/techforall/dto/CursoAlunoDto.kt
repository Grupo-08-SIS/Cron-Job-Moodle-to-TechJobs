package org.techjobs.techforall.dto

data class CursoAlunoDto(
    val alunoId: Long,
    val alunoEmail: String,
    val cursoId: Long,
    val cursoNome: String,
    val totalAtividades: Int,
    val totalAtividadesFeitas: Int
)
