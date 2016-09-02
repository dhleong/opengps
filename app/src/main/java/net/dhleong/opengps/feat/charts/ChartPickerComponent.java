package net.dhleong.opengps.feat.charts;

import dagger.Subcomponent;

/**
 * @author dhleong
 */
@Subcomponent(modules = ChartPickerModule.class)
public interface ChartPickerComponent {

    void inject(ChartPickerView chartPickerView);

}
