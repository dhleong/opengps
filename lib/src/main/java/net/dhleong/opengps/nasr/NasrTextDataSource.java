package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.nasr.util.Parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Options;
import okio.Source;
import rx.Observable;
import rx.functions.Action1;

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

    static final String DEFAULT_ZIP_URL =
        "https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_July_21__2016_-_September_15__2016.zip";

    private final File zipFile;
    private final String zipUrl;

    public NasrTextDataSource(File zipFile) {
        this(zipFile, DEFAULT_ZIP_URL);
    }
    public NasrTextDataSource(File zipFile, String zipUrl) {
        this.zipFile = zipFile;
        this.zipUrl = zipUrl;
    }

    @Override
    public String id() {
        return "NASR";
    }

    @Override
    public Observable<Boolean> loadInto(Storage storage) {
        return ensureZipAvailable()
        .flatMap(file -> Observable.fromCallable(() -> { // indirection to handle IOEs
            storage.beginTransaction();

            // read airports
            Parser apts = Parser.of(Okio.buffer(openAirportsFile()));
            while (!apts.exhausted()) {
                final Airport airport = readAirport(apts);
                if (airport != null) {
                    storage.put(airport);
                }
            }
            apts.close();

            // read in ILS frequencies
            Parser ils = Parser.of(Okio.buffer(openIlsFile()));
            AirportFreqRecord record = new AirportFreqRecord();
            record.type = Airport.FrequencyType.NAV;
            while (!ils.exhausted()) {
                if (readIlsRecord(ils, record)) {
                    storage.addIlsFrequency(record.airportNumber, record.freq);
                }
            }
            ils.close();

            // read in ATC/ATIS frequencies
            record.airportNumber = null;
            record.freq = null;
            record.workspace.setLength(0);
            Action1<AirportFreqRecord> freqObserver = rec ->
                storage.addFrequency(rec.airportNumber, rec.type, rec.freq);
            Parser twr = Parser.of(Okio.buffer(openTwrFile()));
            record.airportNumber = null; // reset
            while (!twr.exhausted()) {
                readTwrRecord(twr, record, freqObserver);
            }
            twr.close();

            storage.markTransactionSuccessful();
            return true;
        }).doAfterTerminate(storage::endTransaction));
    }

    protected Observable<File> ensureZipAvailable() {
        return Observable.fromCallable(() -> {
            if (zipFile.exists()) return zipFile;

            // download
            BufferedSource in = Okio.buffer(Okio.source(new URL(DEFAULT_ZIP_URL).openStream()));
            BufferedSink out = Okio.buffer(Okio.sink(zipFile));
            in.readAll(out);
            out.close();
            in.close();

            return zipFile;
        });
    }

    protected Source openAirportsFile() throws IOException {
        return openZipFile("APT.txt");
    }

    protected Source openIlsFile() throws IOException {
        return openZipFile("ILS.txt");
    }

    protected Source openTwrFile() throws IOException {
        return openZipFile("TWR.txt");
    }

    private Source openZipFile(String fileName) throws IOException {
        final ZipFile zip = new ZipFile(zipFile);
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
            apts.skip(2); // state post office code
            apts.skip(20); // state name
            apts.skip(21); // county name
            apts.skip(2); // county state
            apts.skip(40); // city name
            String name = apts.string(50);

            // ownership data
            apts.skip(340);

            // geographic data
            double lat = apts.latOrLng();
            double lng = apts.latOrLng();
            apts.skip(1); // reference point determination method
            float elevation = apts.decimalNumber(7);
            apts.skip(1); // elevation determination method

            result = new Airport(number, facilityType, id, name, lat, lng);
            result.elevation = elevation;
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
            String id = twr.string(4); // airport id (we don't care)
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
                case 'C': record.type = Airport.FrequencyType.DELIVERY; break;
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

    static class AirportFreqRecord {
        String airportNumber;
        LabeledFrequency freq;
        Airport.FrequencyType type;

        StringBuilder workspace = new StringBuilder(10 + 1 + 3);
    }
}
