package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
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
            // read airports
            Parser apts = Parser.of(Okio.buffer(openAirportsFile()));
            while (!apts.exhausted()) {
                Airport airport = readAirport(apts);
                if (airport != null) {
                    storage.put(airport);
                }
            }

            // TODO read in ILS frequencies
            // TODO read in ATC/ATIS frequencies
            return true;
        });
    }

    protected Source openAirportsFile() throws IOException {
        return openZipFile("APT.txt");
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

}
