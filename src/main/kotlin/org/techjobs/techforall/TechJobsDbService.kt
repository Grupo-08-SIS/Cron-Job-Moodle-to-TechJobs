package org.techjobs.techforall

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.techjobs.techforall.config.TechJobsDbConfig
import org.techjobs.techforall.dto.CursoAlunoDto
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
            val cursoExistente = jdbcTemplateTechJobs.queryForList(
                "SELECT * FROM curso_moodle WHERE id = ? OR nome = ?",
                curso.id, curso.nome
            ).isNotEmpty()

            if (!cursoExistente) {
                val sqlInserirCurso = """
                INSERT INTO curso_moodle (id, nome, categorias)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                nome = VALUES(nome),
                categorias = VALUES(categorias)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirCurso,
                    curso.id, curso.nome, curso.categorias
                )
            }
        }
    }

    fun cadastrarCursosAlunos(cursosAlunos: List<CursoAlunoDto>) {
        cursosAlunos.forEach { cursoAluno ->

            val alunoId = jdbcTemplateTechJobs.queryForObject(
                """
            SELECT a.id 
            FROM aluno a
            JOIN usuario u ON u.id = a.id
            WHERE u.email = ?
            """,
                Long::class.java,
                cursoAluno.alunoEmail
            )

            if (alunoId == null) {
                return@forEach
            }

            val cursoAlunoExistente = jdbcTemplateTechJobs.queryForList(
                """
            SELECT * 
            FROM curso_aluno 
            WHERE curso_id_moodle = ? AND aluno_id = ?
            """,
                cursoAluno.cursoId, alunoId
            ).isNotEmpty()

            if (!cursoAlunoExistente) {

                val sqlInserirCursoAluno = """
            INSERT INTO curso_aluno (nome, curso_id_moodle, aluno_id, aluno_email, total_atividades, total_atividades_do_aluno)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                total_atividades = VALUES(total_atividades),
                total_atividades_do_aluno = VALUES(total_atividades_do_aluno)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirCursoAluno,
                    cursoAluno.cursoNome,
                    cursoAluno.cursoId,
                    alunoId,
                    cursoAluno.alunoEmail,
                    cursoAluno.totalAtividades,
                    cursoAluno.totalAtividadesFeitas
                )
            } else {

                val sqlAtualizarCursoAluno = """
            UPDATE curso_aluno
            SET 
                total_atividades = ?,
                total_atividades_do_aluno = ?
            WHERE curso_id_moodle = ? AND aluno_id = ?
            """
                jdbcTemplateTechJobs.update(
                    sqlAtualizarCursoAluno,
                    cursoAluno.totalAtividades,
                    cursoAluno.totalAtividadesFeitas,
                    cursoAluno.cursoId,
                    alunoId
                )
            }
        }
    }



    fun cadastrarPontuacoes(pontuacoes: List<PontuacaoMoodleDto>) {
        pontuacoes.forEach { pontuacao ->
            // Buscar o aluno_id através do email na tabela usuario
            val alunoId = jdbcTemplateTechJobs.queryForObject(
                """
            SELECT a.id 
            FROM aluno a
            JOIN usuario u ON u.id = a.id
            WHERE u.email = ?
        """,
                Long::class.java,
                pontuacao.alunoEmail
            )

            if (alunoId == null) {
                return@forEach
            }

            // Verifica se a pontuação já existe
            val pontuacaoExistente = jdbcTemplateTechJobs.queryForList(
                "SELECT * FROM pontuacao WHERE aluno_id = ? AND curso_id = ? AND nome_atividade = ?",
                alunoId, pontuacao.cursoId, pontuacao.nomeAtividade
            ).isNotEmpty()

            if (pontuacaoExistente) {
                // Se a pontuação já existir, atualiza os dados
                val sqlAtualizarPontuacao = """
                UPDATE pontuacao
                SET
                    data_entrega = ?, 
                    nota_aluno = ?
                WHERE aluno_id = ? AND curso_id = ? AND nome_atividade = ?
            """
                jdbcTemplateTechJobs.update(
                    sqlAtualizarPontuacao,
                    pontuacao.dataEntrega,
                    pontuacao.notaAluno,
                    alunoId,
                    pontuacao.cursoId,
                    pontuacao.nomeAtividade
                )
            } else {
                // Se não existir a pontuação, insere no banco
                val sqlInserirPontuacao = """
                INSERT INTO pontuacao (aluno_id, aluno_email, curso_id, curso_nome, data_entrega, nome_atividade, nota_atividade, nota_aluno)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    data_entrega = VALUES(data_entrega),
                    nota_aluno = VALUES(nota_aluno)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirPontuacao,
                    alunoId,
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
                INSERT INTO tempo_sessao (aluno_id, dia_sessao, qtd_tempo_sessao)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                qtd_tempo_sessao = VALUES(qtd_tempo_sessao)
            """
                jdbcTemplateTechJobs.update(
                    sqlInserirSessao,
                    tempo.alunoId, tempo.diaSessao, tempo.qtdTempoSessao
                )
            }
        }
    }
}