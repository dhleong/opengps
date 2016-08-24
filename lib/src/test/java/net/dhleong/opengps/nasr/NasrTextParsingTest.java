package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.nasr.util.Parser;
import net.dhleong.opengps.nasr.util.ParserTest;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;

/**
 * @author dhleong
 */
public class NasrTextParsingTest {

    static final String LGA_NUMBER = "15794.*A";
    static final String SNA_NUMBER = "02230.*A";

    static final String LAGUARDIA =
        "APT15794.*A   AIRPORT      LGA 07/21/2016AEANYC NYNEW YORK            QUEENS               NYNEW YORK                                LAGUARDIA                                         PUPUPORT. AUTH. OF N.Y. & N.J.         225 PARK AVE. SOUTH                                                     NEW YORK, NY 10003                               212-435-3703LYSA SCULLY                        HANGAR #7, THIRD FLOOR                                                  FLUSHING, NY 11371                               718-533-340140-46-38.1000N 146798.1000N073-52-21.4000W265941.4000WE   20.6S12W1980    NEW YORK                      04E    680ZNY ZCNNEW YORK                                                           NISP NEW YORK                                      1-800-WX-BRIEF                                                    LGA Y       O I D S 05/1973  NGY3   NOT ANALYZED NYNYF F02102015        100LLA                                  MAJORMAJORHIGH/LOWHIGH/LOW       SS-SR  Y122.950       N   CG Y                      314946      04816300000000662400027912/31/2014NGS             05/30/2006FAA             05/12/2014 TIE         AFRT,AVNCS,CARGO,CHTR                                                  Y-LKLGA                                                                                                                                                                                                                                                                                                                           \n";

    static final String LGA_ILS_13_LOC =
        "ILS215794.*A   13 ILS/DME   OPERATIONAL IFR       05/14/198140-46-17.391N 146777.391N073-51-21.276W265881.276WK   -7396     F    11.1108.50 NO RESTRICTIONS 5.42 700.00    393 NV";

    // NB: this is actually LGA's airport number:
    static final String SNA_TWR1 =
        "TWR1SNA 07/21/201602230.*A   AWPCALIFORNIA                    CASANTA ANA                               JOHN WAYNE AIRPORT-ORANGE COUNTY                  33-40-32.400N 121232.400N117-52-05.600W424325.600WRAL RIVERSIDE                     ATCT        17ALLSCT SOUTHERN CALIFORNIA TRACON                                                                                                                                                                                                                                                                                       FEDERAL AVIATION ADMIN                                                                                                                                                                                                                          JOHN WAYNE                                          SOCAL                                               SOCAL\n";

    static final String SNA_TWR3 =
        "TWR3SNA 118.0                                       CD/P                                              119.9(RWY 02R/20L)                          LCL/P                                             120.8 EAST                                  GND/P                                             121.85                                      CD/S                                              126.0                                       D-ATIS                                            126.8(RWY 02L/20R)                          LCL/P IC                                          128.35                                      LCL/S                                             132.25 WEST                                 GND/P                                             379.9                                       LCL/P                                             118.0                                                       119.9(RWY 02R/20L)                                          120.8 EAST                                                  121.85                                                      126.0                                                       126.8(RWY 02L/20R)                                          128.35                                                      132.25 WEST                                                 379.9";

    static final String SNA_TWR_COMBINED = SNA_TWR1 + SNA_TWR3;

    static final String LGA_VORDME =
        "NAV1LGA VOR/DME             LGA 07/21/2016LA GUARDIA                    NEW YORK                                NEW YORK                      NYAEA                                FEDERAL AVIATION ADMIN                            FEDERAL AVIATION ADMIN                            YYL-VOR/DME  24         ZNY NEW YORK                      ZNY NEW YORK                      40-47-01.376N 146821.376N073-52-06.962W265926.962W640-47-01.376N 146821.376N073-52-06.962W265926.962W    8.9  12W1980Y      N  1NEW YORK RADIO                078X113.10                                     LY                                  ISP NEW YORK                      24                                                                                                  LGA                 OPERATIONAL RESTRICTED        NNN   ";

    @Test
    public void readAirport() throws IOException {
        assertThat(NasrTextDataSource.readAirport(ParserTest.parser(LAGUARDIA)))
            .isType(Airport.Type.AIRPORT)
            .hasNumber("15794.*A")
            .hasId("LGA")
            .hasName("LAGUARDIA")
            .hasLat(40, 46, 38.1)
            .hasLng(-73, -52, -21.4)
        ;
    }

    @Test
    public void readIls() throws IOException {
        NasrTextDataSource.AirportFreqRecord record = new NasrTextDataSource.AirportFreqRecord();
        assertThat(NasrTextDataSource.readIlsRecord(ParserTest.parser(LGA_ILS_13_LOC), record))
                  .isTrue();

        assertThat(record.airportNumber).isEqualTo("15794.*A");
        assertThat(record.freq.label).isEqualTo("ILS/DME 13");
        assertThat(record.freq.frequency).isEqualTo(108.5);
    }

    @Test
    public void readTwr() throws IOException {
        Parser parser = ParserTest.parser(SNA_TWR_COMBINED);
        NasrTextDataSource.AirportFreqRecord record = new NasrTextDataSource.AirportFreqRecord();
        List<LabeledFrequency> freqs = new ArrayList<>();
        Action1<NasrTextDataSource.AirportFreqRecord> action = r -> freqs.add(r.freq);

        NasrTextDataSource.readTwrRecord(parser, record, action);
        assertThat(freqs).isEmpty(); // nothing, yet
        assertThat(record.airportNumber).isEqualTo("02230.*A");

        NasrTextDataSource.readTwrRecord(parser, record, action);
        assertThat(freqs)
            .contains(
                new LabeledFrequency("CD/P", 118.0),
                new LabeledFrequency("LCL/P (RWY 02R/20L)", 119.9),
                new LabeledFrequency("GND/P EAST", 120.8),
                new LabeledFrequency("CD/S", 121.85),
                new LabeledFrequency("D-ATIS", 126.0),
                new LabeledFrequency("LCL/P IC (RWY 02L/20R)", 126.8),
                new LabeledFrequency("LCL/S", 128.35),
                new LabeledFrequency("GND/P WEST", 132.25),
                new LabeledFrequency("LCL/P", 379.9)
            );
    }

    @Test
    public void readNavaid() throws IOException {
        assertThat(NasrTextDataSource.readNavRecord(ParserTest.parser(LGA_VORDME)))
            .hasId("LGA")
            .hasType(Navaid.Type.VORDME)
            .hasName("LA GUARDIA")
            .hasLat(40, 47, 1.376)
            .hasLng(-73, -52, -6.962)
            ;

    }

}
