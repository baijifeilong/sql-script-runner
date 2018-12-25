package io.github.baijifeilong.sql;


import com.zaxxer.hikari.HikariDataSource;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by BaiJiFeiLong@gmail.com at 2018/12/25 下午4:52
 */
public class ScriptRunnerTest {
    private static final String SQL = "DROP TABLE IF EXISTS user;\n" +
            "CREATE TABLE user (\n" +
            "  id                 INT PRIMARY KEY AUTO_INCREMENT,\n" +
            "  mobile             CHAR(11)    NOT NULL,\n" +
            "  encrypted_password VARCHAR(64) NOT NULL\n" +
            ");";

    @Test
    public void runScript() throws IOException, SQLException {
        String resourceRoot = ClassLoader.getSystemResource(".").getFile();
        String sqlFilename = resourceRoot + "/user.sql";
        Files.write(Paths.get(sqlFilename), SQL.getBytes());

        DataSource dataSource = new HikariDataSource() {{
            setJdbcUrl("jdbc:mysql://localhost/foo");
            setUsername("root");
            setPassword("root");
        }};

        new ScriptRunner(dataSource.getConnection(), false, true).runScript(new FileReader(sqlFilename));

        List<Map<String, Object>> maps = new JdbcTemplate(dataSource).queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = 'foo' AND TABLE_NAME = 'user'");
        Assertions.assertThat(maps).hasSize(3);

        System.out.println("Created table has columns:");
        maps.forEach(System.out::println);
    }
}