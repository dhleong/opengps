package net.dhleong.opengps.nasr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ByteString;
import rx.Observable;

/**
 * SIDs with text descriptions, such as the LGA5, or the VNY3
 *  from KBUR (which also has navaid/intersection data in the
 *  transitions, but describes the initial heading in text),
 *  are not included in the regular nasr dataset. This class
 *  is an experimental utility for providing that extra
 *  information (but exactly how it should be utilized is
 *  TBD).
 *
 * This is an optional class, and requires OkHttp and Gson
 *  to be on the classpath for use
 *
 * @author dhleong
 */
public class ENasrSidFetcher {
    static final String URL_BASE = "https://enasr.faa.gov/eNASR/nasr/Current/";
    static final ByteString DATA_EQUALS = ByteString.encodeUtf8("var data = { \"main\" :");
    static final Charset UTF_8 = Charset.forName("UTF-8");

    private final OkHttpClient client;
    private final Gson gson;

    public ENasrSidFetcher() {
        this(new OkHttpClient());
    }

    public ENasrSidFetcher(OkHttpClient client) {
        this.client = client;
        this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(QueryResults.FACTORY)
            .create();
    }

    public Observable<List<String>> departureProcedureInfo(String computerCode) {
        return idForDp(computerCode).flatMap(id ->
                dataForUrl(DPInfo.class, URL_BASE + "DP/" + id)
        ).map(obj -> {
            DPInfo.Category info = obj.findCategory("Additional Information");
            final int len = info.items.size();
            ArrayList<String> result = new ArrayList<>(len);
            for (int i=0; i < len; i++) {
                result.add(info.items.get(i).value);
            }
            return result;
        });
    }

    Observable<String> idForDp(String computerCode) {
        return search("DP", DpQueryResults.class, "Computer_Code", computerCode)
            .map(results -> {
                if (results.data.isEmpty()) return null;

                return results.data.get(0).getId();
            });
    }

    <T> Observable<T> search(String kind, Class<T> type, String... params) {
        StringBuilder url = new StringBuilder(URL_BASE);
        url.append(kind)
           .append("/?");

        for (int i=0, len=params.length; i < len; i += 2) {
            url.append("%E2%80%A0")
               .append(params[i])
               .append('=')
               .append(params[i + 1]);
        }

        return dataForUrl(type, url.toString());
    }

    <T> Observable<T> dataForUrl(Class<T> type, String url) {
        return Observable.fromCallable(() -> findDataStart(
            client.newCall(
                new Request.Builder()
                    .url(url)
                    .build()
            ).execute().body()
        )).map(reader -> gson.<T>fromJson(gson.newJsonReader(reader), type));
    }

    Reader findDataStart(ResponseBody body) throws IOException {
        MediaType contentType = body.contentType();
        Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;

        BufferedSource source = body.source();
        long dataStart = source.indexOf(DATA_EQUALS);
        source.skip(dataStart + DATA_EQUALS.size());
        return new InputStreamReader(source.inputStream(), charset) {
            @Override
            public int read(char[] cbuf, int offset, int length) throws IOException {
                int read = super.read(cbuf, offset, length);
                for (int i=offset, end = offset + read - 1; i < end; i++) {
                    if (cbuf[i] == '\\' && (cbuf[i + 1] == '*' || cbuf[i + 1] == '?')) {
                        cbuf[i + 1] = '\\';
                    }
                }
                return read;
            }
        };
    }

    static class ListOf<T> {
        List<T> items;
    }

    static class QueryResults<T> {
        static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (!QueryResults.class.isAssignableFrom(type.getRawType())) {
                    return null;
                }

                TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        in.beginObject();
                        skipToKey(in, "items");

                        in.beginArray();
                        in.skipValue(); // first value describes the query box

                        in.beginObject();
                        skipToKey(in, "data");

                        in.beginArray();
                        in.skipValue(); // first result is headers

                        T result = delegate.read(new FakingJsonReader(in));

                        in.endArray();
                        in.endObject(); // data

//                        in.endArray();
//                        in.endObject(); // items

                        return result;
                    }

                    void skipToKey(JsonReader in, String key) throws IOException {
                        while (in.hasNext()) {
                            if (key.equals(in.nextName())) {
                                return;
                            }

                            in.skipValue();
                        }

                        throw new IOException("Expected key " + key + " but not found");
                    }
                };
            }
        };

        static class FakingJsonReader extends JsonReader {
            private final JsonReader delegate;

            int fakes = 3;

            public FakingJsonReader(JsonReader in) {
                super(new Reader() {
                    @Override
                    public int read(char[] cbuf, int off, int len) throws IOException {
                        return 0;
                    }

                    @Override
                    public void close() throws IOException {

                    }
                });
                this.delegate = in;
            }

            public void beginArray() throws IOException {
                if (fakes-- > 0) return;
                delegate.beginArray();
            }

            public void endArray() throws IOException {
                delegate.endArray();
            }

            public void beginObject() throws IOException {
                if (fakes-- > 0) return;
                delegate.beginObject();
            }

            public void endObject() throws IOException {
                delegate.endObject();
            }

            public boolean hasNext() throws IOException {
                return delegate.hasNext();
            }

            public JsonToken peek() throws IOException {
                switch (fakes) {
                case 3: return JsonToken.BEGIN_OBJECT;
                case 2: return JsonToken.NAME;
                case 1: return JsonToken.BEGIN_ARRAY;
                }
                return delegate.peek();
            }

            public String nextName() throws IOException {
                if (fakes-- > 0) return "data";
                return delegate.nextName();
            }

            public String nextString() throws IOException {
                return delegate.nextString();
            }

            public boolean nextBoolean() throws IOException {
                return delegate.nextBoolean();
            }

            public void nextNull() throws IOException {
                delegate.nextNull();
            }

            public double nextDouble() throws IOException {
                return delegate.nextDouble();
            }

            public long nextLong() throws IOException {
                return delegate.nextLong();
            }

            public int nextInt() throws IOException {
                return delegate.nextInt();
            }

            public void close() throws IOException {
                delegate.close();
            }

            public void skipValue() throws IOException {
                delegate.skipValue();
            }

            public String toString() {
                return delegate.toString();
            }

            public String getPath() {
                return delegate.getPath();
            }
        }

        List<T> data;

    }

    static class DpQueryResults extends QueryResults<DpQueryResults.Item> {
        static class Item {

            static final Pattern ID_PATTERN = Pattern.compile(".*href=\\./([^ ]*).*");

            String Airport_ID;

            String getId() {
                Matcher m = ID_PATTERN.matcher(Airport_ID);
                if (!m.find()) throw new IllegalStateException("Couldn't get id; raw value=" + Airport_ID);
                return m.group(1);
            }
        }
    }

    public static class DPInfo extends ListOf<DPInfo.Category> {
        public Category findCategory(String title) {
            for (int i=0, len = items.size(); i < len; i++) {
                Category cat = items.get(i);
                if (title.equals(cat.title)) {
                    return cat;
                }
            }

            throw new NoSuchElementException("No category with title `" + title + "`");
        }

        public static class Category extends ListOf<DPInfo.Value> {
            String title;
        }

        public static class Value {
            String value;
        }
    }

}
