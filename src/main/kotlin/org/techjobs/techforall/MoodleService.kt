package org.techjobs.techforall

import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.techjobs.techforall.dto.CursoAlunoDto

@Service
class MoodleService(
    @Qualifier("moodleJdbcTemplate") private val jdbcTemplateMoodle: JdbcTemplate
) {

    fun buscarCursos(): List<CursoMoodleDto> {
        val sql = """
    SELECT 
        curso.id AS id_curso,
        curso.fullname AS nome_curso,
        GROUP_CONCAT(tag.name ORDER BY tag.name SEPARATOR ', ') AS curso_categoria
    FROM 
        mdl_course AS curso
    LEFT JOIN 
        mdl_tag_instance AS tag_instance ON tag_instance.itemid = curso.id
    LEFT JOIN 
        mdl_tag AS tag ON tag.id = tag_instance.tagid
    WHERE 
        curso.format = 'topics'
    GROUP BY 
        curso.id
    ORDER BY 
        curso.id;
"""
        return jdbcTemplateMoodle.query(sql) { rs, _ ->
            CursoMoodleDto(
                id = rs.getLong("id_curso"),
                nome = rs.getString("nome_curso") ?: "Nome não disponível",
                categorias = rs.getString("curso_categoria")
            )
        }
    }

    fun buscarCursosAlunos(): List<CursoAlunoDto> {
        val sql = """
            SELECT 
                aluno.id AS aluno_id, 
                aluno.email AS aluno_email,
                curso.id AS curso_id,
                curso.fullname AS curso_nome,
                COUNT(item.id) AS total_atividades, 
                SUM(CASE WHEN nota.timemodified IS NOT NULL THEN 1 ELSE 0 END) AS total_atividades_feitas
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
            WHERE 
                curso.format = 'topics'
            GROUP BY 
                aluno.id, aluno.email, curso.id, curso.fullname
            ORDER BY 
                aluno.email, curso.fullname
        """.trimIndent()

        return jdbcTemplateMoodle.query(sql) { rs, _ ->
            CursoAlunoDto(
                alunoId = rs.getLong("aluno_id"),
                alunoEmail = rs.getString("aluno_email"),
                cursoId = rs.getLong("curso_id"),
                cursoNome = rs.getString("curso_nome"),
                totalAtividades = rs.getInt("total_atividades"),
                totalAtividadesFeitas = rs.getInt("total_atividades_feitas")
            )
        }
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