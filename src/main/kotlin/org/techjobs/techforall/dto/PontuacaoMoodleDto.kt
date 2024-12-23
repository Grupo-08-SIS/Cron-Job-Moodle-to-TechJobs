package org.techjobs.techforall.dto

data class PontuacaoMoodleDto(
    val alunoId: Long,
    val alunoEmail: String,
    val cursoNome: String,
    val cursoId: Long,
    val dataCriacao: String?,
    val dataEntrega: String?,
    val nomeAtividade: String,
    val notaAtividade: Double,
    val notaAluno: Double?
)