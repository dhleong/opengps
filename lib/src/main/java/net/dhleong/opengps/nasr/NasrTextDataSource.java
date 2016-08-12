package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Storage;
import net.dhleong.opengps.nasr.util.Parser;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okio.ByteString;
import okio.Okio;
import okio.Options;
import okio.Source;
import rx.Observable;

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


    private final File zipFile;

    public NasrTextDataSource(File zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public String id() {
        return "NASR";
    }

    @Override
    public Observable<Boolean> loadInto(Storage storage) {
        return Observable.fromCallable(() -> {
            storage.beginTransaction();

            // read airports
            Parser apts = Parser.of(Okio.buffer(openAirportsFile()));
            while (!apts.exhausted()) {
                Airport airport = readAirport(apts);
                if (airport != null) {
                    storage.put(airport);
                } else {
                    apts.skipToLineEnd();
                }
            }

            // read in ILS frequencies
            Parser ils = Parser.of(Okio.buffer(openIlsFile()));
            IlsRecord record = new IlsRecord();
            while (!ils.exhausted()) {
                if (readIlsRecord(ils, record)) {
                    storage.addIlsFrequency(record.airportNumber, record.freq);
                } else {
                    System.out.println("skip");
                    ils.skipToLineEnd();
                }
            }

            // TODO read in ATC/ATIS frequencies
            storage.markTransactionSuccessful();
            return true;
        }).doAfterTerminate(storage::endTransaction);
    }

    protected Source openAirportsFile() throws IOException {
        return openZipFile("APT.txt");
    }

    protected Source openIlsFile() throws IOException {
        return openZipFile("ILS.txt");
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

    static boolean readIlsRecord(Parser ils, IlsRecord record) throws IOException {
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

    static class IlsRecord {
        String airportNumber;
        LabeledFrequency freq;

        StringBuilder workspace = new StringBuilder(10 + 1 + 3);
    }
}
