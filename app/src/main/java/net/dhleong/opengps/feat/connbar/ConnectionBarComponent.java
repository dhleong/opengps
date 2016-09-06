package net.dhleong.opengps.feat.connbar;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface ConnectionBarComponent {
    void inject(ConnectionAppBarLayout connectionAppBarLayout);
}
