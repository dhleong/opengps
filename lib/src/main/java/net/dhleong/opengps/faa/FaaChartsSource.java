package net.dhleong.opengps.faa;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.nasr.util.AiracCycle;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import rx.Observable;

/**
 * @author dhleong
 */
public class FaaChartsSource implements DataSource {

    private static final String URL_BASE = "http://aeronav.faa.gov/d-tpp/";

    private final File cacheDir;
    private final String xmlUrl;
    private final File xmlFile;
    private final String baseUrl;

    final HashMap<Airport, List<ChartInfo>> cache = new HashMap<>();

    public FaaChartsSource(File cacheDirectory) {
        this(cacheDirectory, AiracCycle.current());
    }

    public FaaChartsSource(File cacheDir, AiracCycle cycle) {
        this(cacheDir, cycle.getFaaChartUrl(), cycle.number);
    }
    public FaaChartsSource(File cacheDir, String xmlUrl, int number) {
        this.cacheDir = cacheDir;
        this.xmlUrl = xmlUrl;
        this.baseUrl = URL_BASE + number + "/"; // TODO: we could reuse a StringBuilder...

        String xmlName = xmlUrl.substring(xmlUrl.lastIndexOf('/') + 1);
        xmlFile = new File(cacheDir, xmlName);
    }

    @Override
    public String id() {
        return "faa-charts";
    }

    @Override
    public Observable<Boolean> loadInto(Storage storage) {
        return ensureXmlAvailable().map(file -> {
            if (file.exists()) {
                storage.finishSource(this);
                return true;
            }

            return false;
        });
    }

    protected Observable<File> ensureXmlAvailable() {
        return Observable.fromCallable(() -> {
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                throw new IOException("Unable to prepare cache dir " + cacheDir);
            }

            if (xmlFile.exists()) {
                System.out.println("FAA Charts xml already downloaded!");
                return xmlFile;
            }

            // don't have it, or out of date
            File expiredDataFile = null;
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".xml")) {
                        // everybody do your share
                        //noinspection ResultOfMethodCallIgnored
                        f.delete();
                        expiredDataFile = f;
                    }
                }
            }

            // TODO better logging
            if (expiredDataFile == null) {
                System.out.println("Fetching initial faa charts set: " + xmlUrl);
            } else {
                System.out.println("Existing faa charts set (" + expiredDataFile
                    + ") expired; fetching " + xmlUrl);
            }

            // download
            final long start = System.currentTimeMillis();
            BufferedSource in = Okio.buffer(Okio.source(new URL(xmlUrl).openStream()));
            BufferedSink out = Okio.buffer(Okio.sink(xmlFile));
            in.readAll(out);
            out.close();
            in.close();
            final long end = System.currentTimeMillis();
            System.out.println("Downloaded faa charts data in " + (end - start) + "ms");
            return xmlFile;
        });
    }

    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return Observable.fromCallable(() -> {

            List<ChartInfo> cached = cache.get(airport);
            if (cached != null) return cached;

            ArrayList<ChartInfo> charts = new ArrayList<>();

            final Document doc =
                DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlFile);

            final XPath path = XPathFactory.newInstance().newXPath();
            final XPathExpression exp =
                path.compile("/digital_tpp/state_code/city_name/airport_name[@icao_ident='"
                    + airport.id() + "']/record");

            NodeList nodes = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
            for (int i=0, len=nodes.getLength(); i < len; i++) {
                Node n = nodes.item(i);
                NodeList record = n.getChildNodes();

                String name = null;
                String url = null;
                for (int j=0, jlen=record.getLength(); j < jlen; j++) {
                    Node jn = record.item(j);
                    if ("chart_name".equals(jn.getNodeName())) {
                        name = jn.getTextContent();
                    } else if ("pdf_name".equals(jn.getNodeName())) {
                        // TODO reuse a StringBuilder to save allocations?
                        url = baseUrl + jn.getTextContent();
                    }

                    if (name != null && url != null) break;
                }

                if (name != null && url != null) {
                    charts.add(new ChartInfo(name, url));
                }
            }

            cache.put(airport, charts);
            return charts;
        });
    }
}
