package com.techone.config;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConstraintCleaner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Drop unique constraint on 'name' column in 'categories' table
        dropUniqueConstraint("categories", "name");

        // Drop unique constraint on 'slug' column in 'categories' table
        dropUniqueConstraint("categories", "slug");
    }

    private void dropUniqueConstraint(String tableName, String columnName) {
        try {
            // SQL Server specific query to find constraint name
            String sqlCheck = "SELECT tc.CONSTRAINT_NAME " +
                    "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc " +
                    "JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME " +
                    "WHERE tc.CONSTRAINT_TYPE = 'UNIQUE' " +
                    "AND tc.TABLE_NAME = ? " +
                    "AND ccu.COLUMN_NAME = ?";

            List<Map<String, Object>> constraints = jdbcTemplate.queryForList(sqlCheck, tableName, columnName);

            for (Map<String, Object> constraint : constraints) {
                String constraintName = (String) constraint.get("CONSTRAINT_NAME");
                System.out
                        .println("Found unique constraint on " + tableName + "." + columnName + ": " + constraintName);

                String sqlDrop = "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName;
                jdbcTemplate.execute(sqlDrop);
                System.out.println("Dropped constraint: " + constraintName);
            }
        } catch (Exception e) {
            System.err.println("Error dropping constraint on " + tableName + "." + columnName + ": " + e.getMessage());
        }
    }
}
