package org.seamcat.plugin;

import org.seamcat.model.plugin.propagation.*;
import org.seamcat.model.propagation.*;
import org.seamcat.model.propagation.p528.P528PropagationModel;
import org.seamcat.model.types.PropagationModel;

public class PropagationModelFactoryImpl implements PropagationModelFactory {

    @Override
    public PropagationModelConfiguration<HataInput> getHataSE21() {
        return getByClass(HataSE21PropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<HataInput> getHataSE21(HataInput input, boolean variation) {
        return getByClass(HataSE21PropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<HataInput> getHataSE24() {
        return getByClass(HataSE24PropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<HataInput> getHataSE24(HataInput input, boolean variation) {
        return getByClass(HataSE24PropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<SphericalDiffractionInput> getSphericalDiffraction() {
        return getByClass(SDPropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<SphericalDiffractionInput> getSphericalDiffraction(SphericalDiffractionInput input, boolean variation) {
        return getByClass(SDPropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<P452ver14Input> getITU_R_P_452_14() {
        return getByClass(P452ver14PropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<P452ver14Input> getITU_R_P_452_14(P452ver14Input input, boolean variation) {
        return getByClass(P452ver14PropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<FreespaceInput> getFreeSpace() {
        return getByClass(FreeSpacePropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<FreespaceInput> getFreeSpace(FreespaceInput input, boolean variation) {
        return getByClass(FreeSpacePropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<P1546ver4Input> getITU_R_P_1546_4land() {
        return getByClass(P1546ver4PropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<P1546ver4Input> getITU_R_P_1546_4land(P1546ver4Input input, boolean variation) {
        return getByClass(P1546ver4PropagationModel.class, input,variation);
    }

    @Override
    public PropagationModelConfiguration<P1546ver1Input> getITU_R_P_1546_1Annex_8() {
        return getByClass(P1546ver1PropagationModel.class);
    }

    @Override
    public PropagationModelConfiguration<P1546ver1Input> getITU_R_P_1546_1Annex_8(P1546ver1Input input, boolean variation) {
        return getByClass(P1546ver1PropagationModel.class, input, variation);
    }

    @Override
    public PropagationModel<P1411LowAntennaHeightInput> getITU_R_P_1411() {
        return getByClass(P1411LowAntennaHeight.class);
    }

    @Override
    public PropagationModel<P1411LowAntennaHeightInput> getITU_R_P_1411(P1411LowAntennaHeightInput input, boolean variations) {
        return getByClass(P1411LowAntennaHeight.class, input, variations);
    }

    @Override
    public PropagationModel<P528Input> getITU_R_P_528() {
        return getByClass(P528PropagationModel.class);
    }

    @Override
    public PropagationModel<P528Input> getITU_R_P_528(P528Input input, boolean variations) {
        return getByClass(P528PropagationModel.class, input, variations);
    }

    @Override
    public PropagationModel<LongleyRice_modInput> getLongleyRice() {
        return getByClass(LongleyRice_mod.class);
    }

    @Override
    public PropagationModel<LongleyRice_modInput> getLongleyRice(LongleyRice_modInput input, boolean variations) {
        return getByClass(LongleyRice_mod.class, input, variations);
    }

    @Override
    public PropagationModel<Model_C_IEEE_802_11_rev3_Input> getModelC_IEEE_802_11_rev3() {
        return getByClass(Model_C_IEEE_802_11_rev3.class);
    }

    @Override
    public PropagationModel<Model_C_IEEE_802_11_rev3_Input> getModelC_IEEE_802_11_rev3(Model_C_IEEE_802_11_rev3_Input input, boolean variations) {
        return getByClass(Model_C_IEEE_802_11_rev3.class, input, variations);
    }

    @Override
    public PropagationModel<JTG56Input> getJTG56() {
        return getByClass(JTG56PropagationModel.class);
    }

    @Override
    public PropagationModel<JTG56Input> getJTG56(JTG56Input input, boolean variations) {
        return getByClass(JTG56PropagationModel.class, input, variations);
    }

    @Override
    public <T> PropagationModelConfiguration<T> getByClass(Class<? extends PropagationModelPlugin<T>> clazz) {
        return new PropagationModelConfiguration<T>(clazz, null);
    }

    @Override
    public <T> PropagationModelConfiguration<T> getByClass(Class<? extends PropagationModelPlugin<T>> clazz, T input, boolean variation) {
        return getByClass(clazz).setModel(input).setVariationSelected(variation);
    }
}
