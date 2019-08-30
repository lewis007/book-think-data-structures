package hello.ch15;

import hello.ch07.WikiFetcher;
import hello.ch14.JedisIndex;
import hello.ch14.JedisMaker;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author downey
 */
public class WikiCrawlerTest {

    private Jedis jedis;
    private WikiCrawler wc;
    private JedisIndex index;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // make a WikiCrawler
        jedis = JedisMaker.make();
        index = new JedisIndex(jedis);
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        wc = new WikiCrawler(source, index);

        // for testing purposes, load up the queue
        WikiFetcher wf = new WikiFetcher();
        Elements paragraphs = wf.readWikipedia(source);
        wc.queueInternalLinks(paragraphs);
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        jedis.close();
    }

    /**
     * Test method for {@link WikiCrawler#crawl(boolean)}.
     *
     * @throws IOException
     */
    @Test
    public void testCrawl() throws IOException {
        String url1 = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        String url2 = "https://en.wikipedia.org/wiki/Programming_language";
        String url3 = "https://en.wikipedia.org/wiki/Concurrent_computing";

        String res = wc.crawl(true);
        assertThat(res.equals(url1), is(true));
        assertThat(wc.queueSize(), is(396));

        res = wc.crawl(true);
        assertThat(res.equals(url2), is(true));
        assertThat(wc.queueSize(), is(653));

        res = wc.crawl(true);
        assertThat(res.equals(url3), is(true));
        assertThat(wc.queueSize(), is(704));

        Map<String, Integer> map = index.getCounts("the");

        int count = map.get(url1);
        assertThat(count, is(339));

        count = map.get(url2);
        assertThat(count, is(264));

        count = map.get(url3);
        assertThat(count, is(53));
    }

}
