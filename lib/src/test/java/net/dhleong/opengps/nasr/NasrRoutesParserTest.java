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

    Parser parser;

    @Before
    public void setUp() {
        parser = Parser.of(OkioTest.source(DATA));
    }

    @Test
    public void test() throws IOException {
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

}