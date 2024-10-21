package org.techjobs.techforall.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class TechJobsDbConfig {
    @Bean(name = ["techJobsDataSourceProperties"])
    @ConfigurationProperties("spring.datasource.main")
    fun techJobsDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean(name =  ["techJobsDataSource"])
    fun techJobsDataSource(@Qualifier("techJobsDataSourceProperties")properties: DataSourceProperties): DataSource {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = ["techJobsJdbcTemplate"])
        fun techJobsJdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(techJobsDataSource(techJobsDataSourceProperties()))
    }
}