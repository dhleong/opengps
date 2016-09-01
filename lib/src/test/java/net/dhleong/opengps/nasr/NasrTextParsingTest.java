package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.nasr.util.Parser;
import net.dhleong.opengps.nasr.util.ParserTest;
import net.dhleong.opengps.storage.InMemoryStorage;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.Navaids.HFD;
import static net.dhleong.opengps.test.Navaids.LGA;

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

    static final String FIX_MERIT =
        "FIX1MERIT                         CONNECTICUT                   K641-22-55.020N 073-08-14.750WFIX                                                                             RNAV                                  YREP-PT         MERITZBW ZBW                               NNN                                                                                                                                                                                                \n" +
        "FIX2MERIT                         CONNECTICUT                   K6CMK*D*084.92                                                                                                                                                                                                                                                                                                                                                                                                    \n" +
        "FIX2MERIT                         CONNECTICUT                   K6HFD*D*252.90                                                                                                                                                                                                                                                                                                                                                                                                    \n" +
        "FIX2MERIT                         CONNECTICUT                   K6HTO*C*320.00                                                                                                                                                                                                                                                                                                                                                                                                    \n" +
        "FIX2MERIT                         CONNECTICUT                   K6LGA*D*054.52/48.86                                                                                                                                                                                                                                                                                                                                                                                              \n" +
        "FIX2MERIT                         CONNECTICUT                   K6MAD*D*294.62                                                                                                                                                                                                                                                                                                                                                                                                    \n" +
        "FIX5MERIT                         CONNECTICUT                   K6CONTROLLER                                                                                                                                                                                                                                                                                                                                                                                                      \n" +
        "FIX5MERIT                         CONNECTICUT                   K6ENROUTE HIGH                                                                                                                                                                                                                                                                                                                                                                                                    \n" +
        "FIX5MERIT                         CONNECTICUT                   K6ENROUTE LOW                                                                                                                                                                                                                                                                                                                                                                                                     \n" +
        "FIX5MERIT                         CONNECTICUT                   K6SID                                                                                                                                                                                                                                                                                                                                                                                                             \n" +
        "FIX5MERIT                         CONNECTICUT                   K6STAR                                                                                                                                                                                                                                                                                                                                                                                                            \n";

    static final String AWY_V99 =
        "AWY1V99      1007/21/2016                   000.00000.00                  04000             BND      1700            BND         BND      12WZNY25730*NY                         V99  *OUTTE*CT                                            BND                                         0000001\n" +
        "AWY2V99      10LA GUARDIA                    VOR/DME                           NY  40-47-01.376N 073-52-06.962W     LGA V99  *LGA*D                                                                                                                                                    0000002\n" +
        "AWY1V99      2007/21/2016                   000.00000.00                  04000             BND      1700            BND         BND         ZNYOUTTE*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      2025730                         ARTCC-BDRY         FIX            NYK640-51-30.5N   073-46-41.8W           V99  *25730*NY                                                                                                                                                 0000002\n" +
        "AWY1V99      3007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBW25007*CT                         V99  *SORRY*CT                                            BND                                         0000001\n" +
        "AWY2V99      30OUTTE                         REP-PT             FIX            CTK641-04-41.48N  073-30-39.78W          V99  *OUTTE*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      4007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBWVAGUS*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      4025007                         AWY-INTXN          FIX            CTK641-06-12.0N   073-28-49.1W           V99  *25007*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      5007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBW25146*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      50VAGUS                         REP-PT             FIX            CTK641-09-49.87N  073-24-22.27W          V99  *VAGUS*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      6007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBWANNEI*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      6025146                         AWY-INTXN          FIX            CTK641-14-48.4N   073-18-15.5W           V99  *25146*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      7007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBW25124*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      70ANNEI                         REP-PT             FIX            CTK641-17-09.83N  073-15-21.24W          V99  *ANNEI*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      8007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBWTRUDE*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      8025124                         AWY-INTXN          FIX            CTK641-17-28.64N  073-14-58.11W          V99  *25124*CT                                                                                                                                                 0000002\n" +
        "AWY1V99      9007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBW25679*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99      90TRUDE                         REP-PT             FIX            CTK641-20-01.96N  073-11-48.73W          V99  *TRUDE*CT                                                                                                                                                 0000002\n" +
        "AWY1V99     10007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBWMERIT*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99     10025679                         AWY-INTXN          FIX            CTK641-20-27.8N   073-11-16.8W           V99  *25679*CT                                                                                                                                                 0000002\n" +
        "AWY1V99     11007/21/2016                   000.00000.00                  04000             BND      2600            BND         BND         ZBWSORRY*CT                                                                                   BND                                         0000001\n" +
        "AWY2V99     110MERIT                         REP-PT             FIX            CTK641-22-55.02N  073-08-14.75W          V99  *MERIT*CT                                                                                                                                                 0000002\n" +
        "AWY1V99     12007/21/2016                   000.00000.00                  03000             BND                      BND         BND         ZBWYALER*CT                         V99  *HFD*D                                               BND                                         0000001\n" +
        "AWY2V99     120SORRY                         REP-PT             FIX            CTK641-28-43.1N   073-01-02.67W          V99  *SORRY*CT                                                                                                                                                 0000002\n" +
        "AWY1V99     13007/21/2016                   000.00000.00                  03000             BND                      BND         BND         ZBWHFD*D                                                                                      BND                                         0000001\n" +
        "AWY2V99     130YALER                         REP-PT             FIX            CTK641-30-56.61N  072-54-39.09W          V99  *YALER*CT                                                                                                                                                 0000002\n" +
        "AWY1V99     14007/21/2016                   000.00000.00                  03000             BND                      BND         BND      13WZBW                                                                                           BND                                         0000001\n" +
        "AWY2V99     140HARTFORD                      VOR/DME                           CT  41-38-27.977N 072-32-50.705W     HFD V99  *HFD*D                                                                                                                                                    0000002\n";

    @Test
    public void readAirport() throws IOException {
        assertThat(NasrTextDataSource.readAirport(ParserTest.parser(LAGUARDIA)))
            .isType(Airport.Type.AIRPORT)
            .hasNumber("15794.*A")
            .hasId("KLGA")
            .hasName("LAGUARDIA")
            .hasLat(40, 46, 38.1)
            .hasLng(-73, -52, -21.4)
            .hasMagVar(-12)
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
            .hasMagVar(-12)
        ;

    }

    @Test
    public void readNavFix() throws IOException {

        // pre-fill with some appropriate refs
        InMemoryStorage storage = new InMemoryStorage();
        storage.put(HFD);

        assertThat(NasrTextDataSource.readNavFix(ParserTest.parser(FIX_MERIT), storage))
            .hasId("MERIT")
            .hasName("MERIT")
            .hasLat(41, 22, 55.02)
            .hasLng(-73, -8, -14.75)
            // test refs:
            .hasRef(HFD, 252.9f)
        ;
    }

    @Test
    public void readAirway() throws IOException {

        // pre-fill with some appropriate refs
        InMemoryStorage storage = new InMemoryStorage();
        storage.put(LGA);
        storage.put(HFD);

        // TODO fixes

        NasrTextDataSource.readAirways(ParserTest.parser(AWY_V99), storage);

        Airway airway = (Airway) storage.find("V99").toBlocking().first();
        assertThat(airway)
            .isNotNull()
            .containsExactly(LGA, HFD);
    }
}
