package org.techjobs.techforall

import org.techjobs.techforall.config.MoodleDbConfig
import org.techjobs.techforall.config.TechJobsDbConfig
import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class MoodleService (
    @Qualifier("moodleJdbcTemplate")
    private val jdbcTemplateMoodle: JdbcTemplate
) {

    fun buscarCursos(): List<CursoMoodleDto> {
        val sql = """
        SELECT
            mdl_course.id AS id_curso,
            mdl_course.fullname AS nome_curso,
            categoria.name AS curso_categoria,
            COUNT(atividade.id) AS total_atividades,
            COUNT(DISTINCT nota.userid) AS total_alunos_com_notas
        FROM
            mdl_course
        JOIN
            mdl_grade_items atividade ON atividade.courseid = mdl_course.id
        JOIN 
			mdl_course_categories AS categoria ON categoria.id =  mdl_course.category
        LEFT JOIN
            mdl_grade_grades nota ON nota.itemid = atividade.id
        GROUP BY
            mdl_course.id, mdl_course.fullname;
    """

        val cursos = jdbcTemplateMoodle.query(sql) { rs, _ ->
            CursoMoodleDto(
                id = rs.getLong("id_curso"),
                nome = rs.getString("nome_curso"),
                categoria = rs.getString("curso_categoria"),
                totalAtividades = rs.getInt("total_atividades"),
                totalAtividadesDoAluno = rs.getInt("total_alunos_com_notas")
            )
        }

        return cursos;
    }

    fun buscarPontuacoes(): List<PontuacaoMoodleDto> {
        val sql = """
        SELECT 
			aluno.id AS aluno_id, 
            aluno.	email AS aluno_email,
            curso.fullname AS curso_nome,
            curso.id AS curso_id,
            categoria.name AS curso_categoria,
            FROM_UNIXTIME(nota.timemodified) AS data_entrega, 
            item.itemname AS nome_atividade,
            nota.rawgrademax AS nota_atividade, 
            nota.finalgrade AS nota_aluno
        FROM 
            mdl_grade_grades AS nota
        JOIN 
            mdl_user AS aluno ON aluno.id = nota.userid
        JOIN 
            mdl_grade_items AS item ON item.id = nota.itemid
        JOIN 
            mdl_course AS curso ON curso.id = item.courseid
		JOIN 
			mdl_course_categories AS categoria ON categoria.id = curso.category;
    """
        return jdbcTemplateMoodle.query(sql) { rs, _ ->
            val dataEntrega = rs.getString("data_entrega")
            val nomeAtividade = rs.getString("nome_atividade")
            val notaAtividade = rs.getDouble("nota_atividade")
            val notaAluno = rs.getDouble("nota_aluno")

            // Verifica se a entrega foi realizada
            if (dataEntrega == null || nomeAtividade == null || notaAtividade == null || notaAluno == null) {
                // Retorna um objeto com informações sobre a não entrega
                return@query PontuacaoMoodleDto(
                    alunoId = rs.getLong("aluno_id"),
                    alunoEmail = rs.getString("aluno_email"),
                    cursoNome = rs.getString("curso_nome"),
                    dataEntrega = "Não entregue", // ou qualquer mensagem que faça sentido
                    nomeAtividade = nomeAtividade ?: "Atividade não informada",
                    notaAtividade = notaAtividade,
                    cursoId = rs.getLong("curso_id"),
                    cursoCategoria = rs.getString("curso_categoria"),
                    notaAluno = notaAluno
                )
            }

            // Caso contrário, retorna a pontuação normal
            PontuacaoMoodleDto(
                alunoId = rs.getLong("aluno_id"),
                alunoEmail = rs.getString("aluno_email"),
                cursoNome = rs.getString("curso_nome"),
                dataEntrega = dataEntrega,
                nomeAtividade = nomeAtividade,
                notaAtividade = notaAtividade,
                cursoId = rs.getLong("curso_id"),
                cursoCategoria = rs.getString("curso_categoria"),
                notaAluno = notaAluno
            )
        }
    }

    fun buscarTemposSessao(): List<TempoSessaoMoodleDto> {
        val sql = """
            SELECT 
                aluno.id AS aluno_id, 
                aluno.email AS aluno_email, 
                FROM_UNIXTIME(sessao.timecreated) AS dia_sessao, 
                (sessao.timemodified - sessao.timecreated) / 60 AS qtd_tempo_sessao -- duração em minutos
            FROM 
                mdl_sessions sessao
            JOIN 
                mdl_user aluno ON aluno.id = sessao.userid;
        """
        return jdbcTemplateMoodle.query(sql) { rs, _ ->
            TempoSessaoMoodleDto(
                alunoId = rs.getLong("aluno_id"),
                alunoEmail = rs.getString("aluno_email"),
                diaSessao = rs.getString("dia_sessao"),
                qtdTempoSessao = rs.getDouble("qtd_tempo_sessao")
            )
        }
    }

}