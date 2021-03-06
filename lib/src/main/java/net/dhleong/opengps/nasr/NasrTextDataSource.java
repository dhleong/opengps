package net.dhleong.opengps.nasr;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.PreferredRoute;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.impl.BaseAeroObject;
import net.dhleong.opengps.nasr.util.AiracCycle;
import net.dhleong.opengps.nasr.util.Parser;
import net.dhleong.opengps.status.DataKind;
import net.dhleong.opengps.status.StatusUpdate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Options;
import okio.Source;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author dhleong
 */
public class NasrTextDataSource implements DataSource {
    static final Options APT_HEADERS = Options.of(
        ByteString.encodeUtf8("APT")
    );
    static final int APT_TYPE_MAIN = 0;


    static final Options ILS_HEADERS = Options.of(
        ByteString.encodeUtf8("ILS2")
    );
    static final int ILS_TYPE_LOC_INFO = 0;


    static final Options TWR_HEADERS = Options.of(
        ByteString.encodeUtf8("TWR1"),
        ByteString.encodeUtf8("TWR3")
    );
    static final int TWR_TYPE_INFO = 0;
    static final int TWR_TYPE_FREQUENCIES = 1;

    private static final int TWR_FREQUENCIES_COUNT = 9;


    static final Options NAV_HEADERS = Options.of(
        ByteString.encodeUtf8("NAV1")
    );
    static final int NAV_TYPE_MAIN = 0;


    static final Options FIX_HEADERS = Options.of(
        ByteString.encodeUtf8("FIX1"),
        ByteString.encodeUtf8("FIX2")
    );
    static final int FIX_TYPE_MAIN = 0;
    static final int FIX_TYPE_NAVAID = 1;

    private static final Options SLASH_OR_BLANK = Options.of(
        ByteString.encodeUtf8("/"),
        ByteString.encodeUtf8(" ")
    );
    static final int VALUE = -1;
    static final int HAS_SLASH = 0;
    static final int HAS_BLANK = 1;

    static final Options AWY_HEADERS = Options.of(
        ByteString.encodeUtf8("AWY2")
    );
    static final int AWY_TYPE_FIX = 0;

    private static final Observer<StatusUpdate> DUMMY_UPDATER = new Observer<StatusUpdate>() {
        @Override public void onCompleted() { }
        @Override public void onError(Throwable e) { }
        @Override public void onNext(StatusUpdate statusUpdate) { }
    };

    private final File cacheDir;
    private final File zipFile;
    private final String zipUrl;
    private ZipFile openedZipFile;

    public NasrTextDataSource(File cacheDirectory) {
        this(cacheDirectory, AiracCycle.current().getNasrDataUrl());
    }
    public NasrTextDataSource(File cacheDir, String zipUrl) {
        this.cacheDir = cacheDir;
        this.zipUrl = zipUrl;

        String zipName = zipUrl.substring(zipUrl.lastIndexOf('/') + 1);
        zipFile = new File(cacheDir, zipName);
    }

    @Override
    public String id() {
        return "NASR";
    }

    @Override
    public String name() {
        // technically "National Aerospace System Resources,"
        //  but that's gonna be really hard to fit
        return "FAA AeroNav Data";
    }

