OpenGps
=======

## What

OpenGps is several projects that work in tandem:

1. OpenGps App: An external GPS device *app* for use with virtual flight planning 
2. OpenGps Lib: A Java library for downloading and accessing *real* FAA navigation data
3. RxConnectr: A reactive Java library for interacting with SimConnect. Could potentially
    be pulled out into a separate project

### The App

The app aims to provide a realistic GPS experience using up-to-date real-world aviation data.
It is many things, but there are also many things it is *not*. Let's start with the things that it *is*.

The OpenGps App:
* can create flight plans with navaids, intersections, and airways, clearly displaying 
    frequencies, radials, and distances.
* can query FAA preferred routes, and attempt to load them into the flight planner automatically.
* can display airport charts, including Airport Diagrams, SIDs, STARs, IFR plates, etc.
* lists all known frequencies (ATIS, Ground, Tower, etc.) for airports, as well as the frequencies
    of Navaids, and references for Intersections.
* can query NOAA for METARs at fields, if available, and show the parsed information
* has a moving map with the course overlaid.
* has radio controls which interact with your simulator. Currently only FSX is supported, since that's
    all I own and use, but it's designed so that any other "connection" can be dropped in.
* is FREE. While I reserve the right to publish a paid version on the Play Store at some time
    (as donationware), anyone can download the source and build their own 
    copy for free.

On the other hand, the app:
* is NOT to be used for real world aviation. While its data should be correct, and once downloaded
    *should* mostly work without an internet connection (except for charts, METARs, and the map, which uses
    Google Maps), use for real world aviation in any way is AT YOUR OWN RISK! OpenGps is intended for
    SIMULATION PURPOSES *ONLY!*
* is NOT an FMS. It doesn't know anything about your aircraft, and can't do any of the fancy 
    calculations that those things typically will. In its current form, OpenGps is aimed more toward
    GA aircraft users. Futhermore, it:
* won't fly your plane for you. There's no obvious way for the Android app to tell FSX what waypoints
    to fly at this time—not without some extra help. This feature will likely involve some sort of 
    relay app installed on the computer running the simulator.
* won't load SIDs or STARs into the flight plan. While the [dataset][2] we're working with *does* include
    intersections for RNAV SIDs, it does *not* include the text data for SIDs that require it (such as
    the LGA5 departure out of KLGA), so this feature would inevitably feel incomplete. Since it won't 
    fly your plane for you anyway, it seems pointless to deal with RNAV SIDs and STARs (for now).
* can't work outside of the US. Our [dataset][2] does appear to include some places in Canada,
    (CYYZ airport, for example), but I wouldn't count on it working 100% anywhere outside of the US. 
    I suppose international data is what you pay for with a service like [Navigraph][1]. If anyone 
    knows of other, free datasets, please open a ticket!

### The Library

The OpenGps library provides a [Reactive][6] interface to the [free National Airspace Systems Resources (NASR)][2]
dataset provided by the FAA. It supports loading data for a specific Airac cycle, or automatically
using the most recent cycle. For more information, see [its README][4].

### RxConnectr

RxConnectr provides a simple, [Reactive][6] interface to SimConnect, supporting automatic object
mapping so you don't have to deal with defining custom types yourself—just annotate the
fields of a class with the variable name you want and the unit it should be in, and RxConnectr will
handle parsing it out for you. For more information, see [its README][5].

## Why

Flight simulation is *expensive*. Simulated versions of real GPS devices can cost $50 USD and more,
and may not even come with the latest navigation data. To get that navigation data from Garmin, for
example, can cost [hundreds of dollars][7]. A yearly [subscription to Navigraph][1] FMS data for some
payware aircraft costs almost $70 USD—much cheaper, but it's still a recurring cost. 
The FAA [publishes navigation data for the US][2] for free, so why not have a simulated GPS that uses 
that data and is also free?

OpenGps is my contribution to the flight sim community. It will always be open source, and anyone can
download that source and build a copy for free. 

## Special Thanks

- [jSimConnect 0.8][3] (LGPL) - RxConnectr dependency

[1]: https://www.navigraph.com/Subscription.aspx
[2]: https://nfdc.faa.gov/xwiki/bin/view/NFDC/56+Day+NASR+Subscription
[3]: http://lc0277.gratisim.fr/jsimconnect.html
[4]: lib/README.md
[5]: rxconnectr/README.md
[6]: https://github.com/ReactiveX/RxJava/
[7]: https://fly.garmin.com/fly-garmin/pilotpak/
