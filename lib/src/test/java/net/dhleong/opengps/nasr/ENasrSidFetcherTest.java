package net.dhleong.opengps.nasr;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class ENasrSidFetcherTest {

    private ENasrSidFetcher fetcher;

    @Before
    public void setUp() {
        fetcher = new ENasrSidFetcher();
    }

    @Test
    public void test() {
        assertThat(fetcher.departureProcedureInfo("LGA5.LGA").toBlocking().first())
            .contains("62011");
    }
}