    @Override
    public Single<NasrTextDataSource> loadInto(Storage storage, Observer<StatusUpdate> updates) {
        final long start = System.currentTimeMillis();
        return ensureZipAvailable(updates)
        .subscribeOn(Schedulers.io())
        .flatMap(file -> Observable.fromCallable(() -> { // indirection to handle IOEs
            storage.beginTransaction();

            // read airports *first* so we can read frequencies into them
            Parser apts = Parser.of(Okio.buffer(openAirportsFile()));
            while (!apts.exhausted()) {
                final Airport airport = readAirport(apts);
                if (airport != null) {
                    storage.put(airport);
                }
            }
            apts.close();

            updates.onNext(new StatusUpdate(this, DataKind.AIRPORTS));
            return this;
        }))
        .flatMap(any -> Observable.merge(
            // do these all in parallel:

            // read in ILS frequencies
            Observable.fromCallable(() -> {
                AirportFreqRecord record = new AirportFreqRecord();
                Parser ils = Parser.of(Okio.buffer(openIlsFile()));
                record.type = Airport.FrequencyType.NAV;
                while (!ils.exhausted()) {
                    if (readIlsRecord(ils, record)) {
                        storage.addIlsFrequency(record.airportNumber, record.freq);
                    }
                }
                ils.close();
                return this;
            }).subscribeOn(Schedulers.io()),

            // read in ATC/ATIS frequencies
            Observable.fromCallable(() -> {
                AirportFreqRecord record = new AirportFreqRecord();
                Action1<AirportFreqRecord> freqObserver = rec ->
                    storage.addFrequency(rec.airportNumber, rec.type, rec.freq);
                Parser twr = Parser.of(Okio.buffer(openTwrFile()));
                record.airportNumber = null; // reset
                while (!twr.exhausted()) {
                    readTwrRecord(twr, record, freqObserver);
                }
                twr.close();

                return this;
            }).subscribeOn(Schedulers.io()),

            // navaids & fixes:
            Observable.fromCallable(() -> {
                Parser nav = Parser.of(Okio.buffer(openNavFile()));
                while (!nav.exhausted()) {
                    Navaid navaid = readNavRecord(nav);
                    if (navaid != null) {
                        storage.put(navaid);
                    }
                }
                nav.close();

                // now read fixes here, since they reference navaids
                Parser fix = Parser.of(Okio.buffer(openFixFile()));
                while (!fix.exhausted()) {
                    NavFix obj = readNavFix(fix, storage);
                    if (obj != null) {
                        storage.put(obj);
                    }
                }
                fix.close();

                updates.onNext(new StatusUpdate(this, DataKind.NAVAIDS));
                return this;
            }).subscribeOn(Schedulers.io())

        ).last()) // just wait for the last to finish
        .flatMap(any -> Observable.fromCallable(() -> {
            // now that we have fixes and navaids, read in airways
            Parser awy = Parser.of(Okio.buffer(openAirwaysFile()));
            readAirways(awy, storage);
            awy.close();
            updates.onNext(new StatusUpdate(this, DataKind.AIRWAYS));
            return this;
        }))
        .doOnNext(any -> {
            storage.markTransactionSuccessful();
            storage.finishSource(this);
            updates.onNext(new StatusUpdate(this, DataKind.READY));

            final ZipFile openedZipFile = this.openedZipFile;
            if (openedZipFile != null) {
                try {
                    openedZipFile.close();
                } catch (IOException e) {
                    // don't care?
                    e.printStackTrace();
                }
            }

            // TODO better logging
            final long end = System.currentTimeMillis();
            System.out.println("Loaded NASR data in " + (end - start) + "ms");
        })
        .doAfterTerminate(storage::endTransaction)
        .toSingle();
    }

