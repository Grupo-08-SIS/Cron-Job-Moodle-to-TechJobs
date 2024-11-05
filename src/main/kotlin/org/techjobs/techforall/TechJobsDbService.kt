package org.techjobs.techforall

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
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
            val categoriaExistente = try {
                jdbcTemplateTechJobs.queryForObject(
                    "SELECT id FROM categoria WHERE nome = ?",
                    arrayOf(curso.categoria),
                    Long::class.java
                )
            } catch (e: EmptyResultDataAccessException) {
                null
            }

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
            jdbcTemplateTechJobs.update(
                sqlInserirCurso,
                curso.id, curso.nome
            )

            val sqlInserirRelacao = """
            INSERT INTO curso_categoria (curso_id, categoria_id)
            VALUES (?, ?)
        """
            jdbcTemplateTechJobs.update(
                sqlInserirRelacao,
                curso.id, categoriaId
            )

            val sqlInserirCursoPontos = """
            INSERT INTO curso (id, nome, total_atividades, total_atividades_do_aluno)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            nome = VALUES(nome),
            total_atividades = VALUES(total_atividades),
            total_atividades_do_aluno = VALUES(total_atividades_do_aluno)
        """
            jdbcTemplateTechJobs.update(
                sqlInserirCursoPontos,
                curso.id, curso.nome, curso.totalAtividades, curso.totalAtividadesDoAluno
            )
        }
    }

    fun cadastrarPontuacoes(pontuacoes: List<PontuacaoMoodleDto>) {
        pontuacoes.forEach { pontuacao ->

            val cursoExistente = try {
                jdbcTemplateTechJobs.queryForObject(
                    "SELECT id FROM curso WHERE id = ?",
                    arrayOf(pontuacao.cursoId),
                    Long::class.java
                )
            } catch (e: EmptyResultDataAccessException) {
                null
            }

            // If curso_id does not exist, insert it
            if (cursoExistente == null) {
                val sqlInserirCurso = """
                INSERT INTO curso (id, nome, total_atividades, total_atividades_do_aluno)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                nome = VALUES(nome),
                total_atividades = VALUES(total_atividades),
                total_atividades_do_aluno = VALUES(total_atividades_do_aluno)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirCurso,
                    pontuacao.cursoId, pontuacao.cursoNome, 0, 0
                )
            }

            val alunoExistente = try {
                jdbcTemplateTechJobs.queryForObject(
                    "SELECT id FROM aluno WHERE id = ?",
                    arrayOf(pontuacao.alunoId),
                    Long::class.java
                )
            } catch (e: EmptyResultDataAccessException) {
                null
            }


            val pontuacaoExistente = try {
                jdbcTemplateTechJobs.queryForObject(
                    "SELECT 1 FROM pontuacao WHERE aluno_id = ? AND curso_id = ? AND nome_atividade = ?",
                    arrayOf(pontuacao.alunoId, pontuacao.cursoId, pontuacao.nomeAtividade),
                    Int::class.java
                )
            } catch (e: EmptyResultDataAccessException) {
                null
            }

            if (pontuacaoExistente == null) {
                val sqlInserirPontuacao = """
                INSERT INTO pontuacao (aluno_id, aluno_email, curso_id, curso_nome, data_entrega, nome_atividade, nota_atividade, nota_aluno)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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