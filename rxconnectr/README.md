RxConnectr
==========

## What

A simple, [Reactive][2] interface to [SimConnect][1], supporting automatic object mapping

## How

RxConnectr is not currently distributed on a central repository, but I would certainly look into that
if anyone should be interested.

Using it straightforward:

```java
RxConnectr conn = new RxConnectr("My SimConnect App", "192.168.1.22", 44506);

// register any data types you want to subscribe to.
//  RxConnectr will use reflection to automatically generate an
//  ObjectFactory to read data from the network into instances
//  of the object, and to prepare the data definitions for SimConnect
conn.registerObjectType(MyDataObject.class);

// You can also create a custom ObjectFactory by hand if you really want to
conn.registerObjectType(LatLngHdg.class, new LatLngHdgFactory());

// open the connection; this returns immediately and does its work on 
//  a background thread; RxConnectr will continue to automatically reconnect 
//  if the connection is ever dropped until you explicitly call #close()
conn.open();

// subscribe() to a data type Observable. By default, you'll get the data 
//  approximately every second, but there's an overload that you can specify 
//  a different period using TimeUnit. SimConnect only provides a couple 
//  different periods, so RxConnectr will pick the most appropriate one
//  based on the time period you specify, and throttle as appropriate
Subscription sub = conn.subscribe(LatLngHdg.class)
    .observeOn(/* whatever scheduler you like; it's just RxJava */)
    .subscribe(data -> {
        System.out.println(String.format("lat=%f, lng=%f hdg=%f", 
            data.lat, data.lng, data.hdg
        ));
    });

// Just send events by name. RxConnectr will handle mapping the names
//  to event ids for you. As with open(), this method returns immediately,
//  doing its work on a background thread where you don't have to worry about it
conn.sendEvent("COM_STBY_RADIO_SWAP", 0);

// Make sure to unsubscribe when you're done! as long as the Observable 
//  is subscribed to, the sim will continue to send the data; unsubscribing 
//  will cancel the data request and free up resources.
sub.unsubscribe();

// Close the connection
conn.close();
```

### Data Objects

Data objects (which you `subscribe()` to, as shown above) are just classes with 
annotated fields:

```java
public class MyDataObject {

    /*
     * Just provide the name of the SimConnect variable and the unit you want it in.
     *  RxConnectr will use the field's type to pick the most appropriate data type
     *  when binding in SimConnect, so it's up to you to make sure the primitive type 
     *  is appropriate for the data and unit.
     */
    @ConnectrField(datumName = "Plane Heading Degrees True", unit = "degrees")
    public float hdg;

    /*
     * SimConnect transmits "bool" types as 1/0 integers, but RxConnectr
     *  will translate that into a regular Java boolean for you. In addition,
     *  to this and `float` (32 bit FLOAT, shown above), RxConnectr supports 
     *  `double` (64 bit FLOAT type), `int` (32 bit INT), and `long` (64 bit INT).
     */
    @ConnectrField(datumName = "Com Transmit:1", unit = "Bool")
    public boolean comTransmit1;

    /*
     * Frequencies will be decoded for you; if the field is a float, it will be
     *  formatted as, eg 118.7f
     */
    @ConnectrField(datumName = "Com Active Frequency:1", unit = "Frequency BCD16")
    public float com1active;

    /*
     * You can also provide an int, in which case it'll be formatted as, eg 118700
     */
    @ConnectrField(datumName = "Com Active Frequency:2", unit = "Frequency BCD16")
    public int com2active;

    /*
     * Transponder must be an int, and will be formatted as eg 4327
     */
    @ConnectrField(datumName = "Transponder Code:1", unit = "BCO16")
    public int transponder;

}
```

Variable names and units are case insensitive; Microsoft provides [an exhaustive list][3].

[1]: https://msdn.microsoft.com/en-us/library/cc526983.aspx
[2]: https://github.com/ReactiveX/RxJava/
[3]: https://msdn.microsoft.com/en-us/library/cc526981.aspx
