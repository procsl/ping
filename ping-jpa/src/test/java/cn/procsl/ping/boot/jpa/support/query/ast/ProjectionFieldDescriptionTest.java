package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ProjectionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectionFieldDescriptionTest {

    private static final Logger log = LoggerFactory.getLogger(ProjectionFieldDescriptionTest.class);

    static class SampleProjection {
        @Setter
        @Getter
        private String name;
        private int age;

        public boolean isAdult() {
            return age >= 18;
        }

    }

    @Test
    void testGetParentField() {
        ProjectionFieldDescription parser = new ProjectionFieldDescription("name", SampleProjection.class);
        assertEquals(Optional.empty(), parser.getParentField(), "Parent field should be empty");
    }

    @Test
    void testGetFieldName() {
        ProjectionFieldDescription parser = new ProjectionFieldDescription("name", SampleProjection.class);
        assertEquals("name", parser.getFieldName(), "Field name should be 'name'");
    }

    @Test
    void testGetType() {
        ProjectionFieldDescription parser = new ProjectionFieldDescription("name", SampleProjection.class);
        assertEquals(SampleProjection.class, parser.getType(), "Type should be SampleProjection.class");
    }

    @Test
    void testGetChildren() {
        ProjectionFieldDescription parser = new ProjectionFieldDescription("name", SampleProjection.class);
        List<FieldDescription> children = parser.getChildren();
        assertEquals(3, children.size(), "Should find 3 child fields");
    }

    @Test
    void testGetAnnotations() {
        ProjectionFieldDescription parser = new ProjectionFieldDescription("name", SampleProjection.class);
        Annotation[] annotations = SampleProjection.class.getAnnotations();
        List<Annotation> parserAnnotations = parser.getAnnotations();

        assertEquals(annotations.length, parserAnnotations.size(), "Annotations size should match");
    }

    @Test
    void print() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        ProjectionFieldDescription parser = new ProjectionFieldDescription(null, ProjectionDTO.class);
        String res = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parser);

        res = getString(res, "(");
        res = getString(res, ")");
        res = getString(res, "\"");
        res = getString(res, "\\[");
        res = getString(res, "\\]");
        res = getString(res, ",");
        res = getString(res, "{");
        res = getString(res, "}");

        log.info("结果: {}", res);
    }

    private static String getString(String res, String key) {
        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";
        String BOLD = "\u001B[1m";
        String BLUE = "\u001B[34m";
        res = res.replace(key, ANSI_RED + BOLD + key + ANSI_RESET);
        return res;
    }

}
