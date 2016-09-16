package net.dhleong.opengps.modules;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.connection.ConnectionConfiguration;
import net.dhleong.opengps.connection.ConnectionDelegate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.subjects.PublishSubject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dhleong
 */
public class RxChangingConnectionDelegateTest {

    PublishRelay<ConnectionConfiguration> configs;
    RxChangingConnectionDelegate conn;

    PublishSubject<String> lastStringSubs;
    ConnectionDelegate lastConnectionDelegate;

    @Before
    public void setUp() {
        lastConnectionDelegate = null;
        configs = PublishRelay.create();
        conn = new RxChangingConnectionDelegate(configs) {

            @Override
            ConnectionDelegate initConnectionFromConfig(ConnectionConfiguration config) {
                ConnectionDelegate delegate = lastConnectionDelegate = mock(ConnectionDelegate.class);
                lastStringSubs = PublishSubject.create();
                when(delegate.subscribe(String.class)).thenReturn(lastStringSubs);
                return delegate;
            }
        };
        conn.open();
    }

    @After
    public void tearDown() {
        conn.close();
    }

    @Test
    public void uninteruptedSubscription() {
        List<String> received = new ArrayList<>();
        conn.subscribe(String.class).subscribe(received::add);
        assertThat(received).isEmpty();

        configs.call(null);
        PublishSubject<String> first = lastStringSubs;
        first.onNext("1");
        assertThat(received).containsExactly("1");

        configs.call(null);
        PublishSubject<String> second = lastStringSubs;
        assertThat(second).isNotSameAs(first);
        second.onNext("2");
        assertThat(received).containsExactly("1", "2");
    }
}