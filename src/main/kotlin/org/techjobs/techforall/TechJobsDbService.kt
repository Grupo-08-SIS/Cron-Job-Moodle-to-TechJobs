package org.techjobs.techforall

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.techjobs.techforall.config.TechJobsDbConfig
import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto

@Service
class TechJobsDbService(
    @Qualifier("techJobsJdbcTemplate")
    private val jdbcTemplateTechJobs: JdbcTemplate
) {

    fun cadastrarCursos(cursos: List<CursoMoodleDto>) {

        cursos.forEach { curso ->

            val categoriaExistente = jdbcTemplateTechJobs.queryForObject(
                "SELECT id FROM categoria WHERE nome = ?",
                arrayOf(curso.categoria),
                Long::class.java
            )

            val categoriaId = categoriaExistente ?: run {
                val sqlInserirCategoria = "INSERT INTO categoria (nome) VALUES (?)"
                jdbcTemplateTechJobs.update(sqlInserirCategoria, curso.categoria)
                jdbcTemplateTechJobs.queryForObject(
                    "SELECT LAST_INSERT_ID()",
                    Long::class.java
                )
            }

            val sqlInserirCurso = """
            INSERT INTO curso_moodle (id, nome)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
            nome = VALUES(nome)
        """

            val cursoExistente = jdbcTemplateTechJobs.queryForList(
                "SELECT * FROM curso_moodle WHERE id = ? AND nome = ?",
                curso.id, curso.nome
            ).isEmpty()

            if (cursoExistente) {
                val sqlInserirCurso = """
                INSERT INTO curso_moodle (id, nome)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                nome = VALUES(nome)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirCurso,
                    curso.id, curso.nome
                )
            }

            val relacaoExistente = jdbcTemplateTechJobs.queryForObject(
                "SELECT COUNT(*) FROM curso_categoria WHERE curso_id = ? AND categoria_id = ?",
                arrayOf(curso.id, categoriaId),
                Int::class.java
            )

            if (relacaoExistente == 0) {
                val sqlInserirRelacao = """
                INSERT INTO curso_categoria (curso_id, categoria_id)
                VALUES (?, ?)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirRelacao,
                    curso.id, categoriaId
                )
            }

            // Inserir a relação ManyToMany na tabela curso_categoria
//            val sqlInserirRelacao = """
//            INSERT INTO curso_categoria (curso_id, categoria_id)
//            VALUES (?, ?)
//        """
//            jdbcTemplateTechJobs.update(
//                sqlInserirRelacao,
//                curso.id, categoriaId
//            )
        }
    }

    fun cadastrarPontuacoes(pontuacoes: List<PontuacaoMoodleDto>) {

        pontuacoes.forEach { pontuacao ->
            val pontuacaoExistente = jdbcTemplateTechJobs.queryForList(
                "SELECT * FROM pontuacao WHERE aluno_id = ? AND curso_id = ? AND nome_atividade = ?",
                pontuacao.alunoId, pontuacao.cursoId, pontuacao.nomeAtividade
            ).isEmpty()

            if (pontuacaoExistente) {
                val sqlInserirPontuacao = """
                INSERT INTO pontuacao (aluno_id, aluno_email, curso_id, curso_nome, data_entrega, nome_atividade, nota_atividade, nota_aluno)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                data_entrega = VALUES(data_entrega),
                nota_aluno = VALUES(nota_aluno)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirPontuacao,
                    pontuacao.alunoId,
                    pontuacao.alunoEmail,
                    pontuacao.cursoId,
                    pontuacao.cursoNome,
                    pontuacao.dataEntrega,
                    pontuacao.nomeAtividade,
                    pontuacao.notaAtividade,
                    pontuacao.notaAluno
                )
            }
        }
    }

    fun cadastrarTemposSessao(temposSessao: List<TempoSessaoMoodleDto>) {

        temposSessao.forEach { tempo ->
            val sessaoExistente = jdbcTemplateTechJobs.queryForList(
                "SELECT * FROM tempo_sessao WHERE aluno_id = ? AND dia_sessao = ?",
                tempo.alunoId, tempo.diaSessao
            ).isEmpty()

            if (sessaoExistente) {
                val sqlInserirSessao = """
                INSERT INTO tempo_sessao (aluno_id, aluno_email, dia_sessao, qtd_tempo_sessao)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                qtd_tempo_sessao = VALUES(qtd_tempo_sessao)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirSessao,
                    tempo.alunoId, tempo.alunoEmail, tempo.diaSessao, tempo.qtdTempoSessao
                )
            }
        }
    }
}