    public Observable<PreferredRoute> preferredRoutes(Airport from, Airport to) {
        return ensureZipAvailable(DUMMY_UPDATER)
        .subscribeOn(Schedulers.io())
        .flatMap(file -> {
            try {
                Parser pfr = Parser.of(Okio.buffer(openRoutesFile()));
                return Observable.from(NasrRoutesParser.find(pfr, from, to));
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    protected Observable<File> ensureZipAvailable(Observer<StatusUpdate> updates) {
        return Observable.fromCallable(() -> {
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                throw new IOException("Unable to prepare cache dir " + cacheDir);
            }

            if (zipFile.exists()) {
                System.out.println("NASR zip already downloaded!");

                try {
                    openedZipFile = new ZipFile(zipFile);
                    updates.onNext(new StatusUpdate(this, DataKind.RAW_FETCHED));
                    return zipFile;
                } catch (ZipException e) {
                    // corrupt
                    System.err.println("Downloaded zip corrupt; redownload...");
                    e.printStackTrace();
                    //noinspection ResultOfMethodCallIgnored
                    zipFile.delete();
                    updates.onNext(new StatusUpdate(this, DataKind.RAW_UPDATE));
                }
            }

            // don't have it, or out of date
            File expiredDataFile = null;
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".zip")) {
                        // everybody do your share
                        //noinspection ResultOfMethodCallIgnored
                        f.delete();
                        expiredDataFile = f;
                    }
                }
            }

            // TODO better logging
            if (expiredDataFile == null) {
                System.out.println("Fetching initial nasr data set: " + zipUrl);
                updates.onNext(new StatusUpdate(this, DataKind.RAW_INIT));
            } else {
                System.out.println("Existing data set (" + expiredDataFile
                    + ") expired; fetching " + zipUrl);
                updates.onNext(new StatusUpdate(this, DataKind.RAW_UPDATE));
            }

            // download
            final long start = System.currentTimeMillis();
            BufferedSource in = Okio.buffer(Okio.source(new URL(zipUrl).openStream()));
            BufferedSink out = Okio.buffer(Okio.sink(zipFile));
            in.readAll(out);
            out.close();
            in.close();
            final long end = System.currentTimeMillis();
            System.out.println("Downloaded NASR data in " + (end - start) + "ms");
            updates.onNext(new StatusUpdate(this, DataKind.RAW_FETCHED));

            openedZipFile = new ZipFile(zipFile);
            return zipFile;
        });
    }

    protected Source openAirportsFile() throws IOException {
        return openZipFile("APT.txt");
    }

    protected Source openAirwaysFile() throws IOException {
        return openZipFile("AWY.txt");
    }

    protected Source openFixFile() throws IOException {
        return openZipFile("FIX.txt");
    }

    protected Source openIlsFile() throws IOException {
        return openZipFile("ILS.txt");
    }

    protected Source openTwrFile() throws IOException {
        return openZipFile("TWR.txt");
    }

    protected Source openNavFile() throws IOException {
        return openZipFile("NAV.txt");
    }

    protected Source openRoutesFile() throws IOException {
        return openZipFile("PFR.txt");
    }

    private Source openZipFile(String fileName) throws IOException {
        final ZipFile zip = openedZipFile;
        final ZipEntry entry = zip.getEntry(fileName);
        return Okio.source(zip.getInputStream(entry));
    }

    static Airport readAirport(Parser apts) throws IOException {
        final int headerType = apts.select(APT_HEADERS);
        final Airport result;
        if (headerType == APT_TYPE_MAIN) {
            String number = apts.string(11);
            Airport.Type facilityType = apts.select(13, Airport.Type.VALUES);
            String id = apts.string(4);

            apts.skip(10); // effective date
            apts.skip(3); // region code
            apts.skip(4); // district/field office code (ex: CHI)
            String stateCode = apts.string(2); // state post office code
            apts.skip(20); // state name
            apts.skip(21); // county name
            apts.skip(2); // county state
            String city = apts.string(40); // city name
            String name = apts.string(50);

            // ownership data
            apts.skip(340);

            // geographic data
            double lat = apts.latOrLng();
            double lng = apts.latOrLng();
            apts.skip(1); // reference point determination method
            float elevation = apts.decimalNumber(7);
            apts.skip(1); // elevation determination method
            float magVar = apts.readMagVar();

            // NB: next field starts at 590; icao is at 1211
            apts.skip(1211 - 590);
            final String icao = apts.string(7);
            final String actualId = icao.isEmpty()
                ? id
                : icao;

            result = new Airport(number, facilityType, actualId, name, lat, lng);
            result.simpleId = id;
            result.elevation = elevation;
            result.magVar = magVar;
            result.stateCode = stateCode;
            result.cityName = city;
        } else {
            result = null;
        }

        apts.skipToLineEnd();
        return result;
    }

    static boolean readIlsRecord(Parser ils, AirportFreqRecord record) throws IOException {
        // TODO we probably want to read the ILS1 header to get the identifier...
        final boolean read;
        final int headerType = ils.select(ILS_HEADERS);
        if (headerType == ILS_TYPE_LOC_INFO) {
            record.airportNumber = ils.string(11);

            final String runwayId = ils.string(3);

            final String runwayType = ils.string(10); // ils system type; we probably want this at some point
            record.workspace.setLength(0);
            record.workspace.append(runwayType)
                            .append(" ")
                            .append(runwayId);

            ils.skip(22); // system status
            ils.skip(10); // effective date
            ils.latOrLng(); // TODO lat
            ils.latOrLng(); // TODO lng
            ils.skip(2); // lat/lng source
            ils.skip(7); // loc distance from approach end
            ils.skip(4); // loc distance from centerline
            ils.skip(1); // loc direction from centerline
            ils.skip(2); // source of loc location info
            ils.skip(7); // loc site elevation (tenth of a foot)

            final double freq = ils.frequency();
            record.freq = new LabeledFrequency(record.workspace.toString(), freq);

            read = true;
        } else {
            read = false;
        }

        ils.skipToLineEnd();
        return read;
    }

    /**
     * Attempt to read part of a TWR record. This is STATEFUL-ish;
     *  if a TWR1 record is read, we will always update `record`
     *  with the airport number; if TWR3 is read, we will always
     *  call freqObserver with any found frequencies USING THE
     *  AIRPORT NUMBER IN `record`.
     * @throws IOException
     */
    static void readTwrRecord(Parser twr, AirportFreqRecord record,
            Action1<AirportFreqRecord> freqObserver) throws IOException {
        int type = twr.select(TWR_HEADERS);
        switch (type) {
        case TWR_TYPE_INFO:
            // read in the identifier
            twr.skip(4); // airport id (we don't care)
            twr.skip(10); // effective date
            record.airportNumber = twr.string(11);

            if (record.airportNumber.trim().length() == 0) {
                record.airportNumber = null;
            }
            break;

        case TWR_TYPE_FREQUENCIES:
            if (record.airportNumber == null) break;

            String airportId = twr.string(4); // airport id (we don't care)

            for (int i=0; i < TWR_FREQUENCIES_COUNT; i++) {
                try {
                    record.freq = twr.ilsFrequency();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to parse ILS frequency " + i + " for "
                        + airportId + " (" + record.airportNumber + ")",
                        e);
                }

                if (record.freq == null) {
                    // no more frequencies
                    break;
                }

                switch (record.freq.label.charAt(0)) {
                case 'L': record.type = Airport.FrequencyType.TOWER; break;
                case 'G': record.type = Airport.FrequencyType.GROUND; break;
                case 'C':
                    if (record.freq.label.charAt(1) == 'D') {
                        record.type = Airport.FrequencyType.DELIVERY;
                    } else {
                        record.type = Airport.FrequencyType.OTHER;
                    }
                    break;
                case 'A':
                case 'D':
                    record.type = Airport.FrequencyType.ATIS;
                    break;

                default:
                    // unknown frequency type (eg: PTD)
                    record.type = null;
                }

                if (record.type != null) {
                    freqObserver.call(record);
                }
            }

            break;

        // default:
        }

        twr.skipToLineEnd();
    }

    static Navaid readNavRecord(Parser nav) throws IOException {
        Navaid result = null;

        final int type = nav.select(NAV_HEADERS);
        if (type == NAV_TYPE_MAIN) {
            String id = nav.string(4);
            Navaid.Type navType = nav.select(20, Navaid.Type.VALUES);
            nav.skip(4); // "official" id
            nav.skip(10); // effective date
            String name = nav.string(30);

            nav.skip(40); // city
            nav.skip(30); // state
            nav.skip(2); // state post office code
            nav.skip(3); // FAA region
            nav.skip(30); // country (if not US)
            nav.skip(2); // country post office code (if not US)

            nav.skip(50); // owner name
            nav.skip(50); // operator name
            nav.skip(1); // common system usage (Y or N)
            nav.skip(1); // public use (Y or N)
            nav.skip(11); // navaid class

            nav.skip(11); // hours of operation (probably important)
            nav.skip(4); // artcc with high altitude boundary containing navaid
            nav.skip(30); // name of the above artcc
            nav.skip(4); // artcc with low altitude boundary containing navaid
            nav.skip(30); // name of the above artcc

            double lat = nav.latOrLng();
            double lng = nav.latOrLng();

            nav.skip(1); // survey accuracy

            nav.skip(25); // lat of tacan portion
            nav.skip(25); // lng of tacan portion

            nav.skip(7); // elevation

            // magnetic variation in degrees
            nav.skip(2);
            float magVar = nav.readMagVar();

            nav.skip(4); // magnetic variation epoch year

            // facilities/features
            nav.skip(3); // simultaneous voice? (probably important)
            nav.skip(4); // power output
            nav.skip(3); // automatic voice identification
            nav.skip(1); // monitoring category

            nav.skip(30); // radio voice call
            nav.skip(4); // tacan channel (probably important)

            final double freq;
            switch (navType) {
            case TACAN:
            case FAN_MARKER:
            case DME:
                freq = 0;
                break;

            default:
                try {
                    freq = nav.frequency6();
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to parse navaid frequency for "
                        + id + " (" + name + ")",
                        e);
                }
            }

            result = new Navaid(navType, id, name, lat, lng, freq);
            result.magVar = magVar;
        }

        nav.skipToLineEnd();
        return result;
    }

    static NavFix readNavFix(Parser fix, Storage storage) throws IOException {

        final int type = fix.select(FIX_HEADERS);
        if (type != FIX_TYPE_MAIN) {
            fix.skipToLineEnd();
            return null;
        }

        // read main info
        final String id = fix.string(30);
        fix.skip(30); // state name
        fix.skip(2); // ICAO region code

        final double lat = fix.latOrLngFmt();
        final double lng = fix.latOrLngFmt();
        fix.skipToLineEnd();

        // read refs
        final List<NavFix.Reference> refs = new ArrayList<>(4);
        while (fix.select(FIX_HEADERS) == FIX_TYPE_NAVAID) {
            // read main info
            fix.skip(30); // id
            fix.skip(30); // state name
            fix.skip(2); // ICAO region code

            Buffer b = fix.readFully(23);
            long refIdEnd = b.indexOf((byte) '*');
            String refId = b.readUtf8(refIdEnd);

            AeroObject refObj =
                storage.find(refId)
                       .filter(o -> !(o instanceof Airport))
                       .toBlocking()
                       .firstOrDefault(null);

            if (refObj != null) {
                if ('*' != b.readByte()) throw new IllegalStateException();
                b.readByte(); // ref obj type

                float bearing = 0;
                float distance = 0;
                if ('*' == b.readByte()) {

                    final boolean hasSlash;
                    switch (b.select(SLASH_OR_BLANK)) {
                    case HAS_SLASH:
                        // no bearing, JUST distance
                        hasSlash = true;
                        break;

                    case VALUE:
                        // no slash; read bearing
                        bearing = (float) Parser.readDecimalNumber(b, 100.);
                        hasSlash = b.select(SLASH_OR_BLANK) == HAS_SLASH;
                        break;

                    default:
                    case HAS_BLANK:
                        hasSlash = false;
                        break;
                    }

                    if (hasSlash) {
                        distance = (float) Parser.readDecimalNumber(b, 100.);
                    }
                }

                refs.add(new NavFix.Reference(refObj, bearing, distance));
            }

            fix.skipToLineEnd();
        }

        return new NavFix(id, id, lat, lng, refs);
    }

    static void readAirways(Parser awy, Storage storage) throws IOException {
        AirwayRecord record = new AirwayRecord();
        while (!awy.exhausted()) {
            if (awy.select(AWY_HEADERS) == AWY_TYPE_FIX) {
                final String id = awy.string(5);
                if (!id.equals(record.id) && record.id != null) {
                    record.buildAndStore(storage);
                }

                record.id = id;
                readAirwayPoint(awy, record);
            }

            awy.skipToLineEnd();
        }

        // store the last one read
        record.buildAndStore(storage);
    }

    static void readAirwayPoint(Parser awy, AirwayRecord record) throws IOException {
        awy.skip(1); // awy type
        awy.skip(5); // sequence number
        awy.skip(30); // facility/fix name
        awy.skip(19); // facility/fix type
        awy.skip(15); // fix type - publication category
        awy.skip(2); // facility/fix state PO code
        awy.skip(2); // icao region (for fix)
        awy.skip(28); // lat/lng
        awy.skip(5); // minimum reception altitude (TODO probably should read?)
        awy.skip(4); // navaid ID (we'll get it below)

        Buffer b = awy.readFully(40);
        long idStart = 1 + b.indexOf((byte) '*');
        if (idStart > 0) {
            b.skip(idStart);
            long idEnd = b.indexOf((byte) '*');
            if (idEnd > 0) {
                String id = b.readUtf8(idEnd);
                record.fixes.add(id);
            }
        }

        // TODO
    }

    static class AirportFreqRecord {
        String airportNumber;
        LabeledFrequency freq;
        Airport.FrequencyType type;

        StringBuilder workspace = new StringBuilder(10 + 1 + 3);
    }

    static class AirwayRecord {
        String id;
        ArrayList<String> fixes = new ArrayList<>(8);

        public void buildAndStore(Storage storage) {
            List<AeroObject> objs =
                Observable.from(fixes)
                          .flatMap(storage::findFix)
                          .toList()
                          .toBlocking()
                          .single();

            fixes.clear(); // reset

            if (objs.isEmpty()) return;

            storage.put(new Airway(id, fillMagVar(objs)));
        }

        static List<AeroObject> fillMagVar(List<AeroObject> objs) {
            float magVar = 0;
            final int len = objs.size();
            for (int i=0; i < len; i++) {
                float thisMagVar = ((BaseAeroObject) objs.get(i)).magVar;
                if (thisMagVar != 0) {
                    magVar = thisMagVar;
                    break;
                }
            }

            for (int i=0; i < len; i++) {
                final BaseAeroObject obj = (BaseAeroObject) objs.get(i);
                if (obj.magVar != 0) {
                    // carry the "closest" magvar around
                    magVar = obj.magVar;
                } else {
                    obj.magVar = magVar;
                }
            }

            return objs;
        }
    }
}
