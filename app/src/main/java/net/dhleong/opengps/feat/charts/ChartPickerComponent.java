package net.dhleong.opengps.feat.charts;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent
public interface ChartPickerComponent {

    void inject(ChartPickerView chartPickerView);

}
