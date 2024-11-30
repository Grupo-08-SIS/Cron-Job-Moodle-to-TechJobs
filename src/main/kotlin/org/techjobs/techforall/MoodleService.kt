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
class MoodleService(
    @Qualifier("moodleJdbcTemplate") private val jdbcTemplateMoodle: JdbcTemplate
) {

    fun buscarCursos(): List<CursoMoodleDto> {
        val sql = """
        SELECT
    mdl_course.id AS id_curso,
    mdl_course.fullname AS nome_curso,
    categoria.name AS curso_categoria,
    COUNT(DISTINCT atividade.id) AS total_atividades, 
    COUNT(DISTINCT nota.userid) AS total_alunos_com_notas
FROM
    mdl_course
JOIN
    mdl_grade_items atividade ON atividade.courseid = mdl_course.id
JOIN 
    mdl_course_categories AS categoria ON categoria.id = mdl_course.category
LEFT JOIN
    mdl_grade_grades nota ON nota.itemid = atividade.id
WHERE  
    atividade.itemmodule != 'course' 
GROUP BY
    mdl_course.id, mdl_course.fullname, categoria.name;
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
            aluno.email AS aluno_email,
            curso.fullname AS curso_nome,
            curso.id AS curso_id,
            item.itemname AS nome_atividade,
            COALESCE(nota.finalgrade, NULL) AS nota_aluno, 
            item.grademax AS nota_atividade, 
            CASE 
                WHEN nota.timemodified IS NOT NULL THEN FROM_UNIXTIME(nota.timemodified)
                ELSE NULL
            END AS data_entrega
        FROM 
            mdl_course AS curso
        JOIN 
            mdl_context AS contexto ON contexto.instanceid = curso.id AND contexto.contextlevel = 50
        JOIN 
            mdl_role_assignments AS atribuicao ON atribuicao.contextid = contexto.id
        JOIN 
            mdl_user AS aluno ON aluno.id = atribuicao.userid
        JOIN 
            mdl_role AS papel ON papel.id = atribuicao.roleid AND papel.shortname = 'student'
        JOIN 
            mdl_grade_items AS item ON item.courseid = curso.id AND item.itemtype = 'mod'
        LEFT JOIN 
            mdl_grade_grades AS nota ON nota.itemid = item.id AND nota.userid = aluno.id
        ORDER BY 
            aluno.email, curso.fullname, item.itemname;
    """

        // Recupera as pontuações dos alunos
        val pontuacoes = jdbcTemplateMoodle.query(sql) { rs, _ ->
            val dataEntrega = rs.getString("data_entrega")
            val nomeAtividade = rs.getString("nome_atividade")
            val notaAtividade = rs.getDouble("nota_atividade")
            val notaAluno = rs.getDouble("nota_aluno")

            // Cria e retorna o DTO da pontuação
            PontuacaoMoodleDto(
                alunoId = rs.getLong("aluno_id"),
                alunoEmail = rs.getString("aluno_email"),
                cursoNome = rs.getString("curso_nome"),
                dataEntrega = dataEntrega,
                nomeAtividade = nomeAtividade,
                notaAtividade = notaAtividade,
                cursoId = rs.getLong("curso_id"),
                notaAluno = notaAluno
            )
        }


        return pontuacoes
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