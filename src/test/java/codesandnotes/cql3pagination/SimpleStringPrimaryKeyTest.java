package codesandnotes.cql3pagination;

import codesandnotes.Integration;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.TokenClause;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SimpleStringPrimaryKeyTest extends Integration {

    public static final String TABLE = "simple_string_pk";

    @Before
    public void _init() {
        embeddedCassandra.loadScripts("db/simple_string_pk.dml.cql");
    }

    @After
    public void _cleanup() {
        embeddedCassandra.clearTables();
    }

    @Test
    public void retrieve_5_ResultsOfPage_2_InAListOf_10_UsingCQL() {
        List<Row> page1 = embeddedCassandra.session().execute(
                QueryBuilder.select().from(CASSANDRA_KEYSPACE, TABLE).limit(5)
        ).all();
        print("page 1:", page1);

        String lastIdInPage1 = page1.get(4).getString("id");

        List<Row> page2 = embeddedCassandra.session().execute(
                "select * from cql3_pagination.simple_string_pk where token(id) > token('" + lastIdInPage1 + "') limit 5"
        ).all();
        print("page 2:", page2);

        Assert.assertEquals(5, page2.size());
        Assert.assertNotEquals(page2.get(0).getString("id"), page1.get(0).getString("id"));
    }

    @Test
    public void retrieve_5_ResultsOfPage_2_InAListOf_10_UsingQueryBuilder() {
        List<Row> page1 = embeddedCassandra.session().execute(
                QueryBuilder.select().from(CASSANDRA_KEYSPACE, TABLE).limit(5)
        ).all();
        print("page 1:", page1);

        String lastIdInPage1 = page1.get(4).getString("id");

        List<Row> page2 = embeddedCassandra.session().execute(
                QueryBuilder.select().from(CASSANDRA_KEYSPACE, TABLE)
                        .where(new TokenClause("id", ">", lastIdInPage1)).limit(5)
        ).all();
        print("page 2:", page2);

        Assert.assertEquals(5, page2.size());
        Assert.assertNotEquals(page2.get(0).getString("id"), page1.get(0).getString("id"));
    }

    private void print(String header, List<Row> rows) {
        System.out.println(header);
        for (Row row : rows) {
            System.out.println(row.getString("id") + " | " + row.getString("value"));
        }
    }
}
