package net.dhleong.opengps.faa;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.nasr.util.AiracCycle;
import net.dhleong.opengps.status.DataKind;
import net.dhleong.opengps.status.StatusUpdate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import rx.Observer;

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
    public String name() {
        return "FAA Charts";
    }

    @Override
    public Observable<Boolean> loadInto(Storage storage, Observer<StatusUpdate> updates) {
        return ensureXmlAvailable(updates).map(file -> {
            if (file.exists()) {
                storage.finishSource(this);
                updates.onNext(new StatusUpdate(this, DataKind.CHARTS));
                updates.onNext(new StatusUpdate(this, DataKind.READY));
                return true;
            }

            return false;
        });
    }

    protected Observable<File> ensureXmlAvailable(Observer<StatusUpdate> updates) {
        return Observable.fromCallable(() -> {
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                throw new IOException("Unable to prepare cache dir " + cacheDir);
            }

            if (xmlFile.exists()) {
                System.out.println("FAA Charts xml already downloaded!");
                updates.onNext(new StatusUpdate(this, DataKind.RAW_FETCHED));
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
                updates.onNext(new StatusUpdate(this, DataKind.RAW_INIT));
            } else {
                System.out.println("Existing faa charts set (" + expiredDataFile
                    + ") expired; fetching " + xmlUrl);
                updates.onNext(new StatusUpdate(this, DataKind.RAW_UPDATE));
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
            updates.onNext(new StatusUpdate(this, DataKind.RAW_FETCHED));
            return xmlFile;
        });
    }

    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return Observable.fromCallable(() -> {

            List<ChartInfo> cached = cache.get(airport);
            if (cached != null) return cached;

            final List<ChartInfo> charts = findRecordNodes(xmlFile, airport);
            if (charts != null) {
                cache.put(airport, charts);
            }
            return charts;
        });
    }

    List<ChartInfo> findRecordNodesXpath(File xmlFile, Airport airport) throws Exception {
        long start = System.currentTimeMillis();
        final Document doc =
            DocumentBuilderFactory.newInstance()
                                  .newDocumentBuilder()
                                  .parse(xmlFile);

        final String rawPath = "/digital_tpp/state_code" +
            (airport.stateCode == null
                ? ""
                : "[@ID='" + airport.stateCode + "']") +
            "/city_name/airport_name[@icao_ident='"
            + airport.id() + "']/record";

        final XPath path = XPathFactory.newInstance().newXPath();
        final XPathExpression exp = path.compile(rawPath);
        final NodeList nodes = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);

        ArrayList<ChartInfo> charts = new ArrayList<>();

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

        sort(charts);
        long end = System.currentTimeMillis();
        System.out.println("XPath query took " + (end - start) + "ms: " + rawPath); // TODO logging
        return charts;
    }

    List<ChartInfo> findRecordNodes(File xmlFile, Airport airport) throws Exception {
        final XmlPullParser parser;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
        } catch (NoClassDefFoundError e) {
            // XmlPullParser not on classpath; fall back to much slower XPath
            return findRecordNodesXpath(xmlFile, airport);
        }

        final String targetState = airport.stateCode.length() == 0
            ? "XX"
            : airport.stateCode;
        final String targetCity = airport.cityName;
        final String targetIcao = airport.id();

        final long start = System.currentTimeMillis();
        final InputStream in = new FileInputStream(xmlFile);
        try {
            parser.setInput(in, null);
            parser.nextTag();

            List<ChartInfo> charts = new ArrayList<>();

            parser.require(XmlPullParser.START_TAG, null, "digital_tpp");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                final String name = parser.getName();
                final String id = parser.getAttributeValue(null, "ID");
                if ("state_code".equals(name) && !id.equals(targetState)) {
                    skip(parser);
                    continue;
                }

                if ("city_name".equals(name) && !id.startsWith(targetCity)) {
                    skip(parser);
                    continue;
                }

                final boolean isAirport ="airport_name".equals(name);
                if (isAirport && !parser.getAttributeValue(null, "icao_ident").equals(targetIcao)) {
                    skip(parser);
                } else if (isAirport) {
                    // read records
                    while (parser.next() != XmlPullParser.END_TAG) {
                        charts.add(readRecord(parser));
                    }
                    // done; break out
                    break;
                }
            }

            sort(charts);
            long end = System.currentTimeMillis();
            System.out.println("XPP query took " + (end - start) + "ms"); // TODO logging
            return charts;
        } catch (Exception e) {
            // just make sure it doesn't get suppressed by the finally
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // don't care
            }
        }
    }

    ChartInfo readRecord(XmlPullParser parser) throws IOException, XmlPullParserException {
        String name = null;
        String url = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            String nodeName = parser.getName();
            if ("chart_name".equals(nodeName)) {
                name = readText(parser);
            } else if ("pdf_name".equals(nodeName)) {
                // TODO reuse a StringBuilder to save allocations?
                url = baseUrl + readText(parser);
            } else {
                skip(parser);
            }
        }
        return new ChartInfo(name, url);
    }

    static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
    }

    static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    static void sort(List<ChartInfo> charts) {
        // rip out the AIRPORT DIAGRAM and move it to the front (lazy way)
        ChartInfo diagram = null;
        Iterator<ChartInfo> iter = charts.iterator();
        while (iter.hasNext()) {
            ChartInfo info = iter.next();
            if ("AIRPORT DIAGRAM".equals(info.name)) {
                iter.remove();
                diagram = info;
                break;
            }
        }

        if (diagram != null) {
            charts.add(0, diagram);
        }
    }

}
