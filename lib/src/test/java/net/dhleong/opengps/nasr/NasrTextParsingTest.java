package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.nasr.util.Parser;

import org.junit.Test;

import java.io.IOException;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.OkioTest.source;

/**
 * @author dhleong
 */
public class NasrTextParsingTest {
    static final String LAGUARDIA =
        "APT15794.*A   AIRPORT      LGA 07/21/2016AEANYC NYNEW YORK            QUEENS               NYNEW YORK                                LAGUARDIA                                         PUPUPORT. AUTH. OF N.Y. & N.J.         225 PARK AVE. SOUTH                                                     NEW YORK, NY 10003                               212-435-3703LYSA SCULLY                        HANGAR #7, THIRD FLOOR                                                  FLUSHING, NY 11371                               718-533-340140-46-38.1000N 146798.1000N073-52-21.4000W265941.4000WE   20.6S12W1980    NEW YORK                      04E    680ZNY ZCNNEW YORK                                                           NISP NEW YORK                                      1-800-WX-BRIEF                                                    LGA Y       O I D S 05/1973  NGY3   NOT ANALYZED NYNYF F02102015        100LLA                                  MAJORMAJORHIGH/LOWHIGH/LOW       SS-SR  Y122.950       N   CG Y                      314946      04816300000000662400027912/31/2014NGS             05/30/2006FAA             05/12/2014 TIE         AFRT,AVNCS,CARGO,CHTR                                                  Y-LKLGA                                                                                                                                                                                                                                                                                                                           \n";

    static final String LGA_ILS_13_LOC =
        "ILS215794.*A   13 ILS/DME   OPERATIONAL IFR       05/14/198140-46-17.391N 146777.391N073-51-21.276W265881.276WK   -7396     F    11.1108.50 NO RESTRICTIONS 5.42 700.00    393 NV";

    @Test
    public void readAirport() throws IOException {
        assertThat(NasrTextDataSource.readAirport(parser(LAGUARDIA)))
            .isType(Airport.Type.AIRPORT)
            .hasNumber("15794.*A")
            .hasId("LGA")
            .hasName("LAGUARDIA")
            .hasLat( 146798.1000)
            .hasLng(-265941.4000)
        ;
    }

    @Test
    public void readIls() throws IOException {
        NasrTextDataSource.IlsRecord record = new NasrTextDataSource.IlsRecord();
        assertThat(NasrTextDataSource.readIlsRecord(parser(LGA_ILS_13_LOC), record))
                  .isTrue();

        assertThat(record.airportNumber).isEqualTo("15794.*A");
        assertThat(record.freq.label).isEqualTo("ILS/DME 13");
        assertThat(record.freq.frequency).isEqualTo(108.5);
    }

    private static Parser parser(String string) {
        return Parser.of(source(string));
    }

}
