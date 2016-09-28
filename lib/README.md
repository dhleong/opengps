OpenGps Lib
===========

## What

The OpenGps library provides a [Reactive][2] interface to the [free National Airspace Systems Resources (NASR)][2]
dataset provided by the FAA. It supports loading data for a specific Airac cycle, or automatically
using the most recent cycle. It is designed to be extensible, allowing you to plug-in different data sources
and storage methods.

### Data Provided

The DataSources that currently come with OpenGps Lib provide the following:

- Airports: ID, name, type, city/state, location (latitude and longitude), elevation, magnetic
    variation, and of course frequencies used at the field
- Airways: ID, and the intersections and navaids that make it up
- Charts by Airport: the name and URL of PDF charts for Airport Diagrams, instrument procedures, etc.
- Intersections: Name, location, and references to nearby navaids
- Navaids: ID, Name, frequency, type, location, and magnetic variation
- Preferred Routes: ID, altitudes, and conditions
- Route "Calculation": Build a point-to-point route between airports—adding navaids and intersections, 
    and loading in Airways, etc.—and it'll calculate "steps" that include distance and bearing
    between each point, taking into acount local magnetic variation.

## How

OpenGps Lib is not currently distributed on a central repository, but I would certainly look into that
if anyone should be interested.

Using it is straightforward with the Builder:

```java
File nasrCacheDir = // ...
File chartsCacheDir = // ...
OpenGps gps = new OpenGps.Builder()
    .addStorage(new InMemoryStorage()) // primary storage
    .addStorage(new FaaChartsStorage()) // see description below 
    .addStorage(new NasrPreferredRoutesStorage())
    .addDataSource(new NasrTextDataSource(nasrCacheDir))
    .addDataSource(new FaaChartsSource(chartsCacheDir))
    .onError(e -> /* Called when there was an error initializing the sources */)
    .build();
```

Then, OpenGps will start eagerly ensuring all your DataSources have been loaded
into storage. You can observe the progress of this with the Observable returned
by `statusUpdates()`, if you like, but there's no need to wait; you can immediately
call methods on OpenGps and they will start emitting as soon as the data is ready.

### DataSources

A `DataSource` is anything that can input data into OpenGps. Typically, they download
some dataset and read it into a `Storage`, but they can also cooperate with a custom
`Storage` to query the dataset on the fly—this is how the `FaaChartsSource` works.
It downloads a 10mb xml file from the FAA, then when the `chartsFor()` method is called
on the `FaaChartsStorage`, it directly calls a method on `FaaChartsSource` that sifts
through the XML for the relevant information. `NasrPreferredRoutesStorage` is also
an on-demand `Storage`, but the data is already downloaded as part of the `NasrTextDataSource`.

You might ask why `FaaChartsSource` (and `NasrTextDataSource` with the routes) don't just
read directly into an actual `Storage`. Mainly, it's for speed; these items are rarely
needed up-front, unlike Navaids and Fixes which are referenced by Airways, so we're skipping
the load of all that data. It's also partially an implementation detail, since we only
actually have an `InMemoryStorage` and I'm using this on Android with memory-constrained
devices. 

But let's go with the speed explanation. 

In the future we should probably add flags to these `DataSource`s so that they *can* read
into the `Storage` if desired, but for now....

### Storage

OpenGps is designed such that queries are made from data stored in some local `Storage`,
rather than directly from a `DataSource`. As mentioned above, it isn't *required*, strictly
speaking, that the `Storage` actually *store* anything—you can easily create a `Storage` 
that does just directly query a `DataSource` (or anything, for that matter)—but the API
encourages it. 

We currently only provide an `InMemoryStorage` which, as its name suggests, does not, in fact,
store the data anywhere persistent, and must be re-loaded on each use. This was originally intended 
to be used only for testing, but has proven to be fast and reliable enough for regular use.

There is a big caveat, however: everything is indexed by ID, and while I have not run into any 
collisions during my own, admittedly limited use, Airway IDs, at least, are known to be non-unique! 
Since the dataset is limited to the US currently, this may not currently be a problem, but it could 
become one in the future. If it does then we can probably refactor it to use a List off of each ID,
but we should probably just implement real database `Storage`s.

## Special Thanks

- [jSimConnect 0.8][3] ([LGPL][4]): Java library that talks to SimConnect

[1]: https://nfdc.faa.gov/xwiki/bin/view/NFDC/56+Day+NASR+Subscription
[2]: https://github.com/ReactiveX/RxJava/
[3]: http://lc0277.gratisim.fr/jsimconnect.html
[4]: https://www.gnu.org/licenses/lgpl.html
