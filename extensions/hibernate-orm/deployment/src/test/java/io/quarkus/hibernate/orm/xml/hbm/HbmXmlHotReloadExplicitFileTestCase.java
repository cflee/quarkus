package io.quarkus.hibernate.orm.xml.hbm;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.hibernate.orm.SchemaUtil;
import io.quarkus.hibernate.orm.SmokeTestUtils;
import io.quarkus.hibernate.orm.TestTags;
import io.quarkus.test.QuarkusDevModeTest;

@Tag(TestTags.DEVMODE)
public class HbmXmlHotReloadExplicitFileTestCase {
    @RegisterExtension
    final static QuarkusDevModeTest TEST = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClass(SmokeTestUtils.class)
                    .addClass(SchemaUtil.class)
                    .addClass(NonAnnotatedEntity.class)
                    .addClass(HbmXmlHotReloadTestResource.class)
                    .addAsResource("application-mapping-files-my-hbm-xml.properties", "application.properties")
                    .addAsResource("META-INF/hbm-simple.xml", "my-hbm.xml"));

    @Test
    public void changeOrmXml() {
        assertThat(getColumnNames())
                .contains("thename")
                .doesNotContain("name", "thename2");

        TEST.modifyResourceFile("my-hbm.xml", s -> s.replace("<property name=\"name\" column=\"thename\"/>",
                "<property name=\"name\" column=\"thename2\"/>"));

        assertThat(getColumnNames())
                .contains("thename2")
                .doesNotContain("name", "thename");
    }

    private String[] getColumnNames() {
        return when().get("/hbm-xml-hot-reload-test/column-names")
                .then().extract().body().asString()
                .split("\n");
    }

}
