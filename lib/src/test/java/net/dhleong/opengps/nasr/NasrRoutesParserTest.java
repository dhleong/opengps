package net.dhleong.opengps.nasr;

import net.dhleong.opengps.PreferredRoute;
import net.dhleong.opengps.nasr.util.Parser;
import net.dhleong.opengps.test.Airports;
import net.dhleong.opengps.test.OkioTest;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class NasrRoutesParserTest {

    static final String DATA =
        "PFR1LGA  PSM  TEC 1TOWER ENROUTE CONTROL                                                                                    10000                                   /E, /F, /G ONLY                                                                                                    \n" +
        "PFR2LGA  PSM  TEC 1005BDR                                             NAVAID     D VOR/DME                                                                                                                                                                                             \n" +
        "PFR2LGA  PSM  TEC 1010HFD                                             NAVAID     D VOR/DME                                                                                                                                                                                             \n" +
        "PFR2LGA  PSM  TEC 1015CLOWW                                           FIX    NHK6                                                                                                                                                                                                      \n" +
        "PFR1LGA  PVD  L   1LOW ALTITUDE                                                                                             110-170 INCL                                                                              1100-0300                                                        \n" +
        "PFR2LGA  PVD  L   1005BAYYS                                           FIX    CTK6                                                                                                                                                                                                      \n" +
        "PFR2LGA  PVD  L   1010V229                                            AIRWAY                                                                                                                                                                                                           \n" +
        "PFR2LGA  PVD  L   1015SEALL                                           FIX    CTK6                                                                                                                                                                                                      \n" +
        "PFR2LGA  PVD  L   1020V188                                            AIRWAY                                                                                                                                                                                                           \n" +
        "PFR2LGA  PVD  L   1025GON                                             NAVAID     D VOR/DME                                                                                                                                                                                             \n" +
        "PFR2LGA  PVD  L   1030V374                                            AIRWAY                                                                                                                                                                                                           \n" +
        "PFR2LGA  PVD  L   1035MINNK                                           FIX    RIK6                                                                                                                                                                                                      \n" +
        // this JFK thing is fake; just for testing:
        "PFR1LGA  JFK  TEC 1TOWER ENROUTE CONTROL                                                                                    9000                                                                                                                                                       \n";

    static final String DATA2 =
        "PFR1SNA  HHR  TEC 1TOWER ENROUTE CONTROL         (FUL LGB SLI SNA TOA(RWY11)) TO (HHR (RWY 25))                             JM70PQ40                                                                                                                               CSTN6               \n" +
        "PFR2SNA  HHR  TEC 1005SLI                                             NAVAID     C VORTAC                                                                                                                                                                                              \n" +
        "PFR2SNA  HHR  TEC 1010SLI                                             NAVAID     C VORTAC              340                                                                                                                                                                             \n" +
        "PFR2SNA  HHR  TEC 1015WELLZ                                           FIX    CAK2                                                                                                                                                                                                      \n";

    Parser parser;

    @Before
    public void setUp() {
        parser = Parser.of(OkioTest.source(DATA + DATA2));
    }

    @Test
    public void lgaToPvd() throws IOException {
        PreferredRoute expected = new PreferredRoute();
        expected.from = Airports.LGA;
        expected.to = Airports.PVD;
        expected.altitude = "110-170 INCL";
        expected.routeString = "BAYYS V229 SEALL V188 GON V374 MINNK";
        expected.area = "";
        expected.direction = "";
        expected.aircraftLimitations = "";

        assertThat(NasrRoutesParser.find(parser, Airports.LGA, Airports.PVD))
            .hasSize(1)
            .containsExactly(expected);
    }

    @Test
    public void snaToHrr() throws IOException {
        // has a radial in the route
        PreferredRoute expected = new PreferredRoute();
        expected.from = Airports.SNA;
        expected.to = Airports.HHR;
        expected.altitude = "JM70PQ40";
        expected.routeString = "SLI SLI340 WELLZ";
        expected.area = "(FUL LGB SLI SNA TOA(RWY11)) TO (HHR (RWY 25))";
        expected.direction = "CSTN6";
        expected.aircraftLimitations = "";

        assertThat(NasrRoutesParser.find(parser, Airports.SNA, Airports.HHR))
            .hasSize(1)
            .containsExactly(expected);
    }